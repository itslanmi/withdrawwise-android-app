package io.github.itslanmi.withdrawwise.feature.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.itslanmi.withdrawwise.domain.InsightPeriod
import io.github.itslanmi.withdrawwise.domain.Insights
import io.github.itslanmi.withdrawwise.domain.InsightsCalculator
import io.github.itslanmi.withdrawwise.domain.WithdrawalLogger
import io.github.itslanmi.withdrawwise.domain.model.Bank
import io.github.itslanmi.withdrawwise.domain.repository.DirectoryRepository
import io.github.itslanmi.withdrawwise.domain.repository.WithdrawalRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class ChartMetric { Withdrawn, Fees }

data class InsightsUiState(
    val loading: Boolean = true,
    val period: InsightPeriod = InsightPeriod.Month,
    val metric: ChartMetric = ChartMetric.Withdrawn,
    val insights: Insights? = null,
    val banks: Map<String, Bank> = emptyMap(),
    val hasAnyWithdrawals: Boolean = false
)

@HiltViewModel
class InsightsViewModel
@Inject
constructor(
    directory: DirectoryRepository,
    withdrawals: WithdrawalRepository,
    private val calculator: InsightsCalculator,
    private val logger: WithdrawalLogger
) : ViewModel() {
    private val period = MutableStateFlow(InsightPeriod.Month)
    private val metric = MutableStateFlow(ChartMetric.Withdrawn)
    private val banks = flow { emit(directory.banks().associateBy { it.id }) }

    val state: StateFlow<InsightsUiState> =
        combine(withdrawals.observeAll(), period, metric, banks) { list, period, metric, banks ->
            InsightsUiState(
                loading = false,
                period = period,
                metric = metric,
                insights = calculator.compute(list, period),
                banks = banks,
                hasAnyWithdrawals = list.isNotEmpty()
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), InsightsUiState())

    fun setPeriod(value: InsightPeriod) {
        period.value = value
    }

    fun setMetric(value: ChartMetric) {
        metric.value = value
    }

    fun loadSampleData() {
        viewModelScope.launch { logger.loadSampleData() }
    }
}
