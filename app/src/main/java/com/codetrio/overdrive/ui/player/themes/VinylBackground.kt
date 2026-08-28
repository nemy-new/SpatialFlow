package com.codetrio.overdrive.ui.player.themes

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/**
 * Gemini-Inspired Dynamic Chromatic Aurora Background for Vinyl Turntable Theme.
 *
 * Theme Adaptability:
 * - Dark Mode: Sleek, deep Dark Gray studio base (#181A20 ~ #090A0D) with luminous surging aurora.
 * - Light Mode: Crisp, pure White studio base (#FFFFFF ~ #ECEEF2) with vibrant surging watercolor aurora.
 *
 * Visual Features:
 * - 100% gap-free bottom surge with full floor saturation across the entire width.
 * - High-energy dynamic fluid undulation with multi-harmonic turbulence and organic surges.
 * - 2~3 distinct primary palette colors from the album artwork co-exist simultaneously and morph seamlessly.
 */
@Composable
fun VinylBackground(
    vibrant: Color,
    lightVibrant: Color,
    darkVibrant: Color,
    muted: Color,
    darkMuted: Color,
    dominant: Color,
    isPlaying: Boolean,
    isDark: Boolean = true,
    modifier: Modifier = Modifier
) {
    // ── 1. High-Energy Infinite Wave Kinetic Transitions (Smooth & Relaxed) ──
    val infiniteTransition = rememberInfiniteTransition(label = "gemini_aurora_engine")
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isPlaying) 9000 else 18000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_phase"
    )
    val harmonicPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isPlaying) 14000 else 26000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "harmonic_phase"
    )
    val colorCyclePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isPlaying) 26000 else 42000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "color_cycle_phase"
    )

    // ── 2. Palette Color Extraction with Chroma Boosting ──────────────
    val colorA = remember(vibrant, lightVibrant, dominant, isDark) {
        boostChroma(
            primary = vibrant,
            fallback1 = lightVibrant,
            fallback2 = dominant,
            defaultColor = Color(0xFF00E5FF), // Electric Cyan
            isDark = isDark
        )
    }
    val colorB = remember(lightVibrant, darkVibrant, dominant, isDark) {
        boostChroma(
            primary = lightVibrant,
            fallback1 = darkVibrant,
            fallback2 = dominant,
            defaultColor = Color(0xFF7C4DFF), // Deep Violet
            isDark = isDark
        )
    }
    val colorC = remember(darkVibrant, muted, dominant, isDark) {
        boostChroma(
            primary = darkVibrant,
            fallback1 = muted,
            fallback2 = dominant,
            defaultColor = Color(0xFF00E676), // Emerald Green
            isDark = isDark
        )
    }

    // Studio Base Canvas Palette (Dark vs Light)
    val studioTop = if (isDark) Color(0xFF181A20) else Color(0xFFFFFFFF)
    val studioMid = if (isDark) Color(0xFF111216) else Color(0xFFF7F8FA)
    val studioBottom = if (isDark) Color(0xFF090A0D) else Color(0xFFECEEF2)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(studioMid)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // ── A. Base Studio Canvas ──────────────────────────────────
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        studioTop,
                        studioMid,
                        studioBottom
                    ),
                    startY = 0f,
                    endY = h
                ),
                size = size
            )

            // ── B. Dynamic Chromatic Interpolation (3 Co-existing Colors) ─
            val tColor = (colorCyclePhase / (2 * PI).toFloat()) % 1f
            val plumeColor1 = interpolate3Colors(colorA, colorB, colorC, tColor)
            val plumeColor2 = interpolate3Colors(colorB, colorC, colorA, tColor)
            val plumeColor3 = interpolate3Colors(colorC, colorA, colorB, tColor)

            // ── C. Continuous Floor Wash (Gap-Free Foundation) ──────────
            // Full-width continuous floor emission ensuring zero gap at the bottom edge
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        plumeColor3.copy(alpha = if (isDark) 0.32f else 0.18f),
                        plumeColor1.copy(alpha = if (isDark) 0.60f else 0.38f),
                        plumeColor2.copy(alpha = if (isDark) 0.85f else 0.52f)
                    ),
                    startY = h * 0.55f,
                    endY = h
                ),
                size = size
            )

            val baseRadius = w * 0.72f

            // Plume Alphas (Luminous in Dark, Watercolor dispersion in Light)
            val a1 = if (isDark) 0.75f else 0.48f
            val a2 = if (isDark) 0.44f else 0.28f
            val a3 = if (isDark) 0.16f else 0.10f

            // ── Plume 1: Left Wing Dynamic Surge (100% seamless closed loop) ──
            val surge1 = abs(sin(wavePhase) + 0.35f * sin(harmonicPhase * 2f + 0.4f)).coerceIn(0f, 1.3f)
            val p1Center = Offset(
                x = w * (0.16f + 0.20f * cos(wavePhase)),
                y = h * (1.04f - 0.46f * surge1)
            )
            val p1Radius = baseRadius * (1.15f + 0.30f * sin(wavePhase + 0.5f))
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        plumeColor1.copy(alpha = a1),
                        plumeColor1.copy(alpha = a2),
                        plumeColor1.copy(alpha = a3),
                        Color.Transparent
                    ),
                    center = p1Center,
                    radius = p1Radius
                ),
                center = p1Center,
                radius = p1Radius
            )

            // ── Plume 2: Right Wing Dynamic Surge (100% seamless closed loop) ──
            val surge2 = abs(cos(wavePhase + 1.2f) + 0.35f * cos(harmonicPhase * 2f + 1.5f)).coerceIn(0f, 1.3f)
            val p2Center = Offset(
                x = w * (0.84f - 0.20f * sin(wavePhase)),
                y = h * (1.05f - 0.48f * surge2)
            )
            val p2Radius = baseRadius * (1.20f + 0.28f * cos(wavePhase * 2f))
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        plumeColor2.copy(alpha = a1 * 0.95f),
                        plumeColor2.copy(alpha = a2 * 0.95f),
                        plumeColor2.copy(alpha = a3 * 0.95f),
                        Color.Transparent
                    ),
                    center = p2Center,
                    radius = p2Radius
                ),
                center = p2Center,
                radius = p2Radius
            )

            // ── Plume 3: Deep Central Erupting Core (100% seamless closed loop) ──
            val surge3 = ((sin(wavePhase * 2f) + sin(harmonicPhase + 1.0f)) / 2f + 1f) / 2f
            val p3Center = Offset(
                x = w * (0.50f + 0.20f * sin(harmonicPhase + 2.0f)),
                y = h * (1.06f - 0.42f * surge3)
            )
            val p3Radius = baseRadius * (1.40f + 0.25f * sin(wavePhase + 3.1f))
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        plumeColor3.copy(alpha = a1),
                        plumeColor3.copy(alpha = a2 * 0.92f),
                        plumeColor3.copy(alpha = a3 * 0.88f),
                        Color.Transparent
                    ),
                    center = p3Center,
                    radius = p3Radius
                ),
                center = p3Center,
                radius = p3Radius
            )

            // ── Plume 4: Mid-Field Orbital Liquid Swirl (100% seamless closed loop) ──
            val swirlCenter = Offset(
                x = w * (0.50f + 0.30f * cos(wavePhase * 2f + 1.0f)),
                y = h * (0.82f - 0.24f * abs(sin(harmonicPhase + 0.8f)))
            )
            val swirlRadius = baseRadius * 0.95f
            val swirlColor = interpolate3Colors(colorA, colorC, colorB, (tColor + 0.33f) % 1f)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        swirlColor.copy(alpha = a1 * 0.80f),
                        swirlColor.copy(alpha = a2 * 0.70f),
                        swirlColor.copy(alpha = a3 * 0.50f),
                        Color.Transparent
                    ),
                    center = swirlCenter,
                    radius = swirlRadius
                ),
                center = swirlCenter,
                radius = swirlRadius
            )

            // ── Plume 5: Ethereal Floating Chromatic Crest (100% seamless closed loop) ──
            val crestCenter = Offset(
                x = w * (0.44f + 0.28f * sin(harmonicPhase * 2f)),
                y = h * (0.70f + 0.12f * cos(wavePhase * 2f))
            )
            val crestRadius = baseRadius * 0.85f
            val crestColor = interpolate3Colors(colorB, colorA, colorC, (tColor + 0.66f) % 1f)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        crestColor.copy(alpha = a1 * 0.65f),
                        crestColor.copy(alpha = a2 * 0.55f),
                        Color.Transparent
                    ),
                    center = crestCenter,
                    radius = crestRadius
                ),
                center = crestCenter,
                radius = crestRadius
            )

            // ── D. Smooth Upper Studio Base Dissolve ────────────────────
            // Gracefully dissolves the upper edge of the surging aurora into studio background
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        studioMid,
                        studioMid,
                        studioMid.copy(alpha = 0.92f),
                        studioMid.copy(alpha = 0.50f),
                        Color.Transparent
                    ),
                    startY = 0f,
                    endY = h * 0.62f
                ),
                size = size
            )
        }
    }
}

