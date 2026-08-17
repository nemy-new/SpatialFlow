package com.codetrio.overdrive.ui.player

import android.content.Context
import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.edit
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.codetrio.overdrive.R
import com.codetrio.overdrive.ui.EqPreset
import com.codetrio.overdrive.ui.ExpressiveSwitch
import com.codetrio.overdrive.ui.ProcessingCard
import com.codetrio.overdrive.ui.ResponsiveSlider
import com.codetrio.overdrive.ui.predefinedEqPresets
import com.codetrio.overdrive.viewmodel.PlayerSharedViewModel
import kotlinx.coroutines.delay
import kotlin.math.abs

private enum class EffectPod {
    SPEED, BALANCE, PRESETS, SPATIAL, LOUDNESS, EQUALIZER
}

@Composable
fun SlidingEffectsDrawer(
    isEffectsExpanded: Boolean,
    onEffectsExpandedChange: (Boolean) -> Unit,
    viewModel: PlayerSharedViewModel,
    playerBackgroundColor: Int,
    dynamicAccentColor: Color,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp

    val slidingOffset by animateDpAsState(
        targetValue = if (isEffectsExpanded) 0.dp else screenHeight + 100.dp,
        animationSpec = spring(dampingRatio = 0.85f, stiffness = 320f),
        label = "EffectsSlidingOffset"
    )

    val effectsCornerRadius by animateDpAsState(
        targetValue = if (isEffectsExpanded) 32.dp else 0.dp,
        animationSpec = spring(dampingRatio = 0.85f, stiffness = 320f),
        label = "EffectsCornerRadius"
    )
    val safeCornerRadius = effectsCornerRadius.coerceAtLeast(0.dp)

    var selectedPod by rememberSaveable { mutableStateOf<EffectPod?>(null) }
    LaunchedEffect(isEffectsExpanded) {
        if (!isEffectsExpanded) {
            selectedPod = null
        }
    }

    if (!isEffectsExpanded && slidingOffset >= screenHeight) {
        return
    }

    val effectsBgColor = remember(playerBackgroundColor, isDark) {
        deriveArtworkSurfaceColor(
            sourceColor = Color(playerBackgroundColor),
            isDark = isDark,
            darkLightness = 0.12f,
            lightLightness = 0.90f,
            darkSaturationRange = 0.28f..0.48f,
            lightSaturationRange = 0.20f..0.40f
        )
    }

    val cardBgColor = remember(effectsBgColor, isDark) {
        deriveArtworkSurfaceColor(
            sourceColor = Color(playerBackgroundColor),
            isDark = isDark,
            darkLightness = 0.18f,
            lightLightness = 0.82f,
            darkSaturationRange = 0.28f..0.52f,
            lightSaturationRange = 0.18f..0.40f
        )
    }

    val contentColor = if (isDark) Color.White else Color.Black

    val isProcessing by viewModel.isProcessingFlow.collectAsStateWithLifecycle()
    val processingProgress by viewModel.processingProgressFlow.collectAsStateWithLifecycle()
    val is8DEnabled by viewModel.is8DEnabledFlow.collectAsStateWithLifecycle()
    val isEqualizerEnabled by viewModel.isEqualizerEnabledFlow.collectAsStateWithLifecycle()
    val isLoudnessEnabled by viewModel.isLoudnessEnabledFlow.collectAsStateWithLifecycle()
    val loudnessGain by viewModel.loudnessGainFlow.collectAsStateWithLifecycle()
    val balancePosition by viewModel.balanceFlow.collectAsStateWithLifecycle()
    val playbackSpeed by viewModel.playbackSpeedFlow.collectAsStateWithLifecycle()
    val isPitchMatched by viewModel.isPitchMatchedFlow.collectAsStateWithLifecycle()
    val eq1 by viewModel.eqBand1Flow.collectAsStateWithLifecycle()
    val eq2 by viewModel.eqBand2Flow.collectAsStateWithLifecycle()
    val eq3 by viewModel.eqBand3Flow.collectAsStateWithLifecycle()
    val eq4 by viewModel.eqBand4Flow.collectAsStateWithLifecycle()
    val eq5 by viewModel.eqBand5Flow.collectAsStateWithLifecycle()

    val bands = listOf(eq1.toFloat(), eq2.toFloat(), eq3.toFloat(), eq4.toFloat(), eq5.toFloat())

    var showProcessingCard by remember { mutableStateOf(false) }
    LaunchedEffect(isProcessing, processingProgress) {
        if (isProcessing) {
            showProcessingCard = true
        } else if (processingProgress >= 100) {
            delay(1200L)
            showProcessingCard = false
        } else {
            showProcessingCard = false
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .offset { IntOffset(0, slidingOffset.roundToPx()) }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onEffectsExpandedChange(false) },
        contentAlignment = Alignment.BottomCenter
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { /* Block clicks inside drawer from passing through */ }
                .pointerInput(Unit) {
                    detectVerticalDragGestures { change: androidx.compose.ui.input.pointer.PointerInputChange, dragAmount: Float ->
                        if (dragAmount > 8f) {
                            onEffectsExpandedChange(false)
                        }
                    }
                },
            shape = RoundedCornerShape(topStart = safeCornerRadius, topEnd = safeCornerRadius),
            color = effectsBgColor,
            border = BorderStroke(
                width = 1.dp,
                color = contentColor.copy(alpha = 0.12f)
            ),
            shadowElevation = 24.dp
        ) {
            CompositionLocalProvider(LocalContentColor provides contentColor) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize(animationSpec = spring(dampingRatio = 0.85f, stiffness = 320f))
                        .padding(bottom = 28.dp)
                ) {
                    // 1. Top Drag Handle
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onEffectsExpandedChange(false) }
                            .padding(top = 14.dp, bottom = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(width = 38.dp, height = 4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(contentColor.copy(alpha = 0.25f))
                        )
                    }

                    // 2. Studio Dashboard Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = androidx.compose.ui.res.stringResource(com.codetrio.overdrive.R.string.text_audio_effects),
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 1.5.sp
                                ),
                                color = contentColor.copy(alpha = 0.7f)
                            )
                            if (selectedPod != null) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = androidx.compose.ui.res.stringResource(com.codetrio.overdrive.R.string.text_tap_card_to_close),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = dynamicAccentColor
                                )
                            }
                        }
                        Text(
                            text = androidx.compose.ui.res.stringResource(com.codetrio.overdrive.R.string.text_reset_all),
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = dynamicAccentColor,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(dynamicAccentColor.copy(alpha = 0.14f))
                                .clickable {
                                    viewModel.setEqualizerEnabled(false)
                                    viewModel.set8DEnabled(false)
                                    viewModel.setReverbEnabled(false)
                                    viewModel.setLoudnessEnabled(false)
                                    viewModel.setPlaybackSpeed(1.0f)
                                    viewModel.setBalance(0f)
                                    viewModel.audioService?.setBalance(0f)
                                    viewModel.triggerEffectsRefresh()
                                }
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    AnimatedVisibility(
                        visible = showProcessingCard,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                            ProcessingCard(processingProgress)
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // 3. Compact 2x3 Grid of 6 Effect Pods
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Row 1: Speed, Balance, Presets
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            EffectPodCard(
                                pod = EffectPod.SPEED,
                                iconRes = R.drawable.ic_timer,
                                title = androidx.compose.ui.res.stringResource(com.codetrio.overdrive.R.string.text_speed),
                                subtitle = "${String.format(LocalLocale.current.platformLocale, "%.2fx", playbackSpeed)}",
                                isSelected = selectedPod == EffectPod.SPEED,
                                isActive = playbackSpeed != 1.0f || isPitchMatched,
                                onClick = { selectedPod = if (selectedPod == EffectPod.SPEED) null else EffectPod.SPEED },
                                contentColor = contentColor,
                                accentColor = dynamicAccentColor,
                                modifier = Modifier.weight(1f)
                            )
                            val balanceSubtitle = when {
                                abs(balancePosition) < 0.05f -> androidx.compose.ui.res.stringResource(com.codetrio.overdrive.R.string.text_center)
                                balancePosition < 0 -> "${androidx.compose.ui.res.stringResource(com.codetrio.overdrive.R.string.text_l_plus)}${(abs(balancePosition) * 10).toInt()}"
                                else -> "${androidx.compose.ui.res.stringResource(com.codetrio.overdrive.R.string.text_r_plus)}${(balancePosition * 10).toInt()}"
                            }
                            EffectPodCard(
                                pod = EffectPod.BALANCE,
                                iconRes = R.drawable.ic_settings,
                                title = androidx.compose.ui.res.stringResource(com.codetrio.overdrive.R.string.text_balance_effects),
                                subtitle = balanceSubtitle,
                                isSelected = selectedPod == EffectPod.BALANCE,
                                isActive = abs(balancePosition) >= 0.05f,
                                onClick = { selectedPod = if (selectedPod == EffectPod.BALANCE) null else EffectPod.BALANCE },
                                contentColor = contentColor,
                                accentColor = dynamicAccentColor,
                                modifier = Modifier.weight(1f)
                            )
                            val eqPresetName = if (isEqualizerEnabled) {
                                predefinedEqPresets.firstOrNull { it.bands == bands }?.name ?: androidx.compose.ui.res.stringResource(com.codetrio.overdrive.R.string.text_custom)
                            } else androidx.compose.ui.res.stringResource(com.codetrio.overdrive.R.string.text_flat_effects)
                            EffectPodCard(
                                pod = EffectPod.PRESETS,
                                iconRes = R.drawable.ic_music_note,
                                title = androidx.compose.ui.res.stringResource(com.codetrio.overdrive.R.string.text_presets_effects),
                                subtitle = eqPresetName,
                                isSelected = selectedPod == EffectPod.PRESETS,
                                isActive = isEqualizerEnabled,
                                onClick = { selectedPod = if (selectedPod == EffectPod.PRESETS) null else EffectPod.PRESETS },
                                contentColor = contentColor,
                                accentColor = dynamicAccentColor,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Row 2: Spatial, Loudness, Equalizer
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            EffectPodCard(
                                pod = EffectPod.SPATIAL,
                                iconRes = R.drawable.ic_headphones,
                                title = androidx.compose.ui.res.stringResource(com.codetrio.overdrive.R.string.text_spatial_effects),
                                subtitle = if (is8DEnabled) androidx.compose.ui.res.stringResource(com.codetrio.overdrive.R.string.text_360_active) else androidx.compose.ui.res.stringResource(com.codetrio.overdrive.R.string.text_standard_effects),
                                isSelected = selectedPod == EffectPod.SPATIAL,
                                isActive = is8DEnabled,
                                onClick = { selectedPod = if (selectedPod == EffectPod.SPATIAL) null else EffectPod.SPATIAL },
                                contentColor = contentColor,
                                accentColor = dynamicAccentColor,
                                modifier = Modifier.weight(1f)
                            )
                            EffectPodCard(
                                pod = EffectPod.LOUDNESS,
                                iconRes = R.drawable.ic_speaker,
                                title = androidx.compose.ui.res.stringResource(com.codetrio.overdrive.R.string.text_loudness_effects),
                                subtitle = if (isLoudnessEnabled) "+${(loudnessGain / 100).toInt()} dB" else androidx.compose.ui.res.stringResource(com.codetrio.overdrive.R.string.text_normal),
                                isSelected = selectedPod == EffectPod.LOUDNESS,
                                isActive = isLoudnessEnabled,
                                onClick = { selectedPod = if (selectedPod == EffectPod.LOUDNESS) null else EffectPod.LOUDNESS },
                                contentColor = contentColor,
                                accentColor = dynamicAccentColor,
                                modifier = Modifier.weight(1f)
                            )
                            EffectPodCard(
                                pod = EffectPod.EQUALIZER,
                                iconRes = R.drawable.ic_equalizer,
                                title = androidx.compose.ui.res.stringResource(com.codetrio.overdrive.R.string.text_equalizer),
                                subtitle = if (isEqualizerEnabled) androidx.compose.ui.res.stringResource(com.codetrio.overdrive.R.string.text_5_band_on) else androidx.compose.ui.res.stringResource(com.codetrio.overdrive.R.string.text_bypass),
                                isSelected = selectedPod == EffectPod.EQUALIZER,
                                isActive = isEqualizerEnabled,
                                onClick = { selectedPod = if (selectedPod == EffectPod.EQUALIZER) null else EffectPod.EQUALIZER },
                                contentColor = contentColor,
                                accentColor = dynamicAccentColor,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // 4. Interactive Control Deck (When Pod Selected)
                    AnimatedVisibility(
                        visible = selectedPod != null,
                        enter = expandVertically(animationSpec = spring(dampingRatio = 0.85f, stiffness = 300f)) + fadeIn(),
                        exit = shrinkVertically(animationSpec = spring(dampingRatio = 0.85f, stiffness = 300f)) + fadeOut()
                    ) {
                        val pod = selectedPod
                        if (pod != null) {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                                    .padding(top = 12.dp),
                                shape = RoundedCornerShape(22.dp),
                                color = cardBgColor.copy(alpha = 0.75f),
                                border = BorderStroke(1.dp, contentColor.copy(alpha = 0.1f))
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                ) {
                                    when (pod) {
                                        EffectPod.SPEED -> {
                                            CompactSpeedDeck(
                                                speed = playbackSpeed,
                                                onSpeedChange = { viewModel.setPlaybackSpeed(it) },
                                                isPitchMatched = isPitchMatched,
                                                onPitchMatchToggle = { viewModel.setPitchMatched(!isPitchMatched) },
                                                contentColor = contentColor,
                                                accentColor = dynamicAccentColor
                                            )
                                        }
                                        EffectPod.BALANCE -> {
                                            CompactBalanceDeck(
                                                balancePosition = balancePosition,
                                                onBalanceChange = { pos ->
                                                    viewModel.setBalance(pos)
                                                    viewModel.audioService?.setBalance(pos)
                                                },
                                                onResetCenter = {
                                                    viewModel.setBalance(0f)
                                                    viewModel.audioService?.setBalance(0f)
                                                },
                                                contentColor = contentColor,
                                                accentColor = dynamicAccentColor
                                            )
                                        }
                                        EffectPod.PRESETS -> {
                                            CompactPresetsDeck(
                                                bands = bands,
                                                onPresetSelect = { preset ->
                                                    viewModel.setEqualizerEnabled(true)
                                                    preset.bands.forEachIndexed { idx, v ->
                                                        val intVal = v.toInt()
                                                        when (idx) {
                                                            0 -> viewModel.setEqBand1(intVal)
                                                            1 -> viewModel.setEqBand2(intVal)
                                                            2 -> viewModel.setEqBand3(intVal)
                                                            3 -> viewModel.setEqBand4(intVal)
                                                            4 -> viewModel.setEqBand5(intVal)
                                                        }
                                                        viewModel.audioService?.setEqBandGain(idx, intVal)
                                                    }
                                                    viewModel.triggerEffectsRefresh()
                                                },
                                                enabled = isEqualizerEnabled,
                                                onToggleEq = { viewModel.setEqualizerEnabled(it) },
                                                contentColor = contentColor,
                                                accentColor = dynamicAccentColor
                                            )
                                        }
                                        EffectPod.SPATIAL -> {
                                            CompactSpatialDeck(
                                                is8DEnabled = is8DEnabled,
                                                onToggle8D = {
                                                    viewModel.set8DEnabled(it)
                                                    viewModel.triggerEffectsRefresh()
                                                },
                                                contentColor = contentColor,
                                                accentColor = dynamicAccentColor
                                            )
                                        }
                                        EffectPod.LOUDNESS -> {
                                            CompactLoudnessDeck(
                                                gain = loudnessGain.toFloat(),
                                                onGainChange = { gain ->
                                                    val intGain = gain.toInt()
                                                    viewModel.setLoudnessGain(intGain)
                                                    if (isLoudnessEnabled) viewModel.audioService?.setLoudnessGain(intGain)
                                                },
                                                enabled = isLoudnessEnabled,
                                                onToggle = { viewModel.setLoudnessEnabled(it) },
                                                contentColor = contentColor,
                                                accentColor = dynamicAccentColor
                                            )
                                        }
                                        EffectPod.EQUALIZER -> {
                                            CompactEqualizerDeck(
                                                bands = bands,
                                                onBandChange = { index, gain ->
                                                    val intGain = gain.toInt()
                                                    when (index) {
                                                        0 -> viewModel.setEqBand1(intGain)
                                                        1 -> viewModel.setEqBand2(intGain)
                                                        2 -> viewModel.setEqBand3(intGain)
                                                        3 -> viewModel.setEqBand4(intGain)
                                                        4 -> viewModel.setEqBand5(intGain)
                                                    }
                                                    if (isEqualizerEnabled) viewModel.audioService?.setEqBandGain(index, intGain)
                                                },
                                                enabled = isEqualizerEnabled,
                                                onToggle = { viewModel.setEqualizerEnabled(it) },
                                                contentColor = contentColor,
                                                accentColor = dynamicAccentColor
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
}

@Composable
private fun EffectPodCard(
    pod: EffectPod,
    iconRes: Int,
    title: String,
    subtitle: String,
    isSelected: Boolean,
    isActive: Boolean,
    onClick: () -> Unit,
    contentColor: Color,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.02f else 1f,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 400f),
        label = "PodScale"
    )
    val bgColor by animateColorAsState(
        targetValue = when {
            isSelected -> accentColor.copy(alpha = 0.22f)
            isActive -> contentColor.copy(alpha = 0.11f)
            else -> contentColor.copy(alpha = 0.05f)
        },
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 300f),
        label = "PodBgColor"
    )
    val borderColor by animateColorAsState(
        targetValue = when {
            isSelected -> accentColor
            isActive -> accentColor.copy(alpha = 0.5f)
            else -> contentColor.copy(alpha = 0.06f)
        },
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 300f),
        label = "PodBorderColor"
    )
    val textColor = if (isSelected || isActive) Color.White else contentColor

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isActive) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-10).dp, y = 4.dp)
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(accentColor)
            )
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = title,
                tint = if (isSelected || isActive) accentColor else contentColor.copy(alpha = 0.85f),
                modifier = Modifier.size(26.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                ),
                color = textColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 11.sp
                ),
                color = if (isActive) accentColor else contentColor.copy(alpha = 0.55f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun CompactSpeedDeck(
    speed: Float,
    onSpeedChange: (Float) -> Unit,
    isPitchMatched: Boolean,
    onPitchMatchToggle: () -> Unit,
    contentColor: Color,
    accentColor: Color
) {
    val speedChips = listOf(0.75f, 1.0f, 1.15f, 1.25f, 1.5f)
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = androidx.compose.ui.res.stringResource(com.codetrio.overdrive.R.string.text_playback_speed_pitch),
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = contentColor
            )
            Text(
                text = "${String.format(LocalLocale.current.platformLocale, "%.2fx", speed)}",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold),
                color = accentColor
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            speedChips.forEach { chipSpeed ->
                val isSelected = abs(speed - chipSpeed) < 0.01f
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) accentColor else contentColor.copy(alpha = 0.08f))
                        .clickable { onSpeedChange(chipSpeed) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${chipSpeed}x",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        ),
                        color = if (isSelected) Color.White else contentColor.copy(alpha = 0.85f)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        ResponsiveSlider(
            value = speed,
            onValueChange = onSpeedChange,
            valueRange = 0.5f..2.0f,
            enabled = true
        )
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(if (isPitchMatched) accentColor.copy(alpha = 0.15f) else contentColor.copy(alpha = 0.05f))
                .clickable { onPitchMatchToggle() }
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(if (isPitchMatched) accentColor else contentColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                if (isPitchMatched) {
                    Text("✓", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = androidx.compose.ui.res.stringResource(com.codetrio.overdrive.R.string.text_match_pitch_preserve_original_vocal_tone),
                style = MaterialTheme.typography.labelMedium,
                color = if (isPitchMatched) accentColor else contentColor.copy(alpha = 0.85f)
            )
        }
    }
}

