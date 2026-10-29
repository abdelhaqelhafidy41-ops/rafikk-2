package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.prayer.PrayerCalculator
import com.example.prayer.PrayerTimesResult
import com.example.ui.theme.*
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnnualPrayerCalendarDialog(
    cityName: String,
    latitude: Double,
    longitude: Double,
    calculationMethodName: String,
    asrJuristicName: String,
    onDismiss: () -> Unit
) {
    val currentCal = remember { Calendar.getInstance() }
    val currentYear = remember { currentCal.get(Calendar.YEAR) }
    var selectedMonth by remember { mutableIntStateOf(currentCal.get(Calendar.MONTH)) } // 0..11

    val monthsAr = listOf(
        "يناير (١)", "فبراير (٢)", "مارس (٣)", "أبريل (٤)",
        "مايو (٥)", "يونيو (٦)", "يوليو (٧)", "أغسطس (٨)",
        "سبتمبر (٩)", "أكتوبر (١٠)", "نوفمبر (١١)", "ديسمبر (١٢)"
    )

    val method = remember(calculationMethodName) {
        try {
            PrayerCalculator.CalculationMethod.valueOf(calculationMethodName)
        } catch (_: Exception) {
            PrayerCalculator.CalculationMethod.UMM_AL_QURA
        }
    }

    val asrJuristic = remember(asrJuristicName) {
        try {
            PrayerCalculator.AsrJuristic.valueOf(asrJuristicName)
        } catch (_: Exception) {
            PrayerCalculator.AsrJuristic.SHAFI
        }
    }

    // Generate prayer times for each day of the selected month
    val daysInMonth = remember(selectedMonth, currentYear) {
        val tempCal = Calendar.getInstance()
        tempCal.set(Calendar.YEAR, currentYear)
        tempCal.set(Calendar.MONTH, selectedMonth)
        tempCal.getActualMaximum(Calendar.DAY_OF_MONTH)
    }

    data class DayPrayerSchedule(
        val dayNumber: Int,
        val times: PrayerTimesResult,
        val isToday: Boolean
    )

    val monthlySchedule = remember(selectedMonth, currentYear, latitude, longitude, method, asrJuristic) {
        val todayCal = Calendar.getInstance()
        val isCurrentYearAndMonth = (todayCal.get(Calendar.YEAR) == currentYear && todayCal.get(Calendar.MONTH) == selectedMonth)
        val todayDay = todayCal.get(Calendar.DAY_OF_MONTH)

        (1..daysInMonth).map { day ->
            val dayCal = Calendar.getInstance().apply {
                set(Calendar.YEAR, currentYear)
                set(Calendar.MONTH, selectedMonth)
                set(Calendar.DAY_OF_MONTH, day)
            }
            val res = PrayerCalculator.calculateTimes(
                latitude = latitude,
                longitude = longitude,
                calendar = dayCal,
                method = method,
                asrJuristic = asrJuristic
            )
            DayPrayerSchedule(
                dayNumber = day,
                times = res,
                isToday = isCurrentYearAndMonth && (day == todayDay)
            )
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "إمساكية ومواقيت العام $currentYear",
                            style = MaterialTheme.typography.titleMedium,
                            fontFamily = CairoFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "$cityName • حساب فلكي دقيق لكامل شهور السنة",
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = CairoFontFamily,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Month Selector Horizontal Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(12) { monthIdx ->
                        val isSelected = selectedMonth == monthIdx
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedMonth = monthIdx },
                            label = { Text(monthsAr[monthIdx], fontFamily = CairoFontFamily, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = IslamicGreenPrimary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Table Column Titles Header
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = IslamicGoldContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp, horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("اليوم", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(0.8f), textAlign = TextAlign.Center)
                        Text("الفجر", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                        Text("الشروق", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                        Text("الظهر", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                        Text("العصر", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                        Text("المغرب", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                        Text("العشاء", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Table List of Days
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(monthlySchedule, key = { it.dayNumber }) { dayItem ->
                        val bg = if (dayItem.isToday) IslamicGreenContainer else if (dayItem.dayNumber % 2 == 0) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f) else Color.Transparent

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = bg,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp, horizontal = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${dayItem.dayNumber}${if (dayItem.isToday) "★" else ""}",
                                    fontFamily = CairoFontFamily,
                                    fontWeight = if (dayItem.isToday) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 11.sp,
                                    color = if (dayItem.isToday) IslamicGreenDark else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(0.8f),
                                    textAlign = TextAlign.Center
                                )
                                Text(dayItem.times.fajr, fontFamily = CairoFontFamily, fontSize = 11.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                                Text(dayItem.times.sunrise, fontFamily = CairoFontFamily, fontSize = 11.sp, color = MaterialTheme.colorScheme.outline, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                                Text(dayItem.times.dhuhr, fontFamily = CairoFontFamily, fontSize = 11.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                                Text(dayItem.times.asr, fontFamily = CairoFontFamily, fontSize = 11.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                                Text(dayItem.times.maghrib, fontFamily = CairoFontFamily, fontWeight = FontWeight.SemiBold, color = IslamicGoldDark, fontSize = 11.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                                Text(dayItem.times.isha, fontFamily = CairoFontFamily, fontSize = 11.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = IslamicGreenPrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("إغلاق الإمساكية", fontFamily = CairoFontFamily)
                }
            }
        }
    }
}
