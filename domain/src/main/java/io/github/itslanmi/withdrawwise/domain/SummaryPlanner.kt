package io.github.itslanmi.withdrawwise.domain

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

enum class SummaryKind { Weekly, Monthly }

/** A summary that should be sent now, covering [range] (inclusive). */
data class SummaryRequest(
    val kind: SummaryKind,
    val range: ClosedRange<LocalDate>,
    /** The date to remember as "sent", so the same summary is never sent twice. */
    val marker: LocalDate
)

/**
 * Decides which summaries are due. Weekly summaries cover the previous Monday–Sunday and become
 * due from Monday; monthly reports cover the previous calendar month and become due from the 1st.
 * Because it compares against the last sent marker, a missed day (phone off) is caught up later.
 */
object SummaryPlanner {
    fun due(
        today: LocalDate,
        weeklyEnabled: Boolean,
        monthlyEnabled: Boolean,
        lastWeekly: LocalDate?,
        lastMonthly: LocalDate?
    ): List<SummaryRequest> = buildList {
        if (weeklyEnabled) {
            val weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            if (lastWeekly == null || lastWeekly < weekStart) {
                add(SummaryRequest(SummaryKind.Weekly, weekStart.minusDays(7)..weekStart.minusDays(1), weekStart))
            }
        }
        if (monthlyEnabled) {
            val monthStart = today.withDayOfMonth(1)
            if (lastMonthly == null || lastMonthly < monthStart) {
                val previous = monthStart.minusMonths(1)
                add(SummaryRequest(SummaryKind.Monthly, previous..monthStart.minusDays(1), monthStart))
            }
        }
    }
}
