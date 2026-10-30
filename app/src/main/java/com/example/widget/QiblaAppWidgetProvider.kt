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
import com.example.prayer.QiblaCalculator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class QiblaAppWidgetProvider : AppWidgetProvider() {

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
            val views = RemoteViews(context.packageName, R.layout.widget_qibla)

            // Open Qibla Compass screen in app on tap
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("TARGET_SCREEN", "QIBLA")
            }
            val flags = PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
            val pendingIntent = PendingIntent.getActivity(context, 30, intent, flags)

            views.setOnClickPendingIntent(R.id.widget_qibla_root, pendingIntent)
            views.setOnClickPendingIntent(R.id.widget_btn_open_qibla, pendingIntent)

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = AppDatabase.getInstance(context)
                    val settings = db.settingsDao().getSettingsSync()
                    val lat = settings?.latitude ?: 21.4225
                    val lng = settings?.longitude ?: 39.8262
                    val city = settings?.cityName ?: "مكة المكرمة"

                    val qibla = QiblaCalculator.calculateQibla(lat, lng)

                    views.setTextViewText(R.id.widget_tv_qibla_angle, "${qibla.qiblaAngle.roundToInt()}° (${qibla.directionLabelAr})")
                    views.setTextViewText(R.id.widget_tv_qibla_distance, "${qibla.distanceKm.roundToInt()} كم إلى مكة")
                    views.setTextViewText(R.id.widget_tv_qibla_city, "موقعك: $city")

                    appWidgetManager.updateAppWidget(appWidgetId, views)
                } catch (_: Exception) {
                    appWidgetManager.updateAppWidget(appWidgetId, views)
                }
            }
        }

        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val ids = appWidgetManager.getAppWidgetIds(
                ComponentName(context, QiblaAppWidgetProvider::class.java)
            )
            for (id in ids) {
                updateAppWidget(context, appWidgetManager, id)
            }
        }
    }
}
