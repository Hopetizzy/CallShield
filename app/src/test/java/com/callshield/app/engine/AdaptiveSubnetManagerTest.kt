package com.callshield.app.engine

import com.callshield.app.data.local.entity.FilterRule
import com.callshield.app.data.local.entity.RuleType
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class AdaptiveSubnetManagerTest {

    @Before
    fun setUp() {
        AdaptiveSubnetManager.clearCache()
    }

    @Test
    fun testTrunkPrefixExtraction() {
        // Nigerian 0201 trunk
        val trunk1 = AdaptiveSubnetManager.extractTrunkPrefix("02018880123")
        assertEquals("+234201888", trunk1)

        val trunk2 = AdaptiveSubnetManager.extractTrunkPrefix("+2342018880999")
        assertEquals("+234201888", trunk2)

        // Standard 11-digit mobile trunk
        val trunk3 = AdaptiveSubnetManager.extractTrunkPrefix("08031234567")
        assertEquals("+2348031234", trunk3)
    }

    @Test
    fun testBurstDetectionTrigger() {
        // First call - no burst
        val res1 = AdaptiveSubnetManager.recordCallAndCheckBurst("02018880001", threshold = 3, windowMinutes = 10)
        assertFalse(res1.isBurstTriggered)
        assertEquals(1, res1.callCountInWindow)

        // Second call from same trunk - no burst yet
        val res2 = AdaptiveSubnetManager.recordCallAndCheckBurst("02018880002", threshold = 3, windowMinutes = 10)
        assertFalse(res2.isBurstTriggered)
        assertEquals(2, res2.callCountInWindow)

        // Third call from same trunk - BURST TRIGGERED!
        val res3 = AdaptiveSubnetManager.recordCallAndCheckBurst("02018880003", threshold = 3, windowMinutes = 10)
        assertTrue(res3.isBurstTriggered)
        assertEquals(3, res3.callCountInWindow)
        assertNotNull(res3.adaptiveRule)
        assertEquals(RuleType.ADAPTIVE_SUBNET, res3.adaptiveRule?.ruleType)
        assertEquals("+234201888", res3.adaptiveRule?.pattern)
        assertNotNull(res3.adaptiveRule?.expiresAt)
    }

    @Test
    fun testAdaptiveSubnetRuleEvaluation() {
        val adaptiveRule = FilterRule(
            id = 99,
            name = "⚡ Subnet Auto-Shield [+234201888*]",
            pattern = "+234201888",
            ruleType = RuleType.ADAPTIVE_SUBNET,
            isEnabled = true
        )

        // An un-encountered number within the locked-out subnet arrives
        val eval = HeuristicPatternEngine.evaluate("02018889999", listOf(adaptiveRule))
        assertTrue("Subsequent call within locked trunk should be blocked", eval.isBlocked)

        // Call from different trunk should pass
        val evalOther = HeuristicPatternEngine.evaluate("08031234567", listOf(adaptiveRule))
        assertFalse("Call from outside the locked trunk should pass", evalOther.isBlocked)
    }
}
