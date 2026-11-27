package io.github.itslanmi.withdrawwise.ui.model

import io.github.itslanmi.withdrawwise.domain.model.Atm
import io.github.itslanmi.withdrawwise.domain.model.Bank
import io.github.itslanmi.withdrawwise.domain.model.FeeRule
import io.github.itslanmi.withdrawwise.domain.model.GeoPoint
import io.github.itslanmi.withdrawwise.domain.model.Money
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AtmUiTest {
    private val nlb = Bank("nlb", "NLB", "NLB", 0, issuesCards = true, otherAtmFee = FeeRule(flat = Money(100)))
    private val otp = Bank("otp", "OTP banka", "OTP", 0, issuesCards = true)
    private val atm = Atm("1", "OTP Črnuče", "Dunajska cesta 380", "Črnuče", GeoPoint(46.1, 14.53), "otp", true, true)

    @Test
    fun `search ignores case and diacritics`() {
        val ui = buildAtmUis(listOf(atm), listOf(nlb, otp), "nlb", null).single()
        assertTrue(ui.matches("crnuce"))
        assertTrue(ui.matches("DUNAJSKA"))
        assertTrue(ui.matches("otp banka"))
        assertFalse(ui.matches("bezigrad"))
    }

    @Test
    fun `quotes use the card bank and distance is optional`() {
        val ui = buildAtmUis(listOf(atm), listOf(nlb, otp), "nlb", null).single()
        assertEquals(Money(100), ui.quote.total)
        assertNull(ui.distanceMeters)
    }

    @Test
    fun `distance formatting`() {
        assertEquals("350 m", formatDistance(347.0))
        assertEquals("1,2 km", formatDistance(1_234.0))
    }
}
