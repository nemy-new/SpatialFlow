package com.codetrio.overdrive.ui.player

import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material.icons.automirrored.rounded.QueueMusic
import androidx.compose.material.icons.automirrored.rounded.PlaylistAdd
import androidx.compose.material.icons.rounded.Cast
import androidx.compose.material.icons.rounded.CastConnected
import com.codetrio.overdrive.cast.CastState
import com.codetrio.overdrive.ui.components.showCastDialog

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.net.toUri
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.zIndex
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.geometry.Offset
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForwardIos
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.res.stringResource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.background
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.palette.graphics.Palette
import coil.ImageLoader
import coil.imageLoader
import coil.request.ImageRequest
import com.codetrio.overdrive.MainActivity
import com.codetrio.overdrive.R
import com.codetrio.overdrive.data.lyrics.LyricLine
import com.codetrio.overdrive.data.lyrics.LyricsResult
import com.codetrio.overdrive.model.SongItem
import com.codetrio.overdrive.player.queue.QueueTrackListItem
import com.codetrio.overdrive.viewmodel.PlayerSharedViewModel
import sh.calvin.reorderable.rememberReorderableLazyListState
import sh.calvin.reorderable.ReorderableItem
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Search
import androidx.compose.ui.draw.scale
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Stateful wrapper for the FullPlayer UI component.
 * Decouples the UI component from ViewModel, Activity, and direct permission handling.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun FullPlayerScreen(
    activity: MainActivity,
    viewModel: PlayerSharedViewModel,
    uiState: PlayerUiState,
    songList: List<SongItem>,
    accentColor: Color,
    context: Context,
    onCollapse: () -> Unit,
    dragModifier: Modifier = Modifier,
    onTabletPlaceholderPositioned: (Offset) -> Unit = {},
    sheetRootCoordinates: LayoutCoordinates? = null,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    val hapticManager = viewModel.hapticManager
    val isLyricsModeEnabled by viewModel.isLyricsModeEnabled.collectAsStateWithLifecycle()
    val syncedLyrics by viewModel.syncedLyrics.collectAsStateWithLifecycle()
    val plainLyrics by viewModel.plainLyrics.collectAsStateWithLifecycle()
    val translatedPlainLyrics by viewModel.translatedPlainLyrics.collectAsStateWithLifecycle()
    val isLyricsLoading by viewModel.isLyricsLoading.collectAsStateWithLifecycle()
    val lyricsError by viewModel.lyricsError.collectAsStateWithLifecycle()
    val providerResults by viewModel.providerResults.collectAsStateWithLifecycle()
    val selectedProvider by viewModel.selectedProvider.collectAsStateWithLifecycle()
    val currentPositionState = viewModel.currentPosition.collectAsStateWithLifecycle()
    val isAutoplayEnabled by viewModel.isAutoplayEnabled.collectAsStateWithLifecycle()
    val videoAspectRatio by viewModel.videoAspectRatio.collectAsStateWithLifecycle()

    // Handle back button when lyrics mode is enabled
    BackHandler(enabled = isLyricsModeEnabled) {
        viewModel.setLyricsModeEnabled(false)
    }

    // Dynamically register the active Compose view hosting FullPlayer inside PlayerHapticManager.
    DisposableEffect(view, hapticManager) {
        hapticManager?.attachView(view)
        onDispose {
            hapticManager?.detachView()
        }
    }

    // Modern Compose-way of handling audio recording permission
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.setHapticsEnabled(true)
        } else {
            com.codetrio.overdrive.ui.SnackbarController.showMessage("Microphone permission required for haptics")
        }
    }
    
    val localPlaylists by viewModel.localPlaylistsFlow.collectAsStateWithLifecycle(initialValue = emptyList())
    var showAddToPlaylistDialog by remember { mutableStateOf(false) }
    var showCreatePlaylistDialog by remember { mutableStateOf(false) }
    var showArtistSelectionDialog by remember { mutableStateOf(false) }
    var artistSelectionList by remember { mutableStateOf<List<String>>(emptyList()) }

    if (showAddToPlaylistDialog) {
        com.codetrio.overdrive.ui.LocalPlaylistPickerDialog(
            playlists = localPlaylists,
            onCreateNew = {
                showCreatePlaylistDialog = true
                showAddToPlaylistDialog = false
            },
            onPlaylistSelected = { playlist ->
                val currentSong = uiState.currentSong
                if (currentSong != null) {
                    viewModel.addSongToLocalPlaylist(playlist.id, currentSong)
                    com.codetrio.overdrive.ui.SnackbarController.showMessage("Added to playlist: ${playlist.title}")
                }
                showAddToPlaylistDialog = false
            },
            onDismiss = { showAddToPlaylistDialog = false }
        )
    }

    if (showCreatePlaylistDialog) {
        com.codetrio.overdrive.ui.CreateLocalPlaylistDialog(
            onConfirm = { name ->
                viewModel.createLocalPlaylist(name)
                showCreatePlaylistDialog = false
                showAddToPlaylistDialog = true
            },
            onDismiss = {
                showCreatePlaylistDialog = false
                showAddToPlaylistDialog = true
            }
        )
    }

    if (showArtistSelectionDialog && artistSelectionList.size > 1) {
        AlertDialog(
            onDismissRequest = { showArtistSelectionDialog = false },
            title = {
                Text(
                    text = "アーティストを選択",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 19.sp)
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "表示するアーティストを選択してください",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    artistSelectionList.forEach { singleArtistName ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .clickable {
                                    showArtistSelectionDialog = false
                                    onCollapse()
                                    activity.showArtistPage(null, singleArtistName)
                                },
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(accentColor.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Person,
                                            contentDescription = null,
                                            tint = accentColor,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                    Text(
                                        text = singleArtistName,
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.ArrowForwardIos,
                                    contentDescription = null,
                                    tint = accentColor,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showArtistSelectionDialog = false }) {
                    Text("閉じる")
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }

    FullPlayer(
        viewModel = viewModel,
        uiState = uiState,
        songList = songList,
        accentColor = accentColor,
        isLyricsModeEnabled = isLyricsModeEnabled,
        syncedLyrics = syncedLyrics,
        plainLyrics = plainLyrics,
        translatedPlainLyrics = translatedPlainLyrics,
        isLyricsLoading = isLyricsLoading,
        lyricsError = lyricsError,
        providerResults = providerResults,
        selectedProvider = selectedProvider,
        videoAspectRatio = videoAspectRatio,
        onProviderSelected = { viewModel.selectLyricsProvider(it) },
        currentPositionProvider = { currentPositionState.value },
        isAutoplayEnabled = isAutoplayEnabled,
        onAutoplayToggle = { viewModel.setAutoplayEnabled(!isAutoplayEnabled) },
        onCollapse = onCollapse,
        onPlayPauseClick = {
            if (uiState.isPlaying) viewModel.pauseAudio() else viewModel.playAudio()
        },
        onPreviousClick = {
            viewModel.playPreviousSong()
        },
        onNextClick = {
            viewModel.playNextSong(force = true)
        },
        onHapticChipClick = {
            val hasPermission = androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.RECORD_AUDIO
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED

            if (!hasPermission) {
                permissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
            } else {
                viewModel.setHapticsEnabled(!uiState.isHapticsEnabled)
            }
        },
        onLyricsModeChanged = { enabled ->
            viewModel.setLyricsModeEnabled(enabled)
        },
        onFetchLyrics = {
            viewModel.fetchLyricsForCurrentSong()
        },
        onRetryLyrics = {
            viewModel.retryLyrics()
        },
        onSeekTo = { position ->
            viewModel.seekTo(position)
        },
        onFavoriteClick = {
            viewModel.toggleFavorite()
        },
        onDislikeClick = {
            viewModel.toggleDislike()
        },
        onSaveClick = {
            showAddToPlaylistDialog = true
        },
        onArtistClick = { artistId, artistName ->
            val parsed = parseArtistNames(artistName)
            if (parsed.size > 1) {
                artistSelectionList = parsed
                showArtistSelectionDialog = true
            } else {
                onCollapse()
                activity.showArtistPage(artistId, artistName)
            }
        },
        onAlbumClick = { albumId, _ ->
            onCollapse()
            activity.showAlbumPage(albumId)
        },
        onTabletPlaceholderPositioned = onTabletPlaceholderPositioned,
        sheetRootCoordinates = sheetRootCoordinates,
        dragModifier = dragModifier,
        modifier = modifier
    )
}

/**
 * Purely stateless UI representation of the Full Player.
 * Does not depend on ViewModels or Activities, ensuring great previewability and testability.
 */
@RequiresApi(Build.VERSION_CODES.Q)
@SuppressLint("ConfigurationScreenWidthHeight")
@OptIn(
    ExperimentalMaterial3ExpressiveApi::class,
    ExperimentalMaterial3Api::class
)
@Composable
fun FullPlayer(
    viewModel: PlayerSharedViewModel,
    uiState: PlayerUiState,
    songList: List<SongItem>,
    accentColor: Color,
    isLyricsModeEnabled: Boolean,
    syncedLyrics: List<LyricLine>?,
    plainLyrics: String?,
    translatedPlainLyrics: String? = null,
    isLyricsLoading: Boolean,
    lyricsError: Throwable?,
    currentPositionProvider: () -> Int,
    onCollapse: () -> Unit,
    onPlayPauseClick: () -> Unit,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    onHapticChipClick: () -> Unit,
    onLyricsModeChanged: (Boolean) -> Unit,
    onFetchLyrics: () -> Unit,
    onRetryLyrics: () -> Unit,
    onSeekTo: (Int) -> Unit,
    onFavoriteClick: () -> Unit,
    onDislikeClick: () -> Unit,
    onSaveClick: () -> Unit,
    providerResults: Map<String, LyricsResult>,
    selectedProvider: String?,
    onProviderSelected: (String) -> Unit,
    isAutoplayEnabled: Boolean,
    onAutoplayToggle: (Boolean) -> Unit,
    videoAspectRatio: Float,
    onArtistClick: (String?, String) -> Unit = { _, _ -> },
    onAlbumClick: (String?, String) -> Unit = { _, _ -> },
    onTabletPlaceholderPositioned: (androidx.compose.ui.geometry.Offset) -> Unit = {},
    sheetRootCoordinates: androidx.compose.ui.layout.LayoutCoordinates? = null,
    dragModifier: Modifier = Modifier,
    modifier: Modifier = Modifier
) {
    val isDark = androidx.compose.material3.MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val isMvMode by viewModel.isMvMode.collectAsStateWithLifecycle()
    val hasMusicVideo by viewModel.hasMusicVideo.collectAsStateWithLifecycle()
    val musicVideoUrl by viewModel.musicVideoUrl.collectAsStateWithLifecycle()
    val isEffectiveMvMode = isMvMode && hasMusicVideo && !musicVideoUrl.isNullOrBlank()
    val canvasArtwork by viewModel.canvasArtwork.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("AppSettings", Context.MODE_PRIVATE) }
    var showVolumeSlider by remember { mutableStateOf(prefs.getBoolean("show_volume_slider", true)) }
    var showMusicHapticsOption by remember { mutableStateOf(prefs.getBoolean("show_music_haptics_option", false)) }
    var showPlayerStatsOption by remember {
        mutableStateOf(prefs.getBoolean("developer_mode", false) && prefs.getBoolean("show_player_stats", false))
    }
    var showPlayerThemeChipOption by remember {
        mutableStateOf(prefs.getBoolean("developer_mode", false) && prefs.getBoolean("show_player_theme_chip", false))
    }
    val isEffectsExpanded by viewModel.isEffectsExpanded.collectAsStateWithLifecycle()
    var showStatsForNerdsDialog by remember { mutableStateOf(false) }
    var showPlayerThemeSheet by remember { mutableStateOf(false) }
    var playerTheme by remember { mutableStateOf(prefs.getString("player_theme", "fluid") ?: "fluid") }
    var showAnimatedArt by remember { mutableStateOf(prefs.getBoolean("show_animated_art", true)) }

    DisposableEffect(prefs) {
        val listener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
            if (key == "show_volume_slider") {
                showVolumeSlider = sharedPreferences.getBoolean(key, true)
            } else if (key == "show_music_haptics_option") {
                showMusicHapticsOption = sharedPreferences.getBoolean(key, false)
            } else if (key == "show_player_stats" || key == "developer_mode") {
                showPlayerStatsOption = sharedPreferences.getBoolean("developer_mode", false) && sharedPreferences.getBoolean("show_player_stats", false)
            } else if (key == "show_player_theme_chip" || key == "developer_mode") {
                showPlayerThemeChipOption = sharedPreferences.getBoolean("developer_mode", false) && sharedPreferences.getBoolean("show_player_theme_chip", false)
            } else if (key == "player_theme") {
                playerTheme = sharedPreferences.getString(key, "fluid") ?: "fluid"
            } else if (key == "show_animated_art") {
                showAnimatedArt = sharedPreferences.getBoolean(key, true)
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        onDispose {
            prefs.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }
    
    val isStatic = playerTheme == "static"
    val isImmersionLike = playerTheme == "immersion" || playerTheme == "immersion-v2"
    val isVinyl = playerTheme == "vinyl"
    val hasCanvas = !isStatic && showAnimatedArt && canvasArtwork != null

    val isTextColorDark = !isDark && (isStatic || isVinyl)
    val contentColor = when {
        isImmersionLike -> Color.White
        isVinyl -> if (isDark) Color.White else Color(0xFF111215)
        isTextColorDark -> Color(0xFF1C1B1F)
        else -> Color.White
    }
    val contentSecondary = when {
        isImmersionLike -> Color.White.copy(alpha = 0.75f)
        isVinyl -> if (isDark) Color.White.copy(alpha = 0.70f) else Color(0xFF111215).copy(alpha = 0.65f)
        isTextColorDark -> Color(0xFF1C1B1F).copy(alpha = 0.6f)
        else -> Color.White.copy(alpha = 0.6f)
    }

    val dynamicAccentColor = remember(accentColor, isTextColorDark, playerTheme, isDark) {
        when {
            isImmersionLike -> Color.White
            isVinyl -> if (isDark) Color(0xFFFFFFFF) else Color(0xFF111215)
            else -> {
                val hsl = FloatArray(3)
                androidx.core.graphics.ColorUtils.colorToHSL(accentColor.toArgb(), hsl)
                if (hsl[1] < 0.08f) {
                    if (isTextColorDark) Color(0xFF1C1B1F) else Color.White
                } else {
                    if (!isTextColorDark) {
                        accentColor
                    } else {
                        hsl[2] = hsl[2].coerceAtMost(0.45f)
                        hsl[1] = hsl[1].coerceAtLeast(0.6f)
                        Color(androidx.core.graphics.ColorUtils.HSLToColor(hsl))
                    }
                }
            }
        }
    }

    val sideButtonContainerColor = when {
        isImmersionLike -> Color.White.copy(alpha = 0.15f)
        isVinyl -> if (isDark) Color.White.copy(alpha = 0.12f) else Color(0xFF111215).copy(alpha = 0.08f)
        else -> contentColor.copy(alpha = if (isDark) 0.08f else 0.06f)
    }

    val sideButtonContentColor = when {
        isImmersionLike -> Color.White
        isVinyl -> if (isDark) Color.White else Color(0xFF111215)
        else -> contentColor
    }

    val playButtonContainerColor = when {
        isImmersionLike -> Color.White.copy(alpha = 0.25f)
        isVinyl -> if (isDark) Color(0xFFFFFFFF) else Color(0xFF111215)
        else -> dynamicAccentColor
    }

    val playButtonContentColor = when {
        isImmersionLike -> Color.White
        isVinyl -> if (isDark) Color(0xFF111215) else Color(0xFFFFFFFF)
        else -> (if (isDark) Color(0xFF1C1B1F) else Color.White)
    }

    val playerBackgroundColor = remember(uiState.playerBackgroundColor, isTextColorDark) {
        val baseColor = Color(uiState.playerBackgroundColor)
        val hsl = FloatArray(3)
        androidx.core.graphics.ColorUtils.colorToHSL(baseColor.toArgb(), hsl)
        val isMonochrome = hsl[1] < 0.06f
        if (!isTextColorDark) {
            hsl[2] = 0.155f
            hsl[1] = if (isMonochrome) 0f else hsl[1].coerceIn(0.32f, 0.54f)
            Color(androidx.core.graphics.ColorUtils.HSLToColor(hsl))
        } else {
            hsl[2] = 0.835f
            hsl[1] = if (isMonochrome) 0f else hsl[1].coerceIn(0.30f, 0.48f)
            Color(androidx.core.graphics.ColorUtils.HSLToColor(hsl))
        }
    }

    val haptic = LocalHapticFeedback.current
    val hasLyrics = !syncedLyrics.isNullOrEmpty() || !plainLyrics.isNullOrBlank()
    
    val currentSongId = uiState.currentSong?.id
    
    androidx.compose.runtime.LaunchedEffect(currentSongId, hasLyrics) {
        val config = context.resources.configuration
        if (config.screenWidthDp >= 600) {
            onLyricsModeChanged(hasLyrics)
        }
    }
    
    // Sliding Queue Drawer State
    val isQueueExpanded by viewModel.isQueueExpanded.collectAsStateWithLifecycle()
    val isShuffleEnabled by viewModel.isShuffleEnabled.collectAsStateWithLifecycle()
    val repeatMode by viewModel.repeatMode.collectAsStateWithLifecycle()
    val currentSongIndex by viewModel.currentSongIndex.collectAsStateWithLifecycle()
    val castState by viewModel.castState.collectAsStateWithLifecycle()

    // Unify BackHandler to collapse the sliding Queue drawer first
    BackHandler(enabled = isLyricsModeEnabled || isQueueExpanded || isEffectsExpanded) {
        if (isEffectsExpanded) {
            viewModel.setEffectsExpanded(false)
        } else if (isLyricsModeEnabled) {
            onLyricsModeChanged(false)
        } else if (isQueueExpanded) {
            viewModel.setQueueExpanded(false)
        }
    }

    // Sleep Timer State & Controller Dialog state
    var showSleepTimerDialog by remember { mutableStateOf(false) }
    val sleepTimerMode by viewModel.sleepTimerMode.collectAsStateWithLifecycle()
    val sleepTimerEndTime by viewModel.sleepTimerEndTime.collectAsStateWithLifecycle()

    // ── Palette extraction pipeline (spec §2.2) ─────────────────────────────────
    // Triggered whenever the playing track's artwork changes.
    // Runs entirely on Dispatchers.IO — never blocks the main thread.
    // Downloads artwork → BitmapResolver.bitmapCompress (64 px square, RGB_565) →
    // AndroidX Palette.generate() → writes vibrant / darkVibrant / darkMuted into
    // PlayerPaletteState (global singleton). Falls back to Color.Black on missing swatch.
    // ── Palette extraction key ────────────────────────────────────────────────────
    // IMPORTANT: for local MP3 files, thumbnailUrl AND videoId are both null.
    // We MUST include getAlbumArtUri() first, otherwise artworkKey is null and
    // the LaunchedEffect short-circuits → PlayerPaletteState stays Color.Black → pitch black.
    val artworkKey = remember(uiState.currentSong) {
        uiState.currentSong?.let { song ->
            song.getAlbumArtUri()?.toString()       // local MP3 embedded art  (content://)
                ?: song.thumbnailUrl                 // YouTube Music stream
                ?: song.videoId?.let { "yt_$it" }   // YouTube video ID fallback
        }
    }
    val paletteContext = context
    LaunchedEffect(artworkKey) {
        if (artworkKey == null) return@LaunchedEffect

        withContext(Dispatchers.IO) {
            val song = uiState.currentSong ?: return@withContext
            // artworkData priority: local URI → thumbnail URL → YouTube fallback
            val artworkData: Any = song.getAlbumArtUri()
                ?: song.thumbnailUrl
                ?: song.videoId?.let { "https://img.youtube.com/vi/$it/hqdefault.jpg" }
                ?: return@withContext

            val loader = paletteContext.imageLoader
            try {
                val request = ImageRequest.Builder(paletteContext)
                    .data(artworkData)
                    .allowHardware(false) // hardware bitmaps cannot be read by Palette
                    .build()

                // Step 1 — download/load artwork
                val thisBitmap = loader.execute(request).drawable
                    ?.toBitmap()
                    ?.run { BitmapResolver.bitmapCompress(this) }  // 64 px, RGB_565

                if (thisBitmap != null) {
                    try {
                        // Step 2 — run AndroidX Palette on the compressed bitmap
                        val palette = Palette.from(thisBitmap).generate()

                        // Use dominantSwatch as non-black fallback when a specific
                        // swatch is missing (dark/monochromatic art like "After Dark").
                        val dominant = palette.dominantSwatch?.rgb?.let { Color(it) }

                        PlayerPaletteState.vibrantColor.value =
                            palette.vibrantSwatch?.rgb?.let { Color(it) } ?: dominant ?: Color.Black

                        PlayerPaletteState.lightVibrantColor.value =
                            palette.lightVibrantSwatch?.rgb?.let { Color(it) } ?: palette.lightMutedSwatch?.rgb?.let { Color(it) } ?: dominant ?: Color.Black

                        PlayerPaletteState.darkVibrantColor.value =
                            palette.darkVibrantSwatch?.rgb?.let { Color(it) } ?: dominant ?: Color.Black

                        PlayerPaletteState.mutedColor.value =
                            palette.mutedSwatch?.rgb?.let { Color(it) } ?: dominant ?: Color.Black

                        PlayerPaletteState.darkMutedColor.value =
                            palette.darkMutedSwatch?.rgb?.let { Color(it) } ?: dominant ?: Color.Black

                        PlayerPaletteState.dominantColor.value =
                            dominant ?: Color.Black

                        // ── Smart Immersion Color Computation (Top, Bottom, Unified) ──
                        val w = thisBitmap.width
                        val h = thisBitmap.height
                        val halfH = (h / 2).coerceAtLeast(1)

                        // Helper to ensure ambient colors stay rich, elegant and high-contrast
                        fun sanitizeAmbient(rgb: Int, minL: Float, maxL: Float, minS: Float = 0.20f): Color {
                            val hsl = FloatArray(3)
                            androidx.core.graphics.ColorUtils.colorToHSL(rgb, hsl)
                            hsl[1] = hsl[1].coerceAtLeast(minS)
                            hsl[2] = hsl[2].coerceIn(minL, maxL)
                            return Color(androidx.core.graphics.ColorUtils.HSLToColor(hsl))
                        }

                        // 1. Top region extraction (header & artwork blend)
                        val topBitmap = android.graphics.Bitmap.createBitmap(thisBitmap, 0, 0, w, halfH)
                        val topPalette = Palette.from(topBitmap).generate()
                        val topSwatch = topPalette.vibrantSwatch
                            ?: topPalette.dominantSwatch
                            ?: topPalette.lightVibrantSwatch
                            ?: topPalette.mutedSwatch
                            ?: palette.vibrantSwatch
                            ?: palette.dominantSwatch
                        val topRgb = topSwatch?.rgb ?: dominant?.toArgb() ?: 0xFF2A3A30.toInt()
                        PlayerPaletteState.topImmersionColor.value = sanitizeAmbient(topRgb, minL = 0.24f, maxL = 0.48f)
                        topBitmap.recycle()

                        // 2. Bottom region extraction (controls & player bottom)
                        val bottomBitmap = android.graphics.Bitmap.createBitmap(thisBitmap, 0, halfH, w, h - halfH)
                        val bottomPalette = Palette.from(bottomBitmap).generate()
                        val bottomSwatch = bottomPalette.darkVibrantSwatch
                            ?: bottomPalette.darkMutedSwatch
                            ?: bottomPalette.dominantSwatch
                            ?: bottomPalette.vibrantSwatch
                            ?: palette.darkVibrantSwatch
                            ?: palette.darkMutedSwatch
                            ?: palette.dominantSwatch
                        val bottomRgb = bottomSwatch?.rgb ?: dominant?.toArgb() ?: 0xFF141E18.toInt()
                        PlayerPaletteState.bottomImmersionColor.value = sanitizeAmbient(bottomRgb, minL = 0.14f, maxL = 0.34f)
                        bottomBitmap.recycle()

                    } catch (_: Exception) { /* palette failure — keep existing colors */ }

                    thisBitmap.recycle() // ← always recycle to avoid OOM
                }
            } finally {
                loader.shutdown() // ← always shut down the loader
            }
        }
    }
    // ── End palette extraction ──────────────────────────────────────────────────

    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
    val isWideLandscape = isLandscape && configuration.screenWidthDp >= 600
    val isCompactLandscape = isLandscape && !isWideLandscape
    val isTablet = isWideLandscape
    val effectiveDragModifier = dragModifier

    AppleMusicBackground(
        song = uiState.currentSong,
        canvasArtwork = canvasArtwork,
        isPlaying = uiState.isPlaying,
        isLyricsModeEnabled = isLyricsModeEnabled,
        isMvMode = isMvMode,
        modifier = modifier
            .fillMaxSize()
            .then(effectiveDragModifier)
    ) {
        val density = androidx.compose.ui.platform.LocalDensity.current
        val statusBarTopDp = with(density) { androidx.compose.foundation.layout.WindowInsets.statusBars.getTop(this).toDp() }
        val dimens = com.codetrio.overdrive.ui.theme.LocalDimens.current

        val availableBodyHeight = (screenHeight - statusBarTopDp - 102.dp).coerceAtLeast(400.dp)

        val albumArtSize = when {
            isWideLandscape -> {
                val availableWidthDp = screenWidth - (dimens.screenMargin * 2)
                val rightPaneWidthDp = (availableWidthDp * 0.44f).coerceIn(320.dp, 540.dp)
                val maxLeftWidth = availableWidthDp - rightPaneWidthDp - 24.dp
                val maxLeftHeight = (screenHeight - statusBarTopDp - 240.dp).coerceAtLeast(180.dp)
                androidx.compose.ui.unit.min(maxLeftWidth * 0.88f, maxLeftHeight).coerceIn(180.dp, 440.dp)
            }
            isCompactLandscape -> {
                androidx.compose.ui.unit.min(220.dp, (screenHeight - statusBarTopDp - 36.dp).coerceAtLeast(140.dp))
            }
            else -> { // Portrait (Phone & Tablet)
                if (screenWidth >= 600.dp) {
                    androidx.compose.ui.unit.min(screenWidth * 0.65f, screenHeight * 0.40f).coerceIn(260.dp, 480.dp)
                } else {
                    val contentWidth = screenWidth - 44.dp
                    val maxArtRatio = if (showVolumeSlider) 0.415f else 0.455f
                    val baseArtSize = androidx.compose.ui.unit.min(contentWidth, availableBodyHeight * maxArtRatio)
                    baseArtSize.coerceIn(195.dp, 350.dp)
                }
            }
        }

        val controlsAreaHeightDp = if (showVolumeSlider) 390.dp else 340.dp
        val availableTopSpaceDp = screenHeight - statusBarTopDp - controlsAreaHeightDp
        val squareTopOffsetDp = statusBarTopDp + ((availableTopSpaceDp - albumArtSize) / 2f).coerceIn(8.dp, 24.dp)
        val squareCenterYDp = squareTopOffsetDp + (albumArtSize / 2f)
        val albumArtHeight = albumArtSize
        val topOffset = squareCenterYDp - (albumArtHeight / 2f)
        val totalGroupHeightDp = albumArtHeight + (if (showVolumeSlider) 380.dp else 340.dp)

        var tabletRightPaneTab by remember { androidx.compose.runtime.mutableIntStateOf(1) } // 0: Queue, 1: Lyrics, 2: Related

        AnimatedVisibility(
            visible = isTablet || !isLyricsModeEnabled,
            enter = fadeIn(animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow)),
            exit = fadeOut(animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow)),
            modifier = Modifier.fillMaxSize()
        ) {
            @OptIn(ExperimentalFoundationApi::class)
            @Composable
            fun FullPlayerPhoneLayout() {
                val haptic = LocalHapticFeedback.current
                val isQueueExpanded by viewModel.isQueueExpanded.collectAsStateWithLifecycle()

                val queueExpansionProgress by animateFloatAsState(
                    targetValue = if (isQueueExpanded) 1f else 0f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    ),
                    label = "queueExpansionProgress"
                )

                val lazyListState = rememberLazyListState()
                var localSongList by remember(songList) { mutableStateOf(songList) }
                var lastMovedFrom by remember { mutableStateOf<Int?>(null) }
                var lastMovedTo by remember { mutableStateOf<Int?>(null) }
                var searchQuery by remember { mutableStateOf("") }
                var isSearchExpanded by remember { mutableStateOf(false) }

                val filteredSongList = remember(localSongList, searchQuery) {
                    if (searchQuery.isEmpty()) {
                        localSongList
                    } else {
                        localSongList.filter {
                            it.title.contains(searchQuery, ignoreCase = true) ||
                            it.artist.contains(searchQuery, ignoreCase = true)
                        }
                    }
                }

                val reorderableState = rememberReorderableLazyListState(
                    lazyListState = lazyListState,
                    onMove = { from, to ->
                        val fromIndex = localSongList.indexOfFirst { it.id == from.key }
                        val toIndex = localSongList.indexOfFirst { it.id == to.key }
                        if (fromIndex != -1 && toIndex != -1 && fromIndex != toIndex) {
                            localSongList = localSongList.toMutableList().apply {
                                add(toIndex, removeAt(fromIndex))
                            }
                            if (lastMovedFrom == null) {
                                lastMovedFrom = fromIndex
                            }
                            lastMovedTo = toIndex
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        }
                    }
                )

                LaunchedEffect(songList) {
                    if (!reorderableState.isAnyItemDragging) {
                        localSongList = songList
                    }
                }

                LaunchedEffect(reorderableState.isAnyItemDragging) {
                    if (!reorderableState.isAnyItemDragging) {
                        val fromIdx = lastMovedFrom
                        val toIdx = lastMovedTo
                        lastMovedFrom = null
                        lastMovedTo = null

                        if (fromIdx != null && toIdx != null && fromIdx != toIdx) {
                            viewModel.reorderQueue(fromIdx, toIdx)
                        }
                    }
                }

                androidx.compose.foundation.layout.BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxSize()
                        .then(effectiveDragModifier)
                ) {
                    val totalHeight = maxHeight
                    val navBarBottomDp = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
                    val topHalfHeight = (totalHeight * 0.44f).coerceAtLeast(330.dp)
                    val queueHalfHeight = totalHeight - (topHalfHeight - 6.dp)
                    val collapsedOffsetY = totalHeight - navBarBottomDp - 54.dp
                    val expandedOffsetY = topHalfHeight - 6.dp
                    val currentOffsetY = androidx.compose.ui.unit.lerp(collapsedOffsetY, expandedOffsetY, queueExpansionProgress)
                    val currentPhonePlayerHeight = androidx.compose.ui.unit.lerp(totalHeight, topHalfHeight, queueExpansionProgress)

                    // --- キュー展開時の上部全幅背景アルバムアート（グラデーションフェード） ---
                    if (queueExpansionProgress > 0.01f && !isImmersionLike) {
                        val activeSong = uiState.currentSong
                        val currentSongArtwork by viewModel.currentSongArtwork.collectAsStateWithLifecycle()
                        val rawUri = activeSong?.getAlbumArtUri()
                        val artworkModel: Any = remember(activeSong?.id, currentSongArtwork, rawUri) {
                            if (currentSongArtwork != null) {
                                currentSongArtwork!!
                            } else if (rawUri != null && rawUri.toString().isNotEmpty()) {
                                val uriStr = rawUri.toString()
                                if (uriStr.startsWith("android.resource://")) {
                                    rawUri
                                } else {
                                    SongItem.enhanceThumbnailUrl(uriStr).toUri()
                                }
                            } else if (activeSong != null && !activeSong.videoId.isNullOrEmpty()) {
                                "https://img.youtube.com/vi/${activeSong.videoId}/hqdefault.jpg".toUri()
                            } else {
                                R.drawable.artwork_autumn_wind
                            }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(topHalfHeight + 40.dp)
                                .graphicsLayer {
                                    alpha = queueExpansionProgress
                                    compositingStrategy = androidx.compose.ui.graphics.CompositingStrategy.Offscreen
                                }
                                .drawWithContent {
                                    drawContent()
                                    drawRect(
                                        brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                                            colorStops = arrayOf(
                                                0.0f to androidx.compose.ui.graphics.Color.Black,
                                                0.45f to androidx.compose.ui.graphics.Color.Black,
                                                0.88f to androidx.compose.ui.graphics.Color.Transparent,
                                                1.0f to androidx.compose.ui.graphics.Color.Transparent
                                            )
                                        ),
                                        blendMode = androidx.compose.ui.graphics.BlendMode.DstIn
                                    )
                                }
                        ) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(artworkModel)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    // --- 1. フルプレイヤー画面（通常時は元の完璧な配置、キュー展開時は上半分にスムーズに移行） ---
                    val effectiveArtHeight = androidx.compose.ui.unit.lerp(
                        if (isImmersionLike) albumArtSize * 0.98f else albumArtHeight,
                        (screenHeight * 0.16f).coerceAtLeast(110.dp),
                        queueExpansionProgress
                    )
                    val effectiveArtWidth = androidx.compose.ui.unit.lerp(albumArtSize, screenWidth - 32.dp, queueExpansionProgress)
                    val defaultContentWidth = if (screenWidth >= 600.dp) albumArtSize else (screenWidth - 44.dp).coerceAtMost(480.dp)
                    val effectiveContentWidth = androidx.compose.ui.unit.lerp(defaultContentWidth, screenWidth - 32.dp, queueExpansionProgress)
                    val baseTopSpacer = (availableBodyHeight * 0.065f).coerceIn(24.dp, 54.dp)
                    val baseArtToTitle = (availableBodyHeight * 0.042f).coerceIn(20.dp, 36.dp)
                    val baseTitleToChips = (availableBodyHeight * (if (showVolumeSlider) 0.028f else 0.038f)).coerceIn(14.dp, 28.dp)
                    val baseChipsToSeek = (availableBodyHeight * (if (showVolumeSlider) 0.034f else 0.046f)).coerceIn(16.dp, 32.dp)
                    val baseSeekToButtons = (availableBodyHeight * (if (showVolumeSlider) 0.036f else 0.050f)).coerceIn(18.dp, 36.dp)
                    val baseButtonGroupHeight = (availableBodyHeight * 0.098f).coerceIn(66.dp, 76.dp)

                    val phoneTopSpacerHeight = (topOffset - (statusBarTopDp + dimens.smallPadding + 48.dp)).coerceIn(0.dp, 16.dp)
                    val effectiveTopSpacer = androidx.compose.ui.unit.lerp(baseTopSpacer + phoneTopSpacerHeight, 8.dp, queueExpansionProgress)
                    val effectiveArtToTitleSpacer = androidx.compose.ui.unit.lerp(baseArtToTitle, 4.dp, queueExpansionProgress)
                    val effectiveTitleToChipsSpacer = androidx.compose.ui.unit.lerp(baseTitleToChips, 0.dp, queueExpansionProgress)
                    val effectiveChipsToSeekSpacer = androidx.compose.ui.unit.lerp(baseChipsToSeek, 4.dp, queueExpansionProgress)
                    val effectiveSeekToButtonsSpacer = androidx.compose.ui.unit.lerp(baseSeekToButtons, 4.dp, queueExpansionProgress)
                    val buttonGroupHeight = androidx.compose.ui.unit.lerp(baseButtonGroupHeight, 50.dp, queueExpansionProgress)
                    val effectiveBottomPadding = androidx.compose.ui.unit.lerp(navBarBottomDp, 0.dp, queueExpansionProgress)

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(currentPhonePlayerHeight)
                            .statusBarsPadding()
                            .padding(bottom = effectiveBottomPadding)
                            .padding(vertical = dimens.smallPadding)
                            .zIndex(5f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (effectiveTopSpacer > 0.dp) {
                            Spacer(modifier = Modifier.height(effectiveTopSpacer))
                        }

                        // Album Art Container Placeholder (ArtworkPager is rendered at this absolute position)
                        Box(
                            modifier = Modifier
                                .size(
                                    width = effectiveArtWidth,
                                    height = effectiveArtHeight
                                )
                                .onGloballyPositioned { coordinates ->
                                    if (sheetRootCoordinates != null && sheetRootCoordinates.isAttached && coordinates.isAttached) {
                                        val relativeOffset = sheetRootCoordinates.localPositionOf(coordinates, Offset.Zero)
                                        onTabletPlaceholderPositioned(relativeOffset)
                                    }
                                }
                        )

                        Spacer(modifier = Modifier.height(effectiveArtToTitleSpacer))

                        // Metadata row: title/artist
                        Row(
                            modifier = Modifier.width(effectiveContentWidth),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val configuredPlayerFont = com.codetrio.overdrive.ui.theme.rememberCustomFontFamily(com.codetrio.overdrive.data.font.FontTarget.PLAYER_TITLE)
                            val playerTitleFont = if (isImmersionLike && configuredPlayerFont == com.codetrio.overdrive.ui.theme.GoogleSansFlex) {
                                com.codetrio.overdrive.ui.theme.GoogleSansFlexImmersion
                            } else {
                                configuredPlayerFont
                            }

                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                val effectiveTitleColor = androidx.compose.ui.graphics.lerp(contentColor, Color.White, queueExpansionProgress)
                                val effectiveArtistColor = androidx.compose.ui.graphics.lerp(contentSecondary, Color.White.copy(alpha = 0.85f), queueExpansionProgress)
                                val textShadow = if (queueExpansionProgress > 0.05f) {
                                    androidx.compose.ui.graphics.Shadow(
                                        color = Color.Black.copy(alpha = 0.85f * queueExpansionProgress),
                                        blurRadius = 16f
                                    )
                                } else null

                                Text(
                                    text = uiState.currentSong?.title ?: "Unknown Title",
                                    style = MaterialTheme.typography.headlineMediumEmphasized.copy(
                                        fontFamily = playerTitleFont,
                                        fontSize = if (isImmersionLike) 32.sp else MaterialTheme.typography.headlineMediumEmphasized.fontSize,
                                        lineHeight = if (isImmersionLike) 40.sp else MaterialTheme.typography.headlineMediumEmphasized.lineHeight,
                                        shadow = textShadow
                                    ),
                                    fontWeight = FontWeight.Bold,
                                    color = effectiveTitleColor,
                                    maxLines = 1,
                                    modifier = Modifier.basicMarqueeWithFadedEdges()
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = uiState.currentSong?.artist ?: "Unknown Artist",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontFamily = playerTitleFont,
                                        fontSize = if (isImmersionLike) 20.sp else MaterialTheme.typography.bodyMedium.fontSize,
                                        fontWeight = if (isImmersionLike) androidx.compose.ui.text.font.FontWeight.Medium else MaterialTheme.typography.bodyMedium.fontWeight,
                                        shadow = textShadow
                                    ),
                                    color = effectiveArtistColor,
                                    maxLines = 1,
                                    modifier = Modifier
                                        .basicMarqueeWithFadedEdges()
                                        .clickable {
                                            val song = uiState.currentSong
                                             if (song != null) {
                                                onArtistClick(song.artistId, song.artist)
                                            }
                                        }
                                )
                            }
                        }

                        if (effectiveTitleToChipsSpacer > 0.dp) {
                            Spacer(modifier = Modifier.height(effectiveTitleToChipsSpacer))
                        }

                        // Premium horizontal control chips row
                        if (queueExpansionProgress < 0.95f) {
                            val chipsScrollState = rememberScrollState()
                            Row(
                                modifier = Modifier
                                    .width(defaultContentWidth)
                                    .height(androidx.compose.ui.unit.lerp(40.dp, 0.dp, queueExpansionProgress))
                                    .graphicsLayer {
                                        alpha = (1f - queueExpansionProgress * 2f).coerceIn(0f, 1f)
                                    }
                                    .horizontalFadingEdges(chipsScrollState, fadeLength = 24.dp)
                                    .horizontalScroll(chipsScrollState),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                SplitLikeDislikeChip(
                                    isLiked = uiState.isCurrentSongFavorite,
                                    isDisliked = uiState.isCurrentSongDisliked,
                                    likesCount = uiState.likesCount,
                                    onLikeClick = onFavoriteClick,
                                    onDislikeClick = onDislikeClick,
                                    contentColor = contentColor,
                                    accentColor = dynamicAccentColor,
                                    isDark = isDark
                                )

                                val musicVideoUrl by viewModel.musicVideoUrl.collectAsStateWithLifecycle()
                                val hasMusicVideo by viewModel.hasMusicVideo.collectAsStateWithLifecycle()
                                
                                if (!musicVideoUrl.isNullOrBlank() && hasMusicVideo) {
                                    PillChip(
                                        icon = painterResource(id = R.drawable.ic_music_video),
                                        label = "MV",
                                        isSelected = isMvMode,
                                        onClick = {
                                            viewModel.toggleMvMode()
                                        },
                                        contentColor = contentColor,
                                        accentColor = dynamicAccentColor,
                                        isDark = isDark
                                    )
                                }

                                PillChip(
                                    icon = painterResource(id = R.drawable.ic_lyrics),
                                    label = stringResource(R.string.text_lyrics),
                                    isSelected = isLyricsModeEnabled,
                                    onClick = {
                                        if (!hasLyrics && !isLyricsLoading) {
                                            onFetchLyrics()
                                        }
                                        onLyricsModeChanged(!isLyricsModeEnabled)
                                    },
                                    contentColor = contentColor,
                                    accentColor = dynamicAccentColor,
                                    isDark = isDark
                                )

                                if (showMusicHapticsOption) {
                                    PillChip(
                                        icon = painterResource(id = R.drawable.ic_haptic),
                                        label = stringResource(R.string.text_music_haptics),
                                        isSelected = uiState.isHapticsEnabled,
                                        onClick = {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            onHapticChipClick()
                                        },
                                        contentColor = contentColor,
                                        accentColor = dynamicAccentColor,
                                        isDark = isDark
                                    )
                                }

                                PillChip(
                                    icon = painterResource(id = R.drawable.ic_equalizer),
                                    label = androidx.compose.ui.res.stringResource(com.codetrio.overdrive.R.string.text_audio_effects),
                                    onClick = { viewModel.setEffectsExpanded(true) },
                                    contentColor = contentColor,
                                    accentColor = dynamicAccentColor,
                                    isDark = isDark
                                )

                                if (showPlayerThemeChipOption) {
                                    PillChip(
                                        icon = Icons.Rounded.Palette,
                                        label = stringResource(R.string.setting_player_theme),
                                        onClick = { showPlayerThemeSheet = true },
                                        contentColor = contentColor,
                                        accentColor = dynamicAccentColor,
                                        isDark = isDark
                                    )
                                }

                                if (showPlayerStatsOption) {
                                    PillChip(
                                        icon = painterResource(id = R.drawable.ic_stats),
                                        label = stringResource(R.string.text_stats),
                                        onClick = { showStatsForNerdsDialog = true },
                                        contentColor = contentColor,
                                        accentColor = dynamicAccentColor,
                                        isDark = isDark
                                    )
                                }

                                PillChip(
                                    icon = Icons.AutoMirrored.Rounded.PlaylistAdd,
                                    label = stringResource(R.string.text_save),
                                    onClick = onSaveClick,
                                    contentColor = contentColor,
                                    accentColor = dynamicAccentColor,
                                    isDark = isDark
                                )

                                PillChip(
                                    icon = painterResource(id = R.drawable.ic_share),
                                    label = stringResource(R.string.text_share),
                                    onClick = {
                                        val shareIntent = Intent().apply {
                                            action = Intent.ACTION_SEND
                                            putExtra(Intent.EXTRA_TEXT, "Listening on OverDrive Check out : https://music.youtube.com/watch?v=${uiState.currentSong?.videoId}")
                                            type = "text/plain"
                                        }
                                        context.startActivity(Intent.createChooser(shareIntent, "Share Track"))
                                    },
                                    contentColor = contentColor,
                                    accentColor = dynamicAccentColor,
                                    isDark = isDark
                                )

                                val realDownloaded = uiState.isCurrentSongDownloaded
                                val realDownloadProgress = uiState.currentSongDownloadProgress
                                val isDownloading = realDownloadProgress != null

                                val downloadLabel = when {
                                    realDownloaded -> stringResource(R.string.text_downloaded)
                                    isDownloading -> stringResource(R.string.text_downloading, realDownloadProgress)
                                    else -> stringResource(R.string.text_download)
                                }
                                val downloadIcon: Any = when {
                                    realDownloaded -> painterResource(id = R.drawable.ic_downloaded)
                                    else -> painterResource(id = R.drawable.ic_download)
                                }
                                PillChip(
                                    icon = downloadIcon,
                                    label = downloadLabel,
                                    isSelected = realDownloaded || isDownloading,
                                    progress = if (isDownloading) realDownloadProgress / 100f else null,
                                    onClick = {
                                        val currentSong = uiState.currentSong
                                        if (currentSong != null && !realDownloaded && !isDownloading) {
                                            com.codetrio.overdrive.util.SongDownloader.downloadSong(context, currentSong)
                                        }
                                    },
                                    contentColor = contentColor,
                                    accentColor = dynamicAccentColor,
                                    isDark = isDark
                                )

                                Spacer(modifier = Modifier.width(12.dp))
                            }
                        }

                        if (effectiveChipsToSeekSpacer > 0.dp) {
                            Spacer(modifier = Modifier.height(effectiveChipsToSeekSpacer))
                        }

                        // Wavy Seek Bar (そのまま表示)
                        Box(modifier = Modifier.width(effectiveContentWidth)) {
                            WavySliderWithLabels(
                                currentPositionProvider = currentPositionProvider,
                                duration = uiState.duration,
                                isPlaying = uiState.isPlaying,
                                onSeekTo = onSeekTo,
                                dynamicAccentColor = dynamicAccentColor,
                                contentColor = contentColor,
                                contentSecondary = contentSecondary,
                                isDark = isDark,
                                playbackFormat = uiState.playbackFormat
                            )
                        }

                        if (effectiveSeekToButtonsSpacer > 0.dp) {
                            Spacer(modifier = Modifier.height(effectiveSeekToButtonsSpacer))
                        }

                        // Playback Controls ButtonGroup (そのまま上半分に移動)
                        androidx.compose.material3.ButtonGroup(
                            modifier = Modifier.width(effectiveContentWidth),
                            expandedRatio = 0.3f,
                            overflowIndicator = {}
                        ) {
                            val scope = this
                            val sideButtonContainerColor = if (isImmersionLike) {
                                Color.White.copy(alpha = 0.15f)
                            } else {
                                dynamicAccentColor.copy(alpha = 0.15f)
                            }
                            val sideButtonContentColor = if (isImmersionLike) {
                                Color.White
                            } else {
                                contentColor
                            }
                            customItem(
                                buttonGroupContent = {
                                    val interactionSource = remember { MutableInteractionSource() }
                                    val isPressed by interactionSource.collectIsPressedAsState()
                                    val cornerRadius by animateDpAsState(
                                        targetValue = if (isPressed) 12.dp else 28.dp,
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioNoBouncy,
                                            stiffness = Spring.StiffnessMedium
                                        ),
                                        label = "PrevCorner"
                                    )
                                    androidx.compose.material3.Button(
                                        onClick = onPreviousClick,
                                        modifier = with(scope) {
                                            Modifier
                                                .animateWidth(interactionSource)
                                                .weight(1f)
                                                .height(buttonGroupHeight)
                                        },
                                        interactionSource = interactionSource,
                                        shape = RoundedCornerShape(cornerRadius),
                                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                            containerColor = sideButtonContainerColor,
                                            contentColor = sideButtonContentColor
                                        ),
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                painter = painterResource(id = R.drawable.ic_skip_previous),
                                                contentDescription = "Previous Song",
                                                modifier = Modifier.size(32.dp)
                                            )
                                        }
                                    }
                                },
                                menuContent = {}
                            )
                            customItem(
                                buttonGroupContent = {
                                    val interactionSource = remember { MutableInteractionSource() }
                                    val isPressed by interactionSource.collectIsPressedAsState()
                                    val cornerRadius by animateDpAsState(
                                        targetValue = if (isPressed) 12.dp else 28.dp,
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioNoBouncy,
                                            stiffness = Spring.StiffnessMedium
                                        ),
                                        label = "PlayPauseCorner"
                                    )
                                    androidx.compose.material3.Button(
                                        onClick = onPlayPauseClick,
                                        modifier = with(scope) {
                                            Modifier
                                                .animateWidth(interactionSource)
                                                .weight(1.3f)
                                                .height(buttonGroupHeight)
                                        },
                                        interactionSource = interactionSource,
                                        shape = RoundedCornerShape(cornerRadius),
                                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                            containerColor = if (isImmersionLike) Color.White else dynamicAccentColor,
                                            contentColor = if (isImmersionLike) Color.Black else if (isDark) Color.Black else Color.White
                                        ),
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                painter = if (uiState.isPlaying) painterResource(id = R.drawable.ic_pause) else painterResource(id = R.drawable.ic_play),
                                                contentDescription = if (uiState.isPlaying) "Pause" else "Play",
                                                modifier = Modifier.size(38.dp)
                                            )
                                        }
                                    }
                                },
                                menuContent = {}
                            )
                            customItem(
                                buttonGroupContent = {
                                    val interactionSource = remember { MutableInteractionSource() }
                                    val isPressed by interactionSource.collectIsPressedAsState()
                                    val cornerRadius by animateDpAsState(
                                        targetValue = if (isPressed) 12.dp else 28.dp,
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioNoBouncy,
                                            stiffness = Spring.StiffnessMedium
                                        ),
                                        label = "NextCorner"
                                    )
                                    androidx.compose.material3.Button(
                                        onClick = onNextClick,
                                        modifier = with(scope) {
                                            Modifier
                                                .animateWidth(interactionSource)
                                                .weight(1f)
                                                .height(buttonGroupHeight)
                                        },
                                        interactionSource = interactionSource,
                                        shape = RoundedCornerShape(cornerRadius),
                                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                            containerColor = sideButtonContainerColor,
                                            contentColor = sideButtonContentColor
                                        ),
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                painter = painterResource(id = R.drawable.ic_skip_next),
                                                contentDescription = "Next Song",
                                                modifier = Modifier.size(32.dp)
                                            )
                                        }
                                    }
                                },
                                menuContent = {}
                            )
                        }

                        if (queueExpansionProgress < 0.8f) {
                            if (showVolumeSlider) {
                                val volTopSpacer = (availableBodyHeight * 0.036f).coerceIn(16.dp, 32.dp)
                                val volBottomSpacer = (availableBodyHeight * 0.016f).coerceIn(6.dp, 16.dp)
                                Spacer(modifier = Modifier.height(androidx.compose.ui.unit.lerp(volTopSpacer, 0.dp, queueExpansionProgress)))
                                com.codetrio.overdrive.ui.player.VolumeSlider(
                                    modifier = Modifier
                                        .width(defaultContentWidth)
                                        .graphicsLayer {
                                            alpha = (1f - queueExpansionProgress * 2f).coerceIn(0f, 1f)
                                        },
                                    contentColor = contentColor,
                                    dynamicAccentColor = dynamicAccentColor
                                )
                                Spacer(modifier = Modifier.height(androidx.compose.ui.unit.lerp(volBottomSpacer, 0.dp, queueExpansionProgress)))
                            } else {
                                val emptyBottomSpacer = (availableBodyHeight * 0.024f).coerceIn(10.dp, 22.dp)
                                Spacer(modifier = Modifier.height(androidx.compose.ui.unit.lerp(emptyBottomSpacer, 0.dp, queueExpansionProgress)))
                            }
                        }
                    }

                    // キュー展開時にBackキーでキューを閉じる
                    BackHandler(enabled = isQueueExpanded) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.setQueueExpanded(false)
                    }

                    // --- 2. 下半分に展開されるキューセクション ---
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(queueHalfHeight)
                            .offset(y = currentOffsetY)
                            .zIndex(10f)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .navigationBarsPadding()
                        ) {
                            // パネル上部ドラッグハンドル（﹀ / ︿）
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp)
                                    .draggable(
                                        orientation = Orientation.Vertical,
                                        state = rememberDraggableState { delta ->
                                            if (delta > 6f && isQueueExpanded) {
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                viewModel.setQueueExpanded(false)
                                            } else if (delta < -6f && !isQueueExpanded) {
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                viewModel.setQueueExpanded(true)
                                            }
                                        }
                                    )
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        viewModel.setQueueExpanded(!isQueueExpanded)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_keyboard_arrow_down),
                                    contentDescription = if (isQueueExpanded) "Close Queue" else "Open Queue",
                                    tint = contentColor.copy(alpha = 0.6f),
                                    modifier = Modifier
                                        .size(30.dp)
                                        .graphicsLayer {
                                            rotationZ = if (isQueueExpanded) 0f else 180f
                                        }
                                )
                            }

                            // ツールバー（曲数、自動再生、シャッフル、リピート、タイマー、検索）
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp)
                                    .padding(bottom = 6.dp)
                                    .graphicsLayer {
                                        alpha = queueExpansionProgress
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${songList.size} 曲",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp
                                        ),
                                        color = contentColor
                                    )

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        // 自動再生
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            modifier = Modifier.padding(end = 4.dp)
                                        ) {
                                            Text(
                                                text = "自動再生",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = contentSecondary,
                                                fontSize = 12.sp
                                            )
                                            androidx.compose.material3.Switch(
                                                checked = isAutoplayEnabled,
                                                onCheckedChange = onAutoplayToggle,
                                                thumbContent = if (isAutoplayEnabled) {
                                                    {
                                                        Icon(
                                                            imageVector = Icons.Rounded.PlayArrow,
                                                            contentDescription = null,
                                                            modifier = Modifier.size(androidx.compose.material3.SwitchDefaults.IconSize)
                                                        )
                                                    }
                                                } else null,
                                                modifier = Modifier.scale(0.72f)
                                            )
                                        }

                                        // シャッフル
                                        IconButton(
                                            onClick = {
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                viewModel.toggleShuffle()
                                            },
                                            modifier = Modifier.size(36.dp),
                                            colors = androidx.compose.material3.IconButtonDefaults.iconButtonColors(
                                                containerColor = if (isShuffleEnabled) dynamicAccentColor.copy(alpha = 0.2f) else Color.Transparent
                                            )
                                        ) {
                                            Icon(
                                                painter = painterResource(id = R.drawable.ic_shuffle),
                                                contentDescription = "Shuffle",
                                                tint = if (isShuffleEnabled) dynamicAccentColor else contentSecondary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }

                                        // リピート
                                        val loopIcon = if (repeatMode == PlayerSharedViewModel.REPEAT_ONE) R.drawable.ic_repeat_one else R.drawable.ic_repeat
                                        val isLoopActive = repeatMode != PlayerSharedViewModel.REPEAT_OFF
                                        IconButton(
                                            onClick = {
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                viewModel.toggleLoopMode()
                                            },
                                            modifier = Modifier.size(36.dp),
                                            colors = androidx.compose.material3.IconButtonDefaults.iconButtonColors(
                                                containerColor = if (isLoopActive) dynamicAccentColor.copy(alpha = 0.2f) else Color.Transparent
                                            )
                                        ) {
                                            Icon(
                                                painter = painterResource(id = loopIcon),
                                                contentDescription = "Repeat",
                                                tint = if (isLoopActive) dynamicAccentColor else contentSecondary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }

                                        // スリープタイマー
                                        val isTimerActive = sleepTimerMode != PlayerSharedViewModel.SleepTimerMode.OFF
                                        IconButton(
                                            onClick = {
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                showSleepTimerDialog = true
                                            },
                                            modifier = Modifier.size(36.dp),
                                            colors = androidx.compose.material3.IconButtonDefaults.iconButtonColors(
                                                containerColor = if (isTimerActive) dynamicAccentColor.copy(alpha = 0.2f) else Color.Transparent
                                            )
                                        ) {
                                            Icon(
                                                painter = painterResource(id = R.drawable.ic_timer),
                                                contentDescription = "Sleep Timer",
                                                tint = if (isTimerActive) dynamicAccentColor else contentSecondary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }

                                        // 検索
                                        IconButton(
                                            onClick = {
                                                isSearchExpanded = !isSearchExpanded
                                                if (!isSearchExpanded) {
                                                    searchQuery = ""
                                                }
                                            },
                                            modifier = Modifier.size(36.dp),
                                            colors = androidx.compose.material3.IconButtonDefaults.iconButtonColors(
                                                containerColor = if (isSearchExpanded) dynamicAccentColor.copy(alpha = 0.2f) else Color.Transparent
                                            )
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.Search,
                                                contentDescription = "Search Queue",
                                                tint = if (isSearchExpanded) dynamicAccentColor else contentSecondary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }

                                // 展開型検索バー
                                AnimatedVisibility(
                                    visible = isSearchExpanded,
                                    enter = expandVertically() + fadeIn(),
                                    exit = shrinkVertically() + fadeOut()
                                ) {
                                    OutlinedTextField(
                                        value = searchQuery,
                                        onValueChange = { searchQuery = it },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 8.dp),
                                        placeholder = {
                                            Text(
                                                text = "キュー内の曲を検索...",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = contentSecondary
                                            )
                                        },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Rounded.Search,
                                                contentDescription = null,
                                                tint = dynamicAccentColor
                                            )
                                        },
                                        trailingIcon = {
                                            if (searchQuery.isNotEmpty()) {
                                                IconButton(onClick = { searchQuery = "" }) {
                                                    Icon(
                                                        imageVector = Icons.Rounded.Clear,
                                                        contentDescription = "Clear search",
                                                        tint = contentSecondary
                                                    )
                                                }
                                            }
                                        },
                                        singleLine = true,
                                        shape = CircleShape,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedContainerColor = (if (isDark) Color.White else Color.Black).copy(alpha = 0.06f),
                                            unfocusedContainerColor = (if (isDark) Color.White else Color.Black).copy(alpha = 0.03f),
                                            focusedBorderColor = dynamicAccentColor,
                                            unfocusedBorderColor = (if (isDark) Color.White else Color.Black).copy(alpha = 0.08f),
                                            focusedTextColor = contentColor,
                                            unfocusedTextColor = contentColor
                                        )
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(1.dp)
                                        .background((if (isDark) Color.White else Color.Black).copy(alpha = 0.06f))
                                )
                            }

                            // キューリスト
                            val currentSongId = songList.getOrNull(currentSongIndex)?.id
                            LazyColumn(
                                state = lazyListState,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer {
                                        alpha = queueExpansionProgress
                                    }
                                    .padding(horizontal = 8.dp),
                                contentPadding = PaddingValues(bottom = 32.dp)
                            ) {
                                itemsIndexed(
                                    items = filteredSongList,
                                    key = { _, song -> song.id },
                                    contentType = { _, _ -> "queue-song" }
                                ) { index, song ->
                                    val isPlaying = (song.id == currentSongId)
                                    val originalIndex = songList.indexOfFirst { it.id == song.id }

                                    Box(modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)) {
                                        if (searchQuery.isNotEmpty()) {
                                            QueueTrackListItem(
                                                song = song,
                                                isPlaying = isPlaying,
                                                isDark = isDark,
                                                contentColor = contentColor,
                                                contentSecondary = contentSecondary,
                                                dynamicAccentColor = dynamicAccentColor,
                                                onClick = {
                                                    if (originalIndex != -1) {
                                                        viewModel.playSongAtIndex(originalIndex)
                                                    }
                                                },
                                                onRemoveClick = {
                                                    if (originalIndex != -1) {
                                                        viewModel.removeSongAtIndex(originalIndex)
                                                    }
                                                },
                                                dragHandle = {}
                                            )
                                        } else {
                                            ReorderableItem(
                                                state = reorderableState,
                                                key = song.id
                                            ) { isDragging ->
                                                val scale by animateFloatAsState(
                                                    targetValue = if (isDragging) 1.02f else 1f,
                                                    animationSpec = spring(
                                                        dampingRatio = Spring.DampingRatioNoBouncy,
                                                        stiffness = Spring.StiffnessMediumLow
                                                    ),
                                                    label = "scaleAnimation"
                                                )

                                                QueueTrackListItem(
                                                    song = song,
                                                    isPlaying = isPlaying,
                                                    isDark = isDark,
                                                    contentColor = contentColor,
                                                    contentSecondary = contentSecondary,
                                                    dynamicAccentColor = dynamicAccentColor,
                                                    onClick = {
                                                        if (originalIndex != -1) {
                                                            viewModel.playSongAtIndex(originalIndex)
                                                        }
                                                    },
                                                    onRemoveClick = {
                                                        if (originalIndex != -1) {
                                                            viewModel.removeSongAtIndex(originalIndex)
                                                        }
                                                    },
                                                    dragHandle = {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(44.dp)
                                                                .draggableHandle(
                                                                    onDragStarted = {
                                                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                                    },
                                                                    onDragStopped = {
                                                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                                    }
                                                                ),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Rounded.Menu,
                                                                contentDescription = "Drag to reorder",
                                                                tint = contentSecondary.copy(alpha = 0.5f),
                                                                modifier = Modifier.size(22.dp)
                                                            )
                                                        }
                                                    },
                                                    modifier = Modifier
                                                        .graphicsLayer {
                                                            scaleX = scale
                                                            scaleY = scale
                                                            compositingStrategy = androidx.compose.ui.graphics.CompositingStrategy.Offscreen
                                                        }
                                                )
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
        fun FullPlayerCompactLandscapeLayout() {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Column: Artwork Container Placeholder
                Box(
                    modifier = Modifier
                        .size(albumArtSize)
                        .onGloballyPositioned { placeholderCoords ->
                            if (placeholderCoords.isAttached && sheetRootCoordinates?.isAttached == true) {
                                val pos = sheetRootCoordinates.localPositionOf(placeholderCoords, Offset.Zero)
                                onTabletPlaceholderPositioned(pos)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {}

                Spacer(modifier = Modifier.width(28.dp))

                // Right Column: Title, Artist, WavySlider, and Controls Row
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.Center
                ) {
                    // Title & Artist
                    Text(
                        text = uiState.currentSong?.title ?: "Unknown Title",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = contentColor,
                        maxLines = 1,
                        modifier = Modifier.basicMarqueeWithFadedEdges()
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = uiState.currentSong?.artist ?: "Unknown Artist",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 14.sp,
                            color = contentSecondary
                        ),
                        maxLines = 1,
                        modifier = Modifier
                            .basicMarqueeWithFadedEdges()
                            .clickable {
                                uiState.currentSong?.let { onArtistClick(it.artistId, it.artist) }
                            }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Wavy Slider
                    WavySliderWithLabels(
                        currentPositionProvider = currentPositionProvider,
                        duration = uiState.duration,
                        isPlaying = uiState.isPlaying,
                        onSeekTo = onSeekTo,
                        dynamicAccentColor = dynamicAccentColor,
                        contentColor = contentColor,
                        contentSecondary = contentSecondary,
                        isDark = isDark,
                        playbackFormat = uiState.playbackFormat
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Playback Controls Row (Shuffle, Previous, Play/Pause, Next, Repeat, Effects)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { viewModel.toggleShuffle() }, modifier = Modifier.size(36.dp)) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_shuffle),
                                contentDescription = "Shuffle",
                                tint = if (isShuffleEnabled) dynamicAccentColor else contentColor.copy(alpha = 0.6f),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        IconButton(
                            onClick = onPreviousClick,
                            modifier = Modifier
                                .size(44.dp)
                                .background(sideButtonContainerColor, androidx.compose.foundation.shape.CircleShape)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_skip_previous),
                                contentDescription = "Previous",
                                tint = sideButtonContentColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        androidx.compose.material3.FilledIconButton(
                            onClick = onPlayPauseClick,
                            modifier = Modifier.size(54.dp),
                            colors = androidx.compose.material3.IconButtonDefaults.filledIconButtonColors(
                                containerColor = playButtonContainerColor,
                                contentColor = playButtonContentColor
                            )
                        ) {
                            Icon(
                                painter = painterResource(id = if (uiState.isPlaying) R.drawable.ic_pause else R.drawable.ic_play),
                                contentDescription = if (uiState.isPlaying) "Pause" else "Play",
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        IconButton(
                            onClick = onNextClick,
                            modifier = Modifier
                                .size(44.dp)
                                .background(sideButtonContainerColor, androidx.compose.foundation.shape.CircleShape)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_skip_next),
                                contentDescription = "Next",
                                tint = sideButtonContentColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        val loopIcon = if (repeatMode == PlayerSharedViewModel.REPEAT_ONE) R.drawable.ic_repeat_one else R.drawable.ic_repeat
                        IconButton(onClick = { viewModel.toggleLoopMode() }, modifier = Modifier.size(36.dp)) {
                            Icon(
                                painter = painterResource(id = loopIcon),
                                contentDescription = "Repeat",
                                tint = if (repeatMode != PlayerSharedViewModel.REPEAT_OFF) dynamicAccentColor else contentColor.copy(alpha = 0.6f),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        IconButton(onClick = { viewModel.setEffectsExpanded(true) }, modifier = Modifier.size(36.dp)) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_equalizer),
                                contentDescription = "Effects",
                                tint = contentColor.copy(alpha = 0.7f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    if (showVolumeSlider) {
                        Spacer(modifier = Modifier.height(8.dp))
                        com.codetrio.overdrive.ui.player.VolumeSlider(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                            contentColor = contentColor,
                            dynamicAccentColor = dynamicAccentColor
                        )
                    }
                }
            }
        }

        @Composable
        fun FullPlayerTabletLayout() {
            val musicVideoUrl by viewModel.musicVideoUrl.collectAsStateWithLifecycle()
            val hasMusicVideo by viewModel.hasMusicVideo.collectAsStateWithLifecycle()

            var leftPaneHeightPx by remember { mutableIntStateOf(0) }
            val density = LocalDensity.current
            val leftPaneHeightDp = with(density) { leftPaneHeightPx.toDp() }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = dimens.screenMargin),
                verticalAlignment = Alignment.Top
            ) {
                val configuration = androidx.compose.ui.platform.LocalConfiguration.current
                val availableWidthDp = configuration.screenWidthDp.dp - (dimens.screenMargin * 2)
                val rightPaneWidthDp = (availableWidthDp * 0.44f).coerceIn(320.dp, 540.dp)

                // Left pane: Artwork / Video and Controls
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 24.dp)
                        .onSizeChanged { leftPaneHeightPx = it.height },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Fixed Artwork / Video container placeholder (retains full layout stability)
                    Box(
                        modifier = Modifier
                            .size(width = albumArtSize, height = albumArtSize)
                            .onGloballyPositioned { placeholderCoords ->
                                if (placeholderCoords.isAttached && sheetRootCoordinates?.isAttached == true) {
                                    val pos = sheetRootCoordinates.localPositionOf(placeholderCoords, Offset.Zero)
                                    onTabletPlaceholderPositioned(pos)
                                }
                            }
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Left-aligned Metadata (Title & Artist, queue button removed on tablet)
                    val configuredTabletPlayerFont = com.codetrio.overdrive.ui.theme.rememberCustomFontFamily(com.codetrio.overdrive.data.font.FontTarget.PLAYER_TITLE)
                    val tabletPlayerTitleFont = if (isImmersionLike && configuredTabletPlayerFont == com.codetrio.overdrive.ui.theme.GoogleSansFlex) {
                        com.codetrio.overdrive.ui.theme.GoogleSansFlexImmersion
                    } else {
                        configuredTabletPlayerFont
                    }

                    Column(
                        modifier = Modifier
                            .width(albumArtSize)
                            .fillMaxWidth()
                    ) {
                        Text(
                            text = uiState.currentSong?.title ?: "Unknown Title",
                            style = MaterialTheme.typography.headlineMediumEmphasized.copy(
                                fontFamily = tabletPlayerTitleFont,
                                fontSize = 26.sp
                            ),
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                            color = contentColor,
                            maxLines = 1,
                            modifier = Modifier.basicMarqueeWithFadedEdges()
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = uiState.currentSong?.artist ?: "Unknown Artist",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = tabletPlayerTitleFont,
                                fontSize = 17.sp,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                            ),
                            color = contentColor.copy(alpha = 0.75f),
                            maxLines = 1,
                            modifier = Modifier
                                .basicMarqueeWithFadedEdges()
                                .clickable {
                                    val song = uiState.currentSong
                                    if (song != null) {
                                        onArtistClick(song.artistId, song.artist)
                                    }
                                }
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Chips Row (Lyrics and Stats chips removed on tablet)
                    Box(modifier = Modifier.width(albumArtSize)) {
                        val tabletChipsScrollState = rememberScrollState()
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalFadingEdges(tabletChipsScrollState, fadeLength = 24.dp)
                                .horizontalScroll(tabletChipsScrollState),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            SplitLikeDislikeChip(
                                isLiked = uiState.isCurrentSongFavorite,
                                isDisliked = uiState.isCurrentSongDisliked,
                                likesCount = uiState.likesCount,
                                onLikeClick = onFavoriteClick,
                                onDislikeClick = onDislikeClick,
                                contentColor = contentColor,
                                accentColor = dynamicAccentColor,
                                isDark = isDark
                            )

                            if (!musicVideoUrl.isNullOrBlank() && hasMusicVideo) {
                                PillChip(
                                    icon = painterResource(id = R.drawable.ic_music_video),
                                    label = "MV",
                                    isSelected = isEffectiveMvMode,
                                    onClick = {
                                        viewModel.toggleMvMode()
                                    },
                                    contentColor = contentColor,
                                    accentColor = dynamicAccentColor,
                                    isDark = isDark
                                )
                            }

                            if (showMusicHapticsOption) {
                                PillChip(
                                    icon = painterResource(id = R.drawable.ic_haptic),
                                    label = stringResource(R.string.text_music_haptics),
                                    isSelected = uiState.isHapticsEnabled,
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        onHapticChipClick()
                                    },
                                    contentColor = contentColor,
                                    accentColor = dynamicAccentColor,
                                    isDark = isDark
                                )
                            }

                            PillChip(
                                icon = painterResource(id = R.drawable.ic_equalizer),
                                label = androidx.compose.ui.res.stringResource(com.codetrio.overdrive.R.string.text_audio_effects),
                                onClick = { viewModel.setEffectsExpanded(true) },
                                contentColor = contentColor,
                                accentColor = dynamicAccentColor,
                                isDark = isDark
                            )

                            // Theme Switcher Chip (Developer Option)
                            if (showPlayerThemeChipOption) {
                                PillChip(
                                    icon = Icons.Rounded.Palette,
                                    label = stringResource(R.string.setting_player_theme),
                                    onClick = { showPlayerThemeSheet = true },
                                    contentColor = contentColor,
                                    accentColor = dynamicAccentColor,
                                    isDark = isDark
                                )
                            }

                            // Google Cast Chip (Tablet)
                            val isCastConnected = castState is CastState.Connected
                            val isCastConnecting = castState is CastState.Connecting
                            val castLabel = when (val state = castState) {
                                is CastState.Connected -> state.deviceName
                                is CastState.Connecting -> stringResource(R.string.text_connecting)
                                else -> stringResource(R.string.text_cast)
                            }
                            PillChip(
                                icon = if (isCastConnected) Icons.Rounded.CastConnected else Icons.Rounded.Cast,
                                label = castLabel,
                                isSelected = isCastConnected,
                                onClick = {
                                    viewModel.showCastSheet()
                                },
                                contentColor = contentColor,
                                accentColor = dynamicAccentColor,
                                isDark = isDark
                            )

                            PillChip(
                                icon = Icons.AutoMirrored.Rounded.PlaylistAdd,
                                label = stringResource(R.string.text_save),
                                onClick = onSaveClick,
                                contentColor = contentColor,
                                accentColor = dynamicAccentColor,
                                isDark = isDark
                            )

                            PillChip(
                                icon = painterResource(id = R.drawable.ic_share),
                                label = stringResource(R.string.text_share),
                                onClick = {
                                    val shareIntent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(Intent.EXTRA_TEXT, "Listening on OverDrive Check out : https://music.youtube.com/watch?v=${uiState.currentSong?.videoId}")
                                        type = "text/plain"
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "Share Track"))
                                },
                                contentColor = contentColor,
                                accentColor = dynamicAccentColor,
                                isDark = isDark
                            )

                            val realDownloaded = uiState.isCurrentSongDownloaded
                            val realDownloadProgress = uiState.currentSongDownloadProgress
                            val isDownloading = realDownloadProgress != null

                            val downloadLabel = when {
                                realDownloaded -> stringResource(R.string.text_downloaded)
                                isDownloading -> stringResource(R.string.text_downloading, realDownloadProgress)
                                else -> stringResource(R.string.text_download)
                            }
                            val downloadIcon: Any = when {
                                realDownloaded -> painterResource(id = R.drawable.ic_downloaded)
                                else -> painterResource(id = R.drawable.ic_download)
                            }
                            PillChip(
                                icon = downloadIcon,
                                label = downloadLabel,
                                isSelected = realDownloaded || isDownloading,
                                progress = if (isDownloading) realDownloadProgress / 100f else null,
                                onClick = {
                                    val currentSong = uiState.currentSong
                                    if (currentSong != null && !realDownloaded && !isDownloading) {
                                        com.codetrio.overdrive.util.SongDownloader.downloadSong(context, currentSong)
                                    }
                                },
                                contentColor = contentColor,
                                accentColor = dynamicAccentColor,
                                isDark = isDark
                            )

                            Spacer(modifier = Modifier.width(12.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Wavy Slider
                    Box(modifier = Modifier.width(albumArtSize)) {
                        WavySliderWithLabels(
                            currentPositionProvider = currentPositionProvider,
                            duration = uiState.duration,
                            isPlaying = uiState.isPlaying,
                            onSeekTo = onSeekTo,
                            dynamicAccentColor = dynamicAccentColor,
                            contentColor = contentColor,
                            contentSecondary = contentSecondary,
                            isDark = isDark,
                            playbackFormat = uiState.playbackFormat
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Playback Controls ButtonGroup
                    androidx.compose.material3.ButtonGroup(
                        modifier = Modifier
                            .width(albumArtSize)
                            .padding(horizontal = 16.dp),
                        expandedRatio = 0.3f,
                        overflowIndicator = {}
                    ) {
                        val scope = this
                        customItem(
                            buttonGroupContent = {
                                val interactionSource = remember { MutableInteractionSource() }
                                val isPressed by interactionSource.collectIsPressedAsState()
                                val cornerRadius by animateDpAsState(
                                    targetValue = if (isPressed) 12.dp else 28.dp,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioNoBouncy,
                                        stiffness = Spring.StiffnessMedium
                                    ),
                                    label = "PrevCorner"
                                )
                                androidx.compose.material3.Button(
                                    onClick = onPreviousClick,
                                    modifier = with(scope) {
                                        Modifier
                                            .animateWidth(interactionSource)
                                            .weight(1f)
                                            .height(72.dp)
                                    },
                                    interactionSource = interactionSource,
                                    shape = RoundedCornerShape(cornerRadius),
                                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                        containerColor = sideButtonContainerColor,
                                        contentColor = sideButtonContentColor
                                    ),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                                ) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.ic_skip_previous),
                                            contentDescription = "Previous Song",
                                            modifier = Modifier.size(36.dp)
                                        )
                                    }
                                }
                            },
                            menuContent = {}
                        )
                        customItem(
                            buttonGroupContent = {
                                val interactionSource = remember { MutableInteractionSource() }
                                val isPressed by interactionSource.collectIsPressedAsState()
                                val cornerRadius by animateDpAsState(
                                    targetValue = if (isPressed) 12.dp else 28.dp,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioNoBouncy,
                                        stiffness = Spring.StiffnessMedium
                                    ),
                                    label = "PlayCorner"
                                )
                                androidx.compose.material3.Button(
                                    onClick = onPlayPauseClick,
                                    modifier = with(scope) {
                                        Modifier
                                            .animateWidth(interactionSource)
                                            .weight(1.2f)
                                            .height(72.dp)
                                    },
                                    interactionSource = interactionSource,
                                    shape = RoundedCornerShape(cornerRadius),
                                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                        containerColor = playButtonContainerColor,
                                        contentColor = playButtonContentColor
                                    ),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                                ) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            painter = painterResource(id = if (uiState.isPlaying) R.drawable.ic_pause else R.drawable.ic_play),
                                            contentDescription = if (uiState.isPlaying) "Pause" else "Play",
                                            modifier = Modifier.size(42.dp)
                                        )
                                    }
                                }
                            },
                            menuContent = {}
                        )
                        customItem(
                            buttonGroupContent = {
                                val interactionSource = remember { MutableInteractionSource() }
                                val isPressed by interactionSource.collectIsPressedAsState()
                                val cornerRadius by animateDpAsState(
                                    targetValue = if (isPressed) 12.dp else 28.dp,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioNoBouncy,
                                        stiffness = Spring.StiffnessMedium
                                    ),
                                    label = "NextCorner"
                                )
                                androidx.compose.material3.Button(
                                    onClick = onNextClick,
                                    modifier = with(scope) {
                                        Modifier
                                            .animateWidth(interactionSource)
                                            .weight(1f)
                                            .height(72.dp)
                                    },
                                    interactionSource = interactionSource,
                                    shape = RoundedCornerShape(cornerRadius),
                                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                        containerColor = sideButtonContainerColor,
                                        contentColor = sideButtonContentColor
                                    ),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                                ) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.ic_skip_next),
                                            contentDescription = "Next Song",
                                            modifier = Modifier.size(36.dp)
                                        )
                                    }
                                }
                            },
                            menuContent = {}
                        )
                    }

                    if (showVolumeSlider) {
                        Spacer(modifier = Modifier.height(14.dp))
                        com.codetrio.overdrive.ui.player.VolumeSlider(
                            modifier = Modifier.width(albumArtSize),
                            contentColor = contentColor,
                            dynamicAccentColor = dynamicAccentColor
                        )
                    }
                }

                // Right pane: Multi-Tab Stage (Lyrics / Up Next Queue / Track Details)
                val rightPaneShape = RoundedCornerShape(28.dp)

                val rightPaneBgModifier = if (isVinyl) {
                    val topBgColor = if (isDark) Color(0xFF1C1E24) else Color(0xFFFFFFFF)
                    val gradientBrush = Brush.verticalGradient(
                        0.00f to topBgColor,
                        0.30f to topBgColor.copy(alpha = 0.90f),
                        0.60f to topBgColor.copy(alpha = 0.58f),
                        0.85f to topBgColor.copy(alpha = 0.32f),
                        1.00f to topBgColor.copy(alpha = 0.20f)
                    )
                    val borderBrush = Brush.verticalGradient(
                        0.00f to (if (isDark) Color(0x28FFFFFF) else Color(0x18000000)),
                        0.50f to (if (isDark) Color(0x18FFFFFF) else Color(0x10000000)),
                        1.00f to (if (isDark) Color(0x0CFFFFFF) else Color(0x08000000))
                    )
                    Modifier
                        .clip(rightPaneShape)
                        .border(BorderStroke(1.dp, borderBrush), rightPaneShape)
                        .background(gradientBrush)
                } else {
                    val rightPaneContainerBg = contentColor.copy(alpha = if (isTextColorDark) 0.06f else 0.10f)
                    Modifier
                        .clip(rightPaneShape)
                        .background(rightPaneContainerBg)
                }

                Column(
                    modifier = Modifier
                        .requiredWidth(rightPaneWidthDp)
                        .then(
                            if (leftPaneHeightDp > 0.dp) Modifier.height(leftPaneHeightDp) else Modifier.fillMaxHeight()
                        )
                        .then(rightPaneBgModifier)
                ) {
                    // Segmented Pill Tabs Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val tabs = listOf(
                            Triple(0, "歌詞", R.drawable.ic_lyrics),
                            Triple(1, "キュー", null),
                            Triple(2, "曲情報", R.drawable.ic_stats)
                        )
                        val selectedTabContentColor = if (isVinyl) {
                            if (isDark) Color(0xFF111215) else Color.White
                        } else {
                            val accentLum = androidx.core.graphics.ColorUtils.calculateLuminance(dynamicAccentColor.toArgb())
                            if (accentLum > 0.45) Color(0xFF1C1B1F) else Color.White
                        }

                        val unselectedTabBg = when {
                            isVinyl -> if (isDark) Color(0xFF272932) else Color(0xFFF1F3F6)
                            else -> contentColor.copy(alpha = if (isTextColorDark) 0.06f else 0.09f)
                        }
                        val unselectedTabContentColor = when {
                            isVinyl -> if (isDark) Color.White.copy(alpha = 0.75f) else Color(0xFF111215).copy(alpha = 0.75f)
                            else -> contentColor.copy(alpha = 0.85f)
                        }

                        tabs.forEach { (tabIdx, title, iconRes) ->
                            val isSelected = tabletRightPaneTab == tabIdx
                            androidx.compose.material3.Surface(
                                onClick = {
                                    tabletRightPaneTab = tabIdx
                                    if (tabIdx == 0 && currentSongId != null && !hasLyrics && !isLyricsLoading) {
                                        onFetchLyrics()
                                    }
                                },
                                shape = RoundedCornerShape(18.dp),
                                color = if (isSelected) dynamicAccentColor else unselectedTabBg,
                                contentColor = if (isSelected) selectedTabContentColor else unselectedTabContentColor,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (iconRes != null) {
                                        Icon(
                                            painter = painterResource(id = iconRes),
                                            contentDescription = title,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Rounded.QueueMusic,
                                            contentDescription = title,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = title,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }

                    // Content of selected Tab
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        when (tabletRightPaneTab) {
                            0 -> {
                                val syncOffsetMs by viewModel.currentLyricsOffsetMs.collectAsStateWithLifecycle()
                                FullScreenLyricsOverlay(
                                    currentSong = uiState.currentSong,
                                    syncedLyrics = syncedLyrics,
                                    plainLyrics = plainLyrics,
                                    translatedPlainLyrics = translatedPlainLyrics,
                                    isLoading = isLyricsLoading,
                                    lyricsError = lyricsError,
                                    currentPositionProvider = currentPositionProvider,
                                    contentReady = true,
                                    playerBackgroundColor = Color.Transparent,
                                    canvasArtwork = canvasArtwork,
                                    contentColor = contentColor,
                                    contentSecondary = contentSecondary,
                                    dynamicAccentColor = dynamicAccentColor,
                                    onRetryLyrics = onRetryLyrics,
                                    onFetchLyrics = onFetchLyrics,
                                    onSeekTo = onSeekTo,
                                    providerResults = providerResults,
                                    selectedProvider = selectedProvider,
                                    onProviderSelected = onProviderSelected,
                                    syncOffsetMs = syncOffsetMs,
                                    onSyncOffsetChange = { viewModel.setLyricsOffset(it) },
                                    isPlaying = uiState.isPlaying,
                                    onPlayPauseClick = onPlayPauseClick,
                                    duration = uiState.duration.toLong(),
                                    onCollapse = {},
                                    isEmbedded = true,
                                    onToggleTranslation = { viewModel.toggleLyricsTranslation() },
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            1 -> {
                                TabletQueuePane(
                                    songList = songList,
                                    currentSongIndex = currentSongIndex,
                                    isShuffleEnabled = isShuffleEnabled,
                                    repeatMode = repeatMode,
                                    sleepTimerMode = sleepTimerMode,
                                    onReorderQueue = { from, to -> viewModel.reorderQueue(from, to) },
                                    onPlaySongAtIndex = { index -> viewModel.playSongAtIndex(index) },
                                    onRemoveSongAtIndex = { index -> viewModel.removeSongAtIndex(index) },
                                    onToggleShuffle = { viewModel.toggleShuffle() },
                                    onToggleLoopMode = { viewModel.toggleLoopMode() },
                                    onShowSleepTimerDialog = { showSleepTimerDialog = true },
                                    dynamicAccentColor = dynamicAccentColor,
                                    contentColor = contentColor,
                                    contentSecondary = contentSecondary,
                                    isDark = isDark,
                                    isAutoplayEnabled = isAutoplayEnabled,
                                    onAutoplayToggle = onAutoplayToggle,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            2 -> {
                                TabletTrackInfoPane(
                                    song = uiState.currentSong,
                                    uiState = uiState,
                                    viewModel = viewModel,
                                    dynamicAccentColor = dynamicAccentColor,
                                    contentColor = contentColor,
                                    contentSecondary = contentSecondary,
                                    isStatsEnabled = showPlayerStatsOption,
                                    onArtistClick = onArtistClick,
                                    onAlbumClick = onAlbumClick,
                                    onSaveClick = onSaveClick,
                                    onOpenStats = { showStatsForNerdsDialog = true },
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                }
            }
        }


                when {
                    isWideLandscape -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .statusBarsPadding()
                                .navigationBarsPadding(),
                            contentAlignment = Alignment.Center
                        ) {
                            FullPlayerTabletLayout()
                        }
                    }
                    isCompactLandscape -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .statusBarsPadding()
                                .navigationBarsPadding(),
                            contentAlignment = Alignment.Center
                        ) {
                            FullPlayerCompactLandscapeLayout()
                        }
                    }
                    else -> {
                        FullPlayerPhoneLayout()
                    }
                }
        }

        if (!isTablet) {
            LyricsBottomSheet(
                visible = isLyricsModeEnabled,
                currentSong = uiState.currentSong,
                syncedLyrics = syncedLyrics,
                plainLyrics = plainLyrics,
                translatedPlainLyrics = translatedPlainLyrics,
                isLoading = isLyricsLoading,
                lyricsError = lyricsError,
                currentPositionProvider = currentPositionProvider,
                contentReady = true,
                playerBackgroundColor = playerBackgroundColor,
                canvasArtwork = canvasArtwork,
                contentColor = contentColor,
                contentSecondary = contentSecondary,
                dynamicAccentColor = dynamicAccentColor,
                onRetryLyrics = onRetryLyrics,
                onFetchLyrics = onFetchLyrics,
                onSeekTo = onSeekTo,
                providerResults = providerResults,
                selectedProvider = selectedProvider,
                onProviderSelected = onProviderSelected,
                isPlaying = uiState.isPlaying,
                onPlayPauseClick = onPlayPauseClick,
                duration = uiState.duration.toLong(),
                onCollapse = { viewModel.setLyricsModeEnabled(false) },
                onToggleTranslation = { viewModel.toggleLyricsTranslation() },
                syncOffsetMs = 0L,
                onSyncOffsetChange = {},
                modifier = Modifier.fillMaxSize()
            )
        }

        // --- Standalone Sleep Timer Bottom Sheet ---
        if (showSleepTimerDialog) {
            SleepTimerBottomSheet(
                onDismissRequest = { showSleepTimerDialog = false },
                sleepTimerEndTime = sleepTimerEndTime,
                sleepTimerMode = sleepTimerMode,
                onStartTimer = { mins ->
                    viewModel.startCustomSleepTimer(mins)
                },
                onCancelTimer = {
                    viewModel.cancelSleepTimer()
                },
                onSetEndOfSong = { enable ->
                    if (enable) viewModel.setSleepTimerMode(PlayerSharedViewModel.SleepTimerMode.END_OF_SONG)
                    else viewModel.cancelSleepTimer()
                }
            )
        }

        // --- Standalone Player Theme Bottom Sheet ---
        if (showPlayerThemeSheet) {
            PlayerThemeBottomSheet(
                onDismissRequest = { showPlayerThemeSheet = false },
                currentTheme = playerTheme,
                onThemeSelect = { newTheme ->
                    prefs.edit().putString("player_theme", newTheme).apply()
                }
            )
        }

        // --- STATS FOR NERDS DIAGNOSTICS DIALOG ---
        if (showStatsForNerdsDialog) {
            StatsForNerdsDialog(
                onDismissRequest = { showStatsForNerdsDialog = false },
                accentColor = dynamicAccentColor
            )
        }
    }
}

