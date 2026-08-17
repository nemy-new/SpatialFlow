package com.codetrio.overdrive.ui.statistics

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.palette.graphics.Palette
import coil.compose.AsyncImage
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.codetrio.overdrive.MainActivity
import com.codetrio.overdrive.R
import com.codetrio.overdrive.model.SongItem
import com.codetrio.overdrive.viewmodel.PlayerSharedViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    playerViewModel: PlayerSharedViewModel,
    onNavigateToExplore: () -> Unit
) {
    val recap by playerViewModel.listeningRecapFlow.collectAsStateWithLifecycle(null)
    val history by playerViewModel.historyFlow.collectAsStateWithLifecycle(emptyList())
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(androidx.compose.ui.res.stringResource(R.string.tab_statistics)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            val nestedScrollConnection = remember {
                object : NestedScrollConnection {
                    override fun onPostScroll(consumed: Offset, available: Offset, source: androidx.compose.ui.input.nestedscroll.NestedScrollSource): Offset {
                        return super.onPostScroll(consumed, available, source)
                    }
                }
            }
            RecapContent(nestedScrollConnection, playerViewModel, onNavigateToExplore, history)
        }
    }
}

private fun getActivityFromContext(context: Context): Activity? {
    var ctx = context
    while (ctx is ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}

@Composable
private fun RecapContent(
    nestedScrollConnection: androidx.compose.ui.input.nestedscroll.NestedScrollConnection,
    viewModel: PlayerSharedViewModel,
    onNavigateToExplore: () -> Unit,
    history: List<com.codetrio.overdrive.data.db.HistoryEventEntity>
) {
    val context = LocalContext.current
    val recap by viewModel.listeningRecapFlow.collectAsStateWithLifecycle(null)

    if (recap == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.BarChart,
                    contentDescription = "No Stats",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(64.dp)
                )
                Text(
                    text = androidx.compose.ui.res.stringResource(com.codetrio.overdrive.R.string.text_your_flow_is_warming_up),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = androidx.compose.ui.res.stringResource(com.codetrio.overdrive.R.string.text_start_playing_your_favorite_tracks_and_your_listening_flow_highlights_will_appear_here),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
            }
        }
    } else {
        val data = recap!!

        val heroSong = remember(data.topSongs) { data.topSongs.firstOrNull() }
        var topSongColor by remember(heroSong?.thumbnailUrl) { mutableStateOf<Color?>(null) }
        LaunchedEffect(heroSong?.thumbnailUrl) {
            val url = heroSong?.thumbnailUrl
            if (!url.isNullOrEmpty()) {
                withContext(Dispatchers.IO) {
                    try {
                        val loader = context.imageLoader
                        val request = ImageRequest.Builder(context)
                            .data(url)
                            .allowHardware(false)
                            .build()
                        val result = loader.execute(request)
                        if (result is SuccessResult) {
                            val bitmap = (result.drawable as? android.graphics.drawable.BitmapDrawable)?.bitmap
                            if (bitmap != null) {
                                val palette = Palette.from(bitmap).generate()
                                val vibrantColor = palette.getVibrantColor(0)
                                val dominantColor = palette.getDominantColor(0)
                                val darkVibrantColor = palette.getDarkVibrantColor(0)
                                val mutedColor = palette.getMutedColor(0)
                                val lightVibrantColor = palette.getLightVibrantColor(0)
                                
                                val extractedInt = if (vibrantColor != 0) vibrantColor
                                                   else if (dominantColor != 0) dominantColor
                                                   else if (darkVibrantColor != 0) darkVibrantColor
                                                   else if (mutedColor != 0) mutedColor
                                                   else if (lightVibrantColor != 0) lightVibrantColor
                                                   else 0
                                if (extractedInt != 0) {
                                    topSongColor = Color(extractedInt)
                                }
                            }
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("RecapColor", "Failed to extract color: ${e.message}")
                    }
                }
            }
        }

        LaunchedEffect(data.topArtists) {
            data.topArtists.forEach { item ->
                viewModel.resolveArtistProfileImage(item.artist)
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(nestedScrollConnection),
            contentPadding = PaddingValues(bottom = 160.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Glassmorphic Summary Metrics
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min)
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Total Listening Card
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Headset,
                                contentDescription = "Minutes Played",
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Text(
                                text = "${data.totalMinutes} min",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Text(
                                text = androidx.compose.ui.res.stringResource(com.codetrio.overdrive.R.string.text_total_listen_time),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }

                    // Peak Habit Mood Card
                    val moodIconRes = when (data.peakMood) {
                        "Morning Spark" -> R.drawable.ic_morning
                        "Afternoon Groove" -> R.drawable.ic_afternoon
                        "Evening Harmony" -> R.drawable.ic_evening
                        else -> R.drawable.ic_evening
                    }
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = moodIconRes),
                                contentDescription = "Peak Vibe",
                                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = data.peakMood,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                            Text(
                                text = data.peakMoodDescription,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }

            // Top Songs Ranked Shelf
            item {
                Text(
                    text = androidx.compose.ui.res.stringResource(com.codetrio.overdrive.R.string.text_top_played_songs),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(start = 24.dp, top = 28.dp, bottom = 12.dp)
                )
            }

            // Hero #1 Song Card
            if (data.topSongs.isNotEmpty()) {
                val heroSong = data.topSongs.first()
                item {
                    val cardBgColor = topSongColor ?: MaterialTheme.colorScheme.primaryContainer
                    val isDark = topSongColor?.let { 
                        (0.2126f * it.red + 0.7152f * it.green + 0.0722f * it.blue) < 0.5f 
                    } ?: true

                    val effectsSpec = MaterialTheme.motionScheme.defaultEffectsSpec<Color>()
                    val animatedCardBgColor by animateColorAsState(
                        targetValue = cardBgColor,
                        animationSpec = effectsSpec,
                        label = "TopSongCardBgAnimation"
                    )

                    val titleColor = if (topSongColor != null) {
                        if (isDark) Color.White else Color(0xFF1C1B1F)
                    } else {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    }

                    val artistColor = if (topSongColor != null) {
                        if (isDark) Color.White.copy(alpha = 0.75f) else Color(0xFF1C1B1F).copy(alpha = 0.75f)
                    } else {
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    }

                    val playedCountColor = if (topSongColor != null) {
                        if (isDark) Color.White else Color(0xFF1C1B1F)
                    } else {
                        MaterialTheme.colorScheme.primary
                    }

                    val badgeBgColor = if (topSongColor != null) {
                        if (isDark) Color.White.copy(alpha = 0.2f) else Color.Black.copy(alpha = 0.2f)
                    } else {
                        MaterialTheme.colorScheme.primary
                    }

                    val badgeTextColor = if (topSongColor != null) {
                        if (isDark) Color.White else Color.Black
                    } else {
                        MaterialTheme.colorScheme.onPrimary
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 8.dp)
                            .clickable {
                                val songItem = if (heroSong.songId.toLongOrNull() != null && !heroSong.thumbnailUrl.isNullOrEmpty() && !heroSong.thumbnailUrl.startsWith("http")) {
                                    SongItem(
                                        heroSong.songId.toLong(),
                                        heroSong.title,
                                        heroSong.artist,
                                        -1L,
                                        heroSong.thumbnailUrl,
                                        0L,
                                        System.currentTimeMillis()
                                    )
                                } else {
                                    SongItem.createOnlineSong(
                                        videoId = heroSong.songId,
                                        title = heroSong.title,
                                        artist = heroSong.artist,
                                        streamUrl = null,
                                        durationMs = 0L,
                                        thumbnailUrl = heroSong.thumbnailUrl
                                    )
                                }
                                viewModel.playSong(songItem)
                            },
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = animatedCardBgColor
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.size(110.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                            ) {
                                if (!heroSong.thumbnailUrl.isNullOrEmpty()) {
                                    AsyncImage(
                                        model = heroSong.thumbnailUrl,
                                        contentDescription = "Cover Art",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(MaterialTheme.colorScheme.surfaceVariant),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.MusicNote,
                                            contentDescription = "Placeholder",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(36.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = badgeBgColor,
                                    modifier = Modifier.wrapContentSize()
                                ) {
                                    Text(
                                        text = androidx.compose.ui.res.stringResource(com.codetrio.overdrive.R.string.text_your_1_track),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = badgeTextColor,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                                Text(
                                    text = heroSong.title,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = titleColor,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = heroSong.artist,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = artistColor
                                )
                                Text(
                                    text = "Played ${heroSong.count} times",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = playedCountColor
                                )
                            }
                        }
                    }
                }
            }

            // Remaining Top Songs (Ranked List)
            if (data.topSongs.size > 1) {
                items(data.topSongs.drop(1)) { song ->
                    ListItem(
                        headlineContent = { Text(song.title, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                        supportingContent = { Text(song.artist, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                        leadingContent = {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                if (!song.thumbnailUrl.isNullOrEmpty()) {
                                    AsyncImage(
                                        model = song.thumbnailUrl,
                                        contentDescription = "Cover",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Icon(Icons.Default.MusicNote, null)
                                }
                            }
                        },
                        trailingContent = {
                            Text(
                                text = "${song.count} plays",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        },
                        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                            .clickable {
                                val songItem = if (song.songId.toLongOrNull() != null && !song.thumbnailUrl.isNullOrEmpty() && !song.thumbnailUrl.startsWith("http")) {
                                    SongItem(
                                        song.songId.toLong(),
                                        song.title,
                                        song.artist,
                                        -1L,
                                        song.thumbnailUrl,
                                        0L,
                                        System.currentTimeMillis()
                                    )
                                } else {
                                    SongItem.createOnlineSong(
                                        videoId = song.songId,
                                        title = song.title,
                                        artist = song.artist,
                                        streamUrl = null,
                                        durationMs = 0L,
                                        thumbnailUrl = song.thumbnailUrl
                                    )
                                }
                                viewModel.playSong(songItem)
                            }
                    )
                }
            }

            // Top Artists Shelf
            if (data.topArtists.isNotEmpty()) {
                item {
                    Text(
                        text = androidx.compose.ui.res.stringResource(com.codetrio.overdrive.R.string.text_your_top_artists),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(start = 24.dp, top = 24.dp, bottom = 12.dp)
                    )
                }

                item {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(data.topArtists) { item ->
                            val imageUrl = viewModel.artistProfileMap[item.artist]
                            Column(
                                modifier = Modifier
                                    .width(96.dp)
                                    .clickable {
                                        onNavigateToExplore()
                                    },
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(80.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surfaceVariant),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (!imageUrl.isNullOrEmpty()) {
                                        AsyncImage(
                                            model = imageUrl,
                                            contentDescription = "Artist picture",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else {
                                        Icon(Icons.Default.Person, null, modifier = Modifier.size(36.dp))
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = item.artist,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = "${item.count} songs played",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }


            // Recent History
            if (history.isNotEmpty()) {
                item {
                    Text(
                        text = "Recent History",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
                    )
                }
                
                items(history.take(100)) { event ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                // Replay the song
                                val songItem = if (!event.thumbnailUrl.isNullOrEmpty() && !event.thumbnailUrl.startsWith("http")) {
                                    SongItem(
                                        event.songId.toLongOrNull() ?: 0L,
                                        event.title,
                                        event.artist,
                                        -1L,
                                        event.thumbnailUrl,
                                        0L,
                                        System.currentTimeMillis()
                                    )
                                } else {
                                    SongItem.createOnlineSong(
                                        videoId = event.songId,
                                        title = event.title,
                                        artist = event.artist,
                                        streamUrl = null,
                                        durationMs = 0L,
                                        thumbnailUrl = event.thumbnailUrl
                                    )
                                }
                                viewModel.playSong(songItem)
                            }
                            .padding(horizontal = 24.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = coil.request.ImageRequest.Builder(LocalContext.current)
                                .data(event.thumbnailUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Album art",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = event.title,
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = event.artist,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        val timeFormatted = android.text.format.DateUtils.getRelativeTimeSpanString(
                            event.timestamp,
                            System.currentTimeMillis(),
                            android.text.format.DateUtils.MINUTE_IN_MILLIS
                        ).toString()
                        Text(
                            text = timeFormatted,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                }
            }

        }
    }
}

// Background scanner helper
