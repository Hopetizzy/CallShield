package com.callshield.app.data.local.dao

import androidx.room.*
import com.callshield.app.data.local.entity.FilterRule
import kotlinx.coroutines.flow.Flow

@Dao
interface RuleDao {
    @Query("SELECT * FROM filter_rules ORDER BY isEnabled DESC, id ASC")
    fun getAllRulesFlow(): Flow<List<FilterRule>>

    @Query("SELECT * FROM filter_rules WHERE isEnabled = 1")
    suspend fun getActiveRules(): List<FilterRule>

    @Query("SELECT COUNT(*) FROM filter_rules WHERE isEnabled = 1")
    fun getActiveRulesCountFlow(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRule(rule: FilterRule): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRules(rules: List<FilterRule>)

    @Update
    suspend fun updateRule(rule: FilterRule)

    @Delete
    suspend fun deleteRule(rule: FilterRule)

    @Query("UPDATE filter_rules SET isEnabled = :isEnabled WHERE id = :id")
    suspend fun setRuleEnabled(id: Long, isEnabled: Boolean)

    @Query("UPDATE filter_rules SET matchCount = matchCount + 1 WHERE id = :id")
    suspend fun incrementMatchCount(id: Long)

    @Query("SELECT COUNT(*) FROM filter_rules")
    suspend fun getRulesCount(): Int
}
