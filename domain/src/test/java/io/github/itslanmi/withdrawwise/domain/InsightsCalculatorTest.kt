package io.github.itslanmi.withdrawwise.domain

import io.github.itslanmi.withdrawwise.domain.model.Money
import io.github.itslanmi.withdrawwise.domain.model.Withdrawal
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class InsightsCalculatorTest {
    // Friday 27 November 2026, noon UTC.
    private val clock = Clock.fixed(Instant.parse("2026-11-27T12:00:00Z"), ZoneOffset.UTC)
    private val calculator = InsightsCalculator(clock)

    private fun withdrawal(date: String, euros: Int, feeCents: Long = 0, atm: String = "a", alternative: Int? = null) =
        Withdrawal(
            atmId = atm,
            atmName = atm.uppercase(),
            atmBankId = "bank",
            amount = Money.euros(euros),
            fee = Money(feeCents),
            timestamp = Instant.parse("${date}T10:00:00Z"),
            freeAlternativeMeters = alternative
        )

    private val data =
        listOf(
            withdrawal("2026-11-26", 50),
            withdrawal("2026-11-20", 100, feeCents = 120, atm = "b", alternative = 300),
            withdrawal("2026-11-02", 40, feeCents = 395, atm = "b", alternative = 900),
            withdrawal("2026-10-15", 200, feeCents = 100, atm = "c"),
            withdrawal("2026-06-01", 20),
            withdrawal("2025-12-24", 60, feeCents = 100)
        )

    @Test
    fun `month totals and comparison with last month`() {
        val insights = calculator.compute(data, InsightPeriod.Month)
        assertEquals(Money.euros(190), insights.current.withdrawn)
        assertEquals(Money(515), insights.current.fees)
        assertEquals(3, insights.current.count)
        assertEquals(33, insights.current.freePercent)
        assertEquals(Money(100), insights.previous.fees)
        assertEquals(Money(415), insights.feesDelta)
    }

    @Test
    fun `only fees with a free ATM within 500 m count as avoidable`() {
        val insights = calculator.compute(data, InsightPeriod.Month)
        assertEquals(Money(120), insights.avoidableFees)
        assertEquals(1, insights.avoidableCount)
    }

    @Test
    fun `week covers the last seven days with daily buckets`() {
        val insights = calculator.compute(data, InsightPeriod.Week)
        assertEquals(1, insights.current.count)
        assertEquals(7, insights.buckets.size)
        assertEquals(LocalDate.of(2026, 11, 21), insights.buckets.first().start)
        assertTrue(insights.buckets.last().isCurrent)
        assertEquals(Money.euros(50), insights.buckets[5].withdrawn)
    }

    @Test
    fun `month view charts the last six months`() {
        val buckets = calculator.compute(data, InsightPeriod.Month).buckets
        assertEquals(6, buckets.size)
        assertEquals(LocalDate.of(2026, 6, 1), buckets.first().start)
        assertEquals(Money.euros(20), buckets.first().withdrawn)
        assertEquals(Money.euros(190), buckets.last().withdrawn)
    }

    @Test
    fun `year compares with the previous calendar year`() {
        val insights = calculator.compute(data, InsightPeriod.Year)
        assertEquals(5, insights.current.count)
        assertEquals(1, insights.previous.count)
        assertEquals(12, insights.buckets.size)
    }

    @Test
    fun `top ATMs are ranked by visits`() {
        val top = calculator.compute(data, InsightPeriod.Month).topAtms
        assertEquals("b", top.first().atmId)
        assertEquals(2, top.first().count)
    }

    @Test
    fun `empty history has no data`() {
        val insights = calculator.compute(emptyList(), InsightPeriod.Month)
        assertFalse(insights.hasData)
        assertEquals(Money.Zero, insights.current.average)
    }
}
