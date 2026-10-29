package com.example.data.adhkar

data class DhikrItem(
    val id: Int,
    val category: DhikrCategory,
    val text: String,
    val count: Int,
    val virtue: String,
    val source: String
)

enum class DhikrCategory(val titleAr: String, val subtitleAr: String) {
    MORNING("أذكار الصباح", "تحصين وبداية مباركة لليوم"),
    EVENING("أذكار المساء", "حفظ وسكينة في المساء والليل"),
    AFTER_PRAYER("أذكار بعد الصلاة", "الأدعية المأثورة دبر الصلوات المكتوبة"),
    SLEEP("أذكار النوم", "آيات وأذكار قبل النوم وحفظ النفس"),
    WAKING("أذكار الاستيقاظ", "حمد الله على تجدد نعمة الحياة"),
    MOSQUE("أذكار المسجد", "دعاء الذهاب ودخول المسجد والخروج منه"),
    FOOD("أذكار الطعام والشراب", "التسمية والحمد والشكر على النعم"),
    TRAVEL("أذكار السفر والخروج", "دعاء الركوب والسفر والخروج من المنزل"),
    DISTRESS_WORRY("أدعية تفريج الكرب", "أدعية الهم والحزن وتفريج الشدائد وقضاء الدين"),
    ISTIKHARA("دعاء صلاة الاستخارة", "طلب الخيرة من الله في سائر الأمور"),
    SICKNESS("أدعية الشفاء والرقية", "الرقية الشرعية وأدعية عيادة المريض"),
    QURANIC_DUA("أدعية من القرآن الكريم", "جوامع الدعاء في كتاب الله العزيز"),
    PROPHETIC_DUA("جوامع الأدعية النبوية", "أدعية جامعة من صحيح السنة المطهرة"),
    FORGIVENESS("أدعية الاستغفار والتوبة", "طلب المغفرة وتجديد التوبة ومحو الخطايا"),
    PARENTS_FAMILY("أدعية الوالدين والذرية", "بر الوالدين وصلاح الأهل والأبناء والبركة")
}

data class DailyInspiration(
    val quote: String,
    val source: String,
    val type: String // "آية كريمة" or "حديث شريف"
)

data class TasbeehPreset(
    val text: String,
    val target: Int
)
