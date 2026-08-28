@file:Suppress("DEPRECATION")

package com.codetrio.overdrive

import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.filled.TrendingUp
import com.codetrio.overdrive.ui.explore.SearchScreen
import androidx.compose.ui.draw.alpha


import android.Manifest
import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.provider.OpenableColumns
import android.util.Log
import android.view.View
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.animateDp
import androidx.compose.runtime.Composable
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.codetrio.overdrive.model.SongItem
import com.codetrio.overdrive.service.AudioPlaybackService
import com.codetrio.overdrive.ui.EffectsScreenEntryPoint
import com.codetrio.overdrive.ui.PlayerBottomSheetCompose
import com.codetrio.overdrive.ui.settingsGraph
import com.codetrio.overdrive.ui.TagEditorScreenEntryPoint
import com.codetrio.overdrive.ui.explore.ExploreScreen
import com.codetrio.overdrive.ui.explore.GoogleSignInScreen
import com.codetrio.overdrive.ui.library.LibraryScreen
import com.codetrio.overdrive.ui.theme.SpatialFlowTheme
import com.codetrio.overdrive.ui.theme.observeKey
import com.codetrio.overdrive.ui.onboarding.OnboardingScreen
import android.content.Context
import com.codetrio.overdrive.util.CrashHandler
import com.codetrio.overdrive.ui.components.CrashReportDialog
import com.codetrio.overdrive.update.GitHubReleaseClient
import com.codetrio.overdrive.update.UpdateManager
import com.codetrio.overdrive.update.VersionUtils
import com.codetrio.overdrive.update.UpdateBottomSheet
import com.codetrio.overdrive.viewmodel.ExploreViewModel
import com.codetrio.overdrive.viewmodel.PlayerSharedViewModel
import com.codetrio.overdrive.ui.ObserveAsEvents
import com.codetrio.overdrive.ui.SnackbarController
import com.codetrio.overdrive.data.diagnostics.PlaybackDiagnosticsLogger
import com.codetrio.overdrive.data.diagnostics.LogLevel
import com.codetrio.overdrive.viewmodel.ExploreEvent
import com.google.android.material.color.DynamicColors
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val playerViewModel: PlayerSharedViewModel by viewModels()
    private val exploreViewModel: ExploreViewModel by viewModels()

    private var navController: NavController? = null
    private var previousDestination = "explore"
    private var isNavigating = false
    private var showShortcutsDialogTrigger: (() -> Unit)? = null

    var audioService: AudioPlaybackService? = null
    var isServiceBound = false
    lateinit var updateManager: UpdateManager

    var isBottomNavVisible by mutableStateOf(true)
        private set

    fun hideBottomNavWithAnimation() {
        val prefs = getSharedPreferences("AppSettings", android.content.Context.MODE_PRIVATE)
        if (prefs.getBoolean("hide_nav_on_scroll", false)) {
            isBottomNavVisible = false
        }
    }

    fun showBottomNavWithAnimation() {
        isBottomNavVisible = true
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            Log.d(TAG, "Service connected")
            val binder = service as? AudioPlaybackService.LocalBinder
            audioService = binder?.getService()
            isServiceBound = true
            audioService?.let {
                playerViewModel.audioService = it
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            Log.d(TAG, "Service disconnected")
            isServiceBound = false
            audioService = null
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        enableEdgeToEdge()

        val appPrefs = getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        val lang = appPrefs.getString("app_language", "system") ?: "system"
        if (lang != "system" && lang.isNotEmpty()) {
            androidx.appcompat.app.AppCompatDelegate.setApplicationLocales(
                androidx.core.os.LocaleListCompat.forLanguageTags(lang)
            )
        }

        DynamicColors.applyToActivityIfAvailable(this)
        super.onCreate(savedInstanceState)

        setupSystemBars()

        playerViewModel.loadLastPlaybackState()

        // Request the highest refresh rate the display supports (90Hz / 120Hz / VRR).
        // Without this, Android's default rate selector stays at 60Hz for most apps.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.attributes = window.attributes.apply {
                preferredRefreshRate = display?.supportedModes
                    ?.maxByOrNull { it.refreshRate }?.refreshRate ?: 0f
            }
        }

        startAudioService()
        // Permissions are now handled in Onboarding Screen

        setContent {
            @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
            val windowSizeClass = calculateWindowSizeClass(this)
            
            val prefs = remember { getSharedPreferences("AppSettings", MODE_PRIVATE) }
            val hasSeenOnboarding by prefs.observeKey("has_seen_onboarding_1_8", false)
            val themeMode by prefs.observeKey("theme_mode", "system")
            val systemDark = androidx.compose.foundation.isSystemInDarkTheme()
            val isDarkTheme = when (themeMode) {
                "dark" -> true
                "light" -> false
                else -> systemDark
            }

            var crashLog by remember { mutableStateOf<String?>(null) }
            LaunchedEffect(Unit) {
                if (CrashHandler.hasCrashReport(this@MainActivity)) {
                    crashLog = CrashHandler.getCrashReport(this@MainActivity)
                }
            }

            val currentSong by playerViewModel.currentSong.collectAsStateWithLifecycle()
            val playerBackgroundColor by playerViewModel.playerBackgroundColor.collectAsStateWithLifecycle()
            val dynamicAlbumColor = if (currentSong != null) playerBackgroundColor else null

            val forceHighRefreshRate by prefs.observeKey("force_high_refresh_rate", false)
            val supportedHighestFps = com.codetrio.overdrive.util.rememberSupportedHighestFps()
            val isHighRefreshRateSupported = supportedHighestFps > 60.5f

            com.codetrio.overdrive.util.ApplyRefreshRate(
                isEnabled = forceHighRefreshRate && isHighRefreshRateSupported,
                targetFps = supportedHighestFps
            )

            SpatialFlowTheme(
                darkTheme = isDarkTheme, 
                dynamicAlbumColor = dynamicAlbumColor,
                windowSizeClass = windowSizeClass
            ) {
                crashLog?.let { log ->
                    CrashReportDialog(
                        crashLog = log,
                        onDismiss = {
                            CrashHandler.clearCrashReport(this@MainActivity)
                            crashLog = null
                        },
                        onReport = {
                            CrashHandler.clearCrashReport(this@MainActivity)
                            crashLog = null
                        }
                    )
                }
                val hideNavLabels by prefs.observeKey("hide_nav_labels", false)
                val dynamicNavStyle by prefs.observeKey("dynamic_nav_style", false)
                val floatingNavBar by prefs.observeKey("floating_nav_bar", false)
                val isBlurEnabled by prefs.observeKey("navigation_blur", true)

                val currentNavController = rememberNavController()
                LaunchedEffect(currentNavController) {
                    navController = currentNavController
                }

                // Global event listener for exploreViewModel (handles instant playback triggers from Search, Artists, Home, Playlists, etc.)
                ObserveAsEvents(exploreViewModel.events) { event ->
                    when (event) {
                        is ExploreEvent.ShowSnackbar -> {
                            SnackbarController.showMessage(event.message.asString(this@MainActivity))
                        }
                        is ExploreEvent.TriggerInstantPlayback -> {
                            val state = exploreViewModel.uiState.value
                            val queue = state.onlineQueue
                            val index = state.currentOnlineIndex
                            val currentSong = state.currentOnlineSong

                            val songsToPlay = when {
                                queue.isNotEmpty() && index in queue.indices -> {
                                    queue.map { s ->
                                        SongItem.createOnlineSong(
                                            videoId = s.videoId,
                                            title = s.title,
                                            artist = s.artist,
                                            streamUrl = "",
                                            durationMs = s.durationMs,
                                            thumbnailUrl = s.thumbnailUrl,
                                            artistId = s.artistId,
                                            animatedThumbnailUrl = s.animatedThumbnailUrl
                                        )
                                    }
                                }
                                currentSong != null -> {
                                    listOf(
                                        SongItem.createOnlineSong(
                                            videoId = currentSong.videoId,
                                            title = currentSong.title,
                                            artist = currentSong.artist,
                                            streamUrl = "",
                                            durationMs = currentSong.durationMs,
                                            thumbnailUrl = currentSong.thumbnailUrl,
                                            artistId = currentSong.artistId,
                                            animatedThumbnailUrl = currentSong.animatedThumbnailUrl
                                        )
                                    )
                                }
                                else -> emptyList()
                            }

                            if (songsToPlay.isNotEmpty()) {
                                val playIndex = if (queue.isNotEmpty() && index in queue.indices) index else 0
                                val targetSong = songsToPlay.getOrNull(playIndex)
                                PlaybackDiagnosticsLogger.log(
                                    level = LogLevel.INFO,
                                    tag = "UI-Trigger",
                                    message = "TriggerInstantPlayback: playing '${targetSong?.title}' (${targetSong?.videoId}) [Queue size: ${songsToPlay.size}, index: $playIndex]"
                                )
                                playerViewModel.setSongList(songsToPlay)
                                playerViewModel.playSongAtIndex(playIndex)
                            } else {
                                PlaybackDiagnosticsLogger.log(
                                    level = LogLevel.WARN,
                                    tag = "UI-Trigger",
                                    message = "TriggerInstantPlayback received but queue and currentSong are both empty!"
                                )
                            }
                        }
                    }
                }

                val isPlayerExpanded by playerViewModel.isPlayerExpanded.collectAsStateWithLifecycle()

                var editingSong by remember { mutableStateOf<SongItem?>(null) }

                val isTablet = windowSizeClass.widthSizeClass != androidx.compose.material3.windowsizeclass.WindowWidthSizeClass.Compact

                androidx.compose.foundation.layout.Row(
                    modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
                ) {
                    if (isTablet) {
                        val navBackStackEntry by currentNavController.currentBackStackEntryAsState()
                        val currentDestination = navBackStackEntry?.destination
                        
                        val showNavRail = currentDestination?.route !in listOf("onboarding", "google_signin")

                        val playerExpansionFraction by playerViewModel.playerExpansionFraction.collectAsStateWithLifecycle(initialValue = 0f)
                        if (showNavRail) {
                            androidx.compose.foundation.layout.Box(
                                modifier = Modifier
                                    .width(80.dp * (1f - playerExpansionFraction))
                                    .alpha(1f - playerExpansionFraction)
                            ) {
                                androidx.compose.material3.NavigationRail(
                                    modifier = Modifier
                                        .requiredWidth(80.dp)
                                        .offset(x = -80.dp * playerExpansionFraction),
                                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                                ) {
                                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.weight(1f))
                                    val tabsState by rememberBottomNavTabs(LocalContext.current)
                                    
                                    var lastClickTime by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(0L) }
                                    var lastClickRoute by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("") }

                                    val items = tabsState.filter { it.isVisible }.map { Triple(it.route, getNavLabelForRoute(it.route), getNavIconForRoute(it.route)) }
                                    items.forEach { (route, label, iconData) ->
                                        val selected = when (route) {
                                            "explore" -> currentDestination?.route in listOf("explore", "album", "playlist", "artist", "section", "mood", "genres")
                                            "library" -> currentDestination?.route in listOf("library", "statistics", "tag_editor")
                                            "search" -> currentDestination?.route == "search"
                                            "settings" -> currentDestination?.route in listOf("settings", "music_management", "account", "appearance", "playback", "haptics", "about", "feedback", "whats_new", "backup_restore", "customize_bottom_nav", "playback_logs", "developer_performance")
                                            else -> currentDestination?.route == route
                                        }
                                        androidx.compose.material3.NavigationRailItem(
                                            selected = selected,
                                            onClick = {
                                                val currentTime = System.currentTimeMillis()
                                                val isDoubleTap = (currentTime - lastClickTime < 400L) || selected
                                                lastClickTime = currentTime
                                                lastClickRoute = route

                                                if (isDoubleTap && selected) {
                                                    when (route) {
                                                        "search" -> exploreViewModel.triggerSearchFocus()
                                                        "explore" -> {
                                                            exploreViewModel.resetToHome()
                                                            exploreViewModel.triggerScrollToTop()
                                                        }
                                                        "library" -> playerViewModel.triggerScrollToTop()
                                                        "settings" -> {
                                                            currentNavController.popBackStack("settings", inclusive = false)
                                                        }
                                                    }
                                                }

                                                if (route == "explore" && !isDoubleTap) {
                                                    exploreViewModel.resetToHome()
                                                }

                                                if (!selected) {
                                                    currentNavController.navigate(route) {
                                                        popUpTo(currentNavController.graph.findStartDestination().id) { saveState = true }
                                                        launchSingleTop = true
                                                        restoreState = true
                                                    }
                                                }
                                            },
                                            icon = {
                                                if (iconData is androidx.compose.ui.graphics.vector.ImageVector) {
                                                    Icon(imageVector = iconData, contentDescription = label, modifier = Modifier.size(26.dp))
                                                } else if (iconData is Int) {
                                                    Icon(painter = painterResource(id = iconData), contentDescription = label, modifier = Modifier.size(26.dp))
                                                }
                                            },
                                            label = if (hideNavLabels) null else { { Text(label) } }
                                        )
                                    }
                                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }

                        LaunchedEffect(Unit) {
                            playerViewModel.setBottomNavHeight(0f)
                            playerViewModel.setBottomNavTranslationY(0f)
                        }
                    }

                    Scaffold(
                        modifier = Modifier.weight(1f),
                        contentWindowInsets = WindowInsets(0, 0, 0, 0),
                        bottomBar = {
                            if (!isTablet) {
                        val navBackStackEntry by currentNavController.currentBackStackEntryAsState()
                        val currentDestination = navBackStackEntry?.destination

                        val showBottomBar = currentDestination?.route !in listOf("onboarding", "google_signin")

                        val density = LocalDensity.current
                        val bottomPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

                        val playerExpansionFractionState = remember { mutableStateOf(0f) }
                        LaunchedEffect(playerViewModel) {
                            playerViewModel.playerExpansionFraction.collect {
                                playerExpansionFractionState.value = it
                            }
                        }

                        val navBarTransition = updateTransition(
                            targetState = dynamicNavStyle,
                            label = "NavBarStyle"
                        )
                        val navBarHeight by navBarTransition.animateDp(
                            label = "navBarHeight",
                            transitionSpec = { spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow) }
                        ) { if (it) 64.dp else 80.dp }
                        val navIconSize by navBarTransition.animateDp(
                            label = "navIconSize",
                            transitionSpec = { spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow) }
                        ) { if (it) 34.dp else 26.dp }
                        val navElevation by navBarTransition.animateDp(
                            label = "navElevation",
                            transitionSpec = { spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow) }
                        ) { if (it) 8.dp else 3.dp }

                        val totalBottomNavHeight = if (floatingNavBar) {
                            navBarHeight + bottomPadding + 16.dp
                        } else {
                            navBarHeight + bottomPadding
                        }
                        val totalSlideDistPx = with(density) { totalBottomNavHeight.toPx() }

                        // User requested the bottom bar to be ALWAYS visible except during media player expansion
                        val baseTargetTranslation = if (showBottomBar) 0f else totalSlideDistPx

                        val animatedBaseTranslationState = animateFloatAsState(
                            targetValue = baseTargetTranslation,
                            animationSpec = spring(
                                dampingRatio = 0.85f,
                                stiffness = Spring.StiffnessLow
                            ),
                            label = "BottomNavTranslationY"
                        )

                        // Monitor translations without triggering recomposition every frame
                        LaunchedEffect(totalSlideDistPx) {
                            snapshotFlow {
                                val currentBase = animatedBaseTranslationState.value
                                val currentFraction = playerExpansionFractionState.value
                                (currentBase + (totalSlideDistPx * currentFraction)).coerceAtMost(totalSlideDistPx)
                            }.collect { translationY ->
                                playerViewModel.setBottomNavTranslationY(translationY)
                            }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(totalBottomNavHeight)
                                .graphicsLayer {
                                    val currentBase = animatedBaseTranslationState.value
                                    val currentFraction = playerExpansionFractionState.value
                                    val translationY = (currentBase + (totalSlideDistPx * currentFraction)).coerceAtMost(totalSlideDistPx)
                                    this.translationY = translationY
                                    this.alpha = if (translationY >= totalSlideDistPx - 1f) 0f else 1f
                                }
                                .onGloballyPositioned { coordinates ->
                                    val height = coordinates.size.height.toFloat()
                                    playerViewModel.setBottomNavHeight(height)
                                }
                        ) {
                            val navBarContent: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit = {
                                val tabsState by rememberBottomNavTabs(LocalContext.current)
                                
                                var lastClickTime by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(0L) }
                                var lastClickRoute by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("") }

                                val items = tabsState.filter { it.isVisible }.map { Triple(it.route, getNavLabelForRoute(it.route), getNavIconForRoute(it.route)) }
                                items.forEach { (route, label, iconData) ->
                                    val selected = currentDestination?.route == route
                                    NavigationBarItem(
                                        selected = selected,
                                        onClick = {
                                            val currentTime = System.currentTimeMillis()
                                            val isDoubleTap = (currentTime - lastClickTime < 400L) || selected
                                            lastClickTime = currentTime
                                            lastClickRoute = route

                                            if (isDoubleTap && selected) {
                                                when (route) {
                                                    "search" -> exploreViewModel.triggerSearchFocus()
                                                    "explore" -> {
                                                        exploreViewModel.resetToHome()
                                                        exploreViewModel.triggerScrollToTop()
                                                    }
                                                    "library" -> playerViewModel.triggerScrollToTop()
                                                    "settings" -> {
                                                        // Pop all the way back to the root settings view
                                                        currentNavController.popBackStack("settings", inclusive = false)
                                                    }
                                                }
                                            }

                                            if (route == "explore" && !isDoubleTap) {
                                                exploreViewModel.resetToHome()
                                            }
                                            
                                            if (!selected) {
                                                currentNavController.navigate(route) {
                                                    popUpTo(currentNavController.graph.findStartDestination().id) {
                                                        saveState = true
                                                    }
                                                    launchSingleTop = true
                                                    restoreState = true
                                                }
                                            }
                                        },
                                        icon = {
                                            if (iconData is androidx.compose.ui.graphics.vector.ImageVector) {
                                                Icon(imageVector = iconData, contentDescription = label, modifier = Modifier.size(navIconSize))
                                            } else if (iconData is Int) {
                                                Icon(painter = painterResource(id = iconData), contentDescription = label, modifier = Modifier.size(navIconSize))
                                            }
                                        },
                                        label = if (hideNavLabels) null else { { Text(label) } }
                                    )
                                }
                            }

                            if (floatingNavBar) {
                                NavigationBar(
                                    modifier = Modifier
                                        .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = bottomPadding + 8.dp)
                                        .clip(RoundedCornerShape(24.dp))
                                        .height(navBarHeight),
                                    windowInsets = androidx.compose.foundation.layout.WindowInsets(0.dp),
                                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                                    tonalElevation = navElevation,
                                    content = navBarContent
                                )
                            } else {
                                NavigationBar(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(totalBottomNavHeight),
                                    windowInsets = androidx.compose.material3.NavigationBarDefaults.windowInsets,
                                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                                    tonalElevation = navElevation,
                                    content = navBarContent
                                )
                            }
                        }
                    }
                }
            ) { paddingValues ->
                    val navBackStackEntry by currentNavController.currentBackStackEntryAsState()
                    val currentDestination = navBackStackEntry?.destination
                    val showBottomBar = currentDestination?.route !in listOf("onboarding", "google_signin")

                    val playerExpansionFractionState = remember { mutableStateOf(0f) }
                    LaunchedEffect(playerViewModel) {
                        playerViewModel.playerExpansionFraction.collect {
                            playerExpansionFractionState.value = it
                        }
                    }

                    // Base padding target not considering expansion
                    val baseTargetPadding = if (showBottomBar && !floatingNavBar) {
                        paddingValues.calculateBottomPadding()
                    } else {
                        0.dp
                    }

                    val animatedBasePaddingState = animateDpAsState(
                        targetValue = baseTargetPadding,
                        animationSpec = spring(
                            dampingRatio = 0.85f,
                            stiffness = Spring.StiffnessLow
                        ),
                        label = "BottomNavPadding"
                    )

                    // Final padding does not scale down during expansion to prevent massive Layout invalidation
                    // The player sheet covers the NavHost anyway, so keeping padding static is optimal
                    val finalPadding = animatedBasePaddingState.value

                    Box(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        val currentContext = androidx.compose.ui.platform.LocalContext.current
                        val bypassBlur = com.codetrio.overdrive.util.PerformanceManager.shouldBypassBlur(currentContext)

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(bottom = finalPadding.coerceAtLeast(0.dp))
                                .graphicsLayer {
                                    if (isBlurEnabled && !bypassBlur) {
                                        val fraction = playerExpansionFractionState.value
                                        if (fraction > 0.01f && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                            val maxBlurRadius = 24.dp.toPx()
                                            val currentBlurPx = maxBlurRadius * fraction
                                            if (currentBlurPx > 0.1f) {
                                                renderEffect = android.graphics.RenderEffect.createBlurEffect(
                                                    currentBlurPx,
                                                    currentBlurPx,
                                                    android.graphics.Shader.TileMode.DECAL
                                                ).asComposeRenderEffect()
                                            }
                                        }
                                    }
                                }
                                .drawWithContent {
                                    drawContent()
                                    if (isBlurEnabled && (bypassBlur || Build.VERSION.SDK_INT < Build.VERSION_CODES.S)) {
                                        val fraction = playerExpansionFractionState.value
                                        if (fraction > 0.01f) {
                                            drawRect(color = androidx.compose.ui.graphics.Color.Black.copy(alpha = fraction * 0.65f))
                                        }
                                    }
                                }
                        ) {
                            val routeIndices = remember {
                                mapOf(
                                    "explore" to 0,
                                    "search" to 1,
                                    "library" to 2,
                                    "effects" to 3,
                                    "settings" to 4,
                                    "music_management" to 5,
                                    "account" to 5,
                                    "appearance" to 5,
                                    "playback" to 5,
                                    "haptics" to 5,
                                    "about" to 5,
                                    "feedback" to 4,
                                    "whats_new" to 4,
                                    "backup_restore" to 4
                                )
                            }

                            NavHost(
                                navController = currentNavController,
                                startDestination = if (hasSeenOnboarding) "explore" else "onboarding",
                                modifier = Modifier.fillMaxSize(),
                                enterTransition = {
                                    val initialRoute = initialState.destination.route ?: ""
                                    val targetRoute = targetState.destination.route ?: ""
                                    val initialIndex = routeIndices[initialRoute] ?: 0
                                    val targetIndex = routeIndices[targetRoute] ?: 0
                                    
                                    if (targetIndex > initialIndex) {
                                        slideInHorizontally(
                                            initialOffsetX = { it },
                                            animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMedium)
                                        ) + fadeIn(animationSpec = tween(180, easing = androidx.compose.animation.core.FastOutSlowInEasing))
                                    } else {
                                        slideInHorizontally(
                                            initialOffsetX = { -it },
                                            animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMedium)
                                        ) + fadeIn(animationSpec = tween(180, easing = androidx.compose.animation.core.FastOutSlowInEasing))
                                    }
                                },
                                exitTransition = {
                                    val initialRoute = initialState.destination.route ?: ""
                                    val targetRoute = targetState.destination.route ?: ""
                                    val initialIndex = routeIndices[initialRoute] ?: 0
                                    val targetIndex = routeIndices[targetRoute] ?: 0
                                    
                                    if (targetIndex > initialIndex) {
                                        slideOutHorizontally(
                                            targetOffsetX = { -it / 3 },
                                            animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMedium)
                                        ) + fadeOut(animationSpec = tween(180, easing = androidx.compose.animation.core.FastOutSlowInEasing))
                                    } else {
                                        slideOutHorizontally(
                                            targetOffsetX = { it / 3 },
                                            animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMedium)
                                        ) + fadeOut(animationSpec = tween(180, easing = androidx.compose.animation.core.FastOutSlowInEasing))
                                    }
                                },
                                popEnterTransition = {
                                    val initialRoute = initialState.destination.route ?: ""
                                    val targetRoute = targetState.destination.route ?: ""
                                    val initialIndex = routeIndices[initialRoute] ?: 0
                                    val targetIndex = routeIndices[targetRoute] ?: 0
                                    
                                    if (targetIndex > initialIndex) {
                                        slideInHorizontally(
                                            initialOffsetX = { it },
                                            animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMedium)
                                        ) + fadeIn(animationSpec = tween(180, easing = androidx.compose.animation.core.FastOutSlowInEasing))
                                    } else {
                                        slideInHorizontally(
                                            initialOffsetX = { -it },
                                            animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMedium)
                                        ) + fadeIn(animationSpec = tween(180, easing = androidx.compose.animation.core.FastOutSlowInEasing))
                                    }
                                },
                                popExitTransition = {
                                    val initialRoute = initialState.destination.route ?: ""
                                    val targetRoute = targetState.destination.route ?: ""
                                    val initialIndex = routeIndices[initialRoute] ?: 0
                                    val targetIndex = routeIndices[targetRoute] ?: 0
                                    
                                    if (targetIndex > initialIndex) {
                                        slideOutHorizontally(
                                            targetOffsetX = { -it / 3 },
                                            animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMedium)
                                        ) + fadeOut(animationSpec = tween(180, easing = androidx.compose.animation.core.FastOutSlowInEasing))
                                    } else {
                                        slideOutHorizontally(
                                            targetOffsetX = { it / 3 },
                                            animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMedium)
                                        ) + fadeOut(animationSpec = tween(180, easing = androidx.compose.animation.core.FastOutSlowInEasing))
                                    }
                                }
                            ) {
                                composableWithBlur("onboarding") {
                                    OnboardingScreen(
                                        onComplete = {
                                            currentNavController.navigate("explore") {
                                                popUpTo("onboarding") { inclusive = true }
                                            }
                                        },
                                        onNavigateToSignIn = {
                                            currentNavController.navigate("google_signin")
                                        }
                                    )
                                }
                                composableWithBlur("explore") {
                                    ExploreScreen(
                                        viewModel = exploreViewModel,
                                        playerSharedViewModel = playerViewModel,
                                        onNavigateToLibrary = {
                                            currentNavController.navigate("library") {
                                                popUpTo(currentNavController.graph.findStartDestination().id) {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    )
                                }
                                composableWithBlur("search") {
                                    SearchScreen(
                                        viewModel = exploreViewModel,
                                        playerSharedViewModel = playerViewModel,
                                        onNavigateToExplore = {
                                            currentNavController.navigate("explore") {
                                                popUpTo(currentNavController.graph.findStartDestination().id) {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    )
                                }
                                composableWithBlur("statistics") {
                                    com.codetrio.overdrive.ui.statistics.StatisticsScreen(
                                        playerViewModel = playerViewModel,
                                        onNavigateToExplore = {
                                            currentNavController.navigate("explore") {
                                                popUpTo(currentNavController.graph.findStartDestination().id) {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        },
                                        onNavigateToDna = {
                                            currentNavController.navigate("music_dna")
                                        }
                                    )
                                }
                                composableWithBlur("music_dna") {
                                    com.codetrio.overdrive.ui.statistics.dna.MusicDnaScreen(
                                        viewModel = playerViewModel,
                                        onNavigateBack = {
                                            currentNavController.popBackStack()
                                        }
                                    )
                                }
                                composableWithBlur("library") {
                                    LibraryScreen(
                                        viewModel = playerViewModel,
                                        onEditSong = { song ->
                                            editingSong = song
                                            currentNavController.navigate("tag_editor")
                                        },
                                        onNavigateToExplore = {
                                            currentNavController.navigate("explore") {
                                                popUpTo(currentNavController.graph.findStartDestination().id) {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    )
                                }
                                composableWithBlur("effects") {
                                    EffectsScreenEntryPoint(viewmodel = playerViewModel)
                                }
                                settingsGraph(currentNavController)
                                composableWithBlur("tag_editor") {
                                    editingSong?.let { song ->
                                        TagEditorScreenEntryPoint(
                                            song = song,
                                            onNavigateUp = {
                                                currentNavController.navigateUp()
                                            }
                                        )
                                    }
                                }
                                composableWithBlur("google_signin") {
                                    val accountViewModel: com.codetrio.overdrive.viewmodel.AccountViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
                                    GoogleSignInScreen(
                                        accountViewModel = accountViewModel,
                                        onSignInSuccess = {
                                            currentNavController.navigate("explore") {
                                                popUpTo("explore") { inclusive = true }
                                            }
                                        },
                                        onNavigateUp = {
                                            currentNavController.navigateUp()
                                        }
                                    )
                                }
                            }
                        }

                        val currentRoute = currentDestination?.route
                        if (currentRoute != "onboarding" && currentRoute != "google_signin") {
                            PlayerBottomSheetCompose(
                                activity = this@MainActivity,
                                viewModel = playerViewModel,
                                isTablet = isTablet
                            )
                        }

                        val isCastSheetVisible by playerViewModel.isCastSheetVisible.collectAsStateWithLifecycle()
                        if (isCastSheetVisible) {
                            com.codetrio.overdrive.ui.components.CastBottomSheet(
                                viewModel = playerViewModel,
                                onDismiss = { playerViewModel.hideCastSheet() }
                            )
                        }

                        var isShortcutsDialogVisible by remember { mutableStateOf(false) }
                        LaunchedEffect(Unit) {
                            showShortcutsDialogTrigger = { isShortcutsDialogVisible = true }
                        }
                        if (isShortcutsDialogVisible) {
                            com.codetrio.overdrive.ui.components.KeyboardShortcutsDialog(
                                onDismissRequest = { isShortcutsDialogVisible = false }
                            )
                        }
                        }
                    }
                }
            }
        }

        updateManager = UpdateManager(this)
        handleIntent(intent)
    }

    fun navigateToSettings() {
        val controller = navController ?: return
        if (controller.currentDestination?.route != "settings") {
            controller.navigate("settings") {
                popUpTo(controller.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
            previousDestination = "settings"
        }
    }

    fun navigateToGoogleSignIn() {
        val controller = navController ?: return
        if (controller.currentDestination?.route != "google_signin") {
            controller.navigate("google_signin")
        }
    }

    fun showArtistPage(artistId: String?, artistName: String?) {
        val controller = navController ?: return
        val cameFromOutside = controller.currentDestination?.route != "explore"
        
        if (cameFromOutside) {
            isNavigating = true
            controller.navigate("explore") {
                popUpTo(controller.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
            previousDestination = "explore"
            isNavigating = false
        }

        if (!artistId.isNullOrBlank()) {
            if (cameFromOutside) {
                exploreViewModel.cameFromLibrary = true
            }
            exploreViewModel.loadArtist(artistId)
        } else if (!artistName.isNullOrBlank() && artistName != "Unknown Artist") {
            if (cameFromOutside) {
                exploreViewModel.cameFromLibrary = true
            }
            lifecycleScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                val searchRes = com.codetrio.overdrive.data.innertube.YouTubeMusic.search(
                    artistName,
                    com.codetrio.overdrive.data.innertube.SearchFilter.ARTISTS
                ).getOrNull()
                val firstArtist = searchRes?.items?.filterIsInstance<com.codetrio.overdrive.data.innertube.SearchItem.Artist>()?.firstOrNull()?.artist
                    ?: searchRes?.items?.filterIsInstance<com.codetrio.overdrive.data.innertube.SearchItem.TopResult>()?.firstOrNull()?.artist
                withContext(kotlinx.coroutines.Dispatchers.Main) {
                    if (firstArtist?.browseId != null) {
                        exploreViewModel.loadArtist(firstArtist.browseId, firstArtist.thumbnailUrl)
                    } else {
                        exploreViewModel.setSearchFilter(com.codetrio.overdrive.data.innertube.SearchFilter.ARTISTS)
                        exploreViewModel.search(artistName)
                    }
                }
            }
        }
    }

    fun showAlbumPage(albumId: String?) {
        val controller = navController ?: return
        if (albumId.isNullOrBlank()) return
        val cameFromOutside = controller.currentDestination?.route != "explore"
        if (cameFromOutside) {
            isNavigating = true
            controller.navigate("explore") {
                popUpTo(controller.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
            previousDestination = "explore"
            isNavigating = false
        }
        if (cameFromOutside) {
            exploreViewModel.cameFromLibrary = true
        }
        exploreViewModel.loadAlbum(albumId)
    }

    private fun startAudioService() {
        val serviceIntent = Intent(this, AudioPlaybackService::class.java)
        try {
            startService(serviceIntent)
        } catch (e: Exception) {
            Log.w(TAG, "startService failed, relying on bind: ${e.message}")
        }
        bindService(serviceIntent, serviceConnection, BIND_AUTO_CREATE)
        Log.d(TAG, "Audio service started and bound")
    }

    private fun ensureServiceRunning() {
        val manager = getSystemService(ACTIVITY_SERVICE) as? ActivityManager ?: return
        val isRunning = manager.getRunningServices(Int.MAX_VALUE).any {
            it.service.className == AudioPlaybackService::class.java.name
        }
        if (!isRunning) startAudioService()
    }

    private fun checkAudioPermission() {
        val requiredPermissions = getRequiredRuntimePermissions()
        val missingPermissions = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, missingPermissions.toTypedArray(), AUDIO_PERMISSION_REQUEST)
        }
    }

    private fun getRequiredRuntimePermissions(): Array<String> {
        return when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> arrayOf(
                Manifest.permission.READ_MEDIA_AUDIO,
                Manifest.permission.POST_NOTIFICATIONS,
                Manifest.permission.RECORD_AUDIO
            )
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO
            )
            else -> arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO
            )
        }
    }

    override fun onResume() {
        super.onResume()
        setupSystemBars()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) setupSystemBars()
    }

    fun setupSystemBars() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isStatusBarContrastEnforced = false
            window.isNavigationBarContrastEnforced = false
        }

        val isDarkMode = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
        WindowInsetsControllerCompat(window, window.decorView).apply {
            isAppearanceLightStatusBars = !isDarkMode
            isAppearanceLightNavigationBars = !isDarkMode
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(KEY_PREVIOUS_DESTINATION, previousDestination)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController?.navigateUp() == true || super.onSupportNavigateUp()
    }

    override fun onStart() {
        super.onStart()
        ensureServiceRunning()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isServiceBound) {
            try {
                unbindService(serviceConnection)
            } catch (_: Exception) {
                Log.d(TAG, "Service not bound, skipping unbind")
            }
            isServiceBound = false
        }
    }
    fun showSnackbar(message: String, duration: Int) {
        com.codetrio.overdrive.ui.SnackbarController.showMessage(message)
    }

    @Deprecated("This method has been deprecated in favor of using the\n      {@link OnBackPressedDispatcher} via {@link #getOnBackPressedDispatcher()}.\n      The OnBackPressedDispatcher controls how back button events are dispatched\n      to one or more {@link OnBackPressedCallback} objects.")
    @SuppressLint("GestureBackNavigation")
    override fun onBackPressed() {
        super.onBackPressed()
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun handleIntent(intent: Intent?): Boolean {
        if (intent == null) return false

        if (Intent.ACTION_SEND == intent.action && "text/plain" == intent.type) {
            val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
            if (!sharedText.isNullOrEmpty()) {
                val urlRegex = "(?i)\\b((?:https?://|www\\d{0,3}[.]|[a-z0-9.\\-]+[.][a-z]{2,4}/)(?:[^\\s()<>]+|\\(([^\\s()<>]+|(\\([^\\s()<>]+\\)))*\\))+(?:\\(([^\\s()<>]+|(\\([^\\s()<>]+\\)))*\\)|[^\\s`!()\\[\\]{};:'\".,<>?«»“”‘’]))".toRegex()
                val url = urlRegex.find(sharedText)?.value
                if (url != null && (url.contains("youtube.com") || url.contains("youtu.be"))) {
                    exploreViewModel.onAction(com.codetrio.overdrive.viewmodel.ExploreAction.OnHandleDeepLink(url))
                    if (navController?.currentDestination?.route != "explore") {
                        navController?.navigate("explore") {
                            popUpTo(navController?.graph?.findStartDestination()?.id ?: 0) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                    return true
                }
            }
        }

        if (Intent.ACTION_VIEW == intent.action || "android.intent.action.MUSIC_PLAYER" == intent.action) {
            intent.data?.let { uri ->
                val scheme = uri.scheme
                if (scheme == "http" || scheme == "https") {
                    exploreViewModel.onAction(com.codetrio.overdrive.viewmodel.ExploreAction.OnHandleDeepLink(uri.toString()))
                    if (navController?.currentDestination?.route != "explore") {
                        navController?.navigate("explore") {
                            popUpTo(navController?.graph?.findStartDestination()?.id ?: 0) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                    return true
                } else {
                    playExternalUri(uri)
                    return false
                }
            }
        }

        if (navController != null && intent.getBooleanExtra(EXTRA_OPEN_PLAYER, false)) {
            try {
                navController?.navigate("library")
            } catch (e: Exception) {
                Log.e(TAG, "Navigation request failed in handleIntent", e)
            }
        }
        return false
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun playExternalUri(uri: Uri) {
        var displayName = "External Track"
        if ("content" == uri.scheme) {
            try {
                contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (nameIndex != -1) {
                            cursor.getString(nameIndex)?.takeIf { it.isNotEmpty() }?.let {
                                displayName = it
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to query metadata for external uri", e)
            }
        } else if ("file" == uri.scheme) {
            displayName = uri.lastPathSegment ?: displayName
        }

        if (displayName.contains(".")) {
            val dotIdx = displayName.lastIndexOf('.')
            if (dotIdx > 0) displayName = displayName.substring(0, dotIdx)
        }

        val externalId = -System.currentTimeMillis()
        val externalSong = SongItem(
            externalId,
            displayName,
            "External Source",
            -1L,
            uri.toString(),
            0L,
            System.currentTimeMillis() / 1000
        ).apply { contentUri = uri }

        val triggerPlayTask = Runnable {
            playerViewModel.playSong(externalSong)
            navController?.let { controller ->
                try {
                    controller.navigate("library")
                } catch (e: Exception) {
                    Log.e(TAG, "Navigation route to PlayerFragment failed in playExternalUri", e)
                }
            }
        }

        if (!isServiceBound) {
            lifecycleScope.launch {
                playerViewModel.audioServiceState.collect { service ->
                    if (service != null) {
                        triggerPlayTask.run()
                        this@launch.coroutineContext[kotlinx.coroutines.Job]?.cancel()
                    }
                }
            }
        } else {
            triggerPlayTask.run()
        }
    }

    override fun dispatchKeyEvent(event: android.view.KeyEvent): Boolean {
        if (com.codetrio.overdrive.util.KeyboardShortcutHandler.handleKeyEvent(
                event = event,
                activity = this,
                viewModel = playerViewModel,
                navController = navController,
                onShowShortcutsHelp = {
                    showShortcutsDialogTrigger?.invoke()
                }
            )
        ) {
            return true
        }
        return super.dispatchKeyEvent(event)
    }

    companion object {
        private const val TAG = "MainActivity"
        private const val AUDIO_PERMISSION_REQUEST = 100
        private const val SPLASH_DURATION = 800L
        private const val KEY_PREVIOUS_DESTINATION = "key_previous_destination"
        const val EXTRA_OPEN_PLAYER = "open_player"
    }
}

fun NavGraphBuilder.composableWithBlur(
    route: String,
    enterTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition?)? = null,
    exitTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition?)? = null,
    popEnterTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition?)? = null,
    popExitTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition?)? = null,
    content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit
) {
    // Main bottom-nav tab routes — blur here is most expensive since users switch these constantly.
    val mainTabRoutes = setOf("explore", "search", "library", "statistics", "effects", "settings")
    val isMainTabRoute = route in mainTabRoutes

    composable(
        route = route,
        enterTransition = enterTransition,
        exitTransition = exitTransition,
        popEnterTransition = popEnterTransition,
        popExitTransition = popExitTransition
    ) { backStackEntry ->
        val context = androidx.compose.ui.platform.LocalContext.current
        val prefs = remember(context) { context.getSharedPreferences("AppSettings", Context.MODE_PRIVATE) }
        val isBlurEnabled by prefs.observeKey("navigation_blur", true)
        // Tab switch blur is off by default — it fires on every bottom-nav press
        val isTabSwitchBlurEnabled by prefs.observeKey("tab_switch_blur", false)

        // Decide whether to apply blur for this specific route + user preference combo
        val bypassBlur = com.codetrio.overdrive.util.PerformanceManager.shouldBypassBlur()
        val shouldBlur = isBlurEnabled && !bypassBlur && (!isMainTabRoute || isTabSwitchBlurEnabled)

        if (shouldBlur) {
            val blurRadiusState = transition.animateDp(
                label = "navigationBlur",
                transitionSpec = { tween(durationMillis = 260) }
            ) { state ->
                if (state == EnterExitState.Visible) 0.dp else 12.dp
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        val radiusPx = blurRadiusState.value.toPx()
                        if (radiusPx > 0.1f && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            renderEffect = android.graphics.RenderEffect.createBlurEffect(
                                radiusPx,
                                radiusPx,
                                android.graphics.Shader.TileMode.DECAL
                            ).asComposeRenderEffect()
                        }
                    }
            ) {
                content(backStackEntry)
            }
        } else {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                content(backStackEntry)
            }
        }
    }
}

@Composable
fun rememberBottomNavTabs(context: android.content.Context): androidx.compose.runtime.State<List<com.codetrio.overdrive.model.BottomNavTab>> {
    val prefs = remember { context.getSharedPreferences("AppSettings", android.content.Context.MODE_PRIVATE) }
    val tabsState = remember { androidx.compose.runtime.mutableStateOf(com.codetrio.overdrive.model.BottomNavTab.parse(prefs.getString("bottom_nav_tabs", null))) }
    
    DisposableEffect(prefs) {
        val listener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
            if (key == "bottom_nav_tabs") {
                tabsState.value = com.codetrio.overdrive.model.BottomNavTab.parse(sharedPreferences.getString(key, null))
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        onDispose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }
    return tabsState
}

fun getNavIconForRoute(route: String): Any {
    return when (route) {
        "explore" -> androidx.compose.material.icons.Icons.Rounded.Home
        "search" -> androidx.compose.material.icons.Icons.Rounded.Search
        "library" -> com.codetrio.overdrive.R.drawable.ic_library_music
        "statistics" -> androidx.compose.material.icons.Icons.Default.TrendingUp
        "effects" -> com.codetrio.overdrive.R.drawable.ic_equalizer
        "settings" -> com.codetrio.overdrive.R.drawable.ic_settings
        else -> androidx.compose.material.icons.Icons.Rounded.Home
    }
}

@androidx.compose.runtime.Composable
fun getNavLabelForRoute(route: String): String {
    return when (route) {
        "explore" -> androidx.compose.ui.res.stringResource(R.string.tab_explore)
        "search" -> androidx.compose.ui.res.stringResource(R.string.tab_search)
        "library" -> androidx.compose.ui.res.stringResource(R.string.tab_library)
        "statistics" -> androidx.compose.ui.res.stringResource(R.string.tab_statistics)
        "effects" -> androidx.compose.ui.res.stringResource(R.string.tab_effects)
        "settings" -> androidx.compose.ui.res.stringResource(R.string.tab_settings)
        else -> route.replaceFirstChar { it.uppercase() }
    }
}
