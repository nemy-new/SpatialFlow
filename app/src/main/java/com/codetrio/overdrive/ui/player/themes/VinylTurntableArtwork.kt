package com.codetrio.overdrive.ui.player.themes

import android.content.Context
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.isActive

/**
 * Ultra-Luxury Flagship Audiophile Vinyl Record & Tonearm Artwork Component.
 *
 * Master-Grade Engineering & Optics:
 * 1. Clean, Minimalist Flagship Geometry:
 *    - All extraneous floating rectangles, levers, and detached dots completely removed.
 *    - Pure, luxury circular cylindrical gimbal turret with jewel pivot bearing.
 *    - Extra-large 100% unobstructed center album artwork (~65% of vinyl disc diameter).
 * 2. Revolutionary Physical Anisotropic Vinyl Optics:
 *    - Multi-pass dual-bowtie specular reflection cones (key light at 135°/315°, soft fill at 45°/225°).
 *    - High-gloss virgin PVC surface lacquer glaze layer across the entire disc face.
 *    - 100+ dense sub-pixel microgrooves with realistic track gap bands and high-gloss lead-out dead wax.
 *    - 3D beveled outer lip and recessed inner label well.
 * 3. Real-Time High-Precision Tonearm Groove Tracking (15.0° to 27.5°) with smooth cueing and diamond stylus gleam.
 */
