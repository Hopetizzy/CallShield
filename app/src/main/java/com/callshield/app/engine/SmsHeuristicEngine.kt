package com.callshield.app.engine

import com.callshield.app.data.local.entity.FilterRule
import com.callshield.app.data.local.entity.SmsThreatCategory
import kotlin.system.measureTimeMillis

data class SmsEvaluationResult(
    val isSpam: Boolean,
    val threatCategory: SmsThreatCategory? = null,
    val matchedKeywordOrPattern: String = "",
    val reason: String = "Legitimate Message",
    val evaluationTimeMs: Long = 0
)

object SmsHeuristicEngine {

    // Pre-compiled keyword regex sets for Nigerian fintech / predatory loan spam
    private val DEFAMATION_KEYWORDS = listOf(
        "post your picture", "post your photo", "contact your family", "broadcast to your contact",
        "broadcast to contacts", "message your contact", "whatsapp contact", "public shame",
        "expose you", "fraudster notice", "call your guarantor", "contact your parents",
        "contact your employer", "defame", "embarrass you", "ruin your reputation"
    )

    private val BVN_REGULATORY_KEYWORDS = listOf(
        "bvn block", "bvn blacklist", "bvn freeze", "freeze your account", "block your account",
        "nibss blacklist", "blacklist on nibss", "credit bureau blacklist", "efcc", "police station",
        "court summon", "warrant of arrest", "legal litigation", "criminal breach", "central bank blacklist",
        "law enforcement agent", "charge you to court", "police arrest"
    )

    private val RECOVERY_SPAM_KEYWORDS = listOf(
        "overdue loan", "loan is overdue", "repay immediately", "repay today", "pay your debt",
        "settle your debt", "defaulted loan", "recovery agent assigned", "field agent visiting",
        "field recovery officer", "final demand notice", "legal recovery team", "penalties accumulating",
        "unpaid balance", "loan repayment reminder", "outstanding loan balance"
    )

    private val SUSPICIOUS_ALPHANUMERIC_SENDERS = listOf(
        "LOAN", "CREDIT", "CASH", "KASH", "PAY", "NAIRA", "QUICK", "FAST", "MONEY"
    )

    /**
     * Evaluates incoming SMS text and sender offline in sub-milliseconds (< 1ms).
     */
    fun evaluate(
        sender: String?,
        body: String?,
        rules: List<FilterRule>
    ): SmsEvaluationResult {
        var result: SmsEvaluationResult
        val timeMs = measureTimeMillis {
            result = evaluateInternal(sender, body, rules)
        }
        return result.copy(evaluationTimeMs = timeMs)
    }

    private fun evaluateInternal(
        sender: String?,
        body: String?,
        rules: List<FilterRule>
    ): SmsEvaluationResult {
        val safeSender = sender?.trim() ?: ""
        val safeBody = body?.trim() ?: ""

        if (safeSender.isBlank() && safeBody.isBlank()) {
            return SmsEvaluationResult(isSpam = false, reason = "Empty message")
        }

        val lowerBody = safeBody.lowercase()
        val upperSender = safeSender.uppercase()

        // 1. Check Defamation & Contact Harassment threats (Highest severity)
        for (kw in DEFAMATION_KEYWORDS) {
            if (lowerBody.contains(kw)) {
                return SmsEvaluationResult(
                    isSpam = true,
                    threatCategory = SmsThreatCategory.DEFAMATION_HARASSMENT,
                    matchedKeywordOrPattern = kw,
                    reason = "Defamation / Contact Harassment Threat detected: '$kw'"
                )
            }
        }

        // 2. Check Fake BVN Freeze / Regulatory scare tactics
        for (kw in BVN_REGULATORY_KEYWORDS) {
            if (lowerBody.contains(kw)) {
                return SmsEvaluationResult(
                    isSpam = true,
                    threatCategory = SmsThreatCategory.BVN_REGULATORY_SCARE,
                    matchedKeywordOrPattern = kw,
                    reason = "Fake BVN Freeze / Regulatory scare tactic: '$kw'"
                )
            }
        }

        // 3. Check Aggressive Predatory Debt Recovery spam
        for (kw in RECOVERY_SPAM_KEYWORDS) {
            if (lowerBody.contains(kw)) {
                return SmsEvaluationResult(
                    isSpam = true,
                    threatCategory = SmsThreatCategory.PREDATORY_LOAN_RECOVERY,
                    matchedKeywordOrPattern = kw,
                    reason = "Predatory Debt Recovery spam: '$kw'"
                )
            }
        }

        // 4. Check if sender matches existing active CallShield rules (e.g. +2342, 0201, 07000)
        if (safeSender.isNotBlank()) {
            val callEval = HeuristicPatternEngine.evaluate(safeSender, rules, blockPrivate = false)
            if (callEval.isBlocked) {
                return SmsEvaluationResult(
                    isSpam = true,
                    threatCategory = SmsThreatCategory.SENDER_BLACKLISTED,
                    matchedKeywordOrPattern = callEval.matchedRule?.pattern ?: safeSender,
                    reason = "Sender matched blocked trunk rule: ${callEval.matchedRule?.name}"
                )
            }
        }

        // 5. Check Alphanumeric Loan Shark Senders combined with lending terms
        if (SUSPICIOUS_ALPHANUMERIC_SENDERS.any { upperSender.contains(it) }) {
            if (lowerBody.contains("loan") || lowerBody.contains("repay") || lowerBody.contains("interest") || lowerBody.contains("due")) {
                return SmsEvaluationResult(
                    isSpam = true,
                    threatCategory = SmsThreatCategory.PREDATORY_LOAN_RECOVERY,
                    matchedKeywordOrPattern = safeSender,
                    reason = "Unsolicited loan sender broadcast [$safeSender]"
                )
            }
        }

        return SmsEvaluationResult(isSpam = false, reason = "Legitimate SMS message")
    }
}
