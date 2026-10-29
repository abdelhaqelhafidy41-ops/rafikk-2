package com.example.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.adhkar.AdhkarData
import com.example.data.adhkar.DhikrCategory
import com.example.data.adhkar.DhikrItem
import com.example.data.database.TasbeehRecordEntity
import com.example.data.sunnah.HadithItem
import com.example.data.sunnah.SunnahData
import com.example.ui.theme.*

enum class AdhkarMainTab(val titleAr: String) {
    ATHKAR("الأذكار والأدعية"),
    SUNNAH("الكتب السنية"),
    TASBEEH("السبحة الإلكترونية")
}

@Composable
fun AdhkarScreen(
    tasbeeh: TasbeehRecordEntity,
    onIncrementTasbeeh: () -> Unit,
    onResetTasbeeh: () -> Unit,
    onSetTasbeehDhikr: (String, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var currentMainTab by remember { mutableStateOf(AdhkarMainTab.ATHKAR) }

    // Athkar Sub-State
    var selectedCategory by remember { mutableStateOf(DhikrCategory.MORNING) }
    var athkarSearchQuery by remember { mutableStateOf("") }
    val currentCounts = remember { mutableStateMapOf<Int, Int>() }

    // Sunnah Sub-State
    var selectedBookId by remember { mutableStateOf("bukhari") }
    var selectedChapter by remember(selectedBookId) { mutableStateOf<String?>(null) }
    var sunnahSearchQuery by remember { mutableStateOf("") }

    fun vibrate() {
        try {
            val v = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v?.vibrate(VibrationEffect.createOneShot(35, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                v?.vibrate(35)
            }
        } catch (_: Exception) {}
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("adhkar_screen_column")
    ) {
        // Top Header
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "الأذكار والكتب السنية",
                    style = MaterialTheme.typography.titleLarge,
                    fontFamily = CairoFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "﴿أَلَا بِذِكْرِ اللَّهِ تَطْمَئِنُّ الْقُلُوبُ﴾",
                    style = MaterialTheme.typography.bodyMedium,
                    fontFamily = AmiriFontFamily,
                    color = IslamicGoldDark
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Main Top Tabs
                TabRow(
                    selectedTabIndex = currentMainTab.ordinal,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    contentColor = IslamicGreenPrimary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                ) {
                    AdhkarMainTab.values().forEach { tab ->
                        Tab(
                            selected = currentMainTab == tab,
                            onClick = { currentMainTab = tab },
                            text = {
                                Text(
                                    text = tab.titleAr,
                                    fontFamily = CairoFontFamily,
                                    fontWeight = if (currentMainTab == tab) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        )
                    }
                }
            }
        }

        when (currentMainTab) {
            AdhkarMainTab.ATHKAR -> {
                // Category Filter Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(DhikrCategory.values()) { category ->
                        val isSelected = selectedCategory == category
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategory = category },
                            label = { Text(category.titleAr, fontFamily = CairoFontFamily) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = IslamicGreenPrimary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                // Search Bar
                OutlinedTextField(
                    value = athkarSearchQuery,
                    onValueChange = { athkarSearchQuery = it },
                    placeholder = { Text("ابحث في الأدعية والأذكار...", fontFamily = CairoFontFamily, fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "بحث") },
                    trailingIcon = {
                        if (athkarSearchQuery.isNotEmpty()) {
                            IconButton(onClick = { athkarSearchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "مسح")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                )

                val displayList = remember(selectedCategory, athkarSearchQuery) {
                    if (athkarSearchQuery.isNotBlank()) {
                        AdhkarData.searchAthkar(athkarSearchQuery)
                    } else {
                        AdhkarData.getAthkarByCategory(selectedCategory)
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("athkar_list"),
                    contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 96.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(displayList, key = { it.id }) { dhikr ->
                        val doneCount = currentCounts[dhikr.id] ?: 0
                        val isCompleted = doneCount >= dhikr.count

                        AthkarCard(
                            dhikr = dhikr,
                            doneCount = doneCount,
                            isCompleted = isCompleted,
                            onIncrement = {
                                if (doneCount < dhikr.count) {
                                    vibrate()
                                    currentCounts[dhikr.id] = doneCount + 1
                                }
                            },
                            onReset = { currentCounts[dhikr.id] = 0 },
                            onCopy = {
                                val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                cm.setPrimaryClip(ClipData.newPlainText("Dhikr", "${dhikr.text}\n[${dhikr.source}]"))
                                Toast.makeText(context, "تم نسخ الذكر", Toast.LENGTH_SHORT).show()
                            },
                            onShare = {
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, "${dhikr.text}\n\n[المصدر: ${dhikr.source}]\n${dhikr.virtue}")
                                    type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "مشاركة الذكر أو الدعاء"))
                            }
                        )
                    }
                }
            }

            AdhkarMainTab.SUNNAH -> {
                // Sunnah Books selector
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(SunnahData.books) { book ->
                        val isSelected = selectedBookId == book.id
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedBookId = book.id
                                selectedChapter = null
                            },
                            label = { Text("${book.titleAr} (${book.hadithsCount})", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = IslamicGold,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                // Sunnah Hadith Search
                OutlinedTextField(
                    value = sunnahSearchQuery,
                    onValueChange = { sunnahSearchQuery = it },
                    placeholder = { Text("ابحث في متون وسند الأحاديث الشريفة...", fontFamily = CairoFontFamily, fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "بحث") },
                    trailingIcon = {
                        if (sunnahSearchQuery.isNotEmpty()) {
                            IconButton(onClick = { sunnahSearchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "مسح")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                )

                // Chapter filter chips when not searching
                val availableChapters = remember(selectedBookId) {
                    SunnahData.getChaptersForBook(selectedBookId)
                }
                if (sunnahSearchQuery.isBlank() && availableChapters.size > 1) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        item {
                            FilterChip(
                                selected = selectedChapter == null,
                                onClick = { selectedChapter = null },
                                label = { Text("جميع الأبواب", fontFamily = CairoFontFamily, fontSize = 12.sp) }
                            )
                        }
                        items(availableChapters) { chapter ->
                            val isSelected = selectedChapter == chapter
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedChapter = if (isSelected) null else chapter },
                                label = { Text(chapter, fontFamily = CairoFontFamily, fontSize = 12.sp) }
                            )
                        }
                    }
                }

                val currentBook = remember(selectedBookId) { SunnahData.getBookById(selectedBookId) }
                val hadithList = remember(selectedBookId, selectedChapter, sunnahSearchQuery) {
                    val list = if (sunnahSearchQuery.isNotBlank()) {
                        SunnahData.searchHadiths(sunnahSearchQuery, selectedBookId)
                    } else {
                        SunnahData.getHadithsByBook(selectedBookId)
                    }
                    if (selectedChapter != null && sunnahSearchQuery.isBlank()) {
                        list.filter { it.chapterTitle == selectedChapter }
                    } else {
                        list
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("sunnah_hadiths_list"),
                    contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 96.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (sunnahSearchQuery.isBlank() && currentBook != null && selectedChapter == null) {
                        item {
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = IslamicGreenContainer),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = currentBook.titleAr,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontFamily = CairoFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            color = IslamicGreenDark
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = IslamicGold.copy(alpha = 0.2f)
                                        ) {
                                            Text(
                                                text = "${currentBook.hadithsCount} حديث معتمد",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontFamily = CairoFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                color = IslamicGoldDark,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = currentBook.authorAr,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontFamily = CairoFontFamily,
                                        color = IslamicGreenPrimary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = currentBook.descriptionAr,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontFamily = CairoFontFamily,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    items(hadithList, key = { it.id }) { hadith ->
                        HadithCard(
                            hadith = hadith,
                            onCopy = {
                                val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                cm.setPrimaryClip(ClipData.newPlainText("Hadith", "${hadith.narrator}\n${hadith.arabicText}\n[${hadith.grade}]"))
                                Toast.makeText(context, "تم نسخ الحديث الشريف", Toast.LENGTH_SHORT).show()
                            },
                            onShare = {
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, "${hadith.narrator}\n\n${hadith.arabicText}\n\n[الحكم: ${hadith.grade}]\n${hadith.explanationOrBenefit}")
                                    type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "مشاركة الحديث الشريف"))
                            }
                        )
                    }
                }
            }

            AdhkarMainTab.TASBEEH -> {
                ElectronicTasbeehView(
                    tasbeeh = tasbeeh,
                    onTap = {
                        vibrate()
                        onIncrementTasbeeh()
                    },
                    onReset = onResetTasbeeh,
                    onSelectDhikr = onSetTasbeehDhikr
                )
            }
        }
    }
}

@Composable
fun AthkarCard(
    dhikr: DhikrItem,
    doneCount: Int,
    isCompleted: Boolean,
    onIncrement: () -> Unit,
    onReset: () -> Unit,
    onCopy: () -> Unit,
    onShare: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("dhikr_card_${dhikr.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCompleted) IslamicGreenContainer else MaterialTheme.colorScheme.surface
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(
                if (isCompleted) listOf(IslamicGreenPrimary, IslamicGold)
                else listOf(MaterialTheme.colorScheme.outlineVariant, MaterialTheme.colorScheme.surfaceVariant)
            )
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Top Bar of Card
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isCompleted) IslamicGreenPrimary else IslamicGoldContainer
                ) {
                    Text(
                        text = if (isCompleted) "تم الانتهاء ✓" else "التكرار: ${dhikr.count} مرات",
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = CairoFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = if (isCompleted) Color.White else IslamicGoldOnContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onCopy, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "نسخ الذكر", modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onShare, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Share, contentDescription = "مشاركة الذكر", modifier = Modifier.size(18.dp))
                    }
                    if (doneCount > 0) {
                        IconButton(onClick = onReset, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Refresh, contentDescription = "إعادة البدء", modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Dhikr Arabic Text
            Text(
                text = dhikr.text,
                style = MaterialTheme.typography.bodyLarge,
                fontFamily = AmiriFontFamily,
                fontSize = 19.sp,
                lineHeight = 32.sp,
                textAlign = TextAlign.Start,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (dhikr.virtue.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, contentDescription = "فضل الذكر", tint = IslamicGold, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = dhikr.virtue,
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = CairoFontFamily,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = dhikr.source,
                style = MaterialTheme.typography.labelSmall,
                fontFamily = CairoFontFamily,
                color = MaterialTheme.colorScheme.outline
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Interactive Progress & Counter Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Progress Bar
                LinearProgressIndicator(
                    progress = { if (dhikr.count > 0) doneCount.toFloat() / dhikr.count else 0f },
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = if (isCompleted) IslamicGreenPrimary else IslamicGold,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )

                Spacer(modifier = Modifier.width(16.dp))

                Button(
                    onClick = onIncrement,
                    enabled = !isCompleted,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isCompleted) IslamicGreenPrimary else IslamicGold,
                        disabledContainerColor = IslamicGreenPrimary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "$doneCount / ${dhikr.count}",
                        fontFamily = CairoFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun HadithCard(
    hadith: HadithItem,
    onCopy: () -> Unit,
    onShare: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("hadith_card_${hadith.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = IslamicGreenContainer
                ) {
                    Text(
                        text = "${hadith.chapterTitle} • حديث #${hadith.hadithNumber}",
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = CairoFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = IslamicGreenDark,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onCopy, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "نسخ الحديث", modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onShare, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Share, contentDescription = "مشاركة الحديث", modifier = Modifier.size(18.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Narrator
            Text(
                text = hadith.narrator,
                style = MaterialTheme.typography.bodySmall,
                fontFamily = CairoFontFamily,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Matn (Arabic Text)
            Text(
                text = hadith.arabicText,
                style = MaterialTheme.typography.bodyLarge,
                fontFamily = AmiriFontFamily,
                fontSize = 18.sp,
                lineHeight = 30.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Benefit / Explanation
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = "من فوائد الحديث وفقهه:",
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = CairoFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = IslamicGoldDark
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = hadith.explanationOrBenefit,
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = CairoFontFamily,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "الحكم: ${hadith.grade}",
                style = MaterialTheme.typography.labelSmall,
                fontFamily = CairoFontFamily,
                fontWeight = FontWeight.SemiBold,
                color = IslamicGreenPrimary
            )
        }
    }
}

@Composable
fun ElectronicTasbeehView(
    tasbeeh: TasbeehRecordEntity,
    onTap: () -> Unit,
    onReset: () -> Unit,
    onSelectDhikr: (String, Int) -> Unit
) {
    var showDhikrPicker by remember { mutableStateOf(false) }

    val progress = remember(tasbeeh.currentCount, tasbeeh.targetCount) {
        if (tasbeeh.targetCount > 0) {
            (tasbeeh.currentCount % tasbeeh.targetCount).toFloat() / tasbeeh.targetCount
        } else 0f
    }

    val animatedProgress by animateFloatAsState(targetValue = progress)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .testTag("tasbeeh_interactive_view"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Selected Dhikr Display
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showDhikrPicker = true }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "الذكر الحالي (اضغط للتغيير)",
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = CairoFontFamily,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = tasbeeh.dhikrText,
                        style = MaterialTheme.typography.titleMedium,
                        fontFamily = AmiriFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = IslamicGreenPrimary
                    )
                }
                Icon(Icons.Default.ArrowDropDown, contentDescription = "تغيير الذكر", tint = IslamicGold)
            }
        }

        // Circular Tappable Counter
        Box(
            modifier = Modifier
                .size(260.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(IslamicGreenPrimary, IslamicGreenDark)
                    )
                )
                .clickable { onTap() }
                .testTag("btn_tasbeeh_tap"),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${tasbeeh.currentCount}",
                    style = MaterialTheme.typography.displayLarge,
                    fontFamily = CairoFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "الهدف: ${tasbeeh.targetCount}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontFamily = CairoFontFamily,
                    color = IslamicGoldLight
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "المس للعد",
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = CairoFontFamily,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }

        // Bottom Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "المجموع الكلي: ${tasbeeh.totalCount}",
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = CairoFontFamily,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedButton(
                onClick = onReset,
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "تصفير", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("تصفير العداد", fontFamily = CairoFontFamily)
            }
        }
    }

    if (showDhikrPicker) {
        AlertDialog(
            onDismissRequest = { showDhikrPicker = false },
            title = { Text("اختر الذكر والهدف", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold) },
            text = {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(AdhkarData.tasbeehPresets) { preset ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSelectDhikr(preset.text, preset.target)
                                    showDhikrPicker = false
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(preset.text, fontFamily = AmiriFontFamily, fontSize = 16.sp)
                                Text("${preset.target} مرة", fontFamily = CairoFontFamily, fontSize = 12.sp, color = IslamicGreenPrimary)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDhikrPicker = false }) {
                    Text("إغلاق", fontFamily = CairoFontFamily)
                }
            }
        )
    }
}