// ── 1. Pure Vinyl Disc Artwork Component (Embedded in HorizontalPager) ──
@Composable
fun VinylDiscArtwork(
    artworkData: Any?,
    isPlaying: Boolean,
    onDoubleTapSeekBackward: () -> Unit = {},
    onDoubleTapSeekForward: () -> Unit = {},
    onDoubleTapFavorite: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // ── Majestic Smooth Vinyl Rotation (30°/s = 12s per full turn) ──
    var currentRotationAngle by rememberSaveable { mutableFloatStateOf(0f) }
    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            var lastTime = withFrameNanos { it }
            while (isActive) {
                withFrameNanos { time ->
                    val deltaSeconds = (time - lastTime) / 1_000_000_000f
                    lastTime = time
                    currentRotationAngle = (currentRotationAngle + deltaSeconds * 30f) % 360f
                }
            }
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .aspectRatio(1f)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = null, // Tap to toggle play/pause is disabled as per user request
                    onDoubleTap = { offset ->
                        val width = size.width
                        if (offset.x < width * 0.33f) {
                            onDoubleTapSeekBackward()
                        } else if (offset.x > width * 0.66f) {
                            onDoubleTapSeekForward()
                        } else {
                            onDoubleTapFavorite()
                        }
                    }
                )
            },
        contentAlignment = Alignment.TopStart
    ) {
        val totalWidth = maxWidth
        val totalHeight = maxHeight

        // Proportional Geometry: Pure Center Alignment for Vinyl Disc
        val vinylCenterXRatio = 0.50f
        val vinylCenterYRatio = 0.50f
        val vinylRadiusRatio = 0.435f  // ~87% total width
        val labelRadiusRatio = 0.285f  // ~57% total width (65% of vinyl diameter: Extra-large, gorgeous album art)

        // ── 1. Vinyl Record Disc Canvas ────────────────────────────
        Canvas(modifier = Modifier.fillMaxSize()) {
            val vc = Offset(size.width * vinylCenterXRatio, size.height * vinylCenterYRatio)
            val vr = size.width * vinylRadiusRatio
            val lr = size.width * labelRadiusRatio

            // ── A. Deep Physical Multi-Layer Drop Shadows ───────────
            // 1. Broad soft ambient room diffuse shadow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0x99000000),
                        Color(0x55000000),
                        Color(0x18000000),
                        Color.Transparent
                    ),
                    center = Offset(vc.x + 14f, vc.y + 20f),
                    radius = vr * 1.18f
                ),
                center = Offset(vc.x + 14f, vc.y + 20f),
                radius = vr * 1.18f
            )
            // 2. Medium directional contact shadow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xCC000000),
                        Color(0x80000000),
                        Color(0x25000000),
                        Color.Transparent
                    ),
                    center = Offset(vc.x + 6f, vc.y + 10f),
                    radius = vr * 1.06f
                ),
                center = Offset(vc.x + 6f, vc.y + 10f),
                radius = vr * 1.06f
            )
            // 3. Crisp edge contact shadow
            drawCircle(
                color = Color(0xAA000000),
                center = Offset(vc.x + 2f, vc.y + 3f),
                radius = vr * 1.008f
            )

            // ── B. Glossy Obsidian Pressed PVC Vinyl Disc Base ─────
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF191B22),
                        Color(0xFF121318),
                        Color(0xFF0A0B0E),
                        Color(0xFF030304)
                    ),
                    center = vc,
                    radius = vr
                ),
                center = vc,
                radius = vr
            )

            // ── C. Polished 3D Vinyl Outer Rim Lip ─────────────────
            drawCircle(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        Color(0xFF4A4E5C),
                        Color(0xFF1E2027),
                        Color(0xFF6E7385),
                        Color(0xFF16171B),
                        Color(0xFF4A4E5C)
                    ),
                    center = vc
                ),
                center = vc,
                radius = vr,
                style = Stroke(width = 3.2f)
            )
            drawCircle(
                color = Color(0xFF090A0D),
                center = vc,
                radius = vr - 2.0f,
                style = Stroke(width = 1.2f)
            )

            // ── D. Outer Lead-In (Run-In Groove Margin) ─────────────
            val leadInOuter = vr * 0.985f
            val leadInInner = vr * 0.950f
            drawCircle(
                color = Color(0xFF0D0E12),
                center = vc,
                radius = (leadInOuter + leadInInner) / 2f,
                style = Stroke(width = leadInOuter - leadInInner)
            )
            drawCircle(
                color = Color(0xFF333644).copy(alpha = 0.45f),
                center = vc,
                radius = leadInOuter,
                style = Stroke(width = 0.8f)
            )

            // ── E. Silky Sub-Pixel Microgrooves & Track Zones ──────
            val grooveStart = lr * 1.025f
            val grooveEnd = leadInInner
            val totalGrooves = 96
            val grooveSpacing = (grooveEnd - grooveStart) / totalGrooves

            val trackGaps = setOf(32, 68)

            for (i in 0 until totalGrooves) {
                val r = grooveStart + i * grooveSpacing
                val isGap = trackGaps.contains(i)
                val isBandAccent = i % 8 == 0

                val grooveAlpha = when {
                    isGap -> 0.28f
                    isBandAccent -> 0.14f
                    else -> 0.04f + (i % 4) * 0.02f
                }
                val grooveColor = when {
                    isGap -> Color(0xFF5E6274)
                    isBandAccent -> Color(0xFF3A3C4A)
                    else -> Color(0xFF20222A)
                }
                val strokeW = when {
                    isGap -> 1.4f
                    isBandAccent -> 0.9f
                    else -> 0.5f
                }

                drawCircle(
                    color = grooveColor.copy(alpha = grooveAlpha),
                    center = vc,
                    radius = r,
                    style = Stroke(width = strokeW)
                )
            }

            // ── F. Run-Out Groove (High-Gloss Dead Wax Area before Label) ─
            val deadWaxOuter = grooveStart
            val deadWaxInner = lr * 1.005f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF16181E), Color(0xFF0A0B0E)),
                    center = vc,
                    radius = deadWaxOuter
                ),
                center = vc,
                radius = (deadWaxOuter + deadWaxInner) / 2f,
                style = Stroke(width = deadWaxOuter - deadWaxInner)
            )
            drawCircle(
                color = Color(0xFF303340).copy(alpha = 0.65f),
                center = vc,
                radius = lr * 1.015f,
                style = Stroke(width = 1.2f)
            )

            // ── G. Revolutionary Multi-Pass Physical Anisotropic Specular Highlights ─
            // 1. Primary High-Intensity Anisotropic Butterfly Specular Sheen (135° and 315°)
            drawCircle(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        Color(0x35FFFFFF), // 0° Core Specular Axis
                        Color(0x18FFFFFF),
                        Color(0x06FFFFFF),
                        Color(0x00FFFFFF),
                        Color(0x00FFFFFF),
                        Color(0x06FFFFFF),
                        Color(0x18FFFFFF),
                        Color(0x35FFFFFF), // 180° Core Specular Axis
                        Color(0x18FFFFFF),
                        Color(0x06FFFFFF),
                        Color(0x00FFFFFF),
                        Color(0x00FFFFFF),
                        Color(0x06FFFFFF),
                        Color(0x18FFFFFF),
                        Color(0x35FFFFFF)  // 360°
                    ),
                    center = vc
                ),
                center = vc,
                radius = grooveEnd
            )

            // 2. Secondary Perpendicular Studio Ambient Fill Specular Lobe (45° and 225°)
            drawCircle(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        Color(0x00FFFFFF),
                        Color(0x12FFFFFF), // 45°
                        Color(0x00FFFFFF),
                        Color(0x00FFFFFF),
                        Color(0x00FFFFFF),
                        Color(0x12FFFFFF), // 225°
                        Color(0x00FFFFFF),
                        Color(0x00FFFFFF)
                    ),
                    center = vc
                ),
                center = vc,
                radius = grooveEnd
            )

            // 3. Virgin Vinyl High-Gloss Lacquer Surface Glaze (Subtle Softbox Studio Sheen)
            drawCircle(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0x14FFFFFF),
                        Color(0x05FFFFFF),
                        Color(0x00FFFFFF),
                        Color(0x08FFFFFF)
                    ),
                    start = Offset(vc.x - vr * 0.8f, vc.y - vr * 0.8f),
                    end = Offset(vc.x + vr * 0.8f, vc.y + vr * 0.8f)
                ),
                center = vc,
                radius = vr
            )
        }

        // ── 2. Rotating Center Label (Extra-Large, Unobstructed Album Art) ─
        val labelSizeDp = totalWidth * (labelRadiusRatio * 2f)
        val labelLeftDp = totalWidth * vinylCenterXRatio - (labelSizeDp / 2f)
        val labelTopDp = totalHeight * vinylCenterYRatio - (labelSizeDp / 2f)

        Box(
            modifier = Modifier
                .offset(x = labelLeftDp, y = labelTopDp)
                .size(labelSizeDp)
                .rotate(currentRotationAngle)
                .shadow(elevation = 16.dp, shape = CircleShape, spotColor = Color.Black)
                .clip(CircleShape)
                .background(Color(0xFF101114))
        ) {
            // 100% Unobstructed High-Definition Album Artwork
            if (artworkData != null) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(artworkData)
                        .size(coil.size.Size.ORIGINAL)
                        .precision(coil.size.Precision.EXACT)
                        .allowHardware(true)
                        .crossfade(200)
                        .build(),
                    contentDescription = "Vinyl Album Artwork",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                com.codetrio.overdrive.ui.player.ExpressiveArtworkPlaceholder(
                    modifier = Modifier.fillMaxSize()
                )
            }

            // High-End Vinyl Label Outer Paper Rim & Recessed Well Shadow
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val labelR = size.width / 2f

                // Recessed label well shadow (sinks the label into the vinyl disc)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color(0x15000000),
                            Color(0x66000000)
                        ),
                        center = center,
                        radius = labelR
                    ),
                    center = center,
                    radius = labelR
                )
                drawCircle(
                    color = Color(0xAA000000),
                    center = center,
                    radius = labelR,
                    style = Stroke(width = 2.4f)
                )

                // Classic Pressed Vinyl Paper Indent Ridge
                drawCircle(
                    color = Color(0x35000000),
                    center = center,
                    radius = labelR * 0.82f,
                    style = Stroke(width = 1.4f)
                )
                drawCircle(
                    color = Color(0x18FFFFFF),
                    center = center,
                    radius = labelR * 0.82f + 1f,
                    style = Stroke(width = 0.7f)
                )
            }
        }
    }
}

