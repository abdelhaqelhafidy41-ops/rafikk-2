package com.example.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.QuranAudioController
import com.example.audio.QuranAudioService
import com.example.data.database.BookmarkEntity
import com.example.data.quran.AyahItem
import com.example.data.quran.QuranData
import com.example.data.quran.RecitersData
import com.example.data.quran.SurahMeta
import com.example.ui.theme.*
import kotlinx.coroutines.launch

enum class QuranDisplayMode(val titleAr: String) {
    MUSHAF("قراءة متصلة (مصحف)"),
    VERSES("قراءة متتابعة (آيات)")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SurahReadingScreen(
    surah: SurahMeta,
    ayahs: List<AyahItem>,
    bookmarks: List<BookmarkEntity>,
    quranFontSize: Int,
    selectedReciterId: String,
    audioSpeed: Float,
    repeatMode: String,
    isAudioPlaying: Boolean,
    isAudioLoading: Boolean,
    playingAyahNumber: Int?,
    onBack: () -> Unit,
    onToggleBookmark: (Int, String, Int, String) -> Unit,
    onSaveLastRead: (Int, String, Int) -> Unit,
    onPlayAyahAudio: (AyahItem) -> Unit,
    onPauseAudio: () -> Unit,
    onStopAudio: () -> Unit,
    onPlayNextAyah: () -> Unit,
    onPlayPreviousAyah: () -> Unit,
    onUpdateFontSize: (Int) -> Unit,
    onSelectReciter: (String) -> Unit,
    onUpdateSpeed: (Float) -> Unit,
    onUpdateRepeatMode: (String) -> Unit,
    onOpenMadinaQuran: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val mushafScrollState = rememberScrollState()

    var displayMode by remember { mutableStateOf(QuranDisplayMode.MUSHAF) }
    var showReciterDialog by remember { mutableStateOf(false) }
    val currentReciter = remember(selectedReciterId) { RecitersData.getReciterById(selectedReciterId) }

    // Auto-scroll to currently playing ayah smoothly without jumping
    LaunchedEffect(playingAyahNumber, displayMode) {
        if (playingAyahNumber != null && displayMode == QuranDisplayMode.VERSES) {
            val index = ayahs.indexOfFirst { it.ayahNumber == playingAyahNumber }
            if (index >= 0) {
                coroutineScope.launch {
                    listState.animateScrollToItem(index + 1) // +1 for header
                }
            }
        }
    }

    // Save reading progress on enter
    LaunchedEffect(surah.number) {
        onSaveLastRead(surah.number, surah.nameAr, 1)
    }

    BackHandler { onBack() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "سورة ${surah.nameAr}",
                            style = MaterialTheme.typography.titleMedium,
                            fontFamily = CairoFontFamily,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${surah.revelationType} • ${surah.versesCount} آيات • ${currentReciter.nameAr}",
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = CairoFontFamily,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("btn_back_surah")) {
                        Icon(Icons.Default.ArrowForward, contentDescription = "رجوع")
                    }
                },
                actions = {
                    // Mode toggle button (Mushaf vs Verses)
                    IconButton(
                        onClick = {
                            displayMode = if (displayMode == QuranDisplayMode.MUSHAF) {
                                QuranDisplayMode.VERSES
                            } else {
                                QuranDisplayMode.MUSHAF
                            }
                        },
                        modifier = Modifier.testTag("btn_toggle_display_mode")
                    ) {
                        Icon(
                            if (displayMode == QuranDisplayMode.MUSHAF) Icons.Default.ViewAgenda else Icons.Default.AutoStories,
                            contentDescription = "تبديل نمط القراءة",
                            tint = IslamicGold
                        )
                    }

                    // Reciter change
                    IconButton(
                        onClick = { showReciterDialog = true },
                        modifier = Modifier.testTag("btn_change_reciter_top")
                    ) {
                        Icon(Icons.Default.RecordVoiceOver, contentDescription = "تغيير القارئ", tint = IslamicGreenPrimary)
                    }

                    // Font size adjuster
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { if (quranFontSize > 18) onUpdateFontSize(quranFontSize - 2) },
                            modifier = Modifier.size(34.dp)
                        ) {
                            Text("أ-", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        IconButton(
                            onClick = { if (quranFontSize < 42) onUpdateFontSize(quranFontSize + 2) },
                            modifier = Modifier.size(34.dp)
                        ) {
                            Text("أ+", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }

                    // Open official Madinah Quran app
                    IconButton(
                        onClick = onOpenMadinaQuran,
                        modifier = Modifier.testTag("btn_open_madina_from_reader")
                    ) {
                        Icon(Icons.Default.Launch, contentDescription = "مصحف المدينة", tint = IslamicGold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = {
            // Persistent / Foreground Quran Audio Bar
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "سورة ${surah.nameAr} • ${if (playingAyahNumber != null) "الآية $playingAyahNumber" else "تلاوة كاملة"}",
                                style = MaterialTheme.typography.titleSmall,
                                fontFamily = CairoFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "${currentReciter.nameAr} • تشغيل بالخلفية والإشعارات",
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = CairoFontFamily,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Display Mode Badge
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = IslamicGreenContainer,
                            modifier = Modifier.clickable {
                                displayMode = if (displayMode == QuranDisplayMode.MUSHAF) QuranDisplayMode.VERSES else QuranDisplayMode.MUSHAF
                            }
                        ) {
                            Text(
                                text = displayMode.titleAr,
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = CairoFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = IslamicGreenDark,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Player Controls Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Previous Surah / Ayah
                        IconButton(onClick = onPlayPreviousAyah) {
                            Icon(Icons.Default.SkipPrevious, contentDescription = "السابق", tint = IslamicGreenPrimary)
                        }

                        // Rewind 10s
                        IconButton(onClick = { QuranAudioService.sendControl(context, QuranAudioService.ACTION_REWIND_10) }) {
                            Icon(Icons.Default.Replay10, contentDescription = "رجوع 10 ثوانٍ", tint = IslamicGoldDark)
                        }

                        // Play / Pause
                        Surface(
                            shape = CircleShape,
                            color = IslamicGreenPrimary,
                            modifier = Modifier.size(46.dp)
                        ) {
                            IconButton(
                                onClick = {
                                    if (isAudioPlaying) {
                                        onPauseAudio()
                                    } else {
                                        val targetAyah = ayahs.find { it.ayahNumber == (playingAyahNumber ?: 1) } ?: ayahs.firstOrNull()
                                        targetAyah?.let { onPlayAyahAudio(it) }
                                    }
                                }
                            ) {
                                if (isAudioLoading) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                                } else {
                                    Icon(
                                        if (isAudioPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                        contentDescription = if (isAudioPlaying) "إيقاف مؤقت" else "تشغيل",
                                        tint = Color.White
                                    )
                                }
                            }
                        }

                        // Forward 10s
                        IconButton(onClick = { QuranAudioService.sendControl(context, QuranAudioService.ACTION_FORWARD_10) }) {
                            Icon(Icons.Default.Forward10, contentDescription = "تقديم 10 ثوانٍ", tint = IslamicGoldDark)
                        }

                        // Next Surah / Ayah
                        IconButton(onClick = onPlayNextAyah) {
                            Icon(Icons.Default.SkipNext, contentDescription = "التالي", tint = IslamicGreenPrimary)
                        }

                        // Repeat Mode
                        IconButton(
                            onClick = {
                                val nextMode = when (repeatMode) {
                                    "NONE" -> "ONE_AYAH"
                                    "ONE_AYAH" -> "SURAH"
                                    else -> "NONE"
                                }
                                onUpdateRepeatMode(nextMode)
                            }
                        ) {
                            Icon(
                                if (repeatMode == "ONE_AYAH") Icons.Default.RepeatOne else if (repeatMode == "SURAH") Icons.Default.Repeat else Icons.Default.East,
                                contentDescription = "التكرار",
                                tint = if (repeatMode != "NONE") IslamicGold else MaterialTheme.colorScheme.outline
                            )
                        }

                        // Speed Button
                        TextButton(
                            onClick = {
                                val nextSpeed = when (audioSpeed) {
                                    1.0f -> 1.25f
                                    1.25f -> 1.5f
                                    1.5f -> 0.75f
                                    else -> 1.0f
                                }
                                onUpdateSpeed(nextSpeed)
                            },
                            contentPadding = PaddingValues(horizontal = 4.dp)
                        ) {
                            Text("${audioSpeed}x", fontFamily = CairoFontFamily, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(QuranParchmentLight)
        ) {
            when (displayMode) {
                // ================= نمط المصحف المتصل السلس =================
                QuranDisplayMode.MUSHAF -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(mushafScrollState)
                            .padding(16.dp)
                    ) {
                        SurahHeaderFrame(surah = surah)

                        Spacer(modifier = Modifier.height(16.dp))

                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(2.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                // Full Continuous Mushaf Flow
                                val annotatedMushaf = buildAnnotatedString {
                                    ayahs.forEach { ayah ->
                                        val isPlaying = playingAyahNumber == ayah.ayahNumber
                                        val isBookmarked = bookmarks.any { it.surahNumber == surah.number && it.ayahNumber == ayah.ayahNumber }

                                        if (isPlaying) {
                                            pushStyle(SpanStyle(background = IslamicGoldContainer, color = TextPrimaryLight, fontWeight = FontWeight.Bold))
                                        } else if (isBookmarked) {
                                            pushStyle(SpanStyle(background = IslamicGreenContainer.copy(alpha = 0.5f)))
                                        }

                                        append(ayah.textUthmani)
                                        append(" ")

                                        // Decorative Ayah End Symbol ﴿١﴾
                                        pushStyle(SpanStyle(color = IslamicGoldDark, fontWeight = FontWeight.Bold, fontSize = (quranFontSize - 4).sp))
                                        append("﴿${ayah.ayahNumber}﴾")
                                        pop()
                                        append(" ")

                                        if (isPlaying || isBookmarked) {
                                            pop()
                                        }
                                    }
                                }

                                Text(
                                    text = annotatedMushaf,
                                    fontFamily = AmiriFontFamily,
                                    fontSize = quranFontSize.sp,
                                    lineHeight = (quranFontSize * 1.85f).sp,
                                    textAlign = TextAlign.Justify,
                                    color = TextPrimaryLight,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Quick Bookmark / Actions Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "اضغط على أي آية في نمط الآيات لتكرارها أو حفظها",
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = CairoFontFamily,
                                color = MaterialTheme.colorScheme.outline
                            )
                            Button(
                                onClick = { displayMode = QuranDisplayMode.VERSES },
                                colors = ButtonDefaults.buttonColors(containerColor = IslamicGreenPrimary),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.FormatListNumbered, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("عرض الآيات مفصلة", fontFamily = CairoFontFamily, fontSize = 12.sp)
                            }
                        }
                    }
                }

                // ================= نمط الآيات المتتابعة السلس =================
                QuranDisplayMode.VERSES -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("surah_reading_column"),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            SurahHeaderFrame(surah = surah)
                        }

                        itemsIndexed(ayahs, key = { _, item -> "${item.surahNumber}_${item.ayahNumber}" }) { _, ayah ->
                            val isBookmarked = bookmarks.any { it.surahNumber == surah.number && it.ayahNumber == ayah.ayahNumber }
                            val isCurrentlyPlaying = playingAyahNumber == ayah.ayahNumber

                            val targetCardColor = if (isCurrentlyPlaying) IslamicGoldContainer else Color.White
                            val animatedCardColor by animateColorAsState(targetValue = targetCardColor, animationSpec = tween(400))

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("ayah_item_${ayah.ayahNumber}"),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = animatedCardColor),
                                border = if (isCurrentlyPlaying) {
                                    CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.Brush.linearGradient(listOf(IslamicGold, IslamicGreenPrimary)))
                                } else null,
                                elevation = CardDefaults.cardElevation(if (isCurrentlyPlaying) 4.dp else 1.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    // Ayah Header Row
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            shape = CircleShape,
                                            color = if (isCurrentlyPlaying) IslamicGold else IslamicGreenContainer,
                                            modifier = Modifier.size(34.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(
                                                    text = "${ayah.ayahNumber}",
                                                    fontFamily = CairoFontFamily,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp,
                                                    color = if (isCurrentlyPlaying) Color.White else IslamicGreenDark
                                                )
                                            }
                                        }

                                        Row {
                                            // Play Audio Button
                                            IconButton(
                                                onClick = {
                                                    if (isCurrentlyPlaying && isAudioPlaying) {
                                                        onPauseAudio()
                                                    } else {
                                                        onPlayAyahAudio(ayah)
                                                    }
                                                },
                                                modifier = Modifier.size(36.dp)
                                            ) {
                                                Icon(
                                                    if (isCurrentlyPlaying && isAudioPlaying) Icons.Default.PauseCircle else Icons.Default.PlayCircle,
                                                    contentDescription = "استماع",
                                                    tint = if (isCurrentlyPlaying) IslamicGoldDark else IslamicGreenPrimary
                                                )
                                            }

                                            // Bookmark Button
                                            IconButton(
                                                onClick = {
                                                    onToggleBookmark(surah.number, surah.nameAr, ayah.ayahNumber, ayah.textUthmani)
                                                    onSaveLastRead(surah.number, surah.nameAr, ayah.ayahNumber)
                                                    Toast.makeText(
                                                        context,
                                                        if (isBookmarked) "تمت إزالة العلامة" else "تمت إضافة العلامة المرجعية",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                },
                                                modifier = Modifier.size(36.dp)
                                            ) {
                                                Icon(
                                                    if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                                    contentDescription = "علامة مرجعية",
                                                    tint = if (isBookmarked) IslamicGold else MaterialTheme.colorScheme.outline
                                                )
                                            }

                                            // Copy Verse
                                            IconButton(
                                                onClick = {
                                                    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                                    cm.setPrimaryClip(ClipData.newPlainText("Ayah", "${ayah.textUthmani} ﴿${ayah.ayahNumber}﴾ [سورة ${surah.nameAr}]"))
                                                    Toast.makeText(context, "تم نسخ الآية الكريمة", Toast.LENGTH_SHORT).show()
                                                },
                                                modifier = Modifier.size(36.dp)
                                            ) {
                                                Icon(Icons.Default.ContentCopy, contentDescription = "نسخ", modifier = Modifier.size(18.dp))
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Verse Text
                                    Text(
                                        text = "${ayah.textUthmani} ﴿${ayah.ayahNumber}﴾",
                                        fontFamily = AmiriFontFamily,
                                        fontSize = quranFontSize.sp,
                                        lineHeight = (quranFontSize * 1.8f).sp,
                                        color = TextPrimaryLight,
                                        textAlign = TextAlign.Start,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Reciter Selection Dialog
    if (showReciterDialog) {
        AlertDialog(
            onDismissRequest = { showReciterDialog = false },
            title = {
                Text(
                    text = "اختر القارئ المفضل",
                    fontFamily = CairoFontFamily,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(RecitersData.allReciters) { reciter ->
                        val isSelected = selectedReciterId == reciter.id
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) IslamicGreenContainer else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSelectReciter(reciter.id)
                                    showReciterDialog = false
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = reciter.nameAr,
                                        fontFamily = CairoFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) IslamicGreenDark else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = reciter.styleAr,
                                        fontFamily = CairoFontFamily,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (isSelected) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = "محدد", tint = IslamicGreenPrimary)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showReciterDialog = false }) {
                    Text("إغلاق", fontFamily = CairoFontFamily)
                }
            }
        )
    }
}

@Composable
fun SurahHeaderFrame(surah: SurahMeta) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = IslamicGoldContainer.copy(alpha = 0.5f)),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.Brush.linearGradient(listOf(IslamicGold, IslamicGreenLight))
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "سورة ${surah.nameAr}",
                style = MaterialTheme.typography.displayMedium,
                fontFamily = AmiriFontFamily,
                fontWeight = FontWeight.Bold,
                color = IslamicGoldDark
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "الجزء ${surah.juzNumber} • ${surah.revelationType} • آياتها ${surah.versesCount}",
                style = MaterialTheme.typography.bodySmall,
                fontFamily = CairoFontFamily,
                color = TextSecondaryLight
            )

            if (surah.number != 1 && surah.number != 9) {
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                    style = MaterialTheme.typography.titleLarge,
                    fontFamily = AmiriFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryLight,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