@Composable
private fun CompactBalanceDeck(
    balancePosition: Float,
    onBalanceChange: (Float) -> Unit,
    onResetCenter: () -> Unit,
    contentColor: Color,
    accentColor: Color
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = androidx.compose.ui.res.stringResource(com.codetrio.overdrive.R.string.text_stereo_soundstage_balance),
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = contentColor
            )
            val status = when {
                abs(balancePosition) < 0.05f -> androidx.compose.ui.res.stringResource(com.codetrio.overdrive.R.string.text_center)
                balancePosition < 0 -> "${androidx.compose.ui.res.stringResource(com.codetrio.overdrive.R.string.text_left)} ${abs(balancePosition).toInt()}"
                else -> "${androidx.compose.ui.res.stringResource(com.codetrio.overdrive.R.string.text_right)} ${balancePosition.toInt()}"
            }
            Text(
                text = status,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold),
                color = accentColor
            )
        }
        Spacer(modifier = Modifier.height(14.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = androidx.compose.ui.res.stringResource(com.codetrio.overdrive.R.string.text_l),
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = if (balancePosition < -5f) accentColor else contentColor.copy(alpha = 0.6f),
                modifier = Modifier.width(24.dp)
            )
            Box(modifier = Modifier.weight(1f).padding(horizontal = 8.dp)) {
                ResponsiveSlider(
                    value = balancePosition,
                    onValueChange = onBalanceChange,
                    valueRange = -50f..50f,
                    enabled = true
                )
            }
            Text(
                text = androidx.compose.ui.res.stringResource(com.codetrio.overdrive.R.string.text_r),
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = if (balancePosition > 5f) accentColor else contentColor.copy(alpha = 0.6f),
                modifier = Modifier.width(24.dp),
                textAlign = TextAlign.End
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .clip(RoundedCornerShape(12.dp))
                .background(contentColor.copy(alpha = 0.08f))
                .clickable { onResetCenter() }
                .padding(horizontal = 20.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = androidx.compose.ui.res.stringResource(com.codetrio.overdrive.R.string.text_snap_to_center),
                style = MaterialTheme.typography.labelMedium,
                color = contentColor.copy(alpha = 0.9f)
            )
        }
    }
}

