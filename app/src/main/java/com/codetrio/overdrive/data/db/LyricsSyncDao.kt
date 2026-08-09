package com.codetrio.overdrive.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LyricsSyncDao {
    @Query("SELECT offsetMs FROM lyrics_sync WHERE videoId = :videoId LIMIT 1")
    fun getOffsetFlow(videoId: String): Flow<Long?>

    @Query("SELECT offsetMs FROM lyrics_sync WHERE videoId = :videoId LIMIT 1")
    suspend fun getOffsetSync(videoId: String): Long?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSyncOffset(entity: LyricsSyncEntity)
}
