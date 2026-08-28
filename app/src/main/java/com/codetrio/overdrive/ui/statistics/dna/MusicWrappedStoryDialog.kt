package com.codetrio.overdrive.ui.statistics.dna

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.PlaylistPlay
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.codetrio.overdrive.data.dna.MusicArchetype
import com.codetrio.overdrive.data.dna.MusicDnaProfile
import com.codetrio.overdrive.model.SongItem
import kotlinx.coroutines.delay
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

@Composable
fun MusicWrappedStoryDialog(
    profile: MusicDnaProfile,
    onDismiss: () -> Unit,
    onPlaySong: (SongItem) -> Unit,
    onPlayCapsule: (MusicDnaProfile) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val totalSlides = 5
    var currentSlide by remember { mutableIntStateOf(0) }
    var isPaused by remember { mutableStateOf(false) }

    // Slide progress timer
    val slideDurationMs = 6000L
    var slideProgress by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(currentSlide, isPaused) {
        if (isPaused) return@LaunchedEffect
        slideProgress = 0f
        val interval = 50L
        val steps = (slideDurationMs / interval).toInt()
        for (i in 1..steps) {
            delay(interval)
            slideProgress = i.toFloat() / steps
        }
        if (currentSlide < totalSlides - 1) {
            currentSlide++
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        val primaryContainer = MaterialTheme.colorScheme.primaryContainer
        val surfaceContainer = MaterialTheme.colorScheme.surfaceContainerHigh
        val onSurface = MaterialTheme.colorScheme.onSurface

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.surfaceContainerLowest,
                            surfaceContainer
                        )
                    )
                )
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = {
                            isPaused = true
                            tryAwaitRelease()
                            isPaused = false
                        },
                        onTap = { offset ->
                            val screenWidth = size.width
                            if (offset.x < screenWidth * 0.35f) {
                                if (currentSlide > 0) currentSlide--
                            } else {
                                if (currentSlide < totalSlides - 1) {
                                    currentSlide++
                                } else {
                                    onDismiss()
                                }
                            }
                        }
                    )
                }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                // Top Progress Bars & Close Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    for (i in 0 until totalSlides) {
                        val progress = when {
                            i < currentSlide -> 1f
                            i == currentSlide -> slideProgress
                            else -> 0f
                        }
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .weight(1f)
                                .height(4.dp)
                                .clip(CircleShape),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.6f),
                            strokeCap = StrokeCap.Round
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Close",
                            tint = onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Slide Content
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    AnimatedContent(
                        targetState = currentSlide,
                        transitionSpec = {
                            if (targetState > initialState) {
                                (fadeIn(tween(300)) + slideInHorizontally { width -> width / 3 })
                                    .togetherWith(fadeOut(tween(250)) + slideOutHorizontally { width -> -width / 3 })
                            } else {
                                (fadeIn(tween(300)) + slideInHorizontally { width -> -width / 3 })
                                    .togetherWith(fadeOut(tween(250)) + slideOutHorizontally { width -> width / 3 })
                            }
                        },
                        label = "StorySlideAnimation"
                    ) { slide ->
                        when (slide) {
                            0 -> StorySlideTotalTime(profile)
                            1 -> StorySlideTopSong(profile, onPlaySong)
                            2 -> StorySlideTopArtists(profile)
                            3 -> StorySlide24hRhythm(profile)
                            4 -> StorySlideDnaSummary(profile, onPlayCapsule, onDismiss)
                        }
                    }
                }

                // Bottom Hint
                Text(
                    text = if (currentSlide < totalSlides - 1) "画面をタップして進む / 長押しで一時停止" else "タップして閉じる",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun StorySlideTotalTime(profile: MusicDnaProfile) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(80.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Rounded.Headphones,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(40.dp)
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "${profile.formattedRangeLabel}のリスニング",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "${profile.totalMinutes}",
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "分間、音楽とともに過ごしました",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${profile.totalPlays}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "総再生回数",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                VerticalDivider(modifier = Modifier.height(40.dp))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${profile.uniqueArtistsCount}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "出会ったアーティスト",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun StorySlideTopSong(profile: MusicDnaProfile, onPlaySong: (SongItem) -> Unit) {
    val topSong = profile.topSong

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.secondaryContainer
        ) {
            Text(
                text = "No. 1 トラック",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }

        if (topSong != null) {
            ElevatedCard(
                modifier = Modifier.size(240.dp),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp)
            ) {
                AsyncImage(
                    model = topSong.thumbnailUrl,
                    contentDescription = topSong.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = topSong.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = topSong.artist,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "計 ${topSong.count} 回再生",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }

            FilledTonalButton(
                onClick = {
                    val songItem = if (topSong.songId.toLongOrNull() != null && !topSong.thumbnailUrl.isNullOrEmpty() && !topSong.thumbnailUrl.startsWith("http")) {
                        SongItem(topSong.songId.toLong(), topSong.title, topSong.artist, -1L, topSong.thumbnailUrl, 0L, System.currentTimeMillis())
                    } else {
                        SongItem.createOnlineSong(videoId = topSong.songId, title = topSong.title, artist = topSong.artist, streamUrl = null, durationMs = 0L, thumbnailUrl = topSong.thumbnailUrl)
                    }
                    onPlaySong(songItem)
                },
                shape = RoundedCornerShape(20.dp)
            ) {
                Icon(Icons.Rounded.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("今すぐ再生", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun StorySlideTopArtists(profile: MusicDnaProfile) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "もっとも聴いたアーティスト",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            profile.topArtists.take(5).forEachIndexed { index, item ->
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = if (index == 0) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceContainerHighest
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "#${index + 1}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = if (index == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.width(36.dp)
                        )
                        Text(
                            text = item.artist,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${item.count} 曲",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StorySlide24hRhythm(profile: MusicDnaProfile) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "あなたのリスニング・リズム",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "1日の中で最も音に夢中になる時間帯",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest)
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
                    Text(
                        text = "ピークタイム",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = "${profile.peakHour}時台",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }

                // 24-Bar visual
                val maxVal = (profile.hourlyDistribution.values.maxOrNull() ?: 1).coerceAtLeast(1)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    for (h in 0..23) {
                        val count = profile.hourlyDistribution[h] ?: 0
                        val barFraction = if (maxVal > 0) (count.toFloat() / maxVal).coerceIn(0.08f, 1f) else 0.08f
                        val isPeak = h == profile.peakHour

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(barFraction)
                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                .background(
                                    if (isPeak) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.surfaceContainerLowest
                                )
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("0:00", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("12:00", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("23:00", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun StorySlideDnaSummary(
    profile: MusicDnaProfile,
    onPlayCapsule: (MusicDnaProfile) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isSharing by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(72.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = getArchetypeVectorIcon(profile.archetype),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = profile.archetype.titleJa,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = profile.archetype.titleEn,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Text(
                    text = profile.archetype.subtitleJa,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Action CTA Buttons
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = {
                    if (!isSharing) {
                        isSharing = true
                        coroutineScope.launch {
                            MusicDnaShareManager.generateAndShareCard(context, profile)
                            isSharing = false
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(26.dp)
            ) {
                Icon(Icons.Rounded.Share, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("SNSでシェア", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            FilledTonalButton(
                onClick = {
                    onPlayCapsule(profile)
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(26.dp)
            ) {
                Icon(Icons.AutoMirrored.Rounded.PlaylistPlay, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("このカプセルを再生", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        }
    }
}
