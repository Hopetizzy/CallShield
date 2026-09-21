package com.callshield.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "blocked_calls")
data class BlockedCallRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val rawNumber: String,
    val normalizedNumber: String,
    val matchedRuleName: String,
    val matchedPattern: String,
    val isPrivateNumber: Boolean = false,
    val interceptionLatencyMs: Long = 0,
    val timestamp: Long = System.currentTimeMillis()
)
