package com.codetrio.overdrive.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.graphics.luminance
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CastConnected
import androidx.compose.ui.draw.clip
import com.codetrio.overdrive.cast.CastState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.WavyProgressIndicatorDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.zIndex
import androidx.compose.material3.MaterialTheme
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.codetrio.overdrive.MainActivity
import com.codetrio.overdrive.R
import com.codetrio.overdrive.model.SongItem
import com.codetrio.overdrive.ui.player.ArtworkPager
import com.codetrio.overdrive.ui.player.SlidingEffectsDrawer
import androidx.compose.foundation.isSystemInDarkTheme
import com.codetrio.overdrive.ui.player.FullPlayerScreen
import com.codetrio.overdrive.ui.player.PlayerUiState
import com.codetrio.overdrive.viewmodel.PlayerSharedViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sign

// ==========================================
// 1. STEP 1: CORE STATE MACHINE & INTERFACES
// ==========================================
enum class PlayerSheetState {
    COLLAPSED,
    EXPANDED
}

private enum class MiniDismissDragPhase {
    IDLE,
    TENSION,
    SNAPPING,
    FREE_DRAG
}

// ==========================================
// 2. STEP 2: DYNAMIC SHAPE & SQUIRCLE MORPHING
// ==========================================
class PlayerSheetDynamicShape(
    private val topRadiusProvider: () -> Dp,
    private val bottomRadiusProvider: () -> Dp,
    private val virtualHeightProvider: () -> Float
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val topRadius = topRadiusProvider().nonNegative()
        val bottomRadius = bottomRadiusProvider().nonNegative()

        // Fall back to standard high-performance rounded corner shape
        val topRadiusPx = with(density) { topRadius.toPx() }
        val bottomRadiusPx = with(density) { bottomRadius.toPx() }
        val virtualHeight = virtualHeightProvider()
        return Outline.Rounded(
            RoundRect(
                rect = Rect(0f, 0f, size.width, virtualHeight),
                topLeft = CornerRadius(topRadiusPx, topRadiusPx),
                topRight = CornerRadius(topRadiusPx, topRadiusPx),
                bottomRight = CornerRadius(bottomRadiusPx, bottomRadiusPx),
                bottomLeft = CornerRadius(bottomRadiusPx, bottomRadiusPx)
            )
        )
    }
}

private fun Dp.nonNegative(): Dp = takeIf { it.value.isFinite() && it.value > 0f } ?: 0.dp

// ==========================================
// 3. STEP 3: DRAW-PHASE & LAYOUT-PHASE PROVIDERS
// ==========================================
val MiniPlayerHeight = 80.dp // High-fidelity capsule height matching controls breathing room

data class SheetVisualState(
    val currentBottomPadding: Dp,
    val playerContentAreaHeightPxProvider: () -> Float,
    val visualSheetTranslationYProvider: () -> Float,
    val overallSheetTopCornerRadiusProvider: () -> Dp,
    val playerContentActualBottomRadiusProvider: () -> Dp,
    val currentHorizontalPaddingStartPxProvider: () -> Float,
    val currentHorizontalPaddingEndPxProvider: () -> Float
)

@Composable
fun rememberSheetVisualState(
    showPlayerContentArea: Boolean,
    collapsedStateHorizontalPadding: Dp,
    predictiveBackCollapseProgress: Float,
    playerContentExpansionFraction: Animatable<Float, AnimationVector1D>,
    containerHeight: Dp,
    currentSheetTranslationY: Animatable<Float, AnimationVector1D>,
    sheetCollapsedTargetYProvider: () -> Float,
    isNavBarHiddenProvider: () -> Boolean,
    navBarCornerRadiusDp: Dp
): SheetVisualState {
    
    val currentBottomPadding by remember(showPlayerContentArea, collapsedStateHorizontalPadding, predictiveBackCollapseProgress) {
        derivedStateOf {
            if (predictiveBackCollapseProgress > 0f && showPlayerContentArea) {
                lerp(0.dp, collapsedStateHorizontalPadding, predictiveBackCollapseProgress)
            } else {
                0.dp
            }
        }
    }

    val density = LocalDensity.current
    val miniHeightPx = remember(density) { with(density) { MiniPlayerHeight.toPx() } }
    val containerHeightPx = remember(containerHeight, density) { with(density) { containerHeight.toPx() } }
    
    // Dynamic height provider (Layout-phase)
    val playerContentAreaHeightPxProvider: () -> Float = remember(showPlayerContentArea, playerContentExpansionFraction) {
        {
            if (showPlayerContentArea) {
                androidx.compose.ui.util.lerp(miniHeightPx, containerHeightPx, playerContentExpansionFraction.value)
            } else {
                0f
            }
        }
    }

    val predictiveBackCollapseProgressState = rememberUpdatedState(predictiveBackCollapseProgress)
    val visualSheetTranslationYProvider: () -> Float = remember(currentSheetTranslationY) {
        {
            val progress = predictiveBackCollapseProgressState.value
            val collapsedY = sheetCollapsedTargetYProvider()
            currentSheetTranslationY.value * (1f - progress) + (collapsedY * progress)
        }
    }

    // Dynamic top corners logic (lerps from pill corner to 0.dp)
    val overallSheetTopCornerRadiusProvider: () -> Dp = remember(showPlayerContentArea, playerContentExpansionFraction, navBarCornerRadiusDp, isNavBarHiddenProvider) {
        {
            if (showPlayerContentArea) {
                val collapsedCornerTarget = 40.dp // Perfect capsule: half of 80.dp height
                val fraction = playerContentExpansionFraction.value
                lerp(collapsedCornerTarget, 0.dp, fraction)
            } else {
                40.dp
            }
        }
    }

    // Dynamic bottom corners logic (morphs down to 0.dp)
    val playerContentActualBottomRadiusProvider: () -> Dp = remember(showPlayerContentArea, playerContentExpansionFraction, isNavBarHiddenProvider, navBarCornerRadiusDp) {
        {
            val fraction = playerContentExpansionFraction.value
            val collapsedRadius = 40.dp // Perfect capsule: half of 80.dp height
            
            // Morphs outward to 26.dp in first 20% drag to form curved floating card, then goes flat
            if (fraction < 0.2f) {
                lerp(collapsedRadius, 26.dp, (fraction / 0.2f).coerceIn(0f, 1f))
            } else {
                lerp(26.dp, 0.dp, ((fraction - 0.2f) / 0.8f).coerceIn(0f, 1f))
            }
        }
    }

    val collapsedStateHorizontalPaddingPx = remember(collapsedStateHorizontalPadding, density) {
        with(density) { collapsedStateHorizontalPadding.toPx() }
    }

    // Draw-phase padding providers
    val currentHorizontalPaddingStartPxProvider: () -> Float = remember(showPlayerContentArea, playerContentExpansionFraction, collapsedStateHorizontalPaddingPx) {
        {
            androidx.compose.ui.util.lerp(collapsedStateHorizontalPaddingPx, 0f, playerContentExpansionFraction.value)
        }
    }
    
    val currentHorizontalPaddingEndPxProvider: () -> Float = remember(showPlayerContentArea, playerContentExpansionFraction, collapsedStateHorizontalPaddingPx) {
        {
            androidx.compose.ui.util.lerp(collapsedStateHorizontalPaddingPx, 0f, playerContentExpansionFraction.value)
        }
    }

    return remember(
        currentBottomPadding,
        playerContentAreaHeightPxProvider,
        visualSheetTranslationYProvider,
        overallSheetTopCornerRadiusProvider,
        playerContentActualBottomRadiusProvider,
        currentHorizontalPaddingStartPxProvider,
        currentHorizontalPaddingEndPxProvider
    ) {
        SheetVisualState(
            currentBottomPadding = currentBottomPadding,
            playerContentAreaHeightPxProvider = playerContentAreaHeightPxProvider,
            visualSheetTranslationYProvider = visualSheetTranslationYProvider,
            overallSheetTopCornerRadiusProvider = overallSheetTopCornerRadiusProvider,
            playerContentActualBottomRadiusProvider = playerContentActualBottomRadiusProvider,
            currentHorizontalPaddingStartPxProvider = currentHorizontalPaddingStartPxProvider,
            currentHorizontalPaddingEndPxProvider = currentHorizontalPaddingEndPxProvider
        )
    }
}

