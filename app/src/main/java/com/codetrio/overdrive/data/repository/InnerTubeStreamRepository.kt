package com.codetrio.overdrive.data.repository

import com.codetrio.overdrive.data.innertube.YouTubeMusic
import com.codetrio.overdrive.domain.error.DataError
import com.codetrio.overdrive.domain.error.Result
import com.codetrio.overdrive.domain.repository.StreamRepository
import kotlinx.coroutines.CancellationException

class InnerTubeStreamRepository : StreamRepository {
    override suspend fun getStreamUrl(videoId: String): Result<String, DataError.Network> {
        return try {
            val result = YouTubeMusic.getStreamUrl(videoId)
            val url = result.getOrNull()
            if (url != null) {
                Result.Success(url)
            } else {
                Result.Error(DataError.Network.NOT_FOUND)
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.Error(DataError.Network.UNKNOWN)
        }
    }

    override suspend fun getPlaybackPosition(videoId: String): Result<Long, DataError.Network> {
        return try {
            val result = YouTubeMusic.getPlaybackPosition(videoId)
            val pos = result.getOrNull()
            if (pos != null) {
                Result.Success(pos)
            } else {
                Result.Error(DataError.Network.NOT_FOUND)
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.Error(DataError.Network.UNKNOWN)
        }
    }
}
