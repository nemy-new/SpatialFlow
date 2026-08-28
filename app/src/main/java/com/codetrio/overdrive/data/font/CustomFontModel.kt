package com.codetrio.overdrive.data.font

import androidx.annotation.FontRes
import androidx.annotation.StringRes
import com.codetrio.overdrive.R

/**
 * Targets in the application where custom typography/fonts can be applied.
 */
enum class FontTarget(
    val key: String,
    @StringRes val titleRes: Int,
    @StringRes val descriptionRes: Int
) {
    GLOBAL(
        key = "font_global",
        titleRes = R.string.font_target_global,
        descriptionRes = R.string.font_target_global_desc
    ),
    LYRICS(
        key = "font_lyrics",
        titleRes = R.string.font_target_lyrics,
        descriptionRes = R.string.font_target_lyrics_desc
    ),
    PLAYER_TITLE(
        key = "font_player_title",
        titleRes = R.string.font_target_player_title,
        descriptionRes = R.string.font_target_player_title_desc
    ),
    HEADINGS(
        key = "font_headings",
        titleRes = R.string.font_target_headings,
        descriptionRes = R.string.font_target_headings_desc
    )
}

/**
 * Definition of a Variable Font OpenType axis (e.g. wght, wdth, slnt, ROND, opsz).
 */
data class VariableAxis(
    val tag: String,
    val name: String,
    val minValue: Float,
    val maxValue: Float,
    val defaultValue: Float
)

/**
 * Representation of an available font in the app (either built-in or user-imported).
 */
data class CustomFontItem(
    val id: String,
    val name: String,
    val postScriptName: String? = null,
    val isBuiltIn: Boolean = false,
    val isVariable: Boolean = false,
    val filePath: String? = null,
    @FontRes val resId: Int? = null,
    val fileSize: Long = 0L,
    val supportedAxes: List<VariableAxis> = emptyList()
)

/**
 * Variation axis configuration for a specific font target.
 */
data class FontVariationConfig(
    val weight: Float = 400f,
    val width: Float = 100f,
    val slant: Float = 0f,
    val roundness: Float = 100f,
    val opticalSize: Float = 14f,
    val customAxes: Map<String, Float> = emptyMap()
) {
    fun toVariationSettingsList(): List<Pair<String, Float>> {
        val list = mutableListOf<Pair<String, Float>>()
        list.add("wght" to weight)
        list.add("wdth" to width)
        if (slant != 0f) list.add("slnt" to slant)
        if (roundness != 0f) list.add("ROND" to roundness)
        if (opticalSize > 0f) list.add("opsz" to opticalSize)
        customAxes.forEach { (tag, value) ->
            if (list.none { it.first == tag }) {
                list.add(tag to value)
            }
        }
        return list
    }
}

/**
 * Japanese Cloud Font available for one-tap download from Google Fonts.
 */
data class CloudFontItem(
    val id: String,
    val name: String,
    val category: String,
    val description: String,
    val sampleText: String = "音楽と、生きていく。",
    val isVariable: Boolean,
    val downloadUrl: String,
    val fileName: String,
    val estimatedSizeMb: Float
)