@Composable
private fun CompactPresetsDeck(
    bands: List<Float>,
    onPresetSelect: (EqPreset) -> Unit,
    enabled: Boolean,
    onToggleEq: (Boolean) -> Unit,
    contentColor: Color,
    accentColor: Color
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("eq_custom_presets", Context.MODE_PRIVATE) }
    var customPresetsStr by remember { mutableStateOf(prefs.getString("presets", "") ?: "") }

    val customPresets = remember(customPresetsStr) {
        if (customPresetsStr.isBlank()) emptyList()
        else {
            customPresetsStr.split(";;").mapNotNull { presetStr ->
                val parts = presetStr.split("|")
                if (parts.size == 2) {
                    val name = parts[0]
                    val bandVals = parts[1].split(",").mapNotNull { it.toFloatOrNull() }
                    if (bandVals.size == 5) EqPreset(name, bandVals) else null
                } else null
            }
        }
    }
    val allPresets = predefinedEqPresets + customPresets

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = androidx.compose.ui.res.stringResource(com.codetrio.overdrive.R.string.text_studio_eq_profiles),
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = contentColor
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (enabled) "Active" else "Bypassed",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (enabled) accentColor else contentColor.copy(alpha = 0.5f),
                    modifier = Modifier.padding(end = 8.dp)
                )
                ExpressiveSwitch(
                    checked = enabled,
                    onCheckedChange = onToggleEq,
                    enabled = true
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            allPresets.forEach { preset ->
                val isSelected = enabled && preset.bands == bands
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isSelected) accentColor else contentColor.copy(alpha = 0.08f))
                        .clickable { onPresetSelect(preset) }
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = preset.name,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        ),
                        color = if (isSelected) Color.White else contentColor.copy(alpha = 0.85f)
                    )
                }
            }
        }
    }
}

