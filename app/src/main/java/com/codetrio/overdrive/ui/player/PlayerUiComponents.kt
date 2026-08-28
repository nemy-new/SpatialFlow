package com.codetrio.overdrive.ui.player

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeDown
import androidx.compose.material.icons.automirrored.rounded.VolumeMute
import androidx.compose.material.icons.automirrored.rounded.VolumeOff
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.AudioFile
import androidx.compose.material.icons.rounded.HighQuality
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.codetrio.overdrive.R
import com.codetrio.overdrive.data.lyrics.LyricsResult
import com.codetrio.overdrive.model.SongItem

/**
 * Custom Compose extension to render a marquee with smooth horizontal alpha-faded edges.
 * Uses drawWithCache to avoid allocating Brush and List objects on every frame of the drawing phase.
 */
@OptIn(ExperimentalFoundationApi::class)
fun Modifier.basicMarqueeWithFadedEdges(
    edgeWidth: Dp = 12.dp
): Modifier = this
    .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
    .drawWithCache {
        val edgeWidthPx = edgeWidth.toPx()
        // Cache the brushes so they aren't recreated every frame
        val leftBrush = Brush.horizontalGradient(
            colors = listOf(Color.Transparent, Color.Black),
            startX = 0f,
            endX = edgeWidthPx
        )
        val rightBrush = Brush.horizontalGradient(
            colors = listOf(Color.Black, Color.Transparent),
            startX = size.width - edgeWidthPx,
            endX = size.width
        )
        
        onDrawWithContent {
            drawContent()
            drawRect(brush = leftBrush, blendMode = BlendMode.DstIn)
            drawRect(brush = rightBrush, blendMode = BlendMode.DstIn)
        }
    }
    .basicMarquee()
    .padding(horizontal = edgeWidth)

internal fun formatDuration(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(java.util.Locale.getDefault(), "%02d:%02d", minutes, seconds)
}

internal fun deriveArtworkSurfaceColor(
    sourceColor: Color,
    isDark: Boolean,
    darkLightness: Float,
    lightLightness: Float,
    darkSaturationRange: ClosedFloatingPointRange<Float>,
    lightSaturationRange: ClosedFloatingPointRange<Float>,
    monochromeSaturationThreshold: Float = 0.06f
): Color {
    val hsl = FloatArray(3)
    androidx.core.graphics.ColorUtils.colorToHSL(sourceColor.toArgb(), hsl)
    val isMonochrome = hsl[1] < monochromeSaturationThreshold
    hsl[2] = if (isDark) darkLightness else lightLightness
    hsl[1] = if (isMonochrome) {
        0f
    } else if (isDark) {
        hsl[1].coerceIn(darkSaturationRange.start, darkSaturationRange.endInclusive)
    } else {
        hsl[1].coerceIn(lightSaturationRange.start, lightSaturationRange.endInclusive)
    }
    return Color(androidx.core.graphics.ColorUtils.HSLToColor(hsl))
}

