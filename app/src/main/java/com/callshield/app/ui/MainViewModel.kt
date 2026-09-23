package com.callshield.app.ui

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.callshield.app.data.local.entity.BlockedCallRecord
import com.callshield.app.data.local.entity.FilterRule
import com.callshield.app.data.local.entity.QuarantinedSmsRecord
import com.callshield.app.data.local.entity.RuleType
import com.callshield.app.data.repository.CallDefenseRepository
import com.callshield.app.engine.AdaptiveSubnetManager
import com.callshield.app.engine.EvaluationResult
import com.callshield.app.engine.HeuristicPatternEngine
import com.callshield.app.engine.LegalEvidenceExporter
import com.callshield.app.engine.NumberNormalizer
import com.callshield.app.engine.SmsEvaluationResult
import com.callshield.app.engine.SmsHeuristicEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

import android.net.Uri
import com.callshield.app.engine.BackupImportResult
import com.callshield.app.engine.RuleBackupManager
import com.callshield.app.ui.theme.AppTheme

data class BackupState(
    val isExporting: Boolean = false,
    val isImporting: Boolean = false,
    val exportedFile: File? = null,
    val shareIntent: Intent? = null,
    val importedCount: Int? = null,
    val errorMessage: String? = null
)

data class SandboxState(
    val testNumberInput: String = "",
    val testSmsBodyInput: String = "",
    val isTesting: Boolean = false,
    val result: EvaluationResult? = null,
    val smsResult: SmsEvaluationResult? = null,
    val normalizedInternational: String = "",
    val normalizedNational: String = "",
    val detectedTrunk: String = "",
    val burstSimulatedCount: Int = 0,
    val isAdaptiveSubnetTriggered: Boolean = false
)

sealed class ExportState {
    object Idle : ExportState()
    object Generating : ExportState()
    data class Success(val file: File, val intent: Intent, val isEmail: Boolean) : ExportState()
    data class Error(val message: String) : ExportState()
}

