package com.codetrio.overdrive.ui.player

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color

/**
 * Global singleton that holds the three reactive palette color slots extracted
 * from the current album artwork. Any composable in the app can observe these
 * without re-extracting the palette.
 *
 * Mirrors MediaViewModelObject.palette* from the design spec.
 */
object PlayerPaletteState {
    // Spec §2.1: initialized to rich ambient color; updated by LaunchedEffect in FullPlayer
    val vibrantColor: MutableState<Color>      = mutableStateOf(Color(0xFF2A3A30))
    val lightVibrantColor: MutableState<Color> = mutableStateOf(Color(0xFF4A6B58))
    val darkVibrantColor: MutableState<Color>  = mutableStateOf(Color(0xFF1B2820))
    val mutedColor: MutableState<Color>        = mutableStateOf(Color(0xFF2F3E35))
    val darkMutedColor: MutableState<Color>    = mutableStateOf(Color(0xFF141E18))
    val dominantColor: MutableState<Color>     = mutableStateOf(Color(0xFF24332A))
    val accentColor: MutableState<Color>       = mutableStateOf(Color(0xFF3A86FF))
}
