package com.example.prayer

import java.util.Calendar
import java.util.Locale
import kotlin.math.*

data class PrayerTimesResult(
    val fajr: String,
    val sunrise: String,
    val dhuhr: String,
    val asr: String,
    val maghrib: String,
    val isha: String,
    val nextPrayerName: String,
    val nextPrayerTime: String,
    val remainingMinutes: Long,
    val remainingFormatted: String,
    val imsak: String = "",
    val midnight: String = ""
)

object PrayerCalculator {

    enum class CalculationMethod(
        val displayNameAr: String,
        val fajrAngle: Double,
        val ishaAngle: Double,
        val ishaFixedMinutes: Int = 0
    ) {
        UMM_AL_QURA("جامعة أم القرى (مكة المكرمة)", 18.5, 0.0, 90),
        MOROCCO_HABOUS("وزارة الأوقاف والشؤون الإسلامية بالمملكة المغربية", 19.0, 17.0, 0),
        MWL("رابطة العالم الإسلامي", 18.0, 17.0, 0),
        EGYPTIAN("الهيئة المصرية العامة للمساحة", 19.5, 17.5, 0),
        KARACHI("جامعة العلوم الإسلامية بكراتشي", 18.0, 18.0, 0),
        ISNA("الجمعية الإسلامية لأمريكا الشمالية (ISNA)", 15.0, 15.0, 0),
        FRANCE_UOIF("اتحاد المنظمات الإسلامية في فرنسا (UOIF)", 12.0, 12.0, 0),
        CUSTOM("طريقة مخصصة", 18.0, 17.0, 0);

        companion object {
            fun fromString(name: String?): CalculationMethod {
                if (name == null) return UMM_AL_QURA
                return try {
                    valueOf(name)
                } catch (_: Exception) {
                    UMM_AL_QURA
                }
            }
        }
    }

    enum class AsrJuristic(val displayNameAr: String, val shadowMultiplier: Double) {
        SHAFI("الجمهور (الشافعي، المالكي، الحنبلي)", 1.0),
        HANAFI("المذهب الحنفي", 2.0);

        companion object {
            fun fromString(name: String?): AsrJuristic {
                if (name == null) return SHAFI
                return try {
                    valueOf(name)
                } catch (_: Exception) {
                    SHAFI
                }
            }
        }
    }

    enum class HighLatitudeRule(val displayNameAr: String) {
        NONE("بدون تعديل (المعادلة الفلكية القياسية مع حماية تلقائية)"),
        MIDNIGHT("منتصف الليل (نصف الفترة بين الغروب والشروق)"),
        SEVENTH_NIGHT("سُبع الليل (١/٧ من طول الليل)"),
        ANGLE_BASED("النسبة المئوية للزاوية (زاوية الفجر / ٦٠)");

        companion object {
            fun fromString(name: String?): HighLatitudeRule {
                if (name == null) return NONE
                return try {
                    valueOf(name)
                } catch (_: Exception) {
                    NONE
                }
            }
        }
    }

