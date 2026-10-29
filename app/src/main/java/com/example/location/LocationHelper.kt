package com.example.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

sealed class LocationFetchResult {
    data class Success(val latitude: Double, val longitude: Double, val provider: String) : LocationFetchResult()
    object PermissionDenied : LocationFetchResult()
    object GpsDisabled : LocationFetchResult()
    data class NoSignal(val fallbackLat: Double, val fallbackLng: Double) : LocationFetchResult()
    data class Error(val messageAr: String) : LocationFetchResult()
}

object LocationHelper {

    fun hasLocationPermission(context: Context): Boolean {
        val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    fun isLocationEnabled(context: Context): Boolean {
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return false
        val gps = try { lm.isProviderEnabled(LocationManager.GPS_PROVIDER) } catch (_: Exception) { false }
        val net = try { lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER) } catch (_: Exception) { false }
        return gps || net
    }

    fun openLocationSettings(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            try {
                val intent = Intent(Settings.ACTION_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } catch (_: Exception) {}
        }
    }

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocationWithTimeout(
        context: Context,
        fallbackLat: Double = 21.4225,
        fallbackLng: Double = 39.8262,
        timeoutMs: Long = 8000L
    ): LocationFetchResult = withContext(Dispatchers.Main) {
        if (!hasLocationPermission(context)) {
            return@withContext LocationFetchResult.PermissionDenied
        }

        if (!isLocationEnabled(context)) {
            return@withContext LocationFetchResult.GpsDisabled
        }

        val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            ?: return@withContext LocationFetchResult.Error("خدمة تحديد الموقع غير متوفرة")

        // 1. Check last known locations first
        val lastGps = try { lm.getLastKnownLocation(LocationManager.GPS_PROVIDER) } catch (_: Exception) { null }
        val lastNet = try { lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER) } catch (_: Exception) { null }
        val bestLast = when {
            lastGps != null && lastNet != null -> if (lastGps.time >= lastNet.time) lastGps else lastNet
            lastGps != null -> lastGps
            else -> lastNet
        }

        // If last known is very recent (less than 15 minutes), return it directly
        val recentThreshold = System.currentTimeMillis() - 15 * 60 * 1000L
        if (bestLast != null && bestLast.time > recentThreshold) {
            return@withContext LocationFetchResult.Success(
                bestLast.latitude,
                bestLast.longitude,
                bestLast.provider ?: "cached"
            )
        }

        // 2. Request a fresh single location update with timeout
        val freshLocation: Location? = withTimeoutOrNull(timeoutMs) {
            suspendCancellableCoroutine { cont ->
                val listener = object : LocationListener {
                    override fun onLocationChanged(location: Location) {
                        try { lm.removeUpdates(this) } catch (_: Exception) {}
                        if (cont.isActive) {
                            cont.resume(location)
                        }
                    }
                    @Deprecated("Deprecated in Java")
                    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                    override fun onProviderEnabled(provider: String) {}
                    override fun onProviderDisabled(provider: String) {}
                }

                var requested = false
                try {
                    if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0L, 0f, listener, Looper.getMainLooper())
                        requested = true
                    }
                    if (lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                        lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0L, 0f, listener, Looper.getMainLooper())
                        requested = true
                    }
                } catch (e: Exception) {
                    try { lm.removeUpdates(listener) } catch (_: Exception) {}
                    if (cont.isActive) cont.resume(null)
                    return@suspendCancellableCoroutine
                }

                if (!requested) {
                    if (cont.isActive) cont.resume(null)
                }

                cont.invokeOnCancellation {
                    try { lm.removeUpdates(listener) } catch (_: Exception) {}
                }
            }
        }

        if (freshLocation != null) {
            return@withContext LocationFetchResult.Success(
                freshLocation.latitude,
                freshLocation.longitude,
                freshLocation.provider ?: "fresh"
            )
        }

        // 3. Fallback to best last known location even if older
        if (bestLast != null) {
            return@withContext LocationFetchResult.Success(
                bestLast.latitude,
                bestLast.longitude,
                "cached"
            )
        }

        // 4. Return NoSignal with fallback coordinates
        return@withContext LocationFetchResult.NoSignal(fallbackLat, fallbackLng)
    }
}
