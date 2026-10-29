package com.example.prayer

import kotlin.math.*

data class QiblaResult(
    val qiblaAngle: Double, // 0..360 relative to True North
    val distanceKm: Double,
    val directionLabelAr: String
)

object QiblaCalculator {

    const val KAABA_LATITUDE = 21.422487
    const val KAABA_LONGITUDE = 39.826206

    fun calculateQibla(userLat: Double, userLng: Double): QiblaResult {
        val lat1 = Math.toRadians(userLat)
        val lat2 = Math.toRadians(KAABA_LATITUDE)
        val dLng = Math.toRadians(KAABA_LONGITUDE - userLng)

        val y = sin(dLng)
        val x = cos(lat1) * tan(lat2) - sin(lat1) * cos(dLng)

        var qibla = Math.toDegrees(atan2(y, x))
        qibla = (qibla + 360.0) % 360.0

        // Calculate Great Circle Distance in KM (Haversine)
        val earthRadiusKm = 6371.0
        val dLat = lat2 - lat1
        val a = sin(dLat / 2.0).pow(2) + cos(lat1) * cos(lat2) * sin(dLng / 2.0).pow(2)
        val c = 2.0 * atan2(sqrt(a), sqrt(1.0 - a))
        val distance = earthRadiusKm * c

        val label = getDirectionLabel(qibla)

        return QiblaResult(
            qiblaAngle = qibla,
            distanceKm = distance,
            directionLabelAr = label
        )
    }

    private fun getDirectionLabel(angle: Double): String {
        return when (angle) {
            in 337.5..360.0, in 0.0..22.5 -> "شمال"
            in 22.5..67.5 -> "شمال شرق"
            in 67.5..112.5 -> "شرق"
            in 112.5..157.5 -> "جنوب شرق"
            in 157.5..202.5 -> "جنوب"
            in 202.5..247.5 -> "جنوب غرب"
            in 247.5..292.5 -> "غرب"
            in 292.5..337.5 -> "شمال غرب"
            else -> "شمال"
        }
    }
}
