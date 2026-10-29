package com.example.data.database

import kotlinx.coroutines.flow.Flow

class AppRepository(private val database: AppDatabase) {

    val reminders: Flow<List<ReminderEntity>> = database.reminderDao().getAllReminders()
    val bookmarks: Flow<List<BookmarkEntity>> = database.bookmarkDao().getAllBookmarks()
    val readingProgress: Flow<ReadingProgressEntity?> = database.readingProgressDao().getProgress()
    val settings: Flow<AppSettingsEntity?> = database.settingsDao().getSettings()
    val tasbeeh: Flow<TasbeehRecordEntity?> = database.tasbeehDao().getTasbeeh()
    val downloadedSurahs: Flow<List<DownloadedSurahEntity>> = database.downloadedSurahDao().getAllDownloadedSurahs()

    suspend fun saveReminder(reminder: ReminderEntity): Long {
        return database.reminderDao().insertOrUpdate(reminder)
    }

    suspend fun deleteReminder(reminder: ReminderEntity) {
        database.reminderDao().delete(reminder)
    }

    suspend fun toggleReminder(id: Int, isEnabled: Boolean) {
        database.reminderDao().setEnabled(id, isEnabled)
    }

    suspend fun getActiveReminders(): List<ReminderEntity> {
        return database.reminderDao().getActiveReminders()
    }

    suspend fun getReminderById(id: Int): ReminderEntity? {
        return database.reminderDao().getReminderById(id)
    }

    suspend fun addBookmark(surahNumber: Int, surahName: String, ayahNumber: Int, ayahText: String) {
        database.bookmarkDao().insertBookmark(
            BookmarkEntity(
                surahNumber = surahNumber,
                surahName = surahName,
                ayahNumber = ayahNumber,
                ayahText = ayahText
            )
        )
    }

    suspend fun removeBookmark(surahNumber: Int, ayahNumber: Int) {
        database.bookmarkDao().deleteBookmark(surahNumber, ayahNumber)
    }

    fun isBookmarked(surahNumber: Int, ayahNumber: Int): Flow<Boolean> {
        return database.bookmarkDao().isBookmarked(surahNumber, ayahNumber)
    }

    suspend fun saveReadingProgress(surahNumber: Int, surahName: String, ayahNumber: Int) {
        database.readingProgressDao().saveProgress(
            ReadingProgressEntity(
                id = 1,
                surahNumber = surahNumber,
                surahName = surahName,
                ayahNumber = ayahNumber,
                lastReadTimestamp = System.currentTimeMillis()
            )
        )
    }

    suspend fun saveSettings(settings: AppSettingsEntity) {
        database.settingsDao().saveSettings(settings)
    }

    suspend fun getSettingsSync(): AppSettingsEntity {
        return database.settingsDao().getSettingsSync() ?: AppSettingsEntity()
    }

    suspend fun setAllRemindersMuted(muted: Boolean) {
        database.settingsDao().setAllRemindersMuted(muted)
    }

    suspend fun updateTasbeeh(count: Int, total: Int, text: String, target: Int) {
        database.tasbeehDao().saveTasbeeh(
            TasbeehRecordEntity(
                id = 1,
                dhikrText = text,
                currentCount = count,
                targetCount = target,
                totalCount = total
            )
        )
    }

    suspend fun saveDownloadedSurah(surahNumber: Int, reciterId: String, localPath: String, sizeBytes: Long): Long {
        return database.downloadedSurahDao().insertDownloaded(
            DownloadedSurahEntity(
                surahNumber = surahNumber,
                reciterId = reciterId,
                localFilePath = localPath,
                fileSizeBytes = sizeBytes
            )
        )
    }

    suspend fun deleteDownloadedSurah(surahNumber: Int, reciterId: String) {
        database.downloadedSurahDao().deleteDownloaded(surahNumber, reciterId)
    }

    suspend fun getDownloadedSurah(surahNumber: Int, reciterId: String): DownloadedSurahEntity? {
        return database.downloadedSurahDao().getDownloaded(surahNumber, reciterId)
    }

    fun isSurahDownloaded(surahNumber: Int, reciterId: String): Flow<Boolean> {
        return database.downloadedSurahDao().isDownloaded(surahNumber, reciterId)
    }

    suspend fun deleteAllDownloads() {
        database.downloadedSurahDao().deleteAll()
    }

    suspend fun seedInitialDataIfEmpty() {
        val currentSettings = database.settingsDao().getSettingsSync()
        if (currentSettings == null) {
            database.settingsDao().saveSettings(AppSettingsEntity())
        }

        val activeList = database.reminderDao().getActiveReminders()
        if (activeList.isEmpty()) {
            database.reminderDao().insertOrUpdate(
                ReminderEntity(
                    title = "الصلاة على النبي ﷺ",
                    message = "اللهم صلِّ وسلم وبارك على نبينا محمد",
                    type = "SALAWAT",
                    intervalMinutes = 30,
                    startHour = 8,
                    startMinute = 0,
                    endHour = 22,
                    endMinute = 0,
                    isEnabled = true
                )
            )
            database.reminderDao().insertOrUpdate(
                ReminderEntity(
                    title = "ورد القرآن اليومي",
                    message = "﴿أَلَا بِذِكْرِ اللَّهِ تَطْمَئِنُّ الْقُلُوبُ﴾ - وقت قراءة وردك اليومي",
                    type = "QURAN",
                    intervalMinutes = 120,
                    startHour = 9,
                    startMinute = 0,
                    endHour = 21,
                    endMinute = 0,
                    isEnabled = true
                )
            )
            database.reminderDao().insertOrUpdate(
                ReminderEntity(
                    title = "الاستغفار وذكر الله",
                    message = "أستغفر الله العظيم وأتوب إليه - اترك ما يشغلك واذكر الله",
                    type = "ISTIGHFAR",
                    intervalMinutes = 60,
                    startHour = 8,
                    startMinute = 0,
                    endHour = 22,
                    endMinute = 0,
                    isEnabled = true
                )
            )
        }
    }
}
