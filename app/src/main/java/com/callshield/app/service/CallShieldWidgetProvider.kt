package com.callshield.app.service

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.callshield.app.AegisApplication
import com.callshield.app.MainActivity
import com.callshield.app.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class CallShieldWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        when (intent.action) {
            ACTION_TOGGLE_SHIELD -> {
                val repository = AegisApplication.repository
                val currentArmed = repository.isShieldArmed.value
                repository.setShieldArmed(!currentArmed)
                updateAllWidgets(context)
            }
            ACTION_UPDATE_WIDGET, AppWidgetManager.ACTION_APPWIDGET_UPDATE -> {
                updateAllWidgets(context)
            }
        }
    }

    companion object {
        const val ACTION_TOGGLE_SHIELD = "com.callshield.app.ACTION_TOGGLE_SHIELD"
        const val ACTION_UPDATE_WIDGET = "com.callshield.app.ACTION_UPDATE_WIDGET"

        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, CallShieldWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
            if (appWidgetIds.isNotEmpty()) {
                for (id in appWidgetIds) {
                    updateWidget(context, appWidgetManager, id)
                }
            }
        }

        private fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val repository = AegisApplication.repository
            val views = RemoteViews(context.packageName, R.layout.widget_call_shield)

            // Intent to open Main App
            val openAppIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val openAppPendingIntent = PendingIntent.getActivity(
                context,
                0,
                openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_root, openAppPendingIntent)

            // Intent to toggle shield
            val toggleIntent = Intent(context, CallShieldWidgetProvider::class.java).apply {
                action = ACTION_TOGGLE_SHIELD
            }
            val togglePendingIntent = PendingIntent.getBroadcast(
                context,
                100,
                toggleIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_toggle_btn, togglePendingIntent)

            val isArmed = repository.isShieldArmed.value

            if (isArmed) {
                views.setTextViewText(R.id.widget_status_badge, "GRID: ARMED")
                views.setTextColor(R.id.widget_status_badge, android.graphics.Color.parseColor("#00FF66"))
                views.setTextViewText(R.id.widget_toggle_btn, "DISARM")
                views.setInt(R.id.widget_toggle_btn, "setBackgroundResource", R.drawable.widget_btn_disarmed)
            } else {
                views.setTextViewText(R.id.widget_status_badge, "GRID: OFFLINE")
                views.setTextColor(R.id.widget_status_badge, android.graphics.Color.parseColor("#FF0055"))
                views.setTextViewText(R.id.widget_toggle_btn, "ARM GRID")
                views.setInt(R.id.widget_toggle_btn, "setBackgroundResource", R.drawable.widget_btn_armed)
            }

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val callsCount = repository.totalBlockedCount.first()
                    val smsCount = repository.totalQuarantinedSmsCount.first()

                    views.setTextViewText(R.id.widget_calls_count, callsCount.toString())
                    views.setTextViewText(R.id.widget_sms_count, smsCount.toString())

                    appWidgetManager.updateAppWidget(appWidgetId, views)
                } catch (e: Exception) {
                    appWidgetManager.updateAppWidget(appWidgetId, views)
                }
            }
        }
    }
}
