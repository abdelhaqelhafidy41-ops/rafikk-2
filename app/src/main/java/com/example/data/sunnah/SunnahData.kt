package com.example.data.sunnah

object SunnahData {

    val books: List<SunnahBook> = listOf(
        SunnahBook(
            id = "bukhari",
            titleAr = "صحيح البخاري",
            authorAr = "الإمام محمد بن إسماعيل البخاري رحمه الله",
            descriptionAr = "أصح كتاب بعد كتاب الله عز وجل، التزم فيه الإمام البخاري أعلى درجات الصحة والتوثيق والتحري.",
            chaptersCount = 18,
            hadithsCount = BukhariHadiths.items.size,
            iconName = "menu_book"
        ),
        SunnahBook(
            id = "muslim",
            titleAr = "صحيح مسلم",
            authorAr = "الإمام مسلم بن الحجاج النيسابوري رحمه الله",
            descriptionAr = "ثاني أصح دواوين الحديث النبوي الشريف، يتميز بحسن الترتيب وسياق طرق الحديث في موضع واحد.",
            chaptersCount = 10,
            hadithsCount = MuslimHadiths.items.size,
            iconName = "library_books"
        ),
        SunnahBook(
            id = "nawawi40",
            titleAr = "الأربعون النووية",
            authorAr = "الإمام يحيى بن شرف النووي رحمه الله",
            descriptionAr = "أشهر وأشمل الأحاديث النبوية الجامعة التي يدور عليها مدار الإسلام وقواعد الدين والأخلاق.",
            chaptersCount = 8,
            hadithsCount = NawawiHadiths.items.size,
            iconName = "auto_stories"
        ),
        SunnahBook(
            id = "riyadh",
            titleAr = "رياض الصالحين",
            authorAr = "الإمام يحيى بن شرف النووي رحمه الله",
            descriptionAr = "من أنفع كتب الأخلاق والآداب والرقائق وسلوك المسلم في كل شؤون حياته اليومية.",
            chaptersCount = 9,
            hadithsCount = RiyadhHadiths.items.size,
            iconName = "import_contacts"
        ),
        SunnahBook(
            id = "abudawood",
            titleAr = "سنن أبي داود",
            authorAr = "الإمام سليمان بن الأشعث السجستاني رحمه الله",
            descriptionAr = "من أمهات كتب السنة المعتمدة في أحكام الفقه والآداب والسنن النبوية الشريفة.",
            chaptersCount = 3,
            hadithsCount = SunanHadiths.items.count { it.bookId == "abudawood" },
            iconName = "menu_book"
        ),
        SunnahBook(
            id = "tirmidhi",
            titleAr = "جامع الترمذي",
            authorAr = "الإمام محمد بن عيسى الترمذي رحمه الله",
            descriptionAr = "ديوان جليل جمع بين متون الأحاديث وبيان درجاتها وفقه الأئمة والصحابة والتابعين.",
            chaptersCount = 3,
            hadithsCount = SunanHadiths.items.count { it.bookId == "tirmidhi" },
            iconName = "library_books"
        ),
        SunnahBook(
            id = "nasai",
            titleAr = "سنن النسائي",
            authorAr = "الإمام أحمد بن شعيب النسائي رحمه الله",
            descriptionAr = "المجتبى من السنن؛ اشتهر بدقة التحري في نقد الأسانيد وعلو المنزلة بين كتب السنن الأربعة.",
            chaptersCount = 2,
            hadithsCount = SunanHadiths.items.count { it.bookId == "nasai" },
            iconName = "auto_stories"
        ),
        SunnahBook(
            id = "ibnmajah",
            titleAr = "سنن ابن ماجه",
            authorAr = "الإمام محمد بن يزيد القزويني رحمه الله",
            descriptionAr = "سادس الكتب الستة المعتمدة، يتميز بحسن التبويب وجمع المسائل والفوائد الزائدة.",
            chaptersCount = 2,
            hadithsCount = SunanHadiths.items.count { it.bookId == "ibnmajah" },
            iconName = "import_contacts"
        ),
        SunnahBook(
            id = "muwatta",
            titleAr = "موطأ الإمام مالك",
            authorAr = "إمام دار الهجرة مالك بن أنس رحمه الله",
            descriptionAr = "من أقدم وأوثق مصادر السنة النبوية وفقه أهل المدينة المنورة، صُنّف بعناية فائقة.",
            chaptersCount = 2,
            hadithsCount = SunanHadiths.items.count { it.bookId == "muwatta" },
            iconName = "menu_book"
        )
    )

    val allHadiths: List<HadithItem> by lazy {
        BukhariHadiths.items +
                MuslimHadiths.items +
                NawawiHadiths.items +
                RiyadhHadiths.items +
                SunanHadiths.items
    }

    fun getBookById(bookId: String): SunnahBook? {
        return books.find { it.id == bookId }
    }

    fun getHadithsByBook(bookId: String): List<HadithItem> {
        return allHadiths.filter { it.bookId == bookId }
    }

    fun getChaptersForBook(bookId: String): List<String> {
        return getHadithsByBook(bookId).map { it.chapterTitle }.distinct()
    }

    fun searchHadiths(query: String, bookId: String? = null): List<HadithItem> {
        val baseList = if (bookId != null) getHadithsByBook(bookId) else allHadiths
        if (query.isBlank()) return baseList
        val clean = query.trim().replace("أ", "ا").replace("إ", "ا").replace("آ", "ا")
        return baseList.filter {
            val txt = it.arabicText.replace("أ", "ا").replace("إ", "ا").replace("آ", "ا")
            val narr = it.narrator.replace("أ", "ا").replace("إ", "ا").replace("آ", "ا")
            val ch = it.chapterTitle.replace("أ", "ا").replace("إ", "ا").replace("آ", "ا")
            val exp = it.explanationOrBenefit.replace("أ", "ا").replace("إ", "ا").replace("آ", "ا")
            txt.contains(clean, ignoreCase = true) ||
                    narr.contains(clean, ignoreCase = true) ||
                    ch.contains(clean, ignoreCase = true) ||
                    exp.contains(clean, ignoreCase = true)
        }
    }
}
