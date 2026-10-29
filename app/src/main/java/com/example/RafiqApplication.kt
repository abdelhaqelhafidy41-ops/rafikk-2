package com.example

import android.app.Application
import com.example.data.database.AppDatabase
import com.example.data.database.AppRepository
import com.example.reminder.ReminderScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RafiqApplication : Application() {

    lateinit var repository: AppRepository
        private set

    override fun onCreate() {
        super.onCreate()
        val database = AppDatabase.getDatabase(this)
        repository = AppRepository(database)

        ReminderScheduler.createNotificationChannels(this)
        com.example.data.quran.QuranData.init(this)

        CoroutineScope(Dispatchers.IO).launch {
            com.example.data.quran.QuranData.preloadAll(this@RafiqApplication)
            repository.seedInitialDataIfEmpty()
            ReminderScheduler.rescheduleAll(this@RafiqApplication)
        }
    }
}
