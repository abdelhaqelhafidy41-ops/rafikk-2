package com.example

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.quran.QuranData
import com.example.location.LocationHelper
import com.example.ui.*
import com.example.ui.theme.CairoFontFamily
import com.example.ui.theme.IslamicGold
import com.example.ui.theme.IslamicGreenPrimary
import com.example.ui.theme.RafiqAlMuslimTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleWidgetNavigation(intent)

        setContent {
            val context = LocalContext.current
            val settings by viewModel.settings.collectAsStateWithLifecycle()
            val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
            val subScreen by viewModel.subScreen.collectAsStateWithLifecycle()
            val prayerTimes by viewModel.prayerTimes.collectAsStateWithLifecycle()
            val todayHijri by viewModel.todayHijri.collectAsStateWithLifecycle()
            val dailyInspiration by viewModel.dailyInspiration.collectAsStateWithLifecycle()
            val readingProgress by viewModel.readingProgress.collectAsStateWithLifecycle()
            val reminders by viewModel.reminders.collectAsStateWithLifecycle()
            val bookmarks by viewModel.bookmarks.collectAsStateWithLifecycle()
            val activeSurah by viewModel.activeSurah.collectAsStateWithLifecycle()
            val activeAyahs by viewModel.activeAyahs.collectAsStateWithLifecycle()
            val filteredSurahs by viewModel.filteredSurahs.collectAsStateWithLifecycle()
            val quranSearchQuery by viewModel.quranSearchQuery.collectAsStateWithLifecycle()
            val tasbeeh by viewModel.tasbeeh.collectAsStateWithLifecycle()
            val showReminderDialog by viewModel.showReminderDialog.collectAsStateWithLifecycle()
            val showMadinaQuranNotInstalled by viewModel.showMadinaQuranNotInstalled.collectAsStateWithLifecycle()
            val locationDialogState by viewModel.locationDialogState.collectAsStateWithLifecycle()
            val isGpsLoading by viewModel.isGpsLoading.collectAsStateWithLifecycle()
            val isAudioPlaying by viewModel.isAudioPlaying.collectAsStateWithLifecycle()
            val isAudioLoading by viewModel.isAudioLoading.collectAsStateWithLifecycle()
            val playingAyahNumber by viewModel.playingAyahNumber.collectAsStateWithLifecycle()

            // Dark theme logic
            val isDark = when (settings.themeMode) {
                "DARK" -> true
                "LIGHT" -> false
                else -> isSystemInDarkTheme()
            }

            // Notification permission request
            val notificationPermissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission()
            ) { /* Handled gracefully */ }

            // Location permission launcher for dialog actions
            val locationPermissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestMultiplePermissions()
            ) { perms ->
                val fine = perms[Manifest.permission.ACCESS_FINE_LOCATION] == true
                val coarse = perms[Manifest.permission.ACCESS_COARSE_LOCATION] == true
                if (fine || coarse) {
                    viewModel.fetchGpsLocation(context)
                }
            }

            LaunchedEffect(Unit) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }

            val canGoBack = subScreen != SubScreen.NONE || selectedTab != NavigationTab.HOME
            androidx.activity.compose.BackHandler(enabled = canGoBack) {
                viewModel.handleBackPress()
            }

            RafiqAlMuslimTheme(darkTheme = isDark) {
                // Arabic RTL direction provider
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Scaffold(
                        contentWindowInsets = WindowInsets.safeDrawing,
                        bottomBar = {
                            if (subScreen == SubScreen.NONE) {
                                NavigationBar(
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    tonalElevation = 6.dp,
                                    modifier = Modifier
                                        .windowInsetsPadding(WindowInsets.navigationBars)
                                        .testTag("bottom_navigation_bar")
                                ) {
                                    NavigationBarItem(
                                        selected = selectedTab == NavigationTab.HOME,
                                        onClick = { viewModel.selectTab(NavigationTab.HOME) },
                                        icon = {
                                            Icon(
                                                if (selectedTab == NavigationTab.HOME) Icons.Filled.Home else Icons.Outlined.Home,
                                                contentDescription = "الرئيسية"
                                            )
                                        },
                                        label = { Text("الرئيسية", fontFamily = CairoFontFamily) },
                                        modifier = Modifier.testTag("nav_item_home")
                                    )
                                    NavigationBarItem(
                                        selected = selectedTab == NavigationTab.QURAN,
                                        onClick = { viewModel.selectTab(NavigationTab.QURAN) },
                                        icon = {
                                            Icon(
                                                if (selectedTab == NavigationTab.QURAN) Icons.Filled.MenuBook else Icons.Outlined.MenuBook,
                                                contentDescription = "القرآن"
                                            )
                                        },
                                        label = { Text("القرآن", fontFamily = CairoFontFamily) },
                                        modifier = Modifier.testTag("nav_item_quran")
                                    )
                                    NavigationBarItem(
                                        selected = selectedTab == NavigationTab.PRAYER,
                                        onClick = { viewModel.selectTab(NavigationTab.PRAYER) },
                                        icon = {
                                            Icon(
                                                if (selectedTab == NavigationTab.PRAYER) Icons.Filled.AccessTimeFilled else Icons.Outlined.AccessTime,
                                                contentDescription = "المواقيت"
                                            )
                                        },
                                        label = { Text("المواقيت", fontFamily = CairoFontFamily) },
                                        modifier = Modifier.testTag("nav_item_prayer")
                                    )
                                    NavigationBarItem(
                                        selected = selectedTab == NavigationTab.ADHKAR,
                                        onClick = { viewModel.selectTab(NavigationTab.ADHKAR) },
                                        icon = {
                                            Icon(
                                                if (selectedTab == NavigationTab.ADHKAR) Icons.Filled.WbSunny else Icons.Outlined.WbSunny,
                                                contentDescription = "الأذكار"
                                            )
                                        },
                                        label = { Text("الأذكار", fontFamily = CairoFontFamily) },
                                        modifier = Modifier.testTag("nav_item_adhkar")
                                    )
                                    NavigationBarItem(
                                        selected = selectedTab == NavigationTab.SETTINGS,
                                        onClick = { viewModel.selectTab(NavigationTab.SETTINGS) },
                                        icon = {
                                            Icon(
                                                if (selectedTab == NavigationTab.SETTINGS) Icons.Filled.Settings else Icons.Outlined.Settings,
                                                contentDescription = "الإعدادات"
                                            )
                                        },
                                        label = { Text("الإعدادات", fontFamily = CairoFontFamily) },
                                        modifier = Modifier.testTag("nav_item_settings")
                                    )
                                }
                            }
                        }
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            when (subScreen) {
                                SubScreen.SURAH_READING -> {
                                    activeSurah?.let { surah ->
                                        SurahReadingScreen(
                                            surah = surah,
                                            ayahs = activeAyahs,
                                            bookmarks = bookmarks,
                                            quranFontSize = settings.quranFontSize,
                                            selectedReciterId = settings.selectedReciterId,
                                            audioSpeed = settings.audioSpeed,
                                            repeatMode = settings.repeatMode,
                                            isAudioPlaying = isAudioPlaying,
                                            isAudioLoading = isAudioLoading,
                                            playingAyahNumber = playingAyahNumber,
                                            onBack = { viewModel.closeSubScreen() },
                                            onToggleBookmark = { surahNum, surahName, ayahNum, ayahText ->
                                                viewModel.toggleBookmark(surahNum, surahName, ayahNum, ayahText)
                                            },
                                            onSaveLastRead = { surahNum, surahName, ayahNum ->
                                                viewModel.saveLastRead(surahNum, surahName, ayahNum)
                                            },
                                            onPlayAyahAudio = { ayah -> viewModel.playAyahAudio(ayah) },
                                            onPauseAudio = { viewModel.pauseAudio() },
                                            onStopAudio = { viewModel.stopAudio() },
                                            onPlayNextAyah = { viewModel.playNextAyah() },
                                            onPlayPreviousAyah = { viewModel.playPreviousAyah() },
                                            onUpdateFontSize = { size -> viewModel.updateQuranFontSize(size) },
                                            onSelectReciter = { reciterId -> viewModel.updateSelectedReciter(reciterId) },
                                            onUpdateSpeed = { speed -> viewModel.updateAudioSpeed(speed) },
                                            onUpdateRepeatMode = { mode -> viewModel.updateRepeatMode(mode) },
                                            onOpenMadinaQuran = { viewModel.onOpenMadinaQuranClicked(context) }
                                        )
                                    }
                                }
                                SubScreen.PRIVACY -> {
                                    PrivacyScreen(
                                        onBack = { viewModel.closeSubScreen() }
                                    )
                                }
                                SubScreen.QIBLA -> {
                                    QiblaScreen(
                                        userLat = settings.latitude,
                                        userLng = settings.longitude,
                                        cityName = settings.cityName,
                                        onBack = { viewModel.closeSubScreen() }
                                    )
                                }
                                SubScreen.CITY_SELECTION -> {
                                    CitySelectionScreen(
                                        currentCityName = settings.cityName,
                                        onSelectCity = { city ->
                                            viewModel.updateCity(city)
                                            viewModel.closeSubScreen()
                                        },
                                        onSaveCustomCoordinates = { cName, coName, lat, lng, tz ->
                                            viewModel.saveCustomCoordinates(cName, coName, lat, lng, tz)
                                            viewModel.closeSubScreen()
                                        },
                                        onBack = { viewModel.closeSubScreen() }
                                    )
                                }
                                SubScreen.AUDIO_DOWNLOADS -> {
                                    AudioDownloadsScreen(
                                        downloadManager = viewModel.downloadManager,
                                        selectedReciterId = settings.selectedReciterId,
                                        downloadOverWifiOnly = settings.downloadOverWifiOnly,
                                        onSelectReciter = { id -> viewModel.updateSelectedReciter(id) },
                                        onToggleWifiOnly = { wifi -> viewModel.toggleWifiOnlyDownloads(wifi) },
                                        onBack = { viewModel.closeSubScreen() }
                                    )
                                }
                                SubScreen.TASBEEH, SubScreen.NONE -> {
                                    when (selectedTab) {
                                        NavigationTab.HOME -> {
                                            HomeScreen(
                                                settings = settings,
                                                prayerTimes = prayerTimes,
                                                todayHijri = todayHijri,
                                                dailyInspiration = dailyInspiration,
                                                readingProgress = readingProgress,
                                                reminders = reminders,
                                                onToggleMuteAll = { viewModel.toggleAllRemindersMuted() },
                                                onToggleReminder = { id, enabled -> viewModel.toggleReminder(id, enabled) },
                                                onEditReminder = { rem -> viewModel.openEditReminderDialog(rem) },
                                                onAddReminder = { viewModel.openNewReminderDialog() },
                                                onNavigateToQuran = { viewModel.selectTab(NavigationTab.QURAN) },
                                                onNavigateToPrayer = { viewModel.selectTab(NavigationTab.PRAYER) },
                                                onNavigateToAdhkar = { viewModel.selectTab(NavigationTab.ADHKAR) },
                                                onNavigateToQibla = { viewModel.openQibla() },
                                                onOpenMadinaQuran = { viewModel.onOpenMadinaQuranClicked(context) }
                                            )
                                        }
                                        NavigationTab.QURAN -> {
                                            QuranScreen(
                                                surahs = filteredSurahs,
                                                bookmarks = bookmarks,
                                                readingProgress = readingProgress,
                                                searchQuery = quranSearchQuery,
                                                onSearchChange = { q -> viewModel.setQuranSearch(q) },
                                                onSelectSurah = { surah -> viewModel.openSurah(surah) },
                                                onDeleteBookmark = { sNum, aNum -> viewModel.toggleBookmark(sNum, "", aNum, "") },
                                                onOpenMadinaQuran = { viewModel.onOpenMadinaQuranClicked(context) }
                                            )
                                        }
                                        NavigationTab.PRAYER -> {
                                            PrayerScreen(
                                                settings = settings,
                                                prayerTimes = prayerTimes,
                                                todayHijri = todayHijri,
                                                isGpsLoading = isGpsLoading,
                                                onOpenCitySelection = { viewModel.openCitySelection() },
                                                onOpenQibla = { viewModel.openQibla() },
                                                onGpsClicked = { hasPermission -> viewModel.onGpsButtonClicked(context, hasPermission) },
                                                onSelectCalculationMethod = { method -> viewModel.updateCalculationMethod(method) },
                                                onSelectAsrJuristic = { juristic -> viewModel.updateAsrJuristic(juristic) },
                                                onSelectHighLatitudeRule = { rule -> viewModel.updateHighLatitudeRule(rule) },
                                                onAdjustPrayer = { key, diff -> viewModel.updatePrayerAdjustment(key, diff) },
                                                onUpdatePrePrayerAlert = { enabled, mins -> viewModel.updatePrePrayerAlert(enabled, mins) }
                                            )
                                        }
                                        NavigationTab.ADHKAR -> {
                                            AdhkarScreen(
                                                tasbeeh = tasbeeh,
                                                onIncrementTasbeeh = { viewModel.incrementTasbeeh() },
                                                onResetTasbeeh = { viewModel.resetTasbeeh() },
                                                onSetTasbeehDhikr = { text, target -> viewModel.setTasbeehPreset(text, target) }
                                            )
                                        }
                                        NavigationTab.SETTINGS -> {
                                            SettingsScreen(
                                                settings = settings,
                                                onUpdateTheme = { mode -> viewModel.updateThemeMode(mode) },
                                                onToggleMuteAll = { viewModel.toggleAllRemindersMuted() },
                                                onNavigateToPrayer = { viewModel.selectTab(NavigationTab.PRAYER) },
                                                onOpenPrivacy = { viewModel.openPrivacy() },
                                                onOpenAddReminder = { viewModel.openNewReminderDialog() },
                                                onUpdateFontSize = { size -> viewModel.updateQuranFontSize(size) },
                                                onOpenAudioDownloads = { viewModel.openAudioDownloads() },
                                                onOpenQibla = { viewModel.openQibla() },
                                                onOpenCitySelection = { viewModel.openCitySelection() },
                                                onOpenMadinaQuran = { viewModel.onOpenMadinaQuranClicked(context) },
                                                onSelectReciter = { reciterId -> viewModel.updateSelectedReciter(reciterId) },
                                                onResetLocationSettings = { viewModel.resetLocationSettings() },
                                                onClearCache = { viewModel.clearAudioCache() }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Add / Edit Reminder Modal Dialog
                        showReminderDialog?.let { initialReminder ->
                            AddEditReminderDialog(
                                initialReminder = initialReminder,
                                onDismiss = { viewModel.closeReminderDialog() },
                                onSave = { updatedReminder -> viewModel.saveReminder(updatedReminder) }
                            )
                        }

                        // Madinah Quran Not Installed Dialog
                        if (showMadinaQuranNotInstalled) {
                            AlertDialog(
                                onDismissRequest = { viewModel.dismissMadinaQuranDialog() },
                                icon = {
                                    Icon(Icons.Filled.MenuBook, contentDescription = null, tint = IslamicGreenPrimary)
                                },
                                title = {
                                    Text("تطبيق مصحف المدينة المنورة", fontFamily = CairoFontFamily)
                                },
                                text = {
                                    Text(
                                        "تطبيق مصحف المدينة المنورة (Ayat / مجمع الملك فهد) غير مثبت حالياً على هاتفك.\n\nهل ترغب في الانتقال إلى متجر Google Play لتثبيته مجاناً والاستمتاع بقراءة المصحف بطبعة مجمع الملك فهد الرسمية؟",
                                        fontFamily = CairoFontFamily
                                    )
                                },
                                confirmButton = {
                                    Button(
                                        onClick = {
                                            viewModel.dismissMadinaQuranDialog()
                                            QuranData.openMadinaQuranInPlayStore(context)
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = IslamicGreenPrimary)
                                    ) {
                                        Text("تثبيت من متجر Play", fontFamily = CairoFontFamily)
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = { viewModel.dismissMadinaQuranDialog() }) {
                                        Text("لاحقاً", fontFamily = CairoFontFamily)
                                    }
                                }
                            )
                        }

                        // Location Dialogs
                        when (val locState = locationDialogState) {
                            is LocationDialogState.ExplanationRationale -> {
                                AlertDialog(
                                    onDismissRequest = { viewModel.dismissLocationDialog() },
                                    icon = {
                                        Icon(Icons.Filled.LocationOn, contentDescription = null, tint = IslamicGold)
                                    },
                                    title = {
                                        Text("إذن الموقع الجغرافي", fontFamily = CairoFontFamily)
                                    },
                                    text = {
                                        Text(
                                            "يحتاج تطبيق «رفيق المسلم» إلى إذن تحديد الموقع لحساب مواقيت الصلاة واتجاه القبلة بدقة متناهية لموقعك الحالي. لن يتم مشاركة موقعك مع أي طرف خارجي.",
                                            fontFamily = CairoFontFamily
                                        )
                                    },
                                    confirmButton = {
                                        Button(
                                            onClick = {
                                                viewModel.dismissLocationDialog()
                                                locationPermissionLauncher.launch(
                                                    arrayOf(
                                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                                    )
                                                )
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = IslamicGreenPrimary)
                                        ) {
                                            Text("منح الإذن", fontFamily = CairoFontFamily)
                                        }
                                    },
                                    dismissButton = {
                                        TextButton(onClick = {
                                            viewModel.dismissLocationDialog()
                                            viewModel.openCitySelection()
                                        }) {
                                            Text("اختيار المدينة يدوياً", fontFamily = CairoFontFamily)
                                        }
                                    }
                                )
                            }
                            is LocationDialogState.GpsDisabled -> {
                                AlertDialog(
                                    onDismissRequest = { viewModel.dismissLocationDialog() },
                                    icon = {
                                        Icon(Icons.Filled.LocationOff, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                                    },
                                    title = {
                                        Text("خدمة الموقع (GPS) غير مفعلة", fontFamily = CairoFontFamily)
                                    },
                                    text = {
                                        Text(
                                            "يرجى تفعيل خدمة تحديد الموقع (GPS) في إعدادات الهاتف لتحديد موقعك الجغرافي بدقة، أو يمكنك اختيار مدينتك يدوياً من قائمة المدن.",
                                            fontFamily = CairoFontFamily
                                        )
                                    },
                                    confirmButton = {
                                        Button(
                                            onClick = {
                                                viewModel.dismissLocationDialog()
                                                LocationHelper.openLocationSettings(context)
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = IslamicGreenPrimary)
                                        ) {
                                            Text("فتح إعدادات الموقع", fontFamily = CairoFontFamily)
                                        }
                                    },
                                    dismissButton = {
                                        TextButton(onClick = {
                                            viewModel.dismissLocationDialog()
                                            viewModel.openCitySelection()
                                        }) {
                                            Text("اختيار المدينة يدوياً", fontFamily = CairoFontFamily)
                                        }
                                    }
                                )
                            }
                            is LocationDialogState.NoSignal -> {
                                AlertDialog(
                                    onDismissRequest = { viewModel.dismissLocationDialog() },
                                    icon = {
                                        Icon(Icons.Filled.SatelliteAlt, contentDescription = null, tint = IslamicGold)
                                    },
                                    title = {
                                        Text("إشارة GPS ضعيفة", fontFamily = CairoFontFamily)
                                    },
                                    text = {
                                        Text(
                                            "تعذر التقاط إشارة GPS دقيقة في غضون 8 ثوانٍ (قد تكون داخل مبنى مغلق أو في منطقة محجوبة). تم الاحتفاظ بآخر إحداثيات معروفة، أو يمكنك اختيار مدينتك يدوياً بكل سهولة.",
                                            fontFamily = CairoFontFamily
                                        )
                                    },
                                    confirmButton = {
                                        Button(
                                            onClick = {
                                                viewModel.dismissLocationDialog()
                                                viewModel.openCitySelection()
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = IslamicGreenPrimary)
                                        ) {
                                            Text("اختيار المدينة يدوياً", fontFamily = CairoFontFamily)
                                        }
                                    },
                                    dismissButton = {
                                        TextButton(onClick = { viewModel.dismissLocationDialog() }) {
                                            Text("موافق", fontFamily = CairoFontFamily)
                                        }
                                    }
                                )
                            }
                            is LocationDialogState.None -> {}
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleWidgetNavigation(intent)
    }

    private fun handleWidgetNavigation(intent: Intent?) {
        val target = intent?.getStringExtra("TARGET_SCREEN") ?: return
        when (target) {
            "PRAYER" -> viewModel.selectTab(NavigationTab.PRAYER)
            "ADHKAR" -> viewModel.selectTab(NavigationTab.ADHKAR)
            "QIBLA" -> {
                viewModel.selectTab(NavigationTab.PRAYER)
                viewModel.openQibla()
            }
            "TASBEEH" -> {
                viewModel.selectTab(NavigationTab.ADHKAR)
                viewModel.openTasbeeh()
            }
            "QURAN" -> viewModel.selectTab(NavigationTab.QURAN)
        }
    }
}
