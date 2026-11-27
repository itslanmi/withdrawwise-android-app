package io.github.itslanmi.withdrawwise.domain

import io.github.itslanmi.withdrawwise.domain.model.Atm
import io.github.itslanmi.withdrawwise.domain.model.Bank
import io.github.itslanmi.withdrawwise.domain.model.GeoPoint
import io.github.itslanmi.withdrawwise.domain.model.Money

enum class FeeTier { Free, Low, High }

/**
 * The cost of one withdrawal, split into what your own bank charges ([issuerFee]) and what the
 * ATM operator adds on top ([surcharge]).
 */
data class FeeQuote(val issuerFee: Money, val surcharge: Money) {
    val total: Money get() = issuerFee + surcharge

    val tier: FeeTier
        get() =
            when {
                total.isZero -> FeeTier.Free
                total <= FeeCalculator.LowFeeThreshold -> FeeTier.Low
                else -> FeeTier.High
            }
}

/**
 * Works out what a withdrawal costs for a given card. Rules:
 * - Your own bank's ATMs are always free.
 * - At a partner's ATM your bank waives its fee.
 * - Anywhere else your bank charges its [Bank.otherAtmFee].
 * - Independent operators may add their own [Bank.atmSurcharge] for foreign cards.
 */
class FeeCalculator(banks: Collection<Bank>) {
    private val banksById = banks.associateBy { it.id }

    fun bank(id: String): Bank? = banksById[id]

    fun quote(cardBankId: String?, atm: Atm, amount: Money = TypicalAmount): FeeQuote {
        val cardBank = cardBankId?.let(banksById::get)
        val operator = banksById[atm.bankId]
        if (cardBank == null || operator == null) return FeeQuote(Money.Zero, Money.Zero)
        if (cardBank.id == operator.id) return FeeQuote(Money.Zero, Money.Zero)

        val issuerFee = if (operator.id in cardBank.freePartnerIds) Money.Zero else cardBank.otherAtmFee.feeFor(amount)
        val surcharge = operator.atmSurcharge.feeFor(amount)
        return FeeQuote(issuerFee, surcharge)
    }

    fun isFree(cardBankId: String?, atm: Atm): Boolean = quote(cardBankId, atm).total.isZero

    /** The closest ATM that would be free for this card, with its distance in metres. */
    fun nearestFreeAtm(
        cardBankId: String?,
        from: GeoPoint,
        atms: Collection<Atm>,
        excludeId: String? = null
    ): Pair<Atm, Double>? = atms
        .asSequence()
        .filter { it.id != excludeId && isFree(cardBankId, it) }
        .map { it to from.distanceTo(it.location) }
        .minByOrNull { it.second }

    companion object {
        /** Amount used when showing a fee on the map before the user enters one. */
        val TypicalAmount: Money = Money.euros(50)

        /** Fees up to this amount are shown as "low" (amber) instead of "high" (red). */
        val LowFeeThreshold: Money = Money(150)
    }
}
