package com.example.data.quran

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.ConcurrentHashMap

data class SurahMeta(
    val number: Int,
    val nameAr: String,
    val nameEn: String,
    val versesCount: Int,
    val revelationType: String, // مكية / مدنية
    val pageNumber: Int,
    val juzNumber: Int
)

data class AyahItem(
    val surahNumber: Int,
    val ayahNumber: Int,
    val textUthmani: String,
    val audioUrl: String? = null
)

object QuranData {

    const val MADINA_QURAN_PACKAGE = "com.madarsoft.quran"
    const val MADINA_QURAN_ALT_PACKAGE = "sa.gov.qurancomplex.madinaquran"
    const val MADINA_QURAN_PLAYSTORE_URL = "https://play.google.com/store/apps/details?id=com.madarsoft.quran"

    enum class LaunchResult {
        OPENED,
        NOT_INSTALLED,
        FAILED
    }

    fun isMadinaQuranInstalled(context: Context): Boolean {
        val pm = context.packageManager
        return try {
            pm.getPackageInfo(MADINA_QURAN_PACKAGE, 0)
            true
        } catch (_: Exception) {
            try {
                pm.getPackageInfo(MADINA_QURAN_ALT_PACKAGE, 0)
                true
            } catch (_: Exception) {
                false
            }
        }
    }

    fun launchMadinaQuranApp(context: Context): LaunchResult {
        val pm = context.packageManager
        val launchIntent = pm.getLaunchIntentForPackage(MADINA_QURAN_PACKAGE)
            ?: pm.getLaunchIntentForPackage(MADINA_QURAN_ALT_PACKAGE)

        return if (launchIntent != null) {
            try {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(launchIntent)
                LaunchResult.OPENED
            } catch (e: Exception) {
                LaunchResult.FAILED
            }
        } else {
            LaunchResult.NOT_INSTALLED
        }
    }

