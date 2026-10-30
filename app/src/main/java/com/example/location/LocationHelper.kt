package com.example.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import androidx.core.content.ContextCompat
import com.example.prayer.CityCoordinates
import com.example.prayer.CityInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.math.*

sealed class LocationFetchResult {
    data class Success(
        val latitude: Double,
        val longitude: Double,
        val provider: String,
        val cityName: String,
        val countryName: String
    ) : LocationFetchResult()

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

    /**
     * Resolves the exact city, village, town, or locality name in Arabic.
     * Tries the system Geocoder first, with fallbacks to closest known city.
     */
    suspend fun reverseGeocode(context: Context, latitude: Double, longitude: Double): Pair<String, String> = withContext(Dispatchers.IO) {
        // 1. Try Android Geocoder with Arabic locale
        try {
            if (Geocoder.isPresent()) {
                val geocoder = Geocoder(context, Locale("ar"))
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(latitude, longitude, 5)
                if (!addresses.isNullOrEmpty()) {
                    for (addr in addresses) {
                        val villageOrSubLocality = addr.subLocality?.trim()
                        val cityOrLocality = addr.locality?.trim()
                        val districtOrSubAdmin = addr.subAdminArea?.trim()
                        val provinceOrAdmin = addr.adminArea?.trim()
                        val feature = addr.featureName?.trim()
                        val country = addr.countryName?.trim() ?: "المملكة المغربية"

                        // Build the most descriptive Arabic name (village, town, city)
                        val resolvedName = when {
                            !villageOrSubLocality.isNullOrBlank() && !cityOrLocality.isNullOrBlank() && villageOrSubLocality != cityOrLocality ->
                                "$villageOrSubLocality ($cityOrLocality)"
                            !villageOrSubLocality.isNullOrBlank() -> villageOrSubLocality
                            !cityOrLocality.isNullOrBlank() -> cityOrLocality
                            !districtOrSubAdmin.isNullOrBlank() -> districtOrSubAdmin
                            !provinceOrAdmin.isNullOrBlank() -> provinceOrAdmin
                            !feature.isNullOrBlank() && feature.toIntOrNull() == null -> feature
                            else -> null
                        }

                        if (!resolvedName.isNullOrBlank()) {
                            return@withContext Pair(resolvedName, country)
                        }
                    }
                }
            }
        } catch (_: Exception) {
            // Geocoder service unavailable or offline, continue to local fallback
        }

        // 2. Offline Fallback: Find closest city from internal database using Haversine
        val closest = findClosestCity(latitude, longitude)
        if (closest != null) {
            val dist = calculateDistanceKm(latitude, longitude, closest.latitude, closest.longitude)
            val name = if (dist <= 15.0) {
                closest.cityNameAr
            } else if (dist <= 75.0) {
                "قرب ${closest.cityNameAr}"
            } else {
                closest.cityNameAr
            }
            return@withContext Pair(name, closest.countryNameAr)
        }

        // 3. Coordinate-based fallback
        val latStr = String.format(Locale.US, "%.3f", latitude)
        val lngStr = String.format(Locale.US, "%.3f", longitude)
        return@withContext Pair("موقع ($latStr, $lngStr)", "تحديد GPS")
    }

    fun findClosestCity(lat: Double, lng: Double): CityInfo? {
        return CityCoordinates.majorCities.minByOrNull { city ->
            calculateDistanceKm(lat, lng, city.latitude, city.longitude)
        }
    }

    fun calculateDistanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2) + cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return 6371.0 * c
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

        // If last known is very recent (less than 15 minutes), return it directly with geocoding
        val recentThreshold = System.currentTimeMillis() - 15 * 60 * 1000L
        if (bestLast != null && bestLast.time > recentThreshold) {
            val (city, country) = reverseGeocode(context, bestLast.latitude, bestLast.longitude)
            return@withContext LocationFetchResult.Success(
                latitude = bestLast.latitude,
                longitude = bestLast.longitude,
                provider = bestLast.provider ?: "cached",
                cityName = city,
                countryName = country
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
                } catch (_: Exception) {
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
            val (city, country) = reverseGeocode(context, freshLocation.latitude, freshLocation.longitude)
            return@withContext LocationFetchResult.Success(
                latitude = freshLocation.latitude,
                longitude = freshLocation.longitude,
                provider = freshLocation.provider ?: "fresh",
                cityName = city,
                countryName = country
            )
        }

        // 3. Fallback to best last known location even if older
        if (bestLast != null) {
            val (city, country) = reverseGeocode(context, bestLast.latitude, bestLast.longitude)
            return@withContext LocationFetchResult.Success(
                latitude = bestLast.latitude,
                longitude = bestLast.longitude,
                provider = "cached",
                cityName = city,
                countryName = country
            )
        }

        // 4. Return NoSignal with fallback coordinates
        return@withContext LocationFetchResult.NoSignal(fallbackLat, fallbackLng)
    }
}
