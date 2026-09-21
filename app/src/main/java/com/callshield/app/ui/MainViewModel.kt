package com.callshield.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.callshield.app.data.local.entity.BlockedCallRecord
import com.callshield.app.data.local.entity.FilterRule
import com.callshield.app.data.local.entity.RuleType
import com.callshield.app.data.repository.CallDefenseRepository
import com.callshield.app.engine.EvaluationResult
import com.callshield.app.engine.HeuristicPatternEngine
import com.callshield.app.engine.NumberNormalizer
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class SandboxState(
    val testNumberInput: String = "",
    val isTesting: Boolean = false,
    val result: EvaluationResult? = null,
    val normalizedInternational: String = "",
    val normalizedNational: String = ""
)

class MainViewModel(
    private val repository: CallDefenseRepository
) : ViewModel() {

    val isShieldArmed: StateFlow<Boolean> = repository.isShieldArmed
    val isWhitelistEnabled: StateFlow<Boolean> = repository.isWhitelistEnabled
    val blockPrivateNumbers: StateFlow<Boolean> = repository.blockPrivateNumbers

    val allRules: StateFlow<List<FilterRule>> = repository.allRules
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeRulesCount: StateFlow<Int> = repository.activeRulesCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val allBlockedCalls: StateFlow<List<BlockedCallRecord>> = repository.allBlockedCalls
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentBlockedCalls: StateFlow<List<BlockedCallRecord>> = repository.recentBlockedCalls
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalBlockedCount: StateFlow<Int> = repository.totalBlockedCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Threat Simulator / Sandbox State
    private val _sandboxState = MutableStateFlow(SandboxState())
    val sandboxState: StateFlow<SandboxState> = _sandboxState.asStateFlow()

    fun toggleShield() {
        repository.setShieldArmed(!isShieldArmed.value)
    }

    fun toggleWhitelist(enabled: Boolean) {
        repository.setWhitelistEnabled(enabled)
    }

    fun toggleBlockPrivate(block: Boolean) {
        repository.setBlockPrivateNumbers(block)
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

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    // Interactive Sandbox Tester Simulation
    fun onSandboxInputChange(input: String) {
        _sandboxState.update { it.copy(testNumberInput = input, result = null) }
    }

    fun runSandboxSimulation() {
        val input = _sandboxState.value.testNumberInput.trim()
        if (input.isBlank()) return

        val variants = NumberNormalizer.getNormalizedVariants(input)
        val activeRules = allRules.value.filter { it.isEnabled }
        val evalResult = HeuristicPatternEngine.evaluate(
            rawNumber = input,
            rules = activeRules,
            isPrivateOrRestricted = input.equals("RESTRICTED", ignoreCase = true) || input.equals("PRIVATE", ignoreCase = true),
            blockPrivate = blockPrivateNumbers.value
        )

        _sandboxState.update {
            it.copy(
                result = evalResult,
                normalizedInternational = variants.international,
                normalizedNational = variants.national
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
