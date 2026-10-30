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
import com.example.data.database.TasbeehRecordEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class TasbeehAppWidgetProvider : AppWidgetProvider() {

    companion object {
        const val ACTION_INCREMENT_TASBEEH = "com.example.rafiqalmuslim.ACTION_INCREMENT_TASBEEH"
        const val ACTION_RESET_TASBEEH = "com.example.rafiqalmuslim.ACTION_RESET_TASBEEH"

        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_tasbeeh)

            // Open Tasbeeh screen in app on card tap
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("TARGET_SCREEN", "TASBEEH")
            }
            val flags = PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
            val pendingIntent = PendingIntent.getActivity(context, 40, intent, flags)
            views.setOnClickPendingIntent(R.id.widget_tasbeeh_root, pendingIntent)

            // Increment action
            val incIntent = Intent(context, TasbeehAppWidgetProvider::class.java).apply {
                action = ACTION_INCREMENT_TASBEEH
            }
            val incPI = PendingIntent.getBroadcast(context, 102, incIntent, flags)
            views.setOnClickPendingIntent(R.id.widget_btn_tasbeeh_increment, incPI)

            // Reset action
            val resetIntent = Intent(context, TasbeehAppWidgetProvider::class.java).apply {
                action = ACTION_RESET_TASBEEH
            }
            val resetPI = PendingIntent.getBroadcast(context, 103, resetIntent, flags)
            views.setOnClickPendingIntent(R.id.widget_btn_tasbeeh_reset, resetPI)

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = AppDatabase.getInstance(context)
                    val record = db.tasbeehDao().getTasbeeh().firstOrNull() ?: TasbeehRecordEntity()

                    views.setTextViewText(R.id.widget_tv_tasbeeh_dhikr, record.dhikrText)
                    views.setTextViewText(R.id.widget_tv_tasbeeh_count, "${record.currentCount}")
                    views.setTextViewText(R.id.widget_tv_tasbeeh_target, "الهدف: ${record.targetCount}")

                    appWidgetManager.updateAppWidget(appWidgetId, views)
                } catch (_: Exception) {
                    appWidgetManager.updateAppWidget(appWidgetId, views)
                }
            }
        }

        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val ids = appWidgetManager.getAppWidgetIds(
                ComponentName(context, TasbeehAppWidgetProvider::class.java)
            )
            for (id in ids) {
                updateAppWidget(context, appWidgetManager, id)
            }
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
        when (intent.action) {
            ACTION_INCREMENT_TASBEEH -> {
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val db = AppDatabase.getInstance(context)
                        val record = db.tasbeehDao().getTasbeeh().firstOrNull() ?: TasbeehRecordEntity()
                        val newCount = record.currentCount + 1
                        val newTotal = record.totalCount + 1
                        db.tasbeehDao().saveTasbeeh(record.copy(currentCount = newCount, totalCount = newTotal))
                        updateAllWidgets(context)
                    } catch (_: Exception) {}
                }
            }
            ACTION_RESET_TASBEEH -> {
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val db = AppDatabase.getInstance(context)
                        val record = db.tasbeehDao().getTasbeeh().firstOrNull() ?: TasbeehRecordEntity()
                        db.tasbeehDao().saveTasbeeh(record.copy(currentCount = 0))
                        updateAllWidgets(context)
                    } catch (_: Exception) {}
                }
            }
        }
    }
}
