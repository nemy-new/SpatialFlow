package com.codetrio.overdrive.ui.settings

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.codetrio.overdrive.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerformanceSettingsScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("AppSettings", Context.MODE_PRIVATE) }

    var sync120Fps by remember { mutableStateOf(prefs.getBoolean("pref_opt_120fps_sync", true)) }
    var gpuHardwareLayer by remember { mutableStateOf(prefs.getBoolean("pref_opt_gpu_hardware_layer", true)) }
    var dynamicAudioPolling by remember { mutableStateOf(prefs.getBoolean("pref_opt_dynamic_audio_polling", true)) }
    var lowLatencyBuffer by remember { mutableStateOf(prefs.getBoolean("pref_opt_low_latency_buffer", true)) }
    var predictivePrecaching by remember { mutableStateOf(prefs.getBoolean("pref_opt_predictive_precaching", true)) }
    var thumbnailDownsampling by remember { mutableStateOf(prefs.getBoolean("pref_opt_thumbnail_downsampling", true)) }

    val resetToastMessage = stringResource(R.string.pref_perf_reset_toast)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = stringResource(R.string.pref_perf_title),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = stringResource(R.string.pref_perf_subtitle),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        sync120Fps = true
                        gpuHardwareLayer = true
                        dynamicAudioPolling = true
                        lowLatencyBuffer = true
                        predictivePrecaching = true
                        thumbnailDownsampling = true
                        prefs.edit()
                            .putBoolean("pref_opt_120fps_sync", true)
                            .putBoolean("pref_opt_gpu_hardware_layer", true)
                            .putBoolean("pref_opt_dynamic_audio_polling", true)
                            .putBoolean("pref_opt_low_latency_buffer", true)
                            .putBoolean("pref_opt_predictive_precaching", true)
                            .putBoolean("pref_opt_thumbnail_downsampling", true)
                            .apply()
                        Toast.makeText(context, resetToastMessage, Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.Rounded.RestartAlt, contentDescription = "Reset to Default")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                // Info banner
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Speed,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = stringResource(R.string.pref_perf_active_title),
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = stringResource(R.string.pref_perf_active_desc),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }

            // Section 1: Rendering & 120Hz Animation
            item {
                PerformanceSectionHeader(
                    title = stringResource(R.string.pref_perf_sec_rendering),
                    icon = Icons.Rounded.Animation
                )
            }

            item {
                PerformanceOptionCard(
                    title = stringResource(R.string.pref_opt_120fps_sync_title),
                    subtitle = stringResource(R.string.pref_opt_120fps_sync_sub),
                    description = stringResource(R.string.pref_opt_120fps_sync_desc),
                    icon = Icons.Rounded.Sync,
                    checked = sync120Fps,
                    onCheckedChange = {
                        sync120Fps = it
                        prefs.edit().putBoolean("pref_opt_120fps_sync", it).apply()
                    }
                )
            }

            item {
                PerformanceOptionCard(
                    title = stringResource(R.string.pref_opt_gpu_layer_title),
                    subtitle = stringResource(R.string.pref_opt_gpu_layer_sub),
                    description = stringResource(R.string.pref_opt_gpu_layer_desc),
                    icon = Icons.Rounded.Layers,
                    checked = gpuHardwareLayer,
                    onCheckedChange = {
                        gpuHardwareLayer = it
                        prefs.edit().putBoolean("pref_opt_gpu_hardware_layer", it).apply()
                    }
                )
            }

            // Section 2: Audio Engine & Battery
            item {
                PerformanceSectionHeader(
                    title = stringResource(R.string.pref_perf_sec_audio),
                    icon = Icons.Rounded.GraphicEq
                )
            }

            item {
                PerformanceOptionCard(
                    title = stringResource(R.string.pref_opt_dynamic_polling_title),
                    subtitle = stringResource(R.string.pref_opt_dynamic_polling_sub),
                    description = stringResource(R.string.pref_opt_dynamic_polling_desc),
                    icon = Icons.Rounded.BatteryChargingFull,
                    checked = dynamicAudioPolling,
                    onCheckedChange = {
                        dynamicAudioPolling = it
                        prefs.edit().putBoolean("pref_opt_dynamic_audio_polling", it).apply()
                    }
                )
            }

            item {
                PerformanceOptionCard(
                    title = stringResource(R.string.pref_opt_low_latency_title),
                    subtitle = stringResource(R.string.pref_opt_low_latency_sub),
                    description = stringResource(R.string.pref_opt_low_latency_desc),
                    icon = Icons.Rounded.PlayCircle,
                    checked = lowLatencyBuffer,
                    onCheckedChange = {
                        lowLatencyBuffer = it
                        prefs.edit().putBoolean("pref_opt_low_latency_buffer", it).apply()
                    }
                )
            }

            item {
                PerformanceOptionCard(
                    title = stringResource(R.string.pref_opt_predictive_precache_title),
                    subtitle = stringResource(R.string.pref_opt_predictive_precache_sub),
                    description = stringResource(R.string.pref_opt_predictive_precache_desc),
                    icon = Icons.Rounded.FastForward,
                    checked = predictivePrecaching,
                    onCheckedChange = {
                        predictivePrecaching = it
                        prefs.edit().putBoolean("pref_opt_predictive_precache_desc", it).apply()
                    }
                )
            }

            // Section 3: Memory & Bitmap Optimization
            item {
                PerformanceSectionHeader(
                    title = stringResource(R.string.pref_perf_sec_memory),
                    icon = Icons.Rounded.Memory
                )
            }

            item {
                PerformanceOptionCard(
                    title = stringResource(R.string.pref_opt_thumbnail_downsampling_title),
                    subtitle = stringResource(R.string.pref_opt_thumbnail_downsampling_sub),
                    description = stringResource(R.string.pref_opt_thumbnail_downsampling_desc),
                    icon = Icons.Rounded.Image,
                    checked = thumbnailDownsampling,
                    onCheckedChange = {
                        thumbnailDownsampling = it
                        prefs.edit().putBoolean("pref_opt_thumbnail_downsampling", it).apply()
                    }
                )
            }
        }
    }
}

@Composable
private fun PerformanceSectionHeader(
    title: String,
    icon: ImageVector
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun PerformanceOptionCard(
    title: String,
    subtitle: String,
    description: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onCheckedChange(!checked) }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (checked) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (checked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = checked,
                    onCheckedChange = onCheckedChange
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall.copy(lineHeight = 16.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f)
            )
        }
    }
}
