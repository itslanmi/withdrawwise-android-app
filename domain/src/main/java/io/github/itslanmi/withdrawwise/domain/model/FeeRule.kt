package io.github.itslanmi.withdrawwise.domain.model

import java.math.BigDecimal
import java.math.RoundingMode

/**
 * How a fee is calculated: a flat part plus a percentage of the withdrawn amount,
 * clamped between an optional minimum and maximum.
 *
 * @property percentBasisPoints percentage in basis points, so `50` means 0.5 %.
 */
data class FeeRule(
    val flat: Money = Money.Zero,
    val percentBasisPoints: Int = 0,
    val min: Money = Money.Zero,
    val max: Money? = null
) {
    init {
        require(flat.cents >= 0) { "Flat fee must not be negative" }
        require(percentBasisPoints >= 0) { "Percentage must not be negative" }
    }

    val isFree: Boolean get() = flat.isZero && percentBasisPoints == 0 && min.isZero

    fun feeFor(amount: Money): Money {
        if (isFree) return Money.Zero
        val percentPart =
            BigDecimal.valueOf(amount.cents)
                .multiply(BigDecimal.valueOf(percentBasisPoints.toLong()))
                .divide(BigDecimal.valueOf(10_000), 0, RoundingMode.HALF_UP)
                .toLong()
        var fee = flat + Money(percentPart)
        if (fee < min) fee = min
        max?.let { if (fee > it) fee = it }
        return fee
    }

    companion object {
        val Free = FeeRule()
    }
}
