package com.callshield.app.engine

import android.util.Log
import com.callshield.app.data.local.entity.FilterRule
import com.callshield.app.data.local.entity.RuleType
import java.util.concurrent.ConcurrentHashMap

data class SubnetBurstResult(
    val isBurstTriggered: Boolean,
    val trunkPrefix: String,
    val callCountInWindow: Int,
    val adaptiveRule: FilterRule? = null
)

object AdaptiveSubnetManager {

    private const val TAG = "AdaptiveSubnetManager"

    // Maps trunk prefix -> List of call timestamps (epoch ms)
    private val trunkCallTimestamps = ConcurrentHashMap<String, MutableList<Long>>()

    /**
     * Extracts the trunk / subnet prefix from a given phone number.
     * E.g.:
     *   "02018880123" -> "+234201888"
     *   "+2342018880123" -> "+234201888"
     *   "08031234567" -> "+2348031234"
     */
    fun extractTrunkPrefix(rawNumber: String): String {
        val variants = NumberNormalizer.getNormalizedVariants(rawNumber)
        val intl = variants.international
        val digits = variants.rawDigitsOnly

        if (intl.isNotBlank() && intl.startsWith("+234")) {
            val nationalDigits = variants.national.filter { it.isDigit() }
            // If VoIP/Landline like 0201... or 02...
            if (nationalDigits.startsWith("0201") && nationalDigits.length >= 7) {
                return "+234" + nationalDigits.substring(1, 7) // e.g. +234201888
            }
            // Standard Nigerian mobile/landline with 11 digits (08031234567 -> +2348031234)
            if (nationalDigits.length >= 8) {
                return "+234" + nationalDigits.substring(1, 8)
            }
            if (intl.length >= 8) {
                return intl.substring(0, intl.length.coerceAtMost(10))
            }
        }

        // Generic fallback: strip last 3-4 digits if length >= 7
        return if (digits.length >= 7) {
            digits.substring(0, digits.length - 3)
        } else {
            digits
        }
    }

    /**
     * Records a call occurrence for the caller's trunk and checks if the burst threshold has been reached.
     * If threshold is reached, creates a new auto-expiring FilterRule.
     */
    fun recordCallAndCheckBurst(
        rawNumber: String?,
        threshold: Int = 3,
        windowMinutes: Int = 10,
        lockoutDurationHours: Int = 24
    ): SubnetBurstResult {
        if (rawNumber.isNullOrBlank()) {
            return SubnetBurstResult(isBurstTriggered = false, trunkPrefix = "", callCountInWindow = 0)
        }

        val trunk = extractTrunkPrefix(rawNumber)
        if (trunk.isBlank()) {
            return SubnetBurstResult(isBurstTriggered = false, trunkPrefix = "", callCountInWindow = 0)
        }

        val currentTime = System.currentTimeMillis()
        val windowMs = windowMinutes * 60 * 1000L
        val lockoutMs = lockoutDurationHours * 3600 * 1000L

        val timestamps = trunkCallTimestamps.compute(trunk) { _, list ->
            val updated = list ?: mutableListOf()
            // Prune timestamps older than the sliding window
            updated.removeAll { currentTime - it > windowMs }
            updated.add(currentTime)
            updated
        } ?: mutableListOf(currentTime)

        val burstCount = timestamps.size
        Log.d(TAG, "Trunk: $trunk | Burst Count: $burstCount / $threshold in $windowMinutes min")

        return if (burstCount >= threshold) {
            val expiresAt = currentTime + lockoutMs
            val rule = FilterRule(
                name = "⚡ Subnet Auto-Shield [$trunk*]",
                pattern = trunk,
                ruleType = RuleType.ADAPTIVE_SUBNET,
                description = "Quarantined rotating trunk ($burstCount calls in $windowMinutes mins). Auto-unlocks in ${lockoutDurationHours}h.",
                isEnabled = true,
                isBuiltIn = false,
                expiresAt = expiresAt,
                createdAt = currentTime
            )
            SubnetBurstResult(
                isBurstTriggered = true,
                trunkPrefix = trunk,
                callCountInWindow = burstCount,
                adaptiveRule = rule
            )
        } else {
            SubnetBurstResult(
                isBurstTriggered = false,
                trunkPrefix = trunk,
                callCountInWindow = burstCount
            )
        }
    }

    /**
     * Clears tracked burst timestamps (useful for resetting testing sandbox or cache).
     */
    fun clearCache() {
        trunkCallTimestamps.clear()
    }
}
