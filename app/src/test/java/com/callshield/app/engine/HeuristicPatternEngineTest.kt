package com.callshield.app.engine

import com.callshield.app.data.local.entity.FilterRule
import com.callshield.app.data.local.entity.RuleType
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HeuristicPatternEngineTest {

    @Test
    fun testVoIPPrefixBlocking() {
        val rules = listOf(
            FilterRule(
                id = 1,
                name = "Nigeria +234 2 / 02 VoIP Trunks",
                pattern = "+2342",
                ruleType = RuleType.PREFIX,
                isEnabled = true
            )
        )

        // Incoming call in national format 0201...
        val res1 = HeuristicPatternEngine.evaluate("02012345678", rules)
        assertTrue("0201... should be blocked by +2342 prefix rule", res1.isBlocked)

        // Incoming call in international format +234201...
        val res2 = HeuristicPatternEngine.evaluate("+2342018889999", rules)
        assertTrue("+2342... should be blocked", res2.isBlocked)

        // Legitimate standard mobile line (0803...)
        val res3 = HeuristicPatternEngine.evaluate("08031234567", rules)
        assertFalse("0803... should not be blocked", res3.isBlocked)
    }

    @Test
    fun testLoanSharkMultiZeroRepetition() {
        val rules = listOf(
            FilterRule(
                id = 2,
                name = "Multi-Zero Heuristic",
                pattern = "000",
                ruleType = RuleType.ZERO_REPETITION,
                isEnabled = true
            )
        )

        // Predatory auto-dialer number with consecutive zeros
        val res1 = HeuristicPatternEngine.evaluate("+2347000001234", rules)
        assertTrue("+2347000001234 should be blocked by 3+ consecutive zeros rule", res1.isBlocked)

        // Normal number without 3 consecutive zeros
        val res2 = HeuristicPatternEngine.evaluate("+2348030123456", rules)
        assertFalse("Normal mobile should pass", res2.isBlocked)
    }

    @Test
    fun testRegexRuleEvaluation() {
        val rules = listOf(
            FilterRule(
                id = 3,
                name = "Regex VoIP Pattern",
                pattern = "^(\\+?234|0)2.*",
                ruleType = RuleType.REGEX,
                isEnabled = true
            )
        )

        val res1 = HeuristicPatternEngine.evaluate("0299999999", rules)
        assertTrue(res1.isBlocked)

        val res2 = HeuristicPatternEngine.evaluate("+234299999999", rules)
        assertTrue(res2.isBlocked)

        val res3 = HeuristicPatternEngine.evaluate("07039999999", rules)
        assertFalse(res3.isBlocked)
    }

    @Test
    fun testRestrictedNumberBlocking() {
        val rules = emptyList<FilterRule>()

        val resBlocked = HeuristicPatternEngine.evaluate("", rules, isPrivateOrRestricted = true, blockPrivate = true)
        assertTrue("Restricted number should be blocked when blockPrivate is true", resBlocked.isBlocked)

        val resAllowed = HeuristicPatternEngine.evaluate("", rules, isPrivateOrRestricted = true, blockPrivate = false)
        assertFalse("Restricted number should be allowed when blockPrivate is false", resAllowed.isBlocked)
    }
}
