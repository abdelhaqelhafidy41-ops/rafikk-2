package com.example.audio

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.PlaybackParams
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.quran.QuranData
import com.example.data.quran.RecitersData
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.widget.Toast
import java.io.File
import java.io.FileInputStream

object QuranAudioController {
    data class PlaybackState(
        val isPlaying: Boolean = false,
        val isLoading: Boolean = false,
        val currentSurahNumber: Int = 1,
        val currentSurahName: String = "الفاتحة",
        val currentAyahNumber: Int = 1,
        val totalAyahs: Int = 7,
        val reciterId: String = "ar.alafasy",
        val reciterName: String = "مشاري راشد العفاسي",
        val durationMs: Int = 0,
        val currentPositionMs: Int = 0,
        val repeatMode: String = "NONE", // NONE, ONE_AYAH, SURAH
        val speed: Float = 1.0f
    )

    private val _state = MutableStateFlow(PlaybackState())
    val state: StateFlow<PlaybackState> = _state.asStateFlow()

    fun updateState(transform: (PlaybackState) -> PlaybackState) {
        _state.value = transform(_state.value)
    }

    // Callbacks to communicate between Service and ViewModel
    var onAyahChangedListener: ((surah: Int, ayah: Int) -> Unit)? = null
}

class QuranAudioService : Service(), AudioManager.OnAudioFocusChangeListener {

    companion object {
        const val CHANNEL_ID = "quran_audio_playback_channel"
        const val NOTIFICATION_ID = 2001

        const val ACTION_START_AYAH = "com.example.rafiqalmuslim.ACTION_START_AYAH"
        const val ACTION_PLAY_PAUSE = "com.example.rafiqalmuslim.ACTION_PLAY_PAUSE"
        const val ACTION_PREV_SURAH = "com.example.rafiqalmuslim.ACTION_PREV_SURAH"
        const val ACTION_NEXT_SURAH = "com.example.rafiqalmuslim.ACTION_NEXT_SURAH"
        const val ACTION_REWIND_10 = "com.example.rafiqalmuslim.ACTION_REWIND_10"
        const val ACTION_FORWARD_10 = "com.example.rafiqalmuslim.ACTION_FORWARD_10"
        const val ACTION_STOP = "com.example.rafiqalmuslim.ACTION_STOP"

        const val EXTRA_SURAH_NUM = "extra_surah_num"
        const val EXTRA_AYAH_NUM = "extra_ayah_num"
        const val EXTRA_RECITER_ID = "extra_reciter_id"
        const val EXTRA_AUDIO_URL = "extra_audio_url"
        const val EXTRA_SPEED = "extra_speed"

        fun startPlaying(
            context: Context,
            surahNumber: Int,
            ayahNumber: Int,
            reciterId: String,
            audioUrl: String? = null,
            speed: Float = 1.0f
        ) {
            try {
                val intent = Intent(context, QuranAudioService::class.java).apply {
                    action = ACTION_START_AYAH
                    putExtra(EXTRA_SURAH_NUM, surahNumber)
                    putExtra(EXTRA_AYAH_NUM, ayahNumber)
                    putExtra(EXTRA_RECITER_ID, reciterId)
                    putExtra(EXTRA_AUDIO_URL, audioUrl)
                    putExtra(EXTRA_SPEED, speed)
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (e: Throwable) {
                Log.e("QuranAudioService", "startPlaying failed: ${e.message}", e)
            }
        }

        fun sendControl(context: Context, action: String) {
            try {
                val intent = Intent(context, QuranAudioService::class.java).apply {
                    this.action = action
                }
                context.startService(intent)
            } catch (e: Throwable) {
                Log.e("QuranAudioService", "sendControl failed: ${e.message}", e)
            }
        }
    }

    private var mediaPlayer: MediaPlayer? = null
    private var audioManager: AudioManager? = null
    private var audioFocusRequest: AudioFocusRequest? = null
    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())

    override fun onCreate() {
        super.onCreate()
        audioManager = getSystemService(Context.AUDIO_SERVICE) as? AudioManager
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_AYAH -> {
                val surah = intent.getIntExtra(EXTRA_SURAH_NUM, 1)
                val ayah = intent.getIntExtra(EXTRA_AYAH_NUM, 1)
                val reciter = intent.getStringExtra(EXTRA_RECITER_ID) ?: "ar.alafasy"
                val url = intent.getStringExtra(EXTRA_AUDIO_URL)
                val speed = intent.getFloatExtra(EXTRA_SPEED, 1.0f)
                playAyah(surah, ayah, reciter, url, speed)
            }
            ACTION_PLAY_PAUSE -> togglePlayPause()
            ACTION_REWIND_10 -> rewind10()
            ACTION_FORWARD_10 -> forward10()
            ACTION_PREV_SURAH -> playPreviousSurah()
            ACTION_NEXT_SURAH -> playNextSurah()
            ACTION_STOP -> stopAndClean()
        }
        return START_NOT_STICKY
    }

