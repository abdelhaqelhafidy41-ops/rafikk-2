package com.example.data.quran

data class ReciterInfo(
    val id: String,
    val nameAr: String,
    val styleAr: String,
    val everyAyahSubfolder: String,
    val descriptionAr: String
)

object RecitersData {

    val allReciters: List<ReciterInfo> get() = famousReciters

    val famousReciters = listOf(
        ReciterInfo(
            id = "alafasy",
            nameAr = "مشاري راشد العفاسي",
            styleAr = "مرتل",
            everyAyahSubfolder = "Alafasy_128kbps",
            descriptionAr = "تلاوة هادئة وواضحة برواية حفص عن عاصم"
        ),
        ReciterInfo(
            id = "abdulbasit_murattal",
            nameAr = "عبد الباسط عبد الصمد",
            styleAr = "مرتل",
            everyAyahSubfolder = "Abdul_Basit_Murattal_192kbps",
            descriptionAr = "المصحف المرتل برواية حفص عن عاصم"
        ),
        ReciterInfo(
            id = "abdulbasit_mujawwad",
            nameAr = "عبد الباسط عبد الصمد",
            styleAr = "مجود",
            everyAyahSubfolder = "Abdul_Basit_Mujawwad_128kbps",
            descriptionAr = "المصحف المجود بتطريب وإتقان المقامات"
        ),
        ReciterInfo(
            id = "minshawy_murattal",
            nameAr = "محمد صديق المنشاوي",
            styleAr = "مرتل",
            everyAyahSubfolder = "Minshawy_Murattal_128kbps",
            descriptionAr = "الصوت الباكي بتلاوة خاشعة مرقعة للقلوب"
        ),
        ReciterInfo(
            id = "minshawy_mujawwad",
            nameAr = "محمد صديق المنشاوي",
            styleAr = "مجود",
            everyAyahSubfolder = "Minshawy_Mujawwad_192kbps",
            descriptionAr = "المصحف المجود برواية حفص عن عاصم"
        ),
        ReciterInfo(
            id = "husary",
            nameAr = "محمود خليل الحصري",
            styleAr = "مرتل (معلم)",
            everyAyahSubfolder = "Husary_128kbps",
            descriptionAr = "شيخ المقارئ المصرية ودقة مخارج الحروف وأحكام التجويد"
        ),
        ReciterInfo(
            id = "ghamadi",
            nameAr = "سعد الغامدي",
            styleAr = "مرتل",
            everyAyahSubfolder = "Ghamadi_40kbps",
            descriptionAr = "تلاوة شجية عذبة برواية حفص عن عاصم"
        ),
        ReciterInfo(
            id = "maher",
            nameAr = "ماهر المعيقلي",
            styleAr = "مرتل",
            everyAyahSubfolder = "Maher_AlMuaiqly_64kbps",
            descriptionAr = "إمام الحرم المكي الشريف وتلاوة خاشعة محببة"
        ),
        ReciterInfo(
            id = "sudais",
            nameAr = "عبد الرحمن السديس",
            styleAr = "مرتل",
            everyAyahSubfolder = "Abdurrahmaan_As-Sudais_192kbps",
            descriptionAr = "إمام وخطيب المسجد الحرام برواية حفص"
        ),
        ReciterInfo(
            id = "dussary",
            nameAr = "ياسر الدوسري",
            styleAr = "مرتل",
            everyAyahSubfolder = "Yasser_Ad-Dussary_128kbps",
            descriptionAr = "إمام الحرم المكي بنبرة مؤثرة وخشوع مميز"
        ),
        ReciterInfo(
            id = "abbad",
            nameAr = "فارس عباد",
            styleAr = "مرتل",
            everyAyahSubfolder = "Fares_Abbad_64kbps",
            descriptionAr = "تلاوة يمنية شجية نالت شهرة واسعة"
        ),
        ReciterInfo(
            id = "rifai",
            nameAr = "نبيل الرفاعي",
            styleAr = "مرتل",
            everyAyahSubfolder = "Nabil_Rifay_48kbps",
            descriptionAr = "تلاوة خاشعة برواية حفص عن عاصم"
        )
    )

    fun getReciterById(id: String): ReciterInfo {
        return famousReciters.find { it.id == id } ?: famousReciters.first()
    }

    fun getAyahAudioUrl(reciterId: String, surahNumber: Int, ayahNumber: Int): String {
        val reciter = getReciterById(reciterId)
        val padSurah = surahNumber.toString().padStart(3, '0')
        val padAyah = ayahNumber.toString().padStart(3, '0')
        return "https://everyayah.com/data/${reciter.everyAyahSubfolder}/$padSurah$padAyah.mp3"
    }
}
