package io.github.itslanmi.withdrawwise.domain.model

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

data class GeoPoint(val latitude: Double, val longitude: Double) {
    /** Great-circle distance in metres (haversine formula). */
    fun distanceTo(other: GeoPoint): Double {
        val dLat = Math.toRadians(other.latitude - latitude)
        val dLng = Math.toRadians(other.longitude - longitude)
        val a =
            sin(dLat / 2).pow(2) +
                cos(Math.toRadians(latitude)) * cos(Math.toRadians(other.latitude)) * sin(dLng / 2).pow(2)
        return EarthRadiusMeters * 2 * atan2(sqrt(a), sqrt(1 - a))
    }

    private companion object {
        const val EarthRadiusMeters = 6_371_000.0
    }
}

data class Atm(
    val id: String,
    val name: String,
    val address: String,
    val neighbourhood: String,
    val location: GeoPoint,
    val bankId: String,
    val isOpen24h: Boolean,
    val hasContactless: Boolean
)
