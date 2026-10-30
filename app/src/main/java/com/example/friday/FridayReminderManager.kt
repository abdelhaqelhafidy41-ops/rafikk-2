package com.example.friday

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import java.util.Calendar

/**
 * Isolated reminder scheduler and notification receiver specifically for Friday section features.
 */
object FridayReminderManager {
    const val CHANNEL_ID = "friday_reminders_channel"
    private const val CHANNEL_NAME = "تذكيرات يوم الجمعة"
    private const val CHANNEL_DESC = "إشعارات تذكيرية بقرآءة سورة الكهف، الصلاة على النبي ﷺ، وساعة الإجابة"

    const val ID_KAHF = 7001
    const val ID_SALAWAT = 7002
    const val ID_DUA = 7003
    const val ID_PREP = 7004

    const val EXTRA_TYPE = "friday_reminder_type"
    const val TYPE_KAHF = "type_kahf"
    const val TYPE_SALAWAT = "type_salawat"
    const val TYPE_DUA = "type_dua"
    const val TYPE_PREP = "type_prep"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = CHANNEL_DESC
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Schedules all enabled Friday reminders.
     */
    fun scheduleAllFridayReminders(context: Context) {
        createNotificationChannel(context)

        if (!FridayPreferences.isFridaySectionEnabled(context)) {
            cancelAllReminders(context)
            return
        }

        if (FridayPreferences.isKahfReminderEnabled(context)) {
            scheduleFridayReminder(context, ID_KAHF, TYPE_KAHF, FridayPreferences.getKahfReminderTime(context))
        } else {
            cancelReminder(context, ID_KAHF)
        }

        if (FridayPreferences.isSalawatReminderEnabled(context)) {
            scheduleFridayReminder(context, ID_SALAWAT, TYPE_SALAWAT, FridayPreferences.getSalawatReminderTime(context))
        } else {
            cancelReminder(context, ID_SALAWAT)
        }

        if (FridayPreferences.isDuaReminderEnabled(context)) {
            scheduleFridayReminder(context, ID_DUA, TYPE_DUA, FridayPreferences.getDuaReminderTime(context))
        } else {
            cancelReminder(context, ID_DUA)
        }

        if (FridayPreferences.isJumuahPrepReminderEnabled(context)) {
            scheduleFridayReminder(context, ID_PREP, TYPE_PREP, FridayPreferences.getJumuahPrepReminderTime(context))
        } else {
            cancelReminder(context, ID_PREP)
        }
    }

    /**
     * Calculates the timestamp of the next occurrence of Friday at the specified "HH:mm".
     */
    fun getNextFridayMillis(timeStr: String): Long {
        val parts = timeStr.split(":")
        val targetHour = parts.getOrNull(0)?.toIntOrNull() ?: 9
        val targetMinute = parts.getOrNull(1)?.toIntOrNull() ?: 0

        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, targetHour)
            set(Calendar.MINUTE, targetMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val now = System.currentTimeMillis()
        while (cal.get(Calendar.DAY_OF_WEEK) != Calendar.FRIDAY || cal.timeInMillis <= now) {
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        return cal.timeInMillis
    }

    private fun scheduleFridayReminder(context: Context, id: Int, type: String, timeStr: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, FridayAlarmReceiver::class.java).apply {
            putExtra(EXTRA_TYPE, type)
            action = "com.example.friday.ACTION_REMINDER_$id"
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerAt = getNextFridayMillis(timeStr)

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
            }
        } catch (_: SecurityException) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
        }
    }

    fun cancelReminder(context: Context, id: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, FridayAlarmReceiver::class.java).apply {
            action = "com.example.friday.ACTION_REMINDER_$id"
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            id,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    fun cancelAllReminders(context: Context) {
        cancelReminder(context, ID_KAHF)
        cancelReminder(context, ID_SALAWAT)
        cancelReminder(context, ID_DUA)
        cancelReminder(context, ID_PREP)
    }
}

/**
 * Handles firing the Friday notification and rescheduling for next week.
 */
class FridayAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val type = intent.getStringExtra(FridayReminderManager.EXTRA_TYPE) ?: return

        if (!FridayPreferences.isFridaySectionEnabled(context)) return

        val (id, title, message, targetScreen) = when (type) {
            FridayReminderManager.TYPE_KAHF -> {
                if (!FridayPreferences.isKahfReminderEnabled(context)) return
                Quadruple(
                    FridayReminderManager.ID_KAHF,
                    "حان وقت قراءة سورة الكهف 📖",
                    "«من قرأ سورة الكهف في يوم الجمعة أضاء له من النور ما بين الجمعتين». اقرأها الآن.",
                    "KAHF"
                )
            }
            FridayReminderManager.TYPE_SALAWAT -> {
                if (!FridayPreferences.isSalawatReminderEnabled(context)) return
                Quadruple(
                    FridayReminderManager.ID_SALAWAT,
                    "أكثروا من الصلاة على النبي ﷺ 🤍",
                    "«إن من أفضل أيامكم يوم الجمعة، فأكثروا عليّ من الصلاة فيه؛ فإن صلاتكم معروضة عليّ».",
                    "FRIDAY"
                )
            }
            FridayReminderManager.TYPE_DUA -> {
                if (!FridayPreferences.isDuaReminderEnabled(context)) return
                Quadruple(
                    FridayReminderManager.ID_DUA,
                    "ساعة الإجابة في يوم الجمعة 🤲",
                    "في يوم الجمعة ساعة لا يوافقها عبد مسلم يسأل الله شيئاً إلا أعطاه إياه. تفرغ للدعاء.",
                    "FRIDAY"
                )
            }
            FridayReminderManager.TYPE_PREP -> {
                if (!FridayPreferences.isJumuahPrepReminderEnabled(context)) return
                Quadruple(
                    FridayReminderManager.ID_PREP,
                    "الاستعداد لصلاة الجمعة المباركة 🕌",
                    "تذكر سنن الجمعة: الاغتسال، التطيب، السواك، والتبكير إلى بيت الله.",
                    "FRIDAY"
                )
            }
            else -> return
        }

        showNotification(context, id, title, message, targetScreen)

        // Reschedule for next Friday
        FridayReminderManager.scheduleAllFridayReminders(context)
    }

    private fun showNotification(context: Context, id: Int, title: String, message: String, targetScreen: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        FridayReminderManager.createNotificationChannel(context)

        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("TARGET_SCREEN", targetScreen)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            id,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val soundUri = if (FridayPreferences.isSoundEnabled(context)) {
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        } else null

        val builder = NotificationCompat.Builder(context, FridayReminderManager.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        if (soundUri != null) {
            builder.setSound(soundUri)
        } else {
            builder.setSilent(true)
        }

        if (FridayPreferences.isVibrationEnabled(context)) {
            builder.setVibrate(longArrayOf(0, 250, 150, 250))
        } else {
            builder.setVibrate(longArrayOf(0))
        }

        notificationManager.notify(id, builder.build())
    }

    private data class Quadruple(val first: Int, val second: String, val third: String, val fourth: String)
}

/**
 * Reschedules Friday alarms on device reboot.
 */
class FridayBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == Intent.ACTION_TIME_CHANGED || intent.action == Intent.ACTION_TIMEZONE_CHANGED) {
            FridayReminderManager.scheduleAllFridayReminders(context)
        }
    }
}
