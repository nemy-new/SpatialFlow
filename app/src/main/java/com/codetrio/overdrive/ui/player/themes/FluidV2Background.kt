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
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun FluidV2Background(
    vibrant: Color,
    lightVibrant: Color,
    darkVibrant: Color,
    muted: Color,
    darkMuted: Color,
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    val isLiteMode = com.codetrio.overdrive.util.rememberIsLiteMode().value

    val infiniteTransition = rememberInfiniteTransition(label = "fluid_v2_motion")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when {
                    !isPlaying -> 60000 // Heavily throttled when paused to save battery
                    isLiteMode -> 16000 // Relaxed framerate on low-end
                    else -> 9000
                },
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "fluid_phase"
    )

    val c1 = if (vibrant != Color.Transparent && vibrant != Color.Black) vibrant else Color(0xFFFF4081)
    val c2 = if (lightVibrant != Color.Transparent && lightVibrant != Color.Black) lightVibrant else Color(0xFF7C4DFF)
    val c3 = if (darkVibrant != Color.Transparent && darkVibrant != Color.Black) darkVibrant else Color(0xFF536DFE)
    val c4 = if (muted != Color.Transparent && muted != Color.Black) muted else Color(0xFF18FFFF)
    val c5 = if (darkMuted != Color.Transparent && darkMuted != Color.Black) darkMuted else Color(0xFF0C091A)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(c5)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val baseRadius = size.minDimension * 0.65f

            if (isLiteMode) {
                // Highly optimized 2-blob layout for low-end devices (100% seamless closed loop)
                val center1 = Offset(
                    width * 0.65f + cos(phase) * (width * 0.15f),
                    height * 0.30f + sin(phase) * (height * 0.12f)
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(c1.copy(alpha = 0.65f), Color.Transparent),
                        center = center1,
                        radius = baseRadius * 1.2f
                    ),
                    center = center1,
                    radius = baseRadius * 1.2f
                )

                val center2 = Offset(
                    width * 0.35f + sin(phase + 2.0f) * (width * 0.15f),
                    height * 0.70f + cos(phase + 1.0f) * (height * 0.12f)
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(c3.copy(alpha = 0.65f), Color.Transparent),
                        center = center2,
                        radius = baseRadius * 1.3f
                    ),
                    center = center2,
                    radius = baseRadius * 1.3f
                )
            } else {
                // Full high-quality 4-blob layout (100% seamless closed loop)
                // Blob 1 (Top Right)
                val center1 = Offset(
                    width * 0.70f + cos(phase) * (width * 0.20f),
                    height * 0.25f + sin(phase * 2f + 0.5f) * (height * 0.15f)
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(c1.copy(alpha = 0.60f), Color.Transparent),
                        center = center1,
                        radius = baseRadius * 1.1f
                    ),
                    center = center1,
                    radius = baseRadius * 1.1f
                )

                // Blob 2 (Left Mid)
                val center2 = Offset(
                    width * 0.25f + sin(phase + 1.8f) * (width * 0.18f),
                    height * 0.50f + cos(phase + 1.2f) * (height * 0.18f)
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(c2.copy(alpha = 0.55f), Color.Transparent),
                        center = center2,
                        radius = baseRadius * 1.2f
                    ),
                    center = center2,
                    radius = baseRadius * 1.2f
                )

                // Blob 3 (Bottom Center)
                val center3 = Offset(
                    width * 0.55f + cos(phase * 2f + 2.4f) * (width * 0.22f),
                    height * 0.78f + sin(phase + 3.1f) * (height * 0.12f)
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(c3.copy(alpha = 0.65f), Color.Transparent),
                        center = center3,
                        radius = baseRadius * 1.3f
                    ),
                    center = center3,
                    radius = baseRadius * 1.3f
                )

                // Blob 4 (Top Left Accent)
                val center4 = Offset(
                    width * 0.20f + sin(phase * 2f + 1.2f) * (width * 0.15f),
                    height * 0.18f + cos(phase + 4.0f) * (height * 0.10f)
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(c4.copy(alpha = 0.40f), Color.Transparent),
                        center = center4,
                        radius = baseRadius * 0.9f
                    ),
                    center = center4,
                    radius = baseRadius * 0.9f
                )
            }

            // Vignette Darkening overlay
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(Color.Transparent, Color(0x99000000)),
                    center = Offset(width / 2f, height / 2f),
                    radius = size.maxDimension * 0.75f
                )
            )
        }
    }
}
