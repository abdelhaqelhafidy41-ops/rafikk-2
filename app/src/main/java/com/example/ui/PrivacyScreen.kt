package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.CairoFontFamily
import com.example.ui.theme.IslamicGold
import com.example.ui.theme.IslamicGreenPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyScreen(
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "الخصوصية والأمان",
                        fontFamily = CairoFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("btn_back_privacy")) {
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
                .verticalScroll(rememberScrollState())
                .testTag("privacy_screen_content")
        ) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Security,
                            contentDescription = "أمان",
                            tint = IslamicGreenPrimary,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "ميثاق خصوصية رفيق المسلم",
                            style = MaterialTheme.typography.titleMedium,
                            fontFamily = CairoFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    val principles = listOf(
                        Pair("تخزين محلي ١٠٠٪ على هاتفك", "جميع تفضيلاتك، سجل قراءتك في المصحف، العلامات المرجعية، وجداول التذكيرات تُحفظ في قاعدة بيانات SQLite محلية داخل جهازك فقط."),
                        Pair("الموقع الجغرافي لحساب المواقيت فقط", "عند منح إذن الموقع، يُستخدم حصريًا لمعرفة خطوط الطول والعرض وحساب مواقيت الصلاة بدقة فلكية. لا يتم حفظ مسار تحركاتك أو إرسال إحداثياتك لأي جهة."),
                        Pair("العمل دون إنترنت وبدون حساب", "يمكنك الاستفادة من كامل وظائف التطبيق دون تسجيل دخول أو إنشاء حساب ودون الحاجة لاتصال بالإنترنت."),
                        Pair("صفر إعلانات ولا بيع للبيانات", "نحن ملتزمون بعدم تتبع المستخدمين، وعدم تضمين أي أدوات إعلانية أو بيع لأي بيانات تحت أي ظرف."),
                        Pair("مصادر شرعية موثوقة ومدققة", "نصوص القرآن الكريم بالرسم العثماني المعتمد، وجميع الأذكار مأخوذة من المصادر والكتب الحديثية المعتمدة (صحيح البخاري، صحيح مسلم، السنن الأربعة).")
                    )

                    principles.forEach { (title, desc) ->
                        Row(modifier = Modifier.padding(vertical = 8.dp)) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = "نقطة",
                                tint = IslamicGold,
                                modifier = Modifier
                                    .size(20.dp)
                                    .padding(top = 2.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontFamily = CairoFontFamily,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = desc,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontFamily = CairoFontFamily,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
