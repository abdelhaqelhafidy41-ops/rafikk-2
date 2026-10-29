package com.example.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.BookmarkEntity
import com.example.data.quran.AyahItem
import com.example.data.quran.QuranData
import com.example.data.quran.RecitersData
import com.example.data.quran.SurahMeta
import com.example.ui.theme.*

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
    BackHandler { onBack() }

    var showReciterDialog by remember { mutableStateOf(false) }
    val currentReciter = remember(selectedReciterId) { RecitersData.getReciterById(selectedReciterId) }

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
                    // Reciter change
                    IconButton(
                        onClick = { showReciterDialog = true },
                        modifier = Modifier.testTag("btn_change_reciter_top")
                    ) {
                        Icon(Icons.Default.RecordVoiceOver, contentDescription = "تغيير القارئ", tint = IslamicGreenPrimary)
                    }

                    // Font size adjuster
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 4.dp)
                    ) {
                        IconButton(
                            onClick = { if (quranFontSize > 18) onUpdateFontSize(quranFontSize - 2) },
                            modifier = Modifier.testTag("btn_font_decrease")
                        ) {
                            Text("أ-", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                        IconButton(
                            onClick = { if (quranFontSize < 40) onUpdateFontSize(quranFontSize + 2) },
                            modifier = Modifier.testTag("btn_font_increase")
                        ) {
                            Text("أ+", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                    }

                    // Open official Madinah Quran (com.madarsoft.quran)
                    IconButton(
                        onClick = onOpenMadinaQuran,
                        modifier = Modifier.testTag("btn_open_madina_from_reader")
                    ) {
                        Icon(
                            Icons.Default.Launch,
                            contentDescription = "فتح في تطبيق مصحف المدينة",
                            tint = IslamicGold
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = {
            // Enhanced Recitation Player Bar
            AnimatedVisibility(visible = playingAyahNumber != null) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    tonalElevation = 8.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (isAudioLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = IslamicGreenPrimary,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(
                                        Icons.Default.VolumeUp,
                                        contentDescription = "صوت",
                                        tint = IslamicGreenPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "${currentReciter.nameAr} - الآية $playingAyahNumber",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontFamily = CairoFontFamily,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "السرعة: ${audioSpeed}x • التكرار: ${if (repeatMode == "ONE_AYAH") "آية واحدة" else if (repeatMode == "SURAH") "السورة كاملة" else "تتابع"}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontFamily = CairoFontFamily,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            // Controls Row
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Previous Ayah
                                IconButton(onClick = onPlayPreviousAyah, modifier = Modifier.size(34.dp)) {
                                    Icon(Icons.Default.SkipPrevious, contentDescription = "الآية السابقة", tint = IslamicGreenPrimary)
                                }

                                // Play / Pause
                                IconButton(onClick = onPauseAudio, modifier = Modifier.size(38.dp)) {
                                    Icon(
                                        if (isAudioPlaying) Icons.Default.PauseCircle else Icons.Default.PlayCircle,
                                        contentDescription = if (isAudioPlaying) "إيقاف مؤقت" else "متابعة",
                                        tint = IslamicGreenPrimary,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }

                                // Next Ayah
                                IconButton(onClick = onPlayNextAyah, modifier = Modifier.size(34.dp)) {
                                    Icon(Icons.Default.SkipNext, contentDescription = "الآية التالية", tint = IslamicGreenPrimary)
                                }

                                // Repeat Mode toggle
                                IconButton(
                                    onClick = {
                                        val nextMode = when (repeatMode) {
                                            "NONE" -> "ONE_AYAH"
                                            "ONE_AYAH" -> "SURAH"
                                            else -> "NONE"
                                        }
                                        onUpdateRepeatMode(nextMode)
                                    },
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Icon(
                                        if (repeatMode == "ONE_AYAH") Icons.Default.RepeatOne else if (repeatMode == "SURAH") Icons.Default.Repeat else Icons.Default.East,
                                        contentDescription = "التكرار",
                                        tint = if (repeatMode != "NONE") IslamicGold else MaterialTheme.colorScheme.outline
                                    )
                                }

                                // Speed Toggle (1.0x -> 1.25x -> 1.5x -> 0.75x)
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
                                    Text("${audioSpeed}x", fontFamily = CairoFontFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                // Stop
                                IconButton(onClick = onStopAudio, modifier = Modifier.size(34.dp)) {
                                    Icon(Icons.Default.Close, contentDescription = "إغلاق المشغل", tint = IslamicRedHighlight)
                                }
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(QuranParchmentLight)
                .testTag("surah_reading_column"),
            contentPadding = PaddingValues(16.dp)
        ) {
            // Surah Decorative Header Frame
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = IslamicGoldContainer.copy(alpha = 0.5f)),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.Brush.linearGradient(listOf(IslamicGold, IslamicGreenLight)))
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

                        if (surah.number != 1 && surah.number != 9) { // Except Surah Al-Fatihah (where Basmalah is Verse 1) and Surah At-Tawbah (no Basmalah)
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

            // Ayahs
            items(ayahs) { ayah ->
                val isBookmarked = bookmarks.any { it.surahNumber == surah.number && it.ayahNumber == ayah.ayahNumber }
                val isCurrentlyPlaying = playingAyahNumber == ayah.ayahNumber

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .testTag("ayah_item_${ayah.ayahNumber}"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isCurrentlyPlaying) IslamicGoldContainer else Color.White
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Ayah Text
                        Text(
                            text = "${ayah.textUthmani} ﴿${ayah.ayahNumber}﴾",
                            fontSize = quranFontSize.sp,
                            lineHeight = (quranFontSize * 1.8).sp,
                            fontFamily = AmiriFontFamily,
                            color = TextPrimaryLight,
                            textAlign = TextAlign.Right,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Ayah Action Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "الآية ${ayah.ayahNumber}",
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = CairoFontFamily,
                                color = TextSecondaryLight
                            )

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Audio play
                                IconButton(
                                    onClick = { onPlayAyahAudio(ayah) },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        if (isCurrentlyPlaying && isAudioPlaying) Icons.Default.PauseCircle else Icons.Default.PlayCircle,
                                        contentDescription = "استماع للآية",
                                        tint = IslamicGreenPrimary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                // Bookmark
                                IconButton(
                                    onClick = {
                                        onToggleBookmark(surah.number, surah.nameAr, ayah.ayahNumber, ayah.textUthmani)
                                        Toast.makeText(
                                            context,
                                            if (isBookmarked) "تمت إزالة العلامة المرجعية" else "تم حفظ العلامة المرجعية",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                        contentDescription = "علامة مرجعية",
                                        tint = if (isBookmarked) IslamicGold else TextSecondaryLight,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                // Mark as last read
                                IconButton(
                                    onClick = {
                                        onSaveLastRead(surah.number, surah.nameAr, ayah.ayahNumber)
                                        Toast.makeText(context, "تم حفظ موضع القراءة الحالي", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Done,
                                        contentDescription = "حفظ كآخر موضع قراءة",
                                        tint = IslamicGreenPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                // Copy
                                IconButton(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val clip = ClipData.newPlainText("Quran Verse", "${ayah.textUthmani} [سورة ${surah.nameAr}: ${ayah.ayahNumber}]")
                                        clipboard.setPrimaryClip(clip)
                                        Toast.makeText(context, "تم نسخ الآية", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        Icons.Default.ContentCopy,
                                        contentDescription = "نسخ الآية",
                                        tint = TextSecondaryLight,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Reciter Selection Sheet
    if (showReciterDialog) {
        ModalBottomSheet(onDismissRequest = { showReciterDialog = false }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "اختر القارئ للتلاوة والاستماع",
                    style = MaterialTheme.typography.titleLarge,
                    fontFamily = CairoFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(12.dp))
                LazyColumn(modifier = Modifier.heightIn(max = 400.dp)) {
                    items(RecitersData.famousReciters) { reciter ->
                        val isSelected = reciter.id == selectedReciterId
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    onSelectReciter(reciter.id)
                                    showReciterDialog = false
                                },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) IslamicGreenContainer else MaterialTheme.colorScheme.surface
                            )
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
                                        style = MaterialTheme.typography.titleSmall,
                                        fontFamily = CairoFontFamily,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${reciter.styleAr} • ${reciter.descriptionAr}",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontFamily = CairoFontFamily,
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
            }
        }
    }
}
