package com.codetrio.overdrive.domain.repository

import com.codetrio.overdrive.domain.error.DataError
import com.codetrio.overdrive.domain.error.Result

interface StreamRepository {
    suspend fun getStreamUrl(videoId: String): Result<String, DataError.Network>
    suspend fun getPlaybackPosition(videoId: String): Result<Long, DataError.Network>
}
