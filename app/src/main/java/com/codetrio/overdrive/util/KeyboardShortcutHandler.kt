package com.codetrio.overdrive.util

import android.app.Activity
import android.content.Context
import android.media.AudioManager
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.codetrio.overdrive.viewmodel.PlayerSharedViewModel

/**
 * High-performance Keyboard Shortcut Handler for physical, Bluetooth, and tablet keyboards.
 * Fully supports media keys, arrow keys, navigation keys, and YouTube Music/Spotify standard hotkeys.
 */
object KeyboardShortcutHandler {

    private const val TAG = "KeyboardShortcutHandler"
    private var previousVolume: Int = -1

    /**
     * Inspects active focus to determine if user is currently typing in an input field.
     */
    fun isTextInputActive(activity: Activity): Boolean {
        val currentFocus = activity.currentFocus ?: return false
        
        // Check classic View-based text editors
        if (currentFocus is EditText) return true
        if (currentFocus is TextView && currentFocus.isTextSelectable) return true

        // Check Compose input / editable semantics if applicable
        val className = currentFocus.javaClass.name
        if (className.contains("TextField", ignoreCase = true) || 
            className.contains("TextInput", ignoreCase = true)) {
            return true
        }

        return false
    }

    /**
     * Intercepts and executes physical keyboard events.
     * Returns true if the key event was consumed by a shortcut.
     */
    fun handleKeyEvent(
        event: KeyEvent,
        activity: Activity,
        viewModel: PlayerSharedViewModel,
        navController: NavController?,
        onShowShortcutsHelp: () -> Unit
    ): Boolean {
        // Only trigger on ACTION_DOWN to avoid double execution
        if (event.action != KeyEvent.ACTION_DOWN) {
            return false
        }

        val keyCode = event.keyCode
        val metaState = event.metaState
        val isShiftPressed = event.isShiftPressed || (metaState and (KeyEvent.META_SHIFT_ON or KeyEvent.META_SHIFT_LEFT_ON or KeyEvent.META_SHIFT_RIGHT_ON)) != 0
        val isCtrlPressed = event.isCtrlPressed || (metaState and (KeyEvent.META_CTRL_ON or KeyEvent.META_CTRL_LEFT_ON or KeyEvent.META_CTRL_RIGHT_ON)) != 0
        val isAltPressed = event.isAltPressed || (metaState and (KeyEvent.META_ALT_ON or KeyEvent.META_ALT_LEFT_ON or KeyEvent.META_ALT_RIGHT_ON)) != 0
        val isMetaPressed = event.isMetaPressed
        val isModifierActive = isCtrlPressed || isAltPressed || isMetaPressed
        val isTyping = isTextInputActive(activity)

        // -------------------------------------------------------------
        // 1. HARDWARE MEDIA KEYS & GLOBAL COMMANDS (Always active)
        // -------------------------------------------------------------
        when (keyCode) {
            KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE,
            KeyEvent.KEYCODE_HEADSETHOOK -> {
                togglePlayPause(viewModel)
                return true
            }
            KeyEvent.KEYCODE_MEDIA_PLAY -> {
                viewModel.playAudio()
                return true
            }
            KeyEvent.KEYCODE_MEDIA_PAUSE -> {
                viewModel.pauseAudio()
                return true
            }
            KeyEvent.KEYCODE_MEDIA_NEXT -> {
                viewModel.playNextSong(force = true)
                return true
            }
            KeyEvent.KEYCODE_MEDIA_PREVIOUS -> {
                viewModel.playPreviousSong()
                return true
            }
            KeyEvent.KEYCODE_MEDIA_FAST_FORWARD -> {
                seekRelative(viewModel, 5000)
                return true
            }
            KeyEvent.KEYCODE_MEDIA_REWIND -> {
                seekRelative(viewModel, -5000)
                return true
            }
            KeyEvent.KEYCODE_VOLUME_MUTE,
            KeyEvent.KEYCODE_MUTE -> {
                toggleMute(activity)
                return true
            }
        }

        // -------------------------------------------------------------
        // 2. HELP DIALOG SHORTCUT: '?' (Shift + /) or 'Ctrl + /' or 'F1' or (non-typing) 'H'
        // -------------------------------------------------------------
        if ((isShiftPressed && keyCode == KeyEvent.KEYCODE_SLASH) ||
            (isCtrlPressed && keyCode == KeyEvent.KEYCODE_SLASH) ||
            keyCode == KeyEvent.KEYCODE_F1 ||
            (!isTyping && !isModifierActive && keyCode == KeyEvent.KEYCODE_H)) {
            onShowShortcutsHelp()
            return true
        }

        // -------------------------------------------------------------
        // 3. ESCAPE / BACK HANDLING
        // -------------------------------------------------------------
        if (keyCode == KeyEvent.KEYCODE_ESCAPE) {
            if (viewModel.isEffectsExpanded.value) {
                viewModel.setEffectsExpanded(false)
                return true
            }
            if (viewModel.isQueueExpanded.value) {
                viewModel.setQueueExpanded(false)
                return true
            }
            if (viewModel.isLyricsModeEnabled.value) {
                viewModel.setLyricsModeEnabled(false)
                return true
            }
            if (viewModel.isPlayerExpanded.value) {
                viewModel.setPlayerExpanded(false)
                return true
            }
            // Fallback: normal back press
            if (activity is androidx.activity.ComponentActivity) {
                activity.onBackPressedDispatcher.onBackPressed()
            } else {
                @Suppress("DEPRECATION")
                activity.onBackPressed()
            }
            return true
        }

        // -------------------------------------------------------------
        // 4. CONTROLLED / MODIFIED SHORTCUTS (Active even while focused, with modifier)
        // -------------------------------------------------------------
        if (isCtrlPressed || isAltPressed) {
            when (keyCode) {
                KeyEvent.KEYCODE_F -> {
                    navigateToTab(navController, "search")
                    return true
                }
                KeyEvent.KEYCODE_DPAD_RIGHT -> {
                    viewModel.playNextSong(force = true)
                    return true
                }
                KeyEvent.KEYCODE_DPAD_LEFT -> {
                    viewModel.playPreviousSong()
                    return true
                }
                KeyEvent.KEYCODE_1, KeyEvent.KEYCODE_NUMPAD_1 -> {
                    navigateToTab(navController, "explore")
                    return true
                }
                KeyEvent.KEYCODE_2, KeyEvent.KEYCODE_NUMPAD_2 -> {
                    navigateToTab(navController, "search")
                    return true
                }
                KeyEvent.KEYCODE_3, KeyEvent.KEYCODE_NUMPAD_3 -> {
                    navigateToTab(navController, "library")
                    return true
                }
                KeyEvent.KEYCODE_4, KeyEvent.KEYCODE_NUMPAD_4 -> {
                    navigateToTab(navController, "statistics")
                    return true
                }
                KeyEvent.KEYCODE_5, KeyEvent.KEYCODE_NUMPAD_5 -> {
                    navigateToTab(navController, "settings")
                    return true
                }
                KeyEvent.KEYCODE_M -> {
                    toggleMute(activity)
                    return true
                }
                KeyEvent.KEYCODE_L -> {
                    viewModel.toggleFavorite()
                    return true
                }
            }
        }

        // If user is currently typing in an active text input, do not intercept regular single keys!
        if (isTyping) {
            return false
        }

        // -------------------------------------------------------------
        // 5. SINGLE-KEY PLAYBACK & NAVIGATION SHORTCUTS (Non-typing mode)
        // -------------------------------------------------------------
        when (keyCode) {
            KeyEvent.KEYCODE_SPACE,
            KeyEvent.KEYCODE_K -> {
                togglePlayPause(viewModel)
                return true
            }

            KeyEvent.KEYCODE_J -> {
                seekRelative(viewModel, -5000)
                return true
            }

            KeyEvent.KEYCODE_L -> {
                seekRelative(viewModel, 5000)
                return true
            }

            KeyEvent.KEYCODE_DPAD_LEFT -> {
                if (isShiftPressed) {
                    viewModel.playPreviousSong()
                } else {
                    seekRelative(viewModel, -5000)
                }
                return true
            }

            KeyEvent.KEYCODE_DPAD_RIGHT -> {
                if (isShiftPressed) {
                    viewModel.playNextSong(force = true)
                } else {
                    seekRelative(viewModel, 5000)
                }
                return true
            }

            KeyEvent.KEYCODE_N -> {
                viewModel.playNextSong(force = true)
                return true
            }

            KeyEvent.KEYCODE_P -> {
                viewModel.playPreviousSong()
                return true
            }

            KeyEvent.KEYCODE_S -> {
                viewModel.toggleShuffle()
                return true
            }

            KeyEvent.KEYCODE_R -> {
                viewModel.toggleLoopMode()
                return true
            }

            KeyEvent.KEYCODE_F -> {
                viewModel.toggleFavorite()
                return true
            }

            KeyEvent.KEYCODE_T -> {
                val current = viewModel.isLyricsModeEnabled.value
                if (!current) {
                    viewModel.setPlayerExpanded(true)
                }
                viewModel.setLyricsModeEnabled(!current)
                return true
            }

            KeyEvent.KEYCODE_Q -> {
                val current = viewModel.isQueueExpanded.value
                if (!current) {
                    viewModel.setPlayerExpanded(true)
                }
                viewModel.setQueueExpanded(!current)
                return true
            }

            KeyEvent.KEYCODE_E -> {
                val current = viewModel.isEffectsExpanded.value
                viewModel.setEffectsExpanded(!current)
                return true
            }

            KeyEvent.KEYCODE_V -> {
                val current = viewModel.isPlayerExpanded.value
                viewModel.setPlayerExpanded(!current)
                return true
            }

            KeyEvent.KEYCODE_DPAD_UP -> {
                if (!viewModel.isPlayerExpanded.value) {
                    viewModel.setPlayerExpanded(true)
                    return true
                }
            }

            KeyEvent.KEYCODE_DPAD_DOWN -> {
                if (viewModel.isEffectsExpanded.value) {
                    viewModel.setEffectsExpanded(false)
                    return true
                }
                if (viewModel.isQueueExpanded.value) {
                    viewModel.setQueueExpanded(false)
                    return true
                }
                if (viewModel.isLyricsModeEnabled.value) {
                    viewModel.setLyricsModeEnabled(false)
                    return true
                }
                if (viewModel.isPlayerExpanded.value) {
                    viewModel.setPlayerExpanded(false)
                    return true
                }
            }

            KeyEvent.KEYCODE_M -> {
                toggleMute(activity)
                return true
            }

            KeyEvent.KEYCODE_SLASH -> {
                navigateToTab(navController, "search")
                return true
            }

            KeyEvent.KEYCODE_1, KeyEvent.KEYCODE_NUMPAD_1 -> {
                navigateToTab(navController, "explore")
                return true
            }
            KeyEvent.KEYCODE_2, KeyEvent.KEYCODE_NUMPAD_2 -> {
                navigateToTab(navController, "search")
                return true
            }
            KeyEvent.KEYCODE_3, KeyEvent.KEYCODE_NUMPAD_3 -> {
                navigateToTab(navController, "library")
                return true
            }
            KeyEvent.KEYCODE_4, KeyEvent.KEYCODE_NUMPAD_4 -> {
                navigateToTab(navController, "statistics")
                return true
            }
            KeyEvent.KEYCODE_5, KeyEvent.KEYCODE_NUMPAD_5 -> {
                navigateToTab(navController, "settings")
                return true
            }
        }

        return false
    }

    private fun togglePlayPause(viewModel: PlayerSharedViewModel) {
        if (viewModel.isPlaying.value) {
            viewModel.pauseAudio()
        } else {
            viewModel.playAudio()
        }
    }

    private fun seekRelative(viewModel: PlayerSharedViewModel, deltaMs: Int) {
        val currentPos = viewModel.currentPosition.value
        val duration = viewModel.duration.value
        val targetPos = (currentPos + deltaMs).coerceIn(0, duration)
        viewModel.seekTo(targetPos)
    }

    private fun toggleMute(context: Context) {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager ?: return
        val currentVol = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        if (currentVol > 0) {
            previousVolume = currentVol
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, AudioManager.FLAG_SHOW_UI)
        } else {
            val restoreVol = if (previousVolume > 0) previousVolume else (audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) / 3).coerceAtLeast(1)
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, restoreVol, AudioManager.FLAG_SHOW_UI)
        }
    }

    private fun navigateToTab(navController: NavController?, route: String) {
        if (navController == null) return
        if (navController.currentDestination?.route == route) return
        try {
            navController.navigate(route) {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        } catch (e: Exception) {
            Log.e(TAG, "Navigation failed to $route: ${e.message}")
        }
    }
}
