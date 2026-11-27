package io.github.itslanmi.withdrawwise.data

import io.github.itslanmi.withdrawwise.data.seed.SeedParser
import io.github.itslanmi.withdrawwise.domain.FeeCalculator
import io.github.itslanmi.withdrawwise.domain.FeeTier
import io.github.itslanmi.withdrawwise.domain.model.Money
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Guards the bundled demo data so a typo in the JSON never ships a broken map. */
class SeedDataTest {
    private val banks = SeedParser.parseBanks(File("src/main/assets/seed/banks.json").readText())
    private val atms = SeedParser.parseAtms(File("src/main/assets/seed/atms_ljubljana.json").readText())
    private val calculator = FeeCalculator(banks)

    @Test
    fun `ids are unique`() {
        assertEquals(banks.size, banks.map { it.id }.toSet().size)
        assertEquals(atms.size, atms.map { it.id }.toSet().size)
    }

    @Test
    fun `every ATM belongs to a known bank and every partner exists`() {
        val bankIds = banks.map { it.id }.toSet()
        atms.forEach { assertTrue("${it.id} has unknown bank ${it.bankId}", it.bankId in bankIds) }
        banks.flatMap { it.freePartnerIds }.forEach { assertTrue("unknown partner $it", it in bankIds) }
    }

    @Test
    fun `every ATM lies within Ljubljana`() {
        atms.forEach {
            assertTrue("${it.id} latitude", it.location.latitude in 45.98..46.13)
            assertTrue("${it.id} longitude", it.location.longitude in 14.40..14.62)
        }
    }

    @Test
    fun `every card-issuing bank has at least one free ATM in the city`() {
        banks.filter { it.issuesCards }.forEach { bank ->
            assertTrue("${bank.id} has no free ATM", atms.any { calculator.isFree(bank.id, it) })
        }
    }

    @Test
    fun `fees are parsed from euros and percentages`() {
        val intesa = banks.single { it.id == "intesa" }
        assertEquals(Money(75), intesa.otherAtmFee.flat)
        assertEquals(30, intesa.otherAtmFee.percentBasisPoints)
        assertEquals(Money(100), intesa.otherAtmFee.min)
        assertEquals(0xFF0E9F8FL, intesa.colorArgb)
    }

    @Test
    fun `independent operators are always expensive`() {
        val euronet = atms.first { it.bankId == "euronet" }
        banks.filter { it.issuesCards }.forEach {
            assertEquals(FeeTier.High, calculator.quote(it.id, euronet).tier)
        }
    }
}
