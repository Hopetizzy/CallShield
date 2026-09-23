package com.callshield.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class SmsThreatCategory {
    DEFAMATION_HARASSMENT,      // Threats to broadcast to family/contacts or public shame
    BVN_REGULATORY_SCARE,       // Fake BVN freeze, NIBSS blacklist, EFCC/Police arrest threats
    PREDATORY_LOAN_RECOVERY,    // Aggressive debt collection spam & field agent threats
    SENDER_BLACKLISTED,         // Message from known blocked trunk or VoIP sender
    SUSPICIOUS_PHISHING         // Suspicious unverified lending links or KYC spoofing
}

@Entity(tableName = "quarantined_sms")
data class QuarantinedSmsRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sender: String,
    val normalizedSender: String,
    val body: String,
    val threatCategory: SmsThreatCategory,
    val matchedKeywordOrPattern: String,
    val isRead: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
