package com.example.garmentlifecycletrackingapp

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { UsageSummaryApp() }
    }
}

data class PiSummaryResponse(
    val device_id: String?,
    val state: String?,
    val wears: Int?,
    val washes: Int?
)

interface PiApiService {
    @GET("summary")
    suspend fun getSummary(): PiSummaryResponse
}

object PiApi {
    private val retrofit = Retrofit.Builder()
        .baseUrl("http://172.20.10.12:5000/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val service: PiApiService = retrofit.create(PiApiService::class.java)
}

private val BgTop = Color(0xFF0F1226)
private val BgBottom = Color(0xFF1B1F3A)
private val Surface1 = Color(0xFF20254A)
private val Surface2 = Color(0xFF2A2F5C)
private val TextHi = Color(0xFFF5F6FF)
private val TextLo = Color(0xFFB7BCE0)

private fun stateColors(state: String): Pair<Color, Color> = when (state.lowercase()) {
    "worn" -> Color(0xFF34D399) to Color(0xFF065F46)
    "washed" -> Color(0xFF60A5FA) to Color(0xFF1E3A8A)
    "stored" -> Color(0xFFFCD34D) to Color(0xFF78350F)
    else -> Color(0xFF94A3B8) to Color(0xFF1F2937)
}

@Composable
fun UsageSummaryApp() {
    MaterialTheme(colorScheme = darkColorScheme()) {
        MainDashboardScreen()
    }
}

@Composable
fun MainDashboardScreen() {
    val context = LocalContext.current
    val prefs = remember {
        context.getSharedPreferences("privacy_settings", Context.MODE_PRIVATE)
    }

    var dataCollectionEnabled by remember {
        mutableStateOf(prefs.getBoolean("data_collection_enabled", true))
    }
    var keepDataOnDevice by remember {
        mutableStateOf(prefs.getBoolean("keep_data_on_device", true))
    }
    var shareUsageSummaries by remember {
        mutableStateOf(prefs.getBoolean("share_usage_summaries", true))
    }

    var piState by remember { mutableStateOf<PiSummaryResponse?>(null) }
    var errorText by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(dataCollectionEnabled) {
        while (true) {
            if (dataCollectionEnabled) {
                try {
                    piState = PiApi.service.getSummary()
                    errorText = null
                } catch (e: Exception) {
                    errorText = e.message
                }
            } else {
                errorText = null
            }
            delay(2000)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(BgTop, BgBottom)))
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            contentPadding = PaddingValues(vertical = 24.dp)
        ) {
            item {
                HeaderSection(
                    connected = errorText == null && piState != null && dataCollectionEnabled,
                    dataCollectionEnabled = dataCollectionEnabled,
                    keepDataOnDevice = keepDataOnDevice,
                    shareUsageSummaries = shareUsageSummaries,
                    onDataCollectionChanged = {
                        dataCollectionEnabled = it
                        prefs.edit().putBoolean("data_collection_enabled", it).apply()
                    },
                    onKeepDataChanged = {
                        keepDataOnDevice = it
                        prefs.edit().putBoolean("keep_data_on_device", it).apply()
                    },
                    onShareSummariesChanged = {
                        shareUsageSummaries = it
                        prefs.edit().putBoolean("share_usage_summaries", it).apply()
                    }
                )
            }

            item {
                CurrentGarmentStateCard(
                    garmentName = "White T-shirt",
                    piState = piState,
                    errorText = if (dataCollectionEnabled) errorText else null,
                    dataCollectionEnabled = dataCollectionEnabled
                )
            }
        }
    }
}

