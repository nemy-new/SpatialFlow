package com.codetrio.overdrive.data.dna

import com.codetrio.overdrive.data.db.HistoryEventEntity
import com.codetrio.overdrive.viewmodel.PlayerSharedViewModel.TopArtistStat
import com.codetrio.overdrive.viewmodel.PlayerSharedViewModel.TopSongStat
import java.util.Calendar

enum class MusicCapsuleTimeRange {
    ALL_TIME,
    THIS_MONTH,
    THIS_WEEK
}

enum class MusicArchetype(
    val titleJa: String,
    val titleEn: String,
    val subtitleJa: String,
    val subtitleEn: String,
    val iconName: String,
    val primaryColorHex: Long,
    val secondaryColorHex: Long
) {
    CYBERNETIC_NIGHTCRAWLER(
        titleJa = "真夜中の探求者",
        titleEn = "Nocturnal Explorer",
        subtitleJa = "深夜の静寂とともに研ぎ澄まされ、深い音の宇宙を探検するリスナー",
        subtitleEn = "Your senses awaken after dark, traversing deep nocturnal soundscapes.",
        iconName = "NightsStay",
        primaryColorHex = 0xFF6750A4,
        secondaryColorHex = 0xFF7D5260
    ),
    MELODIC_DAWN_CHASER(
        titleJa = "夜明けのメロディー探訪者",
        titleEn = "Melodic Dawn Chaser",
        subtitleJa = "朝一番の音楽からチャージし、一日をフレッシュに彩るリスナー",
        subtitleEn = "Morning melodies power your spirit as you greet the day with rhythm.",
        iconName = "WbSunny",
        primaryColorHex = 0xFFE06D53,
        secondaryColorHex = 0xFFF2B8B5
    ),
    AFTERNOON_GROOVER(
        titleJa = "デイライト・グルーヴァー",
        titleEn = "Daylight Groover",
        subtitleJa = "日中のワークや移動時間を心地よいビートで満たすリスナー",
        subtitleEn = "Midday rhythms keep your workflow uninterrupted and energizing.",
        iconName = "WbTwilight",
        primaryColorHex = 0xFF006874,
        secondaryColorHex = 0xFF4A6267
    ),
    INFINITE_VOYAGER(
        titleJa = "ジャンル・ナビゲーター",
        titleEn = "Vibe Navigator",
        subtitleJa = "ジャンルやアーティストの垣根を超え、多彩なサウンドを探検する航海士",
        subtitleEn = "Boundless musical curiosity spanning diverse genres, artists, and tempos.",
        iconName = "Explore",
        primaryColorHex = 0xFF386A20,
        secondaryColorHex = 0xFF55624C
    ),
    LOYAL_SUPERFAN(
        titleJa = "ディヴォーテッド・リスナー",
        titleEn = "Devoted Loyalist",
        subtitleJa = "お気に入りの名曲やアーティストを深く愛し、何度もリピートする情熱家",
        subtitleEn = "When you fall in love with a track, you immerse in every beat repeatedly.",
        iconName = "Favorite",
        primaryColorHex = 0xFFB3261E,
        secondaryColorHex = 0xFF9C4146
    ),
    HARMONIC_ARCHITECT(
        titleJa = "ハーモニック・マスター",
        titleEn = "Harmonic Connoisseur",
        subtitleJa = "一日を通してバランス良く多彩な音楽を楽しむ洗練されたリスナー",
        subtitleEn = "A balanced, refined musical appetite that flows effortlessly with life.",
        iconName = "AutoAwesome",
        primaryColorHex = 0xFF006C51,
        secondaryColorHex = 0xFF4C6358
    )
}

data class MusicDnaProfile(
    val timeRange: MusicCapsuleTimeRange,
    val archetype: MusicArchetype,
    val totalMinutes: Long,
    val totalPlays: Int,
    val uniqueTracksCount: Int,
    val uniqueArtistsCount: Int,
    val topSong: TopSongStat?,
    val topSongs: List<TopSongStat>,
    val topArtists: List<TopArtistStat>,
    val hourlyDistribution: Map<Int, Int>, // 0..23 -> count
    val peakHour: Int,
    val peakMood: String,
    val discoveryScore: Int, // 0..100
    val nightOwlScore: Int, // 0..100
    val devotionScore: Int, // 0..100
    val staminaScore: Int, // 0..100
    val formattedRangeLabel: String
)

object MusicDnaCalculator {

