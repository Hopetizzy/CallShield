package com.callshield.app.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.callshield.app.data.local.CallShieldDatabase
import com.callshield.app.data.local.dao.BlockedCallDao
import com.callshield.app.data.local.dao.QuarantinedSmsDao
import com.callshield.app.data.local.dao.RuleDao
import com.callshield.app.data.local.entity.BlockedCallRecord
import com.callshield.app.data.local.entity.FilterRule
import com.callshield.app.data.local.entity.QuarantinedSmsRecord
import com.callshield.app.service.CallDefenseTileService
import com.callshield.app.ui.theme.AppTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CallDefenseRepository(
    private val ruleDao: RuleDao,
    private val blockedCallDao: BlockedCallDao,
    private val quarantinedSmsDao: QuarantinedSmsDao,
    private val context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("callshield_prefs", Context.MODE_PRIVATE)

    init {
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            try {
                if (ruleDao.getRulesCount() == 0) {
                    CallShieldDatabase.populateDefaultRules(ruleDao)
                }
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    private val _isShieldArmed = MutableStateFlow(prefs.getBoolean(KEY_SHIELD_ARMED, true))
    val isShieldArmed: StateFlow<Boolean> = _isShieldArmed.asStateFlow()

    private val _isWhitelistEnabled = MutableStateFlow(prefs.getBoolean(KEY_WHITELIST_ENABLED, true))
    val isWhitelistEnabled: StateFlow<Boolean> = _isWhitelistEnabled.asStateFlow()

    private val _blockPrivateNumbers = MutableStateFlow(prefs.getBoolean(KEY_BLOCK_PRIVATE, true))
    val blockPrivateNumbers: StateFlow<Boolean> = _blockPrivateNumbers.asStateFlow()

    private val _isAutoSubnetShieldEnabled = MutableStateFlow(prefs.getBoolean(KEY_AUTO_SUBNET_ENABLED, true))
    val isAutoSubnetShieldEnabled: StateFlow<Boolean> = _isAutoSubnetShieldEnabled.asStateFlow()

    private val _isSmsShieldEnabled = MutableStateFlow(prefs.getBoolean(KEY_SMS_SHIELD_ENABLED, true))
    val isSmsShieldEnabled: StateFlow<Boolean> = _isSmsShieldEnabled.asStateFlow()

    private val _burstThreshold = MutableStateFlow(prefs.getInt(KEY_BURST_THRESHOLD, 3))
    val burstThreshold: StateFlow<Int> = _burstThreshold.asStateFlow()

    private val _burstWindowMinutes = MutableStateFlow(prefs.getInt(KEY_BURST_WINDOW_MIN, 10))
    val burstWindowMinutes: StateFlow<Int> = _burstWindowMinutes.asStateFlow()

    private val _lockoutDurationHours = MutableStateFlow(prefs.getInt(KEY_LOCKOUT_HOURS, 24))
    val lockoutDurationHours: StateFlow<Int> = _lockoutDurationHours.asStateFlow()

    private val _isDailyDigestEnabled = MutableStateFlow(prefs.getBoolean(KEY_DAILY_DIGEST, true))
    val isDailyDigestEnabled: StateFlow<Boolean> = _isDailyDigestEnabled.asStateFlow()

    private val _weeklyDigestEnabled = MutableStateFlow(prefs.getBoolean(KEY_WEEKLY_DIGEST, true))
    val isWeeklyDigestEnabled: StateFlow<Boolean> = _weeklyDigestEnabled.asStateFlow()

    private val _currentTheme = MutableStateFlow(
        try {
            AppTheme.valueOf(prefs.getString(KEY_APP_THEME, AppTheme.CYBERPUNK.name) ?: AppTheme.CYBERPUNK.name)
        } catch (e: Exception) {
            AppTheme.CYBERPUNK
        }
    )
    val currentTheme: StateFlow<AppTheme> = _currentTheme.asStateFlow()

    fun setShieldArmed(armed: Boolean) {
        prefs.edit().putBoolean(KEY_SHIELD_ARMED, armed).apply()
        _isShieldArmed.value = armed
        CallDefenseTileService.requestTileUpdate(context)
        notifyWidgetUpdate()
    }

    fun setWhitelistEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_WHITELIST_ENABLED, enabled).apply()
        _isWhitelistEnabled.value = enabled
    }

    fun setBlockPrivateNumbers(block: Boolean) {
        prefs.edit().putBoolean(KEY_BLOCK_PRIVATE, block).apply()
        _blockPrivateNumbers.value = block
    }

    fun setAutoSubnetShieldEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AUTO_SUBNET_ENABLED, enabled).apply()
        _isAutoSubnetShieldEnabled.value = enabled
    }

    fun setSmsShieldEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SMS_SHIELD_ENABLED, enabled).apply()
        _isSmsShieldEnabled.value = enabled
    }

    fun setBurstThreshold(threshold: Int) {
        prefs.edit().putInt(KEY_BURST_THRESHOLD, threshold).apply()
        _burstThreshold.value = threshold
    }

    fun setDailyDigestEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DAILY_DIGEST, enabled).apply()
        _isDailyDigestEnabled.value = enabled
    }

    fun setTheme(theme: AppTheme) {
        prefs.edit().putString(KEY_APP_THEME, theme.name).apply()
        _currentTheme.value = theme
    }

    fun setWeeklyDigestEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_WEEKLY_DIGEST, enabled).apply()
        _weeklyDigestEnabled.value = enabled
    }

    fun notifyWidgetUpdate() {
        try {
            val intent = android.content.Intent("com.callshield.app.ACTION_UPDATE_WIDGET")
            intent.setPackage(context.packageName)
            context.sendBroadcast(intent)
        } catch (e: Exception) {
            // Widget not placed or failed
        }
    }

    suspend fun getBlockedCallsCountSince(sinceTimestamp: Long): Int {
        return blockedCallDao.getBlockedCountSince(sinceTimestamp)
    }

    suspend fun getQuarantinedSmsCountSince(sinceTimestamp: Long): Int {
        return quarantinedSmsDao.getQuarantinedSmsCountSince(sinceTimestamp)
    }

    // Rules
    val allRules: Flow<List<FilterRule>> = ruleDao.getAllRulesFlow()
    val activeRulesCount: Flow<Int> = ruleDao.getActiveRulesCountFlow()
    val activeAdaptiveRules: Flow<List<FilterRule>> = ruleDao.getActiveAdaptiveRulesFlow()

    suspend fun getActiveRules(): List<FilterRule> {
        ruleDao.cleanupExpiredRules()
        return ruleDao.getActiveRules()
    }

    suspend fun addRule(rule: FilterRule): Long = ruleDao.insertRule(rule)

    suspend fun importRules(importedRules: List<FilterRule>): Int {
        val existingRules = ruleDao.getActiveRules()
        val existingPatterns = existingRules.map { it.pattern.trim().lowercase() }.toSet()

        val newRulesToInsert = importedRules.filter {
            !existingPatterns.contains(it.pattern.trim().lowercase())
        }

        if (newRulesToInsert.isNotEmpty()) {
            ruleDao.insertRules(newRulesToInsert)
        }
        return newRulesToInsert.size
    }

    suspend fun updateRule(rule: FilterRule) = ruleDao.updateRule(rule)

    suspend fun deleteRule(rule: FilterRule) = ruleDao.deleteRule(rule)

    suspend fun deleteRuleById(id: Long) = ruleDao.deleteRuleById(id)

    suspend fun restoreDefaultRules() {
        CallShieldDatabase.populateDefaultRules(ruleDao)
        notifyWidgetUpdate()
    }

    suspend fun toggleRule(id: Long, isEnabled: Boolean) = ruleDao.setRuleEnabled(id, isEnabled)

    suspend fun incrementRuleMatch(id: Long) = ruleDao.incrementMatchCount(id)

    suspend fun cleanupExpiredRules() = ruleDao.cleanupExpiredRules()

    // Blocked Records
    val allBlockedCalls: Flow<List<BlockedCallRecord>> = blockedCallDao.getAllBlockedCallsFlow()
    val recentBlockedCalls: Flow<List<BlockedCallRecord>> = blockedCallDao.getRecentBlockedCallsFlow(15)
    val totalBlockedCount: Flow<Int> = blockedCallDao.getTotalBlockedCountFlow()

    suspend fun logBlockedCall(record: BlockedCallRecord): Long {
        val id = blockedCallDao.insertRecord(record)
        notifyWidgetUpdate()
        return id
    }

    suspend fun clearHistory() {
        blockedCallDao.clearAllRecords()
        notifyWidgetUpdate()
    }

    // Quarantined SMS Records
    val allQuarantinedSms: Flow<List<QuarantinedSmsRecord>> = quarantinedSmsDao.getAllQuarantinedSmsFlow()
    val recentQuarantinedSms: Flow<List<QuarantinedSmsRecord>> = quarantinedSmsDao.getRecentQuarantinedSmsFlow(15)
    val totalQuarantinedSmsCount: Flow<Int> = quarantinedSmsDao.getTotalQuarantinedSmsCountFlow()
    val unreadSmsCount: Flow<Int> = quarantinedSmsDao.getUnreadSmsCountFlow()

    suspend fun getAllQuarantinedSmsList(): List<QuarantinedSmsRecord> = quarantinedSmsDao.getAllQuarantinedSmsList()

    suspend fun logQuarantinedSms(record: QuarantinedSmsRecord): Long {
        val id = quarantinedSmsDao.insertRecord(record)
        notifyWidgetUpdate()
        return id
    }

    suspend fun deleteQuarantinedSms(record: QuarantinedSmsRecord) {
        quarantinedSmsDao.deleteRecord(record)
        notifyWidgetUpdate()
    }

    suspend fun deleteQuarantinedSmsById(id: Long) {
        quarantinedSmsDao.deleteRecordById(id)
        notifyWidgetUpdate()
    }

    suspend fun clearAllQuarantinedSms() {
        quarantinedSmsDao.clearAllRecords()
        notifyWidgetUpdate()
    }

    suspend fun markSmsAsRead(id: Long) = quarantinedSmsDao.markAsRead(id)

    companion object {
        private const val KEY_SHIELD_ARMED = "key_shield_armed"
        private const val KEY_WHITELIST_ENABLED = "key_whitelist_enabled"
        private const val KEY_BLOCK_PRIVATE = "key_block_private"
        private const val KEY_AUTO_SUBNET_ENABLED = "key_auto_subnet_enabled"
        private const val KEY_SMS_SHIELD_ENABLED = "key_sms_shield_enabled"
        private const val KEY_BURST_THRESHOLD = "key_burst_threshold"
        private const val KEY_BURST_WINDOW_MIN = "key_burst_window_min"
        private const val KEY_LOCKOUT_HOURS = "key_lockout_hours"
        private const val KEY_APP_THEME = "key_app_theme"
        private const val KEY_DAILY_DIGEST = "key_daily_digest"
        private const val KEY_WEEKLY_DIGEST = "key_weekly_digest"
    }
}
