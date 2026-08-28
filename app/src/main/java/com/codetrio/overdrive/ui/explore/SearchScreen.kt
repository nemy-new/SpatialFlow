package com.codetrio.overdrive.ui.explore

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.codetrio.overdrive.MainActivity
import com.codetrio.overdrive.data.innertube.*
import com.codetrio.overdrive.model.*
import com.codetrio.overdrive.player.queue.YouTubeQueue
import com.codetrio.overdrive.ui.CreateLocalPlaylistDialog
import com.codetrio.overdrive.ui.LocalPlaylistPickerDialog
import com.codetrio.overdrive.viewmodel.AccountViewModel
import com.codetrio.overdrive.viewmodel.DetailType
import com.codetrio.overdrive.viewmodel.ExploreViewModel
import com.codetrio.overdrive.viewmodel.PlayerSharedViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SearchScreen(
    viewModel: ExploreViewModel,
    playerSharedViewModel: PlayerSharedViewModel,
    onNavigateToExplore: () -> Unit = {}
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val context = LocalContext.current
    val mainActivity = context as? MainActivity
    val scope = rememberCoroutineScope()

    val accountVM: AccountViewModel = viewModel()
    val userProfile by accountVM.userProfile.collectAsStateWithLifecycle()
    val accountHistory by accountVM.history.collectAsStateWithLifecycle()

    val searchState by viewModel.searchState.collectAsStateWithLifecycle()
    val homeState by viewModel.homeState.collectAsStateWithLifecycle()
    val detailState by viewModel.detailState.collectAsStateWithLifecycle()
    val playbackState by viewModel.playbackState.collectAsStateWithLifecycle()

    val searchQuery = searchState.searchQuery
    val searchResults = searchState.searchResults
    val suggestions = searchState.suggestions
    val isSearching = searchState.isSearching
    val searchHistory = searchState.searchHistory
    val isLoadingSuggestions = searchState.isLoadingSuggestions

    val homeMoods = homeState.homeMoods
    val currentMood = homeState.currentMood

    val albumDetail = detailState.albumDetail
    val artistDetail = detailState.artistDetail
    val playlistDetail = detailState.playlistDetail
    val sectionDetail = detailState.sectionDetail
    val moodDetail = detailState.moodDetail
    val isLoadingDetail = detailState.isLoadingDetail
    val detailStack = detailState.detailStack

    val isLoadingStream = playbackState.isLoadingStream
    val currentOnlineSong = playbackState.currentOnlineSong

    var isSearchActive by remember { mutableStateOf(false) }
    val keyboardController = androidx.compose.ui.platform.LocalSoftwareKeyboardController.current
    LaunchedEffect(viewModel.focusSearchEvent) {
        viewModel.focusSearchEvent.collect {
            isSearchActive = true
            delay(100.milliseconds)
            keyboardController?.show()
        }
    }
    var selectedSongForMenu by remember { mutableStateOf<OnlineSong?>(null) }
    var showCreditsForSong by remember { mutableStateOf<OnlineSong?>(null) }

    var showAddToPlaylistDialog by remember { mutableStateOf(false) }
    var showCreatePlaylistDialog by remember { mutableStateOf(false) }
    var songToAddPlaylist by remember { mutableStateOf<SongItem?>(null) }
    val localPlaylists by playerSharedViewModel.localPlaylistsFlow.collectAsStateWithLifecycle(emptyList())

    val searchListState = rememberLazyListState()
    val currentFilter by viewModel.searchFilter.collectAsStateWithLifecycle()

    val currentScreen =
        remember(detailStack, isLoadingDetail, showCreditsForSong) {
            when {
                isLoadingDetail -> "loading"
                showCreditsForSong != null -> "credits"
                detailStack.isNotEmpty() -> {
                    when (detailStack.last()) {
                        DetailType.ALBUM -> "album"
                        DetailType.PLAYLIST -> "playlist"
                        DetailType.ARTIST -> "artist"
                        DetailType.SECTION -> "section"
                        DetailType.GENRES -> "genres"
                        DetailType.MOOD -> "mood"
                    }
                }
                else -> "home"
            }
        }

    val handleDetailBack = remember(viewModel, mainActivity) {
        {
            val currentStackSize = viewModel.detailState.value.detailStack.size
            if (currentStackSize <= 1) {
                viewModel.resetToHome()
            } else {
                viewModel.popDetailStack()
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        when (currentScreen) {
            "album" -> albumDetail?.let { detail ->
                AlbumDetailView(
                    albumPage = detail,
                    currentOnlineSong = currentOnlineSong,
                    isLoadingStream = isLoadingStream,
                    onBack = { handleDetailBack() },
                    onSongClick = { song, queue, index ->
                        viewModel.playOnlineSongWithQueue(song, queue, index)
                    },
                    onSongMenuClick = { selectedSongForMenu = it },
                    onStartRadioClick = { videoId -> viewModel.startRadio(videoId) }
                )
            }
            "playlist" -> playlistDetail?.let { detail ->
                PlaylistDetailView(
                    playlistPage = detail,
                    currentOnlineSong = currentOnlineSong,
                    isLoadingStream = isLoadingStream,
                    onBack = { handleDetailBack() },
                    onSongClick = { song, queue, index ->
                        viewModel.playOnlineSongWithQueue(song, queue, index)
                    },
                    onSongMenuClick = { selectedSongForMenu = it },
                    onStartRadioClick = { videoId -> viewModel.startRadio(videoId) }
                )
            }
            "artist" -> artistDetail?.let { detail ->
                ArtistDetailView(
                    artistPage = detail,
                    currentOnlineSong = currentOnlineSong,
                    isSubscribed = detail.artist.isSubscribed,
                    onBack = { handleDetailBack() },
                    onSongClick = { song, queue, index ->
                        viewModel.playOnlineSongWithQueue(song, queue, index)
                    },
                    onAlbumClick = { viewModel.loadAlbum(it.browseId) },
                    onPlaylistClick = { viewModel.loadPlaylist(it.playlistId) },
                    onArtistClick = { viewModel.loadArtist(it.browseId, it.thumbnailUrl) },
                    onSongMenuClick = { selectedSongForMenu = it },
                    onSubscribeClick = { channelId ->
                        if (detail.artist.isSubscribed) viewModel.unsubscribeFromArtist(channelId)
                        else viewModel.subscribeToArtist(channelId)
                    },
                    onStartRadioClick = { videoId -> viewModel.startRadio(videoId) },
                    onSectionClick = { browseId, params, title ->
                        viewModel.loadSectionDetails(browseId, params, title)
                    }
                )
            }
            "section" -> sectionDetail?.let { detail ->
                SectionDetailView(
                    section = detail,
                    currentOnlineSong = currentOnlineSong,
                    isLoadingStream = isLoadingStream,
                    onBack = { handleDetailBack() },
                    onSongClick = { song, queue, index ->
                        viewModel.playOnlineSongWithQueue(song, queue, index)
                    },
                    onAlbumClick = { viewModel.loadAlbum(it.browseId) },
                    onPlaylistClick = { viewModel.loadPlaylist(it.playlistId) },
                    onArtistClick = { viewModel.loadArtist(it.browseId, it.thumbnailUrl) },
                    onSongMenuClick = { selectedSongForMenu = it },
                    onStartRadioClick = { videoId -> viewModel.startRadio(videoId) }
                )
            }
            "mood" -> moodDetail?.let { detail ->
                MoodDetailView(
                    moodDetail = detail,
                    currentOnlineSong = currentOnlineSong,
                    isLoadingStream = isLoadingStream,
                    onBack = { handleDetailBack() },
                    onSongClick = { song, queue, index ->
                        viewModel.playOnlineSongWithQueue(song, queue, index)
                    },
                    onAlbumClick = { viewModel.loadAlbum(it.browseId) },
                    onPlaylistClick = { viewModel.loadPlaylist(it.playlistId) },
                    onArtistClick = { viewModel.loadArtist(it.browseId, it.thumbnailUrl) },
                    onSongMenuClick = { selectedSongForMenu = it },
                    onSectionClick = { browseId, params, title ->
                        viewModel.loadSectionDetails(browseId, params, title)
                    },
                    onStartRadioClick = { videoId -> viewModel.startRadio(videoId) }
                )
            }
            "genres" -> {
                GenresScreen(
                    genresSections = detailState.genresSections,
                    onBack = { handleDetailBack() },
                    onGenreClick = { title: String, browseId: String, params: String? ->
                        viewModel.loadMood(title, browseId, params)
                    }
                )
            }
            "credits" -> showCreditsForSong?.let { s ->
                SongCreditsScreen(song = s, onBack = { showCreditsForSong = null })
            }
            else -> Column(modifier = Modifier.fillMaxSize()) {
                SearchHeader(
                    searchQuery = searchQuery,
                    onQueryChange = { query -> viewModel.setSearchQuery(query) },
                    onSearch = { query -> viewModel.search(query); isSearchActive = false },
                    isSearchActive = isSearchActive,
                    accountHistory = accountHistory,
                    onAccountHistorySongClick = { song, queue, index ->
                        viewModel.playOnlineSongWithQueue(song, queue, index)
                    },
                    onSearchActiveChange = { active -> isSearchActive = active },
                    searchResults = searchResults,
                    searchHistory = searchHistory,
                    onClearSearchHistory = { viewModel.clearSearchHistory() },
                    onRemoveFromSearchHistory = { item -> viewModel.removeFromSearchHistory(item) },
                    onHistoryItemClick = { item ->
                        scope.launch {
                            isSearchActive = false
                            delay(50.milliseconds)
                            viewModel.search(item)
                        }
                    },
                    suggestions = suggestions,
                    isLoadingSuggestions = isLoadingSuggestions,
                    onSuggestionClick = { suggestion ->
                        scope.launch {
                            isSearchActive = false
                            delay(50.milliseconds)
                            viewModel.search(suggestion)
                        }
                    },
                    onAccountVisibleChange = { },
                    userProfile = userProfile,
                    homeMoods = homeMoods,
                    currentMood = currentMood,
                    onMoodClick = { mood -> viewModel.setMood(mood) },
                    currentFilter = currentFilter,
                    onFilterClick = { filter -> viewModel.setSearchFilter(filter) },
                    isLandscape = isLandscape,
                    onClearSearch = { viewModel.clearSearch() },
                    headerTitle = "Search"
                )

                Box(modifier = Modifier.weight(1f)) {
                    when {
                        isSearching && searchResults.isEmpty() -> {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        }
                        searchResults.isNotEmpty() -> {
                            LazyColumn(
                                state = searchListState,
                                modifier = Modifier.fillMaxSize().background(Color.Transparent),
                                contentPadding = PaddingValues(bottom = 120.dp)
                            ) {
                                itemsIndexed(
                                    items = searchResults,
                                    key = { idx, item ->
                                        when (item) {
                                            is SearchItem.TopResult -> "top-$idx-${item.title}"
                                            is SearchItem.Header -> "header-$idx-${item.title}"
                                            is SearchItem.Song -> "song-$idx-${item.song.videoId}"
                                            is SearchItem.Album -> "album-$idx-${item.album.browseId}"
                                            is SearchItem.Artist -> "artist-$idx-${item.artist.browseId}"
                                            is SearchItem.Playlist -> "playlist-$idx-${item.playlist.playlistId}"
                                        }
                                    }
                                ) { _, item ->
                                    when (item) {
                                        is SearchItem.TopResult -> {
                                            TopResultCard(
                                                topResult = item,
                                                isCurrentlyPlaying = item.song?.videoId == currentOnlineSong?.videoId,
                                                onPlayClick = {
                                                    item.song?.let { song ->
                                                        val allSearchSongs = searchResults.filterIsInstance<SearchItem.Song>().map { it.song }
                                                        val targetQueue = if (allSearchSongs.isNotEmpty()) allSearchSongs else listOf(song)
                                                        val targetIndex = targetQueue.indexOfFirst { it.videoId == song.videoId }.coerceAtLeast(0)
                                                        viewModel.playOnlineSongWithQueue(song, targetQueue, targetIndex)
                                                    } ?: viewModel.startRadioForItem(item)
                                                },
                                                onSaveClick = { viewModel.saveItemToLibrary(item) },
                                                onMoreClick = { item.song?.let { selectedSongForMenu = it } },
                                                onClick = {
                                                    when {
                                                        item.song != null -> {
                                                            val song = item.song
                                                            val allSearchSongs = searchResults.filterIsInstance<SearchItem.Song>().map { it.song }
                                                            val targetQueue = if (allSearchSongs.isNotEmpty()) allSearchSongs else listOf(song)
                                                            val targetIndex = targetQueue.indexOfFirst { it.videoId == song.videoId }.coerceAtLeast(0)
                                                            viewModel.playOnlineSongWithQueue(song, targetQueue, targetIndex)
                                                        }
                                                        item.album != null -> viewModel.loadAlbum(item.album.browseId)
                                                        item.artist != null -> viewModel.loadArtist(item.artist.browseId, item.artist.thumbnailUrl)
                                                        item.playlist != null -> viewModel.loadPlaylist(item.playlist.playlistId)
                                                    }
                                                }
                                            )
                                        }
                                        is SearchItem.Header -> {
                                            SearchSectionHeader(title = item.title)
                                        }
                                        else -> {
                                            SearchResultItem(
                                                item = item,
                                                isCurrentlyPlaying = when (item) {
                                                    is SearchItem.Song -> item.song.videoId == currentOnlineSong?.videoId
                                                    else -> false
                                                },
                                                isLoading = isLoadingStream && when (item) {
                                                    is SearchItem.Song -> item.song.videoId == currentOnlineSong?.videoId
                                                    else -> false
                                                },
                                                onSongMenuClick = { selectedSongForMenu = it },
                                                onTouchDown = {
                                                    if (item is SearchItem.Song) {
                                                        playerSharedViewModel.specPrefetch(
                                                            SongItem.createOnlineSong(
                                                                item.song.videoId,
                                                                item.song.title,
                                                                item.song.artist,
                                                                "",
                                                                item.song.durationMs,
                                                                item.song.thumbnailUrl,
                                                                item.song.artistId
                                                            )
                                                        )
                                                    }
                                                },
                                                onClick = {
                                                    when (item) {
                                                        is SearchItem.Song -> {
                                                            val allSearchSongs = searchResults.filterIsInstance<SearchItem.Song>().map { it.song }
                                                            val targetQueue = if (allSearchSongs.isNotEmpty()) allSearchSongs else listOf(item.song)
                                                            val targetIndex = targetQueue.indexOfFirst { it.videoId == item.song.videoId }.coerceAtLeast(0)
                                                            viewModel.playOnlineSongWithQueue(item.song, targetQueue, targetIndex)
                                                        }
                                                        is SearchItem.Album -> viewModel.loadAlbum(item.album.browseId)
                                                        is SearchItem.Artist -> viewModel.loadArtist(item.artist.browseId, item.artist.thumbnailUrl)
                                                        is SearchItem.Playlist -> viewModel.loadPlaylist(item.playlist.playlistId)
                                                        else -> {}
                                                    }
                                                }
                                            )
                                        }
                                    }
                                }
                                item {
                                    if (isSearching) {
                                        Box(
                                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            CircularProgressIndicator(modifier = Modifier.size(32.dp))
                                        }
                                    }
                                }
                            }
                            LaunchedEffect(searchListState) {
                                snapshotFlow { searchListState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
                                    .collect { lastVisibleIndex ->
                                        val threshold = searchResults.size - 3
                                        if (lastVisibleIndex != null && lastVisibleIndex >= threshold) {
                                            viewModel.loadMoreResults()
                                        }
                                    }
                            }
                        }
                        searchQuery.isNotBlank() -> {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(
                                    "No results found for \"$searchQuery\"",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        else -> {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                                contentPadding = PaddingValues(top = 16.dp, bottom = 120.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                item {
                                    Text(
                                        text = androidx.compose.ui.res.stringResource(com.codetrio.overdrive.R.string.text_browse_categories),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(bottom = 4.dp)
                                    )
                                }
                                item {
                                    val newReleasesTitle = androidx.compose.ui.res.stringResource(com.codetrio.overdrive.R.string.text_new_releases)
                                    val chartsTitle = androidx.compose.ui.res.stringResource(com.codetrio.overdrive.R.string.text_charts)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        CategoryCard(
                                            title = newReleasesTitle,
                                            icon = Icons.Default.MusicNote,
                                            gradientColors = listOf(Color(0xFF2E1A47), Color(0xFF160B24)),
                                            onClick = {
                                                viewModel.loadMood(newReleasesTitle, "FEmusic_new_releases", null)
                                            },
                                            modifier = Modifier.weight(1f)
                                        )
                                        CategoryCard(
                                            title = chartsTitle,
                                            icon = Icons.AutoMirrored.Filled.TrendingUp,
                                            gradientColors = listOf(Color(0xFF4C1D1D), Color(0xFF240E0E)),
                                            onClick = {
                                                viewModel.loadMood(chartsTitle, "FEmusic_charts", null)
                                            },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                                item {
                                    val moodsAndGenresTitle = androidx.compose.ui.res.stringResource(com.codetrio.overdrive.R.string.text_moods_and_genres)
                                    val podcastsTitle = androidx.compose.ui.res.stringResource(com.codetrio.overdrive.R.string.text_podcasts)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        CategoryCard(
                                            title = moodsAndGenresTitle,
                                            icon = Icons.Default.Favorite,
                                            gradientColors = listOf(Color(0xFF0F3040), Color(0xFF081820)),
                                            onClick = {
                                                viewModel.loadGenres()
                                            },
                                            modifier = Modifier.weight(1f)
                                        )
                                        CategoryCard(
                                            title = podcastsTitle,
                                            icon = Icons.Default.Mic,
                                            gradientColors = listOf(Color(0xFF4A1E30), Color(0xFF240F18)),
                                            onClick = {
                                                viewModel.loadMood(podcastsTitle, "FEmusic_podcasts", null)
                                            },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (isLoadingDetail) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }

    selectedSongForMenu?.let { song ->
        val isPinned = homeState.pinnedSpeedDialIds.contains(song.videoId)
        OnlineSongBottomSheet(
            song = song,
            isPinned = isPinned,
            onDismissRequest = { selectedSongForMenu = null },
            playerSharedViewModel = playerSharedViewModel,
            exploreViewModel = viewModel,
            onPlaylistAddClick = { onlineSongItem ->
                songToAddPlaylist = onlineSongItem
                showAddToPlaylistDialog = true
            },
            onViewCreditsClick = { s ->
                showCreditsForSong = s
            }
        )
    }

    if (showAddToPlaylistDialog && songToAddPlaylist != null) {
        LocalPlaylistPickerDialog(
            playlists = localPlaylists,
            onCreateNew = {
                showCreatePlaylistDialog = true
                showAddToPlaylistDialog = false
            },
            onPlaylistSelected = { playlist ->
                playerSharedViewModel.addSongToLocalPlaylist(playlist.id, songToAddPlaylist!!)
                showAddToPlaylistDialog = false
                songToAddPlaylist = null
                com.codetrio.overdrive.ui.SnackbarController.showMessage("Added to playlist: ${playlist.title}")
            },
            onDismiss = {
                showAddToPlaylistDialog = false
                songToAddPlaylist = null
            }
        )
    }

    if (showCreatePlaylistDialog) {
        CreateLocalPlaylistDialog(
            onConfirm = { name ->
                playerSharedViewModel.createLocalPlaylist(name)
                showCreatePlaylistDialog = false
                showAddToPlaylistDialog = true
            },
            onDismiss = {
                showCreatePlaylistDialog = false
                showAddToPlaylistDialog = true
            }
        )
    }
}
