package com.codetrio.overdrive.ui.statistics.dna

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.PlaylistPlay
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.codetrio.overdrive.data.dna.MusicArchetype
import com.codetrio.overdrive.data.dna.MusicCapsuleTimeRange
import com.codetrio.overdrive.data.dna.MusicDnaProfile
import com.codetrio.overdrive.model.SongItem
import com.codetrio.overdrive.viewmodel.PlayerSharedViewModel
import kotlinx.coroutines.launch

private fun getArchetypeVectorIcon(archetype: MusicArchetype): ImageVector {
    return when (archetype) {
        MusicArchetype.CYBERNETIC_NIGHTCRAWLER -> Icons.Rounded.NightsStay
        MusicArchetype.MELODIC_DAWN_CHASER -> Icons.Rounded.WbSunny
        MusicArchetype.AFTERNOON_GROOVER -> Icons.Rounded.WbTwilight
        MusicArchetype.INFINITE_VOYAGER -> Icons.Rounded.Explore
        MusicArchetype.LOYAL_SUPERFAN -> Icons.Rounded.Favorite
        MusicArchetype.HARMONIC_ARCHITECT -> Icons.Rounded.AutoAwesome
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusicDnaScreen(
    viewModel: PlayerSharedViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val dnaProfile by viewModel.musicDnaProfileFlow.collectAsStateWithLifecycle(null)
    val selectedRange by viewModel.selectedDnaRange.collectAsStateWithLifecycle(MusicCapsuleTimeRange.ALL_TIME)

    var showStoryDialog by remember { mutableStateOf(false) }
    var isSharing by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "リスニングDNA",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (dnaProfile != null) {
                        IconButton(onClick = { showStoryDialog = true }) {
                            Icon(
                                imageVector = Icons.Rounded.PlayCircle,
                                contentDescription = "Story",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(
                            onClick = {
                                if (!isSharing && dnaProfile != null) {
                                    isSharing = true
                                    coroutineScope.launch {
                                        MusicDnaShareManager.generateAndShareCard(context, dnaProfile!!)
                                        isSharing = false
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Rounded.Share, contentDescription = "Share")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (dnaProfile == null) {
                // Empty state
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceContainerHighest,
                            modifier = Modifier.size(80.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Rounded.GraphicEq,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(40.dp)
                                )
                            }
                        }
                        Text(
                            text = "まだ再生履歴がありません",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "音楽を再生すると、あなたのリスニング傾向を自動分析してリスニングDNAとラップが生成されます。",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                val profile = dnaProfile!!

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 140.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // M3 Segmented Time Range Selector
                    item {
                        val ranges = listOf(
                            MusicCapsuleTimeRange.ALL_TIME to "全期間",
                            MusicCapsuleTimeRange.THIS_MONTH to "今月",
                            MusicCapsuleTimeRange.THIS_WEEK to "今週"
                        )
                        SingleChoiceSegmentedButtonRow(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            ranges.forEachIndexed { index, (range, label) ->
                                SegmentedButton(
                                    selected = selectedRange == range,
                                    onClick = { viewModel.setSelectedDnaRange(range) },
                                    shape = SegmentedButtonDefaults.itemShape(index = index, count = ranges.size),
                                    label = { Text(label, style = MaterialTheme.typography.labelMedium) }
                                )
                            }
                        }
                    }

                    // M3 Archetype Hero Card
                    item {
                        ElevatedCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(28.dp),
                            colors = CardDefaults.elevatedCardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                            ),
                            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        modifier = Modifier.size(56.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = getArchetypeVectorIcon(profile.archetype),
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(30.dp)
                                            )
                                        }
                                    }

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = profile.archetype.titleJa,
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = profile.archetype.titleEn,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }

                                Text(
                                    text = profile.archetype.subtitleJa,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                // Metrics 3-column sub cards
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    val metrics = listOf(
                                        "総再生時間" to "${profile.totalMinutes}分",
                                        "再生回数" to "${profile.totalPlays}回",
                                        "アーティスト" to "${profile.uniqueArtistsCount}組"
                                    )
                                    metrics.forEach { (label, value) ->
                                        Surface(
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(16.dp),
                                            color = MaterialTheme.colorScheme.surfaceContainerHighest
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.spacedBy(2.dp)
                                            ) {
                                                Text(
                                                    text = value,
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = label,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 4-Axis Parameter Card
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainer
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Tune,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = "リスニング特性パラメータ",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                DnaMetricBar(
                                    label = "探索度 (Discovery)",
                                    score = profile.discoveryScore,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                DnaMetricBar(
                                    label = "夜行性 (Night Owl)",
                                    score = profile.nightOwlScore,
                                    color = MaterialTheme.colorScheme.tertiary
                                )
                                DnaMetricBar(
                                    label = "熱中度 (Devotion)",
                                    score = profile.devotionScore,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                                DnaMetricBar(
                                    label = "スタミナ (Volume)",
                                    score = profile.staminaScore,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    // 24h Rhythm Card
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainer
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Schedule,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Text(
                                            text = "24時間のアクティビティ",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.secondaryContainer
                                    ) {
                                        Text(
                                            text = "ピーク: ${profile.peakHour}時台",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                // 24-Bar Chart
                                val maxVal = (profile.hourlyDistribution.values.maxOrNull() ?: 1).coerceAtLeast(1)
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(110.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxSize(),
                                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                                        verticalAlignment = Alignment.Bottom
                                    ) {
                                        for (h in 0..23) {
                                            val count = profile.hourlyDistribution[h] ?: 0
                                            val barFraction = if (maxVal > 0) (count.toFloat() / maxVal).coerceIn(0.06f, 1f) else 0.06f
                                            val isPeak = h == profile.peakHour

                                            Column(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .fillMaxHeight(),
                                                verticalArrangement = Arrangement.Bottom,
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .fillMaxHeight(barFraction)
                                                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp, bottomStart = 2.dp, bottomEnd = 2.dp))
                                                        .background(
                                                            if (isPeak) MaterialTheme.colorScheme.primary
                                                            else MaterialTheme.colorScheme.surfaceContainerHighest
                                                        )
                                                )
                                            }
                                        }
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("00:00", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("12:00", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("23:00", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }

                    // Top Tracks
                    if (profile.topSongs.isNotEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(24.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 20.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Leaderboard,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Text(
                                            text = "トップ再生トラック",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }

                                    profile.topSongs.take(5).forEachIndexed { index, item ->
                                        ListItem(
                                            modifier = Modifier.clickable {
                                                val songItem = if (item.songId.toLongOrNull() != null && !item.thumbnailUrl.isNullOrEmpty() && !item.thumbnailUrl.startsWith("http")) {
                                                    SongItem(item.songId.toLong(), item.title, item.artist, -1L, item.thumbnailUrl, 0L, System.currentTimeMillis())
                                                } else {
                                                    SongItem.createOnlineSong(videoId = item.songId, title = item.title, artist = item.artist, streamUrl = null, durationMs = 0L, thumbnailUrl = item.thumbnailUrl)
                                                }
                                                viewModel.playSong(songItem)
                                            },
                                            leadingContent = {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                                ) {
                                                    Text(
                                                        text = "${index + 1}",
                                                        style = MaterialTheme.typography.labelLarge,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (index == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                        modifier = Modifier.width(20.dp),
                                                        textAlign = TextAlign.Center
                                                    )
                                                    AsyncImage(
                                                        model = item.thumbnailUrl,
                                                        contentDescription = item.title,
                                                        modifier = Modifier
                                                            .size(48.dp)
                                                            .clip(RoundedCornerShape(12.dp)),
                                                        contentScale = ContentScale.Crop
                                                    )
                                                }
                                            },
                                            headlineContent = {
                                                Text(
                                                    text = item.title,
                                                    style = MaterialTheme.typography.bodyLarge,
                                                    fontWeight = FontWeight.SemiBold,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            },
                                            supportingContent = {
                                                Text(
                                                    text = item.artist,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            },
                                            trailingContent = {
                                                Surface(
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = MaterialTheme.colorScheme.surfaceContainerHighest
                                                ) {
                                                    Text(
                                                        text = "${item.count}回",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                    )
                                                }
                                            },
                                            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Action Buttons
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = { showStoryDialog = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                shape = RoundedCornerShape(26.dp)
                            ) {
                                Icon(Icons.Rounded.PlayArrow, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "ストーリーを見る",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            FilledTonalButton(
                                onClick = { viewModel.playCapsule(profile) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                shape = RoundedCornerShape(26.dp)
                            ) {
                                Icon(androidx.compose.material.icons.Icons.AutoMirrored.Rounded.PlaylistPlay, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "このカプセルを再生",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Fullscreen Wrapped Story Dialog
            if (showStoryDialog && dnaProfile != null) {
                MusicWrappedStoryDialog(
                    profile = dnaProfile!!,
                    onDismiss = { showStoryDialog = false },
                    onPlaySong = { song -> viewModel.playSong(song) },
                    onPlayCapsule = { p -> viewModel.playCapsule(p) }
                )
            }
        }
    }
}

@Composable
private fun DnaMetricBar(label: String, score: Int, color: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "$score%",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
        LinearProgressIndicator(
            progress = { (score / 100f).coerceIn(0.04f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape),
            color = color,
            trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
        )
    }
}
