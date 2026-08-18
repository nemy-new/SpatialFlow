package com.codetrio.overdrive.ui.library.model

import android.content.Context
import androidx.annotation.StringRes
import com.codetrio.overdrive.R

enum class LibraryViewMode(
    @StringRes val titleRes: Int
) {
    LARGE_GRID(R.string.lib_view_large_grid),
    COMPACT_GRID(R.string.lib_view_compact_grid),
    STANDARD_LIST(R.string.lib_view_standard_list),
    COMPACT_LIST(R.string.lib_view_compact_list);

    companion object {
        private const val PREFS_NAME = "overdrive_library_prefs"
        private const val KEY_VIEW_MODE = "library_view_mode"

        fun getSavedMode(context: Context): LibraryViewMode {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val name = prefs.getString(KEY_VIEW_MODE, LARGE_GRID.name) ?: LARGE_GRID.name
            return try {
                valueOf(name)
            } catch (_: Exception) {
                LARGE_GRID
            }
        }

        fun saveMode(context: Context, mode: LibraryViewMode) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putString(KEY_VIEW_MODE, mode.name).apply()
        }
    }
}

enum class LibrarySortOrder(@StringRes val titleRes: Int) {
    RECENTLY_ADDED(R.string.lib_sort_recently_added),
    RECENTLY_PLAYED(R.string.lib_sort_recently_played),
    DATE_ADDED(R.string.lib_sort_recently_added),
    TITLE(R.string.lib_sort_title),
    ARTIST(R.string.lib_sort_artist),
    DURATION(R.string.lib_sort_duration),
    SONG_COUNT(R.string.lib_sort_song_count)
}
