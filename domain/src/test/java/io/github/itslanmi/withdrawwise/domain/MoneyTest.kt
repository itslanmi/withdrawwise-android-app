package io.github.itslanmi.withdrawwise.domain

import io.github.itslanmi.withdrawwise.domain.model.Money
import io.github.itslanmi.withdrawwise.domain.model.sum
import org.junit.Assert.assertEquals
import org.junit.Test

class MoneyTest {
    @Test
    fun `converts decimal euros to cents without floating point drift`() {
        assertEquals(Money(120), Money.ofEuros(1.2))
        assertEquals(Money(29), Money.ofEuros(0.285))
        assertEquals(Money(10_000), Money.ofEuros(100.0))
    }

    @Test
    fun `formats with slovenian separators`() {
        assertEquals("1.234,50 €", Money(123_450).format())
        assertEquals("0,90 €", Money(90).format())
    }

    @Test
    fun `short format drops zero cents`() {
        assertEquals("50 €", Money.euros(50).formatShort())
        assertEquals("1,20 €", Money(120).formatShort())
    }

    @Test
    fun `sums a list of amounts`() {
        assertEquals(Money(350), listOf(Money(100), Money(250)).sum())
        assertEquals(Money.Zero, emptyList<Money>().sum())
    }
}
