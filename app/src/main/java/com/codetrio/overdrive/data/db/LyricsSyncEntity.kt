package com.codetrio.overdrive.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lyrics_sync")
data class LyricsSyncEntity(
    @PrimaryKey val videoId: String,
    val offsetMs: Long
)
