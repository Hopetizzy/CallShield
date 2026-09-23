package com.callshield.app.data.local.dao

import androidx.room.*
import com.callshield.app.data.local.entity.FilterRule
import kotlinx.coroutines.flow.Flow

@Dao
interface RuleDao {
    @Query("SELECT * FROM filter_rules ORDER BY isEnabled DESC, id ASC")
    fun getAllRulesFlow(): Flow<List<FilterRule>>

    @Query("SELECT * FROM filter_rules WHERE isEnabled = 1 AND (expiresAt IS NULL OR expiresAt > :currentTime)")
    suspend fun getActiveRules(currentTime: Long = System.currentTimeMillis()): List<FilterRule>

    @Query("SELECT * FROM filter_rules WHERE ruleType = 'ADAPTIVE_SUBNET' AND (expiresAt IS NULL OR expiresAt > :currentTime) ORDER BY createdAt DESC")
    fun getActiveAdaptiveRulesFlow(currentTime: Long = System.currentTimeMillis()): Flow<List<FilterRule>>

    @Query("SELECT COUNT(*) FROM filter_rules WHERE isEnabled = 1 AND (expiresAt IS NULL OR expiresAt > :currentTime)")
    fun getActiveRulesCountFlow(currentTime: Long = System.currentTimeMillis()): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRule(rule: FilterRule): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRules(rules: List<FilterRule>)

    @Update
    suspend fun updateRule(rule: FilterRule)

    @Delete
    suspend fun deleteRule(rule: FilterRule)

    @Query("DELETE FROM filter_rules WHERE id = :id")
    suspend fun deleteRuleById(id: Long)

    @Query("UPDATE filter_rules SET isEnabled = :isEnabled WHERE id = :id")
    suspend fun setRuleEnabled(id: Long, isEnabled: Boolean)

    @Query("UPDATE filter_rules SET matchCount = matchCount + 1 WHERE id = :id")
    suspend fun incrementMatchCount(id: Long)

    @Query("SELECT COUNT(*) FROM filter_rules")
    suspend fun getRulesCount(): Int

    @Query("DELETE FROM filter_rules WHERE expiresAt IS NOT NULL AND expiresAt <= :currentTime")
    suspend fun cleanupExpiredRules(currentTime: Long = System.currentTimeMillis()): Int
}
