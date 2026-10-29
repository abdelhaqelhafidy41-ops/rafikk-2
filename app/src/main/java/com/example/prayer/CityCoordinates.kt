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
        CityInfo("عنيزة", "المملكة العربية السعودية", 26.0911, 43.9856, "Asia/Riyadh", PrayerCalculator.CalculationMethod.UMM_AL_QURA),
        CityInfo("أبها", "المملكة العربية السعودية", 18.2164, 42.5053, "Asia/Riyadh", PrayerCalculator.CalculationMethod.UMM_AL_QURA),
        CityInfo("خميس مشيط", "المملكة العربية السعودية", 18.3000, 42.7333, "Asia/Riyadh", PrayerCalculator.CalculationMethod.UMM_AL_QURA),
        CityInfo("جازان", "المملكة العربية السعودية", 16.8892, 42.5706, "Asia/Riyadh", PrayerCalculator.CalculationMethod.UMM_AL_QURA),
        CityInfo("نجران", "المملكة العربية السعودية", 17.5000, 44.1333, "Asia/Riyadh", PrayerCalculator.CalculationMethod.UMM_AL_QURA),
        CityInfo("حائل", "المملكة العربية السعودية", 27.5219, 41.6961, "Asia/Riyadh", PrayerCalculator.CalculationMethod.UMM_AL_QURA),
        CityInfo("الجبيل", "المملكة العربية السعودية", 27.0046, 49.6601, "Asia/Riyadh", PrayerCalculator.CalculationMethod.UMM_AL_QURA),
        CityInfo("ينبع", "المملكة العربية السعودية", 24.0891, 38.0637, "Asia/Riyadh", PrayerCalculator.CalculationMethod.UMM_AL_QURA),
        CityInfo("الأحساء", "المملكة العربية السعودية", 25.3833, 49.5833, "Asia/Riyadh", PrayerCalculator.CalculationMethod.UMM_AL_QURA),
        CityInfo("عرعر", "المملكة العربية السعودية", 30.9753, 41.0381, "Asia/Riyadh", PrayerCalculator.CalculationMethod.UMM_AL_QURA),
        CityInfo("سكاكا", "المملكة العربية السعودية", 29.9697, 40.2064, "Asia/Riyadh", PrayerCalculator.CalculationMethod.UMM_AL_QURA),
        CityInfo("الباحة", "المملكة العربية السعودية", 20.0129, 41.4677, "Asia/Riyadh", PrayerCalculator.CalculationMethod.UMM_AL_QURA),

        // جمهورية مصر العربية
        CityInfo("القاهرة", "جمهورية مصر العربية", 30.0444, 31.2357, "Africa/Cairo", PrayerCalculator.CalculationMethod.EGYPTIAN),
        CityInfo("الإسكندرية", "جمهورية مصر العربية", 31.2001, 29.9187, "Africa/Cairo", PrayerCalculator.CalculationMethod.EGYPTIAN),
        CityInfo("الجيزة", "جمهورية مصر العربية", 30.0131, 31.2089, "Africa/Cairo", PrayerCalculator.CalculationMethod.EGYPTIAN),
        CityInfo("المنصورة", "جمهورية مصر العربية", 31.0409, 31.3785, "Africa/Cairo", PrayerCalculator.CalculationMethod.EGYPTIAN),
        CityInfo("طنطا", "جمهورية مصر العربية", 30.7865, 31.0004, "Africa/Cairo", PrayerCalculator.CalculationMethod.EGYPTIAN),
        CityInfo("الزقازيق", "جمهورية مصر العربية", 30.5877, 31.5020, "Africa/Cairo", PrayerCalculator.CalculationMethod.EGYPTIAN),
        CityInfo("أسيوط", "جمهورية مصر العربية", 27.1801, 31.1837, "Africa/Cairo", PrayerCalculator.CalculationMethod.EGYPTIAN),
        CityInfo("سوهاج", "جمهورية مصر العربية", 26.5569, 31.6948, "Africa/Cairo", PrayerCalculator.CalculationMethod.EGYPTIAN),
        CityInfo("قنا", "جمهورية مصر العربية", 26.1551, 32.7160, "Africa/Cairo", PrayerCalculator.CalculationMethod.EGYPTIAN),
        CityInfo("أسوان", "جمهورية مصر العربية", 24.0889, 32.8998, "Africa/Cairo", PrayerCalculator.CalculationMethod.EGYPTIAN),
        CityInfo("الأقصر", "جمهورية مصر العربية", 25.6872, 32.6396, "Africa/Cairo", PrayerCalculator.CalculationMethod.EGYPTIAN),
        CityInfo("بورسعيد", "جمهورية مصر العربية", 31.2653, 32.3019, "Africa/Cairo", PrayerCalculator.CalculationMethod.EGYPTIAN),
        CityInfo("السويس", "جمهورية مصر العربية", 29.9668, 32.5498, "Africa/Cairo", PrayerCalculator.CalculationMethod.EGYPTIAN),
        CityInfo("الإسماعيلية", "جمهورية مصر العربية", 30.5965, 32.2715, "Africa/Cairo", PrayerCalculator.CalculationMethod.EGYPTIAN),
        CityInfo("دمياط", "جمهورية مصر العربية", 31.4175, 31.8144, "Africa/Cairo", PrayerCalculator.CalculationMethod.EGYPTIAN),
        CityInfo("كفر الشيخ", "جمهورية مصر العربية", 31.1107, 30.9388, "Africa/Cairo", PrayerCalculator.CalculationMethod.EGYPTIAN),
        CityInfo("الفيوم", "جمهورية مصر العربية", 29.3084, 30.8428, "Africa/Cairo", PrayerCalculator.CalculationMethod.EGYPTIAN),
        CityInfo("بني سويف", "جمهورية مصر العربية", 29.0661, 31.0994, "Africa/Cairo", PrayerCalculator.CalculationMethod.EGYPTIAN),
        CityInfo("المنيا", "جمهورية مصر العربية", 28.0871, 30.7618, "Africa/Cairo", PrayerCalculator.CalculationMethod.EGYPTIAN),
        CityInfo("مطروح", "جمهورية مصر العربية", 31.3543, 27.2373, "Africa/Cairo", PrayerCalculator.CalculationMethod.EGYPTIAN),

        // المغرب
        CityInfo("الرباط", "المملكة المغربية", 34.0209, -6.8416, "Africa/Casablanca", PrayerCalculator.CalculationMethod.MWL),
        CityInfo("الدار البيضاء", "المملكة المغربية", 33.5731, -7.5898, "Africa/Casablanca", PrayerCalculator.CalculationMethod.MWL),
        CityInfo("مراكش", "المملكة المغربية", 31.6295, -7.9811, "Africa/Casablanca", PrayerCalculator.CalculationMethod.MWL),
        CityInfo("فاس", "المملكة المغربية", 34.0181, -5.0078, "Africa/Casablanca", PrayerCalculator.CalculationMethod.MWL),
        CityInfo("طنجة", "المملكة المغربية", 35.7595, -5.8340, "Africa/Casablanca", PrayerCalculator.CalculationMethod.MWL),
        CityInfo("أكادير", "المملكة المغربية", 30.4278, -9.5981, "Africa/Casablanca", PrayerCalculator.CalculationMethod.MWL),
        CityInfo("مكناس", "المملكة المغربية", 33.8935, -5.5473, "Africa/Casablanca", PrayerCalculator.CalculationMethod.MWL),
        CityInfo("وجدة", "المملكة المغربية", 34.6814, -1.9086, "Africa/Casablanca", PrayerCalculator.CalculationMethod.MWL),
        CityInfo("تطوان", "المملكة المغربية", 35.5889, -5.3626, "Africa/Casablanca", PrayerCalculator.CalculationMethod.MWL),
        CityInfo("القنيطرة", "المملكة المغربية", 34.2610, -6.5802, "Africa/Casablanca", PrayerCalculator.CalculationMethod.MWL),
        CityInfo("العيون", "المملكة المغربية", 27.1536, -13.2033, "Africa/Casablanca", PrayerCalculator.CalculationMethod.MWL),

        // الجزائر
        CityInfo("الجزائر", "الجمهورية الجزائرية", 36.7538, 3.0588, "Africa/Algiers", PrayerCalculator.CalculationMethod.MWL),
        CityInfo("وهران", "الجمهورية الجزائرية", 35.6971, -0.6308, "Africa/Algiers", PrayerCalculator.CalculationMethod.MWL),
        CityInfo("قسنطينة", "الجمهورية الجزائرية", 36.3650, 6.6147, "Africa/Algiers", PrayerCalculator.CalculationMethod.MWL),
        CityInfo("عنابة", "الجمهورية الجزائرية", 36.9000, 7.7667, "Africa/Algiers", PrayerCalculator.CalculationMethod.MWL),
        CityInfo("سطيف", "الجمهورية الجزائرية", 36.1911, 5.4137, "Africa/Algiers", PrayerCalculator.CalculationMethod.MWL),
        CityInfo("باتنة", "الجمهورية الجزائرية", 35.5559, 6.1741, "Africa/Algiers", PrayerCalculator.CalculationMethod.MWL),
        CityInfo("تلمسان", "الجمهورية الجزائرية", 34.8783, -1.3150, "Africa/Algiers", PrayerCalculator.CalculationMethod.MWL),
        CityInfo("بسكرة", "الجمهورية الجزائرية", 34.8500, 5.7333, "Africa/Algiers", PrayerCalculator.CalculationMethod.MWL),
        CityInfo("ورقلة", "الجمهورية الجزائرية", 31.9500, 5.3333, "Africa/Algiers", PrayerCalculator.CalculationMethod.MWL),

        // تونس وليبيا وموريتانيا والسودان
        CityInfo("تونس", "الجمهورية التونسية", 36.8065, 10.1815, "Africa/Tunis", PrayerCalculator.CalculationMethod.MWL),
        CityInfo("صفاقس", "الجمهورية التونسية", 34.7406, 10.7603, "Africa/Tunis", PrayerCalculator.CalculationMethod.MWL),
        CityInfo("سوسة", "الجمهورية التونسية", 35.8256, 10.6369, "Africa/Tunis", PrayerCalculator.CalculationMethod.MWL),
        CityInfo("القيروان", "الجمهورية التونسية", 35.6781, 10.0963, "Africa/Tunis", PrayerCalculator.CalculationMethod.MWL),
        CityInfo("طرابلس", "ليبيا", 32.8872, 13.1913, "Africa/Tripoli", PrayerCalculator.CalculationMethod.MWL),
        CityInfo("بنغازي", "ليبيا", 32.1167, 20.0667, "Africa/Tripoli", PrayerCalculator.CalculationMethod.MWL),
        CityInfo("مصراتة", "ليبيا", 32.3754, 15.0925, "Africa/Tripoli", PrayerCalculator.CalculationMethod.MWL),
        CityInfo("نواكشوط", "موريتانيا", 18.0735, -15.9582, "Africa/Nouakchott", PrayerCalculator.CalculationMethod.MWL),
        CityInfo("الخرطوم", "السودان", 15.5007, 32.5599, "Africa/Khartoum", PrayerCalculator.CalculationMethod.EGYPTIAN),
        CityInfo("أم درمان", "السودان", 15.6500, 32.4833, "Africa/Khartoum", PrayerCalculator.CalculationMethod.EGYPTIAN),
        CityInfo("بورتسودان", "السودان", 19.6167, 37.2167, "Africa/Khartoum", PrayerCalculator.CalculationMethod.EGYPTIAN),

        // فلسطين والأردن
        CityInfo("القدس الشريف", "دولة فلسطين", 31.7683, 35.2137, "Asia/Jerusalem", PrayerCalculator.CalculationMethod.MWL),
        CityInfo("غزة", "دولة فلسطين", 31.5017, 34.4668, "Asia/Gaza", PrayerCalculator.CalculationMethod.MWL),
        CityInfo("الخليل", "دولة فلسطين", 31.5326, 35.0998, "Asia/Hebron", PrayerCalculator.CalculationMethod.MWL),
        CityInfo("نابلس", "دولة فلسطين", 32.2211, 35.2544, "Asia/Jerusalem", PrayerCalculator.CalculationMethod.MWL),
        CityInfo("رام الله", "دولة فلسطين", 31.9038, 35.2034, "Asia/Jerusalem", PrayerCalculator.CalculationMethod.MWL),
        CityInfo("جنين", "دولة فلسطين", 32.4608, 35.2974, "Asia/Jerusalem", PrayerCalculator.CalculationMethod.MWL),
        CityInfo("عَمّان", "المملكة الأردنية الهاشمية", 31.9454, 35.9284, "Asia/Amman", PrayerCalculator.CalculationMethod.MWL),
        CityInfo("إربد", "المملكة الأردنية الهاشمية", 32.5568, 35.8469, "Asia/Amman", PrayerCalculator.CalculationMethod.MWL),
        CityInfo("الزرقاء", "المملكة الأردنية الهاشمية", 32.0728, 36.0880, "Asia/Amman", PrayerCalculator.CalculationMethod.MWL),
        CityInfo("العقبة", "المملكة الأردنية الهاشمية", 29.5321, 35.0063, "Asia/Amman", PrayerCalculator.CalculationMethod.MWL),

        // الخليج العربي
        CityInfo("دبي", "الإمارات العربية المتحدة", 25.2048, 55.2708, "Asia/Dubai", PrayerCalculator.CalculationMethod.UMM_AL_QURA),
        CityInfo("أبوظبي", "الإمارات العربية المتحدة", 24.4539, 54.3773, "Asia/Dubai", PrayerCalculator.CalculationMethod.UMM_AL_QURA),
        CityInfo("الشارقة", "الإمارات العربية المتحدة", 25.3463, 55.4209, "Asia/Dubai", PrayerCalculator.CalculationMethod.UMM_AL_QURA),
        CityInfo("العين", "الإمارات العربية المتحدة", 24.2075, 55.7447, "Asia/Dubai", PrayerCalculator.CalculationMethod.UMM_AL_QURA),
        CityInfo("الدوحة", "دولة قطر", 25.2854, 51.5310, "Asia/Qatar", PrayerCalculator.CalculationMethod.UMM_AL_QURA),
        CityInfo("الكويت", "دولة الكويت", 29.3759, 47.9774, "Asia/Kuwait", PrayerCalculator.CalculationMethod.UMM_AL_QURA),
        CityInfo("المنامة", "مملكة البحرين", 26.2285, 50.5860, "Asia/Bahrain", PrayerCalculator.CalculationMethod.UMM_AL_QURA),
        CityInfo("مسقط", "سلطنة عمان", 23.5880, 58.3829, "Asia/Muscat", PrayerCalculator.CalculationMethod.UMM_AL_QURA),
        CityInfo("صلالة", "سلطنة عمان", 17.0151, 54.0924, "Asia/Muscat", PrayerCalculator.CalculationMethod.UMM_AL_QURA),

        // العراق والشام واليمن
        CityInfo("بغداد", "العراق", 33.3152, 44.3661, "Asia/Baghdad", PrayerCalculator.CalculationMethod.MWL),
        CityInfo("الموصل", "العراق", 36.3400, 43.1300, "Asia/Baghdad", PrayerCalculator.CalculationMethod.MWL),
        CityInfo("البصرة", "العراق", 30.5081, 47.7835, "Asia/Baghdad", PrayerCalculator.CalculationMethod.MWL),
        CityInfo("أربيل", "العراق", 36.1901, 44.0091, "Asia/Baghdad", PrayerCalculator.CalculationMethod.MWL),
        CityInfo("النجف الأشرف", "العراق", 32.0000, 44.3333, "Asia/Baghdad", PrayerCalculator.CalculationMethod.MWL),
        CityInfo("كربلاء", "العراق", 32.6160, 44.0249, "Asia/Baghdad", PrayerCalculator.CalculationMethod.MWL),
        CityInfo("دمشق", "سوريا", 33.5138, 36.2765, "Asia/Damascus", PrayerCalculator.CalculationMethod.MWL),
        CityInfo("حلب", "سوريا", 36.2021, 37.1343, "Asia/Damascus", PrayerCalculator.CalculationMethod.MWL),
        CityInfo("حمص", "سوريا", 34.7324, 36.7137, "Asia/Damascus", PrayerCalculator.CalculationMethod.MWL),
        CityInfo("اللاذقية", "سوريا", 35.5317, 35.7901, "Asia/Damascus", PrayerCalculator.CalculationMethod.MWL),
        CityInfo("بيروت", "لبنان", 33.8938, 35.5018, "Asia/Beirut", PrayerCalculator.CalculationMethod.MWL),
        CityInfo("طرابلس الشام", "لبنان", 34.4367, 35.8497, "Asia/Beirut", PrayerCalculator.CalculationMethod.MWL),
        CityInfo("صنعاء", "اليمن", 15.3694, 44.1910, "Asia/Riyadh", PrayerCalculator.CalculationMethod.UMM_AL_QURA),
        CityInfo("عدن", "اليمن", 12.7855, 45.0187, "Asia/Riyadh", PrayerCalculator.CalculationMethod.UMM_AL_QURA),
        CityInfo("تعز", "اليمن", 13.5795, 44.0209, "Asia/Riyadh", PrayerCalculator.CalculationMethod.UMM_AL_QURA),
        CityInfo("الحديدة", "اليمن", 14.7978, 42.9545, "Asia/Riyadh", PrayerCalculator.CalculationMethod.UMM_AL_QURA),

        // تركيا وآسيا
        CityInfo("إسطنبول", "تركيا", 41.0082, 28.9784, "Europe/Istanbul", PrayerCalculator.CalculationMethod.MWL),
        CityInfo("أنقرة", "تركيا", 39.9334, 32.8597, "Europe/Istanbul", PrayerCalculator.CalculationMethod.MWL),
        CityInfo("بورصة", "تركيا", 40.1885, 29.0610, "Europe/Istanbul", PrayerCalculator.CalculationMethod.MWL),
        CityInfo("غازي عنتاب", "تركيا", 37.0662, 37.3833, "Europe/Istanbul", PrayerCalculator.CalculationMethod.MWL),
        CityInfo("كوالالمبور", "ماليزيا", 3.1390, 101.6869, "Asia/Kuala_Lumpur", PrayerCalculator.CalculationMethod.MWL),
        CityInfo("جاكرتا", "إندونيسيا", -6.2088, 106.8456, "Asia/Jakarta", PrayerCalculator.CalculationMethod.MWL),
        CityInfo("سورابايا", "إندونيسيا", -7.2575, 112.7521, "Asia/Jakarta", PrayerCalculator.CalculationMethod.MWL),
        CityInfo("كراتشي", "باكستان", 24.8607, 67.0011, "Asia/Karachi", PrayerCalculator.CalculationMethod.KARACHI),
        CityInfo("لاهور", "باكستان", 31.5204, 74.3587, "Asia/Karachi", PrayerCalculator.CalculationMethod.KARACHI),
        CityInfo("إسلام آباد", "باكستان", 33.6844, 73.0479, "Asia/Karachi", PrayerCalculator.CalculationMethod.KARACHI),
        CityInfo("دكا", "بنغلاديش", 23.8103, 90.4125, "Asia/Dhaka", PrayerCalculator.CalculationMethod.KARACHI),
        CityInfo("نيودلهي", "الهند", 28.6139, 77.2090, "Asia/Kolkata", PrayerCalculator.CalculationMethod.MWL),
        CityInfo("طشقند", "أوزبكستان", 41.2995, 69.2401, "Asia/Tashkent", PrayerCalculator.CalculationMethod.MWL),

        // أوروبا والأمريكتين وبقية العالم
        CityInfo("لندن", "المملكة المتحدة", 51.5074, -0.1278, "Europe/London", PrayerCalculator.CalculationMethod.MWL),
        CityInfo("مانشستر", "المملكة المتحدة", 53.4808, -2.2426, "Europe/London", PrayerCalculator.CalculationMethod.MWL),
        CityInfo("باريس", "فرنسا", 48.8566, 2.3522, "Europe/Paris", PrayerCalculator.CalculationMethod.MWL),
        CityInfo("مارسيليا", "فرنسا", 43.2965, 5.3698, "Europe/Paris", PrayerCalculator.CalculationMethod.MWL),
        CityInfo("برلين", "ألمانيا", 52.5200, 13.4050, "Europe/Berlin", PrayerCalculator.CalculationMethod.MWL),
        CityInfo("فرانكفورت", "ألمانيا", 50.1109, 8.6821, "Europe/Berlin", PrayerCalculator.CalculationMethod.MWL),
        CityInfo("مدريد", "إسبانيا", 40.4168, -3.7038, "Europe/Madrid", PrayerCalculator.CalculationMethod.MWL),
        CityInfo("برشلونة", "إسبانيا", 41.3851, 2.1734, "Europe/Madrid", PrayerCalculator.CalculationMethod.MWL),
        CityInfo("روما", "إيطاليا", 41.9028, 12.4964, "Europe/Rome", PrayerCalculator.CalculationMethod.MWL),
        CityInfo("ميلانو", "إيطاليا", 45.4642, 9.1900, "Europe/Rome", PrayerCalculator.CalculationMethod.MWL),
        CityInfo("أمستردام", "هولندا", 52.3676, 4.9041, "Europe/Amsterdam", PrayerCalculator.CalculationMethod.MWL),
        CityInfo("بروكسل", "بلجيكا", 50.8503, 4.3517, "Europe/Brussels", PrayerCalculator.CalculationMethod.MWL),
        CityInfo("ستوكهولم", "السويد", 59.3293, 18.0686, "Europe/Stockholm", PrayerCalculator.CalculationMethod.MWL),
        CityInfo("أوسلو", "النرويج", 59.9139, 10.7522, "Europe/Oslo", PrayerCalculator.CalculationMethod.MWL),
        CityInfo("موسكو", "روسيا", 55.7558, 37.6173, "Europe/Moscow", PrayerCalculator.CalculationMethod.MWL),
        CityInfo("نيويورك", "الولايات المتحدة الأمريكية", 40.7128, -74.0060, "America/New_York", PrayerCalculator.CalculationMethod.ISNA),
        CityInfo("واشنطن", "الولايات المتحدة الأمريكية", 38.9072, -77.0369, "America/New_York", PrayerCalculator.CalculationMethod.ISNA),
        CityInfo("شيكاغو", "الولايات المتحدة الأمريكية", 41.8781, -87.6298, "America/Chicago", PrayerCalculator.CalculationMethod.ISNA),
        CityInfo("لوس أنجلوس", "الولايات المتحدة الأمريكية", 34.0522, -118.2437, "America/Los_Angeles", PrayerCalculator.CalculationMethod.ISNA),
        CityInfo("هيوستن", "الولايات المتحدة الأمريكية", 29.7604, -95.3698, "America/Chicago", PrayerCalculator.CalculationMethod.ISNA),
        CityInfo("تورونتو", "كندا", 43.6532, -79.3832, "America/Toronto", PrayerCalculator.CalculationMethod.ISNA),
        CityInfo("مونتريال", "كندا", 45.5017, -73.5673, "America/Toronto", PrayerCalculator.CalculationMethod.ISNA),
        CityInfo("سيدني", "أستراليا", -33.8688, 151.2093, "Australia/Sydney", PrayerCalculator.CalculationMethod.MWL),
        CityInfo("ملبورن", "أستراليا", -37.8136, 144.9631, "Australia/Melbourne", PrayerCalculator.CalculationMethod.MWL)
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
