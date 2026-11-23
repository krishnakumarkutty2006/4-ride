package com.example.data.repository

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

data class GpsCoordinates(
    val lat: Double,
    val lng: Double,
    val isLiveGps: Boolean
)

class LocationService(private val context: Context) {

    private val fusedClient by lazy {
        LocationServices.getFusedLocationProviderClient(context.applicationContext)
    }

    fun hasLocationPermission(): Boolean {
        val fine = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    @SuppressLint("MissingPermission")
    suspend fun getCurrentCoordinates(): GpsCoordinates {
        if (!hasLocationPermission()) {
            return GpsCoordinates(LocationCatalog.DEFAULT_LAT, LocationCatalog.DEFAULT_LNG, false)
        }
        return suspendCancellableCoroutine { cont ->
            try {
                val cts = CancellationTokenSource()
                cont.invokeOnCancellation { cts.cancel() }
                fusedClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, cts.token)
                    .addOnSuccessListener { location ->
                        if (location != null) {
                            cont.resume(GpsCoordinates(location.latitude, location.longitude, true))
                        } else {
                            fusedClient.lastLocation
                                .addOnSuccessListener { last ->
                                    if (last != null) {
                                        cont.resume(GpsCoordinates(last.latitude, last.longitude, true))
                                    } else {
                                        cont.resume(GpsCoordinates(LocationCatalog.DEFAULT_LAT, LocationCatalog.DEFAULT_LNG, false))
                                    }
                                }
                                .addOnFailureListener {
                                    cont.resume(GpsCoordinates(LocationCatalog.DEFAULT_LAT, LocationCatalog.DEFAULT_LNG, false))
                                }
                        }
                    }
                    .addOnFailureListener {
                        cont.resume(GpsCoordinates(LocationCatalog.DEFAULT_LAT, LocationCatalog.DEFAULT_LNG, false))
                    }
            } catch (_: SecurityException) {
                cont.resume(GpsCoordinates(LocationCatalog.DEFAULT_LAT, LocationCatalog.DEFAULT_LNG, false))
            } catch (_: Exception) {
                cont.resume(GpsCoordinates(LocationCatalog.DEFAULT_LAT, LocationCatalog.DEFAULT_LNG, false))
            }
        }
    }
}
