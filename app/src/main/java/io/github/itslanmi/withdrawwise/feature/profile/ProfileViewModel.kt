package io.github.itslanmi.withdrawwise.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.itslanmi.withdrawwise.domain.model.ThemeMode
import io.github.itslanmi.withdrawwise.domain.model.UserPreferences
import io.github.itslanmi.withdrawwise.domain.repository.DirectoryRepository
import io.github.itslanmi.withdrawwise.domain.repository.PreferencesRepository
import io.github.itslanmi.withdrawwise.domain.repository.WithdrawalRepository
import io.github.itslanmi.withdrawwise.notification.SummaryRunner
import io.github.itslanmi.withdrawwise.ui.components.BankOption
import io.github.itslanmi.withdrawwise.ui.components.buildBankOptions
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ProfileUiState(
    val preferences: UserPreferences = UserPreferences(),
    val banks: List<BankOption> = emptyList(),
    val withdrawalCount: Int = 0
) {
    val selectedBank: BankOption? get() = banks.firstOrNull { it.bank.id == preferences.bankId }
}

@HiltViewModel
class ProfileViewModel
@Inject
constructor(
    directory: DirectoryRepository,
    private val preferences: PreferencesRepository,
    private val withdrawals: WithdrawalRepository,
    private val summaries: SummaryRunner
) : ViewModel() {
    private val bankOptions = flow { emit(buildBankOptions(directory.banks(), directory.atms())) }

    val state: StateFlow<ProfileUiState> =
        combine(preferences.preferences, bankOptions, withdrawals.observeAll()) { prefs, banks, list ->
            ProfileUiState(preferences = prefs, banks = banks, withdrawalCount = list.size)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProfileUiState())

    fun setName(name: String) = launch { preferences.setDisplayName(name.take(32)) }

    fun setBank(id: String) = launch { preferences.setBank(id) }

    fun setTheme(mode: ThemeMode) = launch { preferences.setThemeMode(mode) }

    fun setWeeklySummary(enabled: Boolean) = launch { preferences.setWeeklySummary(enabled) }

    fun setMonthlySummary(enabled: Boolean) = launch { preferences.setMonthlySummary(enabled) }

    fun previewNotification() = launch { summaries.preview() }

    /** Deletes every logged withdrawal and the profile, which sends the user back to onboarding. */
    fun resetApp() = launch {
        withdrawals.deleteAll()
        preferences.reset()
    }

    private fun launch(block: suspend () -> Unit) {
        viewModelScope.launch { block() }
    }
}
