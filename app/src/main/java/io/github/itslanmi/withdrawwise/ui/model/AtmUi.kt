package io.github.itslanmi.withdrawwise.ui.model

import io.github.itslanmi.withdrawwise.domain.FeeCalculator
import io.github.itslanmi.withdrawwise.domain.FeeQuote
import io.github.itslanmi.withdrawwise.domain.model.Atm
import io.github.itslanmi.withdrawwise.domain.model.Bank
import io.github.itslanmi.withdrawwise.domain.model.GeoPoint
import java.text.Normalizer
import java.util.Locale
import kotlin.math.roundToInt

/** An ATM as the UI shows it: with its operator, what it costs the user and how far away it is. */
data class AtmUi(val atm: Atm, val bank: Bank, val quote: FeeQuote, val distanceMeters: Double?) {
    val id: String get() = atm.id
    val isFree: Boolean get() = quote.total.isZero

    /** True when the search query matches the name, address, neighbourhood or bank (accent-insensitive). */
    fun matches(query: String): Boolean {
        val needle = query.normalizedForSearch()
        if (needle.isBlank()) return true
        return listOf(atm.name, atm.address, atm.neighbourhood, bank.name).any {
            it.normalizedForSearch().contains(needle)
        }
    }
}

/** Builds [AtmUi]s for a card bank, measuring distance from [reference]. */
fun buildAtmUis(atms: List<Atm>, banks: List<Bank>, cardBankId: String?, reference: GeoPoint?): List<AtmUi> {
    val calculator = FeeCalculator(banks)
    val banksById = banks.associateBy { it.id }
    return atms.mapNotNull { atm ->
        val bank = banksById[atm.bankId] ?: return@mapNotNull null
        AtmUi(
            atm = atm,
            bank = bank,
            quote = calculator.quote(cardBankId, atm),
            distanceMeters = reference?.distanceTo(atm.location)
        )
    }
}

/** "350 m" below a kilometre, "1,2 km" above. */
fun formatDistance(meters: Double): String = if (meters < 1_000) {
    "${(meters / 10).roundToInt() * 10} m"
} else {
    String.format(Locale.forLanguageTag("sl-SI"), "%.1f km", meters / 1_000)
}

private fun String.normalizedForSearch(): String = Normalizer
    .normalize(this, Normalizer.Form.NFD)
    .replace(Regex("\\p{M}+"), "")
    .lowercase(Locale.ROOT)
    .trim()

/** Centre of Ljubljana (Prešernov trg). Used when the user's location is unknown or far away. */
val LjubljanaCenter = GeoPoint(46.05132, 14.50613)

/** Users further than this from the centre are treated as "not in Ljubljana". */
const val CityRadiusMeters = 25_000.0
