package io.github.itslanmi.withdrawwise.notification

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.itslanmi.withdrawwise.domain.InsightPeriod
import io.github.itslanmi.withdrawwise.domain.InsightsCalculator
import io.github.itslanmi.withdrawwise.domain.SummaryKind
import io.github.itslanmi.withdrawwise.domain.SummaryPlanner
import io.github.itslanmi.withdrawwise.domain.repository.PreferencesRepository
import io.github.itslanmi.withdrawwise.domain.repository.WithdrawalRepository
import java.time.Clock
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first

/** Runs the summary check: works out which summaries are due, posts them and remembers it did. */
@Singleton
class SummaryRunner
@Inject
constructor(
    private val preferences: PreferencesRepository,
    private val withdrawals: WithdrawalRepository,
    private val calculator: InsightsCalculator,
    private val notifier: SummaryNotifier,
    private val dataStore: DataStore<Preferences>,
    private val clock: Clock
) {
    suspend fun runDueSummaries() {
        val prefs = preferences.preferences.first()
        if (!prefs.onboardingComplete) return
        val state = dataStore.data.first()
        val due =
            SummaryPlanner.due(
                today = LocalDate.now(clock),
                weeklyEnabled = prefs.weeklySummary,
                monthlyEnabled = prefs.monthlySummary,
                lastWeekly = state[LastWeekly]?.let(LocalDate::parse),
                lastMonthly = state[LastMonthly]?.let(LocalDate::parse)
            )
        if (due.isEmpty()) return
        val all = withdrawals.observeAll().first()
        due.forEach { request ->
            val totals = calculator.totalsBetween(all, request.range)
            // Quiet periods do not deserve a notification, but still count as handled.
            if (totals.count > 0) notifier.show(request.kind, totals, request.range)
            dataStore.edit {
                it[if (request.kind == SummaryKind.Weekly) LastWeekly else LastMonthly] = request.marker.toString()
            }
        }
    }

    /** Shows this month's summary right away, so the feature can be tried without waiting. */
    suspend fun preview() {
        val all = withdrawals.observeAll().first()
        val today = LocalDate.now(clock)
        val range = today.withDayOfMonth(1)..today
        val totals = calculator.compute(all, InsightPeriod.Month).current
        notifier.show(SummaryKind.Monthly, totals, range)
    }

    private companion object {
        val LastWeekly = stringPreferencesKey("summary_last_weekly")
        val LastMonthly = stringPreferencesKey("summary_last_monthly")
    }
}

@HiltWorker
class SummaryWorker
@AssistedInject
constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val runner: SummaryRunner
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result = runCatching { runner.runDueSummaries() }
        .fold(onSuccess = { Result.success() }, onFailure = { Result.retry() })
}

/** Schedules the daily summary check for roughly 09:00 local time. */
class SummaryScheduler
@Inject
constructor(
    @param:ApplicationContext private val context: Context,
    private val clock: Clock
) {
    fun schedule() {
        val now = LocalDateTime.now(clock)
        var next = now.toLocalDate().atTime(LocalTime.of(9, 0))
        if (!next.isAfter(now)) next = next.plusDays(1)
        val request =
            PeriodicWorkRequestBuilder<SummaryWorker>(1, TimeUnit.DAYS)
                .setInitialDelay(Duration.between(now, next))
                .build()
        WorkManager.getInstance(
            context
        ).enqueueUniquePeriodicWork(WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, request)
    }

    private companion object {
        const val WORK_NAME = "daily-summaries"
    }
}
