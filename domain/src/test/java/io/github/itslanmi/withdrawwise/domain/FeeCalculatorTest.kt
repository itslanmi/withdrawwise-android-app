package io.github.itslanmi.withdrawwise.domain

import io.github.itslanmi.withdrawwise.domain.model.Atm
import io.github.itslanmi.withdrawwise.domain.model.Bank
import io.github.itslanmi.withdrawwise.domain.model.FeeRule
import io.github.itslanmi.withdrawwise.domain.model.GeoPoint
import io.github.itslanmi.withdrawwise.domain.model.Money
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FeeCalculatorTest {
    private val home =
        Bank(
            id = "home",
            name = "Home Bank",
            shortName = "HB",
            colorArgb = 0,
            issuesCards = true,
            otherAtmFee = FeeRule(flat = Money(100)),
            freePartnerIds = setOf("partner")
        )
    private val partner =
        Bank("partner", "Partner", "PB", 0, issuesCards = true, otherAtmFee = FeeRule(flat = Money(90)))
    private val other = Bank("other", "Other", "OB", 0, issuesCards = true, otherAtmFee = FeeRule(flat = Money(150)))
    private val independent =
        Bank("indie", "Indie ATMs", "IA", 0, issuesCards = false, atmSurcharge = FeeRule(flat = Money(295)))

    private val calculator = FeeCalculator(listOf(home, partner, other, independent))

    private fun atm(id: String, bankId: String, lat: Double = 46.05, lng: Double = 14.50) =
        Atm(id, id, "", "", GeoPoint(lat, lng), bankId, isOpen24h = true, hasContactless = true)

    @Test
    fun `own bank ATM is free`() {
        val quote = calculator.quote("home", atm("a", "home"))
        assertEquals(Money.Zero, quote.total)
        assertEquals(FeeTier.Free, quote.tier)
    }

    @Test
    fun `partner ATM waives the issuer fee`() {
        assertEquals(Money.Zero, calculator.quote("home", atm("a", "partner")).total)
    }

    @Test
    fun `other bank ATM charges the issuer fee`() {
        val quote = calculator.quote("home", atm("a", "other"))
        assertEquals(Money(100), quote.issuerFee)
        assertEquals(Money.Zero, quote.surcharge)
        assertEquals(FeeTier.Low, quote.tier)
    }

    @Test
    fun `independent operator adds a surcharge on top`() {
        val quote = calculator.quote("home", atm("a", "indie"))
        assertEquals(Money(100), quote.issuerFee)
        assertEquals(Money(295), quote.surcharge)
        assertEquals(Money(395), quote.total)
        assertEquals(FeeTier.High, quote.tier)
    }

    @Test
    fun `unknown card bank results in no fee`() {
        assertEquals(Money.Zero, calculator.quote(null, atm("a", "other")).total)
    }

    @Test
    fun `percentage rule respects min and max`() {
        val rule = FeeRule(flat = Money(50), percentBasisPoints = 50, min = Money(100), max = Money(300))
        assertEquals(Money(100), rule.feeFor(Money.euros(20))) // 0.50 + 0.10 -> min 1.00
        assertEquals(Money(300), rule.feeFor(Money.euros(1_000))) // 0.50 + 5.00 -> max 3.00
        assertEquals(Money(150), rule.feeFor(Money.euros(200))) // 0.50 + 1.00
    }

    @Test
    fun `nearest free ATM skips paid ones and the excluded ATM`() {
        val from = GeoPoint(46.0500, 14.5000)
        val atms =
            listOf(
                atm("paid-close", "other", 46.0501, 14.5001),
                atm("free-close", "home", 46.0510, 14.5000),
                atm("free-far", "partner", 46.0600, 14.5000)
            )
        val (nearest, distance) = calculator.nearestFreeAtm("home", from, atms)!!
        assertEquals("free-close", nearest.id)
        assertTrue("was $distance", distance in 100.0..120.0)

        assertEquals("free-far", calculator.nearestFreeAtm("home", from, atms, excludeId = "free-close")!!.first.id)
        assertNull(calculator.nearestFreeAtm("other", from, atms.filter { it.bankId == "home" }))
    }

    @Test
    fun `haversine distance is accurate for city scale`() {
        // Prešernov trg to Ljubljana railway station is roughly 900 m.
        val distance = GeoPoint(46.05132, 14.50613).distanceTo(GeoPoint(46.05800, 14.51050))
        assertTrue("was $distance", distance in 780.0..880.0)
    }
}
