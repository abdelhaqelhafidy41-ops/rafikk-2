package com.example.ui

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
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
import com.example.ui.theme.*

@Composable
fun AdhkarScreen(
    tasbeeh: TasbeehRecordEntity,
    onIncrementTasbeeh: () -> Unit,
    onResetTasbeeh: () -> Unit,
    onSetTasbeehDhikr: (String, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedCategory by remember { mutableStateOf<DhikrCategory?>(DhikrCategory.MORNING) }
    var isTasbeehTab by remember { mutableStateOf(false) }

    // Map to keep track of individual card counts
    val currentCounts = remember { mutableStateMapOf<Int, Int>() }

    fun vibrate() {
        try {
            val v = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v?.vibrate(VibrationEffect.createOneShot(40, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                v?.vibrate(40)
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
                    text = "حصن المسلم والأذكار",
                    style = MaterialTheme.typography.titleLarge,
                    fontFamily = CairoFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "﴿فَاذْكُرُونِي أَذْكُرْكُمْ وَاشْكُرُوا لِي وَلَا تَكْفُرُونِ﴾",
                    style = MaterialTheme.typography.bodyMedium,
                    fontFamily = AmiriFontFamily,
                    color = IslamicGoldDark
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Category Chips Scrollable Row
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        FilterChip(
                            selected = isTasbeehTab,
                            onClick = {
                                isTasbeehTab = true
                                selectedCategory = null
                            },
                            label = { Text("المسبحة الإلكترونية", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold) },
                            leadingIcon = {
                                Icon(Icons.Default.TouchApp, contentDescription = "المسبحة", modifier = Modifier.size(16.dp))
                            }
                        )
                    }

                    items(DhikrCategory.values()) { category ->
                        val isSelected = !isTasbeehTab && selectedCategory == category
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                isTasbeehTab = false
                                selectedCategory = category
                            },
                            label = { Text(category.titleAr, fontFamily = CairoFontFamily) }
                        )
                    }
                }
            }
        }

        if (isTasbeehTab) {
            // Interactive Electronic Tasbeeh Screen
            ElectronicTasbeehView(
                tasbeeh = tasbeeh,
                onTap = {
                    vibrate()
                    onIncrementTasbeeh()
                },
                onReset = onResetTasbeeh,
                onSelectDhikr = onSetTasbeehDhikr
            )
        } else {
            // Athkar Cards List
            val currentAthkarList = when (selectedCategory) {
                DhikrCategory.MORNING -> AdhkarData.morningAthkar
                DhikrCategory.EVENING -> AdhkarData.eveningAthkar
                DhikrCategory.AFTER_PRAYER -> AdhkarData.afterPrayerAthkar
                DhikrCategory.SLEEP -> AdhkarData.sleepAthkar
                DhikrCategory.WAKING -> AdhkarData.wakingAthkar
                null -> AdhkarData.morningAthkar
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("athkar_list"),
                contentPadding = PaddingValues(16.dp, 12.dp, 16.dp, 96.dp)
            ) {
                items(currentAthkarList) { dhikr ->
                    val doneCount = currentCounts[dhikr.id] ?: 0
                    val isCompleted = doneCount >= dhikr.count

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .testTag("dhikr_card_${dhikr.id}"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isCompleted) IslamicGreenContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Dhikr Text in Amiri font
                            Text(
                                text = dhikr.text,
                                style = MaterialTheme.typography.titleMedium.copy(lineHeight = 32.sp),
                                fontFamily = AmiriFontFamily,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Right,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Virtue & Source
                            if (dhikr.virtue.isNotBlank()) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = IslamicGoldContainer.copy(alpha = 0.5f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "الفضل: ${dhikr.virtue}",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontFamily = CairoFontFamily,
                                        color = IslamicGoldOnContainer,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "المصدر: ${dhikr.source}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontFamily = CairoFontFamily,
                                    color = MaterialTheme.colorScheme.outline
                                )

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (doneCount > 0) {
                                        IconButton(
                                            onClick = { currentCounts[dhikr.id] = 0 },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Refresh,
                                                contentDescription = "إعادة ضبط",
                                                tint = MaterialTheme.colorScheme.outline,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(6.dp))
                                    }

                                    // Tap to count button
                                    FilledTonalButton(
                                        onClick = {
                                            if (doneCount < dhikr.count) {
                                                vibrate()
                                                currentCounts[dhikr.id] = doneCount + 1
                                            }
                                        },
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.filledTonalButtonColors(
                                            containerColor = if (isCompleted) IslamicGreenPrimary else IslamicGoldContainer,
                                            contentColor = if (isCompleted) Color.White else IslamicGoldOnContainer
                                        ),
                                        modifier = Modifier.testTag("btn_count_${dhikr.id}")
                                    ) {
                                        if (isCompleted) {
                                            Icon(Icons.Default.Check, contentDescription = "تم", modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("تم بحمد الله", fontFamily = CairoFontFamily, fontSize = 12.sp)
                                        } else {
                                            Text(
                                                "$doneCount / ${dhikr.count}",
                                                fontFamily = CairoFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ElectronicTasbeehView(
    tasbeeh: TasbeehRecordEntity,
    onTap: () -> Unit,
    onReset: () -> Unit,
    onSelectDhikr: (String, Int) -> Unit
) {
    var showDhikrPicker by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .testTag("tasbeeh_view"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Active Dhikr Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showDhikrPicker = true },
            shape = RoundedCornerShape(18.dp),
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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "الذكر الحالي (انقر للتغيير):",
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = CairoFontFamily,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = tasbeeh.dhikrText,
                        style = MaterialTheme.typography.titleMedium,
                        fontFamily = AmiriFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Icon(Icons.Default.SwapHoriz, contentDescription = "تبديل", tint = IslamicGold)
            }
        }

        // Circular Interactive Big Tasbeeh Button
        Box(
            modifier = Modifier
                .size(240.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(IslamicGreenLight, IslamicGreenPrimary, IslamicGreenDark)
                    )
                )
                .clickable { onTap() }
                .testTag("tasbeeh_big_button"),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${tasbeeh.currentCount}",
                    style = MaterialTheme.typography.displayLarge.copy(fontSize = 58.sp),
                    fontFamily = CairoFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "الهدف: ${if (tasbeeh.targetCount > 0) tasbeeh.targetCount else "حر"}",
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = CairoFontFamily,
                    color = IslamicGoldLight
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "انقر للتسبيح",
                    style = MaterialTheme.typography.labelMedium,
                    fontFamily = CairoFontFamily,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }

        // Bottom Stats & Reset
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 80.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "مجموع التسبيحات الكلي:",
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = CairoFontFamily,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${tasbeeh.totalCount}",
                    style = MaterialTheme.typography.titleLarge,
                    fontFamily = CairoFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = IslamicGold
                )
            }

            OutlinedButton(
                onClick = onReset,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("btn_reset_tasbeeh")
            ) {
                Icon(Icons.Default.RestartAlt, contentDescription = "تصفير", modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("تصفير العداد", fontFamily = CairoFontFamily)
            }
        }
    }

    if (showDhikrPicker) {
        ModalBottomSheet(onDismissRequest = { showDhikrPicker = false }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "اختر صيغة الذكر والهدف",
                    style = MaterialTheme.typography.titleLarge,
                    fontFamily = CairoFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(14.dp))

                AdhkarData.tasbeehPresets.forEach { preset ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable {
                                onSelectDhikr(preset, 33)
                                showDhikrPicker = false
                            },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Text(
                            text = preset,
                            style = MaterialTheme.typography.bodyMedium,
                            fontFamily = AmiriFontFamily,
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                }
            }
        }
    }
}
