package com.codetrio.overdrive.ui.player

import android.os.Build
import android.provider.Settings
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Checks system accessibility settings to see if reduced motion is requested.
 */
@Composable
internal fun isReducedMotionEnabled(): Boolean {
    val context = LocalContext.current
    return remember(context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            Settings.Global.getFloat(
                context.contentResolver,
                Settings.Global.ANIMATOR_DURATION_SCALE,
                1f
            ) == 0f
        } else {
            false
        }
    }
}

/**
 * Shifts the hue of a Color by a set number of degrees in HSL space.
 */
internal fun shiftColorHue(color: Color, hueShiftDegrees: Float): Color {
    val hsl = FloatArray(3)
    androidx.core.graphics.ColorUtils.colorToHSL(color.toArgb(), hsl)
    hsl[0] = (hsl[0] + hueShiftDegrees) % 360f
    if (hsl[0] < 0f) hsl[0] += 360f
    return Color(androidx.core.graphics.ColorUtils.HSLToColor(hsl))
}

/**
 * Renders a high-performance, dynamic fluid mesh gradient background.
 * Uses irrational frequency ratios on sinusoidal paths so blobs drift continuously
 * without any visible snap or reset. Each blob moves on its own unique Lissajous orbit.
 */
@Composable
fun Modifier.animatedFluidBackground(
    backgroundColor: Color,
    vibrant: Color,
    darkVibrant: Color,
    darkMuted: Color,
    isAnimated: Boolean = true
): Modifier {
    val isReducedMotion = isReducedMotionEnabled()

    if (isReducedMotion) {
        return this.background(
            Brush.verticalGradient(
                colors = listOf(backgroundColor, vibrant.copy(alpha = 0.25f))
            )
        )
    }

    // Two independent time drivers with coprime-ish durations.
    val infiniteTransition = rememberInfiniteTransition(label = "fluid_background")
    val timeSlow by if (isAnimated) {
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = (2f * PI).toFloat(),
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 37000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "time_slow"
        )
    } else {
        remember { mutableStateOf(0f) }
    }

    val timeFast by if (isAnimated) {
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = (2f * PI).toFloat(),
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 23000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "time_fast"
        )
    } else {
        remember { mutableStateOf(0f) }
    }

    // Map palette colors to blobs
    val color1 = vibrant
    val color2 = darkVibrant
    val color3 = darkMuted

    return this.drawWithCache {
        onDrawBehind {
            // Draw background fill
            drawRect(color = backgroundColor)

            val width = size.width
            val height = size.height
            val diagonal = kotlin.math.hypot(width, height)

            // Blob 1: Vibrant — primary punch
            val blob1X = width * (0.35f + 0.25f * sin(timeSlow * 0.85f) + 0.10f * cos(timeFast * 0.65f))
            val blob1Y = height * (0.30f + 0.20f * cos(timeSlow * 1.10f) + 0.08f * sin(timeFast * 0.80f))
            val radius1 = diagonal * 0.85f // Very large for smooth blending
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(color1.copy(alpha = 0.55f), Color.Transparent),
                    center = Offset(blob1X, blob1Y),
                    radius = radius1
                ),
                center = Offset(blob1X, blob1Y),
                radius = radius1
            )

            // Blob 2: Dark Vibrant — deep transition
            val blob2X = width * (0.75f + 0.20f * cos(timeFast * 0.90f) + 0.12f * sin(timeSlow * 0.75f))
            val blob2Y = height * (0.45f + 0.25f * sin(timeFast * 1.05f) + 0.09f * cos(timeSlow * 0.60f))
            val radius2 = diagonal * 0.90f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(color2.copy(alpha = 0.50f), Color.Transparent),
                    center = Offset(blob2X, blob2Y),
                    radius = radius2
                ),
                center = Offset(blob2X, blob2Y),
                radius = radius2
            )

            // Blob 3: Dark Muted — shadow depth
            val blob3X = width * (0.45f + 0.22f * sin(timeSlow * 0.65f) + 0.15f * cos(timeFast * 1.00f))
            val blob3Y = height * (0.80f + 0.15f * cos(timeSlow * 1.30f) + 0.10f * sin(timeFast * 0.55f))
            val radius3 = diagonal * 0.75f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(color3.copy(alpha = 0.45f), Color.Transparent),
                    center = Offset(blob3X, blob3Y),
                    radius = radius3
                ),
                center = Offset(blob3X, blob3Y),
                radius = radius3
            )

            // Subtle Vignette for deep moody corners (Reduced for more vibrancy)
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(Color.Transparent, PlayerPaletteState.darkMutedColor.value.copy(alpha = 0.35f)),
                    center = Offset(width / 2f, height / 2f),
                    radius = diagonal * 0.95f
                ),
                blendMode = androidx.compose.ui.graphics.BlendMode.Multiply
            )

            // Efficient Grain/Noise overlay (Slightly increased for texture)
            drawRect(
                color = Color.White.copy(alpha = 0.008f),
                blendMode = androidx.compose.ui.graphics.BlendMode.Overlay
            )
        }
    }
}
