package com.codetrio.overdrive.ui.player

import android.content.Context
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.media3.ui.AspectRatioFrameLayout
import com.codetrio.overdrive.model.SongItem
import com.codetrio.overdrive.ui.player.canvas.CanvasArtwork
import com.codetrio.overdrive.ui.player.canvas.CanvasArtworkPlayer

/**
 * Full-screen Now Playing background engine.
 * Supports:
 * 1. "Blurred" Mode: RenderScript hardware blurred cover artwork + KenBurns 12s pan/zoom + low-opacity palette gradient.
 * 2. "Solid" Mode: Clean high-contrast spring-animated palette gradient.
 * 3. Canvas Artwork Video layer integration.
 */
@Composable
fun AppleMusicBackground(
    song: SongItem?,
    modifier: Modifier = Modifier,
    canvasArtwork: CanvasArtwork? = null,
    isPlaying: Boolean = true,
    isLyricsModeEnabled: Boolean = false,
    isMvMode: Boolean = false,
    accentColor: Color = Color(0xFF6200EE),
    backgroundColor: Color = Color.Transparent,
    content: @Composable () -> Unit = {}
) {
    val context = LocalContext.current

    // ── SharedPreferences Settings Observers ───────────────────────────────────
    val bgMode = remember {
        val prefs = context.getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        mutableStateOf(prefs.getString("now_playing_background_v2", "Blurred") ?: "Blurred")
    }
    val isBlurred = remember(bgMode.value) { bgMode.value == "Blurred" }

    val kenBurnsEnabled = remember {
        val prefs = context.getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        mutableStateOf(prefs.getBoolean("now_playing_background_effect", false))
    }

    val showAnimatedArt = remember {
        val prefs = context.getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        mutableStateOf(prefs.getBoolean("show_animated_art", true))
    }

    val playerTheme = remember {
        val prefs = context.getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        mutableStateOf(prefs.getString("player_theme", "fluid") ?: "fluid")
    }
    val currentTheme = playerTheme.value
    val isStatic = currentTheme == "static"
    val isImmersion = currentTheme == "immersion"
    val isImmersionV2 = currentTheme == "immersion-v2"
    val isMesh = currentTheme == "mesh"
    val isMeshV2 = currentTheme == "mesh-v2"
    val isFluidV2 = currentTheme == "fluid-v2"
    val isVinyl = currentTheme == "vinyl"
    val isImmersionLike = isImmersion || isImmersionV2

    DisposableEffect(Unit) {
        val prefs = context.getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        val listener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { p, key ->
            when (key) {
                "now_playing_background_v2" -> bgMode.value = p.getString(key, "Blurred") ?: "Blurred"
                "now_playing_background_effect" -> kenBurnsEnabled.value = p.getBoolean(key, false)
                "show_animated_art" -> showAnimatedArt.value = p.getBoolean(key, true)
                "player_theme" -> playerTheme.value = p.getString(key, "fluid") ?: "fluid"
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        onDispose {
            prefs.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }

    // ── Artwork URI ───────────────────────────────────────────────────────────────
    val rawUri     = song?.getAlbumArtUri()
    val videoId    = song?.videoId
    val artworkUrl = remember(song) {
        when {
            rawUri != null && rawUri.toString().isNotEmpty() ->
                SongItem.enhanceThumbnailUrl(rawUri.toString())
            !videoId.isNullOrEmpty() ->
                "https://img.youtube.com/vi/$videoId/maxresdefault.jpg"
            else -> null
        }
    }

    // ── Palette colors from global singleton ───────────────────────────────────────
    val vibrant      = PlayerPaletteState.vibrantColor.value
    val lightVibrant = PlayerPaletteState.lightVibrantColor.value
    val darkVibrant  = PlayerPaletteState.darkVibrantColor.value
    val muted        = PlayerPaletteState.mutedColor.value
    val darkMuted    = PlayerPaletteState.darkMutedColor.value
    val dominant     = PlayerPaletteState.dominantColor.value

    // Dedicated smart immersion color slots
    val topImmersion     = PlayerPaletteState.topImmersionColor.value
    val bottomImmersion  = PlayerPaletteState.bottomImmersionColor.value

    // ── Spring-animated palette color morphing ─────────────────────────────────────
    val animatedVibrant: Color = animateColorAsState(
        targetValue   = vibrant,
        animationSpec = spring(stiffness = 100f, dampingRatio = 0.8f),
        label         = "vibrant_spring"
    ).value
    val animatedLightVibrant: Color = animateColorAsState(
        targetValue   = lightVibrant,
        animationSpec = spring(stiffness = 100f, dampingRatio = 0.8f),
        label         = "light_vibrant_spring"
    ).value
    val animatedDarkVibrant: Color = animateColorAsState(
        targetValue   = darkVibrant,
        animationSpec = spring(stiffness = 100f, dampingRatio = 0.8f),
        label         = "dark_vibrant_spring"
    ).value
    val animatedMuted: Color = animateColorAsState(
        targetValue   = muted,
        animationSpec = spring(stiffness = 100f, dampingRatio = 0.8f),
        label         = "muted_spring"
    ).value
    val animatedDarkMuted: Color = animateColorAsState(
        targetValue   = darkMuted,
        animationSpec = spring(stiffness = 100f, dampingRatio = 0.8f),
        label         = "dark_muted_spring"
    ).value
    val animatedDominant: Color = animateColorAsState(
        targetValue   = dominant,
        animationSpec = spring(stiffness = 100f, dampingRatio = 0.8f),
        label         = "dominant_spring"
    ).value

    val animatedTopImmersion: Color = animateColorAsState(
        targetValue   = topImmersion,
        animationSpec = spring(stiffness = 100f, dampingRatio = 0.8f),
        label         = "top_immersion_spring"
    ).value
    val animatedBottomImmersion: Color = animateColorAsState(
        targetValue   = bottomImmersion,
        animationSpec = spring(stiffness = 100f, dampingRatio = 0.8f),
        label         = "bottom_immersion_spring"
    ).value

    val isDark = androidx.compose.material3.MaterialTheme.colorScheme.surface.luminance() < 0.5f

    // ── Canvas motion artwork ──────────────────────────────────────────────────────
    val hasCanvas = !isLyricsModeEnabled && showAnimatedArt.value && canvasArtwork != null
    val canvasUrl = remember(canvasArtwork) {
        canvasArtwork?.preferredVerticalAnimationUrl
            ?: canvasArtwork?.preferredAnimationUrl
            ?: canvasArtwork?.videoUrlVertical
            ?: canvasArtwork?.videoUrl
    }

    // ═════════════════════════════════════════════════════════════════════════════
    //  BACKGROUND COMPOSITION
    // ═════════════════════════════════════════════════════════════════════════════
    val defaultImmersionBg = animatedBottomImmersion
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(if (isImmersionLike) defaultImmersionBg else if (isVinyl) (if (isDark) Color(0xFF101114) else Color(0xFFFFFFFF)) else animatedDarkMuted.copy(alpha = 0.5f))
    ) {
        when {
            isStatic -> {
                SpatialWrapper {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(animatedDarkMuted.copy(alpha = 1f))
                    )
                }
            }
            isImmersion -> {
                SpatialWrapper {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colorStops = arrayOf(
                                        0.0f to animatedTopImmersion,
                                        0.38f to animatedTopImmersion,
                                        0.70f to animatedBottomImmersion,
                                        1.0f to animatedBottomImmersion
                                    )
                                )
                            )
                    )
                }
            }
            isImmersionV2 -> {
                SpatialWrapper {
                    com.codetrio.overdrive.ui.player.themes.ImmersionV2Background(
                        vibrant = animatedVibrant,
                        darkVibrant = animatedDarkVibrant,
                        darkMuted = animatedDarkMuted,
                        isPlaying = isPlaying,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            isMesh -> {
                SpatialWrapper {
                    DynamicMeshCanvasBackground(
                        vibrant = vibrant,
                        lightVibrant = lightVibrant,
                        darkVibrant = darkVibrant,
                        muted = muted,
                        darkMuted = darkMuted,
                        dominant = dominant,
                        isPlaying = isPlaying,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            isMeshV2 -> {
                SpatialWrapper {
                    com.codetrio.overdrive.ui.player.themes.MeshV2CanvasBackground(
                        vibrant = vibrant,
                        lightVibrant = lightVibrant,
                        darkVibrant = darkVibrant,
                        muted = muted,
                        darkMuted = darkMuted,
                        dominant = dominant,
                        isPlaying = isPlaying,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            isFluidV2 -> {
                SpatialWrapper {
                    com.codetrio.overdrive.ui.player.themes.FluidV2Background(
                        vibrant = animatedVibrant,
                        lightVibrant = animatedLightVibrant,
                        darkVibrant = animatedDarkVibrant,
                        muted = animatedMuted,
                        darkMuted = animatedDarkMuted,
                        isPlaying = isPlaying,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            isVinyl -> {
                SpatialWrapper {
                    com.codetrio.overdrive.ui.player.themes.VinylBackground(
                        vibrant = animatedVibrant,
                        lightVibrant = animatedLightVibrant,
                        darkVibrant = animatedDarkVibrant,
                        muted = animatedMuted,
                        darkMuted = animatedDarkMuted,
                        dominant = animatedDominant,
                        isPlaying = isPlaying,
                        isDark = isDark,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            else -> {
                // FLUID THEME (Classic) — BLURRED ARTWORK CANVAS + Dynamic KenBurns & Mesh Saturation
                SpatialWrapper {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .animatedFluidBackground(
                                backgroundColor = Color.Black,
                                vibrant = animatedVibrant,
                                darkVibrant = animatedDarkVibrant,
                                darkMuted = animatedDarkMuted,
                                isAnimated = isPlaying
                            )
                    ) {
                        if (!isMvMode) {
                            SpatialFloatingLight(
                                modifier = Modifier.fillMaxSize(),
                                album = { artworkUrl },
                                isPlaying = { isPlaying },
                                isLyricsPage = { isLyricsModeEnabled },
                                backgroundEffectEnabled = kenBurnsEnabled.value
                            )
                        }
                    }
                }

                // Dynamic Palette Gradient Overlay (Subtle blend)
                SpatialWrapper {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        animatedVibrant.copy(alpha = 0.15f),
                                        animatedDarkVibrant.copy(alpha = 0.10f),
                                        animatedDarkMuted.copy(alpha = 0.08f)
                                    )
                                )
                            )
                    )
                }
            }
        }

        // ── Canvas Video Overlay / Immersion Artwork Overlay ─────────────────────
        if (!isMvMode && ((hasCanvas && !canvasUrl.isNullOrBlank()) || (isImmersionLike && !isLyricsModeEnabled && !artworkUrl.isNullOrBlank()))) {
            Box(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = if (isImmersion && !hasCanvas) 20.dp else 0.dp)
                        .aspectRatio(if (isImmersion && !hasCanvas) 1f else (3f / 4f))
                        .align(Alignment.TopCenter)
                        .graphicsLayer {
                            compositingStrategy = CompositingStrategy.Offscreen
                        }
                        .drawWithContent {
                            drawContent()
                            val gradientBrush = if (isImmersion && !hasCanvas) {
                                Brush.verticalGradient(
                                    0.0f  to Color.Transparent,
                                    0.45f to Color.White,
                                    0.58f to Color.White,
                                    1.0f  to Color.Transparent
                                )
                            } else {
                                Brush.verticalGradient(
                                    0.0f  to Color.Transparent,
                                    0.15f to Color.White,
                                    0.50f to Color.White,
                                    1.0f  to Color.Transparent
                                )
                            }
                            drawRect(
                                brush = gradientBrush,
                                blendMode = BlendMode.DstIn
                            )
                        }
                ) {
                    if (hasCanvas && !canvasUrl.isNullOrBlank()) {
                        CanvasArtworkPlayer(
                            primaryUrl  = canvasUrl,
                            fallbackUrl = null,
                            isPlaying   = isPlaying,
                            resizeMode  = AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH,
                            modifier    = Modifier.fillMaxSize()
                        )
                    } else if (artworkUrl != null && !isMvMode) {
                        coil.compose.AsyncImage(
                            model = coil.request.ImageRequest.Builder(context)
                                .data(artworkUrl)
                                .size(coil.size.Size.ORIGINAL)
                                .precision(coil.size.Precision.EXACT)
                                .allowHardware(true)
                                .crossfade(200)
                                .build(),
                            contentDescription = "Immersion Artwork",
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }

        content()
    }
}
