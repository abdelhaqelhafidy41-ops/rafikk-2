package com.example.ui

import android.content.Context
import android.location.Geocoder
import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.prayer.CityCoordinates
import com.example.prayer.CityInfo
import com.example.prayer.PrayerCalculator
import com.example.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CitySelectionScreen(
    currentCityName: String,
    onSelectCity: (CityInfo) -> Unit,
    onSaveCustomCoordinates: (String, String, Double, Double, String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    BackHandler { onBack() }

    var searchQuery by remember { mutableStateOf("") }
    var showCustomCoordDialog by remember { mutableStateOf(false) }
    var isGeocodingLoading by remember { mutableStateOf(false) }
    var globalSearchResults by remember { mutableStateOf<List<CityInfo>>(emptyList()) }

    val filteredLocalCities = remember(searchQuery) {
        CityCoordinates.searchCities(searchQuery)
    }

    fun searchWorldwideWithGeocoder(query: String) {
        if (query.isBlank()) return
        isGeocodingLoading = true
        coroutineScope.launch(Dispatchers.IO) {
            try {
                if (Geocoder.isPresent()) {
                    val geocoder = Geocoder(context, Locale("ar"))
                    val addresses = geocoder.getFromLocationName(query, 8)
                    val results = addresses?.mapNotNull { addr ->
                        val cityName = addr.locality ?: addr.subAdminArea ?: addr.adminArea ?: query
                        val countryName = addr.countryName ?: "موقع عالمي"
                        CityInfo(
                            cityNameAr = cityName,
                            countryNameAr = countryName,
                            latitude = addr.latitude,
                            longitude = addr.longitude,
                            timezoneId = java.util.TimeZone.getDefault().id,
                            defaultMethod = PrayerCalculator.CalculationMethod.MWL
                        )
                    } ?: emptyList()

                    withContext(Dispatchers.Main) {
                        isGeocodingLoading = false
                        globalSearchResults = results
                        if (results.isEmpty()) {
                            Toast.makeText(context, "لم يتم العثور على نتائج عالمية لهذا الاسم، يمكنك إضافته يدويًا بالزر أعلاه", Toast.LENGTH_LONG).show()
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        isGeocodingLoading = false
                        Toast.makeText(context, "خدمة خرائط النظام غير متوفرة، يمكنك إدخال الإحداثيات يدويًا", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (_: Exception) {
                withContext(Dispatchers.Main) {
                    isGeocodingLoading = false
                    Toast.makeText(context, "تعذر البحث عبر الإنترنت، يرجى التحقق من الاتصال أو استخدام الإضافة اليدوية", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "تحديد المدينة أو القرية",
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
            // Search Input Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = {
                        searchQuery = it
                        if (it.isBlank()) globalSearchResults = emptyList()
                    },
                    placeholder = { Text("ابحث عن أي مدينة أو قرية أو دولة...", fontFamily = CairoFontFamily, fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "بحث") },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = {
                                searchQuery = ""
                                globalSearchResults = emptyList()
                            }) {
                                Icon(Icons.Default.Clear, contentDescription = "مسح")
                            }
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("city_search_input"),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.width(8.dp))

                FilledTonalButton(
                    onClick = { searchWorldwideWithGeocoder(searchQuery) },
                    enabled = searchQuery.isNotBlank() && !isGeocodingLoading,
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp)
                ) {
                    if (isGeocodingLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.Public, contentDescription = "بحث عالمي", modifier = Modifier.size(18.dp))
                    }
                }
            }

            // Quick Add Custom Village / City Prompt
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = IslamicGoldContainer.copy(alpha = 0.6f),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showCustomCoordDialog = true }
                    .padding(bottom = 10.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.AddLocation, contentDescription = null, tint = IslamicGoldDark)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "هل قريتك أو منطقتك غير مدرجة؟",
                                style = MaterialTheme.typography.titleSmall,
                                fontFamily = CairoFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = IslamicGoldDark
                            )
                            Text(
                                text = "اضغط هنا لإدخال اسم قريتك وإحداثياتها يدويًا بسهولة",
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = CairoFontFamily,
                                color = IslamicGoldOnContainer
                            )
                        }
                    }
                    Icon(Icons.Default.ChevronLeft, contentDescription = null, tint = IslamicGoldDark)
                }
            }

            // Results List
            val displayList = if (globalSearchResults.isNotEmpty()) globalSearchResults else filteredLocalCities

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (globalSearchResults.isNotEmpty()) {
                    item {
                        Text(
                            text = "نتائج البحث العالمي للقرى والمدن:",
                            style = MaterialTheme.typography.titleSmall,
                            fontFamily = CairoFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = IslamicGreenPrimary,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }

                items(displayList) { city ->
                    val isSelected = city.cityNameAr == currentCityName
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
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
                                val nameDisplay = if (city.cityNameEn.isNotBlank()) "${city.cityNameAr} (${city.cityNameEn})" else city.cityNameAr
                                val countryDisplay = if (city.countryNameEn.isNotBlank()) "${city.countryNameAr} (${city.countryNameEn})" else city.countryNameAr

                                Text(
                                    text = nameDisplay,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontFamily = CairoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) IslamicGreenPrimary else MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "$countryDisplay • ${city.defaultMethod.displayNameAr}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontFamily = CairoFontFamily,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "الإحداثيات: ${String.format(Locale.US, "%.3f", city.latitude)}°, ${String.format(Locale.US, "%.3f", city.longitude)}° • ${city.timezoneId}",
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
        var customTimezone by remember { mutableStateOf(java.util.TimeZone.getDefault().id) }

        AlertDialog(
            onDismissRequest = { showCustomCoordDialog = false },
            title = {
                Text("إدخال مدينة أو قرية مخصصة", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = customCity,
                        onValueChange = { customCity = it },
                        label = { Text("اسم القرية / المدينة", fontFamily = CairoFontFamily) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = customCountry,
                        onValueChange = { customCountry = it },
                        label = { Text("الدولة / المحافظة", fontFamily = CairoFontFamily) },
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
                    OutlinedTextField(
                        value = customTimezone,
                        onValueChange = { customTimezone = it },
                        label = { Text("المنطقة الزمنية (مثلاً: Asia/Riyadh)", fontFamily = CairoFontFamily) },
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
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = IslamicGreenPrimary)
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
