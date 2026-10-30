package com.example.ui

import android.widget.Toast
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.AppSettingsEntity
import com.example.data.quran.QuranData
import com.example.data.quran.RecitersData
import com.example.friday.FridaySettingsDialog
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settings: AppSettingsEntity,
    onUpdateTheme: (String) -> Unit,
    onToggleMuteAll: () -> Unit,
    onNavigateToPrayer: () -> Unit,
    onOpenPrivacy: () -> Unit,
    onOpenAddReminder: () -> Unit,
    onUpdateFontSize: (Int) -> Unit,
    onOpenAudioDownloads: () -> Unit,
    onOpenQibla: () -> Unit,
    onOpenCitySelection: () -> Unit,
    onOpenMadinaQuran: () -> Unit,
    onSelectReciter: (String) -> Unit,
    onResetLocationSettings: () -> Unit,
    onClearCache: () -> Unit,
    onOpenFriday: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showReciterSheet by remember { mutableStateOf(false) }
    var showFridaySettingsDialog by remember { mutableStateOf(false) }
    val currentReciter = remember(settings.selectedReciterId) {
        RecitersData.getReciterById(settings.selectedReciterId)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("settings_screen_column"),
        contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 96.dp)
    ) {
        item {
            Text(
                text = "الإعدادات العامة",
                style = MaterialTheme.typography.titleLarge,
                fontFamily = CairoFontFamily,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Location & Qibla Shortcut Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_location_settings"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.LocationOn, contentDescription = "الموقع", tint = IslamicGreenPrimary)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "الموقع والمدينة الحالية",
                                style = MaterialTheme.typography.titleMedium,
                                fontFamily = CairoFontFamily,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${settings.cityName} • ${settings.countryName}",
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = CairoFontFamily,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onOpenCitySelection,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("اختيار المدينة يدويًا", fontFamily = CairoFontFamily, fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = onOpenQibla,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Explore, contentDescription = "القبلة", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("بوصلة القبلة", fontFamily = CairoFontFamily, fontSize = 12.sp)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Recitations & Downloads Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_recitations_settings"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.RecordVoiceOver, contentDescription = "التلاوة", tint = IslamicGold)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "القارئ والتلاوات",
                                style = MaterialTheme.typography.titleMedium,
                                fontFamily = CairoFontFamily,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "القارئ الافتراضي: ${currentReciter.nameAr}",
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = CairoFontFamily,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { showReciterSheet = true },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("تغيير القارئ", fontFamily = CairoFontFamily, fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = onOpenAudioDownloads,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.DownloadForOffline, contentDescription = "تحميل", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("إدارة التحميلات", fontFamily = CairoFontFamily, fontSize = 12.sp)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Theme Mode Selector
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_theme_settings"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.DarkMode, contentDescription = "المظهر", tint = IslamicGreenPrimary)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "مظهر التطبيق",
                            style = MaterialTheme.typography.titleMedium,
                            fontFamily = CairoFontFamily,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = settings.themeMode == "SYSTEM",
                            onClick = { onUpdateTheme("SYSTEM") },
                            label = { Text("تلقائي", fontFamily = CairoFontFamily) },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = settings.themeMode == "LIGHT",
                            onClick = { onUpdateTheme("LIGHT") },
                            label = { Text("فاتح", fontFamily = CairoFontFamily) },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = settings.themeMode == "DARK",
                            onClick = { onUpdateTheme("DARK") },
                            label = { Text("داكن", fontFamily = CairoFontFamily) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Reminders & Notifications Control
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_notification_settings"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Notifications, contentDescription = "الإشعارات", tint = IslamicGold)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "التنبيهات والتذكيرات",
                            style = MaterialTheme.typography.titleMedium,
                            fontFamily = CairoFontFamily,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("تفعيل التذكيرات الدورية", fontFamily = CairoFontFamily, fontWeight = FontWeight.SemiBold)
                            Text("تشغيل أو كتم جميع التذكيرات المنبهة", fontFamily = CairoFontFamily, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = !settings.allRemindersMuted,
                            onCheckedChange = { onToggleMuteAll() }
                        )
                    }

                    Divider(modifier = Modifier.padding(vertical = 10.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                    OutlinedButton(
                        onClick = onOpenAddReminder,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.AddAlarm, contentDescription = "إضافة تذكير", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("إنشاء جدول تذكير مخصص جديد", fontFamily = CairoFontFamily)
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Friday Settings & Reminders Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_friday_settings"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Event, contentDescription = "يوم الجمعة", tint = IslamicGreenPrimary)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "إعدادات وتذكيرات يوم الجمعة",
                                style = MaterialTheme.typography.titleMedium,
                                fontFamily = CairoFontFamily,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "تخصيص تنبيهات سورة الكهف، الصلاة على النبي ﷺ، وساعة الإجابة",
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = CairoFontFamily,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onOpenFriday,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.MenuBook, contentDescription = "فتح", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("قسم الجمعة", fontFamily = CairoFontFamily, fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = { showFridaySettingsDialog = true },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Tune, contentDescription = "تخصيص", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("تخصيص التنبيهات", fontFamily = CairoFontFamily, fontSize = 12.sp)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Quran Reading Preferences
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_quran_settings"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.FormatSize, contentDescription = "حجم الخط", tint = IslamicGreenPrimary)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "حجم خط القرآن والأذكار (${settings.quranFontSize}sp)",
                            style = MaterialTheme.typography.titleMedium,
                            fontFamily = CairoFontFamily,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Slider(
                        value = settings.quranFontSize.toFloat(),
                        onValueChange = { onUpdateFontSize(it.toInt()) },
                        valueRange = 20f..38f,
                        steps = 8,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = "﴿بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ﴾",
                        fontFamily = AmiriFontFamily,
                        fontSize = settings.quranFontSize.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Madinah Quran Integration Card (com.madarsoft.quran)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_madina_quran_link"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = IslamicGoldContainer.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Book, contentDescription = "مصحف المدينة", tint = IslamicGoldDark)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "تطبيق مصحف المدينة",
                                style = MaterialTheme.typography.titleMedium,
                                fontFamily = CairoFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = IslamicGoldOnContainer
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "يتيح لك فتح تطبيق مصحف المدينة المرتبط بحزمة (com.madarsoft.quran) المثبت على هاتفك أو الانتقال لصفحته الرسمية في متجر Google Play.",
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = CairoFontFamily,
                        color = TextPrimaryLight
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = onOpenMadinaQuran,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = IslamicGoldDark),
                        modifier = Modifier.testTag("btn_open_madina_settings")
                    ) {
                        Icon(Icons.Default.Launch, contentDescription = "فتح المصحف", modifier = Modifier.size(18.dp), tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("فتح في تطبيق مصحف المدينة", fontFamily = CairoFontFamily, color = Color.White)
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Reset and Clear Cache Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "الصيانة وإعادة الضبط",
                        style = MaterialTheme.typography.titleMedium,
                        fontFamily = CairoFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                onResetLocationSettings()
                                Toast.makeText(context, "تمت إعادة ضبط الموقع إلى مكة المكرمة", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("إعادة ضبط الموقع", fontFamily = CairoFontFamily, fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                onClearCache()
                                Toast.makeText(context, "تم مسح الملفات المؤقتة", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("مسح التخزين المؤقت", fontFamily = CairoFontFamily, fontSize = 12.sp)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Privacy Policy Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenPrivacy() }
                    .testTag("card_privacy_link"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Security, contentDescription = "الخصوصية والأمان", tint = IslamicGreenPrimary)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "الخصوصية وحماية البيانات",
                                style = MaterialTheme.typography.titleMedium,
                                fontFamily = CairoFontFamily,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "بياناتك محلية بالكامل، لا جمع ولا بيع لأي معلومات",
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = CairoFontFamily,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Icon(Icons.Default.ArrowBackIosNew, contentDescription = "عرض", modifier = Modifier.size(16.dp))
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // App Information & Version
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "رفيق المسلم • الإصدار 1.0.1",
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = CairoFontFamily,
                    color = MaterialTheme.colorScheme.outline
                )
                Text(
                    text = "صُمم بتصميم هادئ ومريح ومصادر شرعية موثوقة",
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = CairoFontFamily,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }

    // Reciters Bottom Sheet
    if (showReciterSheet) {
        ModalBottomSheet(onDismissRequest = { showReciterSheet = false }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "اختر القارئ الافتراضي",
                    style = MaterialTheme.typography.titleLarge,
                    fontFamily = CairoFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(12.dp))
                LazyColumn(modifier = Modifier.heightIn(max = 420.dp)) {
                    items(RecitersData.famousReciters) { reciter ->
                        val isSelected = reciter.id == settings.selectedReciterId
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
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

    if (showFridaySettingsDialog) {
        FridaySettingsDialog(
            onDismiss = { showFridaySettingsDialog = false }
        )
    }
}
