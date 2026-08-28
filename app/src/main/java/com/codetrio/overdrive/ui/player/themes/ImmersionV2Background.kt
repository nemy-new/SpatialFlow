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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils

@Composable
fun ImmersionV2Background(
    vibrant: Color,
    darkVibrant: Color,
    darkMuted: Color,
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    val isLiteMode = com.codetrio.overdrive.util.rememberIsLiteMode().value
    val shouldAnimate = isPlaying && !isLiteMode

    val infiniteTransition = rememberInfiniteTransition(label = "immersion_v2_pulse")
    val ambientGlow by if (shouldAnimate) {
        infiniteTransition.animateFloat(
            initialValue = 0.85f,
            targetValue = 1.15f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 4000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "glow_pulse"
        )
    } else {
        remember { mutableFloatStateOf(1.0f) }
    }

    val safeTop = remember(vibrant) {
        val hsl = FloatArray(3)
        ColorUtils.colorToHSL(vibrant.toArgb(), hsl)
        hsl[2] = hsl[2].coerceIn(0.28f, 0.55f)
        Color(ColorUtils.HSLToColor(hsl))
    }

    val safeMid = remember(darkVibrant) {
        val hsl = FloatArray(3)
        ColorUtils.colorToHSL(darkVibrant.toArgb(), hsl)
        hsl[2] = hsl[2].coerceIn(0.20f, 0.42f)
        Color(ColorUtils.HSLToColor(hsl))
    }

    val safeBottom = remember(darkMuted) {
        val hsl = FloatArray(3)
        ColorUtils.colorToHSL(darkMuted.toArgb(), hsl)
        hsl[2] = hsl[2].coerceIn(0.12f, 0.28f)
        Color(ColorUtils.HSLToColor(hsl))
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        safeTop,
                        safeTop,
                        safeMid,
                        safeBottom,
                        safeBottom
                    )
                )
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val center = Offset(width / 2f, height * 0.38f)

            // Radiant Ambient Light Cone behind album art
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        safeTop.copy(alpha = 0.40f * ambientGlow),
                        Color.Transparent
                    ),
                    center = center,
                    radius = width * 0.85f * ambientGlow
                ),
                center = center,
                radius = width * 0.85f * ambientGlow
            )

            // Soft Bottom Vignette for supreme text readability
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.Black.copy(alpha = 0.35f),
                        Color.Black.copy(alpha = 0.70f)
                    ),
                    startY = height * 0.45f,
                    endY = height
                )
            )
        }
    }
}
