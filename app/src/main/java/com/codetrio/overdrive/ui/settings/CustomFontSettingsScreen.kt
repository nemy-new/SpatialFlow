package com.codetrio.overdrive.ui.settings

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CloudDownload
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.FontDownload
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryScrollableTabRow
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.codetrio.overdrive.R
import com.codetrio.overdrive.data.font.CustomFontItem
import com.codetrio.overdrive.data.font.CustomFontManager
import com.codetrio.overdrive.data.font.FontTarget
import com.codetrio.overdrive.data.font.FontVariationConfig
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CustomFontSettingsScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val fontManager = remember(context) { CustomFontManager.getInstance(context) }

    val availableFonts by fontManager.availableFonts.collectAsStateWithLifecycle()
    val selectedFontIds by fontManager.selectedFontIds.collectAsStateWithLifecycle()
    val variationConfigs by fontManager.variationConfigs.collectAsStateWithLifecycle()
    val downloadingFontIds by fontManager.downloadingFontIds.collectAsStateWithLifecycle()

    var currentTarget by remember { mutableStateOf(FontTarget.GLOBAL) }
    var fontToDelete by remember { mutableStateOf<CustomFontItem?>(null) }

    // SAF Font file picker launcher (.ttf, .otf, .ttc, .woff2)
    val fontPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val result = fontManager.importFontFromUri(uri)
                if (result.isSuccess) {
                    val item = result.getOrNull()
                    Toast.makeText(context, "${item?.name ?: "Font"} をインポートしました", Toast.LENGTH_SHORT).show()
                    if (item != null) {
                        fontManager.setFontForTarget(currentTarget, item.id)
                    }
                } else {
                    Toast.makeText(context, "フォントのインポートに失敗しました", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    val selectedFontId = selectedFontIds[currentTarget] ?: CustomFontManager.BUILTIN_GOOGLE_SANS_FLEX
    val selectedFont = availableFonts.find { it.id == selectedFontId }
    val currentConfig = variationConfigs[currentTarget] ?: FontVariationConfig()

    // Live preview FontFamily
    val previewFontFamily = remember(selectedFont, currentConfig) {
        if (selectedFont != null) {
            fontManager.createPreviewFontFamily(selectedFont, currentConfig)
        } else {
            FontFamily.Default
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.setting_custom_fonts),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 400.dp)
        ) {
            // ── 1. Target Selector (SecondaryScrollableTabRow) ───────────
            NativeTargetTabRow(
                selectedTarget = currentTarget,
                onTargetSelected = { target ->
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    currentTarget = target
                },
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
            )

            // ── 2. Live Preview Section ──────────────────────────────────
            SettingsHeader(stringResource(R.string.font_preview_title))
            NativePreviewCard(
                target = currentTarget,
                fontName = selectedFont?.name ?: "Google Sans Flex",
                previewFontFamily = previewFontFamily,
                config = currentConfig
            )

            // ── 3. Variable Font Tuning Section (Native Group Card) ──────
            if (selectedFont?.isVariable == true) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.font_variable_tuning),
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                fontManager.resetVariationConfig(currentTarget)
                            }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.RestartAlt,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stringResource(R.string.font_reset_defaults),
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                SettingsGroupCard(buildList {
                    // 1. Weight (wght)
                    val wghtAxis = selectedFont.supportedAxes.find { it.tag == "wght" }
                    val minW = wghtAxis?.minValue ?: 100f
                    val maxW = wghtAxis?.maxValue ?: 1000f
                    add {
                        NativeSliderListItem(
                            title = stringResource(R.string.font_weight),
                            value = currentConfig.weight,
                            valueRange = minW..maxW,
                            onValueChange = { fontManager.setVariationConfig(currentTarget, currentConfig.copy(weight = it)) }
                        )
                    }

                    // 2. Width (wdth)
                    val wdthAxis = selectedFont.supportedAxes.find { it.tag == "wdth" }
                    if (wdthAxis != null) {
                        add {
                            NativeSliderListItem(
                                title = stringResource(R.string.font_width),
                                value = currentConfig.width,
                                valueRange = wdthAxis.minValue..wdthAxis.maxValue,
                                onValueChange = { fontManager.setVariationConfig(currentTarget, currentConfig.copy(width = it)) },
                                unit = "%"
                            )
                        }
                    }

                    // 3. Slant (slnt)
                    val slntAxis = selectedFont.supportedAxes.find { it.tag == "slnt" }
                    if (slntAxis != null) {
                        add {
                            NativeSliderListItem(
                                title = stringResource(R.string.font_slant),
                                value = currentConfig.slant,
                                valueRange = slntAxis.minValue..slntAxis.maxValue,
                                onValueChange = { fontManager.setVariationConfig(currentTarget, currentConfig.copy(slant = it)) },
                                unit = "°"
                            )
                        }
                    }

                    // 4. Roundness (ROND)
                    val rondAxis = selectedFont.supportedAxes.find { it.tag == "ROND" }
                    if (rondAxis != null) {
                        add {
                            NativeSliderListItem(
                                title = stringResource(R.string.font_roundness),
                                value = currentConfig.roundness,
                                valueRange = rondAxis.minValue..rondAxis.maxValue,
                                onValueChange = { fontManager.setVariationConfig(currentTarget, currentConfig.copy(roundness = it)) },
                                unit = "%"
                            )
                        }
                    }

                    // 5. Optical Size (opsz)
                    val opszAxis = selectedFont.supportedAxes.find { it.tag == "opsz" }
                    if (opszAxis != null) {
                        add {
                            NativeSliderListItem(
                                title = stringResource(R.string.font_optical_size),
                                value = currentConfig.opticalSize,
                                valueRange = opszAxis.minValue..opszAxis.maxValue,
                                onValueChange = { fontManager.setVariationConfig(currentTarget, currentConfig.copy(opticalSize = it)) },
                                unit = "pt"
                            )
                        }
                    }
                })
            }

            // ── 4. Available Fonts Section (Native Group Card) ────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "利用可能なフォント",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )

                FilledTonalButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        fontPickerLauncher.launch(arrayOf("font/*", "application/font-sfnt", "application/x-font-ttf", "application/x-font-otf", "*/*"))
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("追加", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                }
            }

            SettingsGroupCard(buildList {
                availableFonts.forEach { fontItem ->
                    val isSelected = fontItem.id == selectedFontId
                    add {
                        NativeFontListItem(
                            font = fontItem,
                            isSelected = isSelected,
                            onSelect = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                fontManager.setFontForTarget(currentTarget, fontItem.id)
                            },
                            onDelete = if (!fontItem.isBuiltIn) {
                                { fontToDelete = fontItem }
                            } else null
                        )
                    }
                }
            })

            // ── 5. Recommended Japanese Cloud Fonts Section ─────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 28.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.font_cloud_catalog_title),
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Text(
                text = stringResource(R.string.font_cloud_catalog_desc),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            SettingsGroupCard(buildList {
                CustomFontManager.cloudJapaneseFonts.forEach { cloudFont ->
                    val isDownloaded = fontManager.isCloudFontDownloaded(cloudFont.id)
                    val downloadedItem = if (isDownloaded) fontManager.getDownloadedFontItem(cloudFont.id) else null
                    val isSelected = downloadedItem?.id == selectedFontId
                    val isDownloading = downloadingFontIds.contains(cloudFont.id)

                    add {
                        NativeCloudFontListItem(
                            cloudFont = cloudFont,
                            isDownloaded = isDownloaded,
                            isSelected = isSelected,
                            isDownloading = isDownloading,
                            onDownloadClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                scope.launch {
                                    val result = fontManager.downloadCloudFont(cloudFont)
                                    if (result.isSuccess) {
                                        val item = result.getOrNull()
                                        Toast.makeText(context, context.getString(R.string.font_download_success, cloudFont.name), Toast.LENGTH_SHORT).show()
                                        if (item != null) {
                                            fontManager.setFontForTarget(currentTarget, item.id)
                                        }
                                    } else {
                                        Toast.makeText(context, context.getString(R.string.font_download_failure, cloudFont.name), Toast.LENGTH_LONG).show()
                                    }
                                }
                            },
                            onApplyClick = {
                                if (downloadedItem != null) {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    fontManager.setFontForTarget(currentTarget, downloadedItem.id)
                                }
                            }
                        )
                    }
                }
            })
        }
    }

    // Delete Confirmation Dialog
    fontToDelete?.let { font ->
        AlertDialog(
            onDismissRequest = { fontToDelete = null },
            title = { Text(stringResource(R.string.font_delete_title)) },
            text = { Text(stringResource(R.string.font_delete_message, font.name)) },
            confirmButton = {
                Button(
                    onClick = {
                        fontManager.deleteFont(font.id)
                        fontToDelete = null
                        Toast.makeText(context, "フォントを削除しました", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(stringResource(R.string.action_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { fontToDelete = null }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NativeTargetTabRow(
    selectedTarget: FontTarget,
    onTargetSelected: (FontTarget) -> Unit,
    modifier: Modifier = Modifier
) {
    val entries = FontTarget.entries
    val selectedIndex = entries.indexOf(selectedTarget)

    SecondaryScrollableTabRow(
        selectedTabIndex = selectedIndex,
        edgePadding = 0.dp,
        containerColor = Color.Transparent,
        divider = {},
        indicator = {
            TabRowDefaults.SecondaryIndicator(
                modifier = Modifier.tabIndicatorOffset(selectedTabIndex = selectedIndex),
                color = MaterialTheme.colorScheme.primary
            )
        },
        modifier = modifier.fillMaxWidth()
    ) {
        entries.forEachIndexed { index, target ->
            val isSelected = index == selectedIndex
            Tab(
                selected = isSelected,
                onClick = { onTargetSelected(target) },
                text = {
                    Text(
                        text = stringResource(target.titleRes),
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        ),
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            )
        }
    }
}

@Composable
private fun NativePreviewCard(
    target: FontTarget,
    fontName: String,
    previewFontFamily: FontFamily,
    config: FontVariationConfig,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.FontDownload,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = stringResource(target.titleRes),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = fontName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = "wght ${config.weight.toInt()}",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
            Spacer(modifier = Modifier.height(16.dp))

            // Sample typography text rendered in the preview font
            when (target) {
                FontTarget.LYRICS -> {
                    Text(
                        text = "溢れ出す想いと旋律が、\n心を満たしていく。",
                        fontFamily = previewFontFamily,
                        fontSize = 22.sp,
                        fontWeight = FontWeight(config.weight.toInt().coerceIn(100, 1000)),
                        lineHeight = 32.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Let the sound wash over you in perfect harmony.",
                        fontFamily = previewFontFamily,
                        fontSize = 15.sp,
                        fontWeight = FontWeight(config.weight.toInt().coerceIn(100, 1000)),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                FontTarget.PLAYER_TITLE -> {
                    Text(
                        text = "夜に駆ける / WannaCry",
                        fontFamily = previewFontFamily,
                        fontSize = 24.sp,
                        fontWeight = FontWeight(config.weight.toInt().coerceIn(100, 1000)),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "YOASOBI • Porter Robinson",
                        fontFamily = previewFontFamily,
                        fontSize = 16.sp,
                        fontWeight = FontWeight(config.weight.toInt().coerceIn(100, 1000)),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                FontTarget.HEADINGS -> {
                    Text(
                        text = "もう一度聴く / お気に入りの曲",
                        fontFamily = previewFontFamily,
                        fontSize = 22.sp,
                        fontWeight = FontWeight(config.weight.toInt().coerceIn(100, 1000)),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "TOP CHARTS & EXPLORE 2026",
                        fontFamily = previewFontFamily,
                        fontSize = 14.sp,
                        fontWeight = FontWeight(config.weight.toInt().coerceIn(100, 1000)),
                        letterSpacing = 1.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                FontTarget.GLOBAL -> {
                    Text(
                        text = "OverDrive Audiophile 2026",
                        fontFamily = previewFontFamily,
                        fontSize = 20.sp,
                        fontWeight = FontWeight(config.weight.toInt().coerceIn(100, 1000)),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "美しく響く次世代ハイレゾ・オーディオ体験をあなたに。",
                        fontFamily = previewFontFamily,
                        fontSize = 14.sp,
                        fontWeight = FontWeight(config.weight.toInt().coerceIn(100, 1000)),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "0123456789 ABCDEFGHIJKLMNOPQRSTUVWXYZ",
                        fontFamily = previewFontFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight(config.weight.toInt().coerceIn(100, 1000)),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Composable
private fun NativeSliderListItem(
    title: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit,
    unit: String = "",
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Text(
                    text = "${value.toInt()}$unit",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }
        Slider(
            value = value.coerceIn(valueRange.start, valueRange.endInclusive),
            onValueChange = onValueChange,
            valueRange = valueRange,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

@Composable
private fun NativeFontListItem(
    font: CustomFontItem,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onDelete: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    ListItem(
        headlineContent = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = font.name,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                if (font.isVariable) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.tertiaryContainer
                    ) {
                        Text(
                            text = stringResource(R.string.font_variable_badge),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            maxLines = 1,
                            softWrap = false,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        },
        supportingContent = {
            val subtitle = if (font.isBuiltIn) {
                "組み込みシステムフォント"
            } else {
                val sizeKb = font.fileSize / 1024
                "カスタムフォント • ${sizeKb} KB"
            }
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        leadingContent = {
            RadioButton(
                selected = isSelected,
                onClick = null
            )
        },
        trailingContent = if (onDelete != null) {
            {
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Rounded.DeleteOutline,
                        contentDescription = "Delete font",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        } else null,
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        modifier = modifier.clickable(onClick = onSelect)
    )
}

@Composable
private fun NativeCloudFontListItem(
    cloudFont: com.codetrio.overdrive.data.font.CloudFontItem,
    isDownloaded: Boolean,
    isSelected: Boolean,
    isDownloading: Boolean,
    onDownloadClick: () -> Unit,
    onApplyClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ListItem(
        headlineContent = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = cloudFont.name,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                if (cloudFont.isVariable) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.tertiaryContainer
                    ) {
                        Text(
                            text = stringResource(R.string.font_variable_badge),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            maxLines = 1,
                            softWrap = false,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        },
        supportingContent = {
            Column(modifier = Modifier.padding(top = 2.dp)) {
                Text(
                    text = "${cloudFont.category} • ${cloudFont.description}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "「${cloudFont.sampleText}」",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    ),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f)
                )
            }
        },
        leadingContent = {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceVariant
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isDownloaded) Icons.Rounded.Check else Icons.Rounded.CloudDownload,
                    contentDescription = null,
                    tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        },
        trailingContent = {
            when {
                isDownloading -> {
                    Box(
                        modifier = Modifier.size(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.5.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                isDownloaded && isSelected -> {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = stringResource(R.string.font_applied_badge),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
                isDownloaded && !isSelected -> {
                    FilledTonalButton(
                        onClick = onApplyClick,
                        shape = RoundedCornerShape(14.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.font_apply_action),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
                else -> {
                    Button(
                        onClick = onDownloadClick,
                        shape = RoundedCornerShape(14.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Download,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${cloudFont.estimatedSizeMb}MB",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        modifier = modifier.clickable {
            if (isDownloaded) onApplyClick() else onDownloadClick()
        }
    )
}

@Composable
private fun SettingsHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp, bottom = 8.dp)
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun SettingsGroupCard(items: List<@Composable () -> Unit>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)
    ) {
        items.forEachIndexed { index, item ->
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = getSettingsSegmentedShape(index = index, count = items.size),
                color = MaterialTheme.colorScheme.surfaceContainerHigh
            ) {
                item()
            }
        }
    }
}

private fun getSettingsSegmentedShape(index: Int, count: Int): Shape {
    val outer = 32.dp
    val inner = 4.dp
    return when {
        count <= 1 -> RoundedCornerShape(outer)
        index == 0 -> RoundedCornerShape(topStart = outer, topEnd = outer, bottomStart = inner, bottomEnd = inner)
        index == count - 1 -> RoundedCornerShape(topStart = inner, topEnd = inner, bottomStart = outer, bottomEnd = outer)
        else -> RoundedCornerShape(inner)
    }
}
