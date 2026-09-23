package com.callshield.app.data.local.dao

import androidx.room.*
import com.callshield.app.data.local.entity.QuarantinedSmsRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface QuarantinedSmsDao {

    @Query("SELECT * FROM quarantined_sms ORDER BY timestamp DESC")
    fun getAllQuarantinedSmsFlow(): Flow<List<QuarantinedSmsRecord>>

    @Query("SELECT * FROM quarantined_sms ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentQuarantinedSmsFlow(limit: Int = 10): Flow<List<QuarantinedSmsRecord>>

    @Query("SELECT COUNT(*) FROM quarantined_sms")
    fun getTotalQuarantinedSmsCountFlow(): Flow<Int>

    @Query("SELECT COUNT(*) FROM quarantined_sms WHERE isRead = 0")
    fun getUnreadSmsCountFlow(): Flow<Int>

    @Query("SELECT * FROM quarantined_sms ORDER BY timestamp DESC")
    suspend fun getAllQuarantinedSmsList(): List<QuarantinedSmsRecord>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: QuarantinedSmsRecord): Long

    @Delete
    suspend fun deleteRecord(record: QuarantinedSmsRecord)

    @Query("DELETE FROM quarantined_sms WHERE id = :id")
    suspend fun deleteRecordById(id: Long)

    @Query("UPDATE quarantined_sms SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: Long)

    @Query("DELETE FROM quarantined_sms")
    suspend fun clearAllRecords()
}