@Composable
private fun TrackDetailCard(
    label: String,
    value: String,
    contentColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = contentColor.copy(alpha = 0.05f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = contentColor.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = contentColor
            )
        }
    }
}

/**
 * Applies ultra-smooth alpha-gradient fading edges to horizontal scrolling content
 * when content is clipped at the left/right boundaries.
 */
fun Modifier.horizontalFadingEdges(
    scrollState: ScrollState,
    fadeLength: androidx.compose.ui.unit.Dp = 24.dp
): Modifier = this
    .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
    .drawWithContent {
        drawContent()
        val fadeLengthPx = fadeLength.toPx()
        val canScrollLeft = scrollState.value > 0
        val canScrollRight = scrollState.value < scrollState.maxValue

        if (fadeLengthPx > 0f) {
            // Left fading edge
            if (canScrollLeft) {
                drawRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color.Transparent, Color.Black),
                        startX = 0f,
                        endX = fadeLengthPx
                    ),
                    blendMode = BlendMode.DstIn
                )
            }

            // Right fading edge
            if (canScrollRight) {
                drawRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color.Black, Color.Transparent),
                        startX = size.width - fadeLengthPx,
                        endX = size.width
                    ),
                    blendMode = BlendMode.DstIn
                )
            }
        }
    }
