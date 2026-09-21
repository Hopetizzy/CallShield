package com.callshield.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.callshield.app.data.local.dao.BlockedCallDao
import com.callshield.app.data.local.dao.RuleDao
import com.callshield.app.data.local.entity.BlockedCallRecord
import com.callshield.app.data.local.entity.FilterRule
import com.callshield.app.data.local.entity.RuleType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [FilterRule::class, BlockedCallRecord::class],
    version = 1,
    exportSchema = false
)
abstract class CallShieldDatabase : RoomDatabase() {

    abstract fun ruleDao(): RuleDao
    abstract fun blockedCallDao(): BlockedCallDao

    companion object {
        @Volatile
        private var INSTANCE: CallShieldDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope = CoroutineScope(Dispatchers.IO)): CallShieldDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CallShieldDatabase::class.java,
                    "callshield_database"
                )
                .addCallback(DatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch {
                        populateDefaultRules(database.ruleDao())
                    }
                }
            }
        }

        suspend fun populateDefaultRules(ruleDao: RuleDao) {
            val defaultRules = listOf(
                FilterRule(
                    name = "Nigeria +234 2 / 02 VoIP Trunks",
                    pattern = "+2342",
                    ruleType = RuleType.PREFIX,
                    description = "Blocks automated PBX/VoIP trunks and Ibadan landlines used by loan sharks",
                    isEnabled = true,
                    isBuiltIn = true
                ),
                FilterRule(
                    name = "Nigeria 0201 Virtual Trunks",
                    pattern = "+234201",
                    ruleType = RuleType.PREFIX,
                    description = "Blocks Lagos/VoIP autodialer pools often used in recovery spam calls",
                    isEnabled = true,
                    isBuiltIn = true
                ),
                FilterRule(
                    name = "Predatory Multi-Zero Series (+234 7000...)",
                    pattern = "+2347000",
                    ruleType = RuleType.PREFIX,
                    description = "Blocks spoofed loan shark numbers starting with multiple repetitive zeros",
                    isEnabled = true,
                    isBuiltIn = true
                ),
                FilterRule(
                    name = "Repetitive Zeros Heuristic (3+ Zeros)",
                    pattern = "000",
                    ruleType = RuleType.ZERO_REPETITION,
                    description = "Detects and flags spoofed virtual numbers containing 3 or more consecutive zeros",
                    isEnabled = true,
                    isBuiltIn = true
                ),
                FilterRule(
                    name = "0700 Toll-Free / Virtual Dialers",
                    pattern = "0700",
                    ruleType = RuleType.PREFIX,
                    description = "Blocks virtual commercial autodialers matching standard 0700 prefixes",
                    isEnabled = false,
                    isBuiltIn = true
                )
            )
            ruleDao.insertRules(defaultRules)
        }
    }
}