/**
 * Ensures extracted album palette colors have rich chroma and vibrant lightness
 * for vivid, expressive Gemini-style fluid rendering across Dark and Light modes.
 */
private fun boostChroma(
    primary: Color,
    fallback1: Color,
    fallback2: Color,
    defaultColor: Color,
    isDark: Boolean
): Color {
    val chosen = when {
        primary != Color.Transparent && primary != Color.Black && primary != Color.White -> primary
        fallback1 != Color.Transparent && fallback1 != Color.Black && fallback1 != Color.White -> fallback1
        fallback2 != Color.Transparent && fallback2 != Color.Black && fallback2 != Color.White -> fallback2
        else -> defaultColor
    }

    val hsl = FloatArray(3)
    ColorUtils.colorToHSL(chosen.toArgb(), hsl)
    // Boost saturation and clamp lightness to expressive range
    hsl[1] = (hsl[1] * 1.35f).coerceIn(0.70f, 1.0f)
    if (isDark) {
        hsl[2] = hsl[2].coerceIn(0.44f, 0.68f)
    } else {
        hsl[2] = hsl[2].coerceIn(0.50f, 0.72f)
    }
    return Color(ColorUtils.HSLToColor(hsl))
}

/**
 * Smoothly interpolates across 3 distinct chromatic points over cycle t in [0..1].
 */
private fun interpolate3Colors(c1: Color, c2: Color, c3: Color, t: Float): Color {
    val scaledT = (t % 1f) * 3f
    val segment = scaledT.toInt() % 3
    val fraction = scaledT - segment

    return when (segment) {
        0 -> lerpColor(c1, c2, fraction)
        1 -> lerpColor(c2, c3, fraction)
        else -> lerpColor(c3, c1, fraction)
    }
}

private fun lerpColor(start: Color, end: Color, fraction: Float): Color {
    val f = fraction.coerceIn(0f, 1f)
    return Color(
        red = start.red + (end.red - start.red) * f,
        green = start.green + (end.green - start.green) * f,
        blue = start.blue + (end.blue - start.blue) * f,
        alpha = start.alpha + (end.alpha - start.alpha) * f
    )
}
