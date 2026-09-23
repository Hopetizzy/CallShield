package com.callshield.app.service

import android.os.Build
import android.telecom.Call
import android.telecom.CallScreeningService
import android.telecom.Connection
import android.telecom.TelecomManager
import android.util.Log
import com.callshield.app.AegisApplication
import com.callshield.app.data.local.entity.BlockedCallRecord
import com.callshield.app.engine.AdaptiveSubnetManager
import com.callshield.app.engine.ContactWhitelistManager
import com.callshield.app.engine.HeuristicPatternEngine
import com.callshield.app.engine.NumberNormalizer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CallDefenseScreeningService : CallScreeningService() {

    private val serviceScope = CoroutineScope(Dispatchers.IO)

    override fun onScreenCall(callDetails: Call.Details) {
        val repository = AegisApplication.repository

        // 1. Is Shield Armed?
        if (!repository.isShieldArmed.value) {
            respondAllow(callDetails)
            return
        }

        // 2. Extract Caller Number & Presentation
        val handleUri = callDetails.handle
        val rawNumber = handleUri?.schemeSpecificPart?.let { NumberNormalizer.sanitize(it) }
        val callerVerificationFailed = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R &&
                callDetails.callerNumberVerificationStatus == Connection.VERIFICATION_STATUS_FAILED

        val isRestricted = callerVerificationFailed ||
                           callDetails.callerDisplayNamePresentation == TelecomManager.PRESENTATION_RESTRICTED ||
                           callDetails.handlePresentation == TelecomManager.PRESENTATION_RESTRICTED ||
                           rawNumber.isNullOrBlank()

        serviceScope.launch {
            try {
                // 3. Contact Whitelist Check (Safety first: Never block known contacts)
                if (repository.isWhitelistEnabled.value && !rawNumber.isNullOrBlank()) {
                    if (ContactWhitelistManager.isContact(applicationContext, rawNumber)) {
                        Log.d(TAG, "Caller is in contacts whitelist. Allowing call: $rawNumber")
                        respondAllow(callDetails)
                        return@launch
                    }
                }

                // 4. Check for Subnet/Trunk Burst Attacks (Adaptive Range Lockout)
                if (repository.isAutoSubnetShieldEnabled.value && !rawNumber.isNullOrBlank()) {
                    val burstResult = AdaptiveSubnetManager.recordCallAndCheckBurst(
                        rawNumber = rawNumber,
                        threshold = repository.burstThreshold.value,
                        windowMinutes = repository.burstWindowMinutes.value,
                        lockoutDurationHours = repository.lockoutDurationHours.value
                    )
                    if (burstResult.isBurstTriggered && burstResult.adaptiveRule != null) {
                        Log.w(TAG, "⚡ BURST ATTACK DETECTED! Locking out trunk: ${burstResult.trunkPrefix}")
                        repository.addRule(burstResult.adaptiveRule)
                    }
                }

                // 5. Retrieve Active Rules from Local SQLite
                val activeRules = repository.getActiveRules()
                val blockPrivate = repository.blockPrivateNumbers.value

                // 6. Run Ultra-Fast Heuristic Engine (< 2ms)
                val evalResult = HeuristicPatternEngine.evaluate(
                    rawNumber = rawNumber,
                    rules = activeRules,
                    isPrivateOrRestricted = isRestricted,
                    blockPrivate = blockPrivate
                )

                if (evalResult.isBlocked) {
                    Log.i(TAG, "🚨 THREAT NEUTRALIZED! Number: $rawNumber | Reason: ${evalResult.reason}")
                    
                    // Silent Drop Response to Android Telecom
                    respondDrop(callDetails)

                    // Log audit event to database
                    val variants = NumberNormalizer.getNormalizedVariants(rawNumber)
                    repository.logBlockedCall(
                        BlockedCallRecord(
                            rawNumber = rawNumber ?: "PRIVATE_NUMBER",
                            normalizedNumber = variants.international.ifEmpty { "UNKNOWN" },
                            matchedRuleName = evalResult.matchedRule?.name ?: "Private/Restricted Number Filter",
                            matchedPattern = evalResult.matchedRule?.pattern ?: "RESTRICTED",
                            isPrivateNumber = isRestricted,
                            interceptionLatencyMs = evalResult.evaluationTimeMs,
                            timestamp = System.currentTimeMillis()
                        )
                    )

                    // Increment rule hit counter
                    evalResult.matchedRule?.let { rule ->
                        repository.incrementRuleMatch(rule.id)
                    }
                } else {
                    respondAllow(callDetails)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error screening call", e)
                respondAllow(callDetails) // Fail-safe: allow legitimate call if error occurs
            }
        }
    }

    private fun respondDrop(callDetails: Call.Details) {
        val responseBuilder = CallResponse.Builder()
            .setDisallowCall(true)        // Block call
            .setRejectCall(true)          // Terminate/hang up immediately
            .setSkipNotification(true)    // Do not show ringing/missed call popup
            .setSkipCallLog(false)        // Keep in call log if user desires, or set true to completely hide

        respondToCall(callDetails, responseBuilder.build())
    }

    private fun respondAllow(callDetails: Call.Details) {
        val responseBuilder = CallResponse.Builder()
            .setDisallowCall(false)
            .setRejectCall(false)
            .setSkipNotification(false)

        respondToCall(callDetails, responseBuilder.build())
    }

    companion object {
        private const val TAG = "CallDefenseService"
    }
}
