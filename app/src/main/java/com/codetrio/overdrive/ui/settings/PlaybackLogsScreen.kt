package com.codetrio.overdrive.ui.settings

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.codetrio.overdrive.R
import com.codetrio.overdrive.data.diagnostics.LogLevel
import com.codetrio.overdrive.data.diagnostics.PlaybackDiagnosticsLogger
import com.codetrio.overdrive.data.diagnostics.PlaybackLogEntry
import com.codetrio.overdrive.data.diagnostics.PlaybackSession

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaybackLogsScreen(
    navController: NavController
) {
    val history by PlaybackDiagnosticsLogger.sessionsHistoryState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var selectedFilter by remember { mutableStateOf("all") } // "all", "error", "online"
    var expandedSessionId by remember { mutableStateOf<String?>(null) }

    val filteredSessions = remember(history, selectedFilter) {
        when (selectedFilter) {
            "error" -> history.filter { it.isError }
            "online" -> history.filter { it.source.contains("YouTube", ignoreCase = true) || it.source.contains("Online", ignoreCase = true) }
            else -> history
        }
    }

    val errorCount = remember(history) { history.count { it.isError } }
    val copiedToast = stringResource(R.string.logs_copied)
    val clearedToast = stringResource(R.string.logs_cleared)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.logs_title), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            val dump = PlaybackDiagnosticsLogger.getAllLogsFormatted()
                            val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            cm.setPrimaryClip(ClipData.newPlainText("OverDrive Playback Logs", dump))
                            Toast.makeText(context, copiedToast, Toast.LENGTH_SHORT).show()
                        },
                        enabled = history.isNotEmpty()
                    ) {
                        Icon(Icons.Rounded.ContentCopy, contentDescription = stringResource(R.string.logs_copy_all))
                    }
                    IconButton(
                        onClick = {
                            PlaybackDiagnosticsLogger.clearAllLogs()
                            Toast.makeText(context, clearedToast, Toast.LENGTH_SHORT).show()
                        },
                        enabled = history.isNotEmpty()
                    ) {
                        Icon(Icons.Rounded.DeleteOutline, contentDescription = stringResource(R.string.logs_clear))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Filter Chips Row
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedFilter == "all",
                        onClick = { selectedFilter = "all" },
                        label = { Text("${stringResource(R.string.logs_all)} (${history.size})") },
                        leadingIcon = {
                            Icon(Icons.Rounded.List, null, modifier = Modifier.size(16.dp))
                        }
                    )
                }
                item {
                    FilterChip(
                        selected = selectedFilter == "error",
                        onClick = { selectedFilter = "error" },
                        label = { Text("${stringResource(R.string.logs_errors)} ($errorCount)") },
                        leadingIcon = {
                            Icon(Icons.Rounded.ErrorOutline, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.error)
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.errorContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onErrorContainer
                        )
                    )
                }
                item {
                    FilterChip(
                        selected = selectedFilter == "online",
                        onClick = { selectedFilter = "online" },
                        label = { Text(stringResource(R.string.logs_online)) },
                        leadingIcon = {
                            Icon(Icons.Rounded.Cloud, null, modifier = Modifier.size(16.dp))
                        }
                    )
                }
            }

            if (filteredSessions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = if (selectedFilter == "error") Icons.Rounded.CheckCircleOutline else Icons.Rounded.History,
                            contentDescription = null,
                            tint = if (selectedFilter == "error") Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(56.dp)
                        )
                        Text(
                            text = stringResource(R.string.logs_empty),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredSessions, key = { it.id }) { session ->
                        PlaybackSessionCard(
                            session = session,
                            isExpanded = expandedSessionId == session.id,
                            onToggleExpand = {
                                expandedSessionId = if (expandedSessionId == session.id) null else session.id
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PlaybackSessionCard(
    session: PlaybackSession,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit
) {
    val context = LocalContext.current
    val copiedToast = stringResource(R.string.logs_copied)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onToggleExpand() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (session.isError) {
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f)
            } else {
                MaterialTheme.colorScheme.surfaceContainer
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header Row: Status, Source, Time, Expand Icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    SessionStatusIndicator(session)
                    Text(
                        text = session.source,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = session.formattedStartTime.substringAfter(" "),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Icon(
                        imageVector = if (isExpanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Song Info
            Text(
                text = session.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = if (isExpanded) 2 else 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = session.artist,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Extractor & Error summary preview
            if (!session.extractor.isNullOrEmpty() || session.isError) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!session.extractor.isNullOrEmpty()) {
                        Text(
                            text = "⚡ ${session.extractor} (${session.extractionDurationMs ?: 0}ms)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (session.isError) {
                        Text(
                            text = "❌ ${session.errorCodeName ?: "Error"}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Expanded Details Section
            if (isExpanded) {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 4.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Technical Details",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    DetailRow("Video ID", session.videoId ?: "N/A")
                    DetailRow("Format / MIME", "${session.codec ?: "N/A"} (${session.mimeType ?: "N/A"})")
                    DetailRow("Bitrate / SampleRate", "${session.bitrate?.let { "${it / 1000}kbps" } ?: "N/A"} • ${session.sampleRate?.let { "${it}Hz" } ?: "N/A"}")
                    DetailRow("Audio Decoder", session.decoderName ?: "Default MediaCodec")
                    DetailRow("AudioSession ID", session.audioSessionId?.toString() ?: "N/A")
                    DetailRow("Player State", session.playbackState)
                    DetailRow("Buffer Health", "${session.bufferHealthMs} ms")

                    if (session.isError) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.errorContainer)
                                .padding(10.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "Error: ${session.errorCodeName}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                                if (session.httpStatusCode != null) {
                                    Text(
                                        text = "HTTP Status Code: ${session.httpStatusCode}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                                Text(
                                    text = session.errorMessage ?: "Unknown error",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                                if (!session.errorStackTrace.isNullOrEmpty()) {
                                    Text(
                                        text = session.errorStackTrace!!.take(300) + "...",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                        fontFamily = FontFamily.Monospace,
                                        color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }
                    }

                    // Events Terminal inside card
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Event Timeline (${session.entries.size}):",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 180.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF121417))
                            .padding(8.dp)
                    ) {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            items(session.entries) { entry ->
                                Text(
                                    text = "[${entry.formattedTime}] [${entry.tag}] ${entry.message}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    fontFamily = FontFamily.Monospace,
                                    color = when (entry.level) {
                                        LogLevel.ERROR -> Color(0xFFEF9A9A)
                                        LogLevel.WARN -> Color(0xFFFFE082)
                                        LogLevel.SUCCESS -> Color(0xFFA5D6A7)
                                        LogLevel.INFO -> Color(0xFF90CAF9)
                                    }
                                )
                            }
                        }
                    }

                    // Copy Button
                    Button(
                        onClick = {
                            val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            cm.setPrimaryClip(ClipData.newPlainText("Session Log", session.toFormattedString()))
                            Toast.makeText(context, copiedToast, Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Rounded.ContentCopy, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(stringResource(R.string.logs_copy_all))
                    }
                }
            }
        }
    }
}

@Composable
private fun SessionStatusIndicator(session: PlaybackSession) {
    val (dotColor, label) = when {
        session.isError && session.isAutoRecovered -> Color(0xFFFFA000) to "Recovered"
        session.isError -> Color(0xFFE53935) to "Failed"
        session.playbackState == "PLAYING" -> Color(0xFF43A047) to "Playing"
        session.playbackState == "BUFFERING" -> Color(0xFFFDD835) to "Buffering"
        else -> Color(0xFF78909C) to session.playbackState
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(dotColor)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = dotColor
        )
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
