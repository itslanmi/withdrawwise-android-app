package io.github.itslanmi.withdrawwise.feature.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.itslanmi.withdrawwise.domain.FeeCalculator
import io.github.itslanmi.withdrawwise.domain.WithdrawalLogger
import io.github.itslanmi.withdrawwise.domain.model.Bank
import io.github.itslanmi.withdrawwise.domain.model.Money
import io.github.itslanmi.withdrawwise.domain.model.Withdrawal
import io.github.itslanmi.withdrawwise.domain.model.sumOfMoney
import io.github.itslanmi.withdrawwise.domain.repository.DirectoryRepository
import io.github.itslanmi.withdrawwise.domain.repository.PreferencesRepository
import io.github.itslanmi.withdrawwise.domain.repository.WithdrawalRepository
import io.github.itslanmi.withdrawwise.ui.model.AtmUi
import io.github.itslanmi.withdrawwise.ui.model.buildAtmUis
import java.time.Clock
import java.time.LocalDateTime
import java.time.YearMonth
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class WithdrawalItem(val withdrawal: Withdrawal, val bank: Bank?, val dateTime: LocalDateTime)

data class MonthGroup(val month: YearMonth, val items: List<WithdrawalItem>) {
    val total: Money get() = items.sumOfMoney { it.withdrawal.amount }
    val fees: Money get() = items.sumOfMoney { it.withdrawal.fee }
}

data class HistoryUiState(
    val loading: Boolean = true,
    val groups: List<MonthGroup> = emptyList(),
    val thisMonthTotal: Money = Money.Zero,
    val thisMonthFees: Money = Money.Zero,
    val atms: List<AtmUi> = emptyList(),
    val calculator: FeeCalculator = FeeCalculator(emptyList()),
    val cardBankId: String? = null
) {
    val count: Int get() = groups.sumOf { it.items.size }
    val isEmpty: Boolean get() = !loading && groups.isEmpty()
}

@HiltViewModel
class HistoryViewModel
@Inject
constructor(
    directory: DirectoryRepository,
    preferences: PreferencesRepository,
    private val withdrawals: WithdrawalRepository,
    private val logger: WithdrawalLogger,
    private val clock: Clock
) : ViewModel() {
    private val directoryFlow = flow { emit(directory.banks() to directory.atms()) }
    private var lastDeleted: Withdrawal? = null

    val state: StateFlow<HistoryUiState> =
        combine(directoryFlow, preferences.preferences, withdrawals.observeAll()) { (banks, atms), prefs, list ->
            val banksById = banks.associateBy { it.id }
            val zone = clock.zone
            val groups =
                list
                    .sortedByDescending { it.timestamp }
                    .map {
                        WithdrawalItem(it, banksById[it.atmBankId], LocalDateTime.ofInstant(it.timestamp, zone))
                    }
                    .groupBy { YearMonth.from(it.dateTime) }
                    .map { (month, items) -> MonthGroup(month, items) }
            val current = groups.firstOrNull { it.month == YearMonth.now(clock) }
            HistoryUiState(
                loading = false,
                groups = groups,
                thisMonthTotal = current?.total ?: Money.Zero,
                thisMonthFees = current?.fees ?: Money.Zero,
                atms = buildAtmUis(atms, banks, prefs.bankId, null),
                calculator = FeeCalculator(banks),
                cardBankId = prefs.bankId
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HistoryUiState())

    fun log(atmId: String, amount: Money) {
        viewModelScope.launch { logger.log(atmId, amount) }
    }

    fun updateAmount(id: Long, amount: Money) {
        viewModelScope.launch { logger.updateAmount(id, amount) }
    }

    fun delete(withdrawal: Withdrawal) {
        lastDeleted = withdrawal
        viewModelScope.launch { withdrawals.delete(withdrawal.id) }
    }

    fun undoDelete() {
        val withdrawal = lastDeleted ?: return
        lastDeleted = null
        viewModelScope.launch { logger.restore(withdrawal.copy(id = 0)) }
    }

    fun loadSampleData() {
        viewModelScope.launch { logger.loadSampleData() }
    }
}
