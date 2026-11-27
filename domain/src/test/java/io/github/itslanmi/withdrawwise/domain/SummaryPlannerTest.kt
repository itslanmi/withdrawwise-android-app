package io.github.itslanmi.withdrawwise.domain

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SummaryPlannerTest {
    private fun date(value: String) = LocalDate.parse(value)

    @Test
    fun `weekly summary on monday covers the previous week`() {
        val due = SummaryPlanner.due(
            date("2026-11-30"),
            weeklyEnabled = true,
            monthlyEnabled = false,
            lastWeekly = date("2026-11-23"),
            lastMonthly = null
        )
        val weekly = due.single()
        assertEquals(SummaryKind.Weekly, weekly.kind)
        assertEquals(date("2026-11-23"), weekly.range.start)
        assertEquals(date("2026-11-29"), weekly.range.endInclusive)
        assertEquals(date("2026-11-30"), weekly.marker)
    }

    @Test
    fun `nothing is due twice in the same week`() {
        val due = SummaryPlanner.due(
            date("2026-12-02"),
            weeklyEnabled = true,
            monthlyEnabled = false,
            lastWeekly = date("2026-11-30"),
            lastMonthly = null
        )
        assertTrue(due.isEmpty())
    }

    @Test
    fun `missed monday is caught up later in the week`() {
        val due = SummaryPlanner.due(
            date("2026-12-03"),
            weeklyEnabled = true,
            monthlyEnabled = false,
            lastWeekly = date("2026-11-23"),
            lastMonthly = null
        )
        assertEquals(date("2026-11-30"), due.single().marker)
    }

    @Test
    fun `monthly report on the first covers the previous month`() {
        val due = SummaryPlanner.due(
            date("2026-12-01"),
            weeklyEnabled = false,
            monthlyEnabled = true,
            lastWeekly = null,
            lastMonthly = date("2026-11-01")
        )
        val monthly = due.single()
        assertEquals(date("2026-11-01"), monthly.range.start)
        assertEquals(date("2026-11-30"), monthly.range.endInclusive)
    }

    @Test
    fun `disabled summaries are never due`() {
        assertTrue(SummaryPlanner.due(date("2026-12-01"), false, false, null, null).isEmpty())
    }
}
