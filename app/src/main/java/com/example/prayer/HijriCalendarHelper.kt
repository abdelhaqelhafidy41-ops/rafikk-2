package com.example.prayer

import java.util.Calendar
import kotlin.math.floor

object HijriCalendarHelper {

    private val hijriMonths = listOf(
        "المحرّم", "صفر", "ربيع الأول", "ربيع الآخر",
        "جمادى الأولى", "جمادى الآخرة", "رجب", "شعبان",
        "رمضان", "شوّال", "ذو القعدة", "ذو الحجة"
    )

    private val arabicDaysOfWeek = listOf(
        "الأحد", "الإثنين", "الثلاثاء", "الأربعاء", "الخميس", "الجمعة", "السبت"
    )

    fun getTodayHijriString(calendar: Calendar = Calendar.getInstance()): String {
        val dayOfWeekIndex = calendar.get(Calendar.DAY_OF_WEEK) - 1 // 0=Sunday
        val dayName = arabicDaysOfWeek.getOrElse(dayOfWeekIndex) { "" }

        val y = calendar.get(Calendar.YEAR)
        val m = calendar.get(Calendar.MONTH) + 1
        val d = calendar.get(Calendar.DAY_OF_MONTH)

        val jd = gregorianToJulian(y, m, d)
        val (hYear, hMonth, hDay) = julianToHijri(jd)

        val monthName = hijriMonths.getOrElse(hMonth - 1) { "" }
        return "$dayName، $hDay $monthName $hYear هـ"
    }

    fun getHijriMonth(calendar: Calendar = Calendar.getInstance()): Int {
        val y = calendar.get(Calendar.YEAR)
        val m = calendar.get(Calendar.MONTH) + 1
        val d = calendar.get(Calendar.DAY_OF_MONTH)
        val jd = gregorianToJulian(y, m, d)
        val (_, hMonth, _) = julianToHijri(jd)
        return hMonth
    }

    fun isRamadan(calendar: Calendar = Calendar.getInstance()): Boolean {
        return getHijriMonth(calendar) == 9
    }

    private fun gregorianToJulian(year: Int, month: Int, day: Int): Double {
        var y = year
        var m = month
        if (m <= 2) {
            y -= 1
            m += 12
        }
        val a = floor(y / 100.0)
        val b = 2 - a + floor(a / 4.0)
        return floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + day + b - 1524.5
    }

    private fun julianToHijri(jd: Double): Triple<Int, Int, Int> {
        val l = floor(jd - 1948439.5) + 10632
        val n = floor((l - 1) / 10631.0)
        val lPrime = l - 10631 * n + 354
        val j = (floor((10985 - lPrime) / 5316.0)) * (floor((50 * lPrime) / 17719.0)) +
                (floor(lPrime / 5670.0)) * (floor((43 * lPrime) / 15238.0))
        val lDoublePrime = lPrime - (floor((30 - j) / 15.0)) * (floor((17719 * j) / 50.0)) -
                (floor(j / 16.0)) * (floor((15238 * j) / 43.0)) + 29
        val m = floor((24 * lDoublePrime) / 709.0).toInt()
        val d = (lDoublePrime - floor((709.0 * m) / 24.0)).toInt()
        val y = (30 * n + j - 30).toInt()
        return Triple(y, m, d)
    }
}