class MainViewModel(
    private val repository: CallDefenseRepository
) : ViewModel() {

    val isShieldArmed: StateFlow<Boolean> = repository.isShieldArmed
    val isWhitelistEnabled: StateFlow<Boolean> = repository.isWhitelistEnabled
    val blockPrivateNumbers: StateFlow<Boolean> = repository.blockPrivateNumbers
    val isAutoSubnetShieldEnabled: StateFlow<Boolean> = repository.isAutoSubnetShieldEnabled
    val isSmsShieldEnabled: StateFlow<Boolean> = repository.isSmsShieldEnabled
    val burstThreshold: StateFlow<Int> = repository.burstThreshold
    val currentTheme: StateFlow<AppTheme> = repository.currentTheme
    val isDailyDigestEnabled: StateFlow<Boolean> = repository.isDailyDigestEnabled
    val isWeeklyDigestEnabled: StateFlow<Boolean> = repository.isWeeklyDigestEnabled

    val allRules: StateFlow<List<FilterRule>> = repository.allRules
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeAdaptiveRules: StateFlow<List<FilterRule>> = repository.activeAdaptiveRules
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeRulesCount: StateFlow<Int> = repository.activeRulesCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val allBlockedCalls: StateFlow<List<BlockedCallRecord>> = repository.allBlockedCalls
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentBlockedCalls: StateFlow<List<BlockedCallRecord>> = repository.recentBlockedCalls
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalBlockedCount: StateFlow<Int> = repository.totalBlockedCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Quarantined SMS State
    val allQuarantinedSms: StateFlow<List<QuarantinedSmsRecord>> = repository.allQuarantinedSms
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalQuarantinedSmsCount: StateFlow<Int> = repository.totalQuarantinedSmsCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val unreadSmsCount: StateFlow<Int> = repository.unreadSmsCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Export Flow State
    private val _exportState = MutableStateFlow<ExportState>(ExportState.Idle)
    val exportState: StateFlow<ExportState> = _exportState.asStateFlow()

    // Backup & Portability State
    private val _backupState = MutableStateFlow(BackupState())
    val backupState: StateFlow<BackupState> = _backupState.asStateFlow()

    // Threat Simulator / Sandbox State
    private val _sandboxState = MutableStateFlow(SandboxState())
    val sandboxState: StateFlow<SandboxState> = _sandboxState.asStateFlow()

    fun setTheme(theme: AppTheme) {
        repository.setTheme(theme)
    }

    fun exportRuleBackup(context: Context, password: String? = null) {
        viewModelScope.launch {
            _backupState.update { it.copy(isExporting = true, errorMessage = null) }
            try {
                val rules = allRules.value
                val backupFile = withContext(Dispatchers.IO) {
                    RuleBackupManager.exportBackup(context, rules, password)
                }
                val shareIntent = RuleBackupManager.createShareIntent(context, backupFile)
                _backupState.update {
                    it.copy(isExporting = false, exportedFile = backupFile, shareIntent = shareIntent)
                }
            } catch (e: Exception) {
                _backupState.update {
                    it.copy(isExporting = false, errorMessage = e.localizedMessage ?: "Backup export failed")
                }
            }
        }
    }

    fun importRuleBackup(context: Context, uri: Uri, password: String? = null) {
        viewModelScope.launch {
            _backupState.update { it.copy(isImporting = true, errorMessage = null, importedCount = null) }
            try {
                val result = withContext(Dispatchers.IO) {
                    context.contentResolver.openInputStream(uri)?.use { inputStream ->
                        RuleBackupManager.importBackup(inputStream, password)
                    } ?: BackupImportResult(isSuccess = false, errorMessage = "Cannot open backup file")
                }

                if (result.isSuccess) {
                    val insertedCount = repository.importRules(result.importedRules)
                    _backupState.update {
                        it.copy(isImporting = false, importedCount = insertedCount)
                    }
                } else {
                    _backupState.update {
                        it.copy(isImporting = false, errorMessage = result.errorMessage ?: "Failed to import rules")
                    }
                }
            } catch (e: Exception) {
                _backupState.update {
                    it.copy(isImporting = false, errorMessage = e.localizedMessage ?: "Failed to process backup file")
                }
            }
        }
    }

    fun resetBackupState() {
        _backupState.value = BackupState()
    }

    fun toggleShield() {
        repository.setShieldArmed(!isShieldArmed.value)
    }

    fun toggleWhitelist(enabled: Boolean) {
        repository.setWhitelistEnabled(enabled)
    }

    fun toggleBlockPrivate(block: Boolean) {
        repository.setBlockPrivateNumbers(block)
    }

    fun toggleAutoSubnetShield(enabled: Boolean) {
        repository.setAutoSubnetShieldEnabled(enabled)
    }

    fun toggleSmsShield(enabled: Boolean) {
        repository.setSmsShieldEnabled(enabled)
    }

    fun toggleDailyDigest(enabled: Boolean) {
        repository.setDailyDigestEnabled(enabled)
    }

    fun toggleWeeklyDigest(enabled: Boolean) {
        repository.setWeeklyDigestEnabled(enabled)
    }

    fun toggleRule(ruleId: Long, isEnabled: Boolean) {
        viewModelScope.launch {
            repository.toggleRule(ruleId, isEnabled)
        }
    }

    fun addRule(
        name: String,
        pattern: String,
        ruleType: RuleType,
        description: String
    ) {
        viewModelScope.launch {
            val rule = FilterRule(
                name = name.trim(),
                pattern = pattern.trim(),
                ruleType = ruleType,
                description = description.trim(),
                isEnabled = true,
                isBuiltIn = false
            )
            repository.addRule(rule)
        }
    }

    fun deleteRule(rule: FilterRule) {
        viewModelScope.launch {
            repository.deleteRule(rule)
        }
    }

    fun deleteRuleById(ruleId: Long) {
        viewModelScope.launch {
            repository.deleteRuleById(ruleId)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    fun deleteQuarantinedSms(record: QuarantinedSmsRecord) {
        viewModelScope.launch {
            repository.deleteQuarantinedSms(record)
        }
    }

    fun deleteQuarantinedSmsById(id: Long) {
        viewModelScope.launch {
            repository.deleteQuarantinedSmsById(id)
        }
    }

    fun clearAllQuarantinedSms() {
        viewModelScope.launch {
            repository.clearAllQuarantinedSms()
        }
    }

    fun resetExportState() {
        _exportState.value = ExportState.Idle
    }

    // Export FCCPC Complaint PDF (Includes Intercepted Calls + Quarantined SMS)
    fun exportFccpcPdf(context: Context) {
        viewModelScope.launch {
            _exportState.value = ExportState.Generating
            try {
                val callRecords = allBlockedCalls.value
                val smsRecords = allQuarantinedSms.value
                val pdfFile = withContext(Dispatchers.IO) {
                    LegalEvidenceExporter.generateFccpcComplaintPdf(context, callRecords, smsRecords)
                }
                val intent = LegalEvidenceExporter.createFccpcEmailIntent(context, pdfFile, callRecords.size, smsRecords.size)
                _exportState.value = ExportState.Success(pdfFile, intent, isEmail = true)
            } catch (e: Exception) {
                _exportState.value = ExportState.Error(e.message ?: "Failed to generate PDF")
            }
        }
    }

    // Export Forensic CSV
    fun exportEvidenceCsv(context: Context) {
        viewModelScope.launch {
            _exportState.value = ExportState.Generating
            try {
                val callRecords = allBlockedCalls.value
                val smsRecords = allQuarantinedSms.value
                val csvFile = withContext(Dispatchers.IO) {
                    LegalEvidenceExporter.generateEvidenceCsv(context, callRecords, smsRecords)
                }
                val intent = LegalEvidenceExporter.createGenericShareIntent(context, csvFile, "text/csv")
                _exportState.value = ExportState.Success(csvFile, intent, isEmail = false)
            } catch (e: Exception) {
                _exportState.value = ExportState.Error(e.message ?: "Failed to export CSV")
            }
        }
    }

    // Interactive Sandbox Tester Simulation
    fun onSandboxInputChange(input: String) {
        val trunk = if (input.isNotBlank()) AdaptiveSubnetManager.extractTrunkPrefix(input) else ""
        _sandboxState.update { it.copy(testNumberInput = input, detectedTrunk = trunk, result = null, smsResult = null) }
    }

    fun onSandboxSmsBodyChange(body: String) {
        _sandboxState.update { it.copy(testSmsBodyInput = body, smsResult = null) }
    }

    fun runSandboxSimulation() {
        val numberInput = _sandboxState.value.testNumberInput.trim()
        val smsBodyInput = _sandboxState.value.testSmsBodyInput.trim()

        if (numberInput.isBlank() && smsBodyInput.isBlank()) return

        val variants = NumberNormalizer.getNormalizedVariants(numberInput)
        val trunk = AdaptiveSubnetManager.extractTrunkPrefix(numberInput)

        // Check burst if number provided
        var burstTriggered = false
        if (numberInput.isNotBlank() && isAutoSubnetShieldEnabled.value) {
            val burstResult = AdaptiveSubnetManager.recordCallAndCheckBurst(
                rawNumber = numberInput,
                threshold = repository.burstThreshold.value,
                windowMinutes = repository.burstWindowMinutes.value,
                lockoutDurationHours = repository.lockoutDurationHours.value
            )
            if (burstResult.isBurstTriggered && burstResult.adaptiveRule != null) {
                burstTriggered = true
                viewModelScope.launch {
                    repository.addRule(burstResult.adaptiveRule)
                }
            }
        }

        val activeRules = allRules.value.filter { it.isEnabled }
        val evalCallResult = if (numberInput.isNotBlank()) {
            HeuristicPatternEngine.evaluate(
                rawNumber = numberInput,
                rules = activeRules,
                isPrivateOrRestricted = numberInput.equals("RESTRICTED", ignoreCase = true) || numberInput.equals("PRIVATE", ignoreCase = true),
                blockPrivate = blockPrivateNumbers.value
            )
        } else null

        val evalSmsResult = if (smsBodyInput.isNotBlank() || numberInput.isNotBlank()) {
            SmsHeuristicEngine.evaluate(
                sender = numberInput,
                body = smsBodyInput,
                rules = activeRules
            )
        } else null

        _sandboxState.update {
            it.copy(
                result = evalCallResult,
                smsResult = evalSmsResult,
                normalizedInternational = variants.international,
                normalizedNational = variants.national,
                detectedTrunk = trunk,
                isAdaptiveSubnetTriggered = burstTriggered
            )
        }
    }
}

class MainViewModelFactory(
    private val repository: CallDefenseRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
