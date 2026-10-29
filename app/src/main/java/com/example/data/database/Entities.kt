package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reminders")
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val message: String = "",
    val type: String, // QURAN, PRAYER, ISTIGHFAR, SALAWAT, ATHKAR, CUSTOM, GENERAL
    val intervalMinutes: Int, // 5, 10, 15, 30, 60, 120, etc.
    val startHour: Int = 8,
    val startMinute: Int = 0,
    val endHour: Int = 22,
    val endMinute: Int = 0,
    val daysOfWeek: String = "1,2,3,4,5,6,7", // 1=Sun .. 7=Sat
    val silentDuringSleep: Boolean = true,
    val silentDuringPrayer: Boolean = true,
    val isEnabled: Boolean = true,
    val lastTriggeredEpochMs: Long = 0L
)

@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val surahNumber: Int,
    val surahName: String,
    val ayahNumber: Int,
    val ayahText: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "reading_progress")
data class ReadingProgressEntity(
    @PrimaryKey
    val id: Int = 1,
    val surahNumber: Int = 1,
    val surahName: String = "الفاتحة",
    val ayahNumber: Int = 1,
    val lastReadTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "app_settings")
data class AppSettingsEntity(
    @PrimaryKey
    val id: Int = 1,
    val cityName: String = "مكة المكرمة",
    val countryName: String = "المملكة العربية السعودية",
    val latitude: Double = 21.4225,
    val longitude: Double = 39.8262,
    val timezoneId: String = "Asia/Riyadh",
    val isManualLocation: Boolean = true,
    val calculationMethod: String = "UMM_AL_QURA", // UMM_AL_QURA, MWL, EGYPTIAN, KARACHI, CUSTOM
    val asrJuristic: String = "SHAFI", // SHAFI, HANAFI
    val highLatitudeRule: String = "NONE", // NONE, MIDNIGHT, SEVENTH_NIGHT, ANGLE_BASED
    val customFajrAngle: Double = 18.0,
    val customIshaAngle: Double = 17.0,
    val fajrAdjustmentMinutes: Int = 0,
    val dhuhrAdjustmentMinutes: Int = 0,
    val asrAdjustmentMinutes: Int = 0,
    val maghribAdjustmentMinutes: Int = 0,
    val ishaAdjustmentMinutes: Int = 0,
    val prePrayerAlertMinutes: Int = 10,
    val adhanAlertEnabled: Boolean = true,
    val prePrayerAlertEnabled: Boolean = true,
    val themeMode: String = "SYSTEM", // SYSTEM, LIGHT, DARK
    val quranFontSize: Int = 26,
    val allRemindersMuted: Boolean = false,
    val selectedReciterId: String = "alafasy",
    val downloadOverWifiOnly: Boolean = true,
    val audioSpeed: Float = 1.0f,
    val autoAdvanceSurah: Boolean = true,
    val repeatMode: String = "NONE", // NONE, ONE_AYAH, SURAH
    val keepAudioPlayingInBackground: Boolean = true
)

@Entity(tableName = "tasbeeh_records")
data class TasbeehRecordEntity(
    @PrimaryKey
    val id: Int = 1,
    val dhikrText: String = "سبحان الله وبحمده",
    val currentCount: Int = 0,
    val targetCount: Int = 33,
    val totalCount: Int = 0
)

@Entity(tableName = "downloaded_surahs")
data class DownloadedSurahEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val surahNumber: Int,
    val reciterId: String,
    val localFilePath: String,
    val fileSizeBytes: Long,
    val downloadedAt: Long = System.currentTimeMillis()
)
