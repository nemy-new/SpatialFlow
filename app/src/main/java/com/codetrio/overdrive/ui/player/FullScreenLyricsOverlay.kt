package com.codetrio.overdrive.ui.player

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.graphics.shapes.Morph
import com.codetrio.overdrive.R
import com.codetrio.overdrive.data.lyrics.LyricLine
import com.codetrio.overdrive.data.lyrics.LyricsResult
import com.codetrio.overdrive.model.SongItem
import com.codetrio.overdrive.ui.explore.MorphShape
import com.codetrio.overdrive.ui.player.canvas.CanvasArtwork

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun FullScreenLyricsOverlay(
    currentSong: SongItem?,
    syncedLyrics: List<LyricLine>?,
    plainLyrics: String?,
    isLoading: Boolean,
    lyricsError: Throwable?,
    currentPositionProvider: () -> Int,
    contentReady: Boolean = true,
    playerBackgroundColor: Color,
    canvasArtwork: CanvasArtwork? = null,
    contentColor: Color,
    contentSecondary: Color,
    dynamicAccentColor: Color,
    onRetryLyrics: () -> Unit,
    onFetchLyrics: () -> Unit,
    onSeekTo: (Int) -> Unit,
    providerResults: Map<String, LyricsResult>,
    selectedProvider: String?,
    onProviderSelected: (String) -> Unit,
    syncOffsetMs: Long,
    onSyncOffsetChange: (Long) -> Unit,
    isPlaying: Boolean = false,
    playbackSpeed: Float = 1f,
    onPlayPauseClick: () -> Unit = {},
    duration: Long,
    onCollapse: (() -> Unit)? = null,
    isEmbedded: Boolean = false,
    onToggleTranslation: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val consumeClicks = remember { MutableInteractionSource() }
    var showProvidersSheet by remember { mutableStateOf(false) }
    var isDraggingSeekbar by remember { mutableStateOf(false) }
    var dragSeekProgress by remember { androidx.compose.runtime.mutableFloatStateOf(0f) }
    var lastSeekTime by remember { androidx.compose.runtime.mutableLongStateOf(0L) }
    var lastSeekPos by remember { androidx.compose.runtime.mutableFloatStateOf(0f) }

    val effectivePositionProvider = remember(currentPositionProvider, syncOffsetMs) {
        { (currentPositionProvider() + syncOffsetMs).toInt().coerceAtLeast(0) }
    }

    val view = LocalView.current
    DisposableEffect(view) {
        view.keepScreenOn = true
        onDispose {
            view.keepScreenOn = false
        }
    }

    Box(
        modifier = modifier
            .clickable(
                interactionSource = consumeClicks,
                indication = null,
                onClick = {}
            )
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(vertical = 12.dp)
        ) {
            // Top Bar Layout (Album Art + Song Title + Artist on Left, Actions on Right)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Top-Left Album Art + Song Title + Artist (Clickable to collapse to normal player)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(
                            enabled = onCollapse != null,
                            onClick = { onCollapse?.invoke() }
                        )
                        .padding(vertical = 4.dp, horizontal = 6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(8.dp))
                    ) {
                        if (currentSong?.thumbnailUrl != null) {
                            coil.compose.AsyncImage(
                                model = coil.request.ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
                                    .data(currentSong.thumbnailUrl)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Return to Player",
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Box(modifier = Modifier.fillMaxSize().background(Color.White.copy(alpha = 0.2f)))
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = currentSong?.title ?: "",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = contentColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = currentSong?.artist ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = contentColor.copy(alpha = 0.7f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                val context = androidx.compose.ui.platform.LocalContext.current
                val prefs = remember { androidx.preference.PreferenceManager.getDefaultSharedPreferences(context) }
                var isTranslationEnabled by remember { mutableStateOf(prefs.getBoolean("enable_lyrics_translation", false)) }
                val translationEngine = prefs.getString("lyrics_translation_engine", "gemini_api")

                androidx.compose.animation.AnimatedVisibility(
                    visible = isTranslationEnabled && translationEngine == "aicore",
                    enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.expandHorizontally(),
                    exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.shrinkHorizontally()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Rounded.Translate,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "AI翻訳オン",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                IconButton(onClick = {
                    isTranslationEnabled = !isTranslationEnabled
                    onToggleTranslation()
                }) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Rounded.Translate,
                        contentDescription = "Toggle Lyrics Translation",
                        tint = if (isTranslationEnabled) MaterialTheme.colorScheme.primary else contentColor.copy(alpha = 0.8f),
                        modifier = Modifier.size(24.dp)
                    )
                }

                IconButton(onClick = { showProvidersSheet = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Change Lyrics Provider",
                        tint = contentColor.copy(alpha = 0.8f),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                when {
                    !contentReady -> Unit

                    !syncedLyrics.isNullOrEmpty() -> {
                        SyncedLyricsCompose(
                            onSeekTo = onSeekTo,
                            lyrics = syncedLyrics,
                            currentPositionProvider = effectivePositionProvider,
                            contentColor = contentColor,
                            dynamicAccentColor = dynamicAccentColor,
                            currentSong = currentSong,
                            selectedProvider = selectedProvider,
                            providerResults = providerResults,
                            isPlayingProvider = { isPlaying },
                            playbackSpeedProvider = { playbackSpeed },
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    !plainLyrics.isNullOrBlank() -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = 16.dp, vertical = 28.dp)
                        ) {
                            Text(
                                text = plainLyrics,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = contentColor.copy(alpha = 0.9f)
                            )
                            LyricsMetadataFooter(
                                currentSong = currentSong,
                                selectedProvider = selectedProvider,
                                providerResults = providerResults,
                                contentColor = contentColor
                            )
                        }
                    }

                    isLoading -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            LinearWavyProgressIndicator(
                                modifier = Modifier
                                    .fillMaxWidth(0.6f)
                                    .height(12.dp),
                                color = dynamicAccentColor
                            )
                            Text(
                                text = androidx.compose.ui.res.stringResource(com.codetrio.overdrive.R.string.text_searching_lyrics_across_providers),
                                style = MaterialTheme.typography.bodyMedium,
                                color = contentColor.copy(alpha = 0.7f)
                            )
                        }
                    }

                    lyricsError != null -> {
                        LyricsErrorState(
                            message = lyricsError.message ?: "Lyrics not found",
                            onRetry = onRetryLyrics
                        )
                    }

                    else -> {
                        LyricsErrorState(
                            message = "Lyrics are not loaded yet",
                            onRetry = onFetchLyrics
                        )
                    }
                }
            }

            // Time Progress Bar
            if (currentSong != null && contentReady) {
                val currentPosition = currentPositionProvider()
                val safeDur = if (duration > 0) duration.toFloat() else 1f
                val progressRatio = (currentPosition.toFloat() / safeDur).coerceIn(0f, 1f)

                val isWaitingForPlayer = remember(progressRatio, lastSeekTime, lastSeekPos) {
                    val elapsed = System.currentTimeMillis() - lastSeekTime
                    val diff = kotlin.math.abs(progressRatio - lastSeekPos)
                    elapsed < 1000 && diff > 0.02f
                }

                val displayProgress = when {
                    isDraggingSeekbar -> dragSeekProgress
                    isWaitingForPlayer -> lastSeekPos
                    else -> progressRatio
                }

                val currentPos = (displayProgress * safeDur).toLong()
                
                // Play/Pause morphing button state
                val playPauseMorphProgress by animateFloatAsState(
                    targetValue = if (isPlaying) 1f else 0f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    ),
                    label = "LyricsPlayPauseMorph"
                )
                val playPauseMorph = remember {
                    Morph(MaterialShapes.Circle, MaterialShapes.Square)
                }
                val playPauseShape = MorphShape(playPauseMorph, playPauseMorphProgress)

                val playInteractionSource = remember { MutableInteractionSource() }
                val isPlayPressed by playInteractionSource.collectIsPressedAsState()
                val pressScale by animateFloatAsState(
                    targetValue = if (isPlayPressed) 0.88f else 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    ),
                    label = "LyricsPlayPressScale"
                )


            }

            if (showProvidersSheet && contentReady) {
                val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                ModalBottomSheet(
                    onDismissRequest = { showProvidersSheet = false },
                    sheetState = sheetState,
                    containerColor = playerBackgroundColor,
                    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                    dragHandle = { BottomSheetDefaults.DragHandle(color = contentColor.copy(alpha = 0.4f)) },
                    scrimColor = Color.Black.copy(alpha = 0.3f)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(bottom = 12.dp)
                    ) {
                        UnifiedLyricsBottomSheetContent(
                            providerResults = providerResults,
                            selectedProvider = selectedProvider,
                            syncOffsetMs = syncOffsetMs,
                            onSyncOffsetChange = onSyncOffsetChange,
                            onProviderSelected = onProviderSelected,
                            onRefindClick = onRetryLyrics,
                            accentColor = dynamicAccentColor,
                            contentColor = contentColor,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun UnifiedLyricsBottomSheetContent(
    providerResults: Map<String, LyricsResult>,
    selectedProvider: String?,
    syncOffsetMs: Long,
    onSyncOffsetChange: (Long) -> Unit,
    onProviderSelected: (String) -> Unit,
    onRefindClick: () -> Unit,
    accentColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    var activeTabIndex by rememberSaveable { mutableIntStateOf(0) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            ),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Expressive Material 3 Segmented Tab Switcher
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
        ) {
            SegmentedButton(
                selected = activeTabIndex == 0,
                onClick = { activeTabIndex = 0 },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                modifier = Modifier.weight(1f),
                icon = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_lyrics),
                        contentDescription = "Providers",
                        modifier = Modifier.size(18.dp)
                    )
                },
                colors = SegmentedButtonDefaults.colors(
                    activeContainerColor = accentColor.copy(alpha = 0.22f),
                    activeContentColor = accentColor,
                    inactiveContainerColor = contentColor.copy(alpha = 0.05f),
                    inactiveContentColor = contentColor.copy(alpha = 0.7f)
                )
            ) {
                Text(
                    text = androidx.compose.ui.res.stringResource(com.codetrio.overdrive.R.string.text_providers),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            SegmentedButton(
                selected = activeTabIndex == 1,
                onClick = { activeTabIndex = 1 },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                modifier = Modifier.weight(1f),
                icon = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_timer),
                        contentDescription = "Sync Timing",
                        modifier = Modifier.size(18.dp)
                    )
                },
                colors = SegmentedButtonDefaults.colors(
                    activeContainerColor = accentColor.copy(alpha = 0.22f),
                    activeContentColor = accentColor,
                    inactiveContainerColor = contentColor.copy(alpha = 0.05f),
                    inactiveContentColor = contentColor.copy(alpha = 0.7f)
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = androidx.compose.ui.res.stringResource(com.codetrio.overdrive.R.string.text_sync_control),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                    if (syncOffsetMs != 0L) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(accentColor)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = String.format(java.util.Locale.US, "%+.1fs", syncOffsetMs / 1000f),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }
                    }
                }
            }
        }

        // Tab Content with Seamless Animated Horizontal Transition
        AnimatedContent(
            targetState = activeTabIndex,
            transitionSpec = {
                if (targetState > initialState) {
                    (slideInHorizontally(
                        initialOffsetX = { width -> width },
                        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow)
                    ) + fadeIn(animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow)))
                        .togetherWith(
                            slideOutHorizontally(
                                targetOffsetX = { width -> -width },
                                animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow)
                            ) + fadeOut(animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow))
                        )
                } else {
                    (slideInHorizontally(
                        initialOffsetX = { width -> -width },
                        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow)
                    ) + fadeIn(animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow)))
                        .togetherWith(
                            slideOutHorizontally(
                                targetOffsetX = { width -> width },
                                animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow)
                            ) + fadeOut(animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow))
                        )
                }
            },
            label = "LyricsSheetTabTransition",
            modifier = Modifier.fillMaxWidth()
        ) { tabIndex ->
            when (tabIndex) {
                0 -> {
                    ProvidersListTab(
                        providerResults = providerResults,
                        selectedProvider = selectedProvider,
                        onProviderSelected = onProviderSelected,
                        onRefindClick = onRefindClick,
                        accentColor = accentColor,
                        contentColor = contentColor,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                1 -> {
                    SyncControlTab(
                        syncOffsetMs = syncOffsetMs,
                        onSyncOffsetChange = onSyncOffsetChange,
                        accentColor = accentColor,
                        contentColor = contentColor,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ProvidersListTab(
    providerResults: Map<String, LyricsResult>,
    selectedProvider: String?,
    onProviderSelected: (String) -> Unit,
    onRefindClick: () -> Unit,
    accentColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    val knownProviders = remember {
        listOf(
            "Local Cache",
            "EmbeddedID3",
            "YouTube Music",
            "YouLyPlus",
            "YouTube Subtitle",
            "Paxsenix: Apple Music",
            "Paxsenix: Spotify",
            "Paxsenix: Musixmatch",
            "Paxsenix: Netease",
            "Paxsenix: YouTube",
            "SyncLRC",
            "LrcLib",
            "KuGou",
            "BetterLyrics",
            "SimpMusic"
        )
    }

    val sortedProviders = remember(providerResults, selectedProvider) {
        knownProviders.sortedWith(
            compareByDescending<String> { selectedProvider == it }
                .thenByDescending { providerResults[it]?.hasLyrics() == true }
                .thenByDescending { !providerResults.containsKey(it) }
        )
    }

    val isSearchingAny = knownProviders.any { !providerResults.containsKey(it) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 340.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)
        ) {
            sortedProviders.forEachIndexed { index, provider ->
                val result = providerResults[provider]

                val isSelected = selectedProvider == provider || (selectedProvider == null && result != null && result.confidence >= 0f && result == providerResults.values.filter { it.confidence >= 0f && it.hasLyrics() }.maxWithOrNull(
                    compareBy<LyricsResult> { it.isWordByWord }
                        .thenBy { it.isSynced }
                        .thenBy { it.providerName?.startsWith("BetterLyrics") == true }
                        .thenBy { it.providerName == "SyncLRC" }
                        .thenBy { it.confidence }
                ))

                val hasData = result != null && result.hasLyrics() && result.confidence >= 0f

                val displayName = when (provider) {
                    "EmbeddedID3" -> "Embedded ID3"
                    else -> provider
                }

                val supportingText = when {
                    !providerResults.containsKey(provider) -> "Searching..."
                    result == null -> "Searching..."
                    !hasData -> "No lyrics found"
                    result.isWordByWord -> "★ Karaoke (Word-by-word)"
                    result.isSynced -> "Synced (LRC)"
                    else -> "Plain Text"
                }

                val shape = getSegmentedShape(index = index, count = sortedProviders.size)

                val itemBgColor = when {
                    isSelected -> accentColor.copy(alpha = 0.18f)
                    hasData -> contentColor.copy(alpha = 0.08f)
                    !providerResults.containsKey(provider) -> contentColor.copy(alpha = 0.04f)
                    else -> contentColor.copy(alpha = 0.02f)
                }

                ListItem(
                    selected = isSelected,
                    onClick = { onProviderSelected(provider) },
                    supportingContent = {
                        Text(text = supportingText)
                    },
                    leadingContent = {
                        val iconRes = when (provider) {
                            "YouTube Music", "Paxsenix: YouTube" -> R.drawable.ic_youtube_music
                            else -> R.drawable.ic_lyrics
                        }
                        Icon(
                            painter = painterResource(id = iconRes),
                            contentDescription = provider,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    trailingContent = {
                        if (isSelected) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_check),
                                contentDescription = "Selected",
                                modifier = Modifier.size(20.dp)
                            )
                        } else if (!providerResults.containsKey(provider)) {
                            androidx.compose.material3.CircularWavyProgressIndicator(
                                color = accentColor.copy(alpha = 0.7f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    },
                    colors = ListItemDefaults.colors(
                        containerColor = itemBgColor,
                        selectedContainerColor = itemBgColor,
                        contentColor = contentColor.copy(alpha = if (hasData || !providerResults.containsKey(provider)) 0.95f else 0.4f),
                        selectedContentColor = accentColor,
                        leadingContentColor = contentColor.copy(alpha = if (hasData || !providerResults.containsKey(provider)) 0.85f else 0.4f),
                        selectedLeadingContentColor = accentColor,
                        trailingContentColor = contentColor.copy(alpha = 0.8f),
                        selectedTrailingContentColor = accentColor,
                        supportingContentColor = contentColor.copy(alpha = if (hasData) 0.75f else 0.45f),
                        selectedSupportingContentColor = accentColor.copy(alpha = 0.85f),
                        disabledContainerColor = itemBgColor,
                        disabledContentColor = contentColor.copy(alpha = 0.3f),
                        disabledLeadingContentColor = contentColor.copy(alpha = 0.3f),
                        disabledTrailingContentColor = contentColor.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(shape)
                ) {
                    Text(
                        text = displayName,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }

        if (isSearchingAny) {
            LinearWavyProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp),
                color = accentColor
            )
        }

        val buttonInteractionSource = remember { MutableInteractionSource() }
        val buttonIsPressed by buttonInteractionSource.collectIsPressedAsState()

        val buttonCornerRadius by animateDpAsState(
            targetValue = if (buttonIsPressed) 28.dp else 16.dp,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            ),
            label = "refind_button_corners"
        )

        androidx.compose.material3.Button(
            onClick = onRefindClick,
            interactionSource = buttonInteractionSource,
            shape = RoundedCornerShape(buttonCornerRadius),
            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                containerColor = contentColor.copy(alpha = 0.12f),
                contentColor = contentColor
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_refresh),
                    contentDescription = "Refind and Research",
                    modifier = Modifier.size(20.dp),
                    tint = contentColor
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = androidx.compose.ui.res.stringResource(com.codetrio.overdrive.R.string.text_refind_re_search_all_providers),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = contentColor
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun SyncControlTab(
    syncOffsetMs: Long,
    onSyncOffsetChange: (Long) -> Unit,
    accentColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // ── Top Header Badge: Dynamic Sync Status ──
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(if (syncOffsetMs != 0L) accentColor.copy(alpha = 0.15f) else contentColor.copy(alpha = 0.08f))
                .padding(horizontal = 18.dp, vertical = 10.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_timer),
                contentDescription = "Sync Offset",
                tint = if (syncOffsetMs != 0L) accentColor else contentColor.copy(alpha = 0.7f),
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = if (syncOffsetMs == 0L) "0.0s (In Sync)" else String.format(java.util.Locale.US, "%+.2fs Offset", syncOffsetMs / 1000f),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = if (syncOffsetMs != 0L) accentColor else contentColor
            )
        }

        // ── 100% Native Google Material 3 Connected Preset Button Row ──
        // ── 100% Native Google Material 3 Connected Preset Button Row ──
        val presetValues = remember {
            listOf(
                -500L to "-0.5s",
                -100L to "-0.1s",
                0L to "Reset", // Exact Center (Index 2)
                100L to "+0.1s",
                500L to "+0.5s"
            )
        }

        // ── Connected Material 3 Expressive Presets Button Group (Clickable Action Buttons with Neighbor Pushing) ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            presetValues.forEachIndexed { index, (delta, label) ->
                val isReset = delta == 0L
                val interactionSource = remember { MutableInteractionSource() }
                val isPressed by interactionSource.collectIsPressedAsState()

                // Dynamic M3 Expressive Neighbor Pushing Weight Physics (Clickable Press Motion)
                val targetWeight = if (isPressed) 1.55f else 1.00f
                val animatedWeight by animateFloatAsState(
                    targetValue = targetWeight,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    ),
                    label = "clickableNeighborPushingWeight"
                )

                // 100% Fixed Stable Connected Shapes
                val buttonShape = when (index) {
                    0 -> RoundedCornerShape(topStart = 20.dp, bottomStart = 20.dp, topEnd = 6.dp, bottomEnd = 6.dp)
                    2 -> RoundedCornerShape(8.dp)
                    4 -> RoundedCornerShape(topEnd = 20.dp, bottomEnd = 20.dp, topStart = 6.dp, bottomStart = 6.dp)
                    else -> RoundedCornerShape(6.dp)
                }

                val containerColor = when {
                    isReset && syncOffsetMs != 0L -> accentColor.copy(alpha = 0.18f)
                    isPressed -> contentColor.copy(alpha = 0.14f)
                    else -> contentColor.copy(alpha = 0.06f)
                }

                val itemContentColor = when {
                    isReset && syncOffsetMs != 0L -> accentColor
                    else -> contentColor
                }

                FilledTonalButton(
                    onClick = {
                        if (isReset) {
                            onSyncOffsetChange(0L)
                        } else {
                            onSyncOffsetChange(syncOffsetMs + delta)
                        }
                    },
                    interactionSource = interactionSource,
                    shape = buttonShape,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = containerColor,
                        contentColor = itemContentColor
                    ),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                    modifier = Modifier
                        .weight(animatedWeight)
                        .height(44.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (isReset) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_refresh),
                                contentDescription = "Reset",
                                tint = itemContentColor,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (isReset && syncOffsetMs != 0L) FontWeight.ExtraBold else FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

private fun getSegmentedShape(index: Int, count: Int): androidx.compose.ui.graphics.Shape {
    val outer = 24.dp
    val inner = 4.dp
    return when {
        count <= 1 -> RoundedCornerShape(outer)
        index == 0 -> RoundedCornerShape(topStart = outer, topEnd = outer, bottomStart = inner, bottomEnd = inner)
        index == count - 1 -> RoundedCornerShape(topStart = inner, topEnd = inner, bottomStart = outer, bottomEnd = outer)
        else -> RoundedCornerShape(inner)
    }
}

private fun formatLyricsTime(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(java.util.Locale.US, "%02d:%02d", minutes, seconds)
}

