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
import com.example.prayer.CityCoordinates
import com.example.prayer.CityInfo
import com.example.prayer.PrayerCalculator
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CitySelectionScreen(
    currentCityName: String,
    onSelectCity: (CityInfo) -> Unit,
    onSaveCustomCoordinates: (String, String, Double, Double, String) -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    var searchQuery by remember { mutableStateOf("") }
    var showCustomCoordDialog by remember { mutableStateOf(false) }

    val filteredCities = remember(searchQuery) {
        CityCoordinates.searchCities(searchQuery)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "اختيار المدينة والدولة يدويًا",
                        fontFamily = CairoFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("btn_back_city_selection")) {
                        Icon(Icons.Default.ArrowForward, contentDescription = "رجوع")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showCustomCoordDialog = true },
                        modifier = Modifier.testTag("btn_custom_coords")
                    ) {
                        Icon(Icons.Default.AddLocationAlt, contentDescription = "إدخال إحداثيات مخصصة", tint = IslamicGold)
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .testTag("city_selection_column")
        ) {
            // Search Field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("ابحث عن المدينة أو الدولة...", fontFamily = CairoFontFamily) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "بحث") },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "مسح")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
                    .testTag("city_search_input"),
                shape = RoundedCornerShape(16.dp),
                singleLine = true
            )

            // Info note
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = IslamicGreenContainer.copy(alpha = 0.5f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, contentDescription = "ملاحظة", tint = IslamicGreenPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "عند اختيار مدينتك يدويًا، سيتم اعتمادها لحساب جميع مواقيت الصلاة والقبلة دون الحاجة لطلب إشارة GPS.",
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = CairoFontFamily,
                        color = IslamicGreenOnContainer
                    )
                }
            }

            // Cities List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(filteredCities) { city ->
                    val isSelected = city.cityNameAr == currentCityName
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable {
                                onSelectCity(city)
                                onBack()
                            }
                            .testTag("city_item_${city.cityNameAr}"),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) IslamicGreenContainer else MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
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
                                    text = city.cityNameAr,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontFamily = CairoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) IslamicGreenPrimary else MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${city.countryNameAr} • ${city.timezoneId}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontFamily = CairoFontFamily,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "خط العرض: ${city.latitude}° ، خط الطول: ${city.longitude}°",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontFamily = CairoFontFamily,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }

                            if (isSelected) {
                                Icon(Icons.Default.CheckCircle, contentDescription = "المحدد", tint = IslamicGreenPrimary)
                            }
                        }
                    }
                }
            }
        }
    }

    // Custom Coordinates Dialog for rural or unlisted areas
    if (showCustomCoordDialog) {
        var customCity by remember { mutableStateOf("") }
        var customCountry by remember { mutableStateOf("") }
        var customLatText by remember { mutableStateOf("") }
        var customLngText by remember { mutableStateOf("") }
        var customTimezone by remember { mutableStateOf("Asia/Riyadh") }

        AlertDialog(
            onDismissRequest = { showCustomCoordDialog = false },
            title = {
                Text("إدخال إحداثيات مخصصة", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = customCity,
                        onValueChange = { customCity = it },
                        label = { Text("اسم المدينة / القرية", fontFamily = CairoFontFamily) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = customCountry,
                        onValueChange = { customCountry = it },
                        label = { Text("الدولة", fontFamily = CairoFontFamily) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = customLatText,
                        onValueChange = { customLatText = it },
                        label = { Text("خط العرض (مثلاً: 24.7136)", fontFamily = CairoFontFamily) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = customLngText,
                        onValueChange = { customLngText = it },
                        label = { Text("خط الطول (مثلاً: 46.6753)", fontFamily = CairoFontFamily) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val lat = customLatText.toDoubleOrNull() ?: 21.4225
                        val lng = customLngText.toDoubleOrNull() ?: 39.8262
                        val cName = customCity.ifBlank { "موقع مخصص" }
                        val cCountry = customCountry.ifBlank { "مخصص" }
                        onSaveCustomCoordinates(cName, cCountry, lat, lng, customTimezone)
                        showCustomCoordDialog = false
                        onBack()
                    }
                ) {
                    Text("حفظ واعتماد", fontFamily = CairoFontFamily)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomCoordDialog = false }) {
                    Text("إلغاء", fontFamily = CairoFontFamily)
                }
            }
        )
    }
}