@Composable
private fun CompactSpatialDeck(
    is8DEnabled: Boolean,
    onToggle8D: (Boolean) -> Unit,
    contentColor: Color,
    accentColor: Color
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = androidx.compose.ui.res.stringResource(com.codetrio.overdrive.R.string.text_360_rotating_spatial_audio),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = contentColor
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (is8DEnabled) "Binaural virtualizer active" else "Standard stereo playback",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (is8DEnabled) accentColor else contentColor.copy(alpha = 0.6f)
                )
            }
            ExpressiveSwitch(
                checked = is8DEnabled,
                onCheckedChange = onToggle8D,
                enabled = true
            )
        }
        Spacer(modifier = Modifier.height(14.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(if (is8DEnabled) accentColor.copy(alpha = 0.15f) else contentColor.copy(alpha = 0.05f))
                .padding(14.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (is8DEnabled) {
                    "🎧 Immerse yourself in a dynamic soundstage that rotates 360° around your head. Recommended for headphones."
                } else {
                    "Tap switch to activate 8D multi-dimensional spatial immersion."
                },
                style = MaterialTheme.typography.bodySmall,
                color = if (is8DEnabled) accentColor else contentColor.copy(alpha = 0.75f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun CompactLoudnessDeck(
    gain: Float,
    onGainChange: (Float) -> Unit,
    enabled: Boolean,
    onToggle: (Boolean) -> Unit,
    contentColor: Color,
    accentColor: Color
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = androidx.compose.ui.res.stringResource(com.codetrio.overdrive.R.string.text_dynamic_loudness_enhancer),
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = contentColor
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (enabled) "+${(gain / 100).toInt()} dB" else "Off",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold),
                    color = if (enabled) accentColor else contentColor.copy(alpha = 0.5f),
                    modifier = Modifier.padding(end = 8.dp)
                )
                ExpressiveSwitch(
                    checked = enabled,
                    onCheckedChange = onToggle,
                    enabled = true
                )
            }
        }
        Spacer(modifier = Modifier.height(14.dp))
        ResponsiveSlider(
            value = gain,
            onValueChange = onGainChange,
            valueRange = 0f..1200f,
            enabled = enabled
        )
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val boostPills = listOf("Low (+3dB)" to 300f, "Med (+6dB)" to 600f, "Max (+12dB)" to 1200f)
            boostPills.forEach { (label, targetGain) ->
                val isSelected = enabled && abs(gain - targetGain) < 50f
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) accentColor else contentColor.copy(alpha = 0.08f))
                        .clickable {
                            if (!enabled) onToggle(true)
                            onGainChange(targetGain)
                        }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        ),
                        color = if (isSelected) Color.White else contentColor.copy(alpha = 0.85f)
                    )
                }
            }
        }
    }
}

