package io.github.itslanmi.withdrawwise.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.itslanmi.withdrawwise.domain.repository.DirectoryRepository
import io.github.itslanmi.withdrawwise.domain.repository.PreferencesRepository
import io.github.itslanmi.withdrawwise.ui.components.BankOption
import io.github.itslanmi.withdrawwise.ui.components.buildBankOptions
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class OnboardingStep { Welcome, Bank, Finish }

data class OnboardingUiState(
    val step: OnboardingStep = OnboardingStep.Welcome,
    val banks: List<BankOption> = emptyList(),
    val selectedBankId: String? = null,
    val name: String = ""
) {
    val canContinue: Boolean
        get() =
            when (step) {
                OnboardingStep.Welcome -> true
                OnboardingStep.Bank -> selectedBankId != null
                OnboardingStep.Finish -> selectedBankId != null
            }
}

@HiltViewModel
class OnboardingViewModel
@Inject
constructor(
    private val directory: DirectoryRepository,
    private val preferences: PreferencesRepository
) : ViewModel() {
    private val _state = MutableStateFlow(OnboardingUiState())
    val state: StateFlow<OnboardingUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val options = buildBankOptions(directory.banks(), directory.atms())
            _state.update { it.copy(banks = options) }
        }
    }

    fun selectBank(id: String) = _state.update { it.copy(selectedBankId = id) }

    fun setName(name: String) = _state.update { it.copy(name = name.take(MaxNameLength)) }

    fun next() = _state.update {
        val next = OnboardingStep.entries.getOrNull(it.step.ordinal + 1) ?: it.step
        if (it.canContinue) it.copy(step = next) else it
    }

    fun back() = _state.update {
        it.copy(step = OnboardingStep.entries.getOrNull(it.step.ordinal - 1) ?: it.step)
    }

    fun finish() {
        val current = _state.value
        val bankId = current.selectedBankId ?: return
        viewModelScope.launch { preferences.completeOnboarding(current.name, bankId) }
    }

    private companion object {
        const val MaxNameLength = 32
    }
}
