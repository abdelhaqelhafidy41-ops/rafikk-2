package com.example.ui

import android.app.Application
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.PlaybackParams
import android.net.Uri
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.RafiqApplication
import com.example.audio.AudioDownloadManager
import com.example.audio.QuranAudioController
import com.example.audio.QuranAudioService
import com.example.data.adhkar.AdhkarData
import com.example.data.adhkar.DailyInspiration
import com.example.data.database.*
import com.example.data.quran.AyahItem
import com.example.data.quran.QuranData
import com.example.data.quran.RecitersData
import com.example.data.quran.SurahMeta
import com.example.location.LocationFetchResult
import com.example.location.LocationHelper
import com.example.prayer.*
import com.example.reminder.ReminderScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.util.Calendar
import java.util.TimeZone

enum class NavigationTab(val titleAr: String) {
    HOME("الرئيسية"),
    QURAN("القرآن"),
    PRAYER("المواقيت"),
    ADHKAR("الأذكار"),
    SETTINGS("الإعدادات")
}

enum class SubScreen {
    NONE,
    SURAH_READING,
    TASBEEH,
    PRIVACY,
    QIBLA,
    CITY_SELECTION,
    AUDIO_DOWNLOADS
}

sealed class LocationDialogState {
    object None : LocationDialogState()
    object ExplanationRationale : LocationDialogState()
    object GpsDisabled : LocationDialogState()
    data class NoSignal(val fallbackLat: Double, val fallbackLng: Double) : LocationDialogState()
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AppRepository = (application as RafiqApplication).repository
    val downloadManager = AudioDownloadManager(application, repository)

    private val _selectedTab = MutableStateFlow(NavigationTab.HOME)
    val selectedTab: StateFlow<NavigationTab> = _selectedTab.asStateFlow()

    private val _subScreen = MutableStateFlow(SubScreen.NONE)
    val subScreen: StateFlow<SubScreen> = _subScreen.asStateFlow()

