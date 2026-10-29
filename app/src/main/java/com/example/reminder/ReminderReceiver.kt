package com.example.reminder

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.database.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val reminderId = intent.getIntExtra(ReminderScheduler.EXTRA_REMINDER_ID, -1)
        if (reminderId == -1) return

        if (action == ReminderScheduler.ACTION_SNOOZE) {
            val snoozeMinutes = intent.getIntExtra(ReminderScheduler.EXTRA_SNOOZE_MINUTES, 10)
            // Dismiss notification
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(reminderId)
            // Snooze
            ReminderScheduler.snoozeReminder(context, reminderId, snoozeMinutes)
            return
        }

        if (action == ReminderScheduler.ACTION_REMINDER) {
            CoroutineScope(Dispatchers.IO).launch {
                val db = AppDatabase.getDatabase(context)
                val settings = db.settingsDao().getSettingsSync()

                // Check if all reminders are muted
                if (settings?.allRemindersMuted == true) {
                    return@launch
                }

                val reminder = db.reminderDao().getReminderById(reminderId) ?: return@launch
                if (!reminder.isEnabled) return@launch

                // Check silent during sleep (default 23:00 to 05:00)
                if (reminder.silentDuringSleep) {
                    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
                    if (hour in 23..24 || hour in 0..4) {
                        // Reschedule for next time without firing during sleep
                        ReminderScheduler.scheduleReminder(context, reminder)
                        return@launch
                    }
                }

                // Show notification
                showNotification(context, reminder.id, reminder.title, reminder.message)

                // Update last triggered and reschedule next
                db.reminderDao().updateLastTriggered(reminder.id, System.currentTimeMillis())
                ReminderScheduler.scheduleReminder(context, reminder)
            }
        }
    }

    private fun showNotification(context: Context, reminderId: Int, title: String, message: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Content intent
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            context,
            reminderId,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Snooze 5 min action
        val snooze5Intent = Intent(context, ReminderReceiver::class.java).apply {
            action = ReminderScheduler.ACTION_SNOOZE
            putExtra(ReminderScheduler.EXTRA_REMINDER_ID, reminderId)
            putExtra(ReminderScheduler.EXTRA_SNOOZE_MINUTES, 5)
        }
        val snooze5Pending = PendingIntent.getBroadcast(
            context,
            reminderId * 10 + 1,
            snooze5Intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Snooze 10 min action
        val snooze10Intent = Intent(context, ReminderReceiver::class.java).apply {
            action = ReminderScheduler.ACTION_SNOOZE
            putExtra(ReminderScheduler.EXTRA_REMINDER_ID, reminderId)
            putExtra(ReminderScheduler.EXTRA_SNOOZE_MINUTES, 10)
        }
        val snooze10Pending = PendingIntent.getBroadcast(
            context,
            reminderId * 10 + 2,
            snooze10Intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val displayBody = if (message.isNotBlank()) message else "اترك ما يشغلك واذكر الله"

        val builder = NotificationCompat.Builder(context, ReminderScheduler.CHANNEL_REMINDERS)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(displayBody)
            .setStyle(NotificationCompat.BigTextStyle().bigText(displayBody))
            .setContentIntent(openAppPendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .addAction(0, "تأجيل ٥ دقائق", snooze5Pending)
            .addAction(0, "تأجيل ١٠ دقائق", snooze10Pending)

        notificationManager.notify(reminderId, builder.build())
    }
}
