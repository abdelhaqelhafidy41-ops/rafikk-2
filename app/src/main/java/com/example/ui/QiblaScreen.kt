package com.example.ui

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.prayer.QiblaCalculator
import com.example.ui.theme.*
import kotlin.math.abs
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QiblaScreen(
    userLat: Double,
    userLng: Double,
    cityName: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    BackHandler { onBack() }

    val qiblaResult = remember(userLat, userLng) {
        QiblaCalculator.calculateQibla(userLat, userLng)
    }

    var azimuth by remember { mutableFloatStateOf(0f) }
    var sensorAccuracy by remember { mutableIntStateOf(SensorManager.SENSOR_STATUS_ACCURACY_HIGH) }
    var hasCompassSensor by remember { mutableStateOf(true) }
    var hasVibratedAlignment by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        val sm = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        val rotationSensor = sm?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        val accelSensor = sm?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val magSensor = sm?.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        if (rotationSensor == null && (accelSensor == null || magSensor == null)) {
            hasCompassSensor = false
        }

        val listener = object : SensorEventListener {
            val rotMatrix = FloatArray(9)
            val orientation = FloatArray(3)
            val lastAccel = FloatArray(3)
            val lastMag = FloatArray(3)
            var hasAccel = false
            var hasMag = false

            override fun onSensorChanged(event: SensorEvent) {
                if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
                    SensorManager.getRotationMatrixFromVector(rotMatrix, event.values)
                    SensorManager.getOrientation(rotMatrix, orientation)
                    val deg = Math.toDegrees(orientation[0].toDouble()).toFloat()
                    azimuth = (deg + 360f) % 360f
                } else if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                    System.arraycopy(event.values, 0, lastAccel, 0, 3)
                    hasAccel = true
                } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
                    System.arraycopy(event.values, 0, lastMag, 0, 3)
                    hasMag = true
                }

                if (event.sensor.type != Sensor.TYPE_ROTATION_VECTOR && hasAccel && hasMag) {
                    if (SensorManager.getRotationMatrix(rotMatrix, null, lastAccel, lastMag)) {
                        SensorManager.getOrientation(rotMatrix, orientation)
                        val deg = Math.toDegrees(orientation[0].toDouble()).toFloat()
                        azimuth = (deg + 360f) % 360f
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                sensorAccuracy = accuracy
            }
        }

        if (rotationSensor != null) {
            sm?.registerListener(listener, rotationSensor, SensorManager.SENSOR_DELAY_UI)
        } else {
            accelSensor?.let { sm?.registerListener(listener, it, SensorManager.SENSOR_DELAY_UI) }
            magSensor?.let { sm?.registerListener(listener, it, SensorManager.SENSOR_DELAY_UI) }
        }

        onDispose {
            sm?.unregisterListener(listener)
        }
    }

    // Relative needle angle to Kaaba
    val relativeAngle = if (hasCompassSensor) {
        (qiblaResult.qiblaAngle.toFloat() - azimuth + 360f) % 360f
    } else {
        qiblaResult.qiblaAngle.toFloat()
    }

    val animatedAngle by animateFloatAsState(
        targetValue = relativeAngle,
        animationSpec = spring(stiffness = 200f)
    )

    val isFacingQibla = abs(relativeAngle) < 3.5f || abs(relativeAngle - 360f) < 3.5f

    // Vibrate when exactly facing Qibla
    LaunchedEffect(isFacingQibla) {
        if (isFacingQibla && !hasVibratedAlignment) {
            hasVibratedAlignment = true
            try {
                val v = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    v?.vibrate(VibrationEffect.createOneShot(80, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    v?.vibrate(80)
                }
            } catch (_: Exception) {}
        } else if (!isFacingQibla) {
            hasVibratedAlignment = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "بوصلة القبلة المشرفة",
                        fontFamily = CairoFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("btn_back_qibla")) {
                        Icon(Icons.Default.ArrowForward, contentDescription = "رجوع")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp)
                .testTag("qibla_screen_column"),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Location & Target Summary
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "موقعك: $cityName",
                            style = MaterialTheme.typography.titleMedium,
                            fontFamily = CairoFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "زاوية القبلة: ${qiblaResult.qiblaAngle.roundToInt()}° (${qiblaResult.directionLabelAr})",
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = CairoFontFamily,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = IslamicGoldContainer
                    ) {
                        Text(
                            text = "${qiblaResult.distanceKm.roundToInt()} كم إلى مكة",
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = CairoFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = IslamicGoldOnContainer,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // Compass Dial View
            Box(
                modifier = Modifier
                    .size(280.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = if (isFacingQibla) {
                                listOf(IslamicGoldContainer, IslamicGreenContainer, IslamicGreenDark)
                            } else {
                                listOf(MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                            }
                        )
                    )
                    .border(
                        width = if (isFacingQibla) 4.dp else 2.dp,
                        color = if (isFacingQibla) IslamicGold else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Compass Cardinal Marks Canvas
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCompassCardinals()
                }

                // Rotating Needle Pointer
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .rotate(animatedAngle),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Kaaba Needle Tip (Pointing to Qibla)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(top = 18.dp)
                    ) {
                        Icon(
                            Icons.Default.Navigation,
                            contentDescription = "اتجاه القبلة",
                            tint = if (isFacingQibla) IslamicGold else IslamicGreenPrimary,
                            modifier = Modifier.size(44.dp)
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isFacingQibla) IslamicGold else IslamicGreenPrimary
                        ) {
                            Text(
                                text = "القبلة",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontFamily = CairoFontFamily,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    // Opposite Pointer (Tail)
                    Box(
                        modifier = Modifier
                            .padding(bottom = 20.dp)
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(IslamicRedHighlight)
                    )
                }

                // Center Hub with Status
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp,
                    modifier = Modifier.size(90.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "${qiblaResult.qiblaAngle.roundToInt()}°",
                            style = MaterialTheme.typography.titleMedium,
                            fontFamily = CairoFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = if (isFacingQibla) IslamicGold else MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = if (isFacingQibla) "أنت تواجه القبلة!" else "وجّه الهاتف",
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = CairoFontFamily,
                            fontSize = 10.sp,
                            color = if (isFacingQibla) IslamicGreenPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Bottom Diagnostics and Calibration Tips
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (!hasCompassSensor) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = IslamicGoldContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, contentDescription = "ملاحظة", tint = IslamicGoldDark)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "مستشعر البوصلة غير متوفر على جهازك. يُعرض اتجاه القبلة المحسوب فلكيًا (${qiblaResult.qiblaAngle.roundToInt()}° بالنسبة للشمال الحقيقي).",
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = CairoFontFamily,
                                color = IslamicGoldOnContainer
                            )
                        }
                    }
                } else if (sensorAccuracy <= SensorManager.SENSOR_STATUS_ACCURACY_LOW) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Refresh, contentDescription = "معايرة", tint = IslamicGold)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "دقة البوصلة منخفضة. يرجى تحريك الهاتف في الهواء على شكل رقم 8 لمعايرة المستشعر والابتعاد عن المعادن.",
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = CairoFontFamily
                            )
                        }
                    }
                } else {
                    Text(
                        text = "ضع الهاتف بشكل أفقي مستوٍ وابتعد عن الأجهزة الإلكترونية والمجالات المغناطيسية",
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = CairoFontFamily,
                        color = MaterialTheme.colorScheme.outline,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

private fun DrawScope.drawCompassCardinals() {
    val center = Offset(size.width / 2f, size.height / 2f)
    val radius = size.width / 2f - 12f

    // Draw ticks
    for (i in 0 until 360 step 30) {
        val angleRad = Math.toRadians(i.toDouble() - 90.0)
        val outerX = center.x + radius * kotlin.math.cos(angleRad).toFloat()
        val outerY = center.y + radius * kotlin.math.sin(angleRad).toFloat()
        val tickLen = if (i % 90 == 0) 14f else 8f
        val innerX = center.x + (radius - tickLen) * kotlin.math.cos(angleRad).toFloat()
        val innerY = center.y + (radius - tickLen) * kotlin.math.sin(angleRad).toFloat()

        drawLine(
            color = Color.Gray.copy(alpha = 0.5f),
            start = Offset(innerX, innerY),
            end = Offset(outerX, outerY),
            strokeWidth = if (i % 90 == 0) 2.5f else 1.5f
        )
    }
}
