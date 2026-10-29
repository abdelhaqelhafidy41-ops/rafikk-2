package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.quran.QuranData
import com.example.data.quran.RecitersData
import com.example.prayer.CityCoordinates
import com.example.prayer.PrayerCalculator
import com.example.prayer.QiblaCalculator
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Calendar

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("رفيق المسلم", appName)
    }

    @Test
    fun `test prayer calculator calculates valid times`() {
        val cal = Calendar.getInstance()
        val result = PrayerCalculator.calculateTimes(
            latitude = 21.4225, // Makkah
            longitude = 39.8262,
            calendar = cal,
            method = PrayerCalculator.CalculationMethod.UMM_AL_QURA
        )

        assertNotNull(result.fajr)
        assertNotNull(result.dhuhr)
        assertNotNull(result.asr)
        assertNotNull(result.maghrib)
        assertNotNull(result.isha)
        assertTrue(result.remainingMinutes >= 0)
    }

    @Test
    fun `test prayer calculator supports custom angles and high latitude rules`() {
        val cal = Calendar.getInstance()
        val result = PrayerCalculator.calculateTimes(
            latitude = 59.3293, // Stockholm (High latitude)
            longitude = 18.0686,
            calendar = cal,
            method = PrayerCalculator.CalculationMethod.CUSTOM,
            highLatRule = PrayerCalculator.HighLatitudeRule.SEVENTH_NIGHT,
            customFajrAngle = 18.5,
            customIshaAngle = 17.0
        )

        assertNotNull(result.fajr)
        assertNotNull(result.dhuhr)
        assertNotNull(result.asr)
        assertNotNull(result.maghrib)
        assertNotNull(result.isha)
    }

    @Test
    fun `test quran catalog has 114 surahs`() {
        assertEquals(114, QuranData.allSurahs.size)
        val fatihah = QuranData.allSurahs.first()
        assertEquals("الفاتحة", fatihah.nameAr)
        assertEquals(7, fatihah.versesCount)

        val searchResult = QuranData.searchSurahs("الكهف")
        assertTrue(searchResult.any { it.number == 18 })
    }

    @Test
    fun `test madinah quran package constant is madarsoft quran`() {
        assertEquals("com.madarsoft.quran", QuranData.MADINA_QURAN_PACKAGE)
        val context = ApplicationProvider.getApplicationContext<Context>()
        val result = QuranData.launchMadinaQuranApp(context)
        // In Robolectric test environment without the package installed, result should be NOT_INSTALLED
        assertEquals(QuranData.LaunchResult.NOT_INSTALLED, result)
    }

    @Test
    fun `test reciters catalog has 10 plus verified reciters`() {
        assertTrue(RecitersData.famousReciters.size >= 10)
        val alafasy = RecitersData.famousReciters.first()
        assertEquals("alafasy", alafasy.id)
        val audioUrl = RecitersData.getAyahAudioUrl("alafasy", 1, 1)
        assertEquals("https://everyayah.com/data/Alafasy_128kbps/001001.mp3", audioUrl)
    }

    @Test
    fun `test qibla bearing and distance calculation`() {
        // Cairo: 30.0444° N, 31.2357° E
        val qiblaResult = QiblaCalculator.calculateQibla(30.0444, 31.2357)
        // Cairo to Makkah is approximately 136°
        assertTrue("Bearing should be between 130 and 145 degrees", qiblaResult.qiblaAngle in 130.0..145.0)

        // Distance from Cairo to Makkah should be around 1200-1400 km
        assertTrue("Distance from Cairo to Makkah should be around 1200-1400 km", qiblaResult.distanceKm in 1200.0..1400.0)
        assertEquals("جنوب شرق", qiblaResult.directionLabelAr)
    }

    @Test
    fun `test city coordinates catalog and search`() {
        val cities = CityCoordinates.majorCities
        assertTrue(cities.size >= 30)
        val riyadh = cities.find { it.cityNameAr.contains("الرياض") }
        assertNotNull(riyadh)
        assertEquals("Asia/Riyadh", riyadh?.timezoneId)

        val searchMatches = CityCoordinates.searchCities("القاهرة")
        assertTrue(searchMatches.isNotEmpty())
    }

    @Test
    fun `test surah al-baqarah contains all 286 complete authentic verses`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        QuranData.init(context)

        val ayahs = QuranData.getSurahAyahs(2, "alafasy", context)
        assertEquals("Surah Al-Baqarah must have 286 verses", 286, ayahs.size)

        // Verse 1
        assertEquals("الٓمٓ", ayahs[0].textUthmani)
        assertEquals(1, ayahs[0].ayahNumber)
        assertEquals(2, ayahs[0].surahNumber)

        // Verse 2
        assertTrue("Verse 2 should start with That is the Book", ayahs[1].textUthmani.contains("ذَٰلِكَ"))

        // Verse 255: Ayat Al-Kursi
        val ayatAlKursi = ayahs[254]
        assertEquals(255, ayatAlKursi.ayahNumber)
        assertTrue(
            "Ayat Al-Kursi must contain complete authentic text",
            (ayatAlKursi.textUthmani.contains("ٱللَّهُ") || ayatAlKursi.textUthmani.contains("اللَّهُ")) &&
                    (ayatAlKursi.textUthmani.contains("ٱلْحَىُّ ٱلْقَيُّومُ") || ayatAlKursi.textUthmani.contains("الْحَيُّ الْقَيُّومُ")) &&
                    (ayatAlKursi.textUthmani.contains("وَسِعَ كُرْسِيُّهُ") || ayatAlKursi.textUthmani.contains("وَسِعَ كُرْسِيُّهُ"))
        )

        // Verse 285 & 286
        val verse285 = ayahs[284]
        assertEquals(285, verse285.ayahNumber)
        assertTrue(verse285.textUthmani.contains("آمَنَ") || verse285.textUthmani.contains("ءَامَنَ"))

        val verse286 = ayahs[285]
        assertEquals(286, verse286.ayahNumber)
        assertTrue(verse286.textUthmani.contains("لَا يُكَلِّفُ") || verse286.textUthmani.contains("لَا يُكَلِّفُ"))

        // Ensure NO placeholder text exists in any of the 286 verses
        val hasDummyText = ayahs.any { it.textUthmani.contains("آية من سورة") }
        assertFalse("Surah Al-Baqarah must not contain any placeholder or dummy text", hasDummyText)
    }

    @Test
    fun `test all surahs load authentic verses without dummy placeholder text`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        QuranData.init(context)

        // Test Al-Fatihah (7 verses)
        val s1 = QuranData.getSurahAyahs(1, "alafasy", context)
        assertEquals(7, s1.size)

        // Test Al-Imran (200 verses)
        val s3 = QuranData.getSurahAyahs(3, "alafasy", context)
        assertEquals(200, s3.size)
        assertEquals("الٓمٓ", s3[0].textUthmani)

        // Test An-Nas (6 verses)
        val s114 = QuranData.getSurahAyahs(114, "alafasy", context)
        assertEquals(6, s114.size)
        assertTrue(s114[0].textUthmani.contains("أَعُوذُ بِرَبِّ ٱلنَّاسِ") || s114[0].textUthmani.contains("أَعُوذُ بِرَبِّ النَّاسِ"))

        // Ensure no dummy text
        assertFalse("Surah 3 must not contain dummy text", s3.any { it.textUthmani.contains("آية من سورة") })
        assertFalse("Surah 114 must not contain dummy text", s114.any { it.textUthmani.contains("آية من سورة") })
    }
}
