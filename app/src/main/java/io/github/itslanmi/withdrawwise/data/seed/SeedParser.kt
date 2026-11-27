package io.github.itslanmi.withdrawwise.data.seed

import io.github.itslanmi.withdrawwise.domain.model.Atm
import io.github.itslanmi.withdrawwise.domain.model.Bank
import io.github.itslanmi.withdrawwise.domain.model.FeeRule
import io.github.itslanmi.withdrawwise.domain.model.GeoPoint
import io.github.itslanmi.withdrawwise.domain.model.Money
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
internal data class BanksFile(val banks: List<BankDto>)

@Serializable
internal data class BankDto(
    val id: String,
    val name: String,
    val shortName: String,
    val color: String,
    val issuesCards: Boolean,
    val otherAtmFee: FeeRuleDto = FeeRuleDto(),
    val freePartners: List<String> = emptyList(),
    val atmSurcharge: FeeRuleDto = FeeRuleDto()
)

/** Fee rule as written in JSON: euros as decimals and the percentage as e.g. `0.3` for 0.3 %. */
@Serializable
internal data class FeeRuleDto(
    val flat: Double = 0.0,
    val percent: Double = 0.0,
    val min: Double = 0.0,
    val max: Double? = null
)

@Serializable
internal data class AtmsFile(val city: String, val atms: List<AtmDto>)

@Serializable
internal data class AtmDto(
    val id: String,
    val name: String,
    val address: String,
    val area: String,
    val lat: Double,
    val lng: Double,
    val bank: String,
    val open24h: Boolean = false,
    val contactless: Boolean = false
)

/** Turns the bundled seed JSON into domain models. Pure and JVM-testable. */
object SeedParser {
    private val json = Json { ignoreUnknownKeys = true }

    fun parseBanks(raw: String): List<Bank> = json.decodeFromString<BanksFile>(raw).banks.map { it.toDomain() }

    fun parseAtms(raw: String): List<Atm> = json.decodeFromString<AtmsFile>(raw).atms.map { it.toDomain() }

    private fun BankDto.toDomain() = Bank(
        id = id,
        name = name,
        shortName = shortName,
        colorArgb = parseColor(color),
        issuesCards = issuesCards,
        otherAtmFee = otherAtmFee.toDomain(),
        freePartnerIds = freePartners.toSet(),
        atmSurcharge = atmSurcharge.toDomain()
    )

    private fun FeeRuleDto.toDomain() = FeeRule(
        flat = Money.ofEuros(flat),
        percentBasisPoints = Math.round(percent * 100).toInt(),
        min = Money.ofEuros(min),
        max = max?.let(Money::ofEuros)
    )

    private fun AtmDto.toDomain() = Atm(
        id = id,
        name = name,
        address = address,
        neighbourhood = area,
        location = GeoPoint(lat, lng),
        bankId = bank,
        isOpen24h = open24h,
        hasContactless = contactless
    )

    /** Parses `#RRGGBB` into an opaque ARGB long. */
    internal fun parseColor(hex: String): Long {
        val clean = hex.removePrefix("#")
        require(clean.length == 6) { "Expected #RRGGBB but was $hex" }
        return 0xFF000000 or clean.toLong(16)
    }
}