    val settings: StateFlow<AppSettingsEntity> = repository.settings
        .filterNotNull()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AppSettingsEntity()
        )

    val reminders: StateFlow<List<ReminderEntity>> = repository.reminders
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val bookmarks: StateFlow<List<BookmarkEntity>> = repository.bookmarks
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val readingProgress: StateFlow<ReadingProgressEntity?> = repository.readingProgress
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val tasbeeh: StateFlow<TasbeehRecordEntity> = repository.tasbeeh
        .filterNotNull()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = TasbeehRecordEntity()
        )

    private val _activeSurah = MutableStateFlow<SurahMeta?>(null)
    val activeSurah: StateFlow<SurahMeta?> = _activeSurah.asStateFlow()

    private val _activeAyahs = MutableStateFlow<List<AyahItem>>(emptyList())
    val activeAyahs: StateFlow<List<AyahItem>> = _activeAyahs.asStateFlow()

    private val _quranSearchQuery = MutableStateFlow("")
    val quranSearchQuery: StateFlow<String> = _quranSearchQuery.asStateFlow()

    val filteredSurahs: StateFlow<List<SurahMeta>> = _quranSearchQuery
        .map { query -> QuranData.searchSurahs(query) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = QuranData.allSurahs
        )

    private val _showReminderDialog = MutableStateFlow<ReminderEntity?>(null)
    val showReminderDialog: StateFlow<ReminderEntity?> = _showReminderDialog.asStateFlow()

    // Madinah Quran missing app dialog
    private val _showMadinaQuranNotInstalled = MutableStateFlow(false)
    val showMadinaQuranNotInstalled: StateFlow<Boolean> = _showMadinaQuranNotInstalled.asStateFlow()

    // Location dialog state
    private val _locationDialogState = MutableStateFlow<LocationDialogState>(LocationDialogState.None)
    val locationDialogState: StateFlow<LocationDialogState> = _locationDialogState.asStateFlow()

    private val _isGpsLoading = MutableStateFlow(false)
    val isGpsLoading: StateFlow<Boolean> = _isGpsLoading.asStateFlow()

    // Audio Player State linked to Background QuranAudioController
    val isAudioPlaying: StateFlow<Boolean> = QuranAudioController.state
        .map { it.isPlaying }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val playingAyahNumber: StateFlow<Int?> = QuranAudioController.state
        .map { if (it.isPlaying || it.isLoading) it.currentAyahNumber else null }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val isAudioLoading: StateFlow<Boolean> = QuranAudioController.state
        .map { it.isLoading }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    init {
        QuranAudioController.onAyahChangedListener = { surah, ayah ->
            val meta = QuranData.allSurahs.find { it.number == surah }
            saveLastRead(surah, meta?.nameAr ?: "", ayah)
        }
    }

    fun handleBackPress(): Boolean {
        if (_subScreen.value != SubScreen.NONE) {
            _subScreen.value = SubScreen.NONE
            return true
        }
        if (_selectedTab.value != NavigationTab.HOME) {
            _selectedTab.value = NavigationTab.HOME
            return true
        }
        return false
    }

    val todayHijri: StateFlow<String> = flow {
        emit(HijriCalendarHelper.getTodayHijriString())
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = HijriCalendarHelper.getTodayHijriString()
    )

    private val _dailyInspiration = MutableStateFlow(AdhkarData.dailyInspirations.random())
    val dailyInspiration: StateFlow<DailyInspiration> = _dailyInspiration.asStateFlow()

    val prayerTimes: StateFlow<PrayerTimesResult> = settings.map { s ->
        val method = try {
            PrayerCalculator.CalculationMethod.valueOf(s.calculationMethod)
        } catch (_: Exception) {
            PrayerCalculator.CalculationMethod.UMM_AL_QURA
        }
        val juristic = try {
            PrayerCalculator.AsrJuristic.valueOf(s.asrJuristic)
        } catch (_: Exception) {
            PrayerCalculator.AsrJuristic.SHAFI
        }
        val highLat = try {
            PrayerCalculator.HighLatitudeRule.valueOf(s.highLatitudeRule)
        } catch (_: Exception) {
            PrayerCalculator.HighLatitudeRule.NONE
        }

        val cal = Calendar.getInstance()
        if (s.timezoneId.isNotBlank()) {
            try {
                cal.timeZone = TimeZone.getTimeZone(s.timezoneId)
            } catch (_: Exception) {}
        }

        PrayerCalculator.calculateTimes(
            latitude = s.latitude,
            longitude = s.longitude,
            calendar = cal,
            method = method,
            asrJuristic = juristic,
            highLatRule = highLat,
            customFajrAngle = s.customFajrAngle,
            customIshaAngle = s.customIshaAngle,
            fajrAdj = s.fajrAdjustmentMinutes,
            dhuhrAdj = s.dhuhrAdjustmentMinutes,
            asrAdj = s.asrAdjustmentMinutes,
            maghribAdj = s.maghribAdjustmentMinutes,
            ishaAdj = s.ishaAdjustmentMinutes
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PrayerCalculator.calculateTimes(21.4225, 39.8262)
    )

    // Navigation
    fun selectTab(tab: NavigationTab) {
        _selectedTab.value = tab
        _subScreen.value = SubScreen.NONE
    }

    fun openSurah(surah: SurahMeta) {
        stopAudio()
        _activeSurah.value = surah
        _activeAyahs.value = QuranData.getSurahAyahs(surah.number, settings.value.selectedReciterId, getApplication())
        _subScreen.value = SubScreen.SURAH_READING
    }

    fun openTasbeeh() {
        _subScreen.value = SubScreen.TASBEEH
    }

    fun openPrivacy() {
        _subScreen.value = SubScreen.PRIVACY
    }

    fun openQibla() {
        _subScreen.value = SubScreen.QIBLA
    }

    fun openCitySelection() {
        _subScreen.value = SubScreen.CITY_SELECTION
    }

    fun openAudioDownloads() {
        _subScreen.value = SubScreen.AUDIO_DOWNLOADS
    }

    fun closeSubScreen() {
        stopAudio()
        _subScreen.value = SubScreen.NONE
    }

    fun setQuranSearch(query: String) {
        _quranSearchQuery.value = query
    }

    // Madinah Quran App Launch
    fun onOpenMadinaQuranClicked(context: Context) {
        when (QuranData.launchMadinaQuranApp(context)) {
            QuranData.LaunchResult.OPENED -> {
                _showMadinaQuranNotInstalled.value = false
            }
            QuranData.LaunchResult.NOT_INSTALLED -> {
                _showMadinaQuranNotInstalled.value = true
            }
            QuranData.LaunchResult.FAILED -> {
                QuranData.openMadinaQuranInPlayStore(context)
            }
        }
    }

    fun dismissMadinaQuranDialog() {
        _showMadinaQuranNotInstalled.value = false
    }

    // Location & GPS Logic
    fun onGpsButtonClicked(context: Context, hasPermission: Boolean) {
        if (!hasPermission) {
            _locationDialogState.value = LocationDialogState.ExplanationRationale
        } else {
            fetchGpsLocation(context)
        }
    }

    fun dismissLocationDialog() {
        _locationDialogState.value = LocationDialogState.None
    }

    fun fetchGpsLocation(context: Context) {
        _isGpsLoading.value = true
        _locationDialogState.value = LocationDialogState.None
        viewModelScope.launch {
            val result = LocationHelper.getCurrentLocationWithTimeout(
                context = context,
                fallbackLat = settings.value.latitude,
                fallbackLng = settings.value.longitude,
                timeoutMs = 8000L
            )
            _isGpsLoading.value = false
            when (result) {
                is LocationFetchResult.Success -> {
                    repository.saveSettings(
                        settings.value.copy(
                            cityName = "موقعي الحالي (GPS)",
                            countryName = "تحديد تلقائي دقيق",
                            latitude = result.latitude,
                            longitude = result.longitude,
                            isManualLocation = false
                        )
                    )
                }
                is LocationFetchResult.GpsDisabled -> {
                    _locationDialogState.value = LocationDialogState.GpsDisabled
                }
                is LocationFetchResult.NoSignal -> {
                    _locationDialogState.value = LocationDialogState.NoSignal(result.fallbackLat, result.fallbackLng)
                }
                is LocationFetchResult.PermissionDenied -> {
                    _locationDialogState.value = LocationDialogState.ExplanationRationale
                }
                is LocationFetchResult.Error -> {
                    _locationDialogState.value = LocationDialogState.NoSignal(settings.value.latitude, settings.value.longitude)
                }
            }
        }
    }

    fun updateCity(city: CityInfo) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.saveSettings(
                settings.value.copy(
                    cityName = city.cityNameAr,
                    countryName = city.countryNameAr,
                    latitude = city.latitude,
                    longitude = city.longitude,
                    timezoneId = city.timezoneId,
                    calculationMethod = city.defaultMethod.name,
                    isManualLocation = true
                )
            )
        }
    }

    fun saveCustomCoordinates(cityName: String, countryName: String, lat: Double, lng: Double, timezoneId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.saveSettings(
                settings.value.copy(
                    cityName = cityName,
                    countryName = countryName,
                    latitude = lat,
                    longitude = lng,
                    timezoneId = timezoneId,
                    isManualLocation = true
                )
            )
        }
    }

    fun updateCalculationMethod(method: PrayerCalculator.CalculationMethod) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.saveSettings(settings.value.copy(calculationMethod = method.name))
        }
    }

    fun updateAsrJuristic(juristic: PrayerCalculator.AsrJuristic) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.saveSettings(settings.value.copy(asrJuristic = juristic.name))
        }
    }

    fun updateHighLatitudeRule(rule: PrayerCalculator.HighLatitudeRule) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.saveSettings(settings.value.copy(highLatitudeRule = rule.name))
        }
    }

    fun updateCustomAngles(fajr: Double, isha: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.saveSettings(
                settings.value.copy(
                    calculationMethod = PrayerCalculator.CalculationMethod.CUSTOM.name,
                    customFajrAngle = fajr,
                    customIshaAngle = isha
                )
            )
        }
    }

    fun updatePrayerAdjustment(prayerKey: String, diffMinutes: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val current = settings.value
            val updated = when (prayerKey) {
                "fajr" -> current.copy(fajrAdjustmentMinutes = (current.fajrAdjustmentMinutes + diffMinutes).coerceIn(-60, 60))
                "dhuhr" -> current.copy(dhuhrAdjustmentMinutes = (current.dhuhrAdjustmentMinutes + diffMinutes).coerceIn(-60, 60))
                "asr" -> current.copy(asrAdjustmentMinutes = (current.asrAdjustmentMinutes + diffMinutes).coerceIn(-60, 60))
                "maghrib" -> current.copy(maghribAdjustmentMinutes = (current.maghribAdjustmentMinutes + diffMinutes).coerceIn(-60, 60))
                "isha" -> current.copy(ishaAdjustmentMinutes = (current.ishaAdjustmentMinutes + diffMinutes).coerceIn(-60, 60))
                else -> current
            }
            repository.saveSettings(updated)
        }
    }

    fun updatePrePrayerAlert(enabled: Boolean, minutes: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.saveSettings(
                settings.value.copy(
                    prePrayerAlertEnabled = enabled,
                    prePrayerAlertMinutes = minutes
                )
            )
        }
    }

    fun updateThemeMode(mode: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.saveSettings(settings.value.copy(themeMode = mode))
        }
    }

    fun updateSelectedReciter(reciterId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.saveSettings(settings.value.copy(selectedReciterId = reciterId))
            // Reload active ayahs with new reciter
            _activeSurah.value?.let { surah ->
                _activeAyahs.value = QuranData.getSurahAyahs(surah.number, reciterId, getApplication())
            }
        }
    }

    fun updateAudioSpeed(speed: Float) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.saveSettings(settings.value.copy(audioSpeed = speed))
        }
        QuranAudioController.updateState { it.copy(speed = speed) }
    }

    fun updateRepeatMode(mode: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.saveSettings(settings.value.copy(repeatMode = mode))
        }
    }

    fun toggleWifiOnlyDownloads(wifiOnly: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.saveSettings(settings.value.copy(downloadOverWifiOnly = wifiOnly))
        }
    }

    // Reminders
    fun openNewReminderDialog() {
        _showReminderDialog.value = ReminderEntity(
            id = 0,
            title = "",
            message = "",
            type = "CUSTOM",
            intervalMinutes = 30,
            startHour = 8,
            startMinute = 0,
            endHour = 22,
            endMinute = 0
        )
    }

    fun openEditReminderDialog(reminder: ReminderEntity) {
        _showReminderDialog.value = reminder
    }

    fun closeReminderDialog() {
        _showReminderDialog.value = null
    }

    fun saveReminder(reminder: ReminderEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            val id = repository.saveReminder(reminder)
            val updated = reminder.copy(id = if (reminder.id == 0) id.toInt() else reminder.id)
            ReminderScheduler.scheduleReminder(getApplication(), updated)
        }
        closeReminderDialog()
    }

    fun toggleReminder(id: Int, isEnabled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.toggleReminder(id, isEnabled)
            val reminder = repository.getReminderById(id)
            if (reminder != null) {
                ReminderScheduler.scheduleReminder(getApplication(), reminder.copy(isEnabled = isEnabled))
            }
        }
    }

    fun deleteReminder(reminder: ReminderEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteReminder(reminder)
            ReminderScheduler.cancelReminder(getApplication(), reminder.id)
        }
    }

    fun toggleAllRemindersMuted() {
        viewModelScope.launch(Dispatchers.IO) {
            val current = settings.value.allRemindersMuted
            repository.setAllRemindersMuted(!current)
        }
    }

    // Bookmarks and Reading Progress
    fun toggleBookmark(surahNumber: Int, surahName: String, ayahNumber: Int, ayahText: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val existing = bookmarks.value.find { it.surahNumber == surahNumber && it.ayahNumber == ayahNumber }
            if (existing != null) {
                repository.removeBookmark(surahNumber, ayahNumber)
            } else {
                repository.addBookmark(surahNumber, surahName, ayahNumber, ayahText)
            }
        }
    }

    fun saveLastRead(surahNumber: Int, surahName: String, ayahNumber: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.saveReadingProgress(surahNumber, surahName, ayahNumber)
        }
    }

    fun updateQuranFontSize(size: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.saveSettings(settings.value.copy(quranFontSize = size))
        }
    }

    fun resetLocationSettings() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.saveSettings(
                settings.value.copy(
                    cityName = "مكة المكرمة",
                    countryName = "المملكة العربية السعودية",
                    latitude = 21.4225,
                    longitude = 39.8262,
                    timezoneId = "Asia/Riyadh",
                    calculationMethod = PrayerCalculator.CalculationMethod.UMM_AL_QURA.name,
                    isManualLocation = true
                )
            )
        }
    }

    fun clearAudioCache() {
        downloadManager.deleteAllDownloads()
    }

    // Tasbeeh
    fun incrementTasbeeh() {
        val current = tasbeeh.value
        val newCount = current.currentCount + 1
        val newTotal = current.totalCount + 1
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateTasbeeh(newCount, newTotal, current.dhikrText, current.targetCount)
        }
    }

    fun resetTasbeeh() {
        val current = tasbeeh.value
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateTasbeeh(0, current.totalCount, current.dhikrText, current.targetCount)
        }
    }

    fun setTasbeehPreset(text: String, target: Int = 33) {
        val current = tasbeeh.value
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateTasbeeh(0, current.totalCount, text, target)
        }
    }

    // Background Audio Playback with Foreground Service and Notification controls
    fun playAyahAudio(ayah: AyahItem) {
        QuranAudioService.startPlaying(
            context = getApplication(),
            surahNumber = ayah.surahNumber,
            ayahNumber = ayah.ayahNumber,
            reciterId = settings.value.selectedReciterId,
            speed = settings.value.audioSpeed
        )
    }

    fun playNextAyah() {
        val s = QuranAudioController.state.value
        if (s.currentAyahNumber < s.totalAyahs) {
            QuranAudioService.startPlaying(
                context = getApplication(),
                surahNumber = s.currentSurahNumber,
                ayahNumber = s.currentAyahNumber + 1,
                reciterId = s.reciterId,
                speed = s.speed
            )
        } else {
            QuranAudioService.sendControl(getApplication(), QuranAudioService.ACTION_NEXT_SURAH)
        }
    }

    fun playPreviousAyah() {
        val s = QuranAudioController.state.value
        if (s.currentAyahNumber > 1) {
            QuranAudioService.startPlaying(
                context = getApplication(),
                surahNumber = s.currentSurahNumber,
                ayahNumber = s.currentAyahNumber - 1,
                reciterId = s.reciterId,
                speed = s.speed
            )
        } else {
            QuranAudioService.sendControl(getApplication(), QuranAudioService.ACTION_PREV_SURAH)
        }
    }

    fun pauseAudio() {
        QuranAudioService.sendControl(getApplication(), QuranAudioService.ACTION_PLAY_PAUSE)
    }

    fun stopAudio() {
        QuranAudioService.sendControl(getApplication(), QuranAudioService.ACTION_STOP)
    }

    override fun onCleared() {
        super.onCleared()
        stopAudio()
    }
}
