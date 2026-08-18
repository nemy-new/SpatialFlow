@file:Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS",
    "TYPE_MISMATCH_BASED_ON_JAVA_ANNOTATIONS"
)

package com.codetrio.overdrive.ui.library

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.automirrored.filled.PlaylistPlay
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Podcasts
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.codetrio.overdrive.MainActivity
import com.codetrio.overdrive.R
import com.codetrio.overdrive.data.db.PlaylistEntity
import com.codetrio.overdrive.data.innertube.OnlineAlbum
import com.codetrio.overdrive.data.innertube.OnlineArtist
import com.codetrio.overdrive.data.innertube.OnlinePlaylist
import com.codetrio.overdrive.data.innertube.OnlineSong
import com.codetrio.overdrive.model.SongItem
import com.codetrio.overdrive.model.toSongItem
import com.codetrio.overdrive.ui.CreateLocalPlaylistDialog
import com.codetrio.overdrive.ui.LocalPlaylistPickerDialog
import com.codetrio.overdrive.ui.explore.AccountScreen
import com.codetrio.overdrive.ui.explore.OnlineSongBottomSheet
import com.codetrio.overdrive.ui.explore.SongCreditsScreen
import com.codetrio.overdrive.ui.library.components.LibraryQuickAccessHero
import com.codetrio.overdrive.ui.library.components.LibraryToolbar
import com.codetrio.overdrive.ui.library.components.PlaylistCollageArt
import com.codetrio.overdrive.ui.library.model.LibrarySortOrder
import com.codetrio.overdrive.ui.library.model.LibraryViewMode
import com.codetrio.overdrive.ui.statistics.StatisticsScreen
import com.codetrio.overdrive.viewmodel.AccountViewModel
import com.codetrio.overdrive.viewmodel.ExploreViewModel
import com.codetrio.overdrive.viewmodel.PlayerSharedViewModel
import kotlinx.coroutines.launch

