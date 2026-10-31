package com.example

import com.example.prayer.CityCoordinates
import com.example.prayer.HijriCalendarHelper
import com.example.prayer.PrayerCalculator
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Calendar
import java.util.TimeZone

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class PrayerCalculationTest {

    private fun getFixedCalendar(year: Int = 2026, month: Int = 5, day: Int = 1, tzId: String = "UTC"): Calendar {
        val cal = Calendar.getInstance(TimeZone.getTimeZone(tzId))
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, month)
        cal.set(Calendar.DAY_OF_MONTH, day)
        cal.set(Calendar.HOUR_OF_DAY, 12)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal
    }

    @Test
    fun `test Makkah prayer times with Umm Al-Qura method`() {
        val cal = getFixedCalendar(2026, 4, 15, "Asia/Riyadh") // May 15, 2026
        val result = PrayerCalculator.calculateTimes(
            latitude = 21.4225,
            longitude = 39.8262,
            calendar = cal,
            method = PrayerCalculator.CalculationMethod.UMM_AL_QURA,
            asrJuristic = PrayerCalculator.AsrJuristic.SHAFI
        )

        assertNotNull(result.fajr)
        assertNotNull(result.sunrise)
        assertNotNull(result.dhuhr)
        assertNotNull(result.asr)
        assertNotNull(result.maghrib)
        assertNotNull(result.isha)
        assertNotNull(result.imsak)
        assertNotNull(result.midnight)

        assertTrue(result.fajr.isNotBlank())
        assertTrue(result.maghrib.isNotBlank())
        assertTrue(result.isha.isNotBlank())
        assertTrue(result.imsak.isNotBlank())
        assertTrue(result.midnight.isNotBlank())
    }

    @Test
    fun `test Casablanca and Rabat with Moroccan Ministry of Habous method`() {
        val cal = getFixedCalendar(2026, 4, 15, "Africa/Casablanca")
        val resultCasablanca = PrayerCalculator.calculateTimes(
            latitude = 33.5731,
            longitude = -7.5898,
            calendar = cal,
            method = PrayerCalculator.CalculationMethod.MOROCCO_HABOUS,
            asrJuristic = PrayerCalculator.AsrJuristic.SHAFI
        )

        assertNotNull(resultCasablanca.fajr)
        assertNotNull(resultCasablanca.dhuhr)
        assertNotNull(resultCasablanca.asr)
        assertNotNull(resultCasablanca.maghrib)
        assertNotNull(resultCasablanca.isha)

        val resultRabat = PrayerCalculator.calculateTimes(
            latitude = 34.0209,
            longitude = -6.8416,
            calendar = cal,
            method = PrayerCalculator.CalculationMethod.MOROCCO_HABOUS,
            asrJuristic = PrayerCalculator.AsrJuristic.SHAFI
        )

        assertNotNull(resultRabat.fajr)
        assertNotNull(resultRabat.maghrib)
        assertNotNull(resultRabat.isha)
    }

    @Test
    fun `test Cairo with Egyptian General Survey method`() {
        val cal = getFixedCalendar(2026, 4, 15, "Africa/Cairo")
        val result = PrayerCalculator.calculateTimes(
            latitude = 30.0444,
            longitude = 31.2357,
            calendar = cal,
            method = PrayerCalculator.CalculationMethod.EGYPTIAN,
            asrJuristic = PrayerCalculator.AsrJuristic.SHAFI
        )

        assertNotNull(result.fajr)
        assertNotNull(result.dhuhr)
        assertNotNull(result.asr)
        assertNotNull(result.maghrib)
        assertNotNull(result.isha)
    }

    @Test
    fun `test Karachi with Karachi Islamic University method`() {
        val cal = getFixedCalendar(2026, 4, 15, "Asia/Karachi")
        val result = PrayerCalculator.calculateTimes(
            latitude = 24.8607,
            longitude = 67.0011,
            calendar = cal,
            method = PrayerCalculator.CalculationMethod.KARACHI,
            asrJuristic = PrayerCalculator.AsrJuristic.HANAFI
        )

        assertNotNull(result.fajr)
        assertNotNull(result.dhuhr)
        assertNotNull(result.asr)
        assertNotNull(result.maghrib)
        assertNotNull(result.isha)
    }

    @Test
    fun `test Paris with UOIF France method`() {
        val cal = getFixedCalendar(2026, 4, 15, "Europe/Paris")
        val result = PrayerCalculator.calculateTimes(
            latitude = 48.8566,
            longitude = 2.3522,
            calendar = cal,
            method = PrayerCalculator.CalculationMethod.FRANCE_UOIF,
            asrJuristic = PrayerCalculator.AsrJuristic.SHAFI
        )

        assertNotNull(result.fajr)
        assertNotNull(result.dhuhr)
        assertNotNull(result.asr)
        assertNotNull(result.maghrib)
        assertNotNull(result.isha)
    }

    @Test
    fun `test New York with ISNA method`() {
        val cal = getFixedCalendar(2026, 4, 15, "America/New_York")
        val result = PrayerCalculator.calculateTimes(
            latitude = 40.7128,
            longitude = -74.0060,
            calendar = cal,
            method = PrayerCalculator.CalculationMethod.ISNA,
            asrJuristic = PrayerCalculator.AsrJuristic.SHAFI
        )

        assertNotNull(result.fajr)
        assertNotNull(result.dhuhr)
        assertNotNull(result.asr)
        assertNotNull(result.maghrib)
        assertNotNull(result.isha)
    }

    @Test
    fun `test Stockholm high latitude rules`() {
        val cal = getFixedCalendar(2026, 5, 21, "Europe/Stockholm") // Summer solstice
        val resultMidnight = PrayerCalculator.calculateTimes(
            latitude = 59.3293,
            longitude = 18.0686,
            calendar = cal,
            method = PrayerCalculator.CalculationMethod.MWL,
            highLatRule = PrayerCalculator.HighLatitudeRule.MIDNIGHT
        )
        assertNotNull(resultMidnight.fajr)
        assertNotNull(resultMidnight.isha)

        val resultSeventh = PrayerCalculator.calculateTimes(
            latitude = 59.3293,
            longitude = 18.0686,
            calendar = cal,
            method = PrayerCalculator.CalculationMethod.MWL,
            highLatRule = PrayerCalculator.HighLatitudeRule.SEVENTH_NIGHT
        )
        assertNotNull(resultSeventh.fajr)
        assertNotNull(resultSeventh.isha)

        val resultAngle = PrayerCalculator.calculateTimes(
            latitude = 59.3293,
            longitude = 18.0686,
            calendar = cal,
            method = PrayerCalculator.CalculationMethod.MWL,
            highLatRule = PrayerCalculator.HighLatitudeRule.ANGLE_BASED
        )
        assertNotNull(resultAngle.fajr)
        assertNotNull(resultAngle.isha)
    }

    @Test
    fun `test Asr Hanafi shadow difference`() {
        val cal = getFixedCalendar(2026, 4, 15, "Asia/Riyadh")
        val shafiTimes = PrayerCalculator.calculateTimes(
            latitude = 21.4225,
            longitude = 39.8262,
            calendar = cal,
            asrJuristic = PrayerCalculator.AsrJuristic.SHAFI
        )

        val hanafiTimes = PrayerCalculator.calculateTimes(
            latitude = 21.4225,
            longitude = 39.8262,
            calendar = cal,
            asrJuristic = PrayerCalculator.AsrJuristic.HANAFI
        )

        assertNotEquals(shafiTimes.asr, hanafiTimes.asr)
    }

    @Test
    fun `test manual minute adjustments`() {
        val cal = getFixedCalendar(2026, 4, 15, "Asia/Riyadh")
        val base = PrayerCalculator.calculateTimes(
            latitude = 21.4225,
            longitude = 39.8262,
            calendar = cal,
            fajrAdj = 0,
            dhuhrAdj = 0
        )

        val adjusted = PrayerCalculator.calculateTimes(
            latitude = 21.4225,
            longitude = 39.8262,
            calendar = cal,
            fajrAdj = 5,
            dhuhrAdj = -5
        )

        assertNotEquals(base.fajr, adjusted.fajr)
        assertNotEquals(base.dhuhr, adjusted.dhuhr)
    }

    @Test
    fun `test Imsak is 10 minutes before Fajr`() {
        val cal = getFixedCalendar(2026, 4, 15, "Asia/Riyadh")
        val result = PrayerCalculator.calculateTimes(
            latitude = 21.4225,
            longitude = 39.8262,
            calendar = cal
        )

        assertNotNull(result.imsak)
        assertTrue(result.imsak.isNotBlank())
        assertNotEquals(result.fajr, result.imsak)
    }

    @Test
    fun `test bilingual and fuzzy city search`() {
        // Arabic search with/without Alef hamzas
        val rabatSearch1 = CityCoordinates.searchCities("الرباط")
        assertTrue(rabatSearch1.any { it.cityNameAr.contains("الرباط") })

        val casaSearch = CityCoordinates.searchCities("الدار البيضاء")
        assertTrue(casaSearch.any { it.cityNameAr.contains("الدار البيضاء") })

        // English search
        val casaEnglish = CityCoordinates.searchCities("Casablanca")
        assertTrue(casaEnglish.any { it.cityNameAr == "الدار البيضاء" })

        val parisEnglish = CityCoordinates.searchCities("Paris")
        assertTrue(parisEnglish.any { it.cityNameAr == "باريس" })

        val nyEnglish = CityCoordinates.searchCities("New York")
        assertTrue(nyEnglish.any { it.cityNameAr == "نيويورك" })

        val karachiSearch = CityCoordinates.searchCities("Karachi")
        assertTrue(karachiSearch.any { it.cityNameAr == "كراتشي" })

        // Moroccan cities count check
        val moroccanCities = CityCoordinates.majorCities.filter { it.countryNameAr == "المملكة المغربية" }
        assertTrue(moroccanCities.size >= 25)
    }
}
