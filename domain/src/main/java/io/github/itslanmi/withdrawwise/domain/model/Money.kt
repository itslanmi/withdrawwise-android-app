package io.github.itslanmi.withdrawwise.domain.model

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.Locale

/**
 * An amount of euros stored as whole cents, so sums and fees never suffer from floating point errors.
 */
@JvmInline
value class Money(val cents: Long) : Comparable<Money> {
    operator fun plus(other: Money) = Money(cents + other.cents)

    operator fun minus(other: Money) = Money(cents - other.cents)

    operator fun times(factor: Int) = Money(cents * factor)

    override fun compareTo(other: Money): Int = cents.compareTo(other.cents)

    val isZero: Boolean get() = cents == 0L

    val isPositive: Boolean get() = cents > 0L

    /** Formats as e.g. `1.234,50 €` using the given locale's separators (Slovenian style by default). */
    fun format(locale: Locale = DefaultLocale): String {
        val formatter =
            NumberFormat.getNumberInstance(locale).apply {
                minimumFractionDigits = 2
                maximumFractionDigits = 2
            }
        return "${formatter.format(BigDecimal.valueOf(cents, 2))} €"
    }

    /** Like [format] but drops `,00` for whole amounts: `50 €`, `1,20 €`. */
    fun formatShort(locale: Locale = DefaultLocale): String {
        if (cents % 100 != 0L) return format(locale)
        return "${NumberFormat.getIntegerInstance(locale).format(cents / 100)} €"
    }

    override fun toString(): String = format()

    companion object {
        val Zero = Money(0)
        private val DefaultLocale: Locale = Locale.forLanguageTag("sl-SI")

        fun euros(euros: Int): Money = Money(euros * 100L)

        /** Converts a decimal euro amount (e.g. from JSON) to cents, rounding half up. */
        fun ofEuros(euros: Double): Money =
            Money(BigDecimal.valueOf(euros).movePointRight(2).setScale(0, RoundingMode.HALF_UP).toLong())
    }
}

fun Iterable<Money>.sum(): Money = Money(sumOf { it.cents })

inline fun <T> Iterable<T>.sumOfMoney(selector: (T) -> Money): Money = Money(sumOf { selector(it).cents })
