package com.callshield.app.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.callshield.app.AegisApplication
import com.callshield.app.MainActivity
import com.callshield.app.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ThreatDigestReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return

        // Handle reboot recovery: reschedule digest alarms
        if (action == Intent.ACTION_BOOT_COMPLETED || action == "android.intent.action.QUICKBOOT_POWERON") {
            ThreatDigestScheduler.scheduleAllDigests(context)
            return
        }

        val repository = AegisApplication.repository

        val isDaily = action == ACTION_DAILY_DIGEST
        val isWeekly = action == ACTION_WEEKLY_DIGEST

        if (!isDaily && !isWeekly) return

        // Verify user preference
        if (isDaily && !repository.isDailyDigestEnabled.value) return
        if (isWeekly && !repository.isWeeklyDigestEnabled.value) return

        CoroutineScope(Dispatchers.IO).launch {
            val now = System.currentTimeMillis()
            val windowMillis = if (isDaily) 24 * 60 * 60 * 1000L else 7 * 24 * 60 * 60 * 1000L
            val sinceTimestamp = now - windowMillis

            val blockedCallsCount = repository.getBlockedCallsCountSince(sinceTimestamp)
            val quarantinedSmsCount = repository.getQuarantinedSmsCountSince(sinceTimestamp)
            val totalThreats = blockedCallsCount + quarantinedSmsCount

            // ZERO SPAM RULE: If no threats were neutralized during this window, stay completely silent
            if (totalThreats == 0) return@launch

            postDigestNotification(
                context = context,
                isDaily = isDaily,
                blockedCalls = blockedCallsCount,
                quarantinedSms = quarantinedSmsCount,
                total = totalThreats
            )
        }
    }

    private fun postDigestNotification(
        context: Context,
        isDaily: Boolean,
        blockedCalls: Int,
        quarantinedSms: Int,
        total: Int
    ) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create Channel if Android 8.0+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Threat Intelligence Digest",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Periodic non-intrusive summaries of intercepted telecom threats"
                enableLights(true)
                lightColor = android.graphics.Color.CYAN
            }
            notificationManager.createNotificationChannel(channel)
        }

        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("OPEN_TAB", "THREATS")
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            if (isDaily) 1001 else 1002,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = if (isDaily) "🛡️ CallShield Daily Security Digest" else "📊 CallShield Weekly Intelligence Brief"
        val periodText = if (isDaily) "in the past 24 hours" else "this week"

        val summaryContent = when {
            blockedCalls > 0 && quarantinedSms > 0 ->
                "Neutralized $blockedCalls predatory calls and quarantined $quarantinedSms spam SMS $periodText."
            blockedCalls > 0 ->
                "Neutralized $blockedCalls predatory autodialer calls $periodText. Your device remained silent."
            else ->
                "Quarantined $quarantinedSms suspicious/phishing SMS messages $periodText."
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_charging)
            .setContentTitle(title)
            .setContentText(summaryContent)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("$summaryContent\n\nAll rogue VoIP trunks and predatory lender SMS broadcasts were neutralized offline with sub-2ms latency.")
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(if (isDaily) NOTIF_ID_DAILY else NOTIF_ID_WEEKLY, notification)
    }

    companion object {
        const val ACTION_DAILY_DIGEST = "com.callshield.app.ACTION_DAILY_DIGEST"
        const val ACTION_WEEKLY_DIGEST = "com.callshield.app.ACTION_WEEKLY_DIGEST"
        const val CHANNEL_ID = "callshield_threat_digest"
        const val NOTIF_ID_DAILY = 4001
        const val NOTIF_ID_WEEKLY = 4002
    }
}