    fun calculate(
        allEvents: List<HistoryEventEntity>,
        timeRange: MusicCapsuleTimeRange
    ): MusicDnaProfile? {
        if (allEvents.isEmpty()) return null

        val now = System.currentTimeMillis()
        val calendar = Calendar.getInstance()

        val filteredEvents = when (timeRange) {
            MusicCapsuleTimeRange.ALL_TIME -> allEvents
            MusicCapsuleTimeRange.THIS_MONTH -> {
                calendar.timeInMillis = now
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val monthStart = calendar.timeInMillis
                allEvents.filter { it.timestamp >= monthStart }
            }
            MusicCapsuleTimeRange.THIS_WEEK -> {
                calendar.timeInMillis = now
                calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val weekStart = calendar.timeInMillis
                allEvents.filter { it.timestamp >= weekStart }
            }
        }

        if (filteredEvents.isEmpty()) return null

        val totalPlays = filteredEvents.size
        val totalMs = filteredEvents.sumOf { it.duration }
        val totalMinutes = if (totalMs > 0) totalMs / 1000 / 60 else (totalPlays * 3L) // fallback to 3 min per song

        // Grouping
        val songsGrouped = filteredEvents.groupBy { it.songId }
        val uniqueTracksCount = songsGrouped.size
        val topSongs = songsGrouped.map { (songId, list) ->
            val first = list.first()
            TopSongStat(
                songId = songId,
                title = first.title,
                artist = first.artist,
                thumbnailUrl = first.thumbnailUrl,
                count = list.size
            )
        }.sortedByDescending { it.count }

        val artistsGrouped = filteredEvents.groupBy { it.artist }
        val uniqueArtistsCount = artistsGrouped.size
        val topArtists = artistsGrouped.map { (artist, list) ->
            TopArtistStat(
                artist = artist,
                count = list.size,
                thumbnailUrl = list.firstOrNull { !it.thumbnailUrl.isNullOrEmpty() }?.thumbnailUrl
            )
        }.sortedByDescending { it.count }

        // Hourly distribution (0..23)
        val hourlyMap = (0..23).associateWith { hour ->
            filteredEvents.count { it.hourOfDay == hour }
        }
        val peakHour = hourlyMap.maxByOrNull { it.value }?.key ?: 12

        // Night vs Day plays
        // Night hours: 21, 22, 23, 0, 1, 2, 3, 4
        val nightPlays = filteredEvents.count { it.hourOfDay in listOf(21, 22, 23, 0, 1, 2, 3, 4) }
        val nightOwlRatio = (nightPlays.toFloat() / totalPlays.coerceAtLeast(1)).coerceIn(0f, 1f)
        val nightOwlScore = (nightOwlRatio * 100).toInt()

        // Discovery / Diversity score: ratio of unique tracks & artists to total plays
        val diversityRatio = ((uniqueTracksCount + uniqueArtistsCount).toFloat() / (totalPlays * 1.5f).coerceAtLeast(1f)).coerceIn(0f, 1f)
        val discoveryScore = (diversityRatio * 100).toInt()

        // Devotion score: % of plays taken by top 3 songs
        val top3Count = topSongs.take(3).sumOf { it.count }
        val devotionRatio = (top3Count.toFloat() / totalPlays.coerceAtLeast(1)).coerceIn(0f, 1f)
        val devotionScore = (devotionRatio * 100).toInt()

        // Stamina score: based on total listening minutes
        val staminaScore = ((totalMinutes.toFloat() / 300f).coerceIn(0f, 1f) * 100).toInt()

        // Peak mood
        val peakMood = when (peakHour) {
            in 5..11 -> "Morning Spark"
            in 12..16 -> "Afternoon Groove"
            in 17..20 -> "Evening Sunset"
            else -> "Midnight Mystique"
        }

        // Archetype diagnosis
        val archetype = when {
            nightOwlScore >= 55 -> MusicArchetype.CYBERNETIC_NIGHTCRAWLER
            peakHour in 5..11 -> MusicArchetype.MELODIC_DAWN_CHASER
            peakHour in 12..16 && nightOwlScore < 30 -> MusicArchetype.AFTERNOON_GROOVER
            discoveryScore >= 65 -> MusicArchetype.INFINITE_VOYAGER
            devotionScore >= 50 -> MusicArchetype.LOYAL_SUPERFAN
            else -> MusicArchetype.HARMONIC_ARCHITECT
        }

        val formattedRangeLabel = when (timeRange) {
            MusicCapsuleTimeRange.ALL_TIME -> "All-Time DNA"
            MusicCapsuleTimeRange.THIS_MONTH -> "This Month's Capsule"
            MusicCapsuleTimeRange.THIS_WEEK -> "Weekly Pulse"
        }

        return MusicDnaProfile(
            timeRange = timeRange,
            archetype = archetype,
            totalMinutes = totalMinutes,
            totalPlays = totalPlays,
            uniqueTracksCount = uniqueTracksCount,
            uniqueArtistsCount = uniqueArtistsCount,
            topSong = topSongs.firstOrNull(),
            topSongs = topSongs.take(10),
            topArtists = topArtists.take(5),
            hourlyDistribution = hourlyMap,
            peakHour = peakHour,
            peakMood = peakMood,
            discoveryScore = discoveryScore,
            nightOwlScore = nightOwlScore,
            devotionScore = devotionScore,
            staminaScore = staminaScore,
            formattedRangeLabel = formattedRangeLabel
        )
    }
}
