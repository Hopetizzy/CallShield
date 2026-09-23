package com.callshield.app.engine

import com.callshield.app.data.local.entity.FilterRule
import com.callshield.app.data.local.entity.RuleType
import com.callshield.app.data.local.entity.SmsThreatCategory
import org.junit.Assert.*
import org.junit.Test

class SmsHeuristicEngineTest {

    private val sampleRules = listOf(
        FilterRule(
            id = 1,
            name = "Nigeria +234 2 / 02 VoIP Trunks",
            pattern = "+2342",
            ruleType = RuleType.PREFIX,
            isEnabled = true
        )
    )

    @Test
    fun testDefamationThreatDetection() {
        val body = "Your loan of N25,000 is past due. We will post your picture to all your whatsapp contacts if you fail to repay by 2pm!"
        val result = SmsHeuristicEngine.evaluate(sender = "08012345678", body = body, rules = sampleRules)

        assertTrue(result.isSpam)
        assertEquals(SmsThreatCategory.DEFAMATION_HARASSMENT, result.threatCategory)
        assertTrue(result.matchedKeywordOrPattern.isNotBlank())
    }

    @Test
    fun testFakeBvnFreezeScareDetection() {
        val body = "FINAL DEMAND: Your BVN block has been submitted to NIBSS blacklist and police station litigation."
        val result = SmsHeuristicEngine.evaluate(sender = "NIBSS_NOTICE", body = body, rules = sampleRules)

        assertTrue(result.isSpam)
        assertEquals(SmsThreatCategory.BVN_REGULATORY_SCARE, result.threatCategory)
    }

    @Test
    fun testPredatoryDebtRecoveryDetection() {
        val body = "Your overdue loan is incurring daily penalties. Our recovery agent assigned will be visiting your residential address today."
        val result = SmsHeuristicEngine.evaluate(sender = "08123456789", body = body, rules = sampleRules)

        assertTrue(result.isSpam)
        assertEquals(SmsThreatCategory.PREDATORY_LOAN_RECOVERY, result.threatCategory)
    }

    @Test
    fun testBlockedSenderTrunkDetection() {
        // Legitimate-sounding text sent from a known blocked VoIP trunk
        val body = "Hello, please review your monthly statement."
        val result = SmsHeuristicEngine.evaluate(sender = "02018889999", body = body, rules = sampleRules)

        assertTrue("SMS from blacklisted trunk should be quarantined", result.isSpam)
        assertEquals(SmsThreatCategory.SENDER_BLACKLISTED, result.threatCategory)
    }

    @Test
    fun testLegitimateSmsPassThrough() {
        val body = "Hey, are we still meeting at the office by 3pm today?"
        val result = SmsHeuristicEngine.evaluate(sender = "08031234567", body = body, rules = sampleRules)

        assertFalse("Normal conversational SMS should not be flagged", result.isSpam)
        assertNull(result.threatCategory)
    }

    @Test
    fun testBankTransactionOtpPassThrough() {
        val body = "Your One Time Password (OTP) for transaction is 492810. Do not share with anyone."
        val result = SmsHeuristicEngine.evaluate(sender = "GTBank", body = body, rules = sampleRules)

        assertFalse("Legitimate bank OTP should pass cleanly", result.isSpam)
        assertNull(result.threatCategory)
    }
}
