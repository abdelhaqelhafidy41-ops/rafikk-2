package com.example.data.sunnah

data class SunnahBook(
    val id: String,
    val titleAr: String,
    val authorAr: String,
    val descriptionAr: String,
    val chaptersCount: Int,
    val hadithsCount: Int,
    val iconName: String
)

data class HadithChapter(
    val bookId: String,
    val chapterId: Int,
    val titleAr: String,
    val hadithList: List<HadithItem> = emptyList()
)

data class HadithItem(
    val id: Int,
    val bookId: String,
    val chapterTitle: String,
    val hadithNumber: Int,
    val narrator: String,
    val arabicText: String,
    val explanationOrBenefit: String,
    val grade: String = "صحيح"
)
