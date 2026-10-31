package com.example.ui

import android.Manifest
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.AppSettingsEntity
import com.example.location.LocationHelper
import com.example.prayer.PrayerCalculator
import com.example.prayer.PrayerTimesResult
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrayerScreen(
    settings: AppSettingsEntity,
    prayerTimes: PrayerTimesResult,
    todayHijri: String,
    isGpsLoading: Boolean,
    onOpenCitySelection: () -> Unit,
    onOpenQibla: () -> Unit,
    onGpsClicked: (Boolean) -> Unit,
    onSelectCalculationMethod: (PrayerCalculator.CalculationMethod) -> Unit,
    onSelectAsrJuristic: (PrayerCalculator.AsrJuristic) -> Unit,
    onSelectHighLatitudeRule: (PrayerCalculator.HighLatitudeRule) -> Unit,
    onAdjustPrayer: (String, Int) -> Unit,
    onResetPrayerAdjustments: () -> Unit = {},
    onUpdateCustomAngles: (Double, Double) -> Unit = { _, _ -> },
    onUpdatePrePrayerAlert: (Boolean, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showSettingsSheet by remember { mutableStateOf(false) }
    var showAdjustDialog by remember { mutableStateOf(false) }
    var showAnnualCalendar by remember { mutableStateOf(false) }

    // Custom angle state for editing
    var customFajrText by remember(settings.customFajrAngle) { mutableStateOf(settings.customFajrAngle.toString()) }
    var customIshaText by remember(settings.customIshaAngle) { mutableStateOf(settings.customIshaAngle.toString()) }

    // Location permission launcher
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        onGpsClicked(granted)
    }

    val currentMethod = remember(settings.calculationMethod) {
        PrayerCalculator.CalculationMethod.fromString(settings.calculationMethod)
    }

    val currentAsr = remember(settings.asrJuristic) {
        PrayerCalculator.AsrJuristic.fromString(settings.asrJuristic)
    }

    val currentHighLat = remember(settings.highLatitudeRule) {
        PrayerCalculator.HighLatitudeRule.fromString(settings.highLatitudeRule)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("prayer_screen_column"),
        contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 96.dp)
    ) {
        // Top Header
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Header Title & Hijri Date
                Text(
                    text = "مواقيت الصلاة",
                    style = MaterialTheme.typography.headlineSmall,
                    fontFamily = CairoFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = todayHijri,
                    style = MaterialTheme.typography.bodyMedium,
                    fontFamily = CairoFontFamily,
                    color = IslamicGold
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Action Buttons: Qibla & Annual Calendar & Prayer Settings
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Qibla Button
                    FilledTonalButton(
                        onClick = onOpenQibla,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = IslamicGreenContainer,
                            contentColor = IslamicGreenDark
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp)
                            .testTag("btn_prayer_qibla")
                    ) {
                        Icon(
                            Icons.Default.Explore,
                            contentDescription = "القبلة",
                            modifier = Modifier.size(18.dp),
                            tint = IslamicGreenPrimary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "القبلة",
                            fontFamily = CairoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            maxLines = 1
                        )
                    }

                    // Annual Calendar Button
                    OutlinedButton(
                        onClick = { showAnnualCalendar = true },
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, IslamicGold.copy(alpha = 0.7f)),
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp)
                            .testTag("btn_prayer_annual_calendar")
                    ) {
                        Icon(
                            Icons.Default.CalendarMonth,
                            contentDescription = "مواقيت العام",
                            modifier = Modifier.size(18.dp),
                            tint = IslamicGoldDark
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "مواقيت العام",
                            fontFamily = CairoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            maxLines = 1
                        )
                    }

                    // Prayer Settings Button (Prominent)
                    Button(
                        onClick = { showSettingsSheet = true },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = IslamicGreenPrimary,
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .weight(1.2f)
                            .heightIn(min = 48.dp)
                            .testTag("btn_prayer_settings")
                    ) {
                        Icon(
                            Icons.Default.Tune,
                            contentDescription = "إعدادات المواقيت",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "إعدادات المواقيت",
                            fontFamily = CairoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            maxLines = 1
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Location & City Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("prayer_city_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.LocationOn, contentDescription = "المدينة", tint = IslamicGreenPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = settings.cityName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontFamily = CairoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                                val locationSourceLabel = if (settings.isManualLocation) {
                                    "🗺️ اختيار يدوي"
                                } else {
                                    "📍 تحديد تلقائي GPS"
                                }
                                Text(
                                    text = "${settings.countryName} • $locationSourceLabel",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontFamily = CairoFontFamily,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                            }
                        }

                        Button(
                            onClick = onOpenCitySelection,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("btn_change_city")
                        ) {
                            Text("تغيير المدينة", fontFamily = CairoFontFamily, fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // GPS auto fetch button (only triggered upon explicit user click)
                    OutlinedButton(
                        onClick = {
                            val hasPerm = LocationHelper.hasLocationPermission(context)
                            if (hasPerm) {
                                onGpsClicked(true)
                            } else {
                                locationPermissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_gps_location"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isGpsLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("جاري استقبال إشارة GPS...", fontFamily = CairoFontFamily)
                        } else {
                            Icon(Icons.Default.MyLocation, contentDescription = "موقعي الحالي", modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("تحديث الموقع الحالي عبر الـ GPS", fontFamily = CairoFontFamily)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Next Prayer Spotlight
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("next_prayer_spotlight"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = IslamicGreenPrimary),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "الصلاة القادمة",
                        style = MaterialTheme.typography.titleSmall,
                        color = Color(0xFFD4E6DF),
                        fontFamily = CairoFontFamily
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = prayerTimes.nextPrayerName,
                        style = MaterialTheme.typography.displayMedium,
                        color = Color.White,
                        fontFamily = CairoFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = IslamicGold
                    ) {
                        Text(
                            text = prayerTimes.nextPrayerTime,
                            style = MaterialTheme.typography.titleLarge,
                            color = Color(0xFF261A02),
                            fontFamily = CairoFontFamily,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "متبقي: ${prayerTimes.remainingFormatted}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = IslamicGoldLight,
                        fontFamily = CairoFontFamily,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Calculation Method & Madhab Badge
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0x33000000)
                    ) {
                        Text(
                            text = "🕌 ${currentMethod.displayNameAr} • ${currentAsr.displayNameAr}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFE2EFEA),
                            fontFamily = CairoFontFamily,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            maxLines = 1
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Daily Prayers List Card (Including Imsak & Midnight)
        item {
            val prayerList = buildList {
                if (prayerTimes.imsak.isNotBlank()) {
                    add(Triple("الإمساك", prayerTimes.imsak, "قبل الفجر بـ ١٠ دقائق"))
                }
                add(Triple("صلاة الفجر", prayerTimes.fajr, ""))
                add(Triple("شروق الشمس", prayerTimes.sunrise, ""))
                add(Triple("صلاة الظهر", prayerTimes.dhuhr, ""))
                add(Triple("صلاة العصر", prayerTimes.asr, ""))
                add(Triple("صلاة المغرب", prayerTimes.maghrib, ""))
                add(Triple("صلاة العشاء", prayerTimes.isha, ""))
                if (prayerTimes.midnight.isNotBlank()) {
                    add(Triple("منتصف الليل الشرعي", prayerTimes.midnight, "نصف الليل بين الغروب والفجر"))
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("prayers_full_list_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "جدول أوقات الصلاة اليوم",
                            style = MaterialTheme.typography.titleMedium,
                            fontFamily = CairoFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        IconButton(onClick = { showSettingsSheet = true }) {
                            Icon(Icons.Default.Settings, contentDescription = "إعدادات", tint = IslamicGreenPrimary)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    prayerList.forEachIndexed { index, (name, time, subtitle) ->
                        val isNext = prayerTimes.nextPrayerName in name
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isNext) IslamicGoldContainer else Color.Transparent)
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontFamily = CairoFontFamily,
                                    fontWeight = if (isNext) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isNext) IslamicGoldOnContainer else MaterialTheme.colorScheme.onSurface
                                )
                                if (subtitle.isNotBlank()) {
                                    Text(
                                        text = subtitle,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontFamily = CairoFontFamily,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Text(
                                text = time,
                                style = MaterialTheme.typography.bodyLarge,
                                fontFamily = CairoFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = if (isNext) IslamicGoldOnContainer else MaterialTheme.colorScheme.primary
                            )
                        }
                        if (index < prayerList.size - 1) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Active Settings Summary Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("prayer_settings_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "طريقة الحساب والضبط الشرعي",
                            style = MaterialTheme.typography.titleMedium,
                            fontFamily = CairoFontFamily,
                            fontWeight = FontWeight.Bold
                        )
                        TextButton(onClick = { showSettingsSheet = true }) {
                            Text("تعديل", fontFamily = CairoFontFamily)
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("الطريقة المعتمدة:", style = MaterialTheme.typography.labelMedium, fontFamily = CairoFontFamily)
                            Text(currentMethod.displayNameAr, style = MaterialTheme.typography.bodyMedium, fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold, color = IslamicGreenPrimary)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("مذهب العصر:", style = MaterialTheme.typography.labelMedium, fontFamily = CairoFontFamily)
                            Text(currentAsr.displayNameAr, style = MaterialTheme.typography.bodyMedium, fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                    Spacer(modifier = Modifier.height(8.dp))

                    // Minute adjustments quick summary
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val hasAdjustments = settings.fajrAdjustmentMinutes != 0 ||
                                settings.dhuhrAdjustmentMinutes != 0 ||
                                settings.asrAdjustmentMinutes != 0 ||
                                settings.maghribAdjustmentMinutes != 0 ||
                                settings.ishaAdjustmentMinutes != 0

                        Column {
                            Text("التعديل اليدوي بالدقائق:", style = MaterialTheme.typography.labelMedium, fontFamily = CairoFontFamily)
                            Text(
                                if (hasAdjustments) "يوجد تعديلات نشطة" else "مطابق للحساب الفلكي المباشر",
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = CairoFontFamily,
                                color = if (hasAdjustments) IslamicGoldDark else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        OutlinedButton(
                            onClick = { showAdjustDialog = true },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Tune, contentDescription = "تعديل الدقائق", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("ضبط الدقائق (+/-)", fontFamily = CairoFontFamily, fontSize = 12.sp)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Official Required Disclaimer Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = "تنبيه رسمي",
                        tint = IslamicGoldDark,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "قد تختلف مواقيت الصلاة بفارق بسيط عن التوقيت الرسمي المحلي، لذلك يُنصح بمراجعة تقويم وزارة الأوقاف أو الجهة الإسلامية الرسمية في بلدك.",
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = CairoFontFamily,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }

    // Comprehensive Prayer Settings Bottom Sheet
    if (showSettingsSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSettingsSheet = false },
            sheetState = rememberModalBottomSheetState()
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                item {
                    Text(
                        text = "إعدادات مواقيت الصلاة",
                        style = MaterialTheme.typography.titleLarge,
                        fontFamily = CairoFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "اختر الهيئة أو الطريقة الفلكية ومذهب حساب صلاة العصر:",
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = CairoFontFamily,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                }

                // 8 Approved Calculation Methods
                item {
                    Text(
                        text = "طرق الحساب المعتمدة (٨ طرق):",
                        style = MaterialTheme.typography.titleMedium,
                        fontFamily = CairoFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                items(PrayerCalculator.CalculationMethod.values().toList()) { method ->
                    val isSelected = settings.calculationMethod == method.name
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable {
                                onSelectCalculationMethod(method)
                            },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) IslamicGreenContainer else MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = method.displayNameAr,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontFamily = CairoFontFamily,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                                val subtitle = when (method) {
                                    PrayerCalculator.CalculationMethod.UMM_AL_QURA -> "الفجر: 18.5° • العشاء: بعد المغرب بـ 90 دقيقة (120 د في رمضان)"
                                    PrayerCalculator.CalculationMethod.MOROCCO_HABOUS -> "الفجر: 19° • العشاء: 17° (المملكة المغربية)"
                                    PrayerCalculator.CalculationMethod.MWL -> "الفجر: 18° • العشاء: 17° (أوروبا والعالم الإسلامي)"
                                    PrayerCalculator.CalculationMethod.EGYPTIAN -> "الفجر: 19.5° • العشاء: 17.5° (مصر، السودان، الشام)"
                                    PrayerCalculator.CalculationMethod.KARACHI -> "الفجر: 18° • العشاء: 18° (باكستان، بنغلاديش، الهند)"
                                    PrayerCalculator.CalculationMethod.ISNA -> "الفجر: 15° • العشاء: 15° (الولايات المتحدة وكندا)"
                                    PrayerCalculator.CalculationMethod.FRANCE_UOIF -> "الفجر: 12° • العشاء: 12° (فرنسا وغرب أوروبا)"
                                    PrayerCalculator.CalculationMethod.CUSTOM -> "تحديد يدوي لزاويتي الفجر والعشاء"
                                }
                                Text(
                                    text = subtitle,
                                    style = MaterialTheme.typography.labelSmall,
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

                // Custom angles input if CUSTOM method selected
                if (settings.calculationMethod == PrayerCalculator.CalculationMethod.CUSTOM.name) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "إدخال زوايا الحساب المخصصة:",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontFamily = CairoFontFamily,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = customFajrText,
                                        onValueChange = {
                                            customFajrText = it
                                            val f = it.toDoubleOrNull()
                                            val i = customIshaText.toDoubleOrNull()
                                            if (f != null && i != null) onUpdateCustomAngles(f, i)
                                        },
                                        label = { Text("زاوية الفجر (°)", fontFamily = CairoFontFamily) },
                                        modifier = Modifier.weight(1f)
                                    )
                                    OutlinedTextField(
                                        value = customIshaText,
                                        onValueChange = {
                                            customIshaText = it
                                            val f = customFajrText.toDoubleOrNull()
                                            val i = it.toDoubleOrNull()
                                            if (f != null && i != null) onUpdateCustomAngles(f, i)
                                        },
                                        label = { Text("زاوية العشاء (°)", fontFamily = CairoFontFamily) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }

                // Asr Juristic Method
                item {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "مذهب حساب صلاة العصر:",
                        style = MaterialTheme.typography.titleMedium,
                        fontFamily = CairoFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = settings.asrJuristic == "SHAFI",
                            onClick = { onSelectAsrJuristic(PrayerCalculator.AsrJuristic.SHAFI) },
                            label = { Text("الجمهور (ظل المثل ١.٠)", fontFamily = CairoFontFamily) },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = settings.asrJuristic == "HANAFI",
                            onClick = { onSelectAsrJuristic(PrayerCalculator.AsrJuristic.HANAFI) },
                            label = { Text("الحنفي (ظل المثلين ٢.٠)", fontFamily = CairoFontFamily) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // High Latitude Adjustment Rule
                item {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "تعديل خطوط العرض العالية (الدول الشمالية):",
                        style = MaterialTheme.typography.titleMedium,
                        fontFamily = CairoFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                }

                items(PrayerCalculator.HighLatitudeRule.values().toList()) { rule ->
                    val isSelected = settings.highLatitudeRule == rule.name
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                            .clickable { onSelectHighLatitudeRule(rule) },
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) IslamicGreenContainer else MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = rule.displayNameAr,
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = CairoFontFamily,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                            if (isSelected) {
                                Icon(Icons.Default.Check, contentDescription = "محدد", tint = IslamicGreenPrimary)
                            }
                        }
                    }
                }

                // Close Button
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { showSettingsSheet = false },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("تم وحفظ الإعدادات", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // Minute Adjustment Dialog
    if (showAdjustDialog) {
        AlertDialog(
            onDismissRequest = { showAdjustDialog = false },
            title = {
                Text("تعديل دقائق الصلوات", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    Text(
                        text = "يمكنك تقديم أو تأخير أي صلاة بدقائق لتطابق أذان وتقويم مسجدك المحلي (-60 إلى +60 دقيقة):",
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = CairoFontFamily
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    val adjList = listOf(
                        Pair("fajr", "الفجر: ${settings.fajrAdjustmentMinutes} د"),
                        Pair("dhuhr", "الظهر: ${settings.dhuhrAdjustmentMinutes} د"),
                        Pair("asr", "العصر: ${settings.asrAdjustmentMinutes} د"),
                        Pair("maghrib", "المغرب: ${settings.maghribAdjustmentMinutes} د"),
                        Pair("isha", "العشاء: ${settings.ishaAdjustmentMinutes} د")
                    )

                    adjList.forEach { (key, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(label, fontFamily = CairoFontFamily, fontWeight = FontWeight.SemiBold)
                            Row {
                                FilledTonalButton(
                                    onClick = { onAdjustPrayer(key, -1) },
                                    contentPadding = PaddingValues(horizontal = 8.dp)
                                ) { Text("-١") }
                                Spacer(modifier = Modifier.width(6.dp))
                                FilledTonalButton(
                                    onClick = { onAdjustPrayer(key, 1) },
                                    contentPadding = PaddingValues(horizontal = 8.dp)
                                ) { Text("+١") }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = { onResetPrayerAdjustments() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "إعادة ضبط", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("إعادة ضبط جميع التعديلات إلى صفر", fontFamily = CairoFontFamily, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showAdjustDialog = false }) {
                    Text("تم", fontFamily = CairoFontFamily)
                }
            }
        )
    }

    if (showAnnualCalendar) {
        AnnualPrayerCalendarDialog(
            cityName = settings.cityName,
            latitude = settings.latitude,
            longitude = settings.longitude,
            calculationMethodName = settings.calculationMethod,
            asrJuristicName = settings.asrJuristic,
            onDismiss = { showAnnualCalendar = false }
        )
    }
}