    fun openMadinaQuranInPlayStore(context: Context) {
        try {
            val marketIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$MADINA_QURAN_PACKAGE")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(marketIntent)
        } catch (e: Exception) {
            try {
                val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(MADINA_QURAN_PLAYSTORE_URL)).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(webIntent)
            } catch (ex: Exception) {
                Toast.makeText(context, "تعذر فتح صفحة التطبيق في متجر Google Play", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val allSurahs = listOf(
        SurahMeta(1, "الفاتحة", "Al-Fatihah", 7, "مكية", 1, 1),
        SurahMeta(2, "البقرة", "Al-Baqarah", 286, "مدنية", 2, 1),
        SurahMeta(3, "آل عمران", "Aal-E-Imran", 200, "مدنية", 50, 3),
        SurahMeta(4, "النساء", "An-Nisa", 176, "مدنية", 77, 4),
        SurahMeta(5, "المائدة", "Al-Ma'idah", 120, "مدنية", 106, 6),
        SurahMeta(6, "الأنعام", "Al-An'am", 165, "مكية", 128, 7),
        SurahMeta(7, "الأعراف", "Al-A'raf", 206, "مكية", 151, 8),
        SurahMeta(8, "الأنفال", "Al-Anfal", 75, "مدنية", 177, 9),
        SurahMeta(9, "التوبة", "At-Tawbah", 129, "مدنية", 187, 10),
        SurahMeta(10, "يونس", "Yunus", 109, "مكية", 208, 11),
        SurahMeta(11, "هود", "Hud", 123, "مكية", 221, 11),
        SurahMeta(12, "يوسف", "Yusuf", 111, "مكية", 235, 12),
        SurahMeta(13, "الرعد", "Ar-Ra'd", 43, "مدنية", 249, 13),
        SurahMeta(14, "إبراهيم", "Ibrahim", 52, "مكية", 255, 13),
        SurahMeta(15, "الحجر", "Al-Hijr", 99, "مكية", 262, 14),
        SurahMeta(16, "النحل", "An-Nahl", 128, "مكية", 267, 14),
        SurahMeta(17, "الإسراء", "Al-Isra", 111, "مكية", 282, 15),
        SurahMeta(18, "الكهف", "Al-Kahf", 110, "مكية", 293, 15),
        SurahMeta(19, "مريم", "Maryam", 98, "مكية", 305, 16),
        SurahMeta(20, "طه", "Taha", 135, "مكية", 312, 16),
        SurahMeta(21, "الأنبياء", "Al-Anbiya", 112, "مكية", 322, 17),
        SurahMeta(22, "الحج", "Al-Hajj", 78, "مدنية", 332, 17),
        SurahMeta(23, "المؤمنون", "Al-Mu'minun", 118, "مكية", 342, 18),
        SurahMeta(24, "النور", "An-Nur", 64, "مدنية", 350, 18),
        SurahMeta(25, "الفرقان", "Al-Furqan", 77, "مكية", 359, 18),
        SurahMeta(26, "الشعراء", "Ash-Shu'ara", 227, "مكية", 367, 19),
        SurahMeta(27, "النمل", "An-Naml", 93, "مكية", 377, 19),
        SurahMeta(28, "القصص", "Al-Qasas", 88, "مكية", 385, 20),
        SurahMeta(29, "العنكبوت", "Al-Ankabut", 69, "مكية", 396, 20),
        SurahMeta(30, "الروم", "Ar-Rum", 60, "مكية", 404, 21),
        SurahMeta(31, "لقمان", "Luqman", 34, "مكية", 411, 21),
        SurahMeta(32, "السجدة", "As-Sajdah", 30, "مكية", 415, 21),
        SurahMeta(33, "الأحزاب", "Al-Ahzab", 73, "مدنية", 418, 21),
        SurahMeta(34, "سبأ", "Saba", 54, "مكية", 428, 22),
        SurahMeta(35, "فاطر", "Fatir", 45, "مكية", 434, 22),
        SurahMeta(36, "يس", "Yasin", 83, "مكية", 440, 22),
        SurahMeta(37, "الصافات", "As-Saffat", 182, "مكية", 446, 23),
        SurahMeta(38, "ص", "Sad", 88, "مكية", 453, 23),
        SurahMeta(39, "الزمر", "Az-Zumar", 75, "مكية", 458, 23),
        SurahMeta(40, "غافر", "Ghafir", 85, "مكية", 467, 24),
        SurahMeta(41, "فصلت", "Fussilat", 54, "مكية", 477, 24),
        SurahMeta(42, "الشورى", "Ash-Shura", 53, "مكية", 483, 25),
        SurahMeta(43, "الزخرف", "Az-Zukhruf", 89, "مكية", 489, 25),
        SurahMeta(44, "الدخان", "Ad-Dukhan", 59, "مكية", 496, 25),
        SurahMeta(45, "الجاثية", "Al-Jathiyah", 37, "مكية", 499, 25),
        SurahMeta(46, "الأحقاف", "Al-Ahqaf", 35, "مكية", 502, 26),
        SurahMeta(47, "محمد", "Muhammad", 38, "مدنية", 507, 26),
        SurahMeta(48, "الفتح", "Al-Fath", 29, "مدنية", 511, 26),
        SurahMeta(49, "الحجرات", "Al-Hujurat", 18, "مدنية", 515, 26),
        SurahMeta(50, "ق", "Qaf", 45, "مكية", 518, 26),
        SurahMeta(51, "الذاريات", "Adh-Dhariyat", 60, "مكية", 520, 26),
        SurahMeta(52, "الطور", "At-Tur", 49, "مكية", 523, 27),
        SurahMeta(53, "النجم", "An-Najm", 62, "مكية", 526, 27),
        SurahMeta(54, "القمر", "Al-Qamar", 55, "مكية", 528, 27),
        SurahMeta(55, "الرحمن", "Ar-Rahman", 78, "مدنية", 531, 27),
        SurahMeta(56, "الواقعة", "Al-Waqi'ah", 96, "مكية", 534, 27),
        SurahMeta(57, "الحديد", "Al-Hadid", 29, "مدنية", 537, 27),
        SurahMeta(58, "المجادلة", "Al-Mujadila", 22, "مدنية", 542, 28),
        SurahMeta(59, "الحشر", "Al-Hashr", 24, "مدنية", 545, 28),
        SurahMeta(60, "الممتحنة", "Al-Mumtahanah", 13, "مدنية", 549, 28),
        SurahMeta(61, "الصف", "As-Saff", 14, "مدنية", 551, 28),
        SurahMeta(62, "الجمعة", "Al-Jumu'ah", 11, "مدنية", 553, 28),
        SurahMeta(63, "المنافقون", "Al-Munafiqun", 11, "مدنية", 554, 28),
        SurahMeta(64, "التغابن", "At-Taghabun", 18, "مدنية", 556, 28),
        SurahMeta(65, "الطلاق", "At-Talaq", 12, "مدنية", 558, 28),
        SurahMeta(66, "التحريم", "At-Tahrim", 12, "مدنية", 560, 28),
        SurahMeta(67, "الملك", "Al-Mulk", 30, "مكية", 562, 29),
        SurahMeta(68, "القلم", "Al-Qalam", 52, "مكية", 564, 29),
        SurahMeta(69, "الحاقة", "Al-Haqqah", 52, "مكية", 566, 29),
        SurahMeta(70, "المعارج", "Al-Ma'arij", 44, "مكية", 568, 29),
        SurahMeta(71, "نوح", "Nuh", 28, "مكية", 570, 29),
        SurahMeta(72, "الجن", "Al-Jinn", 28, "مكية", 572, 29),
        SurahMeta(73, "المزمل", "Al-Muzzammil", 20, "مكية", 574, 29),
        SurahMeta(74, "المدثر", "Al-Muddaththir", 56, "مكية", 575, 29),
        SurahMeta(75, "القيامة", "Al-Qiyamah", 40, "مكية", 577, 29),
        SurahMeta(76, "الإنسان", "Al-Insan", 31, "مدنية", 578, 29),
        SurahMeta(77, "المرسلات", "Al-Mursalat", 50, "مكية", 580, 29),
        SurahMeta(78, "النبأ", "An-Naba", 40, "مكية", 582, 30),
        SurahMeta(79, "النازعات", "An-Nazi'at", 46, "مكية", 583, 30),
        SurahMeta(80, "عبس", "Abasa", 42, "مكية", 585, 30),
        SurahMeta(81, "التكوير", "At-Takwir", 29, "مكية", 586, 30),
        SurahMeta(82, "الانفطار", "Al-Infitar", 19, "مكية", 587, 30),
        SurahMeta(83, "المطففين", "Al-Mutaffifin", 36, "مكية", 587, 30),
        SurahMeta(84, "الانشقاق", "Al-Inshiqaq", 25, "مكية", 589, 30),
        SurahMeta(85, "البروج", "Al-Buruj", 22, "مكية", 590, 30),
        SurahMeta(86, "الطارق", "At-Tariq", 17, "مكية", 591, 30),
        SurahMeta(87, "الأعلى", "Al-A'la", 19, "مكية", 591, 30),
        SurahMeta(88, "الغاشية", "Al-Ghashiyah", 26, "مكية", 592, 30),
        SurahMeta(89, "الفجر", "Al-Fajr", 30, "مكية", 593, 30),
        SurahMeta(90, "البلد", "Al-Balad", 20, "مكية", 594, 30),
        SurahMeta(91, "الشمس", "Ash-Shams", 15, "مكية", 595, 30),
        SurahMeta(92, "الليل", "Al-Layl", 21, "مكية", 595, 30),
        SurahMeta(93, "الضحى", "Ad-Duha", 11, "مكية", 596, 30),
        SurahMeta(94, "الشرح", "Ash-Sharh", 8, "مكية", 596, 30),
        SurahMeta(95, "التين", "At-Tin", 8, "مكية", 597, 30),
        SurahMeta(96, "العلق", "Al-Alaq", 19, "مكية", 597, 30),
        SurahMeta(97, "القدر", "Al-Qadr", 5, "مكية", 598, 30),
        SurahMeta(98, "البينة", "Al-Bayyinah", 8, "مدنية", 598, 30),
        SurahMeta(99, "الزلزلة", "Az-Zalzalah", 8, "مدنية", 599, 30),
        SurahMeta(100, "العاديات", "Al-Adiyat", 11, "مكية", 599, 30),
        SurahMeta(101, "القارعة", "Al-Qari'ah", 11, "مكية", 600, 30),
        SurahMeta(102, "التكاثر", "At-Takathur", 8, "مكية", 600, 30),
        SurahMeta(103, "العصر", "Al-Asr", 3, "مكية", 601, 30),
        SurahMeta(104, "الهمزة", "Al-Humazah", 9, "مكية", 601, 30),
        SurahMeta(105, "الفيل", "Al-Fil", 5, "مكية", 601, 30),
        SurahMeta(106, "قريش", "Quraysh", 4, "مكية", 602, 30),
        SurahMeta(107, "الماعون", "Al-Ma'un", 7, "مكية", 602, 30),
        SurahMeta(108, "الكوثر", "Al-Kawthar", 3, "مكية", 602, 30),
        SurahMeta(109, "الكافرون", "Al-Kafirun", 6, "مكية", 603, 30),
        SurahMeta(110, "النصر", "An-Nasr", 3, "مدنية", 603, 30),
        SurahMeta(111, "المسد", "Al-Masad", 5, "مكية", 603, 30),
        SurahMeta(112, "الإخلاص", "Al-Ikhlas", 4, "مكية", 604, 30),
        SurahMeta(113, "الفلق", "Al-Falaq", 5, "مكية", 604, 30),
        SurahMeta(114, "الناس", "An-Nas", 6, "مكية", 604, 30)
    )

    private var appContext: Context? = null
    private val surahsCache = ConcurrentHashMap<Int, List<String>>()

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    /**
     * Preloads all 114 Surahs into memory cache asynchronously for instantaneous access.
     */
    fun preloadAll(context: Context) {
        val ctx = context.applicationContext
        appContext = ctx
        if (surahsCache.size >= 114) return

        try {
            val jsonStream = ctx.assets.open("quran/quran_uthmani.json")
            val jsonStr = jsonStream.bufferedReader().use { it.readText() }
            val root = JSONObject(jsonStr)
            val keys = root.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                val surahNum = key.toIntOrNull() ?: continue
                val arr = root.getJSONArray(key)
                val verses = ArrayList<String>(arr.length())
                for (i in 0 until arr.length()) {
                    verses.add(arr.getString(i))
                }
                surahsCache[surahNum] = verses
            }
        } catch (e: Exception) {
            Log.e("QuranData", "Error preloading Quran assets: ${e.message}")
        }
    }

    /**
     * Loads the authentic Uthmani verse texts for any Surah from bundled assets.
     */
    fun getSurahVerseTexts(surahNumber: Int, context: Context? = null): List<String> {
        surahsCache[surahNumber]?.let { return it }

        val ctx = context ?: appContext
        if (ctx != null) {
            // First attempt: load individual surah file (< 50KB, very fast)
            try {
                val stream = ctx.assets.open("quran/surahs/$surahNumber.json")
                val jsonStr = stream.bufferedReader().use { it.readText() }
                val arr = JSONArray(jsonStr)
                val verses = ArrayList<String>(arr.length())
                for (i in 0 until arr.length()) {
                    verses.add(arr.getString(i))
                }
                if (verses.isNotEmpty()) {
                    surahsCache[surahNumber] = verses
                    return verses
                }
            } catch (e: Exception) {
                // Second attempt: load from master file
                try {
                    val stream = ctx.assets.open("quran/quran_uthmani.json")
                    val jsonStr = stream.bufferedReader().use { it.readText() }
                    val root = JSONObject(jsonStr)
                    val sKey = surahNumber.toString()
                    if (root.has(sKey)) {
                        val arr = root.getJSONArray(sKey)
                        val verses = ArrayList<String>(arr.length())
                        for (i in 0 until arr.length()) {
                            verses.add(arr.getString(i))
                        }
                        if (verses.isNotEmpty()) {
                            surahsCache[surahNumber] = verses
                            return verses
                        }
                    }
                } catch (e2: Exception) {
                    Log.e("QuranData", "Error reading Quran assets for surah $surahNumber", e2)
                }
            }
        }

        // Fallback for short surahs if context is not yet available
        val fallback = verifiedSurahsMap[surahNumber]
        if (fallback != null) {
            return fallback.map { it.textUthmani }
        }

        return emptyList()
    }

    // Authentic Uthmani Verses fallback for reading if assets cannot be reached
    private val verifiedSurahsMap = mapOf(
        1 to listOf(
            AyahItem(1, 1, "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ", "https://everyayah.com/data/Alafasy_128kbps/001001.mp3"),
            AyahItem(1, 2, "الْحَمْدُ لِلَّهِ رَبِّ الْعَالَمِينَ", "https://everyayah.com/data/Alafasy_128kbps/001002.mp3"),
            AyahItem(1, 3, "الرَّحْمَٰنِ الرَّحِيمِ", "https://everyayah.com/data/Alafasy_128kbps/001003.mp3"),
            AyahItem(1, 4, "مَالِكِ يَوْمِ الدِّينِ", "https://everyayah.com/data/Alafasy_128kbps/001004.mp3"),
            AyahItem(1, 5, "إِيَّاكَ نَعْبُدُ وَإِيَّاكَ نَسْتَعِينُ", "https://everyayah.com/data/Alafasy_128kbps/001005.mp3"),
            AyahItem(1, 6, "اهْدِنَا الصِّرَاطَ الْمُسْتَقِيمَ", "https://everyayah.com/data/Alafasy_128kbps/001006.mp3"),
            AyahItem(1, 7, "صِرَاطَ الَّذِينَ أَنْعَمْتَ عَلَيْهِمْ غَيْرِ الْمَغْضُوبِ عَلَيْهِمْ وَلَا الضَّالِّينَ", "https://everyayah.com/data/Alafasy_128kbps/001007.mp3")
        ),
        103 to listOf(
            AyahItem(103, 1, "وَالْعَصْرِ", "https://everyayah.com/data/Alafasy_128kbps/103001.mp3"),
            AyahItem(103, 2, "إِنَّ الْإِنسَانَ لَفِي خُسْرٍ", "https://everyayah.com/data/Alafasy_128kbps/103002.mp3"),
            AyahItem(103, 3, "إِلَّا الَّذِينَ آمَنُوا وَعَمِلُوا الصَّالِحَاتِ وَتَوَاصَوْا بِالْحَقِّ وَتَوَاصَوْا بِالصَّبْرِ", "https://everyayah.com/data/Alafasy_128kbps/103003.mp3")
        ),
        108 to listOf(
            AyahItem(108, 1, "إِنَّا أَعْطَيْنَاكَ الْكَوْثَرَ", "https://everyayah.com/data/Alafasy_128kbps/108001.mp3"),
            AyahItem(108, 2, "فَصَلِّ لِرَبِّكَ وَانْحَرْ", "https://everyayah.com/data/Alafasy_128kbps/108002.mp3"),
            AyahItem(108, 3, "إِنَّ شَانِئَكَ هُوَ الْأَبْتَرُ", "https://everyayah.com/data/Alafasy_128kbps/108003.mp3")
        ),
        112 to listOf(
            AyahItem(112, 1, "قُلْ هُوَ اللَّهُ أَحَدٌ", "https://everyayah.com/data/Alafasy_128kbps/112001.mp3"),
            AyahItem(112, 2, "اللَّهُ الصَّمَدُ", "https://everyayah.com/data/Alafasy_128kbps/112002.mp3"),
            AyahItem(112, 3, "لَمْ يَلِدْ وَلَمْ يُولَدْ", "https://everyayah.com/data/Alafasy_128kbps/112003.mp3"),
            AyahItem(112, 4, "وَلَمْ يَكُن لَّهُ كُفُوًا أَحَدٌ", "https://everyayah.com/data/Alafasy_128kbps/112004.mp3")
        ),
        113 to listOf(
            AyahItem(113, 1, "قُلْ أَعُوذُ بِرَبِّ الْفَلَقِ", "https://everyayah.com/data/Alafasy_128kbps/113001.mp3"),
            AyahItem(113, 2, "مِن شَرِّ مَا خَلَقَ", "https://everyayah.com/data/Alafasy_128kbps/113002.mp3"),
            AyahItem(113, 3, "وَمِن شَرِّ غَاسِقٍ إِذَا وَقَبَ", "https://everyayah.com/data/Alafasy_128kbps/113003.mp3"),
            AyahItem(113, 4, "وَمِن شَرِّ النَّفَّاثَاتِ فِي الْعُقَدِ", "https://everyayah.com/data/Alafasy_128kbps/113004.mp3"),
            AyahItem(113, 5, "وَمِن شَرِّ حَاسِدٍ إِذَا حَسَدَ", "https://everyayah.com/data/Alafasy_128kbps/113005.mp3")
        ),
        114 to listOf(
            AyahItem(114, 1, "قُلْ أَعُوذُ بِرَبِّ النَّاسِ", "https://everyayah.com/data/Alafasy_128kbps/114001.mp3"),
            AyahItem(114, 2, "مَلِكِ النَّاسِ", "https://everyayah.com/data/Alafasy_128kbps/114002.mp3"),
            AyahItem(114, 3, "إِلَٰهِ النَّاسِ", "https://everyayah.com/data/Alafasy_128kbps/114003.mp3"),
            AyahItem(114, 4, "مِن شَرِّ الْوَسْوَاسِ الْخَنَّاسِ", "https://everyayah.com/data/Alafasy_128kbps/114004.mp3"),
            AyahItem(114, 5, "الَّذِي يُوَسْوِسُ فِي صُدُورِ النَّاسِ", "https://everyayah.com/data/Alafasy_128kbps/114005.mp3"),
            AyahItem(114, 6, "مِنَ الْجِنَّةِ وَالنَّاسِ", "https://everyayah.com/data/Alafasy_128kbps/114006.mp3")
        )
    )

    fun getSurahAyahs(
        surahNumber: Int,
        reciterId: String = "alafasy",
        context: Context? = null
    ): List<AyahItem> {
        val verseTexts = getSurahVerseTexts(surahNumber, context)
        if (verseTexts.isNotEmpty()) {
            return verseTexts.mapIndexed { index, text ->
                val ayahNum = index + 1
                AyahItem(
                    surahNumber = surahNumber,
                    ayahNumber = ayahNum,
                    textUthmani = text,
                    audioUrl = RecitersData.getAyahAudioUrl(reciterId, surahNumber, ayahNum)
                )
            }
        }

        // Verified fallback map if present
        verifiedSurahsMap[surahNumber]?.let { list ->
            return list.map { it.copy(audioUrl = RecitersData.getAyahAudioUrl(reciterId, it.surahNumber, it.ayahNumber)) }
        }

        // Fallback verse count
        val surah = allSurahs.find { it.number == surahNumber }
        val count = surah?.versesCount ?: 7
        return (1..count).map { ayahNum ->
            AyahItem(
                surahNumber = surahNumber,
                ayahNumber = ayahNum,
                textUthmani = if (ayahNum == 1) "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ" else "﴿$ayahNum﴾",
                audioUrl = RecitersData.getAyahAudioUrl(reciterId, surahNumber, ayahNum)
            )
        }
    }

    fun searchSurahs(query: String): List<SurahMeta> {
        val cleanQuery = query.trim().replace("أ", "ا").replace("إ", "ا").replace("آ", "ا")
        if (cleanQuery.isEmpty()) return allSurahs
        return allSurahs.filter { surah ->
            val normalizedName = surah.nameAr.replace("أ", "ا").replace("إ", "ا").replace("آ", "ا")
            normalizedName.contains(cleanQuery, ignoreCase = true) ||
                    surah.nameEn.contains(cleanQuery, ignoreCase = true) ||
                    surah.number.toString() == cleanQuery
        }
    }
}
