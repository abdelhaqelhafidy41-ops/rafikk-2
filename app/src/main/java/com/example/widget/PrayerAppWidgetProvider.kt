package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.prayer.HijriCalendarHelper
import com.example.prayer.PrayerCalculator
import java.util.Calendar

class PrayerAppWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    companion object {
        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_prayer)

            // Open app on tap
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val flags = PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
            val pendingIntent = PendingIntent.getActivity(context, 0, intent, flags)
            views.setOnClickPendingIntent(R.id.widget_prayer_root, pendingIntent)

            try {
                val cal = Calendar.getInstance()
                // Default coordinates Makkah (or read from prefs if present)
                val times = PrayerCalculator.calculateTimes(
                    latitude = 21.4225,
                    longitude = 39.8262,
                    calendar = cal,
                    method = PrayerCalculator.CalculationMethod.UMM_AL_QURA
                )

                views.setTextViewText(R.id.widget_tv_city, "مواقيت الصلاة")
                views.setTextViewText(R.id.widget_tv_hijri, HijriCalendarHelper.getTodayHijriString())
                views.setTextViewText(R.id.widget_tv_next_prayer, "القادمة: ${times.nextPrayerName} (${times.nextPrayerTime})")
                views.setTextViewText(R.id.widget_tv_remaining, times.remainingFormatted)

                views.setTextViewText(R.id.widget_tv_fajr, times.fajr)
                views.setTextViewText(R.id.widget_tv_dhuhr, times.dhuhr)
                views.setTextViewText(R.id.widget_tv_asr, times.asr)
                views.setTextViewText(R.id.widget_tv_maghrib, times.maghrib)
                views.setTextViewText(R.id.widget_tv_isha, times.isha)
            } catch (_: Exception) {}

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val ids = appWidgetManager.getAppWidgetIds(
                ComponentName(context, PrayerAppWidgetProvider::class.java)
            )
            for (id in ids) {
                updateAppWidget(context, appWidgetManager, id)
            }
        }
    }
}
