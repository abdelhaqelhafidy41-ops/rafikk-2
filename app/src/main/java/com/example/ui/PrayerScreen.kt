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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.AppSettingsEntity
import com.example.location.LocationHelper
import com.example.prayer.CityCoordinates
import com.example.prayer.CityInfo
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
    onUpdatePrePrayerAlert: (Boolean, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showMethodPickerSheet by remember { mutableStateOf(false) }
    var showHighLatSheet by remember { mutableStateOf(false) }
    var showAdjustDialog by remember { mutableStateOf(false) }

    // Location permission launcher
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        onGpsClicked(granted)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("prayer_screen_column"),
        contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 96.dp)
    ) {
        // Top Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "مواقيت الصلاة",
                        style = MaterialTheme.typography.titleLarge,
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
                }

                // Qibla Button
                FilledTonalButton(
                    onClick = onOpenQibla,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("btn_prayer_qibla")
                ) {
                    Icon(Icons.Default.Explore, contentDescription = "القبلة", modifier = Modifier.size(18.dp), tint = IslamicGreenPrimary)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("اتجاه القبلة", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold)
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, contentDescription = "المدينة", tint = IslamicGreenPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = settings.cityName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontFamily = CairoFontFamily,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${settings.countryName} • ${if (settings.isManualLocation) "اختيار يدوي محفوظ" else "تحديد GPS"}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontFamily = CairoFontFamily,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
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

                    // GPS auto fetch button (only triggered upon explicit user click!)
                    OutlinedButton(
                        onClick = {
                            val hasPerm = LocationHelper.hasLocationPermission(context)
                            if (hasPerm) {
                                onGpsClicked(true)
                            } else {
                                onGpsClicked(false)
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
                    Spacer(modifier = Modifier.height(6.dp))
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
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Daily Prayers List Card
        item {
            val prayerList = listOf(
                Pair("صلاة الفجر", prayerTimes.fajr),
                Pair("شروق الشمس", prayerTimes.sunrise),
                Pair("صلاة الظهر", prayerTimes.dhuhr),
                Pair("صلاة العصر", prayerTimes.asr),
                Pair("صلاة المغرب", prayerTimes.maghrib),
                Pair("صلاة العشاء", prayerTimes.isha)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("prayers_full_list_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "جدول أوقات الصلاة اليوم",
                        style = MaterialTheme.typography.titleMedium,
                        fontFamily = CairoFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    prayerList.forEachIndexed { index, (name, time) ->
                        val isNext = prayerTimes.nextPrayerName in name
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isNext) IslamicGoldContainer else Color.Transparent)
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = name,
                                style = MaterialTheme.typography.bodyLarge,
                                fontFamily = CairoFontFamily,
                                fontWeight = if (isNext) FontWeight.Bold else FontWeight.Normal,
                                color = if (isNext) IslamicGoldOnContainer else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = time,
                                style = MaterialTheme.typography.bodyLarge,
                                fontFamily = CairoFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = if (isNext) IslamicGoldOnContainer else MaterialTheme.colorScheme.primary
                            )
                        }
                        if (index < prayerList.size - 1) {
                            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Calculation Method, High Latitudes, & Adjustments
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
                    Text(
                        text = "طريقة الحساب والضبط الشرعي",
                        style = MaterialTheme.typography.titleMedium,
                        fontFamily = CairoFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    // Method selector
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showMethodPickerSheet = true }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("طريقة الحساب الفلكية:", style = MaterialTheme.typography.labelMedium, fontFamily = CairoFontFamily)
                            val currentMethod = try {
                                PrayerCalculator.CalculationMethod.valueOf(settings.calculationMethod).displayNameAr
                            } catch (_: Exception) {
                                "أم القرى"
                            }
                            Text(currentMethod, style = MaterialTheme.typography.bodyMedium, fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold, color = IslamicGreenPrimary)
                        }
                        Icon(Icons.Default.ArrowBackIosNew, contentDescription = "تغيير", modifier = Modifier.size(16.dp))
                    }

                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 4.dp))

                    // Asr Juristic Method
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("حساب وقت العصر:", style = MaterialTheme.typography.labelMedium, fontFamily = CairoFontFamily)
                            Text(
                                if (settings.asrJuristic == "HANAFI") "المذهب الحنفي" else "الجمهور (الشافعي، المالكي، الحنبلي)",
                                style = MaterialTheme.typography.bodyMedium,
                                fontFamily = CairoFontFamily,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Row {
                            FilterChip(
                                selected = settings.asrJuristic == "SHAFI",
                                onClick = { onSelectAsrJuristic(PrayerCalculator.AsrJuristic.SHAFI) },
                                label = { Text("الجمهور", fontFamily = CairoFontFamily, fontSize = 12.sp) }
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            FilterChip(
                                selected = settings.asrJuristic == "HANAFI",
                                onClick = { onSelectAsrJuristic(PrayerCalculator.AsrJuristic.HANAFI) },
                                label = { Text("حنفي", fontFamily = CairoFontFamily, fontSize = 12.sp) }
                            )
                        }
                    }

                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 4.dp))

                    // High Latitude Rule
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showHighLatSheet = true }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("تعديل المناطق ذات خطوط العرض العالية:", style = MaterialTheme.typography.labelMedium, fontFamily = CairoFontFamily)
                            val ruleName = try {
                                PrayerCalculator.HighLatitudeRule.valueOf(settings.highLatitudeRule).displayNameAr
                            } catch (_: Exception) {
                                "بدون تعديل"
                            }
                            Text(ruleName, style = MaterialTheme.typography.bodySmall, fontFamily = CairoFontFamily, fontWeight = FontWeight.SemiBold, color = IslamicGoldDark)
                        }
                        Icon(Icons.Default.ArrowBackIosNew, contentDescription = "تغيير", modifier = Modifier.size(16.dp))
                    }

                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 4.dp))

                    // Manual minute adjustments button
                    OutlinedButton(
                        onClick = { showAdjustDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Tune, contentDescription = "تعديل يدوي", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("تعديل يدوي لدقائق الأوقات (+/-)", fontFamily = CairoFontFamily)
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Official Disclaimer
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
                        contentDescription = "تنبيه",
                        tint = IslamicGoldDark,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "تنبيه: مواقيت الصلاة استرشادية وتقريبية وفق الحسابات الفلكية الدقيقة، وقد تختلف قليلًا عن التوقيت الرسمي المحلي في مساجد بلدك.",
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = CairoFontFamily,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    // Calculation Method Bottom Sheet
    if (showMethodPickerSheet) {
        ModalBottomSheet(
            onDismissRequest = { showMethodPickerSheet = false },
            sheetState = rememberModalBottomSheetState()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "اختر طريقة حساب المواقيت",
                    style = MaterialTheme.typography.titleLarge,
                    fontFamily = CairoFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(12.dp))
                PrayerCalculator.CalculationMethod.values().forEach { method ->
                    val isSelected = settings.calculationMethod == method.name
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable {
                                onSelectCalculationMethod(method)
                                showMethodPickerSheet = false
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
                            Text(
                                text = method.displayNameAr,
                                style = MaterialTheme.typography.bodyMedium,
                                fontFamily = CairoFontFamily,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                            if (isSelected) {
                                Icon(Icons.Default.Check, contentDescription = "محدد", tint = IslamicGreenPrimary)
                            }
                        }
                    }
                }
            }
        }
    }

    // High Latitude Rule Bottom Sheet
    if (showHighLatSheet) {
        ModalBottomSheet(
            onDismissRequest = { showHighLatSheet = false },
            sheetState = rememberModalBottomSheetState()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "طرق التقدير لخطوط العرض العالية",
                    style = MaterialTheme.typography.titleLarge,
                    fontFamily = CairoFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "تُستخدم في المناطق الشمالية (أوروبا وأمريكا الشمالية) التي تغيب فيها علامات الشفق الفلكي صيفًا:",
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = CairoFontFamily,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                PrayerCalculator.HighLatitudeRule.values().forEach { rule ->
                    val isSelected = settings.highLatitudeRule == rule.name
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable {
                                onSelectHighLatitudeRule(rule)
                                showHighLatSheet = false
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
                        text = "يمكنك تقديم أو تأخير أي صلاة بدقائق لتطابق أذان مسجدك المحلي:",
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
                }
            },
            confirmButton = {
                Button(onClick = { showAdjustDialog = false }) {
                    Text("تم", fontFamily = CairoFontFamily)
                }
            }
        )
    }
}
