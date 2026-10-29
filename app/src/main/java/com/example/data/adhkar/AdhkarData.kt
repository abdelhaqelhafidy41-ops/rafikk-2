package com.example.data.adhkar

object AdhkarData {

    val dailyInspirations = listOf(
        DailyInspiration(
            quote = "﴿أَلَا بِذِكْرِ اللَّهِ تَطْمَئِنُّ الْقُلُوبُ﴾",
            source = "سورة الرعد: ٢٨",
            type = "آية كريمة"
        ),
        DailyInspiration(
            quote = "«مَثَلُ الَّذِي يَذْكُرُ رَبَّهُ وَالَّذِي لا يَذْكُرُ رَبَّهُ، مَثَلُ الحَيِّ وَالمَيِّتِ»",
            source = "صحيح البخاري (٦٤٠٧)",
            type = "حديث شريف"
        ),
        DailyInspiration(
            quote = "﴿وَسَبِّحْ بِحَمْدِ رَبِّكَ قَبْلَ طُلُوعِ الشَّمْسِ وَقَبْلَ غُرُوبِهَا﴾",
            source = "سورة طه: ١٣٠",
            type = "آية كريمة"
        ),
        DailyInspiration(
            quote = "«كَلِمَتَانِ خَفِيفَتَانِ عَلَى اللِّسَانِ، ثَقِيلَتَانِ فِي المِيزَانِ، حَبِيبَتَانِ إِلَى الرَّحْمَنِ: سُبْحَانَ اللَّهِ وَبِحَمْدِهِ، سُبْحَانَ اللَّهِ العَظِيمِ»",
            source = "متفق عليه (البخاري ٦٤٠٦، مسلم ٢٦٩٤)",
            type = "حديث شريف"
        ),
        DailyInspiration(
            quote = "﴿فَاذْكُرُونِي أَذْكُرْكُمْ وَاشْكُرُوا لِي وَلَا تَكْفُرُونِ﴾",
            source = "سورة البقرة: ١٥٢",
            type = "آية كريمة"
        ),
        DailyInspiration(
            quote = "«مَنْ صَلَّى عَلَيَّ صَلَاةً صَلَّى اللهُ عَلَيْهِ بِهَا عَشْرًا»",
            source = "صحيح مسلم (٣٨٤)",
            type = "حديث شريف"
        ),
        DailyInspiration(
            quote = "«أَحَبُّ الْكَلَامِ إِلَى اللهِ أَرْبَعٌ: سُبْحَانَ اللهِ، وَالْحَمْدُ لِلَّهِ، وَلَا إِلَهَ إِلَّا اللهُ، وَاللهُ أَكْبَرُ»",
            source = "صحيح مسلم (٢١٣٧)",
            type = "حديث شريف"
        ),
        DailyInspiration(
            quote = "﴿وَإِذَا سَأَلَكَ عِبَادِي عَنِّي فَإِنِّي قَرِيبٌ أُجِيبُ دَعْوَةَ الدَّاعِ إِذَا دَعَانِ﴾",
            source = "سورة البقرة: ١٨٦",
            type = "آية كريمة"
        )
    )

    val allAthkar: List<DhikrItem> by lazy {
        DailyAthkar.items + QuranicDuas.items + PropheticDuas.items
    }

    val morningAthkar: List<DhikrItem> get() = getAthkarByCategory(DhikrCategory.MORNING)
    val eveningAthkar: List<DhikrItem> get() = getAthkarByCategory(DhikrCategory.EVENING)
    val afterPrayerAthkar: List<DhikrItem> get() = getAthkarByCategory(DhikrCategory.AFTER_PRAYER)
    val sleepAthkar: List<DhikrItem> get() = getAthkarByCategory(DhikrCategory.SLEEP)
    val wakingAthkar: List<DhikrItem> get() = getAthkarByCategory(DhikrCategory.WAKING)

    val tasbeehPresets = listOf(
        TasbeehPreset("سُبْحَانَ اللَّهِ", 33),
        TasbeehPreset("الْحَمْدُ لِلَّهِ", 33),
        TasbeehPreset("اللَّهُ أَكْبَرُ", 34),
        TasbeehPreset("لَا إِلَهَ إِلَّا اللَّهُ", 100),
        TasbeehPreset("أَسْتَغْفِرُ اللَّهَ وَأَتُوبُ إِلَيْهِ", 100),
        TasbeehPreset("اللَّهُمَّ صَلِّ وَسَلِّمْ عَلَى نَبِيِّنَا مُحَمَّدٍ", 100),
        TasbeehPreset("لَا حَوْلَ وَلَا قُوَّةَ إِلَّا بِاللَّهِ", 100),
        TasbeehPreset("سُبْحَانَ اللَّهِ وَبِحَمْدِهِ ، سُبْحَانَ اللَّهِ الْعَظِيمِ", 100),
        TasbeehPreset("يَا حَيُّ يَا قَيُّومُ بِرَحْمَتِكَ أَسْتَغِيثُ", 33),
        TasbeehPreset("حَسْبُنَا اللَّهُ وَنِعْمَ الْوَكِيلُ", 100)
    )

    fun getAthkarByCategory(category: DhikrCategory): List<DhikrItem> {
        return allAthkar.filter { it.category == category }
    }

    fun searchAthkar(query: String): List<DhikrItem> {
        if (query.isBlank()) return allAthkar
        val clean = query.trim().replace("أ", "ا").replace("إ", "ا").replace("آ", "ا")
        return allAthkar.filter {
            val txt = it.text.replace("أ", "ا").replace("إ", "ا").replace("آ", "ا")
            val cat = it.category.titleAr.replace("أ", "ا").replace("إ", "ا").replace("آ", "ا")
            val vir = it.virtue.replace("أ", "ا").replace("إ", "ا").replace("آ", "ا")
            val src = it.source.replace("أ", "ا").replace("إ", "ا").replace("آ", "ا")
            txt.contains(clean, ignoreCase = true) ||
                    cat.contains(clean, ignoreCase = true) ||
                    vir.contains(clean, ignoreCase = true) ||
                    src.contains(clean, ignoreCase = true)
        }
    }
}
