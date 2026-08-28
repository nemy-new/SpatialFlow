package com.codetrio.overdrive.ui.player.themes

import androidx.annotation.StringRes
import com.codetrio.overdrive.R

enum class PlayerThemeType(
    val id: String,
    @StringRes val titleRes: Int,
    @StringRes val descriptionRes: Int
) {
    FLUID(
        id = "fluid",
        titleRes = R.string.text_fluid_theme,
        descriptionRes = R.string.text_organic_mesh_gradient_with_drifting_colors_and_video_motion_art_when_available
    ),
    IMMERSION(
        id = "immersion",
        titleRes = R.string.text_immersion_theme,
        descriptionRes = R.string.text_immersion_theme_desc
    ),
    MESH(
        id = "mesh",
        titleRes = R.string.text_fluid_mesh_theme,
        descriptionRes = R.string.text_fluid_mesh_theme_desc
    ),
    VINYL(
        id = "vinyl",
        titleRes = R.string.text_vinyl_theme,
        descriptionRes = R.string.text_vinyl_theme_desc
    );

    companion object {
        fun fromId(id: String?): PlayerThemeType {
            return entries.firstOrNull { it.id == id } ?: FLUID
        }
    }
}