@Composable
internal fun SplitLikeDislikeChip(
    isLiked: Boolean,
    isDisliked: Boolean,
    likesCount: String,
    onLikeClick: () -> Unit,
    onDislikeClick: () -> Unit,
    contentColor: Color,
    accentColor: Color,
    isDark: Boolean,
    customBackgroundColor: Color? = null
) {
    val backgroundColor = customBackgroundColor ?: contentColor.copy(alpha = if (isDark) 0.08f else 0.06f)
    val defaultLikeLabel = androidx.compose.ui.res.stringResource(id = com.codetrio.overdrive.R.string.text_like)
    val displayLikesText = remember(likesCount, defaultLikeLabel) {
        likesCount.ifBlank { defaultLikeLabel }
    }
    
    Row(
        modifier = Modifier
            .height(36.dp)
            .clip(CircleShape)
            .background(backgroundColor),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Like Button
        Row(
            modifier = Modifier
                .fillMaxHeight()
                .clip(RoundedCornerShape(topStart = 18.dp, bottomStart = 18.dp))
                .clickable(onClick = onLikeClick)
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                painter = painterResource(id = if (isLiked) R.drawable.ic_thumbup else R.drawable.ic_outline_thumbup),
                contentDescription = "Like",
                tint = if (isLiked) accentColor else contentColor.copy(alpha = 0.8f),
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = displayLikesText,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                color = if (isLiked) accentColor else contentColor.copy(alpha = 0.8f)
            )
        }
        
        // Vertical Divider
        Spacer(
            modifier = Modifier
                .width(1.dp)
                .fillMaxHeight(0.5f)
                .background(contentColor.copy(alpha = 0.15f))
        )
        
        // Dislike Button
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .clip(RoundedCornerShape(topEnd = 18.dp, bottomEnd = 18.dp))
                .clickable(onClick = onDislikeClick)
                .padding(horizontal = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = if (isDisliked) R.drawable.ic_thumbdown else R.drawable.ic_outline_thumbdown),
                contentDescription = "Dislike",
                tint = if (isDisliked) accentColor else contentColor.copy(alpha = 0.8f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
internal fun PillChip(
    icon: Any,
    label: String,
    onClick: () -> Unit,
    contentColor: Color,
    accentColor: Color,
    isDark: Boolean,
    isSelected: Boolean = false,
    progress: Float? = null,
    customBackgroundColor: Color? = null,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isSelected) {
        accentColor.copy(alpha = if (isDark) 0.25f else 0.18f)
    } else {
        customBackgroundColor ?: contentColor.copy(alpha = if (isDark) 0.08f else 0.06f)
    }
    
    val tintColor = if (isSelected) accentColor else contentColor.copy(alpha = 0.8f)
    val progressColor = accentColor.copy(alpha = if (isDark) 0.35f else 0.25f)
    
    val animatedFill by animateFloatAsState(
        targetValue = progress ?: 0f,
        animationSpec = spring(dampingRatio = 0.85f, stiffness = 150f),
        label = "DownloadChipProgress"
    )
    
    Box(
        modifier = modifier
            .height(36.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .drawBehind {
                if (progress != null && progress > 0f) {
                    drawRect(
                        color = progressColor,
                        size = size.copy(width = size.width * animatedFill)
                    )
                }
            }
            .clickable(onClick = onClick),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            when (icon) {
                is ImageVector -> {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = tintColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
                is Painter -> {
                    Icon(
                        painter = icon,
                        contentDescription = label,
                        tint = tintColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                color = tintColor
            )
        }
    }
}

@Composable
internal fun WavySliderWithLabels(
    currentPositionProvider: () -> Int,
    duration: Int,
    isPlaying: Boolean,
    onSeekTo: (Int) -> Unit,
    dynamicAccentColor: Color,
    contentColor: Color,
    contentSecondary: Color,
    isDark: Boolean,
    playbackFormat: String,
    modifier: Modifier = Modifier
) {
    var isScrubbing by remember { mutableStateOf(false) }
    var sliderScrubPos by remember { mutableFloatStateOf(0f) }
    var lastSeekTime by remember { mutableLongStateOf(0L) }
    var lastSeekPos by remember { mutableFloatStateOf(0f) }

    val currentPosition = currentPositionProvider()
    val safeDur = if (duration > 0) duration.toFloat() else 1f
    val progressRatio = (currentPosition.toFloat() / safeDur).coerceIn(0f, 1f)

    val isWaitingForPlayer = remember(progressRatio, lastSeekTime, lastSeekPos) {
        val elapsed = System.currentTimeMillis() - lastSeekTime
        val diff = kotlin.math.abs(progressRatio - lastSeekPos)
        elapsed < 1000 && diff > 0.02f
    }

    val displayValue = when {
        isScrubbing -> sliderScrubPos
        isWaitingForPlayer -> lastSeekPos
        else -> progressRatio
    }

    val displayPos = when {
        isScrubbing -> (sliderScrubPos * safeDur).toInt()
        isWaitingForPlayer -> (lastSeekPos * safeDur).toInt()
        else -> currentPosition
    }

    val formatIcon = when (playbackFormat) {
        "OPUS" -> Icons.Rounded.GraphicEq
        "AAC" -> Icons.Rounded.MusicNote
        "MP3" -> Icons.Rounded.AudioFile
        "FLAC", "WAV" -> Icons.Rounded.HighQuality
        else -> Icons.Rounded.MusicNote
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
    ) {
        WavyMusicSlider(
            value = displayValue,
            onValueChange = {
                isScrubbing = true
                sliderScrubPos = it
            },
            onValueChangeFinished = {
                isScrubbing = false
                lastSeekTime = System.currentTimeMillis()
                lastSeekPos = sliderScrubPos
                onSeekTo((sliderScrubPos * safeDur).toInt())
            },
            activeTrackColor = dynamicAccentColor,
            inactiveTrackColor = contentColor.copy(alpha = if (isDark) 0.08f else 0.06f),
            thumbColor = contentColor,
            isPlaying = isPlaying,
            trackHeight = 4.dp,
            thumbRadius = 6.dp,
            waveAmplitudeWhenPlaying = 6.dp,
            waveLength = 48.dp,
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = formatDuration(displayPos.toLong()),
                style = MaterialTheme.typography.labelSmall,
                color = contentSecondary
            )

            Box(
                modifier = Modifier
                    .background(
                        color = contentColor.copy(alpha = 0.08f),
                        shape = RoundedCornerShape(4.dp)
                    )
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = formatIcon,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = contentColor.copy(alpha = 0.7f)
                    )
                    Text(
                        text = playbackFormat,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = contentColor.copy(alpha = 0.7f)
                    )
                }
            }

            Text(
                text = formatDuration(duration.toLong()),
                style = MaterialTheme.typography.labelSmall,
                color = contentSecondary
            )
        }
    }
}

/**
 * Footer showing song metadata at the bottom of lyrics.
 * Displays song name, artist, album, and lyrics provider — only when values are present.
 * Styled to look "always inactive" with small text and low opacity.
 */
@Composable
internal fun LyricsMetadataFooter(
    currentSong: SongItem?,
    selectedProvider: String?,
    providerResults: Map<String, LyricsResult>,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    if (currentSong == null) return

    // Resolve active result using the prioritized matching logic
    val activeResult = providerResults[selectedProvider] ?: providerResults.values
        .filter { it.confidence >= 0f && it.hasLyrics() }
        .maxWithOrNull(
            compareBy<LyricsResult> { it.isWordByWord }
                .thenBy { it.isSynced }
                .thenBy { it.providerName?.startsWith("BetterLyrics") == true }
                .thenBy { it.providerName == "SyncLRC" }
                .thenBy { it.confidence }
        )

    val albumName = activeResult?.matchedAlbum
    val providerName = activeResult?.providerName ?: selectedProvider

    val playerTitleFont = com.codetrio.overdrive.ui.theme.rememberCustomFontFamily(com.codetrio.overdrive.data.font.FontTarget.PLAYER_TITLE)
    val mutedColor = contentColor.copy(alpha = 0.35f)
    val metaStyle = MaterialTheme.typography.labelSmall.copy(
        fontFamily = playerTitleFont,
        fontWeight = FontWeight.Normal,
        letterSpacing = 0.3.sp,
        color = mutedColor
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Song title
        if (currentSong.title.isNotBlank()) {
            Text(text = currentSong.title, style = metaStyle, maxLines = 1)
        }
        // Artist
        if (currentSong.artist.isNotBlank() &&
            !currentSong.artist.equals("Unknown Artist", ignoreCase = true)
        ) {
            Text(text = currentSong.artist, style = metaStyle, maxLines = 1)
        }
        // Album (from lyrics provider match)
        if (!albumName.isNullOrBlank()) {
            Text(text = albumName, style = metaStyle, maxLines = 1)
        }
        // Lyrics provider
        if (!providerName.isNullOrBlank()) {
            Text(
                text = "Lyrics by $providerName",
                style = metaStyle,
                maxLines = 1
            )
        }
    }
}

@Composable
fun VolumeSlider(
    modifier: Modifier = Modifier,
    contentColor: Color = Color.White,
    dynamicAccentColor: Color = Color.White
) {
    val context = LocalContext.current
    val audioManager = remember {
        try {
            context.getSystemService(android.content.Context.AUDIO_SERVICE) as? android.media.AudioManager
        } catch (_: Exception) { null }
    }
    
    val maxVolume = remember(audioManager) {
        val rawMax = try {
            audioManager?.getStreamMaxVolume(android.media.AudioManager.STREAM_MUSIC)?.toFloat() ?: 15f
        } catch (_: Exception) { 15f }
        if (rawMax > 0f) rawMax else 15f
    }

    val minVolume = remember(audioManager) {
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                audioManager?.getStreamMinVolume(android.media.AudioManager.STREAM_MUSIC)?.toFloat() ?: 0f
            } else 0f
        } catch (_: Exception) { 0f }
    }

    var currentVolume by remember {
        val initial = try {
            audioManager?.getStreamVolume(android.media.AudioManager.STREAM_MUSIC)?.toFloat() ?: (maxVolume * 0.5f)
        } catch (_: Exception) { maxVolume * 0.5f }
        mutableFloatStateOf(initial.coerceIn(minVolume, maxVolume))
    }

    // 1. React to hardware volume button changes via BroadcastReceiver (with Android 14+ safe flags)
    DisposableEffect(context, audioManager) {
        val receiver = object : android.content.BroadcastReceiver() {
            override fun onReceive(ctx: android.content.Context?, intent: android.content.Intent?) {
                try {
                    audioManager?.let {
                        currentVolume = it.getStreamVolume(android.media.AudioManager.STREAM_MUSIC).toFloat().coerceIn(minVolume, maxVolume)
                    }
                } catch (_: Exception) {}
            }
        }
        val filter = android.content.IntentFilter("android.media.VOLUME_CHANGED_ACTION")
        try {
            androidx.core.content.ContextCompat.registerReceiver(
                context,
                receiver,
                filter,
                androidx.core.content.ContextCompat.RECEIVER_EXPORTED
            )
        } catch (_: Exception) {
            try {
                context.registerReceiver(receiver, filter)
            } catch (_: Exception) {}
        }

        // 2. React to volume changes via ContentObserver for OEM devices (Samsung/Xiaomi/Pixel) where broadcast is suppressed
        val contentObserver = object : android.database.ContentObserver(android.os.Handler(android.os.Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                super.onChange(selfChange)
                try {
                    audioManager?.let {
                        currentVolume = it.getStreamVolume(android.media.AudioManager.STREAM_MUSIC).toFloat().coerceIn(minVolume, maxVolume)
                    }
                } catch (_: Exception) {}
            }
        }
        try {
            context.contentResolver.registerContentObserver(
                android.provider.Settings.System.CONTENT_URI,
                true,
                contentObserver
            )
        } catch (_: Exception) {}

        onDispose {
            try {
                context.unregisterReceiver(receiver)
            } catch (_: Exception) {}
            try {
                context.contentResolver.unregisterContentObserver(contentObserver)
            } catch (_: Exception) {}
        }
    }

    val volumeRange = (maxVolume - minVolume).coerceAtLeast(1f)
    val fraction = ((currentVolume - minVolume) / volumeRange).coerceIn(0f, 1f)
    val animatedFraction by animateFloatAsState(
        targetValue = fraction,
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = 600f),
        label = "VolumeFraction"
    )

    fun applyVolume(newVol: Float) {
        val clamped = newVol.coerceIn(minVolume, maxVolume)
        currentVolume = clamped
        try {
            audioManager?.setStreamVolume(android.media.AudioManager.STREAM_MUSIC, clamped.toInt(), 0)
        } catch (_: Exception) {}
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = { applyVolume(minVolume) },
            modifier = Modifier.size(28.dp)
        ) {
            Icon(
                imageVector = if (currentVolume <= minVolume) Icons.AutoMirrored.Rounded.VolumeOff else Icons.AutoMirrored.Rounded.VolumeDown,
                contentDescription = "Volume Down",
                tint = contentColor.copy(alpha = 0.7f),
                modifier = Modifier.size(20.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(8.dp))
        
        val trackHeight = 24.dp

        Box(
            modifier = Modifier
                .weight(1f)
                .height(trackHeight)
                .clip(CircleShape)
                .background(contentColor.copy(alpha = 0.2f))
                .pointerInput(Unit) {
                    val componentWidth = size.width.toFloat().coerceAtLeast(1f)
                    detectTapGestures(
                        onPress = { offset ->
                            val targetFraction = (offset.x / componentWidth).coerceIn(0f, 1f)
                            val newValue = minVolume + (targetFraction * volumeRange)
                            applyVolume(newValue)
                        }
                    )
                }
                .pointerInput(Unit) {
                    val componentWidth = size.width.toFloat().coerceAtLeast(1f)
                    detectHorizontalDragGestures(
                        onHorizontalDrag = { change, _ ->
                            change.consume()
                            val targetFraction = (change.position.x / componentWidth).coerceIn(0f, 1f)
                            val newValue = minVolume + (targetFraction * volumeRange)
                            applyVolume(newValue)
                        }
                    )
                },
            contentAlignment = Alignment.CenterStart
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedFraction.coerceAtLeast(0.001f))
                    .fillMaxHeight()
                    .clip(CircleShape)
                    .background(dynamicAccentColor)
            )
        }
        
        Spacer(modifier = Modifier.width(8.dp))
        
        IconButton(
            onClick = { applyVolume(maxVolume) },
            modifier = Modifier.size(28.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.VolumeUp,
                contentDescription = "Volume Up",
                tint = contentColor.copy(alpha = 0.7f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/**
 * Premium Expressive Artwork Placeholder for tracks with missing album art.
 * Renders a rich ambient gradient with glowing concentric acoustic rings and a frosted glass icon badge.
 */
@Composable
fun ExpressiveArtworkPlaceholder(
    modifier: Modifier = Modifier,
    title: String? = null,
    artist: String? = null,
    accentColor: Color = MaterialTheme.colorScheme.primary
) {
    val surfaceColor = MaterialTheme.colorScheme.surfaceContainerHighest
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        accentColor.copy(alpha = 0.35f),
                        surfaceColor.copy(alpha = 0.85f),
                        MaterialTheme.colorScheme.surfaceContainerLowest
                    ),
                    radius = 800f
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Decorative concentric acoustic waves
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = this.center
            val maxR = size.minDimension * 0.46f

            // Concentric ambient rings
            for (i in 1..3) {
                val radius = maxR * (i / 3.2f)
                drawCircle(
                    color = accentColor.copy(alpha = 0.08f * (4 - i)),
                    radius = radius,
                    center = center,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(
                        width = 1.5.dp.toPx()
                    )
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(16.dp)
        ) {
            // Frosted Glass Icon Badge
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.65f),
                border = androidx.compose.foundation.BorderStroke(
                    1.5.dp,
                    Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.45f),
                            accentColor.copy(alpha = 0.30f),
                            Color.Transparent
                        )
                    )
                ),
                shadowElevation = 8.dp,
                modifier = Modifier.size(72.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.MusicNote,
                        contentDescription = "Music Placeholder",
                        tint = accentColor,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            if (!title.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = onSurfaceColor,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                if (!artist.isNullOrBlank()) {
                    Text(
                        text = artist,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

