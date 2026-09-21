package com.callshield.app.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.callshield.app.data.local.dao.BlockedCallDao
import com.callshield.app.data.local.dao.RuleDao
import com.callshield.app.data.local.entity.BlockedCallRecord
import com.callshield.app.data.local.entity.FilterRule
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CallDefenseRepository(
    private val ruleDao: RuleDao,
    private val blockedCallDao: BlockedCallDao,
    private val context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("callshield_prefs", Context.MODE_PRIVATE)

    private val _isShieldArmed = MutableStateFlow(prefs.getBoolean(KEY_SHIELD_ARMED, true))
    val isShieldArmed: StateFlow<Boolean> = _isShieldArmed.asStateFlow()

    private val _isWhitelistEnabled = MutableStateFlow(prefs.getBoolean(KEY_WHITELIST_ENABLED, true))
    val isWhitelistEnabled: StateFlow<Boolean> = _isWhitelistEnabled.asStateFlow()

    private val _blockPrivateNumbers = MutableStateFlow(prefs.getBoolean(KEY_BLOCK_PRIVATE, true))
    val blockPrivateNumbers: StateFlow<Boolean> = _blockPrivateNumbers.asStateFlow()

    fun setShieldArmed(armed: Boolean) {
        prefs.edit().putBoolean(KEY_SHIELD_ARMED, armed).apply()
        _isShieldArmed.value = armed
    }

    fun setWhitelistEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_WHITELIST_ENABLED, enabled).apply()
        _isWhitelistEnabled.value = enabled
    }

    fun setBlockPrivateNumbers(block: Boolean) {
        prefs.edit().putBoolean(KEY_BLOCK_PRIVATE, block).apply()
        _blockPrivateNumbers.value = block
    }

    // Rules
    val allRules: Flow<List<FilterRule>> = ruleDao.getAllRulesFlow()
    val activeRulesCount: Flow<Int> = ruleDao.getActiveRulesCountFlow()

    suspend fun getActiveRules(): List<FilterRule> = ruleDao.getActiveRules()

    suspend fun addRule(rule: FilterRule): Long = ruleDao.insertRule(rule)

    suspend fun updateRule(rule: FilterRule) = ruleDao.updateRule(rule)

    suspend fun deleteRule(rule: FilterRule) = ruleDao.deleteRule(rule)

    suspend fun toggleRule(id: Long, isEnabled: Boolean) = ruleDao.setRuleEnabled(id, isEnabled)

    suspend fun incrementRuleMatch(id: Long) = ruleDao.incrementMatchCount(id)

    // Blocked Records
    val allBlockedCalls: Flow<List<BlockedCallRecord>> = blockedCallDao.getAllBlockedCallsFlow()
    val recentBlockedCalls: Flow<List<BlockedCallRecord>> = blockedCallDao.getRecentBlockedCallsFlow(15)
    val totalBlockedCount: Flow<Int> = blockedCallDao.getTotalBlockedCountFlow()

    suspend fun logBlockedCall(record: BlockedCallRecord): Long = blockedCallDao.insertRecord(record)

    suspend fun clearHistory() = blockedCallDao.clearAllRecords()

    companion object {
        private const val KEY_SHIELD_ARMED = "key_shield_armed"
        private const val KEY_WHITELIST_ENABLED = "key_whitelist_enabled"
        private const val KEY_BLOCK_PRIVATE = "key_block_private"
    }
}
