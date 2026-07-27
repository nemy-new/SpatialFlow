package com.codetrio.overdrive.domain.repository

import com.codetrio.overdrive.domain.error.DataError
import com.codetrio.overdrive.domain.error.EmptyResult

interface LocalCacheDataSource {
    /**
     * Clears old temporary files (e.g. FFmpeg output, temp audio copies) on startup
     * to prevent app size bloat.
     */
    suspend fun clearOldCache(): EmptyResult<DataError.Local>
}