@Composable
private fun HeaderSection(
    connected: Boolean,
    dataCollectionEnabled: Boolean,
    keepDataOnDevice: Boolean,
    shareUsageSummaries: Boolean,
    onDataCollectionChanged: (Boolean) -> Unit,
    onKeepDataChanged: (Boolean) -> Unit,
    onShareSummariesChanged: (Boolean) -> Unit
) {
    var showPrivacy by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text("Garment Tracker", color = TextHi, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text("Live rental usage summary", color = TextLo, fontSize = 14.sp)
        }

        TextButton(onClick = { showPrivacy = true }) {
            Text("Privacy")
        }

        ConnectionPill(connected)
    }

    if (showPrivacy) {
        AlertDialog(
            onDismissRequest = { showPrivacy = false },
            title = { Text("Privacy Settings") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    PrivacyToggleRow(
                        title = "Data collection",
                        subtitle = "Allow the app to collect garment tracking data",
                        checked = dataCollectionEnabled,
                        onCheckedChange = onDataCollectionChanged
                    )

                    PrivacyToggleRow(
                        title = "Keep data on this device",
                        subtitle = "Store detailed usage data only on this phone",
                        checked = keepDataOnDevice,
                        onCheckedChange = onKeepDataChanged
                    )

                    PrivacyToggleRow(
                        title = "Share usage summaries",
                        subtitle = "Allow sending wear/wash counts. No personal data.",
                        checked = shareUsageSummaries,
                        onCheckedChange = onShareSummariesChanged
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showPrivacy = false }) {
                    Text("Done")
                }
            }
        )
    }
}

@Composable
fun PrivacyToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            Text(subtitle, fontSize = 12.sp, color = TextLo)
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
private fun ConnectionPill(connected: Boolean) {
    val color = if (connected) Color(0xFF34D399) else Color(0xFFF87171)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        PulsingDot(color)
        Spacer(Modifier.width(6.dp))
        Text(
            if (connected) "Live" else "Offline",
            color = color,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun PulsingDot(color: Color) {
    val transition = rememberInfiniteTransition(label = "pulse")
    val scale by transition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(contentAlignment = Alignment.Center) {
        Box(
            Modifier
                .size((10 * scale).dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.35f))
        )
        Box(
            Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
    }
}

@Composable
fun CurrentGarmentStateCard(
    garmentName: String,
    piState: PiSummaryResponse?,
    errorText: String?,
    dataCollectionEnabled: Boolean
) {
    val state = if (dataCollectionEnabled) piState?.state ?: "—" else "privacy off"
    val (accent, _) = stateColors(state)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Surface1)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text("Current garment", color = TextLo, fontSize = 12.sp)
            Text(garmentName, color = TextHi, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)

            if (errorText != null) {
                ErrorBlock(errorText)
            } else {
                StateBadge(state, accent)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    SummaryTile("Wears", if (dataCollectionEnabled) piState?.wears ?: 0 else 0, "👚", Modifier.weight(1f))
                    SummaryTile("Washes", if (dataCollectionEnabled) piState?.washes ?: 0 else 0, "🫧", Modifier.weight(1f))
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Refresh, contentDescription = null, tint = TextLo, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        if (dataCollectionEnabled) "Auto-refreshing every 2s" else "Data collection is off",
                        color = TextLo,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun StateBadge(state: String, accent: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(accent.copy(alpha = 0.18f))
            .padding(horizontal = 18.dp, vertical = 14.dp)
    ) {
        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = accent)
        Spacer(Modifier.width(10.dp))
        Text(state.uppercase(), color = TextHi, fontSize = 20.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ErrorBlock(message: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFF87171).copy(alpha = 0.12f))
            .padding(16.dp)
    ) {
        Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = Color(0xFFF87171))
        Spacer(Modifier.width(10.dp))
        Text("Connection error: $message", color = TextLo, fontSize = 12.sp)
    }
}

@Composable
fun SummaryTile(title: String, value: Int, emoji: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Surface2)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text("$emoji $title", color = TextLo, fontSize = 13.sp)

            AnimatedContent(
                targetState = value,
                transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(200)) },
                label = "value"
            ) { v ->
                Text(v.toString(), color = TextHi, fontSize = 34.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}