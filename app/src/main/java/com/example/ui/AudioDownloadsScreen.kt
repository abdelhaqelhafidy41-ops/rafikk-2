package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.AudioDownloadManager
import com.example.audio.SurahDownloadStatus
import com.example.data.quran.QuranData
import com.example.data.quran.ReciterInfo
import com.example.data.quran.RecitersData
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioDownloadsScreen(
    downloadManager: AudioDownloadManager,
    selectedReciterId: String,
    downloadOverWifiOnly: Boolean,
    onSelectReciter: (String) -> Unit,
    onToggleWifiOnly: (Boolean) -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val downloadStatuses by downloadManager.downloadStatusMap.collectAsState()
    var currentReciterId by remember { mutableStateOf(selectedReciterId) }
    var showReciterSheet by remember { mutableStateOf(false) }
    var showDeleteAllDialog by remember { mutableStateOf(false) }

    val availableStorageMb = remember {
        (downloadManager.getAvailableStorageBytes() / (1024 * 1024)).coerceAtLeast(0)
    }

    val currentReciter = remember(currentReciterId) {
        RecitersData.getReciterById(currentReciterId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "تحميل التلاوات وإدارتها",
                        fontFamily = CairoFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("btn_back_downloads")) {
                        Icon(Icons.Default.ArrowForward, contentDescription = "رجوع")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showDeleteAllDialog = true },
                        modifier = Modifier.testTag("btn_delete_all_downloads")
                    ) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = "حذف الكل", tint = IslamicRedHighlight)
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .testTag("downloads_column"),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // Reciter Selection Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clickable { showReciterSheet = true },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                                text = "القارئ الحالي للتحميل:",
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = CairoFontFamily,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${currentReciter.nameAr} (${currentReciter.styleAr})",
                                style = MaterialTheme.typography.titleMedium,
                                fontFamily = CairoFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Button(onClick = { showReciterSheet = true }, shape = RoundedCornerShape(10.dp)) {
                            Text("تغيير القارئ", fontFamily = CairoFontFamily, fontSize = 12.sp)
                        }
                    }
                }
            }

            // Storage and Wi-Fi Controls Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Storage, contentDescription = "مساحة التخزين", tint = IslamicGold)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "المساحة المتوفرة على هاتفك:",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontFamily = CairoFontFamily
                                )
                            }
                            Text(
                                text = "$availableStorageMb ميجابايت",
                                style = MaterialTheme.typography.titleSmall,
                                fontFamily = CairoFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = IslamicGold
                            )
                        }

                        Divider(modifier = Modifier.padding(vertical = 10.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "التحميل عبر شبكة Wi-Fi فقط",
                                    fontFamily = CairoFontFamily,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "لمنع استهلاك باقة بيانات الهاتف تلقائيًا",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontFamily = CairoFontFamily,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Switch(
                                checked = downloadOverWifiOnly,
                                onCheckedChange = onToggleWifiOnly,
                                modifier = Modifier.testTag("switch_wifi_only")
                            )
                        }
                    }
                }
            }

            // Batch Download Buttons
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            // Download Juz Amma (Surahs 78..114)
                            for (s in 78..114) {
                                downloadManager.downloadSurah(currentReciterId, s, downloadOverWifiOnly)
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_download_juz_amma"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("تحميل جزء عمّ", fontFamily = CairoFontFamily, fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            // Download all 114 Surahs
                            for (s in 1..114) {
                                downloadManager.downloadSurah(currentReciterId, s, downloadOverWifiOnly)
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_download_full_quran"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("تحميل المصحف كاملًا", fontFamily = CairoFontFamily, fontSize = 12.sp)
                    }
                }
            }

            item {
                Text(
                    text = "قائمة السور وحالة التحميل:",
                    style = MaterialTheme.typography.titleMedium,
                    fontFamily = CairoFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // Surah List
            items(QuranData.allSurahs) { surah ->
                val key = "${currentReciterId}_${surah.number}"
                val status = downloadStatuses[key] ?: run {
                    if (downloadManager.isSurahFullyDownloaded(currentReciterId, surah.number)) {
                        val size = downloadManager.getSurahSizeBytes(currentReciterId, surah.number)
                        SurahDownloadStatus.Downloaded(size)
                    } else {
                        SurahDownloadStatus.NotDownloaded
                    }
                }

                val approxMb = ((surah.versesCount * 45) / 1024f).coerceAtLeast(0.3f)
                val approxFormatted = String.format("%.1f ميغابايت", approxMb)

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .testTag("download_surah_item_${surah.number}"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "سورة ${surah.nameAr}",
                                style = MaterialTheme.typography.titleSmall,
                                fontFamily = CairoFontFamily,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${surah.versesCount} آية • الحجم التقديري: $approxFormatted",
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = CairoFontFamily,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        when (status) {
                            is SurahDownloadStatus.Downloaded -> {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = IslamicGreenContainer
                                    ) {
                                        Text(
                                            text = "محملة ✓",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontFamily = CairoFontFamily,
                                            color = IslamicGreenOnContainer,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                    IconButton(
                                        onClick = { downloadManager.deleteDownloadedSurah(currentReciterId, surah.number) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "حذف", tint = IslamicRedHighlight, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                            is SurahDownloadStatus.Downloading -> {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = IslamicGold,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${status.progressPercent}%",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontFamily = CairoFontFamily,
                                        fontWeight = FontWeight.Bold
                                    )
                                    IconButton(
                                        onClick = { downloadManager.cancelDownload(currentReciterId, surah.number) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = "إلغاء", modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                            is SurahDownloadStatus.Failed -> {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "فشل",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = IslamicRedHighlight,
                                        fontFamily = CairoFontFamily
                                    )
                                    IconButton(
                                        onClick = { downloadManager.downloadSurah(currentReciterId, surah.number, downloadOverWifiOnly) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Refresh, contentDescription = "إعادة المحاولة", tint = IslamicGreenPrimary)
                                    }
                                }
                            }
                            is SurahDownloadStatus.NotDownloaded -> {
                                FilledTonalButton(
                                    onClick = { downloadManager.downloadSurah(currentReciterId, surah.number, downloadOverWifiOnly) },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Icon(Icons.Default.Download, contentDescription = "تحميل", modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("تحميل", fontFamily = CairoFontFamily, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Reciter Selector Sheet
    if (showReciterSheet) {
        ModalBottomSheet(onDismissRequest = { showReciterSheet = false }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "اختر القارئ للتحميل والتلاوة",
                    style = MaterialTheme.typography.titleLarge,
                    fontFamily = CairoFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(12.dp))
                LazyColumn(modifier = Modifier.heightIn(max = 420.dp)) {
                    items(RecitersData.famousReciters) { reciter ->
                        val isSelected = reciter.id == currentReciterId
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    currentReciterId = reciter.id
                                    onSelectReciter(reciter.id)
                                    showReciterSheet = false
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

    // Delete All Confirmation Dialog
    if (showDeleteAllDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAllDialog = false },
            title = { Text("حذف جميع التلاوات المحملة؟", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold) },
            text = { Text("سيتم مسح جميع الملفات الصوتية المحملة وتوفير مساحة التخزين على جهازك.", fontFamily = CairoFontFamily) },
            confirmButton = {
                Button(
                    onClick = {
                        downloadManager.deleteAllDownloads()
                        showDeleteAllDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = IslamicRedHighlight)
                ) {
                    Text("حذف الكل", fontFamily = CairoFontFamily)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAllDialog = false }) {
                    Text("إلغاء", fontFamily = CairoFontFamily)
                }
            }
        )
    }
}