// ── 2. Fixed Tonearm Overlay (Mounted directly on Player frame, independent of swipe) ──
@Composable
fun VinylTonearmOverlay(
    isPlaying: Boolean,
    progressFraction: Float = 0f,
    showTonearm: Boolean = true,
    modifier: Modifier = Modifier
) {
    // ── Tonearm Visibility Fade Transition (Hidden in MiniPlayer) ────
    val tonearmAnimatedAlpha by animateFloatAsState(
        targetValue = if (showTonearm) 1f else 0f,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "tonearm_alpha"
    )

    // ── Real-Time Precision Tonearm Tracking & Smooth Cueing ────────
    // 0.0°: Parked on rest position (outside vinyl disc rim)
    // 8.0°: Outermost lead-in groove (song start, 0% progress)
    // 20.7°: Innermost lead-out groove (song end, 100% progress, right beside label edge)
    val cueProgress by animateFloatAsState(
        targetValue = if (isPlaying) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "tonearm_cue"
    )
    val trackProgress by animateFloatAsState(
        targetValue = progressFraction.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 250, easing = LinearEasing),
        label = "tonearm_track"
    )
    val tonearmAngle = if (cueProgress > 0.001f) {
        (8.0f + trackProgress * 12.7f) * cueProgress
    } else {
        0.0f
    }

    // ── High-Fidelity Diamond Stylus & Optical Laser Flare Animation ──
    val isLiteMode = com.codetrio.overdrive.util.rememberIsLiteMode().value
    val shouldAnimateNeedle = showTonearm && isPlaying && !isLiteMode

    val infiniteTransition = rememberInfiniteTransition(label = "stylus_sparkle")
    val needleGlintAlpha by if (shouldAnimateNeedle) {
        infiniteTransition.animateFloat(
            initialValue = 0.78f,
            targetValue = 1.0f,
            animationSpec = infiniteRepeatable(
                animation = tween(1200, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "glint_alpha"
        )
    } else {
        remember { mutableFloatStateOf(0.9f) }
    }

    val needleShimmer by if (shouldAnimateNeedle) {
        infiniteTransition.animateFloat(
            initialValue = 0.92f,
            targetValue = 1.08f,
            animationSpec = infiniteRepeatable(
                animation = tween(280, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "glint_shimmer"
        )
    } else {
        remember { mutableFloatStateOf(1.0f) }
    }

    BoxWithConstraints(
        modifier = modifier.aspectRatio(1f),
        contentAlignment = Alignment.TopStart
    ) {
        val vinylCenterXRatio = 0.50f
        val vinylCenterYRatio = 0.50f
        val vinylRadiusRatio = 0.435f
        val labelRadiusRatio = 0.285f

        val pivotCenterXRatio = 0.88f
        val pivotCenterYRatio = 0.155f
        val pivotBaseRadiusRatio = 0.062f

        if (tonearmAnimatedAlpha > 0.001f) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { alpha = tonearmAnimatedAlpha }
            ) {
                val pc = Offset(size.width * pivotCenterXRatio, size.height * pivotCenterYRatio)
                val baseR = size.width * pivotBaseRadiusRatio

            // ── A. Gimbal Pivot Turret Base (Clean, Minimalist, Luxury Cylinder) ──
            // 1. Ambient Drop Shadow under turret
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xAA000000), Color(0x35000000), Color.Transparent),
                    center = Offset(pc.x + 5f, pc.y + 7f),
                    radius = baseR * 1.55f
                ),
                center = Offset(pc.x + 5f, pc.y + 7f),
                radius = baseR * 1.55f
            )

            // 2. Machined Gunmetal Outer Base Ring with Chamfer Bevel
            drawCircle(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        Color(0xFF757A8C),
                        Color(0xFF2E303A),
                        Color(0xFF8E94A8),
                        Color(0xFF22232A),
                        Color(0xFF757A8C)
                    ),
                    center = pc
                ),
                center = pc,
                radius = baseR
            )
            drawCircle(
                color = Color(0xFF131418),
                center = pc,
                radius = baseR * 0.88f
            )

            // 3. Brushed Titanium Turret Inner Well
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF4C4F5E), Color(0xFF1C1D24)),
                    center = pc,
                    radius = baseR * 0.80f
                ),
                center = pc,
                radius = baseR * 0.80f
            )

            // 4. Center Jewel Pivot Bearing & Chrome Cap
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFA6ABC0),
                        Color(0xFF585C6D),
                        Color(0xFF282A33),
                        Color(0xFF14151A)
                    ),
                    center = Offset(pc.x - 1.5f, pc.y - 1.5f),
                    radius = baseR * 0.55f
                ),
                center = pc,
                radius = baseR * 0.55f
            )
            drawCircle(
                color = Color(0xFF0B0C0E),
                center = pc,
                radius = baseR * 0.20f
            )
            // Polished Diamond-Cut Pivot Screw Highlight
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.White, Color(0xFFA0ABC0), Color(0xFF334155)),
                    center = Offset(pc.x - 1f, pc.y - 1f),
                    radius = baseR * 0.13f
                ),
                center = pc,
                radius = baseR * 0.11f
            )

            // ── B. Tonearm Tube, Headshell & Stylus Needle ────────
            // Rotates around the gimbal pivot center `pc`
            withTransform({
                rotate(degrees = tonearmAngle, pivot = pc)
            }) {
                // High-End Graceful S/J-Curve Coordinates relative to pivot
                val p0 = pc
                val p1 = Offset(pc.x - size.width * 0.045f, pc.y + size.height * 0.22f)
                val p2 = Offset(pc.x + size.width * 0.016f, pc.y + size.height * 0.42f)
                val headshellJoint = Offset(pc.x - size.width * 0.025f, pc.y + size.height * 0.555f)

                // 1. Dynamic Tapered Cast Shadow on the record
                val shadowPath = Path().apply {
                    moveTo(p0.x + 16f, p0.y + 22f)
                    cubicTo(
                        p1.x + 13f, p1.y + 18f,
                        p2.x + 9f, p2.y + 12f,
                        headshellJoint.x + 4f, headshellJoint.y + 6f
                    )
                }
                drawPath(
                    path = shadowPath,
                    color = Color(0x55000000),
                    style = Stroke(width = 9f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                )

                // 2. Main S-Curve Luxury Metallic Tonearm Tube
                val armPath = Path().apply {
                    moveTo(p0.x, p0.y)
                    cubicTo(p1.x, p1.y, p2.x, p2.y, headshellJoint.x, headshellJoint.y)
                }

                // Tube Underside Core Shadow
                drawPath(
                    path = armPath,
                    color = Color(0xFF141518),
                    style = Stroke(width = 7.4f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
                // Brushed Titanium Body
                drawPath(
                    path = armPath,
                    color = Color(0xFF585B6A),
                    style = Stroke(width = 5.4f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
                // Razor-Sharp Specular Ridge Highlight
                drawPath(
                    path = armPath,
                    color = Color(0xFFF3F5FC),
                    style = Stroke(width = 1.6f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                )

                // 3. Headshell Connector Collar Ring
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFFF8FAFC), Color(0xFF475569)),
                        center = headshellJoint,
                        radius = 6f
                    ),
                    center = headshellJoint,
                    radius = 5.8f
                )

                // 4. Audiophile Slotted Headshell, Cartridge Body & Stylus
                val headshellW = size.width * 0.034f
                val headshellL = size.height * 0.078f

                withTransform({
                    rotate(degrees = -20f, pivot = headshellJoint)
                }) {
                    val hsTopLeft = Offset(headshellJoint.x - headshellW / 2f, headshellJoint.y)

                    // Headshell Shadow
                    drawRoundRect(
                        color = Color(0x60000000),
                        topLeft = Offset(hsTopLeft.x + 3f, hsTopLeft.y + 4f),
                        size = Size(headshellW, headshellL),
                        cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
                    )

                    // Headshell Main Body (Matte Obsidian Finish)
                    drawRoundRect(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF565868),
                                Color(0xFF292A34),
                                Color(0xFF15161A),
                                Color(0xFF424454)
                            ),
                            start = Offset(hsTopLeft.x, hsTopLeft.y),
                            end = Offset(hsTopLeft.x + headshellW, hsTopLeft.y + headshellL)
                        ),
                        topLeft = hsTopLeft,
                        size = Size(headshellW, headshellL),
                        cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
                    )
                    // Precision Top Bevel Highlight
                    drawRoundRect(
                        color = Color(0x45FFFFFF),
                        topLeft = hsTopLeft,
                        size = Size(headshellW, headshellL),
                        style = Stroke(width = 1.0f),
                        cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
                    )

                    // Ergonomic Cueing Finger Lift Hook (Right Side)
                    val liftStart = Offset(hsTopLeft.x + headshellW, hsTopLeft.y + headshellL * 0.28f)
                    val liftPath = Path().apply {
                        moveTo(liftStart.x, liftStart.y)
                        cubicTo(
                            liftStart.x + 11f, liftStart.y - 6f,
                            liftStart.x + 18f, liftStart.y + 3f,
                            liftStart.x + 21f, liftStart.y + 13f
                        )
                    }
                    drawPath(
                        path = liftPath,
                        color = Color(0xFF787D90),
                        style = Stroke(width = 2.4f, cap = StrokeCap.Round)
                    )
                    drawPath(
                        path = liftPath,
                        color = Color(0xFFF1F5F9),
                        style = Stroke(width = 1.0f, cap = StrokeCap.Round)
                    )

                    // Cartridge Accent: Clean Sleek Metallic Crimson Edge Trim Line (No square block)
                    drawLine(
                        brush = Brush.horizontalGradient(
                            colors = listOf(Color(0xFFEF4444), Color(0xFFFF1744), Color(0xFFBE123C))
                        ),
                        start = Offset(hsTopLeft.x + headshellW * 0.15f, hsTopLeft.y + headshellL * 0.65f),
                        end = Offset(hsTopLeft.x + headshellW * 0.85f, hsTopLeft.y + headshellL * 0.65f),
                        strokeWidth = 2.0f,
                        cap = StrokeCap.Round
                    )

                    // ── 5. Subtle Audiophile Vinyl Reflection (Under Needle) ──────
                    val cantileverStart = Offset(headshellJoint.x, hsTopLeft.y + headshellL * 0.88f)
                    val cantileverEnd = Offset(headshellJoint.x, hsTopLeft.y + headshellL * 1.06f)

                    // A. Subtle Soft Ruby Light Pool on the Glossy Vinyl Surface (Micro Scale)
                    val poolW = 26f * needleShimmer
                    val poolH = 15f * needleShimmer
                    drawOval(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0x66EF4444).copy(alpha = 0.35f * needleGlintAlpha),
                                Color(0x25B91C1C).copy(alpha = 0.15f * needleGlintAlpha),
                                Color.Transparent
                            ),
                            center = cantileverEnd,
                            radius = poolW / 2f
                        ),
                        topLeft = Offset(cantileverEnd.x - poolW / 2f, cantileverEnd.y - poolH / 2f),
                        size = Size(poolW, poolH)
                    )

                    // B. Delicate Concentric Microgroove Reflection (Only immediate adjacent tracks)
                    val streakW = 16f * needleShimmer
                    val streakH = 1.0f
                    for (offsetStep in -1..1) {
                        val yOff = offsetStep * 2.2f
                        val streakAlpha = when (offsetStep) {
                            0 -> 0.45f
                            else -> 0.20f
                        } * needleGlintAlpha

                        drawRoundRect(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color(0x88FF4D6D).copy(alpha = streakAlpha * 0.6f),
                                    Color(0xEEFFFFFF).copy(alpha = streakAlpha),
                                    Color(0x88FF4D6D).copy(alpha = streakAlpha * 0.6f),
                                    Color.Transparent
                                ),
                                startX = cantileverEnd.x - streakW,
                                endX = cantileverEnd.x + streakW
                            ),
                            topLeft = Offset(cantileverEnd.x - streakW, cantileverEnd.y + yOff - streakH / 2f),
                            size = Size(streakW * 2f, streakH),
                            cornerRadius = CornerRadius(0.5f, 0.5f)
                        )
                    }

                    // ── 6. Cantilever Tube & Stylus Body ───────────────────────────
                    drawLine(
                        color = Color(0xFF1E293B),
                        start = cantileverStart,
                        end = cantileverEnd,
                        strokeWidth = 2.2f,
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        color = Color(0xFFCBD5E1),
                        start = cantileverStart,
                        end = cantileverEnd,
                        strokeWidth = 1.2f,
                        cap = StrokeCap.Round
                    )

                    // Stylus Diamond Mount Base (Tiny Black Gimbal)
                    drawCircle(
                        color = Color(0xFF070709),
                        center = cantileverEnd,
                        radius = 2.8f
                    )

                    // ── 7. Subtle Ruby Diamond Stylus Micro-Gleam ──────────────────
                    // Soft Ambient Ruby Glow
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0x88EF4444).copy(alpha = 0.50f * needleGlintAlpha),
                                Color(0x25B91C1C).copy(alpha = 0.20f * needleGlintAlpha),
                                Color.Transparent
                            ),
                            center = cantileverEnd,
                            radius = 8.5f * needleShimmer
                        ),
                        center = cantileverEnd,
                        radius = 8.5f * needleShimmer
                    )

                    // Compact Crimson Halation
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xEEFF3366).copy(alpha = needleGlintAlpha * 0.85f),
                                Color(0x66DC2626).copy(alpha = needleGlintAlpha * 0.5f),
                                Color.Transparent
                            ),
                            center = cantileverEnd,
                            radius = 4.2f * needleShimmer
                        ),
                        center = cantileverEnd,
                        radius = 4.2f * needleShimmer
                    )

                    // Tiny Ruby Pink Crystal Edge
                    drawCircle(
                        color = Color(0xFFFECDD3).copy(alpha = needleGlintAlpha),
                        center = cantileverEnd,
                        radius = 2.0f
                    )

                    // Crisp Pure White Diamond Sparkle Center
                    drawCircle(
                        color = Color(0xFFFFFFFF).copy(alpha = needleGlintAlpha),
                        center = cantileverEnd,
                        radius = 1.2f
                    )
                }
            }
        }
    }
}
}

// ── 3. Composite Turntable Artwork (Backward-compatible combo) ─────────
@Composable
fun VinylTurntableArtwork(
    artworkData: Any?,
    isPlaying: Boolean,
    progressFraction: Float = 0f,
    showTonearm: Boolean = true,
    onPlayPauseToggle: () -> Unit = {},
    onDoubleTapSeekBackward: () -> Unit = {},
    onDoubleTapSeekForward: () -> Unit = {},
    onDoubleTapFavorite: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.aspectRatio(1f),
        contentAlignment = Alignment.Center
    ) {
        VinylDiscArtwork(
            artworkData = artworkData,
            isPlaying = isPlaying,
            onDoubleTapSeekBackward = onDoubleTapSeekBackward,
            onDoubleTapSeekForward = onDoubleTapSeekForward,
            onDoubleTapFavorite = onDoubleTapFavorite,
            modifier = Modifier.fillMaxSize()
        )
        VinylTonearmOverlay(
            isPlaying = isPlaying,
            progressFraction = progressFraction,
            showTonearm = showTonearm,
            modifier = Modifier.fillMaxSize()
        )
    }
}

