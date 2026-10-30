package com.example.friday

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.widget.FridayAppWidgetProvider
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FridayScreen(
    onBack: () -> Unit,
    onOpenKahf: () -> Unit,
    onPlayKahfAudio: () -> Unit
) {
    val context = LocalContext.current
    BackHandler { onBack() }

    var isKahfRead by remember { mutableStateOf(FridayPreferences.isKahfReadThisWeek(context)) }
    var salawatCount by remember { mutableIntStateOf(FridayPreferences.getSalawatCount(context)) }
    var salawatGoal by remember { mutableIntStateOf(FridayPreferences.getSalawatGoal(context)) }
    var checkedSunan by remember { mutableStateOf(FridayPreferences.getCheckedSunan(context)) }
    var favoriteAdhkar by remember { mutableStateOf(FridayPreferences.getFavoriteAdhkar(context)) }
    var completedAdhkar by remember { mutableStateOf(FridayPreferences.getCompletedAdhkar(context)) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    val isFriday = remember { FridayPreferences.isTodayFriday() }
    val daysUntilFriday = remember { FridayPreferences.getDaysUntilNextFriday() }

    val dateFormat = remember { SimpleDateFormat("EEEE، d MMMM yyyy", Locale("ar")) }
    val todayDateStr = remember { dateFormat.format(Date()) }

    fun vibrateShort() {
        if (!FridayPreferences.isVibrationEnabled(context)) return
        try {
            val v = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v?.vibrate(VibrationEffect.createOneShot(45, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                v?.vibrate(45)
            }
        } catch (_: Exception) {}
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "يوم الجمعة المبارك",
                        fontFamily = CairoFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("btn_back_friday")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "رجوع")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showSettingsDialog = true },
                        modifier = Modifier.testTag("btn_friday_settings")
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = "إعدادات الجمعة")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 10.dp, bottom = 32.dp)
        ) {
            // 1. Hero Header Banner
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    colors = if (isFriday) {
                                        listOf(IslamicGreenPrimary, IslamicGreenDark)
                                    } else {
                                        listOf(IslamicGoldContainer, IslamicGreenDark)
                                    }
                                )
                            )
                            .padding(20.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isFriday) "🕌 اليوم هو يوم الجمعة" else "🕌 استعد ليوم الجمعة",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontFamily = CairoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = IslamicGold.copy(alpha = 0.25f)
                                ) {
                                    Text(
                                        text = if (isFriday) "يوم مبارك" else "متبقي $daysUntilFriday أيام",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontFamily = CairoFontFamily,
                                        color = IslamicGold,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = todayDateStr,
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = CairoFontFamily,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "«خَيْرُ يَوْمٍ طَلَعَتْ عَلَيْهِ الشَّمْسُ يَوْمُ الْجُمُعَةِ، فِيهِ خُلِقَ آدَمُ، وَفِيهِ أُدْخِلَ الْجَنَّةَ، وَفِيهِ أُخْرِجَ مِنْهَا» [صحيح مسلم]",
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = CairoFontFamily,
                                color = Color.White.copy(alpha = 0.95f),
                                lineHeight = 20.sp
                            )
                        }
                    }
                }
            }

            // 2. Surah Al-Kahf Card
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.MenuBook,
                                    contentDescription = null,
                                    tint = IslamicGreenPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "قراءة سورة الكهف",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontFamily = CairoFontFamily,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isKahfRead) IslamicGreenContainer else IslamicGoldContainer
                            ) {
                                Text(
                                    text = if (isKahfRead) "تمت القراءة ✓" else "لم تُقرأ بعد",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontFamily = CairoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isKahfRead) IslamicGreenPrimary else IslamicGoldOnContainer,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "«مَنْ قَرَأَ سُورَةَ الْكَهْفِ فِي يَوْمِ الْجُمُعَةِ أَضَاءَ لَهُ مِنَ النُّورِ مَا بَيْنَ الْجُمُعَتَيْنِ» [رواه الحاكم، صحيح]",
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = CairoFontFamily,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 20.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = onOpenKahf,
                                colors = ButtonDefaults.buttonColors(containerColor = IslamicGreenPrimary),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .testTag("btn_read_kahf_now")
                            ) {
                                Icon(Icons.Default.AutoStories, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("قراءة الآن", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold)
                            }

                            FilledTonalButton(
                                onClick = onPlayKahfAudio,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .height(44.dp)
                                    .testTag("btn_play_kahf_audio")
                            ) {
                                Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("استماع", fontFamily = CairoFontFamily)
                            }

                            IconButton(
                                onClick = {
                                    vibrateShort()
                                    isKahfRead = !isKahfRead
                                    FridayPreferences.setKahfReadThisWeek(context, isKahfRead)
                                    FridayAppWidgetProvider.updateAllWidgets(context)
                                    Toast.makeText(
                                        context,
                                        if (isKahfRead) "تقبل الله طاعتكم! تم تحديد سورة الكهف كمقروءة." else "تم إلغاء التحديد.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                },
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Icon(
                                    if (isKahfRead) Icons.Default.CheckCircle else Icons.Default.CheckCircleOutline,
                                    contentDescription = "تحديد كـ مقروءة",
                                    tint = if (isKahfRead) IslamicGreenPrimary else MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }
                }
            }

            // 3. Salawat on the Prophet Counter Card
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "الصلاة على النبي ﷺ",
                                style = MaterialTheme.typography.titleMedium,
                                fontFamily = CairoFontFamily,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "الهدف: $salawatGoal",
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = CairoFontFamily,
                                color = IslamicGoldDark
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Circular Tap Button
                        Surface(
                            shape = CircleShape,
                            color = IslamicGreenContainer,
                            shadowElevation = 6.dp,
                            modifier = Modifier
                                .size(140.dp)
                                .clip(CircleShape)
                                .clickable {
                                    vibrateShort()
                                    salawatCount = FridayPreferences.incrementSalawat(context)
                                    FridayAppWidgetProvider.updateAllWidgets(context)
                                    if (salawatCount == salawatGoal) {
                                        Toast.makeText(
                                            context,
                                            "ما شاء الله! بلغت هدفك اليوم في الصلاة على الحبيب ﷺ.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                                .testTag("btn_increment_salawat")
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "$salawatCount",
                                    style = MaterialTheme.typography.headlineLarge,
                                    fontFamily = CairoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = IslamicGreenPrimary
                                )
                                Text(
                                    text = "اضغط للتسبيح",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontFamily = CairoFontFamily,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Progress Bar toward goal
                        val progress = if (salawatGoal > 0) (salawatCount.toFloat() / salawatGoal.toFloat()).coerceIn(0f, 1f) else 0f
                        LinearProgressIndicator(
                            progress = { progress },
                            color = IslamicGold,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(CircleShape)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(
                                onClick = {
                                    salawatCount = 0
                                    FridayPreferences.resetSalawat(context)
                                    FridayAppWidgetProvider.updateAllWidgets(context)
                                }
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("تصفير العداد", fontFamily = CairoFontFamily)
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf(100, 300, 500, 1000).forEach { goal ->
                                    FilterChip(
                                        selected = salawatGoal == goal,
                                        onClick = {
                                            salawatGoal = goal
                                            FridayPreferences.setSalawatGoal(context, goal)
                                        },
                                        label = { Text("$goal", fontSize = 11.sp, fontFamily = CairoFontFamily) }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 4. Hour of Response (ساعة الإجابة)
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.AccessTimeFilled,
                                contentDescription = null,
                                tint = IslamicGold,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "تحري ساعة الإجابة يوم الجمعة",
                                style = MaterialTheme.typography.titleMedium,
                                fontFamily = CairoFontFamily,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = FridayData.HOUR_OF_RESPONSE_EXPLANATION,
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = CairoFontFamily,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 20.sp
                        )
                    }
                }
            }

            // 5. Sunan Checklist Section Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "سنن وأعمال مستحبة يوم الجمعة",
                        style = MaterialTheme.typography.titleMedium,
                        fontFamily = CairoFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${checkedSunan.size}/${FridayData.sunanList.size} مكتمل",
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = CairoFontFamily,
                        color = IslamicGreenPrimary
                    )
                }
            }

            // Sunan Checklist Items
            items(FridayData.sunanList, key = { it.id }) { sunnah ->
                val isChecked = checkedSunan.contains(sunnah.id)
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isChecked) IslamicGreenContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            vibrateShort()
                            FridayPreferences.toggleSunnahChecked(context, sunnah.id)
                            checkedSunan = FridayPreferences.getCheckedSunan(context)
                        }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isChecked,
                            onCheckedChange = {
                                vibrateShort()
                                FridayPreferences.toggleSunnahChecked(context, sunnah.id)
                                checkedSunan = FridayPreferences.getCheckedSunan(context)
                            },
                            colors = CheckboxDefaults.colors(checkedColor = IslamicGreenPrimary)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = sunnah.title,
                                style = MaterialTheme.typography.titleSmall,
                                fontFamily = CairoFontFamily,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = sunnah.description,
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = CairoFontFamily,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = sunnah.evidence,
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = CairoFontFamily,
                                color = IslamicGoldDark,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            // 6. Adhkar Section Header
            item {
                Text(
                    text = "أدعية وأذكار مختارة للجمعة",
                    style = MaterialTheme.typography.titleMedium,
                    fontFamily = CairoFontFamily,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // Adhkar Cards
            items(FridayData.adhkarList, key = { it.id }) { dhikr ->
                val isFav = favoriteAdhkar.contains(dhikr.id)
                val isCompleted = completedAdhkar.contains(dhikr.id)

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isCompleted) IslamicGreenContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = dhikr.title,
                                style = MaterialTheme.typography.titleSmall,
                                fontFamily = CairoFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = IslamicGreenPrimary
                            )
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    text = "${dhikr.grade} • ${dhikr.source}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontFamily = CairoFontFamily,
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = dhikr.text,
                            style = MaterialTheme.typography.bodyMedium,
                            fontFamily = CairoFontFamily,
                            lineHeight = 24.sp
                        )

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "الفضل: ${dhikr.virtue}",
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = CairoFontFamily,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                // Copy Button
                                IconButton(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        clipboard.setPrimaryClip(ClipData.newPlainText("Dhikr", dhikr.text))
                                        Toast.makeText(context, "تم نسخ الذكر", Toast.LENGTH_SHORT).show()
                                    }
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "نسخ", modifier = Modifier.size(18.dp))
                                }

                                // Share Button
                                IconButton(
                                    onClick = {
                                        val sendIntent = Intent().apply {
                                            action = Intent.ACTION_SEND
                                            putExtra(Intent.EXTRA_TEXT, "${dhikr.title}\n\n${dhikr.text}\n\n[المصدر: ${dhikr.source}]")
                                            type = "text/plain"
                                        }
                                        context.startActivity(Intent.createChooser(sendIntent, "مشاركة الذكر"))
                                    }
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = "مشاركة", modifier = Modifier.size(18.dp))
                                }

                                // Favorite Button
                                IconButton(
                                    onClick = {
                                        FridayPreferences.toggleFavoriteDhikr(context, dhikr.id)
                                        favoriteAdhkar = FridayPreferences.getFavoriteAdhkar(context)
                                    }
                                ) {
                                    Icon(
                                        if (isFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                        contentDescription = "المفضلة",
                                        tint = if (isFav) Color.Red else MaterialTheme.colorScheme.outline,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            // Done Button
                            FilterChip(
                                selected = isCompleted,
                                onClick = {
                                    vibrateShort()
                                    FridayPreferences.toggleCompletedDhikr(context, dhikr.id)
                                    completedAdhkar = FridayPreferences.getCompletedAdhkar(context)
                                },
                                label = {
                                    Text(
                                        if (isCompleted) "تمت القراءة ✓" else "قراءة (${dhikr.repetitionTarget}x)",
                                        fontFamily = CairoFontFamily,
                                        fontSize = 11.sp
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Friday Settings Dialog
    if (showSettingsDialog) {
        FridaySettingsDialog(
            onDismiss = { showSettingsDialog = false },
            onDataCleared = {
                isKahfRead = false
                salawatCount = 0
                checkedSunan = emptySet()
                favoriteAdhkar = emptySet()
                completedAdhkar = emptySet()
                FridayAppWidgetProvider.updateAllWidgets(context)
            }
        )
    }
}

/**
 * Dedicated settings dialog for Friday section preferences.
 */
@Composable
fun FridaySettingsDialog(
    onDismiss: () -> Unit,
    onDataCleared: () -> Unit = {}
) {
    val context = LocalContext.current

    var kahfReminder by remember { mutableStateOf(FridayPreferences.isKahfReminderEnabled(context)) }
    var salawatReminder by remember { mutableStateOf(FridayPreferences.isSalawatReminderEnabled(context)) }
    var duaReminder by remember { mutableStateOf(FridayPreferences.isDuaReminderEnabled(context)) }
    var jumuahPrepReminder by remember { mutableStateOf(FridayPreferences.isJumuahPrepReminderEnabled(context)) }
    var soundEnabled by remember { mutableStateOf(FridayPreferences.isSoundEnabled(context)) }
    var vibrationEnabled by remember { mutableStateOf(FridayPreferences.isVibrationEnabled(context)) }
    var showSalawatOnWidget by remember { mutableStateOf(FridayPreferences.isShowSalawatOnWidget(context)) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("إعدادات وتذكيرات يوم الجمعة", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("تذكير سورة الكهف (أسبوعياً)", fontFamily = CairoFontFamily, fontSize = 13.sp)
                    Switch(
                        checked = kahfReminder,
                        onCheckedChange = {
                            kahfReminder = it
                            FridayPreferences.setKahfReminderEnabled(context, it)
                            FridayReminderManager.scheduleAllFridayReminders(context)
                        }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("تذكير الصلاة على النبي ﷺ", fontFamily = CairoFontFamily, fontSize = 13.sp)
                    Switch(
                        checked = salawatReminder,
                        onCheckedChange = {
                            salawatReminder = it
                            FridayPreferences.setSalawatReminderEnabled(context, it)
                            FridayReminderManager.scheduleAllFridayReminders(context)
                        }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("تذكير ساعة الإجابة (قبل المغرب)", fontFamily = CairoFontFamily, fontSize = 13.sp)
                    Switch(
                        checked = duaReminder,
                        onCheckedChange = {
                            duaReminder = it
                            FridayPreferences.setDuaReminderEnabled(context, it)
                            FridayReminderManager.scheduleAllFridayReminders(context)
                        }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("تذكير الاستعداد لصلاة الجمعة", fontFamily = CairoFontFamily, fontSize = 13.sp)
                    Switch(
                        checked = jumuahPrepReminder,
                        onCheckedChange = {
                            jumuahPrepReminder = it
                            FridayPreferences.setJumuahPrepReminderEnabled(context, it)
                            FridayReminderManager.scheduleAllFridayReminders(context)
                        }
                    )
                }

                HorizontalDivider()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("صوت التذكير", fontFamily = CairoFontFamily, fontSize = 13.sp)
                    Switch(
                        checked = soundEnabled,
                        onCheckedChange = {
                            soundEnabled = it
                            FridayPreferences.setSoundEnabled(context, it)
                        }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("الاهتزاز", fontFamily = CairoFontFamily, fontSize = 13.sp)
                    Switch(
                        checked = vibrationEnabled,
                        onCheckedChange = {
                            vibrationEnabled = it
                            FridayPreferences.setVibrationEnabled(context, it)
                        }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("عرض عداد الصلاة على النبي في Widget", fontFamily = CairoFontFamily, fontSize = 13.sp)
                    Switch(
                        checked = showSalawatOnWidget,
                        onCheckedChange = {
                            showSalawatOnWidget = it
                            FridayPreferences.setShowSalawatOnWidget(context, it)
                            FridayAppWidgetProvider.updateAllWidgets(context)
                        }
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                OutlinedButton(
                    onClick = {
                        FridayPreferences.resetSunanProgress(context)
                        FridayPreferences.resetSalawat(context)
                        FridayPreferences.setKahfReadThisWeek(context, false)
                        onDataCleared()
                        Toast.makeText(context, "تمت إعادة ضبط تقدم يوم الجمعة لهذا الأسبوع.", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("إعادة ضبط تقدم أعمال هذا الأسبوع", fontFamily = CairoFontFamily, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("تم", fontFamily = CairoFontFamily)
            }
        }
    )
}
