package com.example.prayer

data class CityInfo(
    val cityNameAr: String,
    val countryNameAr: String,
    val latitude: Double,
    val longitude: Double,
    val timezoneId: String,
    val defaultMethod: PrayerCalculator.CalculationMethod = PrayerCalculator.CalculationMethod.UMM_AL_QURA
)

object CityCoordinates {
    val majorCities = listOf(
        // المملكة العربية السعودية
        CityInfo("مكة المكرمة", "المملكة العربية السعودية", 21.4225, 39.8262, "Asia/Riyadh", PrayerCalculator.CalculationMethod.UMM_AL_QURA),
        CityInfo("المدينة المنورة", "المملكة العربية السعودية", 24.4672, 39.6111, "Asia/Riyadh", PrayerCalculator.CalculationMethod.UMM_AL_QURA),
        CityInfo("الرياض", "المملكة العربية السعودية", 24.7136, 46.6753, "Asia/Riyadh", PrayerCalculator.CalculationMethod.UMM_AL_QURA),
        CityInfo("جدة", "المملكة العربية السعودية", 21.5433, 39.1728, "Asia/Riyadh", PrayerCalculator.CalculationMethod.UMM_AL_QURA),
        CityInfo("الدمام", "المملكة العربية السعودية", 26.4207, 50.0888, "Asia/Riyadh", PrayerCalculator.CalculationMethod.UMM_AL_QURA),
        CityInfo("الطائف", "المملكة العربية السعودية", 21.2854, 40.4244, "Asia/Riyadh", PrayerCalculator.CalculationMethod.UMM_AL_QURA),
        CityInfo("تبوك", "المملكة العربية السعودية", 28.3835, 36.5662, "Asia/Riyadh", PrayerCalculator.CalculationMethod.UMM_AL_QURA),
        CityInfo("بريدة", "المملكة العربية السعودية", 26.3260, 43.9750, "Asia/Riyadh", PrayerCalculator.CalculationMethod.UMM_AL_QURA),
        CityInfo("أبها", "المملكة العربية السعودية", 18.2164, 42.5053, "Asia/Riyadh", PrayerCalculator.CalculationMethod.UMM_AL_QURA),
        CityInfo("جازان", "المملكة العربية السعودية", 16.8892, 42.5706, "Asia/Riyadh", PrayerCalculator.CalculationMethod.UMM_AL_QURA),

        // جمهورية مصر العربية
        CityInfo("القاهرة", "جمهورية مصر العربية", 30.0444, 31.2357, "Africa/Cairo", PrayerCalculator.CalculationMethod.EGYPTIAN),
        CityInfo("الإسكندرية", "جمهورية مصر العربية", 31.2001, 29.9187, "Africa/Cairo", PrayerCalculator.CalculationMethod.EGYPTIAN),
        CityInfo("الجيزة", "جمهورية مصر العربية", 30.0131, 31.2089, "Africa/Cairo", PrayerCalculator.CalculationMethod.EGYPTIAN),
        CityInfo("المنصورة", "جمهورية مصر العربية", 31.0409, 31.3785, "Africa/Cairo", PrayerCalculator.CalculationMethod.EGYPTIAN),
        CityInfo("طنطا", "جمهورية مصر العربية", 30.7865, 31.0004, "Africa/Cairo", PrayerCalculator.CalculationMethod.EGYPTIAN),
        CityInfo("أسوان", "جمهورية مصر العربية", 24.0889, 32.8998, "Africa/Cairo", PrayerCalculator.CalculationMethod.EGYPTIAN),
        CityInfo("الأقصر", "جمهورية مصر العربية", 25.6872, 32.6396, "Africa/Cairo", PrayerCalculator.CalculationMethod.EGYPTIAN),
        CityInfo("بورسعيد", "جمهورية مصر العربية", 31.2653, 32.3019, "Africa/Cairo", PrayerCalculator.CalculationMethod.EGYPTIAN),

        // فلسطين والأردن
        CityInfo("القدس الشريف", "فلسطين", 31.7683, 35.2137, "Asia/Jerusalem", PrayerCalculator.CalculationMethod.MWL),
        CityInfo("غزة", "فلسطين", 31.5017, 34.4668, "Asia/Gaza", PrayerCalculator.CalculationMethod.MWL),
        CityInfo("الخليل", "فلسطين", 31.5326, 35.0998, "Asia/Hebron", PrayerCalculator.CalculationMethod.MWL),
        CityInfo("نابلس", "فلسطين", 32.2211, 35.2544, "Asia/Jerusalem", PrayerCalculator.CalculationMethod.MWL),
        CityInfo("عَمّان", "المملكة الأردنية الهاشمية", 31.9454, 35.9284, "Asia/Amman", PrayerCalculator.CalculationMethod.MWL),
        CityInfo("إربد", "المملكة الأردنية الهاشمية", 32.5568, 35.8469, "Asia/Amman", PrayerCalculator.CalculationMethod.MWL),
        CityInfo("الزرقاء", "المملكة الأردنية الهاشمية", 32.0728, 36.0880, "Asia/Amman", PrayerCalculator.CalculationMethod.MWL),

        // الخليج العربي
        CityInfo("دبي", "الإمارات العربية المتحدة", 25.2048, 55.2708, "Asia/Dubai", PrayerCalculator.CalculationMethod.UMM_AL_QURA),
        CityInfo("أبوظبي", "الإمارات العربية المتحدة", 24.4539, 54.3773, "Asia/Dubai", PrayerCalculator.CalculationMethod.UMM_AL_QURA),
        CityInfo("الشارقة", "الإمارات العربية المتحدة", 25.3463, 55.4209, "Asia/Dubai", PrayerCalculator.CalculationMethod.UMM_AL_QURA),
        CityInfo("الدوحة", "دولة قطر", 25.2854, 51.5310, "Asia/Qatar", PrayerCalculator.CalculationMethod.UMM_AL_QURA),
        CityInfo("الكويت", "دولة الكويت", 29.3759, 47.9774, "Asia/Kuwait", PrayerCalculator.CalculationMethod.UMM_AL_QURA),
        CityInfo("المنامة", "مملكة البحرين", 26.2285, 50.5860, "Asia/Bahrain", PrayerCalculator.CalculationMethod.UMM_AL_QURA),
        CityInfo("مسقط", "سلطنة عمان", 23.5880, 58.3829, "Asia/Muscat", PrayerCalculator.CalculationMethod.UMM_AL_QURA),

        // العراق والشام واليمن
        CityInfo("بغداد", "العراق", 33.3152, 44.3661, "Asia/Baghdad", PrayerCalculator.CalculationMethod.MWL),
        CityInfo("الموصل", "العراق", 36.3400, 43.1300, "Asia/Baghdad", PrayerCalculator.CalculationMethod.MWL),
        CityInfo("البصرة", "العراق", 30.5081, 47.7835, "Asia/Baghdad", PrayerCalculator.CalculationMethod.MWL),
        CityInfo("أربيل", "العراق", 36.1901, 44.0091, "Asia/Baghdad", PrayerCalculator.CalculationMethod.MWL),
        CityInfo("دمشق", "سوريا", 33.5138, 36.2765, "Asia/Damascus", PrayerCalculator.CalculationMethod.MWL),
        CityInfo("حلب", "سوريا", 36.2021, 37.1343, "Asia/Damascus", PrayerCalculator.CalculationMethod.MWL),
        CityInfo("بيروت", "لبنان", 33.8938, 35.5018, "Asia/Beirut", PrayerCalculator.CalculationMethod.MWL),
        CityInfo("صنعاء", "اليمن", 15.3694, 44.1910, "Asia/Riyadh", PrayerCalculator.CalculationMethod.UMM_AL_QURA),
        CityInfo("عدن", "اليمن", 12.7855, 45.0187, "Asia/Riyadh", PrayerCalculator.CalculationMethod.UMM_AL_QURA),

        // شمال إفريقيا
        CityInfo("الخرطوم", "السودان", 15.5007, 32.5599, "Africa/Khartoum", PrayerCalculator.CalculationMethod.EGYPTIAN),
        CityInfo("طرابلس", "ليبيا", 32.8872, 13.1913, "Africa/Tripoli", PrayerCalculator.CalculationMethod.MWL),
        CityInfo("بنغازي", "ليبيا", 32.1167, 20.0667, "Africa/Tripoli", PrayerCalculator.CalculationMethod.MWL),
        CityInfo("تونس", "تونس", 36.8065, 10.1815, "Africa/Tunis", PrayerCalculator.CalculationMethod.MWL),
        CityInfo("صفاقس", "تونس", 34.7406, 10.7603, "Africa/Tunis", PrayerCalculator.CalculationMethod.MWL),
        CityInfo("الجزائر", "الجزائر", 36.7538, 3.0588, "Africa/Algiers", PrayerCalculator.CalculationMethod.MWL),
        CityInfo("وهران", "الجزائر", 35.6971, -0.6308, "Africa/Algiers", PrayerCalculator.CalculationMethod.MWL),
        CityInfo("قسنطينة", "الجزائر", 36.3650, 6.6147, "Africa/Algiers", PrayerCalculator.CalculationMethod.MWL),
        CityInfo("الرباط", "المغرب", 34.0209, -6.8416, "Africa/Casablanca", PrayerCalculator.CalculationMethod.MWL),
        CityInfo("الدار البيضاء", "المغرب", 33.5731, -7.5898, "Africa/Casablanca", PrayerCalculator.CalculationMethod.MWL),
        CityInfo("مراكش", "المغرب", 31.6295, -7.9811, "Africa/Casablanca", PrayerCalculator.CalculationMethod.MWL),
        CityInfo("فاس", "المغرب", 34.0181, -5.0078, "Africa/Casablanca", PrayerCalculator.CalculationMethod.MWL),
        CityInfo("طنجة", "المغرب", 35.7595, -5.8340, "Africa/Casablanca", PrayerCalculator.CalculationMethod.MWL),
        CityInfo("نواكشوط", "موريتانيا", 18.0735, -15.9582, "Africa/Nouakchott", PrayerCalculator.CalculationMethod.MWL),

        // عواصم ومدن دولية
        CityInfo("إسطنبول", "تركيا", 41.0082, 28.9784, "Europe/Istanbul", PrayerCalculator.CalculationMethod.MWL),
        CityInfo("أنقرة", "تركيا", 39.9334, 32.8597, "Europe/Istanbul", PrayerCalculator.CalculationMethod.MWL),
        CityInfo("كوالالمبور", "ماليزيا", 3.1390, 101.6869, "Asia/Kuala_Lumpur", PrayerCalculator.CalculationMethod.MWL),
        CityInfo("جاكرتا", "إندونيسيا", -6.2088, 106.8456, "Asia/Jakarta", PrayerCalculator.CalculationMethod.MWL),
        CityInfo("كراتشي", "باكستان", 24.8607, 67.0011, "Asia/Karachi", PrayerCalculator.CalculationMethod.KARACHI),
        CityInfo("إسلام آباد", "باكستان", 33.6844, 73.0479, "Asia/Karachi", PrayerCalculator.CalculationMethod.KARACHI),
        CityInfo("لندن", "المملكة المتحدة", 51.5074, -0.1278, "Europe/London", PrayerCalculator.CalculationMethod.MWL),
        CityInfo("باريس", "فرنسا", 48.8566, 2.3522, "Europe/Paris", PrayerCalculator.CalculationMethod.MWL),
        CityInfo("برلين", "ألمانيا", 52.5200, 13.4050, "Europe/Berlin", PrayerCalculator.CalculationMethod.MWL),
        CityInfo("نيويورك", "الولايات المتحدة", 40.7128, -74.0060, "America/New_York", PrayerCalculator.CalculationMethod.ISNA),
        CityInfo("تورونتو", "كندا", 43.6532, -79.3832, "America/Toronto", PrayerCalculator.CalculationMethod.ISNA),
        CityInfo("سيدني", "أستراليا", -33.8688, 151.2093, "Australia/Sydney", PrayerCalculator.CalculationMethod.MWL)
    )

    fun searchCities(query: String): List<CityInfo> {
        val q = query.trim().replace("أ", "ا").replace("إ", "ا").replace("آ", "ا")
        if (q.isEmpty()) return majorCities
        return majorCities.filter {
            val name = it.cityNameAr.replace("أ", "ا").replace("إ", "ا").replace("آ", "ا")
            val country = it.countryNameAr.replace("أ", "ا").replace("إ", "ا").replace("آ", "ا")
            name.contains(q, ignoreCase = true) || country.contains(q, ignoreCase = true)
        }
    }
}
