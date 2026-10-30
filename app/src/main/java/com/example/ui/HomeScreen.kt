package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.adhkar.DailyInspiration
import com.example.data.database.AppSettingsEntity
import com.example.data.database.ReadingProgressEntity
import com.example.data.database.ReminderEntity
import com.example.data.quran.QuranData
import com.example.prayer.PrayerTimesResult
import com.example.ui.theme.*

@Composable
fun HomeScreen(
    settings: AppSettingsEntity,
    prayerTimes: PrayerTimesResult,
    todayHijri: String,
    dailyInspiration: DailyInspiration,
    readingProgress: ReadingProgressEntity?,
    reminders: List<ReminderEntity>,
    onToggleMuteAll: () -> Unit,
    onToggleReminder: (Int, Boolean) -> Unit,
    onEditReminder: (ReminderEntity) -> Unit,
    onAddReminder: () -> Unit,
    onNavigateToQuran: () -> Unit,
    onNavigateToPrayer: () -> Unit,
    onNavigateToAdhkar: () -> Unit,
    onNavigateToQibla: () -> Unit,
    onOpenMadinaQuran: () -> Unit,
    onNavigateToFriday: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("home_screen_column"),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        // Hero Islamic Banner
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_home_banner),
                    contentDescription = "بانر رفيق المسلم",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Dark gradient overlay for clear contrast
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color(0x990A2E23),
                                    Color(0xE6071F17)
                                )
                            )
                        )
                )

                // Hero content
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Top row: Location & Hijri Date
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0x33000000),
                            tonalElevation = 2.dp
                        ) {
                            Row(
                                modifier = Modifier
                                    .clickable { onNavigateToPrayer() }
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.LocationOn,
                                    contentDescription = "المدينة",
                                    tint = IslamicGoldLight,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = settings.cityName,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White,
                                    fontFamily = CairoFontFamily
                                )
                            }
                        }

                        Text(
                            text = todayHijri,
                            style = MaterialTheme.typography.labelMedium,
                            color = IslamicGoldLight,
                            fontFamily = CairoFontFamily,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Center & Bottom: Next Prayer countdown
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "الصلاة القادمة",
                            style = MaterialTheme.typography.titleSmall,
                            color = Color(0xFFD4E6DF),
                            fontFamily = CairoFontFamily
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = prayerTimes.nextPrayerName,
                                style = MaterialTheme.typography.displayMedium,
                                color = Color.White,
                                fontFamily = CairoFontFamily,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = IslamicGold
                            ) {
                                Text(
                                    text = prayerTimes.nextPrayerTime,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color(0xFF2B2002),
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = CairoFontFamily,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0x4D000000)
                        ) {
                            Text(
                                text = "متبقي عليها: ${prayerTimes.remainingFormatted}",
                                style = MaterialTheme.typography.bodySmall,
                                color = IslamicGoldLight,
                                fontFamily = CairoFontFamily,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        // Daily Five Prayers Strip
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .clickable { onNavigateToPrayer() }
                    .testTag("prayer_strip_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "مواقيت الصلوات الخمس",
                            style = MaterialTheme.typography.titleMedium,
                            fontFamily = CairoFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "عرض الكل",
                            style = MaterialTheme.typography.labelMedium,
                            color = IslamicGold,
                            fontFamily = CairoFontFamily
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val prayers = listOf(
                        Triple("الفجر", prayerTimes.fajr, prayerTimes.nextPrayerName == "الفجر"),
                        Triple("الظهر", prayerTimes.dhuhr, prayerTimes.nextPrayerName == "الظهر"),
                        Triple("العصر", prayerTimes.asr, prayerTimes.nextPrayerName == "العصر"),
                        Triple("المغرب", prayerTimes.maghrib, prayerTimes.nextPrayerName == "المغرب"),
                        Triple("العشاء", prayerTimes.isha, prayerTimes.nextPrayerName == "العشاء")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        prayers.forEach { (name, time, isNext) ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isNext) IslamicGoldContainer else Color.Transparent
                                    )
                                    .padding(vertical = 8.dp, horizontal = 6.dp)
                            ) {
                                Text(
                                    text = name,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontFamily = CairoFontFamily,
                                    fontWeight = if (isNext) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isNext) IslamicGoldOnContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = time.replace(" ص", "").replace(" م", ""),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontFamily = CairoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isNext) IslamicGoldOnContainer else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }

        // Special Friday Section Card
        item {
            val isFriday = remember { com.example.friday.FridayPreferences.isTodayFriday() }
            val isKahfRead = remember { com.example.friday.FridayPreferences.isKahfReadThisWeek(context) }
            val salawatCount = remember { com.example.friday.FridayPreferences.getSalawatCount(context) }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .clickable { onNavigateToFriday() }
                    .testTag("friday_section_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isFriday) IslamicGreenContainer else MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (isFriday) IslamicGold else IslamicGreenPrimary.copy(alpha = 0.15f),
                            modifier = Modifier.size(46.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "🕌",
                                    fontSize = 20.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (isFriday) "يوم الجمعة المبارك" else "قسم يوم الجمعة",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontFamily = CairoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isFriday) IslamicGreenDark else MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                if (isFriday) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = IslamicGoldContainer
                                    ) {
                                        Text(
                                            text = "اليوم",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontFamily = CairoFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            color = IslamicGoldOnContainer,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (isFriday) {
                                    if (isKahfRead) "✓ سورة الكهف مقروءة • الصلاة على النبي: $salawatCount ﷺ" else "📖 تذكير سورة الكهف • سنن وأدعية الجمعة"
                                } else {
                                    "سنن وأذكار، سورة الكهف، وعداد الصلاة على النبي ﷺ"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = CairoFontFamily,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "فتح قسم الجمعة",
                        tint = if (isFriday) IslamicGreenPrimary else MaterialTheme.colorScheme.outline
                    )
                }
            }
        }

        // Quick Reminders Mute/Active Toggle Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .testTag("quick_reminders_toggle_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (settings.allRemindersMuted) MaterialTheme.colorScheme.surfaceVariant else IslamicGreenContainer.copy(alpha = 0.6f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            if (settings.allRemindersMuted) Icons.Default.NotificationsOff else Icons.Default.NotificationsActive,
                            contentDescription = "حالة التذكيرات",
                            tint = if (settings.allRemindersMuted) MaterialTheme.colorScheme.outline else IslamicGreenPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (settings.allRemindersMuted) "التذكيرات موقوفة حاليًا" else "التذكيرات الإسلامية مفعّلة",
                                style = MaterialTheme.typography.titleSmall,
                                fontFamily = CairoFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (settings.allRemindersMuted) "انقر لتشغيل تذكيرات الاستغفار والقرآن" else "تصلك التذكيرات في أوقاتها المحددة",
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = CairoFontFamily,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Switch(
                        checked = !settings.allRemindersMuted,
                        onCheckedChange = { onToggleMuteAll() },
                        modifier = Modifier.testTag("master_reminder_switch")
                    )
                }
            }
        }

        // Daily Quran Portion Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .testTag("quran_portion_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.MenuBook,
                                contentDescription = "المصحف",
                                tint = IslamicGold,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "ورد القرآن الكريم",
                                style = MaterialTheme.typography.titleMedium,
                                fontFamily = CairoFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        // Madinah Quran App external link button
                        TextButton(
                            onClick = onOpenMadinaQuran,
                            modifier = Modifier.testTag("btn_open_madina_quran_home")
                        ) {
                            Icon(
                                Icons.Default.Launch,
                                contentDescription = "فتح مصحف المدينة",
                                modifier = Modifier.size(16.dp),
                                tint = IslamicGold
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "مصحف المدينة",
                                style = MaterialTheme.typography.labelSmall,
                                color = IslamicGold,
                                fontFamily = CairoFontFamily
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    val lastReadText = if (readingProgress != null) {
                        "آخر موضع: سورة ${readingProgress.surahName} (آية ${readingProgress.ayahNumber})"
                    } else {
                        "ابدأ قراءة ورد اليوم من سورة الفاتحة أو البقرة"
                    }

                    Text(
                        text = lastReadText,
                        style = MaterialTheme.typography.bodyMedium,
                        fontFamily = CairoFontFamily,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onNavigateToQuran,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_resume_quran"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = IslamicGreenPrimary)
                        ) {
                            Text(
                                text = "متابعة القراءة",
                                fontFamily = CairoFontFamily,
                                color = Color.White
                            )
                        }

                        OutlinedButton(
                            onClick = onOpenMadinaQuran,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("btn_open_external_madina")
                        ) {
                            Text(
                                text = "فتح التطبيق المعتمد",
                                fontFamily = CairoFontFamily,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }

        // Daily Adhkar Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .clickable { onNavigateToAdhkar() }
                    .testTag("adhkar_home_card"),
                shape = RoundedCornerShape(20.dp),
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(IslamicGoldContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.WbSunny,
                                contentDescription = "أذكار اليوم",
                                tint = IslamicGold,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = "أذكار الصباح والمساء",
                                style = MaterialTheme.typography.titleMedium,
                                fontFamily = CairoFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "حافظ على أذكارك اليومية وحصّن نفسك بالذكر المأثور",
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = CairoFontFamily,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Icon(
                        Icons.Default.ArrowBackIosNew,
                        contentDescription = "عرض الأذكار",
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Qibla Direction Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .clickable { onNavigateToQibla() }
                    .testTag("qibla_home_card"),
                shape = RoundedCornerShape(20.dp),
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(IslamicGreenContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Explore,
                                contentDescription = "بوصلة القبلة",
                                tint = IslamicGreenPrimary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = "بوصلة القبلة المشرفة",
                                style = MaterialTheme.typography.titleMedium,
                                fontFamily = CairoFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "تحديد اتجاه الكعبة المشرفة بدقة عبر مستشعرات الهاتف",
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = CairoFontFamily,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Icon(
                        Icons.Default.ArrowBackIosNew,
                        contentDescription = "عرض البوصلة",
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Daily Inspiration Card (Verified Ayah / Hadith)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .testTag("daily_inspiration_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = QuranParchmentLight),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(IslamicGold, IslamicGreenLight)))
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = IslamicGoldContainer
                    ) {
                        Text(
                            text = dailyInspiration.type,
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = CairoFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = IslamicGoldOnContainer,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = dailyInspiration.quote,
                        style = MaterialTheme.typography.titleMedium.copy(lineHeight = 32.sp),
                        fontFamily = AmiriFontFamily,
                        color = TextPrimaryLight,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = dailyInspiration.source,
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = CairoFontFamily,
                        color = TextSecondaryLight
                    )
                }
            }
        }

        // Reminders Header & List
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "جداول التذكيرات المخصصة",
                    style = MaterialTheme.typography.titleMedium,
                    fontFamily = CairoFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                FilledTonalButton(
                    onClick = onAddReminder,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("btn_add_reminder_home")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "إضافة تذكير", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("إضافة", fontFamily = CairoFontFamily)
                }
            }
        }

        items(reminders) { reminder ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .clickable { onEditReminder(reminder) }
                    .testTag("reminder_item_${reminder.id}"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
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
                            text = reminder.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontFamily = CairoFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "كل ${reminder.intervalMinutes} دقيقة • من ${reminder.startHour}:00 إلى ${reminder.endHour}:00",
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = CairoFontFamily,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (reminder.message.isNotBlank()) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = reminder.message,
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = AmiriFontFamily,
                                color = IslamicGoldDark,
                                maxLines = 1
                            )
                        }
                    }

                    Switch(
                        checked = reminder.isEnabled,
                        onCheckedChange = { isChecked ->
                            onToggleReminder(reminder.id, isChecked)
                        },
                        modifier = Modifier.testTag("switch_reminder_${reminder.id}")
                    )
                }
            }
        }
    }
}
