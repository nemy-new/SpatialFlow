@file:Suppress("DEPRECATION")

package com.codetrio.overdrive.ui.onboarding

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.net.toUri
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import com.codetrio.overdrive.ui.player.ArtworkPager
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.SideEffect
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.VolumeDown
import androidx.compose.material.icons.automirrored.filled.VolumeMute
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.codetrio.overdrive.R
import com.codetrio.overdrive.data.font.CustomFontManager
import com.codetrio.overdrive.data.font.FontTarget
import com.codetrio.overdrive.data.font.FontVariationConfig
import com.codetrio.overdrive.data.innertube.AccountManager
import com.codetrio.overdrive.data.innertube.YouTubeMusic
import com.codetrio.overdrive.model.SongItem
import com.codetrio.overdrive.ui.player.FullPlayer
import com.codetrio.overdrive.ui.player.PlayerUiState
import com.codetrio.overdrive.viewmodel.PlayerSharedViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Palette option item for Onboarding
 */
data class ColorPalettePreset(
    val id: String,
    val nameResId: Int,
    val primaryColor: Color,
    val secondaryColor: Color,
    val containerColor: Color
)

/**
 * Font preset option item for Onboarding
 */
data class TypographyPreset(
    val id: String,
    val titleResId: Int,
    val descResId: Int,
    val fontId: String,
    val weight: Float,
    val roundness: Float
)

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    onNavigateToSignIn: (() -> Unit)? = null,
    playerSharedViewModel: PlayerSharedViewModel? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val prefs = remember { context.getSharedPreferences("AppSettings", Context.MODE_PRIVATE) }
    val fontManager = remember { CustomFontManager.getInstance(context) }

    // Resolve PlayerSharedViewModel
    val activity = remember(context) { context.findActivity() as? ComponentActivity }
    val playerViewModel: PlayerSharedViewModel = playerSharedViewModel
        ?: (activity?.let { androidx.lifecycle.viewmodel.compose.viewModel(it) }
            ?: androidx.lifecycle.viewmodel.compose.viewModel())

    // Setup state (7 steps: Welcome, Theme, Color, Typography, Permissions, Account, Finish)
    val pageCount = 7
    val pagerState = rememberPagerState(pageCount = { pageCount })

    // User customized options
    var selectedPlayerTheme by rememberSaveable { mutableStateOf(prefs.getString("player_theme", "fluid") ?: "fluid") }
    var selectedThemeMode by rememberSaveable { mutableStateOf(prefs.getString("theme_mode", "system") ?: "system") }
    var selectedPaletteId by rememberSaveable { mutableStateOf(prefs.getString("selected_color_palette", "dynamic") ?: "dynamic") }
    var selectedTypographyPresetId by rememberSaveable { mutableStateOf("rounded") }
    var currentLanguage by rememberSaveable { mutableStateOf(prefs.getString("app_language", "system") ?: "system") }

    // Account state
    var isLoggedIn by remember { mutableStateOf(AccountManager.isLoggedIn(context)) }
    var userName by remember { mutableStateOf("Connected User") }
    var userProfileUrl by remember { mutableStateOf<String?>(null) }

    // BGM Volume state (0.0f - 1.0f) & Mute state
    var bgmVolume by rememberSaveable { mutableFloatStateOf(prefs.getFloat("onboarding_bgm_volume", 0.80f)) }
    var isBgmMuted by rememberSaveable { mutableStateOf(false) }

    // Robust MediaPlayer for guaranteed on-device BGM playback
    val bgmPlayer = remember {
        try {
            android.media.MediaPlayer.create(context, R.raw.autumn_wind)?.apply {
                isLooping = true
                val effectiveVol = if (isBgmMuted) 0f else bgmVolume
                setVolume(effectiveVol, effectiveVol)
                start()
            }
        } catch (e: Exception) {
            android.util.Log.e("OnboardingScreen", "Failed to start BGM MediaPlayer", e)
            null
        }
    }

    // Reactively apply volume slider changes
    LaunchedEffect(bgmVolume, isBgmMuted) {
        val effectiveVol = if (isBgmMuted) 0f else bgmVolume
        try {
            bgmPlayer?.setVolume(effectiveVol, effectiveVol)
            prefs.edit { putFloat("onboarding_bgm_volume", bgmVolume) }
        } catch (_: Exception) {}
    }

    // Re-check login state & manage BGM lifecycle whenever screen resumes/pauses
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            when (event) {
                androidx.lifecycle.Lifecycle.Event.ON_RESUME -> {
                    val currentLoggedIn = AccountManager.isLoggedIn(context)
                    if (currentLoggedIn != isLoggedIn) {
                        isLoggedIn = currentLoggedIn
                    }
                    if (bgmPlayer != null && !bgmPlayer.isPlaying) {
                        try { bgmPlayer.start() } catch (_: Exception) {}
                    }
                }
                androidx.lifecycle.Lifecycle.Event.ON_PAUSE -> {
                    if (bgmPlayer != null && bgmPlayer.isPlaying) {
                        try { bgmPlayer.pause() } catch (_: Exception) {}
                    }
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            try {
                bgmPlayer?.stop()
                bgmPlayer?.release()
            } catch (_: Exception) {}
        }
    }

    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) {
            withContext(Dispatchers.IO) {
                val result = YouTubeMusic.accountProfile()
                val profile = result.getOrNull()
                if (profile != null) {
                    userName = profile.name
                    userProfileUrl = profile.avatarUrl
                }
            }
        }
    }

    // Built-in Autumn Wind - Dyalla track for onboarding UI previews
    val onboardingSong = remember(context) {
        val rawUri = "android.resource://${context.packageName}/${R.raw.autumn_wind}".toUri()
        val artUri = "android.resource://${context.packageName}/${R.drawable.artwork_autumn_wind}"
        SongItem(
            id = -9999L,
            rawTitle = "Autumn Wind",
            rawArtist = "Dyalla",
            albumId = -1L,
            path = null,
            duration = 186000L,
            dateAdded = System.currentTimeMillis()
        ).apply {
            contentUri = rawUri
            thumbnailUrl = artUri
        }
    }

    // Permissions state
    val audioPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_AUDIO
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }
    val notifPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.POST_NOTIFICATIONS
    } else null
    val micPermission = Manifest.permission.RECORD_AUDIO

    var audioGranted by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, audioPermission) == PackageManager.PERMISSION_GRANTED)
    }
    var notifGranted by remember {
        mutableStateOf(notifPermission?.let { ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED } ?: true)
    }
    var micGranted by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, micPermission) == PackageManager.PERMISSION_GRANTED)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        audioGranted = perms[audioPermission] ?: audioGranted
        if (notifPermission != null) {
            notifGranted = perms[notifPermission] ?: notifGranted
        }
        micGranted = perms[micPermission] ?: micGranted
    }

    val systemInDark = isSystemInDarkTheme()
    val isEffectiveDark = when (selectedThemeMode) {
        "light" -> false
        "dark" -> true
        else -> systemInDark
    }

    // Material 3 Expressive Harmonized Color Palettes (Curated for premium visual fidelity)
    val systemBaseScheme = remember(isEffectiveDark, context) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            if (isEffectiveDark) androidx.compose.material3.dynamicDarkColorScheme(context)
            else androidx.compose.material3.dynamicLightColorScheme(context)
        } else {
            if (isEffectiveDark) darkColorScheme(primary = Color(0xFFD2BCFF))
            else lightColorScheme(primary = Color(0xFF6750A4))
        }
    }

    val colorPalettes = remember(isEffectiveDark, systemBaseScheme) {
        listOf(
            ColorPalettePreset(
                id = "dynamic",
                nameResId = R.string.onboarding_palette_dynamic,
                primaryColor = systemBaseScheme.primary,
                secondaryColor = systemBaseScheme.secondary,
                containerColor = systemBaseScheme.tertiary
            ),
            ColorPalettePreset(
                id = "celestial",
                nameResId = R.string.onboarding_palette_cyan,
                primaryColor = if (isEffectiveDark) Color(0xFF9ECAFF) else Color(0xFF00629E),
                secondaryColor = if (isEffectiveDark) Color(0xFFBBC7DB) else Color(0xFF50606F),
                containerColor = if (isEffectiveDark) Color(0xFF004975) else Color(0xFFCFE5FF)
            ),
            ColorPalettePreset(
                id = "emerald",
                nameResId = R.string.onboarding_palette_emerald,
                primaryColor = if (isEffectiveDark) Color(0xFF7CD9B1) else Color(0xFF006C4D),
                secondaryColor = if (isEffectiveDark) Color(0xFFB4CCBD) else Color(0xFF4C6356),
                containerColor = if (isEffectiveDark) Color(0xFF005139) else Color(0xFF98F8CD)
            ),
            ColorPalettePreset(
                id = "violet",
                nameResId = R.string.onboarding_palette_violet,
                primaryColor = if (isEffectiveDark) Color(0xFFD2BCFF) else Color(0xFF6851A5),
                secondaryColor = if (isEffectiveDark) Color(0xFFCCC2DC) else Color(0xFF625B71),
                containerColor = if (isEffectiveDark) Color(0xFF4F378B) else Color(0xFFEBDDFF)
            ),
            ColorPalettePreset(
                id = "sunset",
                nameResId = R.string.onboarding_palette_sunset,
                primaryColor = if (isEffectiveDark) Color(0xFFFFB77C) else Color(0xFF8F4E00),
                secondaryColor = if (isEffectiveDark) Color(0xFFDEC0A8) else Color(0xFF725A44),
                containerColor = if (isEffectiveDark) Color(0xFF703800) else Color(0xFFFFDCC2)
            ),
            ColorPalettePreset(
                id = "amber",
                nameResId = R.string.onboarding_palette_amber,
                primaryColor = if (isEffectiveDark) Color(0xFFFFB1C8) else Color(0xFF8F4A60),
                secondaryColor = if (isEffectiveDark) Color(0xFFE3BDC7) else Color(0xFF74565F),
                containerColor = if (isEffectiveDark) Color(0xFF723348) else Color(0xFFFFD9E2)
            )
        )
    }

    val activePalette = remember(selectedPaletteId, colorPalettes) {
        colorPalettes.firstOrNull { it.id == selectedPaletteId } ?: colorPalettes[0]
    }

    // Typography presets
    val typographyPresets = remember {
        listOf(
            TypographyPreset(
                id = "rounded",
                titleResId = R.string.onboarding_font_rounded_title,
                descResId = R.string.onboarding_font_rounded_desc,
                fontId = CustomFontManager.BUILTIN_GOOGLE_SANS_FLEX,
                weight = 700f,
                roundness = 100f
            ),
            TypographyPreset(
                id = "bold",
                titleResId = R.string.onboarding_font_bold_title,
                descResId = R.string.onboarding_font_bold_desc,
                fontId = CustomFontManager.BUILTIN_GOOGLE_SANS_FLEX_NON_ROUNDED,
                weight = 800f,
                roundness = 0f
            ),
            TypographyPreset(
                id = "regular",
                titleResId = R.string.onboarding_font_regular_title,
                descResId = R.string.onboarding_font_regular_desc,
                fontId = CustomFontManager.BUILTIN_GOOGLE_SANS_FLEX_NON_ROUNDED,
                weight = 500f,
                roundness = 0f
            ),
            TypographyPreset(
                id = "system",
                titleResId = R.string.onboarding_font_system_title,
                descResId = R.string.onboarding_font_system_desc,
                fontId = CustomFontManager.BUILTIN_SYSTEM_DEFAULT,
                weight = 400f,
                roundness = 0f
            )
        )
    }

    val activeTypography = remember(selectedTypographyPresetId) {
        typographyPresets.firstOrNull { it.id == selectedTypographyPresetId } ?: typographyPresets[0]
    }

    // Apply font when selected in onboarding
    fun applySelectedFont(preset: TypographyPreset) {
        fontManager.setFontForTarget(FontTarget.PLAYER_TITLE, preset.fontId)
        fontManager.setFontForTarget(FontTarget.HEADINGS, preset.fontId)
        fontManager.setFontForTarget(FontTarget.GLOBAL, preset.fontId)
        fontManager.setVariationConfig(
            FontTarget.PLAYER_TITLE,
            FontVariationConfig(weight = preset.weight, width = 100f, slant = 0f, roundness = preset.roundness, opticalSize = 22f)
        )
        fontManager.setVariationConfig(
            FontTarget.HEADINGS,
            FontVariationConfig(weight = preset.weight, width = 100f, slant = 0f, roundness = preset.roundness, opticalSize = 24f)
        )
    }

    fun switchLanguage(langCode: String) {
        currentLanguage = langCode
        prefs.edit { putString("app_language", langCode) }
    }

    // Dynamic localized context for instantaneous in-app language switching without Activity recreation or BGM stop
    val localizedContext = remember(currentLanguage, context) {
        if (currentLanguage == "system") {
            val systemLocale = java.util.Locale.getDefault()
            val config = android.content.res.Configuration(context.resources.configuration).apply {
                setLocale(systemLocale)
                setLayoutDirection(systemLocale)
            }
            context.createConfigurationContext(config)
        } else {
            val locale = java.util.Locale.forLanguageTag(currentLanguage)
            val config = android.content.res.Configuration(context.resources.configuration).apply {
                setLocale(locale)
                setLayoutDirection(locale)
            }
            context.createConfigurationContext(config)
        }
    }

    // Dynamic Material 3 Expressive ColorScheme that immediately reacts to palette & dark/light mode switches
    val onboardingColorScheme: androidx.compose.material3.ColorScheme = remember(isEffectiveDark, activePalette, systemBaseScheme) {
        if (isEffectiveDark) {
            systemBaseScheme.copy(
                primary = activePalette.primaryColor,
                onPrimary = if (activePalette.id == "dynamic") systemBaseScheme.onPrimary else Color.Black,
                primaryContainer = activePalette.containerColor.copy(alpha = 0.28f),
                onPrimaryContainer = Color.White,
                surface = Color(0xFF0C0E14),
                onSurface = Color(0xFFE2E2E9),
                surfaceVariant = Color(0xFF1B1E26),
                onSurfaceVariant = Color(0xFFC4C6D0),
                surfaceContainerLowest = Color(0xFF07080B),
                surfaceContainerLow = Color(0xFF101218),
                surfaceContainer = Color(0xFF161920),
                surfaceContainerHigh = Color(0xFF1E222A),
                surfaceContainerHighest = Color(0xFF282D37),
                outline = Color(0xFF44474F),
                outlineVariant = Color(0xFF2C3038)
            )
        } else {
            systemBaseScheme.copy(
                primary = activePalette.primaryColor,
                onPrimary = if (activePalette.id == "dynamic") systemBaseScheme.onPrimary else Color.White,
                primaryContainer = activePalette.primaryColor.copy(alpha = 0.14f),
                onPrimaryContainer = activePalette.primaryColor,
                surface = Color(0xFFF6F8FC),
                onSurface = Color(0xFF191C20),
                surfaceVariant = Color(0xFFE0E3EB),
                onSurfaceVariant = Color(0xFF44474E),
                surfaceContainerLowest = Color(0xFFFFFFFF),
                surfaceContainerLow = Color(0xFFF0F3F9),
                surfaceContainer = Color(0xFFE9EEF6),
                surfaceContainerHigh = Color(0xFFE2E8F0),
                surfaceContainerHighest = Color(0xFFDBE2EB),
                outline = Color(0xFF74777F),
                outlineVariant = Color(0xFFC4C7D0)
            )
        }
    }

    val view = androidx.compose.ui.platform.LocalView.current
    SideEffect {
        activity?.window?.let { window ->
            androidx.core.view.WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !isEffectiveDark
                isAppearanceLightNavigationBars = !isEffectiveDark
            }
        }
    }

    CompositionLocalProvider(
        androidx.compose.ui.platform.LocalContext provides localizedContext,
        androidx.compose.ui.platform.LocalConfiguration provides localizedContext.resources.configuration
    ) {
        MaterialTheme(colorScheme = onboardingColorScheme) {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                // ─── 1. TRUE EDGE-TO-EDGE EXPRESSIVE FLUID BACKGROUND ───
                // Background extends completely behind Status Bars & Navigation Bars
                Material3ExpressiveFluidBackground(
                    primaryTone = activePalette.primaryColor,
                    isDark = isEffectiveDark
                )

                // ─── 2. CONTENT SCAFFOLD WITH TRANSLUCENT / GLASS BAR ───
                Scaffold(
                    containerColor = Color.Transparent,
                    bottomBar = {
                        GoogleM3SetupBottomBar(
                            pagerState = pagerState,
                            pageCount = pageCount,
                            isDark = isEffectiveDark,
                            onNextClicked = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                if (pagerState.currentPage < pageCount - 1) {
                                    scope.launch {
                                        pagerState.animateScrollToPage(
                                            page = pagerState.currentPage + 1,
                                            animationSpec = spring(dampingRatio = 0.82f, stiffness = Spring.StiffnessMediumLow)
                                        )
                                    }
                                }
                            },
                            onBackClicked = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                if (pagerState.currentPage > 0) {
                                    scope.launch {
                                        pagerState.animateScrollToPage(
                                            page = pagerState.currentPage - 1,
                                            animationSpec = spring(dampingRatio = 0.82f, stiffness = Spring.StiffnessMediumLow)
                                        )
                                    }
                                }
                            },
                            onFinishClicked = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                prefs.edit {
                                    putBoolean("has_seen_onboarding_1_8", true)
                                    putString("player_theme", selectedPlayerTheme)
                                    putString("theme_mode", selectedThemeMode)
                                    putString("selected_color_palette", selectedPaletteId)
                                    putString("app_language", currentLanguage)
                                }
                                if (currentLanguage == "system") {
                                    AppCompatDelegate.setApplicationLocales(LocaleListCompat.getEmptyLocaleList())
                                } else {
                                    AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(currentLanguage))
                                }
                                applySelectedFont(activeTypography)
                                onComplete()
                            }
                        )
                    }
                ) { paddingValues ->
                val currentStep = pagerState.currentPage
                val showSharedPlayer = currentStep in 2..4 || currentStep == 6
                val activeCameraFocus = when (currentStep) {
                    2 -> RealPlayerCameraFocus.FULL_OVERVIEW
                    3 -> RealPlayerCameraFocus.FULL_OVERVIEW
                    4 -> RealPlayerCameraFocus.FULL_OVERVIEW
                    6 -> RealPlayerCameraFocus.FULL_OVERVIEW
                    else -> RealPlayerCameraFocus.FULL_OVERVIEW
                }

                val configuration = androidx.compose.ui.platform.LocalConfiguration.current
                val screenWidthDp = configuration.screenWidthDp.dp
                val screenHeightDp = configuration.screenHeightDp.dp
                val isTablet = screenWidthDp >= 600.dp && screenWidthDp > screenHeightDp * 0.75f

                if (isTablet) {
                    // ─── TABLET / LARGE SCREEN 2-COLUMN SPLIT LAYOUT ───
                    if (showSharedPlayer) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .statusBarsPadding()
                                .padding(bottom = paddingValues.calculateBottomPadding())
                                .padding(horizontal = 24.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(28.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // LEFT COLUMN: Floating Player Preview (Zero Margins, Responsive Morphing)
                            Box(
                                modifier = Modifier
                                    .weight(0.44f)
                                    .fillMaxHeight(),
                                contentAlignment = Alignment.Center
                            ) {
                                RealOriginalFullPlayerHost(
                                    viewModel = playerViewModel,
                                    accentColor = activePalette.primaryColor,
                                    cameraFocus = activeCameraFocus,
                                    selectedPlayerTheme = selectedPlayerTheme,
                                    isTabletLayout = true
                                )
                            }

                            // RIGHT COLUMN: Content Pager (Steps 2, 3, 4, 6)
                            Box(
                                modifier = Modifier
                                    .weight(0.56f)
                                    .fillMaxHeight()
                            ) {
                                HorizontalPager(
                                    state = pagerState,
                                    userScrollEnabled = false,
                                    modifier = Modifier.fillMaxSize()
                                ) { page ->
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(horizontal = 8.dp)
                                    ) {
                                        when (page) {
                                            0 -> GoogleM3ExpressiveWelcomeStep(
                                                currentLanguage = currentLanguage,
                                                onLanguageSelected = { switchLanguage(it) },
                                                bgmVolume = bgmVolume,
                                                isBgmMuted = isBgmMuted,
                                                onBgmVolumeChange = {
                                                    bgmVolume = it
                                                    if (it > 0f) isBgmMuted = false
                                                },
                                                onToggleBgmMute = { isBgmMuted = !isBgmMuted },
                                                onStartClicked = {
                                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                    scope.launch {
                                                        pagerState.animateScrollToPage(
                                                            page = 1,
                                                            animationSpec = spring(dampingRatio = 0.82f, stiffness = Spring.StiffnessMediumLow)
                                                        )
                                                    }
                                                }
                                            )
                                            1 -> GoogleM3AccountStep(
                                                isLoggedIn = isLoggedIn,
                                                userName = userName,
                                                userProfileUrl = userProfileUrl,
                                                onSignInClick = {
                                                    if (onNavigateToSignIn != null) {
                                                        onNavigateToSignIn()
                                                    }
                                                },
                                                onContinue = {
                                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                    scope.launch {
                                                        pagerState.animateScrollToPage(
                                                            page = 2,
                                                            animationSpec = spring(dampingRatio = 0.82f, stiffness = Spring.StiffnessMediumLow)
                                                        )
                                                    }
                                                }
                                            )
                                            2 -> GoogleM3PlayerThemeStep(
                                                selectedTheme = selectedPlayerTheme,
                                                onThemeSelected = {
                                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                    selectedPlayerTheme = it
                                                    prefs.edit { putString("player_theme", it) }
                                                }
                                            )
                                            3 -> GoogleM3AppearanceStep(
                                                selectedThemeMode = selectedThemeMode,
                                                onThemeModeSelected = {
                                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                    selectedThemeMode = it
                                                }
                                            )
                                            4 -> GoogleM3TypographyStep(
                                                presets = typographyPresets,
                                                selectedPresetId = selectedTypographyPresetId,
                                                onPresetSelected = {
                                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                    selectedTypographyPresetId = it.id
                                                    applySelectedFont(it)
                                                }
                                            )
                                            5 -> GoogleM3PermissionsStep(
                                                audioGranted = audioGranted,
                                                notifGranted = notifGranted,
                                                micGranted = micGranted,
                                                onRequestAudio = { permissionLauncher.launch(arrayOf(audioPermission)) },
                                                onRequestNotif = { notifPermission?.let { permissionLauncher.launch(arrayOf(it)) } },
                                                onRequestMic = { permissionLauncher.launch(arrayOf(micPermission)) },
                                                onGrantAll = {
                                                    val list = mutableListOf(audioPermission, micPermission)
                                                    if (notifPermission != null) list.add(notifPermission)
                                                    permissionLauncher.launch(list.toTypedArray())
                                                }
                                            )
                                            6 -> GoogleM3FinishStep(
                                                selectedPlayerTheme = selectedPlayerTheme,
                                                selectedThemeMode = selectedThemeMode,
                                                activeTypography = activeTypography
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        // Tablet Centered Layout for Non-Player Steps (0, 1, 5)
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .statusBarsPadding()
                                .padding(bottom = paddingValues.calculateBottomPadding()),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .widthIn(max = 680.dp)
                                    .fillMaxHeight()
                            ) {
                                HorizontalPager(
                                    state = pagerState,
                                    userScrollEnabled = false,
                                    modifier = Modifier.fillMaxSize()
                                ) { page ->
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(horizontal = 24.dp)
                                    ) {
                                        when (page) {
                                            0 -> GoogleM3ExpressiveWelcomeStep(
                                                currentLanguage = currentLanguage,
                                                onLanguageSelected = { switchLanguage(it) },
                                                bgmVolume = bgmVolume,
                                                isBgmMuted = isBgmMuted,
                                                onBgmVolumeChange = {
                                                    bgmVolume = it
                                                    if (it > 0f) isBgmMuted = false
                                                },
                                                onToggleBgmMute = { isBgmMuted = !isBgmMuted },
                                                onStartClicked = {
                                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                    scope.launch {
                                                        pagerState.animateScrollToPage(
                                                            page = 1,
                                                            animationSpec = spring(dampingRatio = 0.82f, stiffness = Spring.StiffnessMediumLow)
                                                        )
                                                    }
                                                }
                                            )
                                            1 -> GoogleM3AccountStep(
                                                isLoggedIn = isLoggedIn,
                                                userName = userName,
                                                userProfileUrl = userProfileUrl,
                                                onSignInClick = {
                                                    if (onNavigateToSignIn != null) {
                                                        onNavigateToSignIn()
                                                    }
                                                },
                                                onContinue = {
                                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                    scope.launch {
                                                        pagerState.animateScrollToPage(
                                                            page = 2,
                                                            animationSpec = spring(dampingRatio = 0.82f, stiffness = Spring.StiffnessMediumLow)
                                                        )
                                                    }
                                                }
                                            )
                                            5 -> GoogleM3PermissionsStep(
                                                audioGranted = audioGranted,
                                                notifGranted = notifGranted,
                                                micGranted = micGranted,
                                                onRequestAudio = { permissionLauncher.launch(arrayOf(audioPermission)) },
                                                onRequestNotif = { notifPermission?.let { permissionLauncher.launch(arrayOf(it)) } },
                                                onRequestMic = { permissionLauncher.launch(arrayOf(micPermission)) },
                                                onGrantAll = {
                                                    val list = mutableListOf(audioPermission, micPermission)
                                                    if (notifPermission != null) list.add(notifPermission)
                                                    permissionLauncher.launch(list.toTypedArray())
                                                }
                                            )
                                            else -> {}
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // ─── PHONE 1-COLUMN PORTRAIT LAYOUT ───
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .statusBarsPadding()
                            .padding(bottom = paddingValues.calculateBottomPadding())
                    ) {
                        // 1. Morphing Shared Player Header (Steps 2, 3, 4, 6)
                        AnimatedVisibility(
                            visible = showSharedPlayer,
                            enter = fadeIn(tween(220)) + expandVertically(spring(dampingRatio = 0.82f, stiffness = Spring.StiffnessMediumLow)),
                            exit = fadeOut(tween(180)) + shrinkVertically(spring(dampingRatio = 0.82f, stiffness = Spring.StiffnessMediumLow))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                RealOriginalFullPlayerHost(
                                    viewModel = playerViewModel,
                                    accentColor = activePalette.primaryColor,
                                    cameraFocus = activeCameraFocus,
                                    selectedPlayerTheme = selectedPlayerTheme,
                                    isTabletLayout = false
                                )
                            }
                        }

                        // 2. Content Pager
                        HorizontalPager(
                            state = pagerState,
                            userScrollEnabled = false,
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) { page ->
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 14.dp)
                            ) {
                                when (page) {
                                    0 -> GoogleM3ExpressiveWelcomeStep(
                                        currentLanguage = currentLanguage,
                                        onLanguageSelected = { switchLanguage(it) },
                                        bgmVolume = bgmVolume,
                                        isBgmMuted = isBgmMuted,
                                        onBgmVolumeChange = {
                                            bgmVolume = it
                                            if (it > 0f) isBgmMuted = false
                                        },
                                        onToggleBgmMute = { isBgmMuted = !isBgmMuted },
                                        onStartClicked = {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            scope.launch {
                                                pagerState.animateScrollToPage(
                                                    page = 1,
                                                    animationSpec = spring(dampingRatio = 0.82f, stiffness = Spring.StiffnessMediumLow)
                                                )
                                            }
                                        }
                                    )
                                    1 -> GoogleM3AccountStep(
                                        isLoggedIn = isLoggedIn,
                                        userName = userName,
                                        userProfileUrl = userProfileUrl,
                                        onSignInClick = {
                                            if (onNavigateToSignIn != null) {
                                                onNavigateToSignIn()
                                            }
                                        },
                                        onContinue = {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            scope.launch {
                                                pagerState.animateScrollToPage(
                                                    page = 2,
                                                    animationSpec = spring(dampingRatio = 0.82f, stiffness = Spring.StiffnessMediumLow)
                                                )
                                            }
                                        }
                                    )
                                    2 -> GoogleM3PlayerThemeStep(
                                        selectedTheme = selectedPlayerTheme,
                                        onThemeSelected = {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            selectedPlayerTheme = it
                                            prefs.edit { putString("player_theme", it) }
                                        }
                                    )
                                    3 -> GoogleM3AppearanceStep(
                                        selectedThemeMode = selectedThemeMode,
                                        onThemeModeSelected = {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            selectedThemeMode = it
                                        }
                                    )
                                    4 -> GoogleM3TypographyStep(
                                        presets = typographyPresets,
                                        selectedPresetId = selectedTypographyPresetId,
                                        onPresetSelected = {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            selectedTypographyPresetId = it.id
                                            applySelectedFont(it)
                                        }
                                    )
                                    5 -> GoogleM3PermissionsStep(
                                        audioGranted = audioGranted,
                                        notifGranted = notifGranted,
                                        micGranted = micGranted,
                                        onRequestAudio = { permissionLauncher.launch(arrayOf(audioPermission)) },
                                        onRequestNotif = { notifPermission?.let { permissionLauncher.launch(arrayOf(it)) } },
                                        onRequestMic = { permissionLauncher.launch(arrayOf(micPermission)) },
                                        onGrantAll = {
                                            val list = mutableListOf(audioPermission, micPermission)
                                            if (notifPermission != null) list.add(notifPermission)
                                            permissionLauncher.launch(list.toTypedArray())
                                        }
                                    )
                                    6 -> GoogleM3FinishStep(
                                        selectedPlayerTheme = selectedPlayerTheme,
                                        selectedThemeMode = selectedThemeMode,
                                        activeTypography = activeTypography
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
}

// ═══════════════════════════════════════════════════════════════════════════════
// 1. GOOGLE MATERIAL 3 EXPRESSIVE DYNAMIC MORPHING BACKGROUND
// ═══════════════════════════════════════════════════════════════════════════════

/**
 * Material 3 Expressive Dynamic Fluid Surface Background.
 * Renders sleek, high-contrast Material 3 Expressive dual-layer sweeping surface wave contours
 * that breathe and elevate with organic rhythm and subtle highlight rims.
 * Seamlessly adapts to Dark and Light modes with smooth color transitions.
 */
@Composable
fun Material3ExpressiveFluidBackground(
    primaryTone: Color,
    isDark: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "m3ExpressiveMorph")

    // Gentle breathing elevation for wave contours
    val waveBreathing by infiniteTransition.animateFloat(
        initialValue = -16f,
        targetValue = 16f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "waveBreathing"
    )

    val waveShiftX by infiniteTransition.animateFloat(
        initialValue = -24f,
        targetValue = 24f,
        animationSpec = infiniteRepeatable(
            animation = tween(8500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "waveShiftX"
    )

    // Animated color palette for fluid mode transitions
    val baseBgColor by animateColorAsState(
        targetValue = if (isDark) Color(0xFF0C0E14) else Color(0xFFF3F5FA),
        animationSpec = tween(400, easing = FastOutSlowInEasing),
        label = "baseBgColor"
    )
    val midWaveColor1 by animateColorAsState(
        targetValue = if (isDark) Color(0xFF2E343E) else Color(0xFFDDE3ED),
        animationSpec = tween(400, easing = FastOutSlowInEasing),
        label = "midWave1"
    )
    val midWaveColor2 by animateColorAsState(
        targetValue = if (isDark) Color(0xFF1E222A) else Color(0xFFE5EAF2),
        animationSpec = tween(400, easing = FastOutSlowInEasing),
        label = "midWave2"
    )
    val midWaveColor3 by animateColorAsState(
        targetValue = if (isDark) Color(0xFF14171D) else Color(0xFFEFF2F8),
        animationSpec = tween(400, easing = FastOutSlowInEasing),
        label = "midWave3"
    )
    val midWaveRimColor by animateColorAsState(
        targetValue = if (isDark) Color.White.copy(alpha = 0.22f) else Color(0xFF708090).copy(alpha = 0.20f),
        animationSpec = tween(400, easing = FastOutSlowInEasing),
        label = "midWaveRim"
    )

    val fgWaveColor1 by animateColorAsState(
        targetValue = if (isDark) Color(0xFF424A58) else Color(0xFFCDD6E4),
        animationSpec = tween(400, easing = FastOutSlowInEasing),
        label = "fgWave1"
    )
    val fgWaveColor2 by animateColorAsState(
        targetValue = if (isDark) Color(0xFF262A33) else Color(0xFFD8E1ED),
        animationSpec = tween(400, easing = FastOutSlowInEasing),
        label = "fgWave2"
    )
    val fgWaveColor3 by animateColorAsState(
        targetValue = if (isDark) Color(0xFF161920) else Color(0xFFE6ECF5),
        animationSpec = tween(400, easing = FastOutSlowInEasing),
        label = "fgWave3"
    )
    val fgWaveColor4 by animateColorAsState(
        targetValue = if (isDark) Color(0xFF0F1116) else Color(0xFFF0F4FA),
        animationSpec = tween(400, easing = FastOutSlowInEasing),
        label = "fgWave4"
    )
    val fgWaveRimColor by animateColorAsState(
        targetValue = if (isDark) Color.White.copy(alpha = 0.38f) else Color(0xFF5A6A80).copy(alpha = 0.28f),
        animationSpec = tween(400, easing = FastOutSlowInEasing),
        label = "fgWaveRim"
    )

    val ambientGlowColor by animateColorAsState(
        targetValue = primaryTone.copy(alpha = if (isDark) 0.16f else 0.10f),
        animationSpec = tween(400, easing = FastOutSlowInEasing),
        label = "ambientGlow"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        // 1. Base Surface
        drawRect(color = baseBgColor)

        // Subtle Ambient Primary Glow at Top-Right
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(ambientGlowColor, Color.Transparent),
                center = androidx.compose.ui.geometry.Offset(width * 0.85f, height * 0.15f),
                radius = width * 0.70f
            )
        )

        // 2. Layer 1 (Mid-depth Wave): Sweeping smoothly across the middle-lower region
        val midWaveStartY = height * 0.44f - waveBreathing * 0.6f
        val midWavePeakX = width * 0.65f - waveShiftX
        val midWavePeakY = height * 0.40f + waveBreathing * 0.4f
        val midWaveEndY = height * 0.58f

        val midWavePath = Path().apply {
            moveTo(-20f, height + 20f)
            lineTo(-20f, midWaveStartY)
            cubicTo(
                width * 0.25f, midWaveStartY + height * 0.02f,
                midWavePeakX - width * 0.18f, midWavePeakY,
                midWavePeakX, midWavePeakY
            )
            cubicTo(
                midWavePeakX + width * 0.18f, midWavePeakY,
                width * 0.85f, midWaveEndY - height * 0.02f,
                width + 20f, midWaveEndY
            )
            lineTo(width + 20f, height + 20f)
            close()
        }

        drawPath(
            path = midWavePath,
            brush = Brush.verticalGradient(
                colors = listOf(midWaveColor1, midWaveColor2, midWaveColor3),
                startY = midWavePeakY,
                endY = height
            )
        )

        val midWaveRim = Path().apply {
            moveTo(-20f, midWaveStartY)
            cubicTo(
                width * 0.25f, midWaveStartY + height * 0.02f,
                midWavePeakX - width * 0.18f, midWavePeakY,
                midWavePeakX, midWavePeakY
            )
            cubicTo(
                midWavePeakX + width * 0.18f, midWavePeakY,
                width * 0.85f, midWaveEndY - height * 0.02f,
                width + 20f, midWaveEndY
            )
        }

        drawPath(
            path = midWaveRim,
            color = midWaveRimColor,
            style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round)
        )

        // 4. Layer 2 (Foreground Wave Dome): Sweeping from bottom-left to center-peak to bottom-right
        val fgWaveStartY = height * 0.58f + waveBreathing
        val fgWavePeakX = width * 0.35f + waveShiftX
        val fgWavePeakY = height * 0.50f + waveBreathing * 0.8f
        val fgWaveEndY = height * 0.66f - waveBreathing * 0.5f

        val fgWavePath = Path().apply {
            moveTo(-20f, height + 20f)
            lineTo(-20f, fgWaveStartY)
            cubicTo(
                width * 0.12f, fgWaveStartY - height * 0.04f,
                fgWavePeakX - width * 0.16f, fgWavePeakY,
                fgWavePeakX, fgWavePeakY
            )
            cubicTo(
                fgWavePeakX + width * 0.22f, fgWavePeakY,
                width * 0.82f, fgWaveEndY - height * 0.03f,
                width + 20f, fgWaveEndY
            )
            lineTo(width + 20f, height + 20f)
            close()
        }

        drawPath(
            path = fgWavePath,
            brush = Brush.verticalGradient(
                colors = listOf(fgWaveColor1, fgWaveColor2, fgWaveColor3, fgWaveColor4),
                startY = fgWavePeakY,
                endY = height
            )
        )

        val fgWaveRim = Path().apply {
            moveTo(-20f, fgWaveStartY)
            cubicTo(
                width * 0.12f, fgWaveStartY - height * 0.04f,
                fgWavePeakX - width * 0.16f, fgWavePeakY,
                fgWavePeakX, fgWavePeakY
            )
            cubicTo(
                fgWavePeakX + width * 0.22f, fgWavePeakY,
                width * 0.82f, fgWaveEndY - height * 0.03f,
                width + 20f, fgWaveEndY
            )
        }

        drawPath(
            path = fgWaveRim,
            color = fgWaveRimColor,
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// 2. EMBEDDED REAL ORIGINAL FULLPLAYER WITH M3 EXPRESSIVE CAMERA ZOOM & PAN
// ═══════════════════════════════════════════════════════════════════════════════

enum class RealPlayerCameraFocus {
    FULL_OVERVIEW,      // Full player overview (Theme step, Finish step)
    CONTROLS_AND_SEEK,  // Zoom onto seeker & playback controls (Color palette step)
    TYPOGRAPHY_HEADER,  // Deep cinematic zoom onto Track Title & Artist typography (Typography step)
    GENTLE_HERO         // Gentle hero scale
}

private data class PlayerMorphingLayoutConfig(
    val cardWidth: androidx.compose.ui.unit.Dp,
    val cardHeight: androidx.compose.ui.unit.Dp,
    val scale: Float,
    val offsetY: Float,
    val offsetX: Float,
    val cornerRadius: androidx.compose.ui.unit.Dp
)

@Composable
fun RealOriginalFullPlayerHost(
    viewModel: PlayerSharedViewModel,
    accentColor: Color,
    cameraFocus: RealPlayerCameraFocus,
    selectedPlayerTheme: String = "fluid",
    isTabletLayout: Boolean = false,
    modifier: Modifier = Modifier
) {
    val liveCurrentSong by viewModel.currentSong.collectAsStateWithLifecycle()
    val liveIsPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()
    val liveDuration by viewModel.duration.collectAsStateWithLifecycle()
    val liveIsProcessing by viewModel.isProcessing.collectAsStateWithLifecycle()
    val liveCurrentSongIndex by viewModel.currentSongIndex.collectAsStateWithLifecycle()
    val liveIsHapticsEnabled by viewModel.isHapticsEnabled.collectAsStateWithLifecycle()
    val liveMiniPlayerBlendColor by viewModel.miniPlayerBlendColor.collectAsStateWithLifecycle()
    val liveIsFavorite by viewModel.isCurrentSongFavorite.collectAsStateWithLifecycle()
    val liveIsDisliked by viewModel.isCurrentSongDisliked.collectAsStateWithLifecycle()
    val liveRepeatMode by viewModel.repeatMode.collectAsStateWithLifecycle()
    val liveIsShuffle by viewModel.isShuffleEnabled.collectAsStateWithLifecycle()
    val livePlayerBgColor by viewModel.playerBackgroundColor.collectAsStateWithLifecycle()
    val liveLikesCount by viewModel.likesCount.collectAsStateWithLifecycle()
    val liveIsDownloaded by viewModel.isCurrentSongDownloaded.collectAsStateWithLifecycle()
    val liveDownloadProgress by viewModel.currentSongDownloadProgress.collectAsStateWithLifecycle()
    val liveStreamingQuality by viewModel.streamingQuality.collectAsStateWithLifecycle()
    val livePlaybackFormat by viewModel.playbackFormat.collectAsStateWithLifecycle()
    val liveSongList by viewModel.songList.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val sampleSong = remember(context) {
        val rawUri = "android.resource://${context.packageName}/${R.raw.autumn_wind}".toUri()
        val artUri = "android.resource://${context.packageName}/${R.drawable.artwork_autumn_wind}"
        SongItem(
            id = -9999L,
            rawTitle = "Autumn Wind",
            rawArtist = "Dyalla",
            albumId = -1L,
            path = null,
            duration = 186000L,
            dateAdded = 0L
        ).apply {
            contentUri = rawUri
            thumbnailUrl = artUri
        }
    }

    val effectiveUiState = remember(
        liveCurrentSong, liveIsPlaying, liveDuration, liveIsProcessing, liveCurrentSongIndex,
        liveIsHapticsEnabled, liveMiniPlayerBlendColor, liveIsFavorite, liveIsDisliked, liveRepeatMode,
        liveIsShuffle, livePlayerBgColor, liveLikesCount, liveIsDownloaded, liveDownloadProgress,
        liveStreamingQuality, livePlaybackFormat, accentColor
    ) {
        val song = liveCurrentSong ?: sampleSong
        PlayerUiState(
            currentSong = song,
            isPlaying = if (liveCurrentSong != null) liveIsPlaying else true,
            duration = if (liveCurrentSong != null && liveDuration > 0) liveDuration else 225000,
            isProcessing = liveIsProcessing,
            currentSongIndex = liveCurrentSongIndex,
            isHapticsEnabled = liveIsHapticsEnabled,
            miniPlayerBlendColor = liveMiniPlayerBlendColor,
            isCurrentSongFavorite = liveIsFavorite,
            isCurrentSongDisliked = liveIsDisliked,
            repeatMode = liveRepeatMode,
            isShuffleEnabled = liveIsShuffle,
            playerBackgroundColor = if (liveCurrentSong != null) livePlayerBgColor else accentColor.toArgb(),
            likesCount = liveLikesCount,
            isCurrentSongDownloaded = liveIsDownloaded,
            currentSongDownloadProgress = liveDownloadProgress,
            streamingQuality = liveStreamingQuality,
            playbackFormat = livePlaybackFormat
        )
    }

    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp

    // Base virtual design dimensions of full-player content
    val basePlayerWidth = 360.dp
    val basePlayerHeight = 770.dp // High-fidelity standard mobile player aspect ratio

    // Calculate free-morphing target dimensions without margins
    val layoutConfig = when {
        isTabletLayout -> {
            val maxH = (screenHeight.value - 120f).coerceIn(460f, 720f).dp
            val maxW = (screenWidth.value * 0.44f - 32f).coerceIn(240f, 420f).dp
            val s = minOf(maxW.value / basePlayerWidth.value, maxH.value / basePlayerHeight.value).coerceIn(0.55f, 0.92f)
            val w = (basePlayerWidth.value * s).dp
            val h = (basePlayerHeight.value * s).dp
            PlayerMorphingLayoutConfig(
                cardWidth = w,
                cardHeight = h,
                scale = s,
                offsetY = 0f,
                offsetX = 0f,
                cornerRadius = 30.dp
            )
        }
        else -> { // Phone (1-column portrait)
            val h = (screenHeight.value * 0.38f).coerceIn(260f, 310f).dp
            val s = h.value / basePlayerHeight.value
            val w = (basePlayerWidth.value * s).dp
            PlayerMorphingLayoutConfig(
                cardWidth = w,
                cardHeight = h,
                scale = s,
                offsetY = 0f,
                offsetX = 0f,
                cornerRadius = 28.dp
            )
        }
    }

    val commonSpringSpec = spring<Float>(
        dampingRatio = 0.82f,
        stiffness = Spring.StiffnessMediumLow
    )

    val animatedCardWidth by animateDpAsState(
        targetValue = layoutConfig.cardWidth,
        animationSpec = spring(dampingRatio = 0.82f, stiffness = Spring.StiffnessMediumLow),
        label = "morphingCardWidth"
    )
    val animatedCardHeight by animateDpAsState(
        targetValue = layoutConfig.cardHeight,
        animationSpec = spring(dampingRatio = 0.82f, stiffness = Spring.StiffnessMediumLow),
        label = "morphingCardHeight"
    )
    val animatedCornerRadius by animateDpAsState(
        targetValue = layoutConfig.cornerRadius,
        animationSpec = spring(dampingRatio = 0.82f, stiffness = Spring.StiffnessMediumLow),
        label = "morphingCorner"
    )

    val animScale = remember { Animatable(layoutConfig.scale) }
    val animOffsetY = remember { Animatable(layoutConfig.offsetY) }
    val animOffsetX = remember { Animatable(layoutConfig.offsetX) }
    var isInitialized by remember { mutableStateOf(false) }

    LaunchedEffect(cameraFocus, isTabletLayout) {
        if (!isInitialized) {
            isInitialized = true
            animScale.snapTo(layoutConfig.scale)
            animOffsetY.snapTo(layoutConfig.offsetY)
            animOffsetX.snapTo(layoutConfig.offsetX)
        } else {
            kotlinx.coroutines.coroutineScope {
                launch { animScale.animateTo(layoutConfig.scale, commonSpringSpec) }
                launch { animOffsetY.animateTo(layoutConfig.offsetY, commonSpringSpec) }
                launch { animOffsetX.animateTo(layoutConfig.offsetX, commonSpringSpec) }
            }
        }
    }

    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(animatedCornerRadius),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.95f)),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.40f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            modifier = Modifier
                .size(width = animatedCardWidth, height = animatedCardHeight)
                .clip(RoundedCornerShape(animatedCornerRadius))
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                val albumArtSize = minOf(basePlayerWidth * 0.88f, basePlayerHeight * 0.44f).coerceIn(200.dp, 360.dp)
                val controlsAreaHeightDp = 400.dp
                val availableTopSpaceDp = basePlayerHeight - controlsAreaHeightDp
                val squareTopOffsetDp = ((availableTopSpaceDp - albumArtSize) / 2f).coerceAtLeast(16.dp)

                val previewConfiguration = remember(configuration) {
                    android.content.res.Configuration(configuration).apply {
                        screenWidthDp = basePlayerWidth.value.toInt()
                        screenHeightDp = basePlayerHeight.value.toInt()
                        orientation = android.content.res.Configuration.ORIENTATION_PORTRAIT
                    }
                }

                Box(
                    modifier = Modifier
                        .requiredSize(width = basePlayerWidth, height = basePlayerHeight)
                        .graphicsLayer {
                            scaleX = animScale.value
                            scaleY = animScale.value
                            translationY = animOffsetY.value.dp.toPx()
                            translationX = animOffsetX.value.dp.toPx()
                            transformOrigin = TransformOrigin(0.5f, 0.5f)
                        }
                ) {
                    // 1. Full Player Base (Background, Sliders, Controls, Chips)
                    androidx.compose.runtime.CompositionLocalProvider(
                        androidx.compose.ui.platform.LocalConfiguration provides previewConfiguration
                    ) {
                        FullPlayer(
                            viewModel = viewModel,
                            uiState = effectiveUiState,
                            songList = liveSongList.ifEmpty { listOf(sampleSong) },
                            accentColor = accentColor,
                            isLyricsModeEnabled = false,
                            syncedLyrics = null,
                            plainLyrics = null,
                            isLyricsLoading = false,
                            lyricsError = null,
                            currentPositionProvider = { (225000 * 0.42f).toInt() },
                            onCollapse = {},
                            onPlayPauseClick = {},
                            onPreviousClick = {},
                            onNextClick = {},
                            onHapticChipClick = {},
                            onLyricsModeChanged = {},
                            onFetchLyrics = {},
                            onRetryLyrics = {},
                            onSeekTo = {},
                            onFavoriteClick = {},
                            onDislikeClick = {},
                            onSaveClick = {},
                            providerResults = emptyMap(),
                            selectedProvider = null,
                            onProviderSelected = {},
                            isAutoplayEnabled = false,
                            onAutoplayToggle = {},
                            videoAspectRatio = 1.77f,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    // 2. Real Album Artwork Layer (Renders ArtworkPager with cover art)
                    // In Immersion mode, AppleMusicBackground inside FullPlayer already renders the full-bleed background artwork.
                    // Only render the floating ArtworkPager for non-immersion themes so there is never duplicate artwork!
                    if (selectedPlayerTheme != "immersion") {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .offset(y = squareTopOffsetDp)
                                .size(albumArtSize)
                                .then(
                                    if (selectedPlayerTheme == "vinyl") Modifier
                                    else Modifier.clip(RoundedCornerShape(22.dp))
                                )
                        ) {
                            ArtworkPager(
                                viewModel = viewModel,
                                currentSong = effectiveUiState.currentSong ?: sampleSong,
                                songList = listOf(effectiveUiState.currentSong ?: sampleSong),
                                currentSongIndex = 0,
                                context = context,
                                userScrollEnabled = false,
                                allowCanvas = false,
                                showTonearm = true,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// 3. GOOGLE MATERIAL 3 EXPRESSIVE WELCOME STEP
// ═══════════════════════════════════════════════════════════════════════════════

/**
 * Renders the actual real application icon from PackageManager with original colors and high resolution.
 */
@Composable
fun AppIconImage(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val imageBitmap: androidx.compose.ui.graphics.ImageBitmap = remember(context) {
        val appIcon = context.packageManager.getApplicationIcon(context.packageName)
        val width = if (appIcon.intrinsicWidth > 0) appIcon.intrinsicWidth else 192
        val height = if (appIcon.intrinsicHeight > 0) appIcon.intrinsicHeight else 192
        val bmp = android.graphics.Bitmap.createBitmap(width, height, android.graphics.Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bmp)
        appIcon.setBounds(0, 0, canvas.width, canvas.height)
        appIcon.draw(canvas)
        bmp.asImageBitmap()
    }

    Image(
        bitmap = imageBitmap,
        contentDescription = "OverDrive Icon",
        modifier = modifier
    )
}

/**
 * Google Material 3 Expressive Welcome Step.
 * Clean, balanced Google-standard onboarding UI with expressive typography,
 * integrated M3 language segmented control, and expressive primary action button.
 */
@Composable
fun GoogleM3ExpressiveWelcomeStep(
    currentLanguage: String,
    onLanguageSelected: (String) -> Unit,
    bgmVolume: Float,
    isBgmMuted: Boolean,
    onBgmVolumeChange: (Float) -> Unit,
    onToggleBgmMute: () -> Unit,
    onStartClicked: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 8.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Language Segmented Pill at the Top
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
            modifier = Modifier.padding(top = 4.dp)
        ) {
            Row(
                modifier = Modifier.padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Language,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .padding(start = 8.dp, end = 4.dp)
                        .size(18.dp)
                )

                listOf(
                    Pair("system", stringResource(R.string.onboarding_lang_system)),
                    Pair("ja", stringResource(R.string.onboarding_lang_ja)),
                    Pair("en", stringResource(R.string.onboarding_lang_en))
                ).forEach { (code, label) ->
                    val isSelected = currentLanguage == code
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { onLanguageSelected(code) }
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }

        // 2. Center Branding & Introduction
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 12.dp)
        ) {
            // Original OverDrive App Launcher Icon with Original Colors & Elevation
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color.Transparent,
                shadowElevation = 8.dp,
                modifier = Modifier
                    .size(88.dp)
                    .clip(RoundedCornerShape(24.dp))
            ) {
                AppIconImage(
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.onboarding_welcome_title),
                style = MaterialTheme.typography.displaySmall.copy(
                    fontFamily = com.codetrio.overdrive.ui.theme.GoogleSansFlex,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = stringResource(R.string.onboarding_welcome_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 12.dp),
                lineHeight = 24.sp
            )
        }

        // 3. Bottom: BGM Volume Control & Primary "Get Started" Action Button
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Material 3 Expressive BGM Volume Slider Card
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.90f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.40f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onToggleBgmMute() }
                                .padding(4.dp)
                        ) {
                            val volumeIcon = when {
                                isBgmMuted || bgmVolume <= 0f -> Icons.AutoMirrored.Filled.VolumeOff
                                bgmVolume < 0.35f -> Icons.AutoMirrored.Filled.VolumeMute
                                bgmVolume < 0.7f -> Icons.AutoMirrored.Filled.VolumeDown
                                else -> Icons.AutoMirrored.Filled.VolumeUp
                            }
                            Icon(
                                imageVector = volumeIcon,
                                contentDescription = "BGM Volume",
                                tint = if (isBgmMuted || bgmVolume <= 0f) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = stringResource(R.string.onboarding_bgm_volume),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Text(
                            text = if (isBgmMuted || bgmVolume <= 0f) "OFF" else "${(bgmVolume * 100).toInt()}%",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isBgmMuted || bgmVolume <= 0f) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Slider(
                        value = if (isBgmMuted) 0f else bgmVolume,
                        onValueChange = onBgmVolumeChange,
                        valueRange = 0f..1f,
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary,
                            inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(34.dp)
                    )
                }
            }

            // Primary "Get Started" Action Button
            Button(
                onClick = onStartClicked,
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    text = stringResource(R.string.onboarding_welcome_get_started),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(10.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// 4. GOOGLE MATERIAL 3 EXPRESSIVE STEPS (1 - 6)
// ═══════════════════════════════════════════════════════════════════════════════

/**
 * Step 1: Player Theme Selection Step (Google M3 Expressive Style)
 */
@Composable
fun GoogleM3PlayerThemeStep(
    selectedTheme: String,
    onThemeSelected: (String) -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(top = 4.dp, bottom = 12.dp)
    ) {
        Text(
            text = stringResource(R.string.onboarding_theme_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = stringResource(R.string.onboarding_theme_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(14.dp))

        val themeOptions = listOf(
            Triple("fluid", stringResource(R.string.onboarding_theme_fluid), stringResource(R.string.onboarding_theme_fluid_desc)),
            Triple("immersion", stringResource(R.string.onboarding_theme_immersion), stringResource(R.string.onboarding_theme_immersion_desc)),
            Triple("mesh", stringResource(R.string.onboarding_theme_mesh), stringResource(R.string.onboarding_theme_mesh_desc)),
            Triple("vinyl", stringResource(R.string.onboarding_theme_vinyl), stringResource(R.string.onboarding_theme_vinyl_desc))
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            themeOptions.forEach { (id, title, desc) ->
                val isSelected = selectedTheme == id
                Card(
                    onClick = { onThemeSelected(id) },
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceContainerHigh
                    ),
                    border = BorderStroke(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHighest,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                if (isSelected) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = desc,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Step 3: Appearance & Theme Mode Step (System, Dark, Light)
 */
@Composable
fun GoogleM3AppearanceStep(
    selectedThemeMode: String,
    onThemeModeSelected: (String) -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(top = 4.dp, bottom = 12.dp)
    ) {
        Text(
            text = stringResource(R.string.onboarding_color_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = stringResource(R.string.onboarding_color_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Vertical List of 3 Expressive Theme Mode Cards
        val themeOptions = listOf(
            Triple("system", Pair(stringResource(R.string.onboarding_mode_system), stringResource(R.string.onboarding_mode_system_desc)), Icons.Default.Settings),
            Triple("dark", Pair(stringResource(R.string.onboarding_mode_dark), stringResource(R.string.onboarding_mode_dark_desc)), Icons.Default.DarkMode),
            Triple("light", Pair(stringResource(R.string.onboarding_mode_light), stringResource(R.string.onboarding_mode_light_desc)), Icons.Default.LightMode)
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            themeOptions.forEach { (mode, textPair, icon) ->
                val (label, description) = textPair
                val isSelected = selectedThemeMode == mode

                Card(
                    onClick = { onThemeModeSelected(mode) },
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) {
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                        } else {
                            MaterialTheme.colorScheme.surfaceContainerHigh
                        }
                    ),
                    border = BorderStroke(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.40f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHighest,
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = label,
                                    tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = description,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (isSelected) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(16.dp)
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

/**
 * Step 3: Typography Presets Step (Deep Typography Camera Zoom)
 */
@Composable
fun GoogleM3TypographyStep(
    presets: List<TypographyPreset>,
    selectedPresetId: String,
    onPresetSelected: (TypographyPreset) -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(top = 4.dp, bottom = 12.dp)
    ) {
        Text(
            text = stringResource(R.string.onboarding_typography_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = stringResource(R.string.onboarding_typography_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = stringResource(R.string.onboarding_typography_presets_header),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(12.dp))

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            presets.forEach { preset ->
                val isSelected = selectedPresetId == preset.id
                Card(
                    onClick = { onPresetSelected(preset) },
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceContainerHigh
                    ),
                    border = BorderStroke(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHighest,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "Aa",
                                    fontWeight = if (preset.weight >= 700f) FontWeight.Black else FontWeight.Normal,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 18.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(preset.titleResId),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = stringResource(preset.descResId),
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Active",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Step 4: Permissions Step (Google M3 List Style)
 */
@Composable
fun GoogleM3PermissionsStep(
    audioGranted: Boolean,
    notifGranted: Boolean,
    micGranted: Boolean,
    onRequestAudio: () -> Unit,
    onRequestNotif: () -> Unit,
    onRequestMic: () -> Unit,
    onGrantAll: () -> Unit
) {
    val allGranted = audioGranted && notifGranted && micGranted

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = stringResource(R.string.onboarding_permissions_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.onboarding_permissions_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                GooglePermissionItem(
                    title = stringResource(R.string.onboarding_permission_audio_title),
                    desc = stringResource(R.string.onboarding_permission_audio_desc),
                    icon = Icons.Default.Audiotrack,
                    isGranted = audioGranted,
                    onGrant = onRequestAudio
                )

                GooglePermissionItem(
                    title = stringResource(R.string.onboarding_permission_notif_title),
                    desc = stringResource(R.string.onboarding_permission_notif_desc),
                    icon = Icons.Default.Notifications,
                    isGranted = notifGranted,
                    onGrant = onRequestNotif
                )

                GooglePermissionItem(
                    title = stringResource(R.string.onboarding_permission_mic_title),
                    desc = stringResource(R.string.onboarding_permission_mic_desc),
                    icon = Icons.Default.Mic,
                    isGranted = micGranted,
                    onGrant = onRequestMic
                )
            }
        }

        if (!allGranted) {
            Button(
                onClick = onGrantAll,
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Icon(Icons.Default.Security, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.onboarding_grant_all),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        } else {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.onboarding_all_permissions_granted),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun GooglePermissionItem(
    title: String,
    desc: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isGranted: Boolean,
    onGrant: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isGranted) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f) else MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (isGranted) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = if (isGranted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isGranted) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = desc,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            if (isGranted) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Granted",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                FilledTonalButton(
                    onClick = onGrant,
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(text = stringResource(R.string.onboarding_permission_grant_btn), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

/**
 * Step 1: Dedicated YouTube Music & Google Sign-In Step (Google M3 Expressive Style)
 */
@Composable
fun GoogleM3AccountStep(
    isLoggedIn: Boolean,
    userName: String,
    userProfileUrl: String?,
    onSignInClick: () -> Unit,
    onContinue: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(top = 16.dp, bottom = 20.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Header Section
        Column(
            horizontalAlignment = Alignment.Start,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = stringResource(R.string.onboarding_account_title),
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontFamily = com.codetrio.overdrive.ui.theme.GoogleSansFlex,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.onboarding_account_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(20.dp))

            if (!isLoggedIn) {
                // ─── UNCONNECTED STATE: Open, Frameless Expressive Layout ───
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // YouTube Music Iconic Glowing Badge
                    Surface(
                        shape = RoundedCornerShape(26.dp),
                        color = Color(0xFFFF0000),
                        shadowElevation = 8.dp,
                        modifier = Modifier.size(72.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_youtube_music),
                                contentDescription = "YouTube Music",
                                modifier = Modifier.size(42.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "YouTube Music",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontFamily = com.codetrio.overdrive.ui.theme.GoogleSansFlex,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.3).sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Benefit items list (Frameless sleek items)
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        AccountBenefitItem(
                            icon = Icons.Default.Favorite,
                            iconTint = Color(0xFFFF5252),
                            title = stringResource(R.string.onboarding_account_benefit_likes_title),
                            description = stringResource(R.string.onboarding_account_benefit_likes_desc)
                        )
                        AccountBenefitItem(
                            icon = Icons.Default.QueueMusic,
                            iconTint = Color(0xFF4285F4),
                            title = stringResource(R.string.onboarding_account_benefit_playlists_title),
                            description = stringResource(R.string.onboarding_account_benefit_playlists_desc)
                        )
                        AccountBenefitItem(
                            icon = Icons.Default.AutoAwesome,
                            iconTint = Color(0xFFFBBC04),
                            title = stringResource(R.string.onboarding_account_benefit_recs_title),
                            description = stringResource(R.string.onboarding_account_benefit_recs_desc)
                        )
                    }
                }
            } else {
                // ─── CONNECTED STATE: Open, Frameless Personalized Greeting ───
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // User Profile Picture with Multi-Layer Google Ring
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(104.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            border = BorderStroke(3.5.dp, MaterialTheme.colorScheme.primary),
                            shadowElevation = 8.dp,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            if (userProfileUrl != null) {
                                coil.compose.AsyncImage(
                                    model = userProfileUrl,
                                    contentDescription = "User Avatar",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                )
                            } else {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.size(52.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Personalized Welcome Greeting
                    Text(
                        text = stringResource(R.string.onboarding_account_welcome_user, userName),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontFamily = com.codetrio.overdrive.ui.theme.GoogleSansFlex,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.5).sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Connected Status Pill
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFF1E3A2F),
                        border = BorderStroke(1.dp, Color(0xFF34A853).copy(alpha = 0.5f)),
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF34A853),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.onboarding_account_connected),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF81C995)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = stringResource(R.string.onboarding_account_ready_desc),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp),
                        lineHeight = 24.sp
                    )
                }
            }
        }

        // 2. Action Buttons Footer (Displayed only when not logged in; bottom bar handles navigation when logged in)
        if (!isLoggedIn) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = onSignInClick,
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_youtube_music),
                        contentDescription = "YouTube Music",
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = stringResource(R.string.onboarding_sign_in_ytm),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                OutlinedButton(
                    onClick = onContinue,
                    shape = RoundedCornerShape(28.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text(
                        text = stringResource(R.string.onboarding_continue_guest),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Helper row component for highlighting YouTube Music account benefits.
 */
@Composable
private fun AccountBenefitItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    title: String,
    description: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = iconTint.copy(alpha = 0.14f),
            modifier = Modifier.size(38.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Step 6: Finish Step (Ready to Play)
 */
@Composable
fun GoogleM3FinishStep(
    selectedPlayerTheme: String,
    selectedThemeMode: String,
    activeTypography: TypographyPreset
) {
    val scrollState = rememberScrollState()
    val themeModeLabel = when (selectedThemeMode) {
        "dark" -> stringResource(R.string.onboarding_mode_dark)
        "light" -> stringResource(R.string.onboarding_mode_light)
        else -> stringResource(R.string.onboarding_mode_system)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(top = 4.dp, bottom = 12.dp)
    ) {
        Text(
            text = stringResource(R.string.onboarding_finish_title),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = stringResource(R.string.onboarding_finish_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(14.dp))

        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = stringResource(R.string.onboarding_summary_theme), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                    Text(text = selectedPlayerTheme.uppercase(), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = stringResource(R.string.onboarding_summary_color), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                    Text(text = themeModeLabel, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = stringResource(R.string.onboarding_summary_font), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                    Text(text = stringResource(activeTypography.titleResId), color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// 5. GOOGLE M3 SETUP BOTTOM BAR
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun GoogleM3SetupBottomBar(
    pagerState: PagerState,
    pageCount: Int,
    isDark: Boolean,
    onNextClicked: () -> Unit,
    onBackClicked: () -> Unit,
    onFinishClicked: () -> Unit
) {
    val isLastPage = pagerState.currentPage == pageCount - 1
    val isFirstPage = pagerState.currentPage == 0

    if (isFirstPage) {
        return
    }

    // Dynamic gradient scrim behind bottom navigation for seamless readability & Edge-to-Edge immersion
    val bottomBarBg = if (isDark) {
        Brush.verticalGradient(
            colors = listOf(Color.Transparent, Color(0x990C0E14), Color(0xDD0C0E14))
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(Color.Transparent, Color(0x99F3F5FA), Color(0xDDF3F5FA))
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(bottomBarBg)
            .navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            FilledTonalIconButton(
                onClick = onBackClicked,
                colors = androidx.compose.material3.IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            // Expressive Morphing Dots Indicator
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(pageCount) { idx ->
                    val isCurrent = pagerState.currentPage == idx
                    val targetWidth by animateFloatAsState(
                        targetValue = if (isCurrent) 24f else 8f,
                        animationSpec = spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessMediumLow),
                        label = "dotWidth"
                    )
                    val dotColor = if (isCurrent) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        if (isDark) {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.40f)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .height(8.dp)
                            .width(targetWidth.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(dotColor)
                    )
                }
            }

            if (!isLastPage) {
                Button(
                    onClick = onNextClicked,
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.height(48.dp)
                ) {
                    Text(
                        text = stringResource(R.string.onboarding_btn_next),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            } else {
                Button(
                    onClick = onFinishClicked,
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.height(48.dp)
                ) {
                    Text(
                        text = stringResource(R.string.onboarding_start_listening),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}