data class LibraryTabItem(
    val key: String,
    val titleRes: Int
)

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    viewModel: PlayerSharedViewModel,
    onEditSong: (SongItem) -> Unit = {},
    onNavigateToExplore: () -> Unit = {}
) {
    val context = LocalContext.current
    val mainActivity = remember(context) { getActivityFromContext(context) as? MainActivity }
    val fragmentActivity = remember(context) { getActivityFromContext(context) as? androidx.fragment.app.FragmentActivity }

    val scope = rememberCoroutineScope()

    val accountViewModel = remember(fragmentActivity) {
        fragmentActivity?.let { ViewModelProvider(it)[AccountViewModel::class.java] }
    }
    val exploreViewModel = remember(fragmentActivity) {
        fragmentActivity?.let { ViewModelProvider(it)[ExploreViewModel::class.java] }
    }

    val userProfile by accountViewModel?.userProfile?.collectAsStateWithLifecycle() ?: remember { mutableStateOf(null) }
    val isLoggedIn = remember(userProfile) { com.codetrio.overdrive.data.innertube.AccountManager.isLoggedIn(context) }

    var activeTab by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }
    var showInlineSearch by remember { mutableStateOf(false) }
    var sortOrder by remember { mutableStateOf(LibrarySortOrder.RECENTLY_ADDED) }
    var viewMode by remember { mutableStateOf(LibraryViewMode.getSavedMode(context)) }

    fun updateViewMode(newMode: LibraryViewMode) {
        viewMode = newMode
        LibraryViewMode.saveMode(context, newMode)
    }

    var isRefreshing by remember { mutableStateOf(false) }
    var showAccountScreen by remember { mutableStateOf(false) }
    var showHistoryScreen by remember { mutableStateOf(false) }
    var showStatsScreen by remember { mutableStateOf(false) }

    val history by accountViewModel?.history?.collectAsStateWithLifecycle(emptyList()) ?: remember { mutableStateOf(emptyList()) }
    val currentOnlineSong by exploreViewModel?.currentOnlineSong?.collectAsStateWithLifecycle(null) ?: remember { mutableStateOf(null) }
    val isLoadingStream by exploreViewModel?.isLoadingStream?.collectAsStateWithLifecycle(false) ?: remember { mutableStateOf(false) }

    val localSongs by viewModel.localSongs.collectAsStateWithLifecycle(emptyList())
    val favoriteIds by viewModel.favoriteSongIds.collectAsStateWithLifecycle(emptySet())
    val currentSong by viewModel.currentSong.collectAsStateWithLifecycle()
    val localPlaylists by viewModel.localPlaylistsFlow.collectAsStateWithLifecycle(emptyList())

    val onlinePlaylists by accountViewModel?.playlists?.collectAsStateWithLifecycle(emptyList()) ?: remember { mutableStateOf(emptyList()) }
    val onlineAlbums by accountViewModel?.albums?.collectAsStateWithLifecycle(emptyList()) ?: remember { mutableStateOf(emptyList()) }
    val onlineArtists by accountViewModel?.artists?.collectAsStateWithLifecycle(emptyList()) ?: remember { mutableStateOf(emptyList()) }
    val onlinePodcasts by accountViewModel?.podcasts?.collectAsStateWithLifecycle(emptyList()) ?: remember { mutableStateOf(emptyList()) }
    val onlineSongs by accountViewModel?.songs?.collectAsStateWithLifecycle(emptyList()) ?: remember { mutableStateOf(emptyList()) }

    var selectedSongForMenu by remember { mutableStateOf<OnlineSong?>(null) }
    var songToAddPlaylist by remember { mutableStateOf<SongItem?>(null) }
    var showAddToPlaylistDialog by remember { mutableStateOf(false) }
    var showCreatePlaylistDialog by remember { mutableStateOf(false) }
    var showCreditsForSong by remember { mutableStateOf<OnlineSong?>(null) }
    var selectedLocalPlaylist by remember { mutableStateOf<PlaylistEntity?>(null) }
    var selectedSongItems by remember { mutableStateOf<Set<SongItem>>(emptySet()) }
    var showBatchAddToPlaylistDialog by remember { mutableStateOf(false) }

    // Trigger local files scan on first composition
    LaunchedEffect(Unit) {
        if (localSongs.isEmpty()) {
            scanLocalFiles(context, viewModel)
        }
    }

    val subscriptionChanged by exploreViewModel?.subscriptionChanged?.collectAsStateWithLifecycle(false) ?: remember { mutableStateOf(false) }

    val listState = rememberLazyListState()
    val gridState = rememberLazyGridState()

    LaunchedEffect(viewModel.scrollToTopEvent) {
        viewModel.scrollToTopEvent.collect {
            launch { listState.animateScrollToItem(0) }
            launch { gridState.animateScrollToItem(0) }
        }
    }

    LaunchedEffect(subscriptionChanged) {
        if (subscriptionChanged) {
            accountViewModel?.loadLibrary()
            exploreViewModel?.consumeSubscriptionChanged()
        }
    }

    val nestedScrollConnection = remember(mainActivity) {
        object : androidx.compose.ui.input.nestedscroll.NestedScrollConnection {
            override fun onPostScroll(
                consumed: androidx.compose.ui.geometry.Offset,
                available: androidx.compose.ui.geometry.Offset,
                source: androidx.compose.ui.input.nestedscroll.NestedScrollSource
            ): androidx.compose.ui.geometry.Offset {
                if (mainActivity != null) {
                    if (consumed.y < -10f) {
                        mainActivity.hideBottomNavWithAnimation()
                    } else if (consumed.y > 10f) {
                        mainActivity.showBottomNavWithAnimation()
                    }
                }
                return super.onPostScroll(consumed, available, source)
            }
        }
    }

    val filterTabs = remember(isLoggedIn) {
        listOf(
            LibraryTabItem("", R.string.lib_tab_all),
            LibraryTabItem("playlists", R.string.lib_tab_playlists),
            LibraryTabItem("albums", R.string.lib_tab_albums),
            LibraryTabItem("songs", R.string.lib_tab_songs),
            LibraryTabItem("artists", R.string.lib_tab_artists),
            LibraryTabItem("downloads", R.string.lib_tab_downloads),
            LibraryTabItem("device_files", R.string.lib_tab_device_files),
            LibraryTabItem("podcasts", R.string.lib_tab_podcasts),
            LibraryTabItem("recap", R.string.lib_tab_recap)
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Header Bar with Title and Actions (Animated for Multi-Selection)
                AnimatedContent(
                    targetState = selectedSongItems.isNotEmpty(),
                    label = "LibraryHeaderTransition"
                ) { isSelecting ->
                    if (isSelecting) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 8.dp, end = 16.dp, top = 8.dp, bottom = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { selectedSongItems = emptySet() }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Cancel selection",
                                        tint = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${selectedSongItems.size} 選択中",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = {
                                    val currentSongs: List<SongItem> = when (activeTab) {
                                        "songs" -> onlineSongs.map { s ->
                                            SongItem.createOnlineSong(s.videoId, s.title, s.artist, "", s.durationMs, s.thumbnailUrl, s.artistId)
                                        }
                                        "downloads", "device_files" -> localSongs
                                        else -> localSongs + onlineSongs.map { s ->
                                            SongItem.createOnlineSong(s.videoId, s.title, s.artist, "", s.durationMs, s.thumbnailUrl, s.artistId)
                                        }
                                    }
                                    if (selectedSongItems.size == currentSongs.size) {
                                        selectedSongItems = emptySet()
                                    } else {
                                        selectedSongItems = currentSongs.toSet()
                                    }
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.SelectAll,
                                        contentDescription = "Select All",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.text_library),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                IconButton(onClick = { showInlineSearch = !showInlineSearch }) {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = "Search",
                                        tint = if (showInlineSearch) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                IconButton(onClick = { showStatsScreen = true }) {
                                    Icon(
                                        imageVector = Icons.Default.BarChart,
                                        contentDescription = "Stats",
                                        tint = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                IconButton(onClick = { showHistoryScreen = true }) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_history),
                                        contentDescription = "History",
                                        tint = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                if (userProfile?.avatarUrl != null) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(LocalContext.current)
                                            .data(userProfile?.avatarUrl)
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = "Account Settings",
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .clickable { showAccountScreen = true },
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    IconButton(onClick = { showAccountScreen = true }) {
                                        Icon(
                                            imageVector = Icons.Default.AccountCircle,
                                            contentDescription = "Account Settings",
                                            tint = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Filter Tabs Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    filterTabs.forEach { tabItem ->
                        val isSelected = activeTab == tabItem.key
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                activeTab = tabItem.key
                            },
                            label = {
                                Text(
                                    text = stringResource(tabItem.titleRes),
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            shape = RoundedCornerShape(12.dp),
                            border = null,
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                                labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }

                // Content Routing
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .nestedScroll(nestedScrollConnection)
                ) {
                    when (activeTab) {
                        "" -> {
                            // "All" / Overview Tab
                            AllOverviewTabContent(
                                listState = listState,
                                gridState = gridState,
                                nestedScrollConnection = nestedScrollConnection,
                                viewModel = viewModel,
                                accountViewModel = accountViewModel,
                                exploreViewModel = exploreViewModel,
                                isLoggedIn = isLoggedIn,
                                onlineSongs = onlineSongs,
                                onlinePlaylists = onlinePlaylists,
                                onlineAlbums = onlineAlbums,
                                onlineArtists = onlineArtists,
                                localPlaylists = localPlaylists,
                                localSongs = localSongs,
                                history = history,
                                favoriteIds = favoriteIds,
                                searchQuery = searchQuery,
                                onSearchQueryChange = { searchQuery = it },
                                showSearch = showInlineSearch,
                                sortOrder = sortOrder,
                                onSortOrderChange = { sortOrder = it },
                                viewMode = viewMode,
                                onViewModeChange = { updateViewMode(it) },
                                onCreatePlaylist = { showCreatePlaylistDialog = true },
                                onLocalPlaylistClick = { selectedLocalPlaylist = it },
                                onOnlinePlaylistClick = { playlist ->
                                    exploreViewModel?.cameFromLibrary = true
                                    exploreViewModel?.loadPlaylist(playlist.playlistId)
                                    navigateToExplore(fragmentActivity)
                                },
                                onAlbumClick = { album ->
                                    exploreViewModel?.cameFromLibrary = true
                                    exploreViewModel?.loadAlbum(album.browseId)
                                    navigateToExplore(fragmentActivity)
                                },
                                onArtistClick = { artist ->
                                    exploreViewModel?.cameFromLibrary = true
                                    exploreViewModel?.loadArtist(artist.browseId, artist.thumbnailUrl)
                                    navigateToExplore(fragmentActivity)
                                },
                                onLikedSongsClick = {
                                    exploreViewModel?.cameFromLibrary = true
                                    exploreViewModel?.loadPlaylist("LM")
                                    navigateToExplore(fragmentActivity)
                                },
                                onLikedSongsShuffle = {
                                    if (onlineSongs.isNotEmpty()) {
                                        val list = onlineSongs.map { s ->
                                            SongItem.createOnlineSong(s.videoId, s.title, s.artist, "", s.durationMs, s.thumbnailUrl, s.artistId)
                                        }.shuffled()
                                        viewModel.setSongList(list)
                                        viewModel.playSong(list.first())
                                    }
                                },
                                onDownloadsClick = { activeTab = "downloads" },
                                onStatsClick = { showStatsScreen = true },
                                onRecentSongClick = { song, queue, index ->
                                    exploreViewModel?.playOnlineSongWithQueue(song, queue, index)
                                }
                            )
                        }
                        "playlists" -> {
                            PlaylistsOverviewContent(
                                gridState = gridState,
                                listState = listState,
                                nestedScrollConnection = nestedScrollConnection,
                                localPlaylists = localPlaylists,
                                onlinePlaylists = onlinePlaylists,
                                searchQuery = searchQuery,
                                onSearchQueryChange = { searchQuery = it },
                                showSearch = showInlineSearch,
                                sortOrder = sortOrder,
                                onSortOrderChange = { sortOrder = it },
                                viewMode = viewMode,
                                onViewModeChange = { updateViewMode(it) },
                                onCreatePlaylist = { showCreatePlaylistDialog = true },
                                onLocalPlaylistClick = { selectedLocalPlaylist = it },
                                onOnlinePlaylistClick = { playlist ->
                                    exploreViewModel?.cameFromLibrary = true
                                    exploreViewModel?.loadPlaylist(playlist.playlistId)
                                    navigateToExplore(fragmentActivity)
                                }
                            )
                        }
                        "albums" -> {
                            AlbumsOverviewContent(
                                gridState = gridState,
                                listState = listState,
                                nestedScrollConnection = nestedScrollConnection,
                                albums = onlineAlbums,
                                searchQuery = searchQuery,
                                onSearchQueryChange = { searchQuery = it },
                                showSearch = showInlineSearch,
                                sortOrder = sortOrder,
                                onSortOrderChange = { sortOrder = it },
                                viewMode = viewMode,
                                onViewModeChange = { updateViewMode(it) },
                                onAlbumClick = { album ->
                                    exploreViewModel?.cameFromLibrary = true
                                    exploreViewModel?.loadAlbum(album.browseId)
                                    navigateToExplore(fragmentActivity)
                                }
                            )
                        }
                        "songs" -> {
                            SongsOverviewContent(
                                listState = listState,
                                gridState = gridState,
                                nestedScrollConnection = nestedScrollConnection,
                                songs = onlineSongs,
                                searchQuery = searchQuery,
                                onSearchQueryChange = { searchQuery = it },
                                showSearch = showInlineSearch,
                                sortOrder = sortOrder,
                                onSortOrderChange = { sortOrder = it },
                                viewMode = viewMode,
                                onViewModeChange = { updateViewMode(it) },
                                viewModel = viewModel,
                                selectedSongItems = selectedSongItems,
                                onToggleSelect = { songItem ->
                                    selectedSongItems = if (selectedSongItems.any { it.id == songItem.id }) {
                                        selectedSongItems.filter { it.id != songItem.id }.toSet()
                                    } else {
                                        selectedSongItems + songItem
                                    }
                                },
                                onSongMenuClick = { selectedSongForMenu = it }
                            )
                        }
                        "artists" -> {
                            ArtistsOverviewContent(
                                gridState = gridState,
                                listState = listState,
                                nestedScrollConnection = nestedScrollConnection,
                                artists = onlineArtists,
                                searchQuery = searchQuery,
                                onSearchQueryChange = { searchQuery = it },
                                showSearch = showInlineSearch,
                                sortOrder = sortOrder,
                                onSortOrderChange = { sortOrder = it },
                                viewMode = viewMode,
                                onViewModeChange = { updateViewMode(it) },
                                onArtistClick = { artist ->
                                    exploreViewModel?.cameFromLibrary = true
                                    exploreViewModel?.loadArtist(artist.browseId, artist.thumbnailUrl)
                                    navigateToExplore(fragmentActivity)
                                }
                            )
                        }
                        "downloads" -> {
                            DownloadsTabContent(
                                listState = listState,
                                gridState = gridState,
                                nestedScrollConnection = nestedScrollConnection,
                                viewModel = viewModel,
                                favoriteIds = favoriteIds,
                                searchQuery = searchQuery,
                                onSearchQueryChange = { searchQuery = it },
                                showSearch = showInlineSearch,
                                sortOrder = sortOrder,
                                onSortOrderChange = { sortOrder = it },
                                viewMode = viewMode,
                                onViewModeChange = { updateViewMode(it) },
                                selectedSongItems = selectedSongItems,
                                onToggleSelect = { songItem ->
                                    selectedSongItems = if (selectedSongItems.any { it.id == songItem.id }) {
                                        selectedSongItems.filter { it.id != songItem.id }.toSet()
                                    } else {
                                        selectedSongItems + songItem
                                    }
                                }
                            )
                        }
                        "device_files" -> {
                            val onRefreshAction = {
                                scope.launch {
                                    isRefreshing = true
                                    scanLocalFiles(context, viewModel)
                                    isRefreshing = false
                                }
                            }
                            val pullToRefreshState = rememberPullToRefreshState()

                            PullToRefreshBox(
                                isRefreshing = isRefreshing,
                                onRefresh = { onRefreshAction() },
                                state = pullToRefreshState,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                DeviceFilesTabContent(
                                    listState = listState,
                                    gridState = gridState,
                                    nestedScrollConnection = nestedScrollConnection,
                                    localSongs = localSongs,
                                    searchQuery = searchQuery,
                                    onSearchQueryChange = { searchQuery = it },
                                    showSearch = showInlineSearch,
                                    sortOrder = sortOrder,
                                    onSortOrderChange = { sortOrder = it },
                                    viewMode = viewMode,
                                    onViewModeChange = { updateViewMode(it) },
                                    viewModel = viewModel,
                                    currentSong = currentSong,
                                    favoriteIds = favoriteIds,
                                    selectedSongItems = selectedSongItems,
                                    onToggleSelect = { songItem ->
                                        selectedSongItems = if (selectedSongItems.any { it.id == songItem.id }) {
                                            selectedSongItems.filter { it.id != songItem.id }.toSet()
                                        } else {
                                            selectedSongItems + songItem
                                        }
                                    }
                                )
                            }
                        }
                        "podcasts" -> {
                            PodcastsOverviewContent(
                                gridState = gridState,
                                listState = listState,
                                nestedScrollConnection = nestedScrollConnection,
                                podcasts = onlinePodcasts,
                                searchQuery = searchQuery,
                                onSearchQueryChange = { searchQuery = it },
                                showSearch = showInlineSearch,
                                sortOrder = sortOrder,
                                onSortOrderChange = { sortOrder = it },
                                viewMode = viewMode,
                                onViewModeChange = { updateViewMode(it) },
                                onPodcastClick = { podcast ->
                                    exploreViewModel?.cameFromLibrary = true
                                    exploreViewModel?.loadPlaylist(podcast.playlistId)
                                    navigateToExplore(fragmentActivity)
                                }
                            )
                        }
                        "recap" -> {
                            StatisticsScreen(
                                playerViewModel = viewModel,
                                onNavigateToExplore = onNavigateToExplore
                            )
                        }
                    }
                }
            }

            // Dialogs & Bottom Sheets
            if (showCreatePlaylistDialog) {
                var name by remember { mutableStateOf("") }
                AlertDialog(
                    onDismissRequest = { showCreatePlaylistDialog = false },
                    title = { Text(stringResource(R.string.text_create_new_playlist), fontWeight = FontWeight.Bold) },
                    text = {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text(stringResource(R.string.text_playlist_name)) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (name.isNotBlank()) {
                                    viewModel.createLocalPlaylist(name)
                                    showCreatePlaylistDialog = false
                                }
                            },
                            enabled = name.isNotBlank()
                        ) {
                            Text(stringResource(R.string.text_create))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showCreatePlaylistDialog = false }) {
                            Text(stringResource(R.string.action_cancel))
                        }
                    }
                )
            }

            selectedLocalPlaylist?.let { playlist ->
                LocalPlaylistDetailsBottomSheet(
                    playlist = playlist,
                    viewModel = viewModel,
                    onDismiss = { selectedLocalPlaylist = null }
                )
            }

            // Song 3-Dot Menu Bottom Sheet
            selectedSongForMenu?.let { song ->
                val isPinned = exploreViewModel?.uiState?.collectAsState()?.value?.pinnedSpeedDialIds?.contains(song.videoId) == true
                if (exploreViewModel != null) {
                    OnlineSongBottomSheet(
                        song = song,
                        isPinned = isPinned,
                        onDismissRequest = { selectedSongForMenu = null },
                        playerSharedViewModel = viewModel,
                        exploreViewModel = exploreViewModel,
                        onPlaylistAddClick = { onlineSongItem ->
                            songToAddPlaylist = onlineSongItem
                            showAddToPlaylistDialog = true
                        },
                        onViewCreditsClick = { s ->
                            showCreditsForSong = s
                        }
                    )
                }
            }

            if (showAddToPlaylistDialog && songToAddPlaylist != null) {
                LocalPlaylistPickerDialog(
                    playlists = localPlaylists,
                    onCreateNew = {
                        showCreatePlaylistDialog = true
                        showAddToPlaylistDialog = false
                    },
                    onPlaylistSelected = { playlist ->
                        viewModel.addSongToLocalPlaylist(playlist.id, songToAddPlaylist!!)
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

            if (showBatchAddToPlaylistDialog && selectedSongItems.isNotEmpty()) {
                LocalPlaylistPickerDialog(
                    playlists = localPlaylists,
                    onCreateNew = {
                        showCreatePlaylistDialog = true
                        showBatchAddToPlaylistDialog = false
                    },
                    onPlaylistSelected = { playlist ->
                        viewModel.addSongsToLocalPlaylist(playlist.id, selectedSongItems.toList())
                        com.codetrio.overdrive.ui.SnackbarController.showMessage("${selectedSongItems.size}曲を「${playlist.title}」に追加しました")
                        showBatchAddToPlaylistDialog = false
                        selectedSongItems = emptySet()
                    },
                    onDismiss = {
                        showBatchAddToPlaylistDialog = false
                    }
                )
            }

            // Material 3 Expressive Multi-Selection Floating Action Bar
            AnimatedVisibility(
                visible = selectedSongItems.isNotEmpty(),
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 100.dp, start = 16.dp, end = 16.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                    shadowElevation = 8.dp,
                    tonalElevation = 6.dp,
                    modifier = Modifier.height(56.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "${selectedSongItems.size}曲",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(end = 4.dp)
                        )

                        VerticalDivider(
                            modifier = Modifier
                                .height(24.dp)
                                .padding(horizontal = 4.dp)
                        )

                        // Add to Local Playlist
                        IconButton(onClick = { showBatchAddToPlaylistDialog = true }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.PlaylistAdd,
                                contentDescription = "Add to playlist",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Play Next
                        IconButton(onClick = {
                            val count = selectedSongItems.size
                            viewModel.addSongsToQueueNext(selectedSongItems.toList())
                            com.codetrio.overdrive.ui.SnackbarController.showMessage("${count}曲を次に再生に追加しました")
                            selectedSongItems = emptySet()
                        }) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Play Next",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Add to Queue End
                        IconButton(onClick = {
                            val count = selectedSongItems.size
                            viewModel.addSongsToQueue(selectedSongItems.toList())
                            com.codetrio.overdrive.ui.SnackbarController.showMessage("${count}曲をキューに追加しました")
                            selectedSongItems = emptySet()
                        }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.QueueMusic,
                                contentDescription = "Add to Queue",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Share
                        IconButton(onClick = {
                            val shareText = selectedSongItems.joinToString("\n") { "${it.title} - ${it.artist}" }
                            val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(android.content.Intent.EXTRA_TEXT, shareText)
                            }
                            context.startActivity(android.content.Intent.createChooser(intent, "Share Songs"))
                        }) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Overlay Fullscreen Screens (Stats, Account, History, Credits)
            AnimatedVisibility(
                visible = showStatsScreen,
                enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn(),
                exit = slideOutHorizontally(targetOffsetX = { it }) + fadeOut(),
                modifier = Modifier.fillMaxSize()
            ) {
                StatisticsScreen(
                    playerViewModel = viewModel,
                    onNavigateToExplore = {
                        showStatsScreen = false
                        onNavigateToExplore()
                    }
                )
            }

            AnimatedVisibility(
                visible = showAccountScreen && accountViewModel != null,
                enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn(),
                exit = slideOutHorizontally(targetOffsetX = { it }) + fadeOut(),
                modifier = Modifier.fillMaxSize()
            ) {
                if (accountViewModel != null) {
                    AccountScreen(
                        viewModel = accountViewModel,
                        onBack = { showAccountScreen = false },
                        onSongClick = { song, queue, index ->
                            exploreViewModel?.playOnlineSongWithQueue(song, queue, index)
                        },
                        onNavigateToSignIn = {
                            showAccountScreen = false
                            mainActivity?.navigateToGoogleSignIn()
                        }
                    )
                }
            }

            AnimatedVisibility(
                visible = showHistoryScreen,
                enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn(),
                exit = slideOutHorizontally(targetOffsetX = { it }) + fadeOut(),
                modifier = Modifier.fillMaxSize()
            ) {
                HistoryScreen(
                    history = history,
                    currentOnlineSong = currentOnlineSong,
                    isLoadingStream = isLoadingStream,
                    isRefreshing = accountViewModel?.isLoading?.collectAsStateWithLifecycle()?.value == true,
                    onRefresh = { accountViewModel?.loadLibrary() },
                    onBack = { showHistoryScreen = false },
                    onSongClick = { song, queue, index ->
                        exploreViewModel?.playOnlineSongWithQueue(song, queue, index)
                    },
                    onSongMenuClick = { song ->
                        selectedSongForMenu = song
                    }
                )
            }

            AnimatedVisibility(
                visible = showCreditsForSong != null,
                enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn(),
                exit = slideOutHorizontally(targetOffsetX = { it }) + fadeOut(),
                modifier = Modifier.fillMaxSize()
            ) {
                showCreditsForSong?.let { song ->
                    SongCreditsScreen(
                        song = song,
                        onBack = { showCreditsForSong = null }
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// Tab Contents & Adaptive Renderers
// -------------------------------------------------------------

interface UnifiedItem {
    val id: String
    val title: String
    val subtitle: String
    val thumbnailUrl: String?
    val thumbnails: List<String>
    val isCircle: Boolean
    val onClick: () -> Unit
}

private fun getAdaptiveColumns(mode: LibraryViewMode, screenWidth: Int): Int {
    return when (mode) {
        LibraryViewMode.LARGE_GRID -> when {
            screenWidth >= 1200 -> 6
            screenWidth >= 840 -> 4
            screenWidth >= 600 -> 3
            else -> 2
        }
        LibraryViewMode.COMPACT_GRID -> when {
            screenWidth >= 1200 -> 9
            screenWidth >= 840 -> 6
            screenWidth >= 600 -> 4
            else -> 3
        }
        LibraryViewMode.STANDARD_LIST,
        LibraryViewMode.COMPACT_LIST -> when {
            screenWidth >= 840 -> 2
            else -> 1
        }
    }
}

@Composable
private fun AllOverviewTabContent(
    listState: androidx.compose.foundation.lazy.LazyListState,
    gridState: androidx.compose.foundation.lazy.grid.LazyGridState,
    nestedScrollConnection: androidx.compose.ui.input.nestedscroll.NestedScrollConnection,
    viewModel: PlayerSharedViewModel,
    accountViewModel: AccountViewModel?,
    exploreViewModel: ExploreViewModel?,
    isLoggedIn: Boolean,
    onlineSongs: List<OnlineSong>,
    onlinePlaylists: List<OnlinePlaylist>,
    onlineAlbums: List<OnlineAlbum>,
    onlineArtists: List<OnlineArtist>,
    localPlaylists: List<PlaylistEntity>,
    localSongs: List<SongItem>,
    history: List<OnlineSong>,
    favoriteIds: Set<Long>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    showSearch: Boolean,
    sortOrder: LibrarySortOrder,
    onSortOrderChange: (LibrarySortOrder) -> Unit,
    viewMode: LibraryViewMode,
    onViewModeChange: (LibraryViewMode) -> Unit,
    onCreatePlaylist: () -> Unit,
    onLocalPlaylistClick: (PlaylistEntity) -> Unit,
    onOnlinePlaylistClick: (OnlinePlaylist) -> Unit,
    onAlbumClick: (OnlineAlbum) -> Unit,
    onArtistClick: (OnlineArtist) -> Unit,
    onLikedSongsClick: () -> Unit,
    onLikedSongsShuffle: () -> Unit,
    onDownloadsClick: () -> Unit,
    onStatsClick: () -> Unit,
    onRecentSongClick: (OnlineSong, List<OnlineSong>, Int) -> Unit
) {
    val allUnifiedItems = remember(
        onlinePlaylists, onlineAlbums, onlineArtists, localPlaylists, localSongs, searchQuery, sortOrder
    ) {
        val list = mutableListOf<UnifiedItem>()

        localPlaylists.forEach { playlist ->
            list.add(object : UnifiedItem {
                override val id = "local_pl_${playlist.id}"
                override val title = playlist.title
                override val subtitle = "Local Playlist"
                override val thumbnailUrl = null
                override val thumbnails = emptyList<String>()
                override val isCircle = false
                override val onClick = { onLocalPlaylistClick(playlist) }
            })
        }

        onlinePlaylists.forEach { playlist ->
            list.add(object : UnifiedItem {
                override val id = "online_pl_${playlist.playlistId}"
                override val title = playlist.title
                override val subtitle = playlist.songCount ?: "Playlist"
                override val thumbnailUrl = playlist.thumbnailUrl
                override val thumbnails = listOfNotNull(playlist.thumbnailUrl)
                override val isCircle = false
                override val onClick = { onOnlinePlaylistClick(playlist) }
            })
        }

        onlineAlbums.forEach { album ->
            list.add(object : UnifiedItem {
                override val id = "online_al_${album.browseId}"
                override val title = album.title
                override val subtitle = album.artists.firstOrNull()?.name ?: "Album"
                override val thumbnailUrl = album.thumbnailUrl
                override val thumbnails = listOfNotNull(album.thumbnailUrl)
                override val isCircle = false
                override val onClick = { onAlbumClick(album) }
            })
        }

        onlineArtists.forEach { artist ->
            list.add(object : UnifiedItem {
                override val id = "online_ar_${artist.browseId}"
                override val title = artist.title
                override val subtitle = "Artist"
                override val thumbnailUrl = artist.thumbnailUrl
                override val thumbnails = listOfNotNull(artist.thumbnailUrl)
                override val isCircle = true
                override val onClick = { onArtistClick(artist) }
            })
        }

        val filtered = if (searchQuery.isBlank()) list else {
            list.filter { it.title.contains(searchQuery, ignoreCase = true) || it.subtitle.contains(searchQuery, ignoreCase = true) }
        }

        when (sortOrder) {
            LibrarySortOrder.TITLE -> filtered.sortedBy { it.title.lowercase() }
            LibrarySortOrder.ARTIST -> filtered.sortedBy { it.subtitle.lowercase() }
            else -> filtered
        }
    }

    val screenWidth = androidx.compose.ui.platform.LocalConfiguration.current.screenWidthDp
    val columns = getAdaptiveColumns(viewMode, screenWidth)

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(nestedScrollConnection),
        contentPadding = PaddingValues(bottom = 160.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 1. Hero Quick Access Section (Visible when no search active)
        if (searchQuery.isBlank()) {
            item {
                LibraryQuickAccessHero(
                    likedSongsCount = onlineSongs.size + localSongs.count { favoriteIds.contains(it.id) },
                    likedSongsThumbnails = onlineSongs.take(4).mapNotNull { it.thumbnailUrl },
                    onLikedSongsClick = onLikedSongsClick,
                    onLikedSongsShuffle = onLikedSongsShuffle,
                    downloadedCount = localSongs.size,
                    onDownloadsClick = onDownloadsClick,
                    onStatsClick = onStatsClick,
                    recentSongs = history,
                    onRecentSongClick = onRecentSongClick
                )
            }
        }

        // 2. Universal Toolbar (Search & Sort & 4-tier ViewMode Switcher)
        item {
            LibraryToolbar(
                searchQuery = searchQuery,
                onSearchQueryChange = onSearchQueryChange,
                sortOrder = sortOrder,
                onSortOrderChange = onSortOrderChange,
                viewMode = viewMode,
                onViewModeChange = onViewModeChange,
                onCreatePlaylist = onCreatePlaylist,
                showSearch = showSearch
            )
        }

        // 3. Adaptive Items Rendering based on ViewMode (Large Grid / Compact Grid / Standard List / Compact List)
        when (viewMode) {
            LibraryViewMode.LARGE_GRID -> {
                val chunked = allUnifiedItems.chunked(columns)
                items(chunked.size, key = { "all_lg_row_$it" }) { rowIndex ->
                    val rowItems = chunked[rowIndex]
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 5.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        rowItems.forEach { item ->
                            Box(modifier = Modifier.weight(1f)) {
                                LargeGridCard(item)
                            }
                        }
                        repeat(columns - rowItems.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
            LibraryViewMode.COMPACT_GRID -> {
                val chunked = allUnifiedItems.chunked(columns)
                items(chunked.size, key = { "all_cg_row_$it" }) { rowIndex ->
                    val rowItems = chunked[rowIndex]
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        rowItems.forEach { item ->
                            Box(modifier = Modifier.weight(1f)) {
                                CompactGridCard(item)
                            }
                        }
                        repeat(columns - rowItems.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
            LibraryViewMode.STANDARD_LIST -> {
                if (columns > 1) {
                    val chunked = allUnifiedItems.chunked(columns)
                    items(chunked.size, key = { "all_sl_row_$it" }) { rowIndex ->
                        val rowItems = chunked[rowIndex]
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 2.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowItems.forEach { item ->
                                Box(modifier = Modifier.weight(1f)) {
                                    StandardListItem(item)
                                }
                            }
                            repeat(columns - rowItems.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                } else {
                    items(allUnifiedItems, key = { it.id }) { item ->
                        StandardListItem(item)
                    }
                }
            }
            LibraryViewMode.COMPACT_LIST -> {
                if (columns > 1) {
                    val chunked = allUnifiedItems.chunked(columns)
                    items(chunked.size, key = { "all_cl_row_$it" }) { rowIndex ->
                        val rowItems = chunked[rowIndex]
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 1.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowItems.forEach { item ->
                                Box(modifier = Modifier.weight(1f)) {
                                    CompactDensityListItem(item)
                                }
                            }
                            repeat(columns - rowItems.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                } else {
                    items(allUnifiedItems, key = { it.id }) { item ->
                        CompactDensityListItem(item)
                    }
                }
            }
        }
    }
}

@Composable
private fun PlaylistsOverviewContent(
    gridState: androidx.compose.foundation.lazy.grid.LazyGridState,
    listState: androidx.compose.foundation.lazy.LazyListState,
    nestedScrollConnection: androidx.compose.ui.input.nestedscroll.NestedScrollConnection,
    localPlaylists: List<PlaylistEntity>,
    onlinePlaylists: List<OnlinePlaylist>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    showSearch: Boolean,
    sortOrder: LibrarySortOrder,
    onSortOrderChange: (LibrarySortOrder) -> Unit,
    viewMode: LibraryViewMode,
    onViewModeChange: (LibraryViewMode) -> Unit,
    onCreatePlaylist: () -> Unit,
    onLocalPlaylistClick: (PlaylistEntity) -> Unit,
    onOnlinePlaylistClick: (OnlinePlaylist) -> Unit
) {
    val items = remember(localPlaylists, onlinePlaylists, searchQuery, sortOrder) {
        val list = mutableListOf<UnifiedItem>()
        localPlaylists.forEach { playlist ->
            list.add(object : UnifiedItem {
                override val id = "local_${playlist.id}"
                override val title = playlist.title
                override val subtitle = "Local Playlist"
                override val thumbnailUrl = null
                override val thumbnails = emptyList<String>()
                override val isCircle = false
                override val onClick = { onLocalPlaylistClick(playlist) }
            })
        }
        onlinePlaylists.forEach { playlist ->
            list.add(object : UnifiedItem {
                override val id = "online_${playlist.playlistId}"
                override val title = playlist.title
                override val subtitle = playlist.songCount ?: "Playlist"
                override val thumbnailUrl = playlist.thumbnailUrl
                override val thumbnails = listOfNotNull(playlist.thumbnailUrl)
                override val isCircle = false
                override val onClick = { onOnlinePlaylistClick(playlist) }
            })
        }

        val filtered = if (searchQuery.isBlank()) list else {
            list.filter { it.title.contains(searchQuery, ignoreCase = true) }
        }
        when (sortOrder) {
            LibrarySortOrder.TITLE -> filtered.sortedBy { it.title.lowercase() }
            else -> filtered
        }
    }

    val screenWidth = androidx.compose.ui.platform.LocalConfiguration.current.screenWidthDp
    val columns = getAdaptiveColumns(viewMode, screenWidth)

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(nestedScrollConnection),
        contentPadding = PaddingValues(bottom = 160.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            LibraryToolbar(
                searchQuery = searchQuery,
                onSearchQueryChange = onSearchQueryChange,
                sortOrder = sortOrder,
                onSortOrderChange = onSortOrderChange,
                viewMode = viewMode,
                onViewModeChange = onViewModeChange,
                onCreatePlaylist = onCreatePlaylist,
                showSearch = showSearch
            )
        }

        when (viewMode) {
            LibraryViewMode.LARGE_GRID -> {
                val chunked = items.chunked(columns)
                items(chunked.size, key = { "pl_lg_row_$it" }) { rowIndex ->
                    val row = chunked[rowIndex]
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 5.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        row.forEach { item -> Box(Modifier.weight(1f)) { LargeGridCard(item) } }
                        repeat(columns - row.size) { Spacer(Modifier.weight(1f)) }
                    }
                }
            }
            LibraryViewMode.COMPACT_GRID -> {
                val chunked = items.chunked(columns)
                items(chunked.size, key = { "pl_cg_row_$it" }) { rowIndex ->
                    val row = chunked[rowIndex]
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        row.forEach { item -> Box(Modifier.weight(1f)) { CompactGridCard(item) } }
                        repeat(columns - row.size) { Spacer(Modifier.weight(1f)) }
                    }
                }
            }
            LibraryViewMode.STANDARD_LIST -> {
                if (columns > 1) {
                    val chunked = items.chunked(columns)
                    items(chunked.size, key = { "pl_sl_row_$it" }) { rowIndex ->
                        val row = chunked[rowIndex]
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 2.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            row.forEach { item -> Box(Modifier.weight(1f)) { StandardListItem(item) } }
                            repeat(columns - row.size) { Spacer(Modifier.weight(1f)) }
                        }
                    }
                } else {
                    items(items, key = { it.id }) { item -> StandardListItem(item) }
                }
            }
            LibraryViewMode.COMPACT_LIST -> {
                if (columns > 1) {
                    val chunked = items.chunked(columns)
                    items(chunked.size, key = { "pl_cl_row_$it" }) { rowIndex ->
                        val row = chunked[rowIndex]
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 1.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            row.forEach { item -> Box(Modifier.weight(1f)) { CompactDensityListItem(item) } }
                            repeat(columns - row.size) { Spacer(Modifier.weight(1f)) }
                        }
                    }
                } else {
                    items(items, key = { it.id }) { item -> CompactDensityListItem(item) }
                }
            }
        }
    }
}

@Composable
private fun AlbumsOverviewContent(
    gridState: androidx.compose.foundation.lazy.grid.LazyGridState,
    listState: androidx.compose.foundation.lazy.LazyListState,
    nestedScrollConnection: androidx.compose.ui.input.nestedscroll.NestedScrollConnection,
    albums: List<OnlineAlbum>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    showSearch: Boolean,
    sortOrder: LibrarySortOrder,
    onSortOrderChange: (LibrarySortOrder) -> Unit,
    viewMode: LibraryViewMode,
    onViewModeChange: (LibraryViewMode) -> Unit,
    onAlbumClick: (OnlineAlbum) -> Unit
) {
    val items = remember(albums, searchQuery, sortOrder) {
        val list = albums.map { album ->
            object : UnifiedItem {
                override val id = "album_${album.browseId}"
                override val title = album.title
                override val subtitle = album.artists.firstOrNull()?.name ?: "Album"
                override val thumbnailUrl = album.thumbnailUrl
                override val thumbnails = listOfNotNull(album.thumbnailUrl)
                override val isCircle = false
                override val onClick = { onAlbumClick(album) }
            }
        }
        val filtered = if (searchQuery.isBlank()) list else list.filter { it.title.contains(searchQuery, ignoreCase = true) || it.subtitle.contains(searchQuery, ignoreCase = true) }
        when (sortOrder) {
            LibrarySortOrder.TITLE -> filtered.sortedBy { it.title.lowercase() }
            LibrarySortOrder.ARTIST -> filtered.sortedBy { it.subtitle.lowercase() }
            else -> filtered
        }
    }

    val screenWidth = androidx.compose.ui.platform.LocalConfiguration.current.screenWidthDp
    val columns = getAdaptiveColumns(viewMode, screenWidth)

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(nestedScrollConnection),
        contentPadding = PaddingValues(bottom = 160.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            LibraryToolbar(
                searchQuery = searchQuery,
                onSearchQueryChange = onSearchQueryChange,
                sortOrder = sortOrder,
                onSortOrderChange = onSortOrderChange,
                viewMode = viewMode,
                onViewModeChange = onViewModeChange,
                onCreatePlaylist = {},
                showSearch = showSearch,
                showNewPlaylist = false
            )
        }

        when (viewMode) {
            LibraryViewMode.LARGE_GRID -> {
                val chunked = items.chunked(columns)
                items(chunked.size, key = { "al_lg_row_$it" }) { rowIndex ->
                    val row = chunked[rowIndex]
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 5.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        row.forEach { item -> Box(Modifier.weight(1f)) { LargeGridCard(item) } }
                        repeat(columns - row.size) { Spacer(Modifier.weight(1f)) }
                    }
                }
            }
            LibraryViewMode.COMPACT_GRID -> {
                val chunked = items.chunked(columns)
                items(chunked.size, key = { "al_cg_row_$it" }) { rowIndex ->
                    val row = chunked[rowIndex]
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        row.forEach { item -> Box(Modifier.weight(1f)) { CompactGridCard(item) } }
                        repeat(columns - row.size) { Spacer(Modifier.weight(1f)) }
                    }
                }
            }
            LibraryViewMode.STANDARD_LIST -> {
                if (columns > 1) {
                    val chunked = items.chunked(columns)
                    items(chunked.size, key = { "al_sl_row_$it" }) { rowIndex ->
                        val row = chunked[rowIndex]
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 2.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            row.forEach { item -> Box(Modifier.weight(1f)) { StandardListItem(item) } }
                            repeat(columns - row.size) { Spacer(Modifier.weight(1f)) }
                        }
                    }
                } else {
                    items(items, key = { it.id }) { item -> StandardListItem(item) }
                }
            }
            LibraryViewMode.COMPACT_LIST -> {
                if (columns > 1) {
                    val chunked = items.chunked(columns)
                    items(chunked.size, key = { "al_cl_row_$it" }) { rowIndex ->
                        val row = chunked[rowIndex]
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 1.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            row.forEach { item -> Box(Modifier.weight(1f)) { CompactDensityListItem(item) } }
                            repeat(columns - row.size) { Spacer(Modifier.weight(1f)) }
                        }
                    }
                } else {
                    items(items, key = { it.id }) { item -> CompactDensityListItem(item) }
                }
            }
        }
    }
}

@Composable
private fun SongsOverviewContent(
    listState: androidx.compose.foundation.lazy.LazyListState,
    gridState: androidx.compose.foundation.lazy.grid.LazyGridState,
    nestedScrollConnection: androidx.compose.ui.input.nestedscroll.NestedScrollConnection,
    songs: List<OnlineSong>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    showSearch: Boolean,
    sortOrder: LibrarySortOrder,
    onSortOrderChange: (LibrarySortOrder) -> Unit,
    viewMode: LibraryViewMode,
    onViewModeChange: (LibraryViewMode) -> Unit,
    viewModel: PlayerSharedViewModel,
    selectedSongItems: Set<SongItem>,
    onToggleSelect: (SongItem) -> Unit,
    onSongMenuClick: (OnlineSong) -> Unit
) {
    val filteredSongs = remember(songs, searchQuery, sortOrder) {
        val list = if (searchQuery.isBlank()) songs else songs.filter {
            it.title.contains(searchQuery, ignoreCase = true) || it.artist.contains(searchQuery, ignoreCase = true)
        }
        when (sortOrder) {
            LibrarySortOrder.TITLE -> list.sortedBy { it.title.lowercase() }
            LibrarySortOrder.ARTIST -> list.sortedBy { it.artist.lowercase() }
            LibrarySortOrder.DURATION -> list.sortedByDescending { it.durationMs }
            else -> list
        }
    }

    val isMultiSelectMode = selectedSongItems.isNotEmpty()
    val screenWidth = androidx.compose.ui.platform.LocalConfiguration.current.screenWidthDp
    val columns = getAdaptiveColumns(viewMode, screenWidth)

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(nestedScrollConnection),
        contentPadding = PaddingValues(bottom = 160.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        item {
            LibraryToolbar(
                searchQuery = searchQuery,
                onSearchQueryChange = onSearchQueryChange,
                sortOrder = sortOrder,
                onSortOrderChange = onSortOrderChange,
                viewMode = viewMode,
                onViewModeChange = onViewModeChange,
                onCreatePlaylist = {},
                showSearch = showSearch,
                showNewPlaylist = false
            )
        }

        if (columns > 1 && (viewMode == LibraryViewMode.STANDARD_LIST || viewMode == LibraryViewMode.COMPACT_LIST)) {
            val chunked = filteredSongs.chunked(columns)
            items(chunked.size, key = { "songs_row_$it" }) { rowIndex ->
                val row = chunked[rowIndex]
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    row.forEach { song ->
                        val isCompact = viewMode == LibraryViewMode.COMPACT_LIST
                        val songItem = remember(song) {
                            SongItem.createOnlineSong(song.videoId, song.title, song.artist, "", song.durationMs, song.thumbnailUrl, song.artistId)
                        }
                        val isSelected = selectedSongItems.any { it.id == songItem.id }
                        Box(modifier = Modifier.weight(1f)) {
                            SongItemRow(
                                song = song,
                                isCompact = isCompact,
                                isMultiSelectMode = isMultiSelectMode,
                                isSelected = isSelected,
                                onClick = {
                                    if (isMultiSelectMode) {
                                        onToggleSelect(songItem)
                                    } else {
                                        val queue = filteredSongs.map { s ->
                                            SongItem.createOnlineSong(s.videoId, s.title, s.artist, "", s.durationMs, s.thumbnailUrl, s.artistId)
                                        }
                                        viewModel.setSongList(queue)
                                        val idx = queue.indexOfFirst { it.videoId == song.videoId }
                                        if (idx != -1) viewModel.playSongAtIndex(idx)
                                    }
                                },
                                onLongClick = {
                                    onToggleSelect(songItem)
                                },
                                onMenuClick = { onSongMenuClick(song) }
                            )
                        }
                    }
                    repeat(columns - row.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        } else {
            items(filteredSongs, key = { it.videoId }) { song ->
                val isCompact = viewMode == LibraryViewMode.COMPACT_LIST
                val songItem = remember(song) {
                    SongItem.createOnlineSong(song.videoId, song.title, song.artist, "", song.durationMs, song.thumbnailUrl, song.artistId)
                }
                val isSelected = selectedSongItems.any { it.id == songItem.id }

                SongItemRow(
                    song = song,
                    isCompact = isCompact,
                    isMultiSelectMode = isMultiSelectMode,
                    isSelected = isSelected,
                    onClick = {
                        if (isMultiSelectMode) {
                            onToggleSelect(songItem)
                        } else {
                            val queue = filteredSongs.map { s ->
                                SongItem.createOnlineSong(s.videoId, s.title, s.artist, "", s.durationMs, s.thumbnailUrl, s.artistId)
                            }
                            viewModel.setSongList(queue)
                            val idx = queue.indexOfFirst { it.videoId == song.videoId }
                            if (idx != -1) viewModel.playSongAtIndex(idx)
                        }
                    },
                    onLongClick = {
                        onToggleSelect(songItem)
                    },
                    onMenuClick = { onSongMenuClick(song) }
                )
            }
        }
    }
}

@Composable
private fun ArtistsOverviewContent(
    gridState: androidx.compose.foundation.lazy.grid.LazyGridState,
    listState: androidx.compose.foundation.lazy.LazyListState,
    nestedScrollConnection: androidx.compose.ui.input.nestedscroll.NestedScrollConnection,
    artists: List<OnlineArtist>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    showSearch: Boolean,
    sortOrder: LibrarySortOrder,
    onSortOrderChange: (LibrarySortOrder) -> Unit,
    viewMode: LibraryViewMode,
    onViewModeChange: (LibraryViewMode) -> Unit,
    onArtistClick: (OnlineArtist) -> Unit
) {
    val items = remember(artists, searchQuery, sortOrder) {
        val list = artists.map { artist ->
            object : UnifiedItem {
                override val id = "artist_${artist.browseId}"
                override val title = artist.title
                override val subtitle = "Artist"
                override val thumbnailUrl = artist.thumbnailUrl
                override val thumbnails = listOfNotNull(artist.thumbnailUrl)
                override val isCircle = true
                override val onClick = { onArtistClick(artist) }
            }
        }
        val filtered = if (searchQuery.isBlank()) list else list.filter { it.title.contains(searchQuery, ignoreCase = true) }
        when (sortOrder) {
            LibrarySortOrder.TITLE -> filtered.sortedBy { it.title.lowercase() }
            else -> filtered
        }
    }

    val screenWidth = androidx.compose.ui.platform.LocalConfiguration.current.screenWidthDp
    val columns = getAdaptiveColumns(viewMode, screenWidth)

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(nestedScrollConnection),
        contentPadding = PaddingValues(bottom = 160.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            LibraryToolbar(
                searchQuery = searchQuery,
                onSearchQueryChange = onSearchQueryChange,
                sortOrder = sortOrder,
                onSortOrderChange = onSortOrderChange,
                viewMode = viewMode,
                onViewModeChange = onViewModeChange,
                onCreatePlaylist = {},
                showSearch = showSearch,
                showNewPlaylist = false
            )
        }

        when (viewMode) {
            LibraryViewMode.LARGE_GRID -> {
                val chunked = items.chunked(columns)
                items(chunked.size, key = { "ar_lg_row_$it" }) { rowIndex ->
                    val row = chunked[rowIndex]
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 5.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        row.forEach { item -> Box(Modifier.weight(1f)) { LargeGridCard(item) } }
                        repeat(columns - row.size) { Spacer(Modifier.weight(1f)) }
                    }
                }
            }
            LibraryViewMode.COMPACT_GRID -> {
                val chunked = items.chunked(columns)
                items(chunked.size, key = { "ar_cg_row_$it" }) { rowIndex ->
                    val row = chunked[rowIndex]
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        row.forEach { item -> Box(Modifier.weight(1f)) { CompactGridCard(item) } }
                        repeat(columns - row.size) { Spacer(Modifier.weight(1f)) }
                    }
                }
            }
            LibraryViewMode.STANDARD_LIST -> {
                if (columns > 1) {
                    val chunked = items.chunked(columns)
                    items(chunked.size, key = { "ar_sl_row_$it" }) { rowIndex ->
                        val row = chunked[rowIndex]
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 2.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            row.forEach { item -> Box(Modifier.weight(1f)) { StandardListItem(item) } }
                            repeat(columns - row.size) { Spacer(Modifier.weight(1f)) }
                        }
                    }
                } else {
                    items(items, key = { it.id }) { item -> StandardListItem(item) }
                }
            }
            LibraryViewMode.COMPACT_LIST -> {
                if (columns > 1) {
                    val chunked = items.chunked(columns)
                    items(chunked.size, key = { "ar_cl_row_$it" }) { rowIndex ->
                        val row = chunked[rowIndex]
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 1.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            row.forEach { item -> Box(Modifier.weight(1f)) { CompactDensityListItem(item) } }
                            repeat(columns - row.size) { Spacer(Modifier.weight(1f)) }
                        }
                    }
                } else {
                    items(items, key = { it.id }) { item -> CompactDensityListItem(item) }
                }
            }
        }
    }
}

@Composable
private fun DownloadsTabContent(
    listState: androidx.compose.foundation.lazy.LazyListState,
    gridState: androidx.compose.foundation.lazy.grid.LazyGridState,
    nestedScrollConnection: androidx.compose.ui.input.nestedscroll.NestedScrollConnection,
    viewModel: PlayerSharedViewModel,
    favoriteIds: Set<Long>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    showSearch: Boolean,
    sortOrder: LibrarySortOrder,
    onSortOrderChange: (LibrarySortOrder) -> Unit,
    viewMode: LibraryViewMode,
    onViewModeChange: (LibraryViewMode) -> Unit,
    selectedSongItems: Set<SongItem>,
    onToggleSelect: (SongItem) -> Unit
) {
    val localSongs by viewModel.localSongs.collectAsStateWithLifecycle(emptyList())

    val filtered = remember(localSongs, searchQuery, sortOrder) {
        val list = if (searchQuery.isBlank()) localSongs else localSongs.filter {
            it.title.contains(searchQuery, ignoreCase = true) || it.artist.contains(searchQuery, ignoreCase = true)
        }
        when (sortOrder) {
            LibrarySortOrder.TITLE -> list.sortedBy { it.title.lowercase() }
            LibrarySortOrder.ARTIST -> list.sortedBy { it.artist.lowercase() }
            LibrarySortOrder.DATE_ADDED -> list.sortedByDescending { it.dateAdded }
            LibrarySortOrder.DURATION -> list.sortedByDescending { it.duration }
            else -> list
        }
    }

    val isMultiSelectMode = selectedSongItems.isNotEmpty()
    val screenWidth = androidx.compose.ui.platform.LocalConfiguration.current.screenWidthDp
    val columns = getAdaptiveColumns(viewMode, screenWidth)

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(nestedScrollConnection),
        contentPadding = PaddingValues(bottom = 160.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        item {
            LibraryToolbar(
                searchQuery = searchQuery,
                onSearchQueryChange = onSearchQueryChange,
                sortOrder = sortOrder,
                onSortOrderChange = onSortOrderChange,
                viewMode = viewMode,
                onViewModeChange = onViewModeChange,
                onCreatePlaylist = {},
                showSearch = showSearch,
                showNewPlaylist = false
            )
        }

        if (filtered.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.lib_empty_library),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            if (columns > 1 && (viewMode == LibraryViewMode.STANDARD_LIST || viewMode == LibraryViewMode.COMPACT_LIST)) {
                val chunked = filtered.chunked(columns)
                items(chunked.size, key = { "dl_row_$it" }) { rowIndex ->
                    val row = chunked[rowIndex]
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        row.forEach { song ->
                            val isCompact = viewMode == LibraryViewMode.COMPACT_LIST
                            val isFav = favoriteIds.contains(song.id)
                            val isSelected = selectedSongItems.any { it.id == song.id }
                            Box(modifier = Modifier.weight(1f)) {
                                LocalSongItemRow(
                                    song = song,
                                    isCompact = isCompact,
                                    isFavorite = isFav,
                                    isMultiSelectMode = isMultiSelectMode,
                                    isSelected = isSelected,
                                    onClick = {
                                        if (isMultiSelectMode) {
                                            onToggleSelect(song)
                                        } else {
                                            viewModel.setSongList(filtered)
                                            viewModel.playSong(song)
                                        }
                                    },
                                    onLongClick = {
                                        onToggleSelect(song)
                                    }
                                )
                            }
                        }
                        repeat(columns - row.size) { Spacer(Modifier.weight(1f)) }
                    }
                }
            } else {
                items(filtered, key = { it.id }) { song ->
                    val isCompact = viewMode == LibraryViewMode.COMPACT_LIST
                    val isFav = favoriteIds.contains(song.id)
                    val isSelected = selectedSongItems.any { it.id == song.id }
                    LocalSongItemRow(
                        song = song,
                        isCompact = isCompact,
                        isFavorite = isFav,
                        isMultiSelectMode = isMultiSelectMode,
                        isSelected = isSelected,
                        onClick = {
                            if (isMultiSelectMode) {
                                onToggleSelect(song)
                            } else {
                                viewModel.setSongList(filtered)
                                viewModel.playSong(song)
                            }
                        },
                        onLongClick = {
                            onToggleSelect(song)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun DeviceFilesTabContent(
    listState: androidx.compose.foundation.lazy.LazyListState,
    gridState: androidx.compose.foundation.lazy.grid.LazyGridState,
    nestedScrollConnection: androidx.compose.ui.input.nestedscroll.NestedScrollConnection,
    localSongs: List<SongItem>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    showSearch: Boolean,
    sortOrder: LibrarySortOrder,
    onSortOrderChange: (LibrarySortOrder) -> Unit,
    viewMode: LibraryViewMode,
    onViewModeChange: (LibraryViewMode) -> Unit,
    viewModel: PlayerSharedViewModel,
    currentSong: SongItem?,
    favoriteIds: Set<Long>,
    selectedSongItems: Set<SongItem>,
    onToggleSelect: (SongItem) -> Unit
) {
    val filteredSongs = remember(localSongs, searchQuery, sortOrder) {
        val list = if (searchQuery.isBlank()) localSongs else localSongs.filter {
            it.title.contains(searchQuery, ignoreCase = true) || it.artist.contains(searchQuery, ignoreCase = true)
        }
        when (sortOrder) {
            LibrarySortOrder.TITLE -> list.sortedBy { it.title.lowercase() }
            LibrarySortOrder.ARTIST -> list.sortedBy { it.artist.lowercase() }
            LibrarySortOrder.DATE_ADDED -> list.sortedByDescending { it.dateAdded }
            LibrarySortOrder.DURATION -> list.sortedByDescending { it.duration }
            else -> list
        }
    }

    val isMultiSelectMode = selectedSongItems.isNotEmpty()
    val screenWidth = androidx.compose.ui.platform.LocalConfiguration.current.screenWidthDp
    val columns = getAdaptiveColumns(viewMode, screenWidth)

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(nestedScrollConnection),
        contentPadding = PaddingValues(bottom = 160.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        item {
            LibraryToolbar(
                searchQuery = searchQuery,
                onSearchQueryChange = onSearchQueryChange,
                sortOrder = sortOrder,
                onSortOrderChange = onSortOrderChange,
                viewMode = viewMode,
                onViewModeChange = onViewModeChange,
                onCreatePlaylist = {},
                showSearch = showSearch,
                showNewPlaylist = false
            )
        }

        if (filteredSongs.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.lib_empty_library),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            if (columns > 1 && (viewMode == LibraryViewMode.STANDARD_LIST || viewMode == LibraryViewMode.COMPACT_LIST)) {
                val chunked = filteredSongs.chunked(columns)
                items(chunked.size, key = { "dev_row_$it" }) { rowIndex ->
                    val row = chunked[rowIndex]
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        row.forEach { song ->
                            val isPlaying = currentSong?.id == song.id
                            val isCompact = viewMode == LibraryViewMode.COMPACT_LIST
                            val isFav = favoriteIds.contains(song.id)
                            val isSelected = selectedSongItems.any { it.id == song.id }
                            Box(modifier = Modifier.weight(1f)) {
                                LocalSongItemRow(
                                    song = song,
                                    isPlaying = isPlaying,
                                    isCompact = isCompact,
                                    isFavorite = isFav,
                                    isMultiSelectMode = isMultiSelectMode,
                                    isSelected = isSelected,
                                    onClick = {
                                        if (isMultiSelectMode) {
                                            onToggleSelect(song)
                                        } else {
                                            viewModel.setSongList(filteredSongs)
                                            viewModel.playSong(song)
                                        }
                                    },
                                    onLongClick = {
                                        onToggleSelect(song)
                                    }
                                )
                            }
                        }
                        repeat(columns - row.size) { Spacer(Modifier.weight(1f)) }
                    }
                }
            } else {
                items(filteredSongs, key = { it.id }) { song ->
                    val isPlaying = currentSong?.id == song.id
                    val isCompact = viewMode == LibraryViewMode.COMPACT_LIST
                    val isFav = favoriteIds.contains(song.id)
                    val isSelected = selectedSongItems.any { it.id == song.id }
                    LocalSongItemRow(
                        song = song,
                        isPlaying = isPlaying,
                        isCompact = isCompact,
                        isFavorite = isFav,
                        isMultiSelectMode = isMultiSelectMode,
                        isSelected = isSelected,
                        onClick = {
                            if (isMultiSelectMode) {
                                onToggleSelect(song)
                            } else {
                                viewModel.setSongList(filteredSongs)
                                viewModel.playSong(song)
                            }
                        },
                        onLongClick = {
                            onToggleSelect(song)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun PodcastsOverviewContent(
    gridState: androidx.compose.foundation.lazy.grid.LazyGridState,
    listState: androidx.compose.foundation.lazy.LazyListState,
    nestedScrollConnection: androidx.compose.ui.input.nestedscroll.NestedScrollConnection,
    podcasts: List<OnlinePlaylist>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    showSearch: Boolean,
    sortOrder: LibrarySortOrder,
    onSortOrderChange: (LibrarySortOrder) -> Unit,
    viewMode: LibraryViewMode,
    onViewModeChange: (LibraryViewMode) -> Unit,
    onPodcastClick: (OnlinePlaylist) -> Unit
) {
    val items = remember(podcasts, searchQuery, sortOrder) {
        val list = podcasts.map { podcast ->
            object : UnifiedItem {
                override val id = "podcast_${podcast.playlistId}"
                override val title = podcast.title
                override val subtitle = "Podcast"
                override val thumbnailUrl = podcast.thumbnailUrl
                override val thumbnails = listOfNotNull(podcast.thumbnailUrl)
                override val isCircle = false
                override val onClick = { onPodcastClick(podcast) }
            }
        }
        if (searchQuery.isBlank()) list else list.filter { it.title.contains(searchQuery, ignoreCase = true) }
    }

    val screenWidth = androidx.compose.ui.platform.LocalConfiguration.current.screenWidthDp
    val columns = getAdaptiveColumns(viewMode, screenWidth)

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(nestedScrollConnection),
        contentPadding = PaddingValues(bottom = 160.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            LibraryToolbar(
                searchQuery = searchQuery,
                onSearchQueryChange = onSearchQueryChange,
                sortOrder = sortOrder,
                onSortOrderChange = onSortOrderChange,
                viewMode = viewMode,
                onViewModeChange = onViewModeChange,
                onCreatePlaylist = {},
                showSearch = showSearch,
                showNewPlaylist = false
            )
        }

        if (items.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.text_no_podcasts_found),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            when (viewMode) {
                LibraryViewMode.LARGE_GRID -> {
                    val chunked = items.chunked(columns)
                    items(chunked.size, key = { "pod_lg_row_$it" }) { rowIndex ->
                        val row = chunked[rowIndex]
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 5.dp),
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            row.forEach { item -> Box(Modifier.weight(1f)) { LargeGridCard(item) } }
                            repeat(columns - row.size) { Spacer(Modifier.weight(1f)) }
                        }
                    }
                }
                LibraryViewMode.COMPACT_GRID -> {
                    val chunked = items.chunked(columns)
                    items(chunked.size, key = { "pod_cg_row_$it" }) { rowIndex ->
                        val row = chunked[rowIndex]
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            row.forEach { item -> Box(Modifier.weight(1f)) { CompactGridCard(item) } }
                            repeat(columns - row.size) { Spacer(Modifier.weight(1f)) }
                        }
                    }
                }
                LibraryViewMode.STANDARD_LIST -> {
                    if (columns > 1) {
                        val chunked = items.chunked(columns)
                        items(chunked.size, key = { "pod_sl_row_$it" }) { rowIndex ->
                            val row = chunked[rowIndex]
                            Row(
                                modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 2.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                row.forEach { item -> Box(Modifier.weight(1f)) { StandardListItem(item) } }
                                repeat(columns - row.size) { Spacer(Modifier.weight(1f)) }
                            }
                        }
                    } else {
                        items(items, key = { it.id }) { item -> StandardListItem(item) }
                    }
                }
                LibraryViewMode.COMPACT_LIST -> {
                    if (columns > 1) {
                        val chunked = items.chunked(columns)
                        items(chunked.size, key = { "pod_cl_row_$it" }) { rowIndex ->
                            val row = chunked[rowIndex]
                            Row(
                                modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 1.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                row.forEach { item -> Box(Modifier.weight(1f)) { CompactDensityListItem(item) } }
                                repeat(columns - row.size) { Spacer(Modifier.weight(1f)) }
                            }
                        }
                    } else {
                        items(items, key = { it.id }) { item -> CompactDensityListItem(item) }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// Visual Item Cards (Large Grid, Compact Grid, Standard, Compact)
// -------------------------------------------------------------

@Composable
private fun LargeGridCard(item: UnifiedItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = item.onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            PlaylistCollageArt(
                thumbnails = item.thumbnails,
                modifier = Modifier
                    .aspectRatio(1f)
                    .fillMaxWidth(),
                isCircle = item.isCircle,
                shape = if (item.isCircle) CircleShape else RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = item.title,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = item.subtitle,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CompactGridCard(item: UnifiedItem) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = item.onClick)
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        PlaylistCollageArt(
            thumbnails = item.thumbnails,
            modifier = Modifier
                .aspectRatio(1f)
                .fillMaxWidth(),
            isCircle = item.isCircle,
            shape = if (item.isCircle) CircleShape else RoundedCornerShape(10.dp),
            iconSize = 24.dp
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = item.title,
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.labelMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Text(
            text = item.subtitle,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun StandardListItem(item: UnifiedItem) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 2.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = item.onClick),
        color = MaterialTheme.colorScheme.surfaceContainer,
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PlaylistCollageArt(
                thumbnails = item.thumbnails,
                modifier = Modifier.size(54.dp),
                isCircle = item.isCircle,
                shape = if (item.isCircle) CircleShape else RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = item.subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun CompactDensityListItem(item: UnifiedItem) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 1.dp)
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = item.onClick),
        color = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.7f),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PlaylistCollageArt(
                thumbnails = item.thumbnails,
                modifier = Modifier.size(40.dp),
                isCircle = item.isCircle,
                shape = if (item.isCircle) CircleShape else RoundedCornerShape(8.dp),
                iconSize = 20.dp
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = item.subtitle,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SongItemRow(
    song: OnlineSong,
    isCompact: Boolean,
    isMultiSelectMode: Boolean = false,
    isSelected: Boolean = false,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
    onMenuClick: () -> Unit
) {
    val artSize = if (isCompact) 36.dp else 48.dp

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = if (isCompact) 1.dp else 2.dp)
            .clip(RoundedCornerShape(12.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        color = if (isSelected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceContainer,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = if (isCompact) 4.dp else 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isMultiSelectMode) {
                Icon(
                    imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                    contentDescription = null,
                    tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .padding(end = 10.dp)
                        .size(22.dp)
                )
            }

            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(song.thumbnailUrl)
                    .crossfade(true)
                    .error(R.drawable.ic_music_note)
                    .build(),
                contentDescription = song.title,
                modifier = Modifier
                    .size(artSize)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = song.title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = if (isCompact) 14.sp else 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = song.artist,
                    fontSize = if (isCompact) 12.sp else 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (!isMultiSelectMode) {
                IconButton(
                    onClick = onMenuClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Options",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun LocalSongItemRow(
    song: SongItem,
    isCompact: Boolean,
    isPlaying: Boolean = false,
    isFavorite: Boolean = false,
    isMultiSelectMode: Boolean = false,
    isSelected: Boolean = false,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {}
) {
    val artSize = if (isCompact) 36.dp else 48.dp

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = if (isCompact) 1.dp else 2.dp)
            .clip(RoundedCornerShape(12.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        color = when {
            isSelected -> MaterialTheme.colorScheme.secondaryContainer
            isPlaying -> MaterialTheme.colorScheme.primaryContainer
            else -> MaterialTheme.colorScheme.surfaceContainer
        },
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = if (isCompact) 4.dp else 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isMultiSelectMode) {
                Icon(
                    imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                    contentDescription = null,
                    tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .padding(end = 10.dp)
                        .size(22.dp)
                )
            }

            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(song.getAlbumArtUri() ?: R.drawable.ic_music_note)
                    .crossfade(true)
                    .error(R.drawable.ic_music_note)
                    .build(),
                contentDescription = song.title,
                modifier = Modifier
                    .size(artSize)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = song.title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = if (isCompact) 14.sp else 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = when {
                        isSelected -> MaterialTheme.colorScheme.onSecondaryContainer
                        isPlaying -> MaterialTheme.colorScheme.onPrimaryContainer
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                )
                Text(
                    text = song.artist,
                    fontSize = if (isCompact) 12.sp else 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = when {
                        isSelected -> MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                        isPlaying -> MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }

            if (isFavorite && !isMultiSelectMode) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = null,
                    tint = if (isPlaying) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .size(16.dp)
                )
            }
        }
    }
}

// -------------------------------------------------------------
// Helper BottomSheet & Functions
// -------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LocalPlaylistDetailsBottomSheet(
    playlist: PlaylistEntity,
    viewModel: PlayerSharedViewModel,
    onDismiss: () -> Unit
) {
    val songsEntity by viewModel.getSongsForLocalPlaylist(playlist.id).collectAsStateWithLifecycle(emptyList())
    val songs = remember(songsEntity) { songsEntity.map { it.toSongItem() } }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        playlist.title,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        "${songs.size} songs",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = {
                        viewModel.deleteLocalPlaylist(playlist.id)
                        onDismiss()
                    }
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete Playlist",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (songs.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            viewModel.setSongList(songs)
                            if (songs.isNotEmpty()) viewModel.playSong(songs.first())
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Icon(Icons.Default.PlayArrow, null)
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.action_play_all))
                    }

                    OutlinedButton(
                        onClick = {
                            val shuffled = songs.shuffled()
                            viewModel.setSongList(shuffled)
                            if (shuffled.isNotEmpty()) viewModel.playSong(shuffled.first())
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Shuffle, null)
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.text_shuffle))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            if (songs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        stringResource(R.string.text_this_playlist_is_empty),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .heightIn(max = 400.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(songs) { song ->
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceContainer
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.setSongList(songs)
                                        viewModel.playSong(song)
                                        onDismiss()
                                    }
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (song.thumbnailUrl?.isNotEmpty() == true) {
                                        AsyncImage(
                                            model = ImageRequest.Builder(LocalContext.current)
                                                .data(song.thumbnailUrl)
                                                .crossfade(true)
                                                .build(),
                                            contentDescription = null,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Icon(
                                            Icons.Default.MusicNote,
                                            null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Spacer(Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        song.title,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyMedium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        song.artist,
                                        style = MaterialTheme.typography.bodySmall,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        viewModel.removeSongFromLocalPlaylist(playlist.id, song.videoId ?: song.id.toString())
                                    }
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Remove song",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
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

private suspend fun scanLocalFiles(context: Context, viewModel: PlayerSharedViewModel) {
    viewModel.rescanLocalFiles()
}

private fun getActivityFromContext(context: Context): Activity? {
    var ctx = context
    while (ctx is ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}

private fun navigateToExplore(activity: Activity?) {
    (activity as? MainActivity)?.showArtistPage(null, null)
}
