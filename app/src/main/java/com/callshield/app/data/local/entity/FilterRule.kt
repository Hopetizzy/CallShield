package com.callshield.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class RuleType {
    PREFIX,             // Starts with (e.g., +2342, 02)
    REGEX,              // Advanced Regex (e.g., ^(\+?234|0)2.*)
    WILDCARD,           // Wildcard matching (e.g., +2347000*)
    ZERO_REPETITION,    // Repetitive zeros detector (e.g., 3+ zeros in sequence)
    EXACT_MATCH,        // Single blocked number
    ADAPTIVE_SUBNET     // Auto-locked rotating trunk range (e.g., 0201888*)
}

@Entity(tableName = "filter_rules")
data class FilterRule(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val pattern: String,
    val ruleType: RuleType = RuleType.PREFIX,
    val description: String = "",
    val isEnabled: Boolean = true,
    val isBuiltIn: Boolean = false,
    val matchCount: Int = 0,
    val expiresAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)
