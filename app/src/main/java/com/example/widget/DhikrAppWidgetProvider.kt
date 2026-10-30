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
import com.example.data.adhkar.AdhkarData

class DhikrAppWidgetProvider : AppWidgetProvider() {

    companion object {
        const val ACTION_CYCLE_DHIKR = "com.example.rafiqalmuslim.ACTION_CYCLE_DHIKR"
        private var currentIndex = 0

        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_dhikr)

            // Open Adhkar tab on root tap
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("TARGET_SCREEN", "ADHKAR")
            }
            val flags = PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
            val pendingIntent = PendingIntent.getActivity(context, 20, intent, flags)
            views.setOnClickPendingIntent(R.id.widget_dhikr_root, pendingIntent)

            // Cycle Dhikr PendingIntent
            val cycleIntent = Intent(context, DhikrAppWidgetProvider::class.java).apply {
                action = ACTION_CYCLE_DHIKR
            }
            val cyclePI = PendingIntent.getBroadcast(context, 101, cycleIntent, flags)
            views.setOnClickPendingIntent(R.id.widget_btn_next_dhikr, cyclePI)

            // Pick Dhikr
            val inspirations = AdhkarData.dailyInspirations
            val item = inspirations[currentIndex % inspirations.size]

            views.setTextViewText(R.id.widget_tv_dhikr_text, "﴿ ${item.quote} ﴾")
            views.setTextViewText(R.id.widget_tv_dhikr_source, "${item.source} • ${item.type}")

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_CYCLE_DHIKR) {
            currentIndex++
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val ids = appWidgetManager.getAppWidgetIds(
                ComponentName(context, DhikrAppWidgetProvider::class.java)
            )
            for (id in ids) {
                updateAppWidget(context, appWidgetManager, id)
            }
        }
    }
}