    fun calculateTimes(
        latitude: Double,
        longitude: Double,
        calendar: Calendar = Calendar.getInstance(),
        method: CalculationMethod = CalculationMethod.UMM_AL_QURA,
        asrJuristic: AsrJuristic = AsrJuristic.SHAFI,
        highLatRule: HighLatitudeRule = HighLatitudeRule.NONE,
        customFajrAngle: Double = 18.0,
        customIshaAngle: Double = 17.0,
        fajrAdj: Int = 0,
        dhuhrAdj: Int = 0,
        asrAdj: Int = 0,
        maghribAdj: Int = 0,
        ishaAdj: Int = 0
    ): PrayerTimesResult {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        // TimeZone offset including DST automatically
        val timezoneOffsetHours = calendar.timeZone.getOffset(calendar.timeInMillis).toDouble() / (1000.0 * 3600.0)

        // Julian date calculation
        val a = floor((14.0 - month) / 12.0)
        val y = year + 4800.0 - a
        val m = month + 12.0 * a - 3.0
        val julianDate = day + floor((153.0 * m + 2.0) / 5.0) + 365.0 * y + floor(y / 4.0) - floor(y / 100.0) + floor(y / 400.0) - 32045.0
        val d = julianDate - 2451545.0

        // Sun coordinates
        val g = fixAngle(357.529 + 0.98560028 * d)
        val q = fixAngle(280.459 + 0.98564736 * d)
        val l = fixAngle(q + 1.915 * sin(degToRad(g)) + 0.020 * sin(degToRad(2.0 * g)))
        val e = 23.439 - 0.00000036 * d

        val ra = fixAngle(radToDeg(atan2(cos(degToRad(e)) * sin(degToRad(l)), cos(degToRad(l))))) / 15.0
        val declination = radToDeg(asin(sin(degToRad(e)) * sin(degToRad(l))))
        val equationOfTime = q / 15.0 - fixHour(ra)

        // Midday (Dhuhr) in local hours
        val noonUtc = 12.0 - equationOfTime - (longitude / 15.0)
        val dhuhrHour = fixHour(noonUtc + timezoneOffsetHours)

        // Sunrise & Sunset angle is 0.833 degrees below horizon (sun semi-diameter + refraction)
        val sunAlt = -0.833
        val sunriseHour = dhuhrHour - hourAngle(sunAlt, latitude, declination)
        val sunsetHour = dhuhrHour + hourAngle(sunAlt, latitude, declination)

        // Effective Fajr & Isha angles
        val fAngle = if (method == CalculationMethod.CUSTOM) customFajrAngle else method.fajrAngle
        val iAngle = if (method == CalculationMethod.CUSTOM) customIshaAngle else method.ishaAngle

        // Fajr calculation
        var fajrHour = dhuhrHour - hourAngle(-fAngle, latitude, declination)

        // Asr calculation
        val asrAngle = -radToDeg(atan(1.0 / (asrJuristic.shadowMultiplier + tan(degToRad(abs(latitude - declination))))))
        val asrHour = dhuhrHour + hourAngle(-asrAngle, latitude, declination)

        // Maghrib
        val maghribHour = sunsetHour

        // Isha calculation: for Umm Al-Qura, 90 mins normally, 120 mins in Ramadan
        val isRamadan = HijriCalendarHelper.isRamadan(calendar)
        val effectiveIshaFixedMinutes = when {
            method == CalculationMethod.UMM_AL_QURA -> if (isRamadan) 120 else 90
            else -> method.ishaFixedMinutes
        }

        var ishaHour = if (effectiveIshaFixedMinutes > 0 && method != CalculationMethod.CUSTOM) {
            maghribHour + (effectiveIshaFixedMinutes / 60.0)
        } else {
            dhuhrHour + hourAngle(-iAngle, latitude, declination)
        }

        // Apply High Latitude Adjustments if requested or if night is abnormal / polar twilight
        val nightDurationHours = (24.0 - sunsetHour + sunriseHour) % 24.0
        when (highLatRule) {
            HighLatitudeRule.MIDNIGHT -> {
                val halfNight = nightDurationHours / 2.0
                if (fajrHour < sunriseHour - halfNight || fajrHour.isNaN()) fajrHour = sunriseHour - halfNight
                if (ishaHour > sunsetHour + halfNight || ishaHour.isNaN()) ishaHour = sunsetHour + halfNight
            }
            HighLatitudeRule.SEVENTH_NIGHT -> {
                val seventh = nightDurationHours / 7.0
                if (fajrHour < sunriseHour - seventh || fajrHour.isNaN()) fajrHour = sunriseHour - seventh
                if (ishaHour > sunsetHour + seventh || ishaHour.isNaN()) ishaHour = sunsetHour + seventh
            }
            HighLatitudeRule.ANGLE_BASED -> {
                val fajrFrac = (fAngle / 60.0) * nightDurationHours
                val ishaFrac = (iAngle / 60.0) * nightDurationHours
                if (fajrHour < sunriseHour - fajrFrac || fajrHour.isNaN()) fajrHour = sunriseHour - fajrFrac
                if (ishaHour > sunsetHour + ishaFrac || ishaHour.isNaN()) ishaHour = sunsetHour + ishaFrac
            }
            HighLatitudeRule.NONE -> {
                // Built-in automatic safety fallback if NaN (polar regions/extreme latitudes)
                if (fajrHour.isNaN()) fajrHour = sunriseHour - (nightDurationHours / 7.0)
                if (ishaHour.isNaN()) ishaHour = sunsetHour + (nightDurationHours / 7.0)
            }
        }

        // Apply manual adjustments in minutes
        val fajrMinutes = toTotalMinutes(fajrHour) + fajrAdj
        val sunriseMinutes = toTotalMinutes(sunriseHour)
        val dhuhrMinutes = toTotalMinutes(dhuhrHour) + dhuhrAdj
        val asrMinutes = toTotalMinutes(asrHour) + asrAdj
        val maghribMinutes = toTotalMinutes(maghribHour) + maghribAdj
        val ishaMinutes = toTotalMinutes(ishaHour) + ishaAdj

        // Imsak: 10 minutes before Fajr
        val imsakMinutes = (fajrMinutes - 10 + 24 * 60) % (24 * 60)

        // Islamic Midnight: halfway between Maghrib and Fajr
        val nightMins = ((fajrMinutes - maghribMinutes) % (24 * 60) + 24 * 60) % (24 * 60)
        val midnightMinutes = (maghribMinutes + (nightMins / 2)) % (24 * 60)

        val currentMinutes = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE)

