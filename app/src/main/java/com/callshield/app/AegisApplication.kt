package com.callshield.app

import android.app.Application
import com.callshield.app.data.local.CallShieldDatabase
import com.callshield.app.data.repository.CallDefenseRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class AegisApplication : Application() {

    private val applicationScope = CoroutineScope(Dispatchers.IO)

    val database by lazy { CallShieldDatabase.getDatabase(this, applicationScope) }
    val repository by lazy { 
        CallDefenseRepository(
            ruleDao = database.ruleDao(),
            blockedCallDao = database.blockedCallDao(),
            quarantinedSmsDao = database.quarantinedSmsDao(),
            context = this
        ) 
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        _repository = repository
    }

    companion object {
        private var instance: AegisApplication? = null
        private var _repository: CallDefenseRepository? = null

        val repository: CallDefenseRepository
            get() = _repository ?: throw IllegalStateException("AegisApplication not initialized")

        fun getInstance(): AegisApplication = instance ?: throw IllegalStateException("AegisApplication not initialized")
    }
}
