package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.friday.FridayPreferences
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Android AppWidgetProvider for Friday Widget ("Widget يوم الجمعة").
 * Displays Hijri/Gregorian date, Al-Kahf status, Salawat counter, and deep-links.
 */
class FridayAppWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    companion object {
        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, FridayAppWidgetProvider::class.java)
            val ids = appWidgetManager.getAppWidgetIds(componentName)
            if (ids.isNotEmpty()) {
                val intent = Intent(context, FridayAppWidgetProvider::class.java).apply {
                    action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
                }
                context.sendBroadcast(intent)
            }
        }

        private fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val views = RemoteViews(context.packageName, R.layout.widget_friday)

            val isFriday = FridayPreferences.isTodayFriday()
            val isKahfRead = FridayPreferences.isKahfReadThisWeek(context)
            val salawatCount = FridayPreferences.getSalawatCount(context)
            val showSalawat = FridayPreferences.isShowSalawatOnWidget(context)

            val dateFormat = SimpleDateFormat("EEEE، d MMMM", Locale("ar"))
            val dateStr = dateFormat.format(Date())

            if (isFriday) {
                views.setTextViewText(R.id.widget_tv_friday_title, "🕌 يوم الجمعة المبارك")
                views.setTextViewText(R.id.widget_tv_friday_date, dateStr)
                if (isKahfRead) {
                    views.setTextViewText(R.id.widget_tv_friday_status, "✓ تمت قراءة سورة الكهف (تقبل الله)")
                } else {
                    views.setTextViewText(R.id.widget_tv_friday_status, "📖 نور ما بين الجمعتين: اقرأ سورة الكهف اليوم")
                }
            } else {
                val daysLeft = FridayPreferences.getDaysUntilNextFriday()
                views.setTextViewText(R.id.widget_tv_friday_title, "🕌 استعد ليوم الجمعة")
                views.setTextViewText(R.id.widget_tv_friday_date, "متبقي $daysLeft أيام")
                views.setTextViewText(R.id.widget_tv_friday_status, "«خير يوم طلعت عليه الشمس يوم الجمعة»")
            }

            if (showSalawat) {
                views.setViewVisibility(R.id.widget_tv_salawat_counter, View.VISIBLE)
                views.setTextViewText(R.id.widget_tv_salawat_counter, "الصلاة على النبي: $salawatCount ﷺ")
            } else {
                views.setViewVisibility(R.id.widget_tv_salawat_counter, View.GONE)
            }

            // Click Intent to open Surah Al-Kahf
            val kahfIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("TARGET_SCREEN", "KAHF")
            }
            val kahfPending = PendingIntent.getActivity(
                context,
                901,
                kahfIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_btn_read_kahf, kahfPending)

            // Click Intent to open Friday section
            val fridayIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("TARGET_SCREEN", "FRIDAY")
            }
            val fridayPending = PendingIntent.getActivity(
                context,
                902,
                fridayIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_btn_open_friday, fridayPending)
            views.setOnClickPendingIntent(R.id.widget_friday_root, fridayPending)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
