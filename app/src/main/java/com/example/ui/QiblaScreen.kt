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
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.prayer.QiblaCalculator
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

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

    // Helper for shortest angular delta between angles (-180..180)
    fun shortestAngleDiff(target: Float, current: Float): Float {
        var diff = (target - current) % 360f
        if (diff > 180f) diff -= 360f
        if (diff < -180f) diff += 360f
        return diff
    }

    var smoothedAzimuth by remember { mutableFloatStateOf(0f) }
    var sensorAccuracy by remember { mutableIntStateOf(SensorManager.SENSOR_STATUS_ACCURACY_HIGH) }
    var hasCompassSensor by remember { mutableStateOf(true) }
    var hasReceivedSensorEvent by remember { mutableStateOf(false) }
    var hasVibratedAlignment by remember { mutableStateOf(false) }

    // Multi-sensor registration to prevent freezing on any Android device / chipset
    DisposableEffect(Unit) {
        val sm = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        val rotationVectorSensor = sm?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        val geomagVectorSensor = sm?.getDefaultSensor(Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR)
        val accelSensor = sm?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val magSensor = sm?.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        @Suppress("DEPRECATION")
        val orientationSensor = sm?.getDefaultSensor(Sensor.TYPE_ORIENTATION)

        if (rotationVectorSensor == null && geomagVectorSensor == null && (accelSensor == null || magSensor == null) && orientationSensor == null) {
            hasCompassSensor = false
        }

        val listener = object : SensorEventListener {
            val rotMatrix = FloatArray(9)
            val outMatrix = FloatArray(9)
            val orientation = FloatArray(3)
            val lastAccel = FloatArray(3)
            val lastMag = FloatArray(3)
            var hasAccel = false
            var hasMag = false

            override fun onSensorChanged(event: SensorEvent) {
                hasReceivedSensorEvent = true
                var newDeg: Float? = null

                when (event.sensor.type) {
                    Sensor.TYPE_ROTATION_VECTOR, Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR -> {
                        try {
                            val vec = if (event.values.size > 4) {
                                FloatArray(4).apply { System.arraycopy(event.values, 0, this, 0, 4) }
                            } else {
                                event.values
                            }
                            SensorManager.getRotationMatrixFromVector(rotMatrix, vec)
                            SensorManager.getOrientation(rotMatrix, orientation)
                            // Remap if phone is tilted upright (> 45 degrees pitch)
                            val pitch = orientation[1]
                            if (abs(pitch) > 0.785f) {
                                SensorManager.remapCoordinateSystem(
                                    rotMatrix,
                                    SensorManager.AXIS_X,
                                    SensorManager.AXIS_Z,
                                    outMatrix
                                )
                                SensorManager.getOrientation(outMatrix, orientation)
                            }
                            val rad = orientation[0].toDouble()
                            newDeg = ((Math.toDegrees(rad) + 360.0) % 360.0).toFloat()
                        } catch (_: Exception) {}
                    }
                    Sensor.TYPE_ACCELEROMETER -> {
                        for (i in 0..2) {
                            lastAccel[i] = lastAccel[i] + 0.25f * (event.values[i] - lastAccel[i])
                        }
                        hasAccel = true
                    }
                    Sensor.TYPE_MAGNETIC_FIELD -> {
                        for (i in 0..2) {
                            lastMag[i] = lastMag[i] + 0.25f * (event.values[i] - lastMag[i])
                        }
                        hasMag = true
                    }
                    @Suppress("DEPRECATION")
                    Sensor.TYPE_ORIENTATION -> {
                        newDeg = (event.values[0] + 360f) % 360f
                    }
                }

                // If rotation vector didn't provide a degree yet, compute from accel + mag
                if (newDeg == null && hasAccel && hasMag) {
                    try {
                        if (SensorManager.getRotationMatrix(rotMatrix, null, lastAccel, lastMag)) {
                            SensorManager.getOrientation(rotMatrix, orientation)
                            val pitch = orientation[1]
                            if (abs(pitch) > 0.785f) {
                                SensorManager.remapCoordinateSystem(
                                    rotMatrix,
                                    SensorManager.AXIS_X,
                                    SensorManager.AXIS_Z,
                                    outMatrix
                                )
                                SensorManager.getOrientation(outMatrix, orientation)
                            }
                            val rad = orientation[0].toDouble()
                            newDeg = ((Math.toDegrees(rad) + 360.0) % 360.0).toFloat()
                        }
                    } catch (_: Exception) {}
                }

                newDeg?.let { deg ->
                    val diff = shortestAngleDiff(deg, smoothedAzimuth)
                    val factor = if (abs(diff) < 1.0f) 0.08f else 0.22f
                    smoothedAzimuth = (smoothedAzimuth + diff * factor + 360f) % 360f
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                sensorAccuracy = accuracy
            }
        }

        // Register all available sensors simultaneously for seamless fallback
        rotationVectorSensor?.let { sm?.registerListener(listener, it, SensorManager.SENSOR_DELAY_UI) }
        geomagVectorSensor?.let { sm?.registerListener(listener, it, SensorManager.SENSOR_DELAY_UI) }
        accelSensor?.let { sm?.registerListener(listener, it, SensorManager.SENSOR_DELAY_UI) }
        magSensor?.let { sm?.registerListener(listener, it, SensorManager.SENSOR_DELAY_UI) }
        orientationSensor?.let { sm?.registerListener(listener, it, SensorManager.SENSOR_DELAY_UI) }

        onDispose {
            sm?.unregisterListener(listener)
        }
    }

    // Check if sensors are not emitting (e.g. streaming emulator) after 1.5 seconds
    LaunchedEffect(Unit) {
        delay(1500)
        if (!hasReceivedSensorEvent) {
            hasCompassSensor = false
        }
    }

    // Relative angle: how much the needle needs to point from top towards Kaaba
    val targetRelativeAngle = shortestAngleDiff(qiblaResult.qiblaAngle.toFloat(), smoothedAzimuth)

    // Unwrapped continuous angles to prevent 360-degree spinning glitch
    var unwrappedDialAngle by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(smoothedAzimuth) {
        val targetDial = -smoothedAzimuth
        val diff = shortestAngleDiff(targetDial, unwrappedDialAngle)
        unwrappedDialAngle += diff
    }
    val animatedDialAngle by animateFloatAsState(
        targetValue = unwrappedDialAngle,
        animationSpec = spring(dampingRatio = 0.85f, stiffness = 160f),
        label = "dial_rotation"
    )

    var unwrappedNeedleAngle by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(targetRelativeAngle) {
        val diff = shortestAngleDiff(targetRelativeAngle, unwrappedNeedleAngle)
        unwrappedNeedleAngle += diff
    }
    val animatedNeedleAngle by animateFloatAsState(
        targetValue = unwrappedNeedleAngle,
        animationSpec = spring(dampingRatio = 0.85f, stiffness = 160f),
        label = "needle_rotation"
    )

    // Alignment threshold: within +/- 4.5 degrees of Qibla
    val isFacingQibla = abs(targetRelativeAngle) <= 4.5f

    // Vibration on exact alignment
    LaunchedEffect(isFacingQibla) {
        if (isFacingQibla && !hasVibratedAlignment) {
            hasVibratedAlignment = true
            try {
                val v = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    v?.vibrate(VibrationEffect.createOneShot(75, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    v?.vibrate(75)
                }
            } catch (_: Exception) {}
        } else if (!isFacingQibla) {
            hasVibratedAlignment = false
        }
    }

    val glowColor by animateColorAsState(
        targetValue = if (isFacingQibla) IslamicGold else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
        label = "qibla_glow"
    )

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
                .padding(horizontal = 16.dp, vertical = 10.dp)
                .testTag("qibla_screen_column"),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Location and Angle Summary Card
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = IslamicGreenPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = cityName,
                                style = MaterialTheme.typography.titleMedium,
                                fontFamily = CairoFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "اتجاه القبلة: ${qiblaResult.qiblaAngle.roundToInt()}° (${qiblaResult.directionLabelAr})",
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

            // Central Compass Container
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                // Top Heading Indicator Notch (indicates where the phone is currently pointing)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(bottom = 6.dp)
                ) {
                    Icon(
                        Icons.Default.ArrowDropDown,
                        contentDescription = "أمام الهاتف",
                        tint = if (isFacingQibla) IslamicGold else IslamicGreenPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                    Text(
                        text = "مقدمة الهاتف",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        fontFamily = CairoFontFamily,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Main Compass Dial Box
                Box(
                    modifier = Modifier
                        .size(280.dp)
                        .clip(CircleShape)
                        .shadow(if (isFacingQibla) 16.dp else 4.dp, CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = if (isFacingQibla) {
                                    listOf(IslamicGoldContainer.copy(alpha = 0.5f), IslamicGreenContainer, IslamicGreenDark)
                                } else {
                                    listOf(
                                        MaterialTheme.colorScheme.surface,
                                        MaterialTheme.colorScheme.surfaceVariant,
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                                    )
                                }
                            )
                        )
                        .border(
                            width = if (isFacingQibla) 4.dp else 2.dp,
                            color = glowColor,
                            shape = CircleShape
                        )
                        .pointerInput(hasCompassSensor) {
                            // Allows manual drag rotation if compass sensor is unavailable (e.g., emulator)
                            if (!hasCompassSensor) {
                                detectDragGestures { change, dragAmount ->
                                    change.consume()
                                    smoothedAzimuth = (smoothedAzimuth - dragAmount.x * 0.5f + 360f) % 360f
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    // Rotating Dial Canvas (Rotates with -smoothedAzimuth so North points to real physical North)
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .rotate(animatedDialAngle)
                    ) {
                        drawCompassDial(
                            qiblaAngle = qiblaResult.qiblaAngle.toFloat(),
                            isFacing = isFacingQibla
                        )
                    }

                    // Rotating Kaaba Pointer Needle (Directly points towards Kaaba relative to the phone)
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .rotate(animatedNeedleAngle),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Kaaba Needle Head (Tip)
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(top = 16.dp)
                        ) {
                            Icon(
                                Icons.Default.Navigation,
                                contentDescription = "سهم القبلة",
                                tint = if (isFacingQibla) IslamicGold else IslamicGreenPrimary,
                                modifier = Modifier.size(38.dp)
                            )
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isFacingQibla) IslamicGold else IslamicGreenPrimary,
                                shadowElevation = 3.dp
                            ) {
                                Text(
                                    text = "الكعبة",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 11.sp,
                                    color = Color.White,
                                    fontFamily = CairoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }

                        // Counterweight tail (South end of needle)
                        Box(
                            modifier = Modifier
                                .padding(bottom = 22.dp)
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(IslamicRedHighlight)
                        )
                    }

                    // Center Hub with Live Angle & Status
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 8.dp,
                        shadowElevation = 6.dp,
                        modifier = Modifier.size(90.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "${smoothedAzimuth.roundToInt()}°",
                                style = MaterialTheme.typography.titleMedium,
                                fontFamily = CairoFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = if (isFacingQibla) IslamicGold else MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = if (isFacingQibla) "تواجه القبلة!" else "${qiblaResult.qiblaAngle.roundToInt()}° قبلة",
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = CairoFontFamily,
                                fontSize = 10.sp,
                                fontWeight = if (isFacingQibla) FontWeight.Bold else FontWeight.Normal,
                                color = if (isFacingQibla) IslamicGreenPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Directional Guidance Banner (Tells user exactly which way to turn the phone)
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isFacingQibla) IslamicGreenContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (isFacingQibla) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = IslamicGreenPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "أنت تواجه القبلة المشرفة بدقة (تقبل الله)",
                            style = MaterialTheme.typography.titleSmall,
                            fontFamily = CairoFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = IslamicGreenPrimary
                        )
                    } else {
                        val diff = targetRelativeAngle
                        val diffAbs = abs(diff).roundToInt()
                        val turnInstruction = if (diff > 0) {
                            "أدر الهاتف يميناً بمقدار $diffAbs°"
                        } else {
                            "أدر الهاتف يساراً بمقدار $diffAbs°"
                        }
                        Icon(
                            if (diff > 0) Icons.Default.RotateRight else Icons.Default.RotateLeft,
                            contentDescription = null,
                            tint = IslamicGoldDark
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = turnInstruction,
                            style = MaterialTheme.typography.titleSmall,
                            fontFamily = CairoFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Diagnostic & Calibration Tip
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (!hasCompassSensor) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = IslamicGoldContainer.copy(alpha = 0.7f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.TouchApp, contentDescription = null, tint = IslamicGoldDark)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "مستشعر البوصلة المغناطيسية غير متوفر في هذا الجهاز/المحاكي. اتجاه القبلة فلكيًا هو ${qiblaResult.qiblaAngle.roundToInt()}°. يمكنك سحب القرص بيدك للاختبار.",
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = CairoFontFamily,
                                color = IslamicGoldOnContainer,
                                fontSize = 11.sp
                            )
                        }
                    }
                } else if (sensorAccuracy <= SensorManager.SENSOR_STATUS_ACCURACY_LOW) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Refresh, contentDescription = "معايرة", tint = IslamicGold)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "يرجى تحريك الهاتف في الهواء على شكل الرقم (8) لمعايرة مستشعر البوصلة والابتعاد عن الأجهزة الإلكترونية والمعادن.",
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = CairoFontFamily,
                                fontSize = 11.sp
                            )
                        }
                    }
                } else {
                    Text(
                        text = "ضع الهاتف مستوياً في راحة يدك واجعله أفقياً للإشارة الدقيقة للكعبة المشرفة",
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = CairoFontFamily,
                        color = MaterialTheme.colorScheme.outline,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

/**
 * Draws the rotating compass dial with Arabic Cardinal points and the Holy Kaaba marker.
 */
private fun DrawScope.drawCompassDial(qiblaAngle: Float, isFacing: Boolean) {
    val center = Offset(size.width / 2f, size.height / 2f)
    val radius = size.width / 2f - 12f

    // 1. Draw 360-degree ticks
    for (i in 0 until 360 step 15) {
        val angleRad = Math.toRadians(i.toDouble() - 90.0)
        val outerX = center.x + radius * cos(angleRad).toFloat()
        val outerY = center.y + radius * sin(angleRad).toFloat()

        val isMajor = i % 90 == 0
        val isMedium = i % 30 == 0
        val tickLen = if (isMajor) 14f else if (isMedium) 9f else 5f

        val innerX = center.x + (radius - tickLen) * cos(angleRad).toFloat()
        val innerY = center.y + (radius - tickLen) * sin(angleRad).toFloat()

        val tickColor = when {
            i == 0 -> Color(0xFFE53935) // North is Red
            isMajor -> Color.Gray
            else -> Color.Gray.copy(alpha = 0.4f)
        }

        drawLine(
            color = tickColor,
            start = Offset(innerX, innerY),
            end = Offset(outerX, outerY),
            strokeWidth = if (isMajor) 2.5f else 1.2f
        )
    }

    // 2. Draw Cardinal Labels (شمال N, شرق E, جنوب S, غرب W) using native canvas
    val cardinals = listOf(
        Pair(0, "ش"),   // North (شمال)
        Pair(90, "ق"),  // East (شرق)
        Pair(180, "ج"), // South (جنوب)
        Pair(270, "غ")  // West (غرب)
    )

    val paint = android.graphics.Paint().apply {
        isAntiAlias = true
        textSize = 34f
        textAlign = android.graphics.Paint.Align.CENTER
        typeface = android.graphics.Typeface.DEFAULT_BOLD
    }

    drawContext.canvas.nativeCanvas.apply {
        for ((deg, label) in cardinals) {
            val rad = Math.toRadians(deg.toDouble() - 90.0)
            val textDist = radius - 28f
            val x = center.x + textDist * cos(rad).toFloat()
            val y = center.y + textDist * sin(rad).toFloat() + 12f

            paint.color = if (deg == 0) android.graphics.Color.parseColor("#E53935") else android.graphics.Color.GRAY
            drawText(label, x, y, paint)
        }
    }

    // 3. Draw Golden Kaaba Landmark on the Dial at exactly qiblaAngle
    val qiblaRad = Math.toRadians(qiblaAngle.toDouble() - 90.0)
    val kaabaDist = radius - 18f
    val kaabaX = center.x + kaabaDist * cos(qiblaRad).toFloat()
    val kaabaY = center.y + kaabaDist * sin(qiblaRad).toFloat()

    // Kaaba outer circle
    drawCircle(
        color = if (isFacing) IslamicGold else IslamicGreenPrimary,
        radius = 9f,
        center = Offset(kaabaX, kaabaY)
    )
    drawCircle(
        color = Color.White,
        radius = 4f,
        center = Offset(kaabaX, kaabaY)
    )
}
