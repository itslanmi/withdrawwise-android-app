package io.github.itslanmi.withdrawwise.domain.model

/**
 * A bank, or an independent ATM operator.
 *
 * @property colorArgb brand-ish colour used for the bank's monogram tile.
 * @property issuesCards whether people can hold a card from this bank (independent operators can't).
 * @property otherAtmFee what this bank charges its own cardholders for using someone else's ATM.
 * @property freePartnerIds operators whose ATMs this bank's cardholders can use without the [otherAtmFee].
 * @property atmSurcharge what this operator adds on top when a card from another bank is used at its ATMs.
 */
data class Bank(
    val id: String,
    val name: String,
    val shortName: String,
    val colorArgb: Long,
    val issuesCards: Boolean,
    val otherAtmFee: FeeRule = FeeRule.Free,
    val freePartnerIds: Set<String> = emptySet(),
    val atmSurcharge: FeeRule = FeeRule.Free
)
