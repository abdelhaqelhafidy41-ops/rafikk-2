package com.example.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminders ORDER BY id DESC")
    fun getAllReminders(): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE isEnabled = 1")
    suspend fun getActiveReminders(): List<ReminderEntity>

    @Query("SELECT * FROM reminders WHERE id = :id LIMIT 1")
    suspend fun getReminderById(id: Int): ReminderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(reminder: ReminderEntity): Long

    @Delete
    suspend fun delete(reminder: ReminderEntity)

    @Query("UPDATE reminders SET isEnabled = :enabled WHERE id = :id")
    suspend fun setEnabled(id: Int, enabled: Boolean)

    @Query("UPDATE reminders SET lastTriggeredEpochMs = :timestamp WHERE id = :id")
    suspend fun updateLastTriggered(id: Int, timestamp: Long)
}

@Dao
interface BookmarkDao {
    @Query("SELECT * FROM bookmarks ORDER BY createdAt DESC")
    fun getAllBookmarks(): Flow<List<BookmarkEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: BookmarkEntity): Long

    @Query("DELETE FROM bookmarks WHERE surahNumber = :surahNumber AND ayahNumber = :ayahNumber")
    suspend fun deleteBookmark(surahNumber: Int, ayahNumber: Int)

    @Query("SELECT EXISTS(SELECT 1 FROM bookmarks WHERE surahNumber = :surahNumber AND ayahNumber = :ayahNumber)")
    fun isBookmarked(surahNumber: Int, ayahNumber: Int): Flow<Boolean>
}

@Dao
interface ReadingProgressDao {
    @Query("SELECT * FROM reading_progress WHERE id = 1 LIMIT 1")
    fun getProgress(): Flow<ReadingProgressEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveProgress(progress: ReadingProgressEntity)
}

@Dao
interface SettingsDao {
    @Query("SELECT * FROM app_settings WHERE id = 1 LIMIT 1")
    fun getSettings(): Flow<AppSettingsEntity?>

    @Query("SELECT * FROM app_settings WHERE id = 1 LIMIT 1")
    suspend fun getSettingsSync(): AppSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSettings(settings: AppSettingsEntity)

    @Query("UPDATE app_settings SET allRemindersMuted = :muted WHERE id = 1")
    suspend fun setAllRemindersMuted(muted: Boolean)
}

@Dao
interface TasbeehDao {
    @Query("SELECT * FROM tasbeeh_records WHERE id = 1 LIMIT 1")
    fun getTasbeeh(): Flow<TasbeehRecordEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveTasbeeh(record: TasbeehRecordEntity)
}

@Dao
interface DownloadedSurahDao {
    @Query("SELECT * FROM downloaded_surahs ORDER BY surahNumber ASC")
    fun getAllDownloadedSurahs(): Flow<List<DownloadedSurahEntity>>

    @Query("SELECT * FROM downloaded_surahs WHERE reciterId = :reciterId")
    fun getDownloadedByReciter(reciterId: String): Flow<List<DownloadedSurahEntity>>

    @Query("SELECT * FROM downloaded_surahs WHERE surahNumber = :surahNumber AND reciterId = :reciterId LIMIT 1")
    suspend fun getDownloaded(surahNumber: Int, reciterId: String): DownloadedSurahEntity?

    @Query("SELECT EXISTS(SELECT 1 FROM downloaded_surahs WHERE surahNumber = :surahNumber AND reciterId = :reciterId)")
    fun isDownloaded(surahNumber: Int, reciterId: String): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDownloaded(entity: DownloadedSurahEntity): Long

    @Query("DELETE FROM downloaded_surahs WHERE surahNumber = :surahNumber AND reciterId = :reciterId")
    suspend fun deleteDownloaded(surahNumber: Int, reciterId: String)

    @Query("DELETE FROM downloaded_surahs")
    suspend fun deleteAll()
}
