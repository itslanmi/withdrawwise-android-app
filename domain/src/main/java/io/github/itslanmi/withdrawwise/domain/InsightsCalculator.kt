package io.github.itslanmi.withdrawwise.domain

import io.github.itslanmi.withdrawwise.domain.model.Money
import io.github.itslanmi.withdrawwise.domain.model.Withdrawal
import io.github.itslanmi.withdrawwise.domain.model.sumOfMoney
import java.time.Clock
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

enum class InsightPeriod { Week, Month, Year }

enum class BucketSize { Day, Month }

data class Totals(val withdrawn: Money, val fees: Money, val count: Int, val freeCount: Int) {
    val average: Money get() = if (count == 0) Money.Zero else Money(withdrawn.cents / count)

    /** Share of withdrawals that cost nothing, 0..100. */
    val freePercent: Int get() = if (count == 0) 0 else (freeCount * 100 + count / 2) / count

    companion object {
        val Empty = Totals(Money.Zero, Money.Zero, 0, 0)

        fun of(withdrawals: List<Withdrawal>) = Totals(
            withdrawn = withdrawals.sumOfMoney { it.amount },
            fees = withdrawals.sumOfMoney { it.fee },
            count = withdrawals.size,
            freeCount = withdrawals.count { it.fee.isZero }
        )
    }
}

/** One bar of the chart: a day or a month. */
data class ChartBucket(
    val start: LocalDate,
    val size: BucketSize,
    val withdrawn: Money,
    val fees: Money,
    val isCurrent: Boolean
)

data class AtmUsage(
    val atmId: String,
    val atmName: String,
    val bankId: String,
    val count: Int,
    val withdrawn: Money,
    val fees: Money
)

data class Insights(
    val period: InsightPeriod,
    val current: Totals,
    val previous: Totals,
    /** Fees paid at ATMs that had a free alternative within [InsightsCalculator.AvoidableWithinMeters]. */
    val avoidableFees: Money,
    val avoidableCount: Int,
    val buckets: List<ChartBucket>,
    val topAtms: List<AtmUsage>
) {
    val feesDelta: Money get() = current.fees - previous.fees
    val hasData: Boolean get() = current.count > 0 || buckets.any { it.withdrawn.isPositive }
}

/** Turns the raw withdrawal log into the numbers shown on the Insights screen and in summaries. */
class InsightsCalculator
@Inject
constructor(private val clock: Clock) {
    fun compute(withdrawals: List<Withdrawal>, period: InsightPeriod): Insights {
        val today = LocalDate.now(clock)
        val dated = withdrawals.map { it to it.timestamp.atZone(clock.zone).toLocalDate() }
        val (currentRange, previousRange) = ranges(period, today)

        val current = dated.filter { it.second in currentRange }.map { it.first }
        val previous = dated.filter { it.second in previousRange }.map { it.first }
        val avoidable = current.filter {
            it.fee.isPositive &&
                (it.freeAlternativeMeters ?: Int.MAX_VALUE) <= AvoidableWithinMeters
        }

        return Insights(
            period = period,
            current = Totals.of(current),
            previous = Totals.of(previous),
            avoidableFees = avoidable.sumOfMoney { it.fee },
            avoidableCount = avoidable.size,
            buckets = buckets(period, today, dated),
            topAtms =
                current
                    .groupBy { it.atmId }
                    .map { (id, list) ->
                        AtmUsage(
                            id,
                            list.first().atmName,
                            list.first().atmBankId,
                            list.size,
                            list.sumOfMoney {
                                it.amount
                            },
                            list.sumOfMoney { it.fee }
                        )
                    }.sortedWith(compareByDescending<AtmUsage> { it.count }.thenByDescending { it.withdrawn.cents })
                    .take(TopAtmCount)
        )
    }

    /** Totals for an arbitrary date range, used by notification summaries. */
    fun totalsBetween(withdrawals: List<Withdrawal>, range: ClosedRange<LocalDate>): Totals =
        Totals.of(withdrawals.filter { it.timestamp.atZone(clock.zone).toLocalDate() in range })

    private fun ranges(period: InsightPeriod, today: LocalDate): Pair<ClosedRange<LocalDate>, ClosedRange<LocalDate>> =
        when (period) {
            InsightPeriod.Week -> today.minusDays(6)..today to today.minusDays(13)..today.minusDays(7)

            InsightPeriod.Month -> {
                val month = YearMonth.from(today)
                val last = month.minusMonths(1)
                month.atDay(1)..month.atEndOfMonth() to last.atDay(1)..last.atEndOfMonth()
            }

            InsightPeriod.Year -> {
                val start = today.withDayOfYear(1)
                start..start.plusYears(1).minusDays(1) to start.minusYears(1)..start.minusDays(1)
            }
        }

    private fun buckets(
        period: InsightPeriod,
        today: LocalDate,
        dated: List<Pair<Withdrawal, LocalDate>>
    ): List<ChartBucket> = when (period) {
        InsightPeriod.Week ->
            (6 downTo 0).map { offset ->
                val day = today.minusDays(offset.toLong())
                bucket(day, BucketSize.Day, dated.filter { it.second == day }.map { it.first }, offset == 0)
            }

        InsightPeriod.Month, InsightPeriod.Year -> {
            val months = if (period == InsightPeriod.Month) 6 else 12
            val thisMonth = YearMonth.from(today)
            (months - 1 downTo 0).map { offset ->
                val month = thisMonth.minusMonths(offset.toLong())
                bucket(
                    month.atDay(1),
                    BucketSize.Month,
                    dated.filter { YearMonth.from(it.second) == month }.map { it.first },
                    offset == 0
                )
            }
        }
    }

    private fun bucket(start: LocalDate, size: BucketSize, items: List<Withdrawal>, isCurrent: Boolean) =
        ChartBucket(start, size, items.sumOfMoney { it.amount }, items.sumOfMoney { it.fee }, isCurrent)

    companion object {
        const val AvoidableWithinMeters = 500
        private const val TopAtmCount = 3
    }
}
