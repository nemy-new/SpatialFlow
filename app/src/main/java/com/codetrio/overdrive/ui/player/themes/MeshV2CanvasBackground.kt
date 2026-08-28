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
fun MeshV2CanvasBackground(
    vibrant: Color,
    lightVibrant: Color,
    darkVibrant: Color,
    muted: Color,
    darkMuted: Color,
    dominant: Color,
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    val isLiteMode = com.codetrio.overdrive.util.rememberIsLiteMode().value
    val shouldAnimate = isPlaying && !isLiteMode

    val infiniteTransition = rememberInfiniteTransition(label = "mesh_v2_anim")
    val phase by if (shouldAnimate) {
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = (2 * PI).toFloat(),
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 8000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "mesh_v2_phase"
        )
    } else {
        androidx.compose.runtime.remember { androidx.compose.runtime.mutableFloatStateOf(0f) }
    }

    val pulse by if (shouldAnimate) {
        infiniteTransition.animateFloat(
            initialValue = 0.92f,
            targetValue = 1.08f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 2200, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "mesh_v2_pulse"
        )
    } else {
        androidx.compose.runtime.remember { androidx.compose.runtime.mutableFloatStateOf(1.0f) }
    }

    val cVibrant = if (vibrant != Color.Transparent && vibrant != Color.Black) vibrant else Color(0xFFFF5252)
    val cLightVibrant = if (lightVibrant != Color.Transparent && lightVibrant != Color.Black) lightVibrant else Color(0xFFFF4081)
    val cDarkVibrant = if (darkVibrant != Color.Transparent && darkVibrant != Color.Black) darkVibrant else Color(0xFFE040FB)
    val cMuted = if (muted != Color.Transparent && muted != Color.Black) muted else Color(0xFF7C4DFF)
    val cDarkMuted = if (darkMuted != Color.Transparent && darkMuted != Color.Black) darkMuted else Color(0xFF100C1F)
    val cDominant = if (dominant != Color.Transparent && dominant != Color.Black) dominant else Color(0xFF536DFE)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(cDarkMuted)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val baseRadius = size.minDimension * 0.70f * pulse

            val nodes = if (isLiteMode) {
                listOf(
                    Pair(Offset(width * 0.25f + sin(phase) * (width * 0.12f), height * 0.25f + cos(phase) * (height * 0.10f)), cVibrant),
                    Pair(Offset(width * 0.75f + cos(phase + 1.5f) * (width * 0.12f), height * 0.50f + sin(phase + 2.0f) * (height * 0.10f)), cDominant),
                    Pair(Offset(width * 0.40f + sin(phase * 2f + 1.0f) * (width * 0.10f), height * 0.80f + cos(phase + 3.0f) * (height * 0.10f)), cDarkVibrant)
                )
            } else {
                listOf(
                    Pair(Offset(width * 0.20f + sin(phase) * (width * 0.15f), height * 0.20f + cos(phase) * (height * 0.12f)), cVibrant),
                    Pair(Offset(width * 0.80f + cos(phase + 1.2f) * (width * 0.15f), height * 0.25f + sin(phase * 2f + 0.5f) * (height * 0.14f)), cLightVibrant),
                    Pair(Offset(width * 0.50f + sin(phase * 2f + 2.0f) * (width * 0.18f), height * 0.50f + cos(phase + 2.5f) * (height * 0.15f)), cDominant),
                    Pair(Offset(width * 0.15f + cos(phase * 2f + 3.2f) * (width * 0.12f), height * 0.75f + sin(phase + 4.0f) * (height * 0.15f)), cDarkVibrant),
                    Pair(Offset(width * 0.85f + sin(phase + 4.8f) * (width * 0.14f), height * 0.80f + cos(phase + 1.0f) * (height * 0.12f)), cMuted),
                    Pair(Offset(width * 0.50f + cos(phase * 2f + 0.8f) * (width * 0.16f), height * 0.88f + sin(phase * 2f + 3.5f) * (height * 0.08f)), cVibrant.copy(alpha = 0.7f))
                )
            }

            nodes.forEach { (center, color) ->
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            color.copy(alpha = 0.52f),
                            color.copy(alpha = 0.20f),
                            Color.Transparent
                        ),
                        center = center,
                        radius = baseRadius
                    ),
                    center = center,
                    radius = baseRadius
                )
            }
        }
    }
}
