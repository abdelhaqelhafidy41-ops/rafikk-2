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
import com.example.data.database.AppDatabase
import com.example.prayer.HijriCalendarHelper
import com.example.prayer.PrayerCalculator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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

            // Open app on tap to Prayer Tab
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("TARGET_SCREEN", "PRAYER")
            }
            val flags = PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
            val pendingIntent = PendingIntent.getActivity(context, 10, intent, flags)
            views.setOnClickPendingIntent(R.id.widget_prayer_root, pendingIntent)

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = AppDatabase.getInstance(context)
                    val settings = db.settingsDao().getSettingsSync()
                    val lat = settings?.latitude ?: 21.4225
                    val lng = settings?.longitude ?: 39.8262
                    val city = settings?.cityName ?: "مكة المكرمة"
                    val method = try {
                        PrayerCalculator.CalculationMethod.valueOf(settings?.calculationMethod ?: "")
                    } catch (_: Exception) {
                        PrayerCalculator.CalculationMethod.UMM_AL_QURA
                    }

                    val cal = Calendar.getInstance()
                    val times = PrayerCalculator.calculateTimes(
                        latitude = lat,
                        longitude = lng,
                        calendar = cal,
                        method = method
                    )

                    views.setTextViewText(R.id.widget_tv_city, "🕌 $city")
                    views.setTextViewText(R.id.widget_tv_hijri, HijriCalendarHelper.getTodayHijriString())
                    views.setTextViewText(R.id.widget_tv_next_prayer, "الصلاة القادمة: ${times.nextPrayerName} (${times.nextPrayerTime})")
                    views.setTextViewText(R.id.widget_tv_remaining, times.remainingFormatted)

                    views.setTextViewText(R.id.widget_tv_fajr, times.fajr)
                    views.setTextViewText(R.id.widget_tv_dhuhr, times.dhuhr)
                    views.setTextViewText(R.id.widget_tv_asr, times.asr)
                    views.setTextViewText(R.id.widget_tv_maghrib, times.maghrib)
                    views.setTextViewText(R.id.widget_tv_isha, times.isha)

                    // Highlight the next upcoming prayer box
                    views.setInt(R.id.widget_box_fajr, "setBackgroundResource", if (times.nextPrayerName == "الفجر") R.drawable.bg_widget_prayer_active else R.drawable.bg_widget_prayer_item)
                    views.setInt(R.id.widget_box_dhuhr, "setBackgroundResource", if (times.nextPrayerName == "الظهر") R.drawable.bg_widget_prayer_active else R.drawable.bg_widget_prayer_item)
                    views.setInt(R.id.widget_box_asr, "setBackgroundResource", if (times.nextPrayerName == "العصر") R.drawable.bg_widget_prayer_active else R.drawable.bg_widget_prayer_item)
                    views.setInt(R.id.widget_box_maghrib, "setBackgroundResource", if (times.nextPrayerName == "المغرب") R.drawable.bg_widget_prayer_active else R.drawable.bg_widget_prayer_item)
                    views.setInt(R.id.widget_box_isha, "setBackgroundResource", if (times.nextPrayerName == "العشاء") R.drawable.bg_widget_prayer_active else R.drawable.bg_widget_prayer_item)

                    appWidgetManager.updateAppWidget(appWidgetId, views)
                } catch (_: Exception) {
                    appWidgetManager.updateAppWidget(appWidgetId, views)
                }
            }
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