    private fun playAyah(
        surahNumber: Int,
        ayahNumber: Int,
        reciterId: String,
        explicitUrl: String?,
        speed: Float
    ) {
        releasePlayer()

        val surahMeta = QuranData.allSurahs.find { it.number == surahNumber }
        val surahName = surahMeta?.nameAr ?: "سورة $surahNumber"
        val totalAyahs = surahMeta?.versesCount ?: 7
        val reciter = RecitersData.getReciterById(reciterId)

        QuranAudioController.updateState {
            it.copy(
                isPlaying = false,
                isLoading = true,
                currentSurahNumber = surahNumber,
                currentSurahName = surahName,
                currentAyahNumber = ayahNumber,
                totalAyahs = totalAyahs,
                reciterId = reciterId,
                reciterName = reciter.nameAr,
                speed = speed
            )
        }

        QuranAudioController.onAyahChangedListener?.invoke(surahNumber, ayahNumber)
        startServiceInForeground(buildNotification(isLoading = true))

        requestAudioFocus()

        serviceScope.launch(Dispatchers.IO) {
            try {
                // Find local file if available (downloaded surah or cached ayah)
                val localFile = if (!explicitUrl.isNullOrEmpty() && (explicitUrl.startsWith("/") || explicitUrl.startsWith("file://"))) {
                    val cleanPath = explicitUrl.removePrefix("file://")
                    val f = File(cleanPath)
                    if (f.exists() && f.length() > 512) f else findLocalAudioFile(reciterId, surahNumber, ayahNumber)
                } else {
                    findLocalAudioFile(reciterId, surahNumber, ayahNumber)
                }

                val isLocal = localFile != null && localFile.exists() && localFile.length() > 512
                val isOnline = isNetworkConnected()

                if (!isLocal && !isOnline) {
                    Log.w("QuranAudioService", "Offline: Surah $surahNumber Ayah $ayahNumber has not been downloaded.")
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            applicationContext,
                            "هذه السورة غير محملة وتتطلب اتصالاً بالإنترنت للاستماع",
                            Toast.LENGTH_LONG
                        ).show()
                        QuranAudioController.updateState { it.copy(isPlaying = false, isLoading = false) }
                        updateNotification()
                    }
                    return@launch
                }

                val finalDataSource = if (isLocal) {
                    localFile!!.absolutePath
                } else {
                    explicitUrl ?: RecitersData.getAyahAudioUrl(reciterId, surahNumber, ayahNumber)
                }

                withContext(Dispatchers.Main) {
                    mediaPlayer = MediaPlayer().apply {
                        setAudioAttributes(
                            AudioAttributes.Builder()
                                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                                .setUsage(AudioAttributes.USAGE_MEDIA)
                                .build()
                        )

                        if (isLocal) {
                            val fis = FileInputStream(File(finalDataSource))
                            setDataSource(fis.fd)
                            fis.close()
                        } else {
                            setDataSource(finalDataSource)
                        }

                        setOnPreparedListener { mp ->
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                try {
                                    mp.playbackParams = mp.playbackParams.setSpeed(speed)
                                } catch (_: Exception) {}
                            }
                            mp.start()
                            QuranAudioController.updateState {
                                it.copy(
                                    isPlaying = true,
                                    isLoading = false,
                                    durationMs = mp.duration,
                                    currentPositionMs = 0
                                )
                            }
                            updateNotification()
                        }

                        setOnCompletionListener {
                            onCurrentAyahCompleted()
                        }

                        setOnErrorListener { _, what, extra ->
                            Log.e("QuranAudioService", "MediaPlayer error: what=$what extra=$extra")
                            QuranAudioController.updateState { it.copy(isPlaying = false, isLoading = false) }
                            updateNotification()
                            true
                        }

                        prepareAsync()
                    }
                }
            } catch (e: Exception) {
                Log.e("QuranAudioService", "playAyah failed: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    QuranAudioController.updateState { it.copy(isPlaying = false, isLoading = false) }
                    updateNotification()
                }
            }
        }
    }

    private fun findLocalAudioFile(reciterId: String, surahNumber: Int, ayahNumber: Int): File? {
        // Priority 1: Primary path from AudioDownloadManager: filesDir/audio/$reciterId/${surahNumber}_${ayahNumber}.mp3
        val path1 = File(filesDir, "audio/$reciterId/${surahNumber}_${ayahNumber}.mp3")
        if (path1.exists() && path1.length() > 512) return path1

        // Priority 2: Padded 3-digit everyAyah format: filesDir/audio/$reciterId/001001.mp3
        val padSurah = surahNumber.toString().padStart(3, '0')
        val padAyah = ayahNumber.toString().padStart(3, '0')
        val path2 = File(filesDir, "audio/$reciterId/$padSurah$padAyah.mp3")
        if (path2.exists() && path2.length() > 512) return path2

        // Priority 3: Subdirectory format: filesDir/audio/$reciterId/$surahNumber/${ayahNumber}.mp3
        val path3 = File(filesDir, "audio/$reciterId/$surahNumber/${ayahNumber}.mp3")
        if (path3.exists() && path3.length() > 512) return path3

        // Priority 4: Legacy cache paths
        val path4 = File(filesDir, "audio_cache/$reciterId/$surahNumber/${ayahNumber}.mp3")
        if (path4.exists() && path4.length() > 512) return path4

        val path5 = File(filesDir, "audio_cache/$reciterId/${surahNumber}_${ayahNumber}.mp3")
        if (path5.exists() && path5.length() > 512) return path5

        // Priority 5: Fallback if downloaded under another reciter
        val audioRoot = File(filesDir, "audio")
        if (audioRoot.exists() && audioRoot.isDirectory) {
            val reciterDirs = audioRoot.listFiles() ?: emptyArray()
            for (dir in reciterDirs) {
                if (dir.isDirectory) {
                    val candidate = File(dir, "${surahNumber}_${ayahNumber}.mp3")
                    if (candidate.exists() && candidate.length() > 512) {
                        return candidate
                    }
                    val candidatePadded = File(dir, "$padSurah$padAyah.mp3")
                    if (candidatePadded.exists() && candidatePadded.length() > 512) {
                        return candidatePadded
                    }
                }
            }
        }

        return null
    }

    private fun isNetworkConnected(): Boolean {
        return try {
            val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val network = cm.activeNetwork ?: return false
                val capabilities = cm.getNetworkCapabilities(network) ?: return false
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            } else {
                @Suppress("DEPRECATION")
                val activeInfo = cm.activeNetworkInfo
                @Suppress("DEPRECATION")
                activeInfo != null && activeInfo.isConnected
            }
        } catch (_: Exception) {
            true
        }
    }

    private fun togglePlayPause() {
        val mp = mediaPlayer ?: return
        if (mp.isPlaying) {
            mp.pause()
            QuranAudioController.updateState { it.copy(isPlaying = false) }
        } else {
            requestAudioFocus()
            mp.start()
            QuranAudioController.updateState { it.copy(isPlaying = true) }
        }
        updateNotification()
    }

    private fun rewind10() {
        val mp = mediaPlayer ?: return
        try {
            val newPos = (mp.currentPosition - 10000).coerceAtLeast(0)
            mp.seekTo(newPos)
            QuranAudioController.updateState { it.copy(currentPositionMs = newPos) }
        } catch (_: Exception) {}
    }

    private fun forward10() {
        val mp = mediaPlayer ?: return
        try {
            val newPos = (mp.currentPosition + 10000).coerceAtMost(mp.duration)
            mp.seekTo(newPos)
            QuranAudioController.updateState { it.copy(currentPositionMs = newPos) }
        } catch (_: Exception) {}
    }

    private fun playNextSurah() {
        val currentSurah = QuranAudioController.state.value.currentSurahNumber
        val nextSurah = if (currentSurah < 114) currentSurah + 1 else 1
        playAyah(
            surahNumber = nextSurah,
            ayahNumber = 1,
            reciterId = QuranAudioController.state.value.reciterId,
            explicitUrl = null,
            speed = QuranAudioController.state.value.speed
        )
    }

    private fun playPreviousSurah() {
        val currentSurah = QuranAudioController.state.value.currentSurahNumber
        val prevSurah = if (currentSurah > 1) currentSurah - 1 else 114
        playAyah(
            surahNumber = prevSurah,
            ayahNumber = 1,
            reciterId = QuranAudioController.state.value.reciterId,
            explicitUrl = null,
            speed = QuranAudioController.state.value.speed
        )
    }

    private fun onCurrentAyahCompleted() {
        val s = QuranAudioController.state.value
        when (s.repeatMode) {
            "ONE_AYAH" -> {
                playAyah(s.currentSurahNumber, s.currentAyahNumber, s.reciterId, null, s.speed)
            }
            "SURAH" -> {
                if (s.currentAyahNumber < s.totalAyahs) {
                    playAyah(s.currentSurahNumber, s.currentAyahNumber + 1, s.reciterId, null, s.speed)
                } else {
                    // Loop back to ayah 1
                    playAyah(s.currentSurahNumber, 1, s.reciterId, null, s.speed)
                }
            }
            else -> {
                if (s.currentAyahNumber < s.totalAyahs) {
                    playAyah(s.currentSurahNumber, s.currentAyahNumber + 1, s.reciterId, null, s.speed)
                } else {
                    // Finished surah: advance to next surah
                    if (s.currentSurahNumber < 114) {
                        playAyah(s.currentSurahNumber + 1, 1, s.reciterId, null, s.speed)
                    } else {
                        stopAndClean()
                    }
                }
            }
        }
    }

    private fun buildNotification(isLoading: Boolean = false): android.app.Notification {
        val s = QuranAudioController.state.value
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentPendingIntent = PendingIntent.getActivity(
            this, 0, openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        val prevSurahPI = buildServicePI(ACTION_PREV_SURAH, 1)
        val rew10PI = buildServicePI(ACTION_REWIND_10, 2)
        val playPausePI = buildServicePI(ACTION_PLAY_PAUSE, 3)
        val fwd10PI = buildServicePI(ACTION_FORWARD_10, 4)
        val nextSurahPI = buildServicePI(ACTION_NEXT_SURAH, 5)
        val stopPI = buildServicePI(ACTION_STOP, 6)

        val title = "سورة ${s.currentSurahName} (الآية ${s.currentAyahNumber}/${s.totalAyahs})"
        val subtitle = "${s.reciterName} • رفيق المسلم"
        val playPauseIcon = if (s.isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentTitle(title)
            .setContentText(if (isLoading) "جاري تحميل التلاوة..." else subtitle)
            .setSubText("تلاوة القرآن الكريم")
            .setContentIntent(contentPendingIntent)
            .setOngoing(s.isPlaying)
            .setOnlyAlertOnce(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .addAction(android.R.drawable.ic_media_previous, "السابقة", prevSurahPI)
            .addAction(android.R.drawable.ic_media_rew, "-10ث", rew10PI)
            .addAction(playPauseIcon, if (s.isPlaying) "إيقاف" else "تشغيل", playPausePI)
            .addAction(android.R.drawable.ic_media_ff, "+10ث", fwd10PI)
            .addAction(android.R.drawable.ic_media_next, "التالية", nextSurahPI)

        try {
            // Custom compact view (Islamic colors, play/pause, +/-10s, title, close)
            val collapsedView = RemoteViews(packageName, R.layout.notification_quran_player).apply {
                setTextViewText(R.id.notif_tv_title, "سورة ${s.currentSurahName} • الآية ${s.currentAyahNumber}")
                setTextViewText(R.id.notif_tv_reciter, if (isLoading) "جاري التحميل..." else "${s.reciterName} • رفيق المسلم")
                setImageViewResource(R.id.notif_btn_play_pause, playPauseIcon)
                setOnClickPendingIntent(R.id.notif_btn_rewind, rew10PI)
                setOnClickPendingIntent(R.id.notif_btn_play_pause, playPausePI)
                setOnClickPendingIntent(R.id.notif_btn_forward, fwd10PI)
                setOnClickPendingIntent(R.id.notif_btn_close, stopPI)
                setOnClickPendingIntent(R.id.notif_root, contentPendingIntent)
            }

            // Custom expanded view (all 5 player controls, large gold play button, reciter & surah info)
            val expandedView = RemoteViews(packageName, R.layout.notification_quran_player_expanded).apply {
                setTextViewText(R.id.notif_expanded_tv_app_title, "رفيق المسلم • تلاوة القرآن الكريم")
                setTextViewText(R.id.notif_expanded_tv_title, "سورة ${s.currentSurahName} • الآية ${s.currentAyahNumber} من ${s.totalAyahs}")
                setTextViewText(R.id.notif_expanded_tv_reciter, if (isLoading) "جاري التحميل..." else s.reciterName)
                setImageViewResource(R.id.notif_expanded_btn_play_pause, playPauseIcon)
                setOnClickPendingIntent(R.id.notif_expanded_btn_prev, prevSurahPI)
                setOnClickPendingIntent(R.id.notif_expanded_btn_rewind, rew10PI)
                setOnClickPendingIntent(R.id.notif_expanded_btn_play_pause, playPausePI)
                setOnClickPendingIntent(R.id.notif_expanded_btn_forward, fwd10PI)
                setOnClickPendingIntent(R.id.notif_expanded_btn_next, nextSurahPI)
                setOnClickPendingIntent(R.id.notif_expanded_btn_close, stopPI)
                setOnClickPendingIntent(R.id.notif_expanded_root, contentPendingIntent)
            }

            builder.setCustomContentView(collapsedView)
            builder.setCustomBigContentView(expandedView)
            builder.setStyle(NotificationCompat.DecoratedCustomViewStyle())
        } catch (e: Throwable) {
            Log.e("QuranAudioService", "Error binding RemoteViews: ${e.message}", e)
            builder.setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("سورة ${s.currentSurahName} • الآية ${s.currentAyahNumber} من ${s.totalAyahs}\nالقارئ: ${s.reciterName}")
            )
        }

        return builder.build()
    }

    private fun startServiceInForeground(notification: android.app.Notification) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(
                    NOTIFICATION_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
                )
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
        } catch (e: Throwable) {
            Log.e("QuranAudioService", "Error in startForeground: ${e.message}", e)
        }
    }

    private fun updateNotification() {
        try {
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            nm?.notify(NOTIFICATION_ID, buildNotification())
        } catch (e: Throwable) {
            Log.e("QuranAudioService", "updateNotification failed: ${e.message}", e)
        }
    }

    private fun buildServicePI(action: String, requestCode: Int): PendingIntent {
        val intent = Intent(this, QuranAudioService::class.java).apply {
            this.action = action
        }
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        return PendingIntent.getService(this, requestCode, intent, flags)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "تشغيل القرآن الكريم"
            val desc = "إشعارات التحكم في تلاوة القرآن الكريم بالخلفية"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = desc
                setShowBadge(false)
            }
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            nm?.createNotificationChannel(channel)
        }
    }

    private fun requestAudioFocus() {
        val am = audioManager ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build()
                )
                .setOnAudioFocusChangeListener(this)
                .build()
            audioFocusRequest?.let { am.requestAudioFocus(it) }
        } else {
            @Suppress("DEPRECATION")
            am.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN)
        }
    }

    override fun onAudioFocusChange(focusChange: Int) {
        when (focusChange) {
            AudioManager.AUDIOFOCUS_LOSS -> {
                mediaPlayer?.pause()
                QuranAudioController.updateState { it.copy(isPlaying = false) }
                updateNotification()
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT,
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                mediaPlayer?.pause()
                QuranAudioController.updateState { it.copy(isPlaying = false) }
                updateNotification()
            }
            AudioManager.AUDIOFOCUS_GAIN -> {
                // Resume if needed
            }
        }
    }

    private fun releasePlayer() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (_: Exception) {}
        mediaPlayer = null
    }

    private fun stopAndClean() {
        releasePlayer()
        QuranAudioController.updateState { it.copy(isPlaying = false, isLoading = false) }
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        releasePlayer()
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
