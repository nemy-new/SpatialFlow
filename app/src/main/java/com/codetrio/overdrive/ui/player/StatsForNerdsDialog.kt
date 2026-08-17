package com.codetrio.overdrive.ui.player

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import java.util.Locale
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.codetrio.overdrive.data.diagnostics.LogLevel
import com.codetrio.overdrive.data.diagnostics.PlaybackDiagnosticsLogger
import com.codetrio.overdrive.data.diagnostics.PlaybackLogEntry
import com.codetrio.overdrive.data.diagnostics.PlaybackSession
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsForNerdsDialog(
    onDismissRequest: () -> Unit,
    accentColor: Color = MaterialTheme.colorScheme.primary
) {
    val session by PlaybackDiagnosticsLogger.currentSessionState.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 6.dp,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = {
            BottomSheetDefaults.DragHandle(
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.88f)
                .padding(horizontal = 20.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(accentColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Info,
                            contentDescription = "Diagnostics",
                            tint = accentColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Playback Diagnostics",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Stats for Nerds & Stream Inspector",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(onClick = {
                    scope.launch { sheetState.hide() }.invokeOnCompletion { onDismissRequest() }
                }) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (session == null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.MusicOff,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "No active playback session",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                val current = session!!
                val listState = rememberLazyListState()

                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Status Badge & Song Info
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (current.isError) {
                                    MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
                                } else {
                                    MaterialTheme.colorScheme.surfaceContainer
                                }
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    StatusBadge(current)
                                    Text(
                                        text = current.source,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                Text(
                                    text = current.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = current.artist,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                if (!current.videoId.isNullOrEmpty()) {
                                    Text(
                                        text = "Video ID: ${current.videoId}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontFamily = FontFamily.Monospace,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }
                    }

                    // Audio Stream & Decoder Metrics
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text(
                                    text = "Audio Pipeline & Stream Specs",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = accentColor
                                )

                                MetricRow(label = "Format & Codec", value = "${current.codec ?: current.mimeType ?: "Resolving..."}")
                                MetricRow(label = "Bitrate / SampleRate", value = "${current.bitrate?.let { "${it / 1000} kbps" } ?: "N/A"} • ${current.sampleRate?.let { "${it} Hz" } ?: "N/A"}")
                                MetricRow(label = "Stream Extractor", value = "${current.extractor ?: "N/A"} (${current.extractionDurationMs?.let { "${it}ms" } ?: "N/A"})")
                                MetricRow(label = "Audio Decoder", value = current.decoderName ?: "Android MediaCodec (Default)")
                                MetricRow(label = "AudioSession ID", value = current.audioSessionId?.toString() ?: "N/A")
                                MetricRow(label = "YouTube Loudness Target", value = current.loudnessDb?.let { "${it} dB" } ?: "Normal (0.0 dB)")
                            }
                        }
                    }

                    // Buffer & Real-time Health
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Buffer Health",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = accentColor
                                    )
                                    Text(
                                        text = "${current.bufferHealthMs / 1000.0}s buffered",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (current.bufferHealthMs > 5000) Color(0xFF4CAF50) else Color(0xFFFFA000)
                                    )
                                }

                                val bufferFraction = (current.bufferHealthMs / 30000f).coerceIn(0f, 1f)
                                LinearProgressIndicator(
                                    progress = { bufferFraction },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp)),
                                    color = if (current.bufferHealthMs > 5000) Color(0xFF4CAF50) else Color(0xFFFFA000),
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                                )

                                MetricRow(
                                    label = "Player State",
                                    value = current.playbackState
                                )
                                MetricRow(
                                    label = "Position / Duration",
                                    value = "${formatMs(current.currentPositionMs)} / ${formatMs(current.durationMs)}"
                                )
                            }
                        }
                    }

                    // Error Section (if applicable)
                    if (current.isError) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                            ) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Icon(Icons.Rounded.ErrorOutline, null, tint = MaterialTheme.colorScheme.error)
                                        Text(
                                            text = "Playback Error (${current.errorCodeName ?: "UNKNOWN"})",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onErrorContainer
                                        )
                                    }
                                    if (current.httpStatusCode != null) {
                                        Text(
                                            text = "HTTP Status Code: ${current.httpStatusCode}",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onErrorContainer
                                        )
                                    }
                                    Text(
                                        text = current.errorMessage ?: "Unknown error occurred",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                    if (current.isAutoRecovered) {
                                        Text(
                                            text = "✓ Auto-recovery attempted: ${current.recoveryAction}",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF2E7D32)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Terminal Live Event Log
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Event Timeline (${current.entries.size} logs)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 240.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color(0xFF121417))
                                    .border(1.dp, Color(0xFF2A2E35), RoundedCornerShape(14.dp))
                                    .padding(12.dp)
                            ) {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    items(current.entries) { entry ->
                                        LogEntryView(entry)
                                    }
                                }
                            }
                        }
                    }

                    // Quick Actions Row
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    cm.setPrimaryClip(ClipData.newPlainText("Playback Diagnostics", current.toFormattedString()))
                                    Toast.makeText(context, "Full Diagnostics Log copied to clipboard", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Icon(Icons.Rounded.ContentCopy, null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Copy Full Log")
                            }

                            if (!current.streamUrl.isNullOrEmpty()) {
                                OutlinedButton(
                                    onClick = {
                                        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        cm.setPrimaryClip(ClipData.newPlainText("Stream URL", current.streamUrl))
                                        Toast.makeText(context, "Stream URL copied", Toast.LENGTH_SHORT).show()
                                    },
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Icon(Icons.Rounded.Link, null, modifier = Modifier.size(18.dp))
                                }

                                IconButton(
                                    onClick = {
                                        try {
                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(current.streamUrl))
                                            context.startActivity(intent)
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "Could not open URL", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                ) {
                                    Icon(Icons.Rounded.OpenInBrowser, null)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(session: PlaybackSession) {
    val (badgeBg, badgeFg, label) = when {
        session.isError && session.isAutoRecovered -> Triple(Color(0xFFFFF3E0), Color(0xFFE65100), "Auto-Recovered")
        session.isError -> Triple(Color(0xFFFFEBEE), Color(0xFFC62828), "Failed")
        session.playbackState == "PLAYING" -> Triple(Color(0xFFE8F5E9), Color(0xFF2E7D32), "Playing")
        session.playbackState == "BUFFERING" -> Triple(Color(0xFFFFFDE7), Color(0xFFF57F17), "Buffering")
        else -> Triple(Color(0xFFECEFF1), Color(0xFF455A64), session.playbackState)
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(badgeBg)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = badgeFg
        )
    }
}

@Composable
private fun MetricRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun LogEntryView(entry: PlaybackLogEntry) {
    val levelColor = when (entry.level) {
        LogLevel.INFO -> Color(0xFF90CAF9)
        LogLevel.SUCCESS -> Color(0xFFA5D6A7)
        LogLevel.WARN -> Color(0xFFFFE082)
        LogLevel.ERROR -> Color(0xFFEF9A9A)
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = entry.formattedTime,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                fontFamily = FontFamily.Monospace,
                color = Color(0xFF78909C)
            )
            Text(
                text = "[${entry.tag}]",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = levelColor
            )
            Text(
                text = entry.message,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                fontFamily = FontFamily.Monospace,
                color = Color(0xFFECEFF1),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (!entry.details.isNullOrEmpty()) {
            Text(
                text = "  └─ ${entry.details}",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                fontFamily = FontFamily.Monospace,
                color = Color(0xFFB0BEC5),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(start = 12.dp)
            )
        }
    }
}

private fun formatMs(ms: Long): String {
    if (ms <= 0) return "0:00"
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.getDefault(), "%d:%02d", minutes, seconds)
}