@Composable
private fun CompactEqualizerDeck(
    bands: List<Float>,
    onBandChange: (Int, Float) -> Unit,
    enabled: Boolean,
    onToggle: (Boolean) -> Unit,
    contentColor: Color,
    accentColor: Color
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = androidx.compose.ui.res.stringResource(com.codetrio.overdrive.R.string.text_5_band_studio_equalizer),
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = contentColor
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (enabled) "Active" else "Bypassed",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (enabled) accentColor else contentColor.copy(alpha = 0.5f),
                    modifier = Modifier.padding(end = 8.dp)
                )
                ExpressiveSwitch(
                    checked = enabled,
                    onCheckedChange = onToggle,
                    enabled = true
                )
            }
        }
        Spacer(modifier = Modifier.height(14.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val freqs = listOf("60Hz", "230Hz", "910Hz", "3.6kHz", "14kHz")
            bands.forEachIndexed { index, value ->
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${value.toInt().let { if (it > 0) "+$it" else "$it" }}",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (value != 0f && enabled) accentColor else contentColor.copy(alpha = 0.5f),
                        fontSize = 10.sp
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        ResponsiveSlider(
                            value = value,
                            onValueChange = { onBandChange(index, it) },
                            valueRange = -12f..12f,
                            enabled = enabled,
                            modifier = Modifier
                                .graphicsLayer {
                                    rotationZ = 270f
                                    transformOrigin = TransformOrigin.Center
                                }
                                .requiredWidth(80.dp)
                        )
                    }
                    Text(
                        text = freqs[index],
                        style = MaterialTheme.typography.labelSmall,
                        color = contentColor.copy(alpha = 0.7f),
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}