// ==========================================
// 4. STEP 4: STACKING & CROSS-FADING
// ==========================================
@Composable
fun BoxScope.UnifiedPlayerMiniAndFullLayers(
    activity: MainActivity,
    viewModel: PlayerSharedViewModel,
    uiState: PlayerUiState,
    songList: List<SongItem>,
    baseAccentColor: Color,
    context: Context,
    playerContentExpansionFraction: Animatable<Float, AnimationVector1D>,
    containerHeight: Dp,
    onCollapse: () -> Unit,
    onExpand: () -> Unit,
    dragModifier: Modifier,
    onTabletPlaceholderPositioned: (Offset) -> Unit = {},
    sheetRootCoordinates: LayoutCoordinates? = null
) {
    val currentSong = uiState.currentSong ?: return
    val isPlaying = uiState.isPlaying

    val miniPlayerZIndex by remember {
        derivedStateOf { if (playerContentExpansionFraction.value < 0.5f) 1f else 0f }
    }
    val fullPlayerZIndex by remember {
        derivedStateOf { if (playerContentExpansionFraction.value >= 0.5f) 1f else 0f }
    }
    val isMiniPlayerVisible by remember {
        derivedStateOf { playerContentExpansionFraction.value < 0.6f }
    }
    val isFullPlayerActive by remember {
        derivedStateOf { playerContentExpansionFraction.value > 0.001f }
    }

    // Mini Player Container
    Box(
        modifier = Modifier
            .align(Alignment.TopCenter)
            .fillMaxWidth()
            .height(MiniPlayerHeight)
            .graphicsLayer {
                // Cross-fades fully to 0.0 alpha at 50% expansion
                alpha = (1f - playerContentExpansionFraction.value * 2f).coerceIn(0f, 1f)
            }
            .zIndex(miniPlayerZIndex)
    ) {
        if (isMiniPlayerVisible) {
            MiniPlayerContentInternal(
                viewModel = viewModel,
                currentSong = currentSong,
                isPlaying = isPlaying,
                isProcessing = uiState.isProcessing,
                accentColor = baseAccentColor,
                onClick = onExpand,
                modifier = Modifier.fillMaxSize()
            )
        }
    }

    // Full Player Container
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .requiredHeight(containerHeight)
            .graphicsLayer {
                val fraction = playerContentExpansionFraction.value
                // Fades in smoothly past 50% expansion
                alpha = ((fraction - 0.5f) * 2f).coerceIn(0f, 1f)
                val scale = androidx.compose.ui.util.lerp(0.972f, 1f, fraction)
                scaleX = scale
                scaleY = scale
                
                // Shift offscreen when collapsed to protect touch targets without causing layout invalidation
                translationY = if (fraction <= 0.01f) {
                    10000f
                } else {
                    androidx.compose.ui.util.lerp(100f, 0f, fraction)
                }
            }
            .zIndex(fullPlayerZIndex)
    ) {
        if (isFullPlayerActive) {
            FullPlayerScreen(
                activity = activity,
                viewModel = viewModel,
                uiState = uiState,
                songList = songList,
                accentColor = baseAccentColor,
                context = context,
                onCollapse = onCollapse,
                dragModifier = dragModifier,
                onTabletPlaceholderPositioned = onTabletPlaceholderPositioned,
                sheetRootCoordinates = sheetRootCoordinates,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

// ==========================================
// 5. STEP 5: VERTICAL GESTURE HANDLER & MATH
// ==========================================
data class SheetVerticalDragFrame(
    val translationY: Float,
    val expansionFraction: Float
)

fun computeSheetVerticalDragFrame(
    accumulatedDragY: Float,
    expandedY: Float,
    collapsedY: Float,
    miniHeightPx: Float,
    initialFractionOnDragStart: Float,
    initialYOnDragStart: Float
): SheetVerticalDragFrame {
    // Clamped strictly to expandedY to prevent vertical rubber-band/upward drift when fully expanded
    val newY = (initialYOnDragStart + accumulatedDragY)
        .coerceIn(
            expandedY,
            collapsedY + miniHeightPx * 0.2f
        )
    val denominator = (collapsedY - expandedY).coerceAtLeast(1f)
    val dragRatio = (initialYOnDragStart - newY) / denominator
    val newFraction = (initialFractionOnDragStart + dragRatio).coerceIn(0f, 1f)
    return SheetVerticalDragFrame(translationY = newY, expansionFraction = newFraction)
}

fun resolveVerticalSheetTargetState(
    currentSheetContentState: PlayerSheetState,
    accumulatedDragY: Float,
    minDragThresholdPx: Float,
    verticalVelocity: Float,
    velocityThreshold: Float,
    currentFraction: Float
): PlayerSheetState {
    // 1. High velocity fling takes priority
    if (abs(verticalVelocity) > velocityThreshold) {
        return if (verticalVelocity < 0f) PlayerSheetState.EXPANDED else PlayerSheetState.COLLAPSED
    }

    // 2. Position and drag delta based threshold
    return if (currentSheetContentState == PlayerSheetState.EXPANDED) {
        // If dragged down from EXPANDED by noticeable distance and not pulled back up, collapse it
        if (currentFraction < 0.90f || accumulatedDragY > minDragThresholdPx * 4f) {
            PlayerSheetState.COLLAPSED
        } else {
            PlayerSheetState.EXPANDED
        }
    } else {
        // If dragged up from COLLAPSED by noticeable distance and not pulled back down, expand it
        if (currentFraction > 0.10f || accumulatedDragY < -minDragThresholdPx * 4f) {
            PlayerSheetState.EXPANDED
        } else {
            PlayerSheetState.COLLAPSED
        }
    }
}

fun collapseSpringDampingForFraction(currentFraction: Float): Float {
    return Spring.DampingRatioNoBouncy
}

fun collapseInitialSquashForFraction(currentFraction: Float): Float {
    return 1.0f
}

class SheetVerticalDragGestureHandler(
    private val scope: CoroutineScope,
    private val velocityTracker: VelocityTracker,
    private val densityProvider: () -> Density,
    private val playerContentExpansionFraction: Animatable<Float, AnimationVector1D>,
    private val currentSheetTranslationY: Animatable<Float, AnimationVector1D>,
    private val expandedYProvider: () -> Float,
    private val collapsedYProvider: () -> Float,
    private val miniHeightPxProvider: () -> Float,
    private val currentSheetStateProvider: () -> PlayerSheetState,
    private val visualOvershootScaleY: Animatable<Float, AnimationVector1D>,
    private val onAnimateSheet: suspend (targetExpanded: Boolean, animationSpec: AnimationSpec<Float>?, initialVelocity: Float) -> Unit,
    private val onExpandSheetState: () -> Unit,
    private val onCollapseSheetState: () -> Unit,
    private val onDragStateChange: (Boolean) -> Unit,
    private val onFractionChanged: (Float) -> Unit
) {
    private var initialFractionOnDragStart = 0f
    private var initialYOnDragStart = 0f
    private var accumulatedDragYSinceStart = 0f
    private var dragJob: Job? = null
    private var isActivelyDragging = false

    fun onDragStart() {
        android.util.Log.d("PlayerDrag", "onDragStart: currentFraction=${playerContentExpansionFraction.value}, currentY=${currentSheetTranslationY.value}")
        isActivelyDragging = true
        onDragStateChange(true)
        dragJob?.cancel()
        velocityTracker.resetTracking()
        initialFractionOnDragStart = playerContentExpansionFraction.value
        initialYOnDragStart = currentSheetTranslationY.value
        accumulatedDragYSinceStart = 0f
    }

    fun onVerticalDrag(uptimeMillis: Long, position: Offset, dragAmount: Float) {
        val isExpanded = currentSheetStateProvider() == PlayerSheetState.EXPANDED
        
        if (isExpanded) {
            // When dragging from EXPANDED:
            // If already at top and dragging up, ignore
            if (accumulatedDragYSinceStart <= 0f && dragAmount < 0f) {
                accumulatedDragYSinceStart = 0f
                return
            }
            // Allow dragging down (positive) and dragging back up (negative)
            accumulatedDragYSinceStart = (accumulatedDragYSinceStart + dragAmount).coerceAtLeast(0f)
        } else {
            // When dragging from COLLAPSED:
            // If already at bottom and dragging down, ignore
            if (accumulatedDragYSinceStart >= 0f && dragAmount > 0f) {
                accumulatedDragYSinceStart = 0f
                return
            }
            // Allow dragging up (negative) and dragging back down (positive)
            accumulatedDragYSinceStart = (accumulatedDragYSinceStart + dragAmount).coerceAtMost(0f)
        }

        val dragFrame = computeSheetVerticalDragFrame(
            accumulatedDragY = accumulatedDragYSinceStart,
            expandedY = expandedYProvider(),
            collapsedY = collapsedYProvider(),
            miniHeightPx = miniHeightPxProvider(),
            initialFractionOnDragStart = initialFractionOnDragStart,
            initialYOnDragStart = initialYOnDragStart
        )
        dragJob?.cancel()
        dragJob = scope.launch(start = CoroutineStart.UNDISPATCHED) {
            currentSheetTranslationY.snapTo(dragFrame.translationY)
            playerContentExpansionFraction.snapTo(dragFrame.expansionFraction)
            onFractionChanged(dragFrame.expansionFraction)
        }
        velocityTracker.addPosition(uptimeMillis, position)
    }

    fun onDragEnd(providedVelocity: Float? = null) {
        if (!isActivelyDragging) return
        isActivelyDragging = false
        onDragStateChange(false)
        dragJob?.cancel()
        val verticalVelocity = providedVelocity ?: velocityTracker.calculateVelocity().y
        val currentFraction = playerContentExpansionFraction.value
        val minDragThresholdPx = with(densityProvider()) { 5.dp.toPx() }

        val targetState = resolveVerticalSheetTargetState(
            currentSheetContentState = currentSheetStateProvider(),
            accumulatedDragY = accumulatedDragYSinceStart,
            minDragThresholdPx = minDragThresholdPx,
            verticalVelocity = verticalVelocity,
            velocityThreshold = 100f,
            currentFraction = currentFraction
        )

        android.util.Log.d("PlayerDrag", "onDragEnd: targetState=$targetState, accumulatedDragY=$accumulatedDragYSinceStart, velocity=$verticalVelocity, currentFraction=$currentFraction")

        scope.launch {
            if (targetState == PlayerSheetState.EXPANDED) {
                onExpandSheetState()
                onAnimateSheet(true, null, verticalVelocity)
            } else {
                onCollapseSheetState()
                onAnimateSheet(false, null, verticalVelocity)
            }
        }
    }
}

@Composable
fun Modifier.playerSheetVerticalDragGesture(
    enabled: Boolean,
    handler: SheetVerticalDragGestureHandler
): Modifier = this.draggable(
    orientation = androidx.compose.foundation.gestures.Orientation.Vertical,
    enabled = enabled,
    state = androidx.compose.foundation.gestures.rememberDraggableState { delta ->
        handler.onVerticalDrag(System.currentTimeMillis(), androidx.compose.ui.geometry.Offset.Zero, delta)
    },
    onDragStarted = { handler.onDragStart() },
    onDragStopped = { velocity -> handler.onDragEnd(velocity) }
)

// ==========================================
// 6. STEP 6: HORIZONTAL SWIPE-TO-DISMISS TENSION
// ==========================================
class MiniPlayerDismissGestureHandler(
    private val scope: CoroutineScope,
    private val density: Density,
    private val hapticFeedback: HapticFeedback,
    private val offsetAnimatable: Animatable<Float, AnimationVector1D>,
    private val screenWidthPx: Float,
    private val onDismissQueue: () -> Unit
) {
    private var dragPhase = MiniDismissDragPhase.IDLE
    private var accumulatedDragX = 0f
    private var offsetJob: Job? = null

    fun onDragStart() {
        dragPhase = MiniDismissDragPhase.TENSION
        accumulatedDragX = 0f
        offsetJob?.cancel()
    }

    fun onHorizontalDrag(dragAmount: Float) {
        accumulatedDragX += dragAmount
        val snapThresholdPx = 40f * density.density

        when (dragPhase) {
            MiniDismissDragPhase.TENSION -> {
                if (abs(accumulatedDragX) < snapThresholdPx) {
                    val maxTensionOffsetPx = 30f * density.density
                    val dragFraction = (abs(accumulatedDragX) / snapThresholdPx).coerceIn(0f, 1f)
                    val tensionOffset = androidx.compose.ui.util.lerp(0f, maxTensionOffsetPx, dragFraction)
                    offsetJob?.cancel()
                    offsetJob = scope.launch(start = CoroutineStart.UNDISPATCHED) {
                        offsetAnimatable.snapTo(tensionOffset * accumulatedDragX.sign)
                    }
                } else {
                    dragPhase = MiniDismissDragPhase.SNAPPING
                }
            }

            MiniDismissDragPhase.SNAPPING -> {
                // Triggers tactile haptic pulse upon snapping
                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                offsetJob?.cancel()
                offsetJob = scope.launch(start = CoroutineStart.UNDISPATCHED) {
                    offsetAnimatable.animateTo(
                        targetValue = accumulatedDragX,
                        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessLow)
                    )
                }
                dragPhase = MiniDismissDragPhase.FREE_DRAG
            }

            MiniDismissDragPhase.FREE_DRAG -> {
                offsetJob?.cancel()
                offsetJob = scope.launch(start = CoroutineStart.UNDISPATCHED) {
                    offsetAnimatable.animateTo(
                        targetValue = accumulatedDragX,
                        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessHigh)
                    )
                }
            }
            else -> Unit
        }
    }

    fun onDragEnd() {
        dragPhase = MiniDismissDragPhase.IDLE
        offsetJob?.cancel()
        val dismissThreshold = screenWidthPx * 0.4f
        
        if (abs(accumulatedDragX) > dismissThreshold) {
            val targetDismissOffset = if (accumulatedDragX < 0) -screenWidthPx else screenWidthPx
            offsetJob = scope.launch(start = CoroutineStart.UNDISPATCHED) {
                offsetAnimatable.animateTo(
                    targetValue = targetDismissOffset,
                    animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing)
                )
                onDismissQueue()
                offsetAnimatable.snapTo(0f)
            }
        } else {
            // Springs back smoothly to center with no bounce
            offsetJob = scope.launch(start = CoroutineStart.UNDISPATCHED) {
                offsetAnimatable.animateTo(
                    targetValue = 0f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMedium)
                )
            }
        }
    }
}

