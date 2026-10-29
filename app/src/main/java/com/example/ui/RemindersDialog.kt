package com.example.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.database.ReminderEntity
import com.example.ui.theme.CairoFontFamily
import com.example.ui.theme.IslamicGold
import com.example.ui.theme.IslamicGreenPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditReminderDialog(
    initialReminder: ReminderEntity,
    onDismiss: () -> Unit,
    onSave: (ReminderEntity) -> Unit
) {
    var title by remember { mutableStateOf(initialReminder.title) }
    var message by remember { mutableStateOf(initialReminder.message) }
    var selectedType by remember { mutableStateOf(initialReminder.type) }
    var intervalMinutes by remember { mutableStateOf(initialReminder.intervalMinutes) }
    var customMinutesText by remember { mutableStateOf(if (initialReminder.intervalMinutes !in listOf(5, 10, 15, 30, 60, 120)) initialReminder.intervalMinutes.toString() else "") }
    var startHour by remember { mutableStateOf(initialReminder.startHour) }
    var endHour by remember { mutableStateOf(initialReminder.endHour) }
    var silentDuringSleep by remember { mutableStateOf(initialReminder.silentDuringSleep) }
    var silentDuringPrayer by remember { mutableStateOf(initialReminder.silentDuringPrayer) }

    val presetTypes = listOf(
        Pair("SALAWAT", "الصلاة على النبي ﷺ"),
        Pair("ISTIGHFAR", "الاستغفار"),
        Pair("QURAN", "قراءة القرآن"),
        Pair("ATHKAR", "أذكار الصباح والمساء"),
        Pair("PRAYER", "تذكير بالصلاة"),
        Pair("GENERAL", "اترك ما يشغلك واذكر الله"),
        Pair("CUSTOM", "ذكر مخصص")
    )

    val presetIntervals = listOf(
        Pair(5, "٥ دقائق"),
        Pair(10, "١٠ دقائق"),
        Pair(15, "١٥ دقيقة"),
        Pair(30, "٣٠ دقيقة"),
        Pair(60, "ساعة"),
        Pair(120, "ساعتان")
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .testTag("add_edit_reminder_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (initialReminder.id == 0) "إضافة تذكير مخصص" else "تعديل التذكير",
                        style = MaterialTheme.typography.titleLarge,
                        fontFamily = CairoFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("dialog_close_button")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Type Selection Chips
                Text(
                    text = "نوع التذكير:",
                    style = MaterialTheme.typography.titleSmall,
                    fontFamily = CairoFontFamily,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    presetTypes.chunked(2).forEach { rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowItems.forEach { (typeKey, typeLabel) ->
                                val isSelected = selectedType == typeKey
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        selectedType = typeKey
                                        if (title.isBlank() || presetTypes.any { it.second == title }) {
                                            title = typeLabel
                                        }
                                        if (typeKey == "SALAWAT") message = "اللهم صلِّ وسلم وبارك على نبينا محمد"
                                        if (typeKey == "ISTIGHFAR") message = "أستغفر الله العظيم وأتوب إليه"
                                        if (typeKey == "GENERAL") message = "اترك ما يشغلك واذكر الله"
                                    },
                                    label = { Text(typeLabel, fontFamily = CairoFontFamily, fontSize = 12.sp) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Title Input
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("عنوان التذكير", fontFamily = CairoFontFamily) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("reminder_title_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Message Input
                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    label = { Text("نص التذكير أو الذكر", fontFamily = CairoFontFamily) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("reminder_message_input"),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Frequency / Interval
                Text(
                    text = "تكرار التذكير:",
                    style = MaterialTheme.typography.titleSmall,
                    fontFamily = CairoFontFamily,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    presetIntervals.chunked(3).forEach { rowIntervals ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowIntervals.forEach { (mins, label) ->
                                val isSelected = intervalMinutes == mins && customMinutesText.isEmpty()
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        intervalMinutes = mins
                                        customMinutesText = ""
                                    },
                                    label = { Text(label, fontFamily = CairoFontFamily, fontSize = 12.sp) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Custom Interval Input
                OutlinedTextField(
                    value = customMinutesText,
                    onValueChange = { text ->
                        customMinutesText = text
                        val num = text.toIntOrNull()
                        if (num != null && num > 0) {
                            intervalMinutes = num
                        }
                    },
                    label = { Text("أو اكتب فترة مخصصة بالدقائق (مثلاً: 45)", fontFamily = CairoFontFamily) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("reminder_custom_interval_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Active Hours Window (e.g. from 8 AM to 10 PM)
                Text(
                    text = "وقت نشاط التذكير:",
                    style = MaterialTheme.typography.titleSmall,
                    fontFamily = CairoFontFamily,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("يبدأ الساعة:", style = MaterialTheme.typography.bodySmall, fontFamily = CairoFontFamily)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            FilledTonalButton(
                                onClick = { startHour = (startHour - 1).coerceAtLeast(0) },
                                contentPadding = PaddingValues(horizontal = 8.dp)
                            ) { Text("-") }
                            Text(" ${startHour}:00 ", fontWeight = FontWeight.Bold)
                            FilledTonalButton(
                                onClick = { startHour = (startHour + 1).coerceAtMost(23) },
                                contentPadding = PaddingValues(horizontal = 8.dp)
                            ) { Text("+") }
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("ينتهي الساعة:", style = MaterialTheme.typography.bodySmall, fontFamily = CairoFontFamily)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            FilledTonalButton(
                                onClick = { endHour = (endHour - 1).coerceAtLeast(0) },
                                contentPadding = PaddingValues(horizontal = 8.dp)
                            ) { Text("-") }
                            Text(" ${endHour}:00 ", fontWeight = FontWeight.Bold)
                            FilledTonalButton(
                                onClick = { endHour = (endHour + 1).coerceAtMost(23) },
                                contentPadding = PaddingValues(horizontal = 8.dp)
                            ) { Text("+") }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Silent Toggles
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("إيقاف أثناء أوقات النوم", fontFamily = CairoFontFamily, fontSize = 14.sp)
                            Switch(
                                checked = silentDuringSleep,
                                onCheckedChange = { silentDuringSleep = it },
                                modifier = Modifier.testTag("switch_silent_sleep")
                            )
                        }
                        Divider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("إيقاف أثناء أوقات الصلاة", fontFamily = CairoFontFamily, fontSize = 14.sp)
                            Switch(
                                checked = silentDuringPrayer,
                                onCheckedChange = { silentDuringPrayer = it },
                                modifier = Modifier.testTag("switch_silent_prayer")
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("dialog_cancel_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("إلغاء", fontFamily = CairoFontFamily)
                    }

                    Button(
                        onClick = {
                            val finalTitle = title.ifBlank { "تذكير بذكر الله" }
                            val finalMessage = message.ifBlank { "اترك ما يشغلك واذكر الله" }
                            val updated = initialReminder.copy(
                                title = finalTitle,
                                message = finalMessage,
                                type = selectedType,
                                intervalMinutes = intervalMinutes.coerceAtLeast(1),
                                startHour = startHour,
                                endHour = endHour,
                                silentDuringSleep = silentDuringSleep,
                                silentDuringPrayer = silentDuringPrayer,
                                isEnabled = true
                            )
                            onSave(updated)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("dialog_save_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = IslamicGreenPrimary)
                    ) {
                        Text("حفظ التذكير", fontFamily = CairoFontFamily, color = androidx.compose.ui.graphics.Color.White)
                    }
                }
            }
        }
    }
}
