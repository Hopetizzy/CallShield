package com.callshield.app.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log
import com.callshield.app.AegisApplication
import com.callshield.app.data.local.entity.QuarantinedSmsRecord
import com.callshield.app.data.local.entity.SmsThreatCategory
import com.callshield.app.engine.ContactWhitelistManager
import com.callshield.app.engine.NumberNormalizer
import com.callshield.app.engine.SmsHeuristicEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SmsDefenseReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val repository = AegisApplication.repository

        // 1. Is SMS Shield Armed?
        if (!repository.isSmsShieldEnabled.value || !repository.isShieldArmed.value) {
            return
        }

        val pendingResult = goAsync()
        val scope = CoroutineScope(Dispatchers.IO)

        scope.launch {
            try {
                val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent) ?: emptyArray()
                val activeRules = repository.getActiveRules()
                val isWhitelistOn = repository.isWhitelistEnabled.value

                for (sms in messages) {
                    val sender = sms.originatingAddress ?: continue
                    val body = sms.messageBody ?: continue

                    // 2. Safety Whitelist Check (Never quarantine messages from saved contacts)
                    if (isWhitelistOn && ContactWhitelistManager.isContact(context, sender)) {
                        Log.d(TAG, "Sender $sender is in contacts whitelist. Allowing SMS.")
                        continue
                    }

                    // 3. Evaluate SMS Content & Sender Heuristics (< 1ms)
                    val evalResult = SmsHeuristicEngine.evaluate(
                        sender = sender,
                        body = body,
                        rules = activeRules
                    )

                    if (evalResult.isSpam) {
                        Log.w(TAG, "🚨 PREDATORY SMS QUARANTINED! Sender: $sender | Category: ${evalResult.threatCategory} | Reason: ${evalResult.reason}")

                        val variants = NumberNormalizer.getNormalizedVariants(sender)
                        val record = QuarantinedSmsRecord(
                            sender = sender,
                            normalizedSender = variants.international.ifEmpty { sender },
                            body = body,
                            threatCategory = evalResult.threatCategory ?: SmsThreatCategory.PREDATORY_LOAN_RECOVERY,
                            matchedKeywordOrPattern = evalResult.matchedKeywordOrPattern,
                            timestamp = System.currentTimeMillis()
                        )

                        repository.logQuarantinedSms(record)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error screening incoming SMS", e)
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        private const val TAG = "SmsDefenseReceiver"
    }
}
