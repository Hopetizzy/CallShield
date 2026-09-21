package com.callshield.app.engine

import com.callshield.app.data.local.entity.FilterRule
import com.callshield.app.data.local.entity.RuleType
import kotlin.system.measureTimeMillis

data class EvaluationResult(
    val isBlocked: Boolean,
    val matchedRule: FilterRule? = null,
    val reason: String = "Allowed",
    val evaluationTimeMs: Long = 0
)

object HeuristicPatternEngine {

    /**
     * Evaluates a phone number against active rules in sub-milliseconds (< 2ms).
     */
    fun evaluate(
        rawNumber: String?,
        rules: List<FilterRule>,
        isPrivateOrRestricted: Boolean = false,
        blockPrivate: Boolean = true
    ): EvaluationResult {
        var result: EvaluationResult
        val timeMs = measureTimeMillis {
            result = evaluateInternal(rawNumber, rules, isPrivateOrRestricted, blockPrivate)
        }
        return result.copy(evaluationTimeMs = timeMs)
    }

    private fun evaluateInternal(
        rawNumber: String?,
        rules: List<FilterRule>,
        isPrivateOrRestricted: Boolean,
        blockPrivate: Boolean
    ): EvaluationResult {
        // 1. Check for Private / Restricted / Hidden Numbers
        if (isPrivateOrRestricted || rawNumber.isNullOrBlank()) {
            return if (blockPrivate) {
                EvaluationResult(
                    isBlocked = true,
                    reason = "Blocked: Private/Restricted Caller ID"
                )
            } else {
                EvaluationResult(isBlocked = false, reason = "Allowed: Private Caller Allowed by user")
            }
        }

        val variants = NumberNormalizer.getNormalizedVariants(rawNumber)
        val intl = variants.international
        val nat = variants.national
        val digitsOnly = variants.rawDigitsOnly

        // 2. Evaluate against user & built-in rules
        for (rule in rules) {
            if (!rule.isEnabled) continue

            val pattern = rule.pattern.trim()
            if (pattern.isBlank()) continue

            val isMatch = when (rule.ruleType) {
                RuleType.PREFIX -> matchPrefix(pattern, intl, nat, digitsOnly)
                RuleType.REGEX -> matchRegex(pattern, intl, nat, rawNumber)
                RuleType.WILDCARD -> matchWildcard(pattern, intl, nat)
                RuleType.ZERO_REPETITION -> matchZeroRepetition(pattern, digitsOnly)
                RuleType.EXACT_MATCH -> matchExact(pattern, intl, nat, digitsOnly)
            }

            if (isMatch) {
                return EvaluationResult(
                    isBlocked = true,
                    matchedRule = rule,
                    reason = "Blocked by rule: ${rule.name} [${rule.pattern}]"
                )
            }
        }

        return EvaluationResult(isBlocked = false, reason = "Allowed: No matching block pattern")
    }

    private fun matchPrefix(pattern: String, intl: String, nat: String, digitsOnly: String): Boolean {
        val cleanPattern = NumberNormalizer.sanitize(pattern)
        val patternVariants = NumberNormalizer.getNormalizedVariants(cleanPattern)

        return intl.startsWith(patternVariants.international) ||
               nat.startsWith(patternVariants.national) ||
               digitsOnly.startsWith(patternVariants.rawDigitsOnly) ||
               intl.startsWith(cleanPattern) ||
               nat.startsWith(cleanPattern)
    }

    private fun matchRegex(pattern: String, intl: String, nat: String, raw: String): Boolean {
        return try {
            val regex = Regex(pattern, RegexOption.IGNORE_CASE)
            regex.containsMatchIn(intl) || regex.containsMatchIn(nat) || regex.containsMatchIn(raw)
        } catch (e: Exception) {
            false
        }
    }

    private fun matchWildcard(pattern: String, intl: String, nat: String): Boolean {
        val regexPattern = "^" + Regex.escape(pattern)
            .replace("\\*", ".*")
            .replace("\\?", ".") + "$"
        return try {
            val regex = Regex(regexPattern, RegexOption.IGNORE_CASE)
            regex.matches(intl) || regex.matches(nat)
        } catch (e: Exception) {
            false
        }
    }

    private fun matchZeroRepetition(pattern: String, digitsOnly: String): Boolean {
        // Default check for 3+ consecutive zeros or custom pattern
        val minZeros = if (pattern.all { it == '0' } && pattern.isNotEmpty()) pattern.length else 3
        val zeroRegex = Regex("0{$minZeros,}")
        return zeroRegex.containsMatchIn(digitsOnly)
    }

    private fun matchExact(pattern: String, intl: String, nat: String, digitsOnly: String): Boolean {
        val cleanPattern = NumberNormalizer.sanitize(pattern)
        val patternVariants = NumberNormalizer.getNormalizedVariants(cleanPattern)

        return intl == patternVariants.international ||
               nat == patternVariants.national ||
               digitsOnly == patternVariants.rawDigitsOnly
    }
}
