package io.github.itslanmi.withdrawwise.data

import android.annotation.SuppressLint
import android.content.Context
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.itslanmi.withdrawwise.domain.model.GeoPoint
import io.github.itslanmi.withdrawwise.ui.components.hasLocationPermission
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.tasks.await

interface LocationProvider {
    /** A fresh-ish location, or `null` without permission or when no fix is available. */
    suspend fun currentLocation(): GeoPoint?
}

@Singleton
class FusedLocationProvider
@Inject
constructor(@param:ApplicationContext private val context: Context) :
    LocationProvider {
    private val client by lazy { LocationServices.getFusedLocationProviderClient(context) }

    @SuppressLint("MissingPermission")
    override suspend fun currentLocation(): GeoPoint? {
        if (!context.hasLocationPermission()) return null
        return runCatching {
            val location =
                client.getCurrentLocation(
                    Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                    CancellationTokenSource().token
                ).await()
                    ?: client.lastLocation.await()
            location?.let { GeoPoint(it.latitude, it.longitude) }
        }.getOrNull()
    }
}
