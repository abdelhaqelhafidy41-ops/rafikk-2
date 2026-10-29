package com.example.reminder

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.data.database.AppDatabase
import com.example.data.database.ReminderEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

object ReminderScheduler {

    const val CHANNEL_REMINDERS = "rafiq_reminders_channel"
    const val CHANNEL_PRAYER = "rafiq_prayer_channel"

    const val ACTION_REMINDER = "com.example.rafiqalmuslim.ACTION_REMINDER"
    const val ACTION_SNOOZE = "com.example.rafiqalmuslim.ACTION_SNOOZE"

    const val EXTRA_REMINDER_ID = "extra_reminder_id"
    const val EXTRA_SNOOZE_MINUTES = "extra_snooze_minutes"

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val reminderChannel = NotificationChannel(
                CHANNEL_REMINDERS,
                "تذكيرات رفيق المسلم",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "قناة إشعارات التذكيرات الإسلامية والأذكار"
                enableVibration(true)
            }

            val prayerChannel = NotificationChannel(
                CHANNEL_PRAYER,
                "مواقيت الصلاة والأذان",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "قناة إشعارات دخول وقت الصلاة والتنبيه قبلها"
                enableVibration(true)
            }

            notificationManager.createNotificationChannel(reminderChannel)
            notificationManager.createNotificationChannel(prayerChannel)
        }
    }

    fun scheduleReminder(context: Context, reminder: ReminderEntity) {
        if (!reminder.isEnabled) {
            cancelReminder(context, reminder.id)
            return
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val nextTriggerTime = calculateNextTriggerTime(reminder)

        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = ACTION_REMINDER
            putExtra(EXTRA_REMINDER_ID, reminder.id)
        }

        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        val pendingIntent = PendingIntent.getBroadcast(context, reminder.id, intent, flags)

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    nextTriggerTime,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    nextTriggerTime,
                    pendingIntent
                )
            }
        } catch (e: SecurityException) {
            // Fallback for devices restricting exact alarms
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                nextTriggerTime,
                pendingIntent
            )
        }
    }

    fun snoozeReminder(context: Context, reminderId: Int, minutes: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val triggerTime = System.currentTimeMillis() + (minutes * 60 * 1000L)

        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = ACTION_REMINDER
            putExtra(EXTRA_REMINDER_ID, reminderId)
        }

        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        val pendingIntent = PendingIntent.getBroadcast(context, reminderId, intent, flags)

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            }
        } catch (e: Exception) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
        }
    }

    fun cancelReminder(context: Context, reminderId: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = ACTION_REMINDER
            putExtra(EXTRA_REMINDER_ID, reminderId)
        }
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        val pendingIntent = PendingIntent.getBroadcast(context, reminderId, intent, flags)
        alarmManager.cancel(pendingIntent)
    }

    fun rescheduleAll(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            val db = AppDatabase.getDatabase(context)
            val reminders = db.reminderDao().getActiveReminders()
            for (reminder in reminders) {
                scheduleReminder(context, reminder)
            }
        }
    }

    private fun calculateNextTriggerTime(reminder: ReminderEntity): Long {
        val now = Calendar.getInstance()
        val candidate = Calendar.getInstance()

        // Interval in ms
        val intervalMs = (reminder.intervalMinutes.coerceAtLeast(1)) * 60 * 1000L
        candidate.timeInMillis = now.timeInMillis + intervalMs

        // Check if inside active time window (startHour:startMinute to endHour:endMinute)
        val candidateHour = candidate.get(Calendar.HOUR_OF_DAY)
        val candidateMinute = candidate.get(Calendar.MINUTE)
        val candidateMinutes = candidateHour * 60 + candidateMinute

        val startMinutes = reminder.startHour * 60 + reminder.startMinute
        val endMinutes = reminder.endHour * 60 + reminder.endMinute

        if (endMinutes > startMinutes) {
            if (candidateMinutes < startMinutes) {
                candidate.set(Calendar.HOUR_OF_DAY, reminder.startHour)
                candidate.set(Calendar.MINUTE, reminder.startMinute)
                candidate.set(Calendar.SECOND, 0)
            } else if (candidateMinutes > endMinutes) {
                // Move to tomorrow at start time
                candidate.add(Calendar.DAY_OF_YEAR, 1)
                candidate.set(Calendar.HOUR_OF_DAY, reminder.startHour)
                candidate.set(Calendar.MINUTE, reminder.startMinute)
                candidate.set(Calendar.SECOND, 0)
            }
        }

        // Adjust for allowed days of week
        val allowedDays = reminder.daysOfWeek.split(",").mapNotNull { it.trim().toIntOrNull() }
        if (allowedDays.isNotEmpty()) {
            for (i in 0..7) {
                val dayOfWeek = candidate.get(Calendar.DAY_OF_WEEK)
                if (allowedDays.contains(dayOfWeek)) {
                    break
                }
                candidate.add(Calendar.DAY_OF_YEAR, 1)
                candidate.set(Calendar.HOUR_OF_DAY, reminder.startHour)
                candidate.set(Calendar.MINUTE, reminder.startMinute)
                candidate.set(Calendar.SECOND, 0)
            }
        }

        return candidate.timeInMillis
    }
}
