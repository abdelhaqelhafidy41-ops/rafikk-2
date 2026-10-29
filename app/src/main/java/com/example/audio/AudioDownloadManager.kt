package com.example.audio

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Environment
import android.os.StatFs
import com.example.data.database.AppRepository
import com.example.data.quran.QuranData
import com.example.data.quran.RecitersData
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.concurrent.TimeUnit

sealed class SurahDownloadStatus {
    object NotDownloaded : SurahDownloadStatus()
    data class Downloading(val progressPercent: Int) : SurahDownloadStatus()
    data class Downloaded(val fileSizeBytes: Long) : SurahDownloadStatus()
    data class Failed(val errorAr: String) : SurahDownloadStatus()
}

class AudioDownloadManager(
    private val context: Context,
    private val repository: AppRepository
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val _downloadStatusMap = MutableStateFlow<Map<String, SurahDownloadStatus>>(emptyMap())
    val downloadStatusMap: StateFlow<Map<String, SurahDownloadStatus>> = _downloadStatusMap.asStateFlow()

    private val activeJobs = mutableMapOf<String, Job>()
    private val downloadScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun getAudioDir(reciterId: String): File {
        val dir = File(context.filesDir, "audio/$reciterId")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun getLocalAyahFile(reciterId: String, surahNumber: Int, ayahNumber: Int): File? {
        val dir = getAudioDir(reciterId)
        val file = File(dir, "${surahNumber}_${ayahNumber}.mp3")
        return if (file.exists() && file.length() > 1024) file else null
    }

    fun isSurahFullyDownloaded(reciterId: String, surahNumber: Int): Boolean {
        val ayahs = QuranData.getSurahAyahs(surahNumber, reciterId, context)
        if (ayahs.isEmpty()) return false
        val dir = getAudioDir(reciterId)
        for (ayah in ayahs) {
            val file = File(dir, "${surahNumber}_${ayah.ayahNumber}.mp3")
            if (!file.exists() || file.length() < 1024) {
                return false
            }
        }
        return true
    }

    fun getSurahSizeBytes(reciterId: String, surahNumber: Int): Long {
        val ayahs = QuranData.getSurahAyahs(surahNumber, reciterId, context)
        val dir = getAudioDir(reciterId)
        var total = 0L
        for (ayah in ayahs) {
            val file = File(dir, "${surahNumber}_${ayah.ayahNumber}.mp3")
            if (file.exists()) {
                total += file.length()
            }
        }
        return total
    }

    fun getAvailableStorageBytes(): Long {
        return try {
            val path = context.filesDir
            val stat = StatFs(path.path)
            stat.availableBlocksLong * stat.blockSizeLong
        } catch (_: Exception) {
            1024L * 1024L * 1024L // 1 GB fallback
        }
    }

    fun isWifiConnected(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = cm.activeNetwork ?: return false
            val cap = cm.getNetworkCapabilities(network) ?: return false
            cap.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || cap.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
        } else {
            @Suppress("DEPRECATION")
            val netInfo = cm.activeNetworkInfo
            @Suppress("DEPRECATION")
            netInfo != null && netInfo.type == ConnectivityManager.TYPE_WIFI
        }
    }

    fun downloadSurah(
        reciterId: String,
        surahNumber: Int,
        wifiOnly: Boolean,
        onComplete: (Boolean) -> Unit = {}
    ) {
        val key = "${reciterId}_$surahNumber"
        if (activeJobs[key]?.isActive == true) return

        if (wifiOnly && !isWifiConnected()) {
            updateStatus(key, SurahDownloadStatus.Failed("يتطلب التحميل اتصال Wi-Fi حسب إعداداتك"))
            onComplete(false)
            return
        }

        val job = downloadScope.launch {
            val ayahs = QuranData.getSurahAyahs(surahNumber, reciterId, context)
            val dir = getAudioDir(reciterId)
            var downloadedCount = 0
            var totalBytes = 0L

            updateStatus(key, SurahDownloadStatus.Downloading(0))

            for (ayah in ayahs) {
                if (!isActive) break

                val targetFile = File(dir, "${surahNumber}_${ayah.ayahNumber}.mp3")
                if (targetFile.exists() && targetFile.length() > 1024) {
                    downloadedCount++
                    totalBytes += targetFile.length()
                    val pct = (downloadedCount * 100) / ayahs.size
                    updateStatus(key, SurahDownloadStatus.Downloading(pct))
                    continue
                }

                val url = RecitersData.getAyahAudioUrl(reciterId, surahNumber, ayah.ayahNumber)
                var success = false

                // Retry up to 2 times
                for (attempt in 1..2) {
                    try {
                        val request = Request.Builder().url(url).build()
                        val response = client.newCall(request).execute()
                        if (response.isSuccessful) {
                            val body = response.body
                            if (body != null) {
                                val tempFile = File(dir, "${surahNumber}_${ayah.ayahNumber}.tmp")
                                val input: InputStream = body.byteStream()
                                val output = FileOutputStream(tempFile)
                                input.copyTo(output)
                                output.flush()
                                output.close()
                                input.close()

                                if (tempFile.length() > 1024) {
                                    tempFile.renameTo(targetFile)
                                    totalBytes += targetFile.length()
                                    success = true
                                    break
                                } else {
                                    tempFile.delete()
                                }
                            }
                        }
                    } catch (_: Exception) {
                        delay(500)
                    }
                }

                if (success) {
                    downloadedCount++
                    val pct = (downloadedCount * 100) / ayahs.size
                    updateStatus(key, SurahDownloadStatus.Downloading(pct))
                } else {
                    updateStatus(key, SurahDownloadStatus.Failed("تعذر تحميل الآية ${ayah.ayahNumber}"))
                    onComplete(false)
                    return@launch
                }
            }

            if (downloadedCount == ayahs.size) {
                repository.saveDownloadedSurah(surahNumber, reciterId, dir.absolutePath, totalBytes)
                updateStatus(key, SurahDownloadStatus.Downloaded(totalBytes))
                onComplete(true)
            } else {
                updateStatus(key, SurahDownloadStatus.Failed("اكتمل التحميل جزئيًا"))
                onComplete(false)
            }
        }

        activeJobs[key] = job
    }

    fun cancelDownload(reciterId: String, surahNumber: Int) {
        val key = "${reciterId}_$surahNumber"
        activeJobs[key]?.cancel()
        activeJobs.remove(key)
        updateStatus(key, SurahDownloadStatus.NotDownloaded)
    }

    fun deleteDownloadedSurah(reciterId: String, surahNumber: Int) {
        val key = "${reciterId}_$surahNumber"
        cancelDownload(reciterId, surahNumber)
        downloadScope.launch {
            val dir = getAudioDir(reciterId)
            val files = dir.listFiles { _, name -> name.startsWith("${surahNumber}_") }
            files?.forEach { it.delete() }
            repository.deleteDownloadedSurah(surahNumber, reciterId)
            updateStatus(key, SurahDownloadStatus.NotDownloaded)
        }
    }

    fun deleteAllDownloads() {
        downloadScope.launch {
            val root = File(context.filesDir, "audio")
            if (root.exists()) {
                root.deleteRecursively()
            }
            repository.deleteAllDownloads()
            _downloadStatusMap.value = emptyMap()
        }
    }

    private fun updateStatus(key: String, status: SurahDownloadStatus) {
        val current = _downloadStatusMap.value.toMutableMap()
        current[key] = status
        _downloadStatusMap.value = current
    }
}
