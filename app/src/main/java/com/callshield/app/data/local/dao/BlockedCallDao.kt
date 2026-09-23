package com.callshield.app.data.local.dao

import androidx.room.*
import com.callshield.app.data.local.entity.BlockedCallRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface BlockedCallDao {
    @Query("SELECT * FROM blocked_calls ORDER BY timestamp DESC")
    fun getAllBlockedCallsFlow(): Flow<List<BlockedCallRecord>>

    @Query("SELECT * FROM blocked_calls ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentBlockedCallsFlow(limit: Int = 10): Flow<List<BlockedCallRecord>>

    @Query("SELECT COUNT(*) FROM blocked_calls")
    fun getTotalBlockedCountFlow(): Flow<Int>

    @Query("SELECT COUNT(*) FROM blocked_calls WHERE timestamp >= :sinceTimestamp")
    fun getBlockedCountSinceFlow(sinceTimestamp: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM blocked_calls WHERE timestamp >= :sinceTimestamp")
    suspend fun getBlockedCountSince(sinceTimestamp: Long): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: BlockedCallRecord): Long

    @Delete
    suspend fun deleteRecord(record: BlockedCallRecord)

    @Query("DELETE FROM blocked_calls")
    suspend fun clearAllRecords()
}