@Composable
fun rememberMiniPlayerDismissGestureHandler(
    scope: CoroutineScope,
    density: Density,
    hapticFeedback: HapticFeedback,
    offsetAnimatable: Animatable<Float, AnimationVector1D>,
    screenWidthPx: Float,
    onDismissQueue: () -> Unit
): MiniPlayerDismissGestureHandler {
    val onDismissQueueState = rememberUpdatedState(onDismissQueue)
    return remember(scope, density, hapticFeedback, offsetAnimatable, screenWidthPx) {
        MiniPlayerDismissGestureHandler(
            scope = scope,
            density = density,
            hapticFeedback = hapticFeedback,
            offsetAnimatable = offsetAnimatable,
            screenWidthPx = screenWidthPx,
            onDismissQueue = { onDismissQueueState.value() }
        )
    }
}

@Composable
fun Modifier.miniPlayerDismissHorizontalGesture(
    enabled: Boolean,
    handler: MiniPlayerDismissGestureHandler
): Modifier {
    if (!enabled) return this
    return this.draggable(
        orientation = androidx.compose.foundation.gestures.Orientation.Horizontal,
        enabled = enabled,
        state = androidx.compose.foundation.gestures.rememberDraggableState { delta ->
            handler.onHorizontalDrag(delta)
        },
        onDragStarted = { handler.onDragStart() },
        onDragStopped = { handler.onDragEnd() }
    )
}