        // Determine next prayer among the five daily prayers + sunrise
        val prayerList = listOf(
            Triple("الفجر", fajrMinutes, formatMinutes(fajrMinutes)),
            Triple("الشروق", sunriseMinutes, formatMinutes(sunriseMinutes)),
            Triple("الظهر", dhuhrMinutes, formatMinutes(dhuhrMinutes)),
            Triple("العصر", asrMinutes, formatMinutes(asrMinutes)),
            Triple("المغرب", maghribMinutes, formatMinutes(maghribMinutes)),
            Triple("العشاء", ishaMinutes, formatMinutes(ishaMinutes))
        )

        var nextPrayer = prayerList.firstOrNull { it.second > currentMinutes }
        val remainingMinutes: Long
        if (nextPrayer != null) {
            remainingMinutes = (nextPrayer.second - currentMinutes).toLong()
        } else {
            val tomorrowFajrMinutes = fajrMinutes + 24 * 60
            remainingMinutes = (tomorrowFajrMinutes - currentMinutes).toLong()
            nextPrayer = prayerList[0]
        }

        val remHours = remainingMinutes / 60
        val remMins = remainingMinutes % 60
        val remainingFormatted = if (remHours > 0) {
            "$remHours س و $remMins د"
        } else {
            "$remMins دقيقة"
        }

        return PrayerTimesResult(
            fajr = formatMinutes(fajrMinutes),
            sunrise = formatMinutes(sunriseMinutes),
            dhuhr = formatMinutes(dhuhrMinutes),
            asr = formatMinutes(asrMinutes),
            maghrib = formatMinutes(maghribMinutes),
            isha = formatMinutes(ishaMinutes),
            nextPrayerName = nextPrayer.first,
            nextPrayerTime = nextPrayer.third,
            remainingMinutes = remainingMinutes,
            remainingFormatted = remainingFormatted,
            imsak = formatMinutes(imsakMinutes),
            midnight = formatMinutes(midnightMinutes)
        )
    }

    private fun hourAngle(altitude: Double, latitude: Double, declination: Double): Double {
        val cosHA = (sin(degToRad(altitude)) - sin(degToRad(latitude)) * sin(degToRad(declination))) /
                (cos(degToRad(latitude)) * cos(degToRad(declination)))
        val clamped = cosHA.coerceIn(-1.0, 1.0)
        return radToDeg(acos(clamped)) / 15.0
    }

    private fun toTotalMinutes(timeInHours: Double): Int {
        val normalized = fixHour(timeInHours)
        val hours = floor(normalized).toInt()
        val minutes = floor((normalized - hours) * 60.0).toInt()
        return hours * 60 + minutes
    }

    fun formatMinutes(totalMinutes: Int): String {
        val normalized = (totalMinutes % (24 * 60) + 24 * 60) % (24 * 60)
        val hours = normalized / 60
        val minutes = normalized % 60
        val period = if (hours < 12) "ص" else "م"
        val displayHours = when (hours) {
            0 -> 12
            in 1..12 -> hours
            else -> hours - 12
        }
        return String.format(Locale.getDefault(), "%02d:%02d %s", displayHours, minutes, period)
    }

    private fun degToRad(degrees: Double) = degrees * (PI / 180.0)
    private fun radToDeg(radians: Double) = radians * (180.0 / PI)
    private fun fixAngle(a: Double) = (a % 360.0 + 360.0) % 360.0
    private fun fixHour(h: Double) = (h % 24.0 + 24.0) % 24.0
}