// ==========================================
// 7. STEP 7: THE MASTER ASSEMBLY CONTAINER
// ==========================================
@RequiresApi(Build.VERSION_CODES.Q)
@SuppressLint("ConfigurationScreenWidthHeight")
@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalFoundationApi::class)
@Composable
fun PlayerBottomSheetCompose(
    activity: MainActivity,
    viewModel: PlayerSharedViewModel,
    modifier: Modifier = Modifier,
    isTablet: Boolean = false
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val context = LocalContext.current
        val density = LocalDensity.current
        val scope = rememberCoroutineScope()
        val hapticFeedback = LocalHapticFeedback.current
        
        // Playback state observers combined into stable UI model
        val currentSong by viewModel.currentSong.collectAsStateWithLifecycle()
        val videoAspectRatio by viewModel.videoAspectRatio.collectAsStateWithLifecycle()
        val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()
        val duration by viewModel.duration.collectAsStateWithLifecycle()
        val isProcessing by viewModel.isProcessing.collectAsStateWithLifecycle()
        val currentSongIndex by viewModel.currentSongIndex.collectAsStateWithLifecycle()
        val isHapticsEnabled by viewModel.isHapticsEnabled.collectAsStateWithLifecycle()
        val miniPlayerBlendColor by viewModel.miniPlayerBlendColor.collectAsStateWithLifecycle()
        val isCurrentSongFavorite by viewModel.isCurrentSongFavoriteFlow.collectAsStateWithLifecycle()
        val isEffectsExpanded by viewModel.isEffectsExpanded.collectAsStateWithLifecycle()
    val repeatMode by viewModel.repeatMode.collectAsStateWithLifecycle()
        val isShuffleEnabled by viewModel.isShuffleEnabled.collectAsStateWithLifecycle()
        val playerBackgroundColor by viewModel.playerBackgroundColor.collectAsStateWithLifecycle()
        val likesCount by viewModel.likesCountFlow.collectAsStateWithLifecycle()
        val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
        val dynamicBgColor = remember(playerBackgroundColor, isDark) {
            val baseColor = Color(playerBackgroundColor)
            val hsl = FloatArray(3)
            androidx.core.graphics.ColorUtils.colorToHSL(baseColor.toArgb(), hsl)
            val isMonochrome = hsl[1] < 0.06f
            if (isDark) {
                // Dark Theme: Set a premium, richly-colored dark solid background (Exactly 15.5% lightness for gorgeous color presence)
                hsl[2] = 0.155f
                hsl[1] = if (isMonochrome) 0f else hsl[1].coerceIn(0.32f, 0.54f)
                Color(androidx.core.graphics.ColorUtils.HSLToColor(hsl))
            } else {
                // Light Theme: Set a premium, elegant soft pastel solid background (Exactly 83.5% lightness for deep pastel flavor)
                hsl[2] = 0.835f
                hsl[1] = if (isMonochrome) 0f else hsl[1].coerceIn(0.30f, 0.48f)
                Color(androidx.core.graphics.ColorUtils.HSLToColor(hsl))
            }
        }
        val isCurrentSongDisliked by viewModel.isCurrentSongDisliked.collectAsStateWithLifecycle()
        val isCurrentSongDownloaded by viewModel.isCurrentSongDownloaded.collectAsStateWithLifecycle()
        val streamingQuality by viewModel.streamingQuality.collectAsStateWithLifecycle()
        val playbackFormat by viewModel.playbackFormat.collectAsStateWithLifecycle()
        val currentSongDownloadProgress by viewModel.currentSongDownloadProgress.collectAsStateWithLifecycle()
        val isLyricsModeEnabled by viewModel.isLyricsModeEnabled.collectAsStateWithLifecycle()
        val showSignInDialog by viewModel.showSignInDialog.collectAsStateWithLifecycle()
        if (showSignInDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.dismissSignInDialog() },
                title = {
                    Text(
                        text = "Sign in Required",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(
                        text = "To like or dislike songs on YouTube Music, please connect your account in Settings. This enables full synchronization of liked tracks and personalization algorithms.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.dismissSignInDialog()
                            activity.navigateToSettings()
                        }
                    ) {
                        Text(text = "Go to Settings", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.dismissSignInDialog() }) {
                        Text(text = "Cancel")
                    }
                }
            )
        }
        val configuration = androidx.compose.ui.platform.LocalConfiguration.current
        val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
        val isWideLandscape = isLandscape && configuration.screenWidthDp >= 600
        val isCompactLandscape = isLandscape && !isWideLandscape
        val isTabletTopLevel = isWideLandscape
        val lyricsArtworkProgress by animateFloatAsState(
            targetValue = if (isLyricsModeEnabled && !isTabletTopLevel) 1f else 0f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = 380f),
            label = "LyricsArtworkSharedElement"
        )
 
        val uiState = remember(
            currentSong, isPlaying, duration, isProcessing, currentSongIndex,
            isHapticsEnabled, miniPlayerBlendColor, isCurrentSongFavorite, isCurrentSongDisliked, repeatMode, isShuffleEnabled,
            playerBackgroundColor, likesCount, isCurrentSongDownloaded, currentSongDownloadProgress, streamingQuality, playbackFormat
        ) {
            PlayerUiState(
                currentSong = currentSong,
                isPlaying = isPlaying,
                duration = duration,
                isProcessing = isProcessing,
                currentSongIndex = currentSongIndex,
                isHapticsEnabled = isHapticsEnabled,
                miniPlayerBlendColor = miniPlayerBlendColor,
                isCurrentSongFavorite = isCurrentSongFavorite,
                isCurrentSongDisliked = isCurrentSongDisliked,
                repeatMode = repeatMode,
                isShuffleEnabled = isShuffleEnabled,
                playerBackgroundColor = playerBackgroundColor,
                likesCount = likesCount,
                isCurrentSongDownloaded = isCurrentSongDownloaded,
                currentSongDownloadProgress = currentSongDownloadProgress,
                streamingQuality = streamingQuality,
                playbackFormat = playbackFormat
            )
        }

        // List is separate from quick state changes
        val songList by viewModel.songList.collectAsStateWithLifecycle()
        val isQueueExpanded by viewModel.isQueueExpanded.collectAsStateWithLifecycle()
        val artworkAlpha by animateFloatAsState(
            targetValue = 1f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = 300f),
            label = "ArtworkAlpha"
        )

        // Sliding Layout States
        val containerHeight = with(density) { maxHeight.toPx().toDp() }
        val screenHeightPx = with(density) { containerHeight.toPx() }
        val bottomNavTranslationYState = viewModel.bottomNavTranslationY.collectAsStateWithLifecycle()
        val dynamicBottomNavHeightState = viewModel.bottomNavHeight.collectAsStateWithLifecycle()
        val navBarsBottomPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
        val defaultNavHeightPx = with(density) { (80.dp + navBarsBottomPadding + 16.dp).toPx() }
        
        // Zero-Recomposition logic: calculating layout targets inside derivedStateOf
        // This ensures the composable body doesn't re-run for every pixel of nav bar scroll
        val sheetCollapsedTargetYState = remember(screenHeightPx, density, isTablet, defaultNavHeightPx) {
            derivedStateOf {
                val bottomNavHeightPx = if (isTablet) {
                    0f
                } else if (dynamicBottomNavHeightState.value > 0f) {
                    dynamicBottomNavHeightState.value
                } else {
                    defaultNavHeightPx
                }
                val miniPlayerHeightPx = with(density) { MiniPlayerHeight.toPx() }
                
                // Dynamically adjust gap: 12dp when BottomNav is visible, 8dp when hidden
                val bottomNavVisibilityFraction = if (isTablet) 0f else (bottomNavTranslationYState.value / (if (bottomNavHeightPx > 0) bottomNavHeightPx else 1f)).coerceIn(0f, 1f)
                val currentBottomGapPx = with(density) { 
                    if (isTablet) 8.dp.toPx() else lerp(12.dp, 8.dp, bottomNavVisibilityFraction).toPx() 
                }
                
                // Ensure the mini player stops at the bottom margin and doesn't follow the nav bar into the abyss
                val effectiveBottomNavHeight = if (isTablet) 0f else (bottomNavHeightPx - bottomNavTranslationYState.value).coerceAtLeast(0f)
                screenHeightPx - miniPlayerHeightPx - effectiveBottomNavHeight - currentBottomGapPx
            }
        }

        // Animation States
        val playerContentExpansionFraction = remember { Animatable(0f) }
        val isPlaybackReady by viewModel.isPlaybackReady.collectAsStateWithLifecycle()

        var currentSheetContentState by remember { mutableStateOf(PlayerSheetState.COLLAPSED) }
        
        val currentSheetTranslationY = remember { Animatable(screenHeightPx) }
        val visualOvershootScaleY = remember { Animatable(1f) }
        val offsetAnimatable = remember { Animatable(0f) }

        val screenWidthPx = with(density) { maxWidth.toPx() }
        
        var isDragging by remember { mutableStateOf(false) }

        val showPlayerContentArea = currentSong != null

        val isPlayerExpandedExternal by viewModel.isPlayerExpanded.collectAsStateWithLifecycle()
        LaunchedEffect(isPlayerExpandedExternal) {
            val targetState = if (isPlayerExpandedExternal) PlayerSheetState.EXPANDED else PlayerSheetState.COLLAPSED
            if (currentSheetContentState != targetState && !isDragging) {
                currentSheetContentState = targetState
            }
        }

        LaunchedEffect(currentSheetContentState) {
            val isExp = currentSheetContentState == PlayerSheetState.EXPANDED
            if (viewModel.isPlayerExpanded.value != isExp) {
                viewModel.setPlayerExpanded(isExp)
            }
        }

        // Synchronize dynamic scrolling Bottom Nav visibility changes
        LaunchedEffect(Unit) {
            snapshotFlow { sheetCollapsedTargetYState.value }.collect { targetY ->
                if (currentSheetContentState == PlayerSheetState.COLLAPSED && !isDragging) {
                    if (currentSheetTranslationY.isRunning) {
                        currentSheetTranslationY.animateTo(
                            targetValue = targetY,
                            animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow)
                        )
                    } else {
                        currentSheetTranslationY.snapTo(targetY)
                    }
                }
            }
        }

        // Standard collapsed Y when bottom navigation bar is fully visible
        val computeNormalCollapsedY: () -> Float = remember(screenHeightPx, density, isTablet, defaultNavHeightPx) {
            {
                val bottomNavHeightPx = if (isTablet) {
                    0f
                } else if (dynamicBottomNavHeightState.value > 0f) {
                    dynamicBottomNavHeightState.value
                } else {
                    defaultNavHeightPx
                }
                val miniPlayerHeightPx = with(density) { MiniPlayerHeight.toPx() }
                val normalBottomGapPx = with(density) { if (isTablet) 8.dp.toPx() else 12.dp.toPx() }
                screenHeightPx - miniPlayerHeightPx - bottomNavHeightPx - normalBottomGapPx
            }
        }

        var sheetBackProgress by remember { mutableFloatStateOf(0f) }

        androidx.activity.compose.PredictiveBackHandler(enabled = currentSheetContentState == PlayerSheetState.EXPANDED && (isTablet || !isLyricsModeEnabled)) { progressFlow ->
            try {
                val normalCollapsedY = computeNormalCollapsedY()
                progressFlow.collect { backEvent ->
                    sheetBackProgress = backEvent.progress
                    val targetY = backEvent.progress * normalCollapsedY
                    currentSheetTranslationY.snapTo(targetY)
                }
                sheetBackProgress = 0f
                currentSheetContentState = PlayerSheetState.COLLAPSED
            } catch (e: java.util.concurrent.CancellationException) {
                sheetBackProgress = 0f
                currentSheetTranslationY.animateTo(
                    targetValue = 0f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow)
                )
            }
        }

        // Auto-expand/collapse sheet when currentSong is changed / loaded
        LaunchedEffect(currentSong) {
            if (currentSong != null) {
                if (currentSheetContentState == PlayerSheetState.COLLAPSED) {
                    currentSheetTranslationY.snapTo(computeNormalCollapsedY())
                }
            }
        }

        // Dynamic Blend Colors from Artwork
        val baseAccentColor = remember(miniPlayerBlendColor, isDark) {
            val rawAccent = if (miniPlayerBlendColor != 0) Color(miniPlayerBlendColor) else Color(0xFF8338EC)
            val hsl = FloatArray(3)
            androidx.core.graphics.ColorUtils.colorToHSL(rawAccent.toArgb(), hsl)
            if (hsl[1] < 0.05f) {
                // Monochromatic / Grayscale
                if (isDark) Color.White else Color(0xFF1C1B1F)
            } else {
                rawAccent
            }
        }

        // Resolves sheet animations safely via a single coroutine Job
        var sheetAnimationJob by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }

        fun triggerSheetAnimation(targetExpanded: Boolean, initialVelocity: Float = 0f) {
            android.util.Log.d("PlayerDrag", "triggerSheetAnimation: targetExpanded=$targetExpanded, velocity=$initialVelocity, currentY=${currentSheetTranslationY.value}, currentFraction=${playerContentExpansionFraction.value}")
            sheetAnimationJob?.cancel()
            sheetAnimationJob = scope.launch {
                val normalCollapsedY = computeNormalCollapsedY()
                val destY = if (targetExpanded) 0f else normalCollapsedY
                val destFraction = if (targetExpanded) 1f else 0f
                
                val initialFractionVelocity = if (normalCollapsedY > 0f) {
                    -initialVelocity / normalCollapsedY
                } else 0f
                
                android.util.Log.d("PlayerDrag", "Starting animations to destY=$destY, destFraction=$destFraction")
                try {
                    coroutineScope {
                        launch {
                            currentSheetTranslationY.animateTo(
                                targetValue = destY,
                                animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = 400f),
                                initialVelocity = initialVelocity
                            )
                            currentSheetTranslationY.snapTo(destY)
                            android.util.Log.d("PlayerDrag", "currentSheetTranslationY finished at ${currentSheetTranslationY.value}")
                        }
                        launch {
                            playerContentExpansionFraction.animateTo(
                                targetValue = destFraction,
                                animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = 400f),
                                initialVelocity = initialFractionVelocity
                            ) {
                                viewModel.setPlayerExpansionFraction(value)
                            }
                            viewModel.setPlayerExpansionFraction(destFraction)
                            android.util.Log.d("PlayerDrag", "playerContentExpansionFraction finished at ${playerContentExpansionFraction.value}")
                        }
                    }
                } catch (e: Exception) {
                    android.util.Log.d("PlayerDrag", "Animation cancelled/interrupted: ${e.message}")
                }
            }
        }

        LaunchedEffect(currentSheetContentState) {
            android.util.Log.d("PlayerDrag", "LaunchedEffect(currentSheetContentState): state=$currentSheetContentState, isDragging=$isDragging")
            if (!isDragging) {
                triggerSheetAnimation(currentSheetContentState == PlayerSheetState.EXPANDED)
            }
        }

        LaunchedEffect(playerContentExpansionFraction) {
            snapshotFlow { playerContentExpansionFraction.value }
                .collect { viewModel.setPlayerExpansionFraction(it) }
        }

        val sheetVisualState = rememberSheetVisualState(
            showPlayerContentArea = showPlayerContentArea,
            collapsedStateHorizontalPadding = 12.dp,
            predictiveBackCollapseProgress = sheetBackProgress,
            playerContentExpansionFraction = playerContentExpansionFraction,
            containerHeight = containerHeight,
            currentSheetTranslationY = currentSheetTranslationY,
            sheetCollapsedTargetYProvider = computeNormalCollapsedY,
            isNavBarHiddenProvider = { (bottomNavTranslationYState.value / (if (dynamicBottomNavHeightState.value > 0) dynamicBottomNavHeightState.value else 1f)) > 0.5f },
            navBarCornerRadiusDp = 32.dp
        )

        // Dynamic Shape instantiation
        val playerShadowShape = remember(sheetVisualState.overallSheetTopCornerRadiusProvider, sheetVisualState.playerContentActualBottomRadiusProvider, sheetVisualState.playerContentAreaHeightPxProvider) {
            PlayerSheetDynamicShape(
                topRadiusProvider = sheetVisualState.overallSheetTopCornerRadiusProvider,
                bottomRadiusProvider = sheetVisualState.playerContentActualBottomRadiusProvider,
                virtualHeightProvider = sheetVisualState.playerContentAreaHeightPxProvider
            )
        }

        val velocityTracker = remember { VelocityTracker() }
        
        val sheetVerticalDragGestureHandler = remember(density) {
            val miniHeightPx = with(density) { MiniPlayerHeight.toPx() }
            SheetVerticalDragGestureHandler(
                scope = scope,
                velocityTracker = velocityTracker,
                densityProvider = { density },
                playerContentExpansionFraction = playerContentExpansionFraction,
                currentSheetTranslationY = currentSheetTranslationY,
                expandedYProvider = { 0f },
                collapsedYProvider = computeNormalCollapsedY,
                miniHeightPxProvider = { miniHeightPx },
                currentSheetStateProvider = { currentSheetContentState },
                visualOvershootScaleY = visualOvershootScaleY,
                onAnimateSheet = { target, _, velocity -> triggerSheetAnimation(target, velocity) },
                onExpandSheetState = { currentSheetContentState = PlayerSheetState.EXPANDED },
                onCollapseSheetState = { currentSheetContentState = PlayerSheetState.COLLAPSED },
                onDragStateChange = { dragging -> isDragging = dragging },
                onFractionChanged = { viewModel.setPlayerExpansionFraction(it) }
            )
        }

        val miniDismissGestureHandler = rememberMiniPlayerDismissGestureHandler(
            scope = scope,
            density = density,
            hapticFeedback = hapticFeedback,
            offsetAnimatable = offsetAnimatable,
            screenWidthPx = screenWidthPx,
            onDismissQueue = {
                scope.launch {
                    currentSheetTranslationY.snapTo(screenHeightPx)
                }
                viewModel.dismissPlayer()
            }
        )

        if (showPlayerContentArea) {
            var sheetRootCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }
            var tabletPlaceholderOffset by remember { mutableStateOf<Offset?>(null) }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset { IntOffset(0, sheetVisualState.visualSheetTranslationYProvider().roundToInt()) }
                    .height(containerHeight),
                color = Color.Transparent
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = sheetVisualState.currentBottomPadding)
                        .onGloballyPositioned { sheetRootCoordinates = it }
                ) {
                    val verticalDragModifier = Modifier.playerSheetVerticalDragGesture(
                        enabled = currentSheetContentState == PlayerSheetState.EXPANDED && (isTablet || !isLyricsModeEnabled) && !isQueueExpanded,
                        handler = sheetVerticalDragGestureHandler
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .zIndex(2f)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                // Phase-Optimized Layout measuring (Zero-Recomposition)
                                .layout { measurable, constraints ->
                                    val targetHeightPx = sheetVisualState.playerContentAreaHeightPxProvider().toInt()
                                    val startPaddingPx = sheetVisualState.currentHorizontalPaddingStartPxProvider().toInt()
                                    val endPaddingPx = sheetVisualState.currentHorizontalPaddingEndPxProvider().toInt()
                                    val innerWidth = (constraints.maxWidth - startPaddingPx - endPaddingPx)
                                    
                                    val placeable = measurable.measure(
                                        constraints.copy(
                                            minWidth = innerWidth,
                                            maxWidth = innerWidth,
                                            minHeight = targetHeightPx,
                                            maxHeight = targetHeightPx
                                        )
                                    )
                                    layout(constraints.maxWidth, targetHeightPx) {
                                        placeable.placeRelative(startPaddingPx, 0)
                                    }
                                }

                                .graphicsLayer {
                                    translationX = offsetAnimatable.value
                                    scaleY = visualOvershootScaleY.value
                                    transformOrigin = TransformOrigin(0.5f, 1f)
                                }
                                .shadow(
                                    elevation = if (currentSheetContentState == PlayerSheetState.COLLAPSED) 6.dp else 0.dp,
                                    shape = playerShadowShape
                                )
                                .background(
                                    color = dynamicBgColor,
                                    shape = playerShadowShape
                                )
                                .then(
                                    if (currentSheetContentState == PlayerSheetState.COLLAPSED) {
                                        Modifier
                                            .playerSheetVerticalDragGesture(
                                                enabled = true,
                                                handler = sheetVerticalDragGestureHandler
                                            )
                                            .miniPlayerDismissHorizontalGesture(
                                                enabled = true,
                                                handler = miniDismissGestureHandler
                                            )
                                    } else {
                                        Modifier
                                    }
                                )
                                .zIndex(if (isQueueExpanded) 5f else 1f)
                        ) {
                            UnifiedPlayerMiniAndFullLayers(
                                activity = activity,
                                viewModel = viewModel,
                                uiState = uiState,
                                songList = songList,
                                baseAccentColor = baseAccentColor,
                                context = context,
                                playerContentExpansionFraction = playerContentExpansionFraction,
                                containerHeight = containerHeight,
                                onCollapse = { currentSheetContentState = PlayerSheetState.COLLAPSED },
                                onExpand = { currentSheetContentState = PlayerSheetState.EXPANDED },
                                dragModifier = verticalDragModifier,
                                onTabletPlaceholderPositioned = { tabletPlaceholderOffset = it },
                                sheetRootCoordinates = sheetRootCoordinates
                            )
                        }
                    }

                    // Integrated shared artwork layer. In lyrics mode the same ArtworkPager
                    // moves into the app bar as a compact thumbnail instead of being hidden.
                    val isMvMode by viewModel.isMvMode.collectAsStateWithLifecycle()
                    val hasMusicVideo by viewModel.hasMusicVideo.collectAsStateWithLifecycle()
                    val musicVideoUrl by viewModel.musicVideoUrl.collectAsStateWithLifecycle()
                    val isEffectiveMvMode = isMvMode && hasMusicVideo && !musicVideoUrl.isNullOrBlank()

                    val isMvFullscreen by viewModel.isMvFullscreen.collectAsStateWithLifecycle()
                    val isInPipMode by viewModel.isInPipMode.collectAsStateWithLifecycle()
                    val isTrueFullscreen = isMvFullscreen || isInPipMode

                    val statusBarTopPx = WindowInsets.statusBars.getTop(density).toFloat()
                    val navBarBottomPx = WindowInsets.navigationBars.getBottom(density).toFloat()

                    val screenWidth = with(density) { screenWidthPx.toDp() }
                    val screenHeight = with(density) { screenHeightPx.toDp() }
                    val isLandscape = LocalConfiguration.current.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
                    val isWideLandscape = isLandscape && screenWidth >= 600.dp
                    val isCompactLandscape = isLandscape && !isWideLandscape
                    val isTablet = isWideLandscape
                    val dimens = com.codetrio.overdrive.ui.theme.LocalDimens.current

                    val baseAlbumArtHeightDp = when {
                        isWideLandscape -> {
                            val availableWidthDp = screenWidth - (dimens.screenMargin * 2)
                            val rightPaneWidthDp = (availableWidthDp * 0.44f).coerceIn(320.dp, 540.dp)
                            val maxLeftWidth = availableWidthDp - rightPaneWidthDp - 24.dp
                            val maxLeftHeight = (screenHeight - with(density) { statusBarTopPx.toDp() } - 240.dp).coerceAtLeast(180.dp)
                            androidx.compose.ui.unit.min(maxLeftWidth * 0.88f, maxLeftHeight).coerceIn(180.dp, 440.dp)
                        }
                        isCompactLandscape -> {
                            androidx.compose.ui.unit.min(220.dp, (screenHeight - with(density) { statusBarTopPx.toDp() } - 36.dp).coerceAtLeast(140.dp))
                        }
                        else -> { // Portrait
                            if (screenWidth >= 600.dp) {
                                androidx.compose.ui.unit.min(screenWidth * 0.65f, screenHeight * 0.40f).coerceIn(260.dp, 480.dp)
                            } else {
                                val contentWidth = screenWidth - 44.dp
                                androidx.compose.ui.unit.min(contentWidth, screenHeight * 0.39f).coerceIn(220.dp, 350.dp)
                            }
                        }
                    }

                    val videoAspectRatio by viewModel.videoAspectRatio.collectAsStateWithLifecycle()

                    val targetAlbumArtWidthDp = if (isTrueFullscreen) {
                        screenWidth
                    } else {
                        baseAlbumArtHeightDp
                    }

                    val targetAlbumArtHeightDp = if (isTrueFullscreen) {
                        screenHeight
                    } else {
                        baseAlbumArtHeightDp
                    }
                    
                    val albumArtWidthDp by androidx.compose.animation.core.animateDpAsState(
                        targetValue = targetAlbumArtWidthDp,
                        animationSpec = androidx.compose.animation.core.spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = 380f),
                        label = "AlbumArtWidth"
                    )

                    val albumArtHeightDp by androidx.compose.animation.core.animateDpAsState(
                        targetValue = targetAlbumArtHeightDp,
                        animationSpec = androidx.compose.animation.core.spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = 380f),
                        label = "AlbumArtHeight"
                    )

                    val queueFadeAlpha by androidx.compose.animation.core.animateFloatAsState(
                        targetValue = if (isQueueExpanded) 1f else 0f,
                        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = 380f),
                        label = "QueueFadeAlpha"
                    )

                    val canvasArtwork by viewModel.canvasArtwork.collectAsStateWithLifecycle()
                    val prefs = remember { context.getSharedPreferences("AppSettings", Context.MODE_PRIVATE) }
                    var showAnimatedArt by remember { mutableStateOf(prefs.getBoolean("show_animated_art", true)) }
                    var playerTheme by remember { mutableStateOf(prefs.getString("player_theme", "fluid") ?: "fluid") }

                    DisposableEffect(prefs) {
                        val listener = SharedPreferences.OnSharedPreferenceChangeListener { sp, key ->
                            if (key == "player_theme") {
                                playerTheme = sp.getString(key, "fluid") ?: "fluid"
                            } else if (key == "show_animated_art") {
                                showAnimatedArt = sp.getBoolean(key, true)
                            }
                        }
                        prefs.registerOnSharedPreferenceChangeListener(listener)
                        onDispose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
                    }

                    val isStatic = playerTheme == "static"
                    val isVinyl = playerTheme == "vinyl"
                    val hasCanvas = !isStatic && !isVinyl && showAnimatedArt && canvasArtwork != null &&
                        (!canvasArtwork!!.preferredVerticalAnimationUrl.isNullOrBlank() || !canvasArtwork!!.preferredAnimationUrl.isNullOrBlank())

                    val miniSizePx = with(density) { 48.dp.toPx() }
                    val fullWidthPx = with(density) { albumArtWidthDp.toPx() }
                    val fullHeightPx = with(density) { albumArtHeightDp.toPx() }
                    val screenWidthPx = with(density) { screenWidth.toPx() }
                    val xStartOffsetPx = with(density) { 16.dp.toPx() }
                    val yStartPx = with(density) { 16.dp.toPx() }

                    val xEndPxTarget = remember(isTrueFullscreen, isTablet, isCompactLandscape, screenWidthPx, fullWidthPx, density, dimens, tabletPlaceholderOffset) {
                        if (isTrueFullscreen) {
                            0f
                        } else if (tabletPlaceholderOffset != null && isTablet) {
                            tabletPlaceholderOffset!!.x
                        } else if (isTablet || isCompactLandscape) {
                            val screenMarginPx = with(density) { dimens.screenMargin.toPx() }
                            val availableWidthPx = screenWidthPx - (2 * screenMarginPx)
                            val leftPaneWidthPx = availableWidthPx / 2f
                            screenMarginPx + (leftPaneWidthPx - fullWidthPx) / 2f
                        } else {
                            (screenWidthPx - fullWidthPx) / 2f
                        }
                    }
                    val xEndPx by animateFloatAsState(
                        targetValue = xEndPxTarget,
                        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = 380f),
                        label = "AlbumArtX"
                    )

                    val yEndPxTarget = remember(isTrueFullscreen, isTablet, isCompactLandscape, statusBarTopPx, navBarBottomPx, density, containerHeight, albumArtHeightDp, fullHeightPx, screenHeight, screenWidth, tabletPlaceholderOffset) {
                        if (isTrueFullscreen) {
                            0f
                        } else if (isTablet || isCompactLandscape) {
                            if (tabletPlaceholderOffset != null) {
                                tabletPlaceholderOffset!!.y
                            } else {
                                val availableHeightPx = screenHeightPx - statusBarTopPx - navBarBottomPx
                                val topMarginPx = ((availableHeightPx - fullHeightPx) / 2f).coerceAtLeast(0f)
                                statusBarTopPx + topMarginPx
                            }
                        } else {
                            val availableTopSpaceDp = screenHeight - with(density) { statusBarTopPx.toDp() } - 376.dp
                            val squareTopOffsetDp = with(density) { statusBarTopPx.toDp() } + ((availableTopSpaceDp - baseAlbumArtHeightDp) / 2f).coerceIn(12.dp, 28.dp)
                            val squareCenterYDp = squareTopOffsetDp + (baseAlbumArtHeightDp / 2f)
                            val currentArtHeightDp = baseAlbumArtHeightDp
                            val phoneTopOffsetDp = squareCenterYDp - (currentArtHeightDp / 2f)
                            with(density) { phoneTopOffsetDp.toPx() }
                        }
                    }
                    val yEndPx by animateFloatAsState(
                        targetValue = yEndPxTarget,
                        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = 380f),
                        label = "AlbumArtY"
                    )
                    val targetExpandedCornerRadiusPx = with(density) { 28.dp.toPx() }

                    val isArtworkPagerInteractive by remember(isTablet, isLyricsModeEnabled, isQueueExpanded) {
                        derivedStateOf {
                            playerContentExpansionFraction.value > 0.95f && (isTablet || !isLyricsModeEnabled) && !isQueueExpanded
                        }
                    }
                    val isCanvasAllowed by remember(isTablet, isLyricsModeEnabled) {
                        derivedStateOf {
                            playerContentExpansionFraction.value > 0.95f && (isTablet || !isLyricsModeEnabled) && lyricsArtworkProgress == 0f
                        }
                    }
                    val isTonearmVisible by remember {
                        derivedStateOf {
                            playerContentExpansionFraction.value > 0.85f && lyricsArtworkProgress == 0f
                        }
                    }
                    val isSpecialTheme = isVinyl || (playerTheme == "immersion")
                    val isArtworkDragEnabled by remember(isTablet, isLyricsModeEnabled, isQueueExpanded, currentSheetContentState) {
                        derivedStateOf {
                            currentSheetContentState == PlayerSheetState.EXPANDED && (isTablet || !isLyricsModeEnabled) && !isQueueExpanded
                        }
                    }
                    val artworkPagerZIndex by remember(isTablet, isLyricsModeEnabled, isQueueExpanded, isTrueFullscreen) {
                        derivedStateOf {
                            if (isTrueFullscreen) 10f
                            else if (!isTablet && (isLyricsModeEnabled || isQueueExpanded)) -1f
                            else 3f
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(width = albumArtWidthDp, height = albumArtHeightDp)
                            .graphicsLayer {
                                val t = playerContentExpansionFraction.value.coerceIn(0f, 1f)
                                val lyricsT = lyricsArtworkProgress

                                val motionT = androidx.compose.animation.core.FastOutSlowInEasing.transform(t)

                                val normalScale = androidx.compose.ui.util.lerp(miniSizePx / fullWidthPx, 1f, motionT)
                                val startPaddingPx = sheetVisualState.currentHorizontalPaddingStartPxProvider()
                                val xStartPx = startPaddingPx + xStartOffsetPx

                                val normalX = androidx.compose.ui.util.lerp(xStartPx, xEndPx, motionT)
                                val normalY = androidx.compose.ui.util.lerp(yStartPx, yEndPx, motionT)

                                val visualCornerRadiusPx = androidx.compose.ui.util.lerp(miniSizePx / 2f, targetExpandedCornerRadiusPx, motionT)
                                val unscaledCornerRadiusPx = visualCornerRadiusPx / normalScale.coerceAtLeast(0.001f)

                                val visualShadowElevationPx = androidx.compose.ui.util.lerp(0f, with(density) { 8.dp.toPx() }, motionT)
                                val unscaledShadowElevationPx = visualShadowElevationPx / normalScale.coerceAtLeast(0.001f)

                                val dismissOffsetPx = offsetAnimatable.value * (1f - t)

                                val queueEnlargeScale = if (fullWidthPx > 0f) screenWidthPx / fullWidthPx else 1f
                                val effectiveScale = if (!isSpecialTheme && !isTablet && t > 0.9f) {
                                    androidx.compose.ui.util.lerp(normalScale, queueEnlargeScale, queueFadeAlpha)
                                } else {
                                    normalScale
                                }

                                val effectiveX = if (!isSpecialTheme && !isTablet && t > 0.9f) {
                                    androidx.compose.ui.util.lerp(normalX + dismissOffsetPx, 0f, queueFadeAlpha)
                                } else {
                                    normalX + dismissOffsetPx
                                }

                                val effectiveY = if (!isSpecialTheme && !isTablet && t > 0.9f) {
                                    androidx.compose.ui.util.lerp(normalY, 0f, queueFadeAlpha)
                                } else {
                                    normalY
                                }

                                val effectiveCornerRadiusPx = if (!isSpecialTheme && !isTablet && t > 0.9f) {
                                    androidx.compose.ui.util.lerp(visualCornerRadiusPx, 0f, queueFadeAlpha) / effectiveScale.coerceAtLeast(0.001f)
                                } else {
                                    unscaledCornerRadiusPx
                                }

                                scaleX = effectiveScale
                                scaleY = effectiveScale
                                translationX = effectiveX
                                translationY = if (!isTablet && queueFadeAlpha >= 0.99f) 10000f else effectiveY
                                transformOrigin = TransformOrigin(0f, 0f)
                                compositingStrategy = androidx.compose.ui.graphics.CompositingStrategy.Offscreen

                                shape = if (isEffectiveMvMode) {
                                    RoundedCornerShape(0.dp)
                                } else {
                                    RoundedCornerShape(effectiveCornerRadiusPx)
                                }
                                clip = if (isEffectiveMvMode) false else if (isVinyl && t > 0.5f) false else true
                                shadowElevation = if (isEffectiveMvMode) 0f else if (isVinyl && t > 0.5f) 0f else if (!isSpecialTheme && queueFadeAlpha > 0.5f) 0f else unscaledShadowElevationPx

                                val isImmersionLike = playerTheme == "immersion" || playerTheme == "immersion-v2"
                                val immersionHideT = if (isImmersionLike && !isEffectiveMvMode) {
                                    ((t - 0.75f).coerceAtLeast(0f) * 4f) * (1f - lyricsT)
                                } else {
                                    0f
                                }
                                val canvasHideT = if (hasCanvas && !isQueueExpanded) {
                                    ((t - 0.75f).coerceAtLeast(0f) * 4f) * (1f - lyricsT)
                                } else {
                                    0f
                                }
                                val hideArtworkT = maxOf(canvasHideT, immersionHideT)
                                val lyricsFade = if (isTablet) 1f else (1f - lyricsT)
                                alpha = artworkAlpha * (1f - hideArtworkT) * lyricsFade * (1f - queueFadeAlpha)
                            }
                            .drawWithContent {
                                drawContent()
                                if (!isSpecialTheme && !isTablet && queueFadeAlpha > 0.01f) {
                                    drawRect(
                                        brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                                            colorStops = arrayOf(
                                                0.0f to Color.Black,
                                                0.45f to Color.Black,
                                                androidx.compose.ui.util.lerp(1.0f, 0.88f, queueFadeAlpha) to Color.Transparent,
                                                1.0f to Color.Transparent
                                            )
                                        ),
                                        blendMode = androidx.compose.ui.graphics.BlendMode.DstIn
                                    )
                                }
                            }
                            .zIndex(artworkPagerZIndex)
                            .then(if (isArtworkDragEnabled) verticalDragModifier else Modifier)
                    ) {
                        ArtworkPager(
                            viewModel = viewModel,
                            currentSong = currentSong!!,
                            songList = songList,
                            currentSongIndex = uiState.currentSongIndex,
                            context = context,
                            userScrollEnabled = isArtworkPagerInteractive,
                            allowCanvas = isCanvasAllowed,
                            showTonearm = isTonearmVisible,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    // --- Sliding Effects Drawer ---
                    val dynamicAccentColor = androidx.compose.material3.MaterialTheme.colorScheme.primary
                    
                    Box(modifier = Modifier.fillMaxSize().zIndex(4f)) {
                        SlidingEffectsDrawer(
                            isEffectsExpanded = isEffectsExpanded,
                            onEffectsExpandedChange = { viewModel.setEffectsExpanded(it) },
                            viewModel = viewModel,
                            playerBackgroundColor = playerBackgroundColor,
                            dynamicAccentColor = dynamicAccentColor,
                            isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
                        )
                    }
                }
            }
        }

        // Global Dynamic Snackbar

        val snackbarHostState = remember { androidx.compose.material3.SnackbarHostState() }
        
        LaunchedEffect(Unit) {
            com.codetrio.overdrive.ui.SnackbarController.events.collect { event ->
                snackbarHostState.showSnackbar(
                    com.codetrio.overdrive.ui.CustomSnackbarVisuals(
                        message = event.message,
                        duration = event.duration,
                        iconResId = event.iconResId,
                        iconVector = event.iconVector
                    )
                )
            }
        }
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .zIndex(100f) // Keep Snackbar always on top
        ) {
            androidx.compose.material3.SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset {
                        val bottomNavHeightPx = if (dynamicBottomNavHeightState.value > 0f) {
                            dynamicBottomNavHeightState.value
                        } else {
                            defaultNavHeightPx
                        }
                        val bottomNavVisibilityFraction = (bottomNavTranslationYState.value / (if (bottomNavHeightPx > 0) bottomNavHeightPx else 1f)).coerceIn(0f, 1f)
                        val currentBottomGapPx = with(density) { 
                            lerp(12.dp, 8.dp, bottomNavVisibilityFraction).toPx()
                        }
                        
                        val effectiveBottomNavHeight = (bottomNavHeightPx - bottomNavTranslationYState.value).coerceAtLeast(0f)
                        
                        var bottomOffsetPx = effectiveBottomNavHeight + with(density) { 16.dp.toPx() }
                        
                        if (showPlayerContentArea) {
                            val miniPlayerHeightPx = with(density) { MiniPlayerHeight.toPx() }
                            val collapsedOffset = effectiveBottomNavHeight + currentBottomGapPx + miniPlayerHeightPx + with(density) { 8.dp.toPx() }
                            val expandedOffset = with(density) { 120.dp.toPx() }
                            bottomOffsetPx = collapsedOffset + (expandedOffset - collapsedOffset) * playerContentExpansionFraction.value
                        }
                        
                        IntOffset(0, -bottomOffsetPx.roundToInt())
                    }
            ) { data ->
                androidx.compose.material3.Snackbar(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    containerColor = MaterialTheme.colorScheme.inverseSurface,
                    contentColor = MaterialTheme.colorScheme.inverseOnSurface,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    val customVisuals = data.visuals as? com.codetrio.overdrive.ui.CustomSnackbarVisuals
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 2.dp)
                    ) {
                        if (customVisuals?.iconVector != null) {
                            Icon(
                                imageVector = customVisuals.iconVector,
                                contentDescription = null,
                                modifier = Modifier.size(22.dp),
                                tint = MaterialTheme.colorScheme.inversePrimary
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                        } else if (customVisuals?.iconResId != null) {
                            Icon(
                                painter = painterResource(id = customVisuals.iconResId),
                                contentDescription = null,
                                modifier = Modifier.size(22.dp),
                                tint = MaterialTheme.colorScheme.inversePrimary
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                        }
                        Text(
                            text = data.visuals.message,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}


// ==========================================
// 8. MINI PLAYER ROW INTERNAL CONTENT
// ==========================================
@Composable
private fun MiniPlayerContentInternal(
    viewModel: PlayerSharedViewModel,
    currentSong: SongItem,
    isPlaying: Boolean,
    isProcessing: Boolean,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val contentColor = if (isDark) Color.White else Color(0xFF1C1B1F)
    val contentSecondary = if (isDark) Color.White.copy(alpha = 0.6f) else Color(0xFF1C1B1F).copy(alpha = 0.6f)
    val playBgColor = if (isDark) Color(0x1AFFFFFF) else Color(0x0D000000)

    Row(
        modifier = modifier
            .fillMaxSize()
            .clickable(onClick = onClick)
            .padding(start = 10.dp, end = 8.dp, top = 10.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Morphing Art Space
        Box(
            modifier = Modifier.size(60.dp),
            contentAlignment = Alignment.Center
        ) {
            val progressAnimatable = remember { Animatable(0f) }
            LaunchedEffect(Unit) {
                combine(viewModel.currentPosition, viewModel.duration) { pos, dur ->
                    if (dur > 0) (pos.toFloat() / dur.toFloat()).coerceIn(0f, 1f) else 0f
                }.distinctUntilChanged().collect { target ->
                    progressAnimatable.animateTo(
                        targetValue = target,
                        animationSpec = tween(durationMillis = 350, easing = LinearEasing)
                    )
                }
            }

            val amplitudeAnimatable = remember { Animatable(if (isPlaying) 1f else 0f) }
            LaunchedEffect(isPlaying) {
                amplitudeAnimatable.animateTo(
                    targetValue = if (isPlaying) 1f else 0f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
            }

            if (isProcessing) {
                CircularWavyProgressIndicator(
                    modifier = Modifier.size(58.dp),
                    color = accentColor,
                    trackColor = accentColor.copy(alpha = 0.25f),
                    amplitude = amplitudeAnimatable.value,
                    stroke = androidx.compose.ui.graphics.drawscope.Stroke(
                        width = with(LocalDensity.current) { 6.dp.toPx() },
                        cap = androidx.compose.ui.graphics.StrokeCap.Round
                    ),
                    trackStroke = androidx.compose.ui.graphics.drawscope.Stroke(
                        width = with(LocalDensity.current) { 6.dp.toPx() },
                        cap = androidx.compose.ui.graphics.StrokeCap.Round
                    )
                )
            } else {
                CircularWavyProgressIndicator(
                    progress = { progressAnimatable.value },
                    modifier = Modifier.size(58.dp),
                    color = accentColor,
                    trackColor = accentColor.copy(alpha = 0.25f),
                    waveSpeed = WavyProgressIndicatorDefaults.CircularWavelength * 0.4f,
                    amplitude = { amplitudeAnimatable.value },
                    stroke = androidx.compose.ui.graphics.drawscope.Stroke(
                        width = with(LocalDensity.current) { 6.dp.toPx() },
                        cap = androidx.compose.ui.graphics.StrokeCap.Round
                    ),
                    trackStroke = androidx.compose.ui.graphics.drawscope.Stroke(
                        width = with(LocalDensity.current) { 6.dp.toPx() },
                        cap = androidx.compose.ui.graphics.StrokeCap.Round
                    )
                )
            }

            Box(modifier = Modifier.size(48.dp)) {
                // Placeholder Box for structure: AsyncImage is managed globally via parent's floating shared element!
            }
        }

        Spacer(modifier = Modifier.width(20.dp))

        // Song Info
        val playerTitleFont = com.codetrio.overdrive.ui.theme.rememberCustomFontFamily(com.codetrio.overdrive.data.font.FontTarget.PLAYER_TITLE)

        Column(
            modifier = Modifier
                .weight(1f)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick
                )
        ) {
            Text(
                text = currentSong.title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = playerTitleFont,
                    fontWeight = FontWeight.Bold,
                    color = contentColor
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            val castState by viewModel.castState.collectAsStateWithLifecycle()
            val isCastConnected = castState is CastState.Connected

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                if (isCastConnected) {
                    val deviceName = (castState as CastState.Connected).deviceName
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { viewModel.showCastSheet() }
                            .padding(end = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CastConnected,
                            contentDescription = "Casting",
                            tint = accentColor,
                            modifier = Modifier
                                .size(13.dp)
                                .padding(end = 2.dp)
                        )
                        Text(
                            text = "$deviceName • ",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = playerTitleFont,
                                color = accentColor,
                                fontWeight = FontWeight.SemiBold
                            ),
                            maxLines = 1
                        )
                    }
                }
                Text(
                    text = currentSong.artist,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = playerTitleFont,
                        color = contentSecondary
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
            }
        }

        // Controls Row using custom icons painterResource
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { viewModel.playPreviousSong() }) {
                Icon(
                    painter = painterResource(R.drawable.ic_skip_previous),
                    contentDescription = "Previous",
                    tint = contentColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            IconButton(
                onClick = {
                    if (isPlaying) viewModel.pauseAudio() else viewModel.playAudio()
                },
                modifier = Modifier
                    .size(42.dp)
                    .background(playBgColor, CircleShape)
            ) {
                Icon(
                    painter = painterResource(if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play),
                    contentDescription = "Play/Pause",
                    tint = contentColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            IconButton(onClick = { viewModel.playNextSong() }) {
                Icon(
                    painter = painterResource(R.drawable.ic_skip_next),
                    contentDescription = "Next",
                    tint = contentColor,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
