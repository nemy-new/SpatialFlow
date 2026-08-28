package com.codetrio.overdrive.ui.explore

import android.content.res.Configuration
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.animateFloat
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.codetrio.overdrive.MainActivity
import com.codetrio.overdrive.data.innertube.HomeSection
import com.codetrio.overdrive.data.innertube.OnlineSong
import com.codetrio.overdrive.data.innertube.SearchFilter
import com.codetrio.overdrive.data.innertube.SearchItem
import com.codetrio.overdrive.data.innertube.UserProfile
import com.codetrio.overdrive.data.innertube.resize
import com.codetrio.overdrive.model.SongItem
import com.codetrio.overdrive.ui.CreateLocalPlaylistDialog
import com.codetrio.overdrive.ui.LocalPlaylistPickerDialog
import com.codetrio.overdrive.viewmodel.AccountViewModel
import com.codetrio.overdrive.viewmodel.DetailType
import com.codetrio.overdrive.viewmodel.ExploreViewModel
import com.codetrio.overdrive.viewmodel.PlayerSharedViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds
import java.util.concurrent.ConcurrentHashMap
import androidx.compose.runtime.produceState
import androidx.compose.ui.focus.focusRequester
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.codetrio.overdrive.data.innertube.YouTubeMusic

// ===== Shared Transition Locals =====

@OptIn(ExperimentalSharedTransitionApi::class)
val LocalSharedTransitionScope = compositionLocalOf<SharedTransitionScope?> { null }

@OptIn(ExperimentalSharedTransitionApi::class)
val LocalAnimatedVisibilityScope = compositionLocalOf<AnimatedVisibilityScope?> { null }

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun Modifier.sharedElementIfAvailable(key: String): Modifier {
    val sharedScope = LocalSharedTransitionScope.current
    val animatedScope = LocalAnimatedVisibilityScope.current
    return if (sharedScope != null && animatedScope != null) {
        with(sharedScope) {
            this@sharedElementIfAvailable.sharedElement(
                rememberSharedContentState(key = key),
                animatedVisibilityScope = animatedScope
            )
        }
    } else this
}



// ===== Explore Screen =====

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class,
    ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ExploreScreen(
    viewModel: ExploreViewModel,
    playerSharedViewModel: PlayerSharedViewModel,
    onNavigateToLibrary: () -> Unit = {}
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
    val error by viewModel.error.collectAsStateWithLifecycle()

    val searchQuery = searchState.searchQuery
    val searchResults = searchState.searchResults
    val suggestions = searchState.suggestions
    val isSearching = searchState.isSearching
    val searchHistory = searchState.searchHistory
    val isLoadingSuggestions = searchState.isLoadingSuggestions

    val homeSections = homeState.homeSections
    val isLoadingHome = homeState.isLoadingHome
    val currentMood = homeState.currentMood
    val homeMoods = homeState.homeMoods
    val isRefreshing = homeState.isRefreshing

    val albumDetail = detailState.albumDetail
    val artistDetail = detailState.artistDetail
    val playlistDetail = detailState.playlistDetail
    val sectionDetail = detailState.sectionDetail
    val moodDetail = detailState.moodDetail
    val genresSections = detailState.genresSections
    val isLoadingDetail = detailState.isLoadingDetail
    val detailStack = detailState.detailStack

    val isLoadingStream = playbackState.isLoadingStream
    val currentOnlineSong = playbackState.currentOnlineSong


    var isSearchActive by remember { mutableStateOf(false) }
    var selectedSongForMenu by remember { mutableStateOf<OnlineSong?>(null) }
    var showCreditsForSong by remember { mutableStateOf<OnlineSong?>(null) }

    var showAddToPlaylistDialog by remember { mutableStateOf(false) }
    var showCreatePlaylistDialog by remember { mutableStateOf(false) }
    var songToAddPlaylist by remember { mutableStateOf<SongItem?>(null) }
    val localPlaylists by playerSharedViewModel.localPlaylistsFlow.collectAsStateWithLifecycle(
        emptyList()
    )


    LaunchedEffect(Unit) {
        playerSharedViewModel.currentSongIndex.collect { idx ->
            if (idx >= 0) {
                val queue = viewModel.onlineQueue.value
                if (idx < queue.size) {
                    viewModel.updateActiveSongAndIndex(idx, queue[idx])
                }
            }
        }
    }

    val homeListState = rememberLazyListState()
    
    LaunchedEffect(viewModel.scrollToTopEvent) {
        viewModel.scrollToTopEvent.collect {
            homeListState.animateScrollToItem(0)
        }
    }
    val searchListState = rememberLazyListState()

    val isPlayerExpanded by playerSharedViewModel.isPlayerExpanded.collectAsStateWithLifecycle()
    val castState by playerSharedViewModel.castState.collectAsStateWithLifecycle()

    androidx.activity.compose.BackHandler(enabled = showCreditsForSong != null && !isPlayerExpanded) {
        showCreditsForSong = null
    }

    val prefs = remember { context.getSharedPreferences("explore_prefs", 0) }
    var lastPlayedArtist by remember { mutableStateOf(prefs.getString("last_played_artist", null)) }

    DisposableEffect(context) {
        val listener =
            android.content.SharedPreferences.OnSharedPreferenceChangeListener { p, key ->
                if (key == "last_played_artist") lastPlayedArtist =
                    p.getString("last_played_artist", null)
            }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        onDispose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    val appPrefs = remember { context.getSharedPreferences("AppSettings", 0) }
    DisposableEffect(context) {
        val listener =
            android.content.SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
                if (key == "yt_cookies") viewModel.forceReloadHomeFeed()
            }
        appPrefs.registerOnSharedPreferenceChangeListener(listener)
        onDispose { appPrefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    var isAccountVisible by rememberSaveable { mutableStateOf(false) }

    val detailScreenActive = detailStack.isNotEmpty() || isLoadingDetail

    val handleDetailBack = remember(viewModel, mainActivity) {
        {
            val currentStackSize = viewModel.detailState.value.detailStack.size
            val isLastDetail = currentStackSize <= 1
            val wasFromLibrary = viewModel.cameFromLibrary && isLastDetail

            viewModel.popDetailStack()

            if (isLastDetail) {
                viewModel.cameFromLibrary = false
            }

            if (wasFromLibrary) {
                onNavigateToLibrary()
            }
        }
    }

    androidx.activity.compose.BackHandler(enabled = detailScreenActive && !isPlayerExpanded) {
        handleDetailBack()
    }

    androidx.activity.compose.BackHandler(enabled = isSearchActive && !isPlayerExpanded) {
        isSearchActive = false
    }

    androidx.activity.compose.BackHandler(enabled = searchQuery.isNotBlank() && !isPlayerExpanded) {
        viewModel.clearSearch()
    }

    androidx.activity.compose.BackHandler(enabled = isAccountVisible && !isPlayerExpanded) {
        isAccountVisible = false
    }

    LaunchedEffect(Unit) { viewModel.loadHomeFeed() }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                if (!isLandscape && mainActivity != null) {
                    if (consumed.y < -10f) mainActivity.hideBottomNavWithAnimation()
                    else if (consumed.y > 10f) mainActivity.showBottomNavWithAnimation()
                }
                return super.onPostScroll(consumed, available, source)
            }
        }
    }


    val screenPriority = remember {
        mapOf(
            "home" to 0,
            "account" to 1,
            "loading" to 2,
            "album" to 3,
            "playlist" to 3,
            "artist" to 3,
            "section" to 3,
            "genres" to 3,
            "mood" to 3,
            "credits" to 3
        )
    }

    val currentScreen =
        remember(detailStack, isLoadingDetail, isAccountVisible, showCreditsForSong) {
            when {
                isLoadingDetail -> "loading"
                showCreditsForSong != null -> "credits"
                isAccountVisible -> "account"
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

    val currentFilter by viewModel.searchFilter.collectAsStateWithLifecycle()

    val onSearchHeaderQueryChange = remember(viewModel) {
        { query: String -> viewModel.setSearchQuery(query) }
    }
    val onSearchHeaderSearch = remember(viewModel) {
        { query: String -> viewModel.search(query); isSearchActive = false }
    }
    val onSearchHeaderActiveChange = remember {
        { active: Boolean -> isSearchActive = active }
    }
    val onSearchHeaderClearHistory = remember(viewModel) {
        { viewModel.clearSearchHistory() }
    }
    val onSearchHeaderRemoveHistoryItem = remember(viewModel) {
        { item: String -> viewModel.removeFromSearchHistory(item) }
    }
    val onSearchHeaderAccountVisibleChange = remember {
        { visible: Boolean -> isAccountVisible = visible }
    }
    val onSearchHeaderMoodClick = remember(viewModel) {
        { mood: String? -> viewModel.setMood(mood) }
    }
    val onSearchHeaderFilterClick = remember(viewModel) {
        { filter: SearchFilter? -> viewModel.setSearchFilter(filter) }
    }
    val onSearchHeaderClearSearch = remember(viewModel) {
        { viewModel.clearSearch() }
    }

    val color1 = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
    val color2 = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f)
    val color3 = MaterialTheme.colorScheme.secondary.copy(alpha = 0.05f)
    val backgroundColor = MaterialTheme.colorScheme.background

    val gradientAlpha by animateFloatAsState(
        targetValue = if (isLoadingHome || homeSections.isEmpty()) 0f else 1f,
        animationSpec = androidx.compose.animation.core.tween(durationMillis = 1200, easing = androidx.compose.animation.core.LinearOutSlowInEasing),
        label = "GradientFadeIn"
    )

    // Capture which list is active as a stable lambda so graphicsLayer reads stay in draw phase
    val activeListState = if (isSearchActive || searchQuery.isNotBlank() || searchResults.isNotEmpty())
        searchListState else homeListState

    Box(modifier = Modifier.fillMaxSize().background(backgroundColor)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(500.dp)
                .graphicsLayer {
                    // All reads inside graphicsLayer{} are draw-phase — zero recompositions on scroll
                    val scrollY = if (activeListState.firstVisibleItemIndex == 0)
                        activeListState.firstVisibleItemScrollOffset.toFloat() else 2000f
                    val scrollAlpha = (1f - scrollY / 600f).coerceIn(0f, 1f)
                    alpha = scrollAlpha * gradientAlpha
                    translationY = -scrollY * 0.1f
                }
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(color1, color2, color3, backgroundColor)
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
            ) {
            // ===== Main Content (Now encapsulates scrolling headers) =====
            Box(modifier = Modifier.fillMaxSize().nestedScroll(nestedScrollConnection)) {
                SharedTransitionLayout {
                    CompositionLocalProvider(LocalSharedTransitionScope provides this) {
                        val spatialSpec = MaterialTheme.motionScheme.defaultSpatialSpec<IntOffset>()
                        val effectsSpec = MaterialTheme.motionScheme.defaultEffectsSpec<Float>()

                        AnimatedContent(
                            targetState = currentScreen,
                            transitionSpec = {
                                val initialWeight = screenPriority[initialState] ?: 0
                                val targetWeight = screenPriority[targetState] ?: 0

                                if (targetWeight > initialWeight) {
                                    // SLIDE PUSH: Entering from right, existing sliding out to left
                                    (slideInHorizontally(animationSpec = spatialSpec) { it } + fadeIn(
                                        effectsSpec
                                    ))
                                        .togetherWith(slideOutHorizontally(animationSpec = spatialSpec) { -it / 3 } + fadeOut(
                                            effectsSpec
                                        ))
                                } else {
                                    // SLIDE POP: Entering from left, existing sliding out to right
                                    (slideInHorizontally(animationSpec = spatialSpec) { -it / 3 } + fadeIn(
                                        effectsSpec
                                    ))
                                        .togetherWith(slideOutHorizontally(animationSpec = spatialSpec) { it } + fadeOut(
                                            effectsSpec
                                        ))
                                }
                            },
                            label = "Explore Slide Transitions"
                        ) { screen ->
                            CompositionLocalProvider(LocalAnimatedVisibilityScope provides this) {
                                when (screen) {
                                    "account" -> {
                                        AccountScreen(
                                            viewModel = accountVM,
                                            onBack = { isAccountVisible = false },
                                            onSongClick = { song, queue, index ->
                                                viewModel.playOnlineSongWithQueue(
                                                    song,
                                                    queue,
                                                    index
                                                )
                                            },
                                            onNavigateToSignIn = {
                                                mainActivity?.navigateToGoogleSignIn()
                                            }
                                        )
                                    }

                                    "loading" -> DetailScreenSkeleton()

                                    "album" -> albumDetail?.let { detail ->
                                        AlbumDetailView(
                                            albumPage = detail,
                                            currentOnlineSong = currentOnlineSong,
                                            isLoadingStream = isLoadingStream,
                                            onBack = { handleDetailBack() },
                                            onSongClick = { song, queue, index ->
                                                viewModel.playOnlineSongWithQueue(
                                                    song,
                                                    queue,
                                                    index
                                                )
                                            },
                                            onSongMenuClick = { selectedSongForMenu = it },
                                            onStartRadioClick = { videoId ->
                                                viewModel.startRadio(
                                                    videoId
                                                )
                                            }
                                        )
                                    }

                                    "playlist" -> playlistDetail?.let { detail ->
                                        PlaylistDetailView(
                                            playlistPage = detail,
                                            currentOnlineSong = currentOnlineSong,
                                            isLoadingStream = isLoadingStream,
                                            onBack = { handleDetailBack() },
                                            onSongClick = { song, queue, index ->
                                                viewModel.playOnlineSongWithQueue(
                                                    song,
                                                    queue,
                                                    index
                                                )
                                            },
                                            onSongMenuClick = { selectedSongForMenu = it },
                                            onStartRadioClick = { videoId ->
                                                viewModel.startRadio(
                                                    videoId
                                                )
                                            }
                                        )
                                    }

                                    "artist" -> artistDetail?.let { detail ->
                                        ArtistDetailView(
                                            artistPage = detail,
                                            currentOnlineSong = currentOnlineSong,
                                            isSubscribed = detail.artist.isSubscribed,
                                            onBack = { handleDetailBack() },
                                            onSongClick = { song, queue, index ->
                                                viewModel.playOnlineSongWithQueue(
                                                    song,
                                                    queue,
                                                    index
                                                )
                                            },
                                            onAlbumClick = { viewModel.loadAlbum(it.browseId) },
                                            onPlaylistClick = { viewModel.loadPlaylist(it.playlistId) },
                                            onArtistClick = {
                                                viewModel.loadArtist(
                                                    it.browseId,
                                                    it.thumbnailUrl
                                                )
                                            },
                                            onSongMenuClick = { selectedSongForMenu = it },
                                            onSubscribeClick = { channelId ->
                                                if (detail.artist.isSubscribed) {
                                                    viewModel.unsubscribeFromArtist(channelId)
                                                } else {
                                                    viewModel.subscribeToArtist(channelId)
                                                }
                                            },
                                            onStartRadioClick = { videoId ->
                                                viewModel.startRadio(
                                                    videoId
                                                )
                                            },
                                            onSectionClick = { browseId, params, title ->
                                                viewModel.loadSectionDetails(
                                                    browseId,
                                                    params,
                                                    title
                                                )
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
                                                viewModel.playOnlineSongWithQueue(
                                                    song,
                                                    queue,
                                                    index
                                                )
                                            },
                                            onAlbumClick = { viewModel.loadAlbum(it.browseId) },
                                            onPlaylistClick = { viewModel.loadPlaylist(it.playlistId) },
                                            onArtistClick = {
                                                viewModel.loadArtist(
                                                    it.browseId,
                                                    it.thumbnailUrl
                                                )
                                            },
                                            onSongMenuClick = { selectedSongForMenu = it },
                                            onStartRadioClick = { videoId ->
                                                viewModel.startRadio(
                                                    videoId
                                                )
                                            }
                                        )
                                    }

                                    "genres" -> {
                                        GenresScreen(
                                            genresSections = genresSections,
                                            onBack = { handleDetailBack() },
                                            onGenreClick = { title: String, browseId: String, params: String? ->
                                                viewModel.loadMood(title, browseId, params)
                                            }
                                        )
                                    }

                                    "mood" -> moodDetail?.let { detail ->
                                        MoodDetailView(
                                            moodDetail = detail,
                                            currentOnlineSong = currentOnlineSong,
                                            isLoadingStream = isLoadingStream,
                                            onBack = { handleDetailBack() },
                                            onSongClick = { song, queue, index ->
                                                viewModel.playOnlineSongWithQueue(
                                                    song,
                                                    queue,
                                                    index
                                                )
                                            },
                                            onAlbumClick = { viewModel.loadAlbum(it.browseId) },
                                            onPlaylistClick = { viewModel.loadPlaylist(it.playlistId) },
                                            onArtistClick = {
                                                viewModel.loadArtist(
                                                    it.browseId,
                                                    it.thumbnailUrl
                                                )
                                            },
                                            onSongMenuClick = { selectedSongForMenu = it },
                                            onSectionClick = { browseId, params, title ->
                                                viewModel.loadSectionDetails(
                                                    browseId,
                                                    params,
                                                    title
                                                )
                                            },
                                            onStartRadioClick = { videoId ->
                                                viewModel.startRadio(
                                                    videoId
                                                )
                                            }
                                        )
                                    }

                                    "credits" -> showCreditsForSong?.let { s ->
                                        SongCreditsScreen(
                                            song = s,
                                            onBack = { showCreditsForSong = null }
                                        )
                                    }

                                    "home" -> Column(modifier = Modifier.fillMaxSize()) {
                                        SearchHeader(
                                            searchQuery = searchQuery,
                                            onQueryChange = onSearchHeaderQueryChange,
                                            onSearch = onSearchHeaderSearch,
                                            isSearchActive = isSearchActive,
                                            accountHistory = accountHistory,
                                            onAccountHistorySongClick = { song: OnlineSong, queue: List<OnlineSong>, index: Int ->
                                                viewModel.playOnlineSongWithQueue(
                                                    song,
                                                    queue,
                                                    index
                                                )
                                            },
                                            onSearchActiveChange = onSearchHeaderActiveChange,
                                            searchResults = searchResults,
                                            searchHistory = searchHistory,
                                            onClearSearchHistory = onSearchHeaderClearHistory,
                                            onRemoveFromSearchHistory = onSearchHeaderRemoveHistoryItem,
                                            onHistoryItemClick = { historyItem: String ->
                                                scope.launch {
                                                    isSearchActive = false
                                                    delay(50.milliseconds)
                                                    viewModel.search(historyItem)
                                                }
                                            },
                                            suggestions = suggestions,
                                            isLoadingSuggestions = isLoadingSuggestions,
                                            onSuggestionClick = { suggestion: String ->
                                                scope.launch {
                                                    isSearchActive = false
                                                    delay(50.milliseconds)
                                                    viewModel.search(suggestion)
                                                }
                                            },
                                            onAccountVisibleChange = onSearchHeaderAccountVisibleChange,
                                            userProfile = userProfile,
                                            homeMoods = homeMoods,
                                            currentMood = currentMood,
                                            onMoodClick = onSearchHeaderMoodClick,
                                            currentFilter = currentFilter,
                                            onFilterClick = onSearchHeaderFilterClick,
                                            isLandscape = isLandscape,
                                            onClearSearch = onSearchHeaderClearSearch,
                                            castState = castState,
                                            onCastClick = { playerSharedViewModel.showCastSheet() }
                                        )

                                        Box(modifier = Modifier.weight(1f)) {
                                            when {
                                                isSearching && searchResults.isEmpty() -> {
                                                    Box(
                                                        modifier = Modifier.fillMaxSize(),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        LoadingIndicator()
                                                    }
                                                }

                                                searchResults.isNotEmpty() -> {
                                                    LazyColumn(
                                                        state = searchListState,
                                                        modifier = Modifier
                                                            .fillMaxSize()
                                                            .background(Color.Transparent),
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
                                                                        onSaveClick = {
                                                                            viewModel.saveItemToLibrary(item)
                                                                        },
                                                                        onMoreClick = {
                                                                            item.song?.let { selectedSongForMenu = it }
                                                                        },
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
                                                                                is SearchItem.Artist -> viewModel.loadArtist(
                                                                                    item.artist.browseId,
                                                                                    item.artist.thumbnailUrl
                                                                                )
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
                                                                    modifier = Modifier
                                                                        .fillMaxWidth()
                                                                        .padding(16.dp),
                                                                    contentAlignment = Alignment.Center
                                                                ) {
                                                                    LoadingIndicator(
                                                                        modifier = Modifier.size(32.dp)
                                                                    )
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
                                                    Box(
                                                        modifier = Modifier.fillMaxSize(),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Column(
                                                            horizontalAlignment = Alignment.CenterHorizontally,
                                                            verticalArrangement = Arrangement.spacedBy(
                                                                8.dp
                                                            )
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Default.Search,
                                                                contentDescription = null,
                                                                modifier = Modifier.size(48.dp),
                                                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                                                            )
                                                            Text(
                                                                text = "No results found for \"$searchQuery\"",
                                                                style = MaterialTheme.typography.bodyLarge,
                                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                                            )
                                                        }
                                                    }
                                                }

                                                else -> {
                                                    val pullToRefreshState =
                                                        rememberPullToRefreshState()
                                                    PullToRefreshBox(
                                                        isRefreshing = isRefreshing,
                                                        onRefresh = { viewModel.refreshHomeFeed() },
                                                        state = pullToRefreshState,
                                                        modifier = Modifier.fillMaxSize(),
                                                        indicator = {
                                                            PullToRefreshDefaults.LoadingIndicator(
                                                                state = pullToRefreshState,
                                                                isRefreshing = isRefreshing,
                                                                modifier = Modifier.align(Alignment.TopCenter)
                                                            )
                                                        }
                                                    ) {
                                                        when {
                                                            homeSections.isNotEmpty() -> {
                                                                LazyColumn(
                                                                    state = homeListState,
                                                                    modifier = Modifier.fillMaxSize()
                                                                        .background(Color.Transparent),
                                                                    contentPadding = PaddingValues(
                                                                        bottom = 120.dp
                                                                    )
                                                                ) {

                                                                    items(
                                                                        items = homeSections,
                                                                        key = { section -> "section-${section.title}" },
                                                                        contentType = { section ->
                                                                            when {
                                                                                section.title.contains("もう一度聴く", ignoreCase = true) ||
                                                                                section.title.contains("listen again", ignoreCase = true) ||
                                                                                section.title.contains("クイック ピック", ignoreCase = true) ||
                                                                                section.title.contains("quick picks", ignoreCase = true) ||
                                                                                section.title.contains("speed dial", ignoreCase = true) ||
                                                                                section.title.contains("ライブラリから", ignoreCase = true) -> "quick_picks"
                                                                                section.title.contains("daily discover", ignoreCase = true) -> "daily_discover"
                                                                                section.title.contains("community", ignoreCase = true) -> "community"
                                                                                section.items.isNotEmpty() && section.items.all { it is SearchItem.Song } -> "song_list"
                                                                                else -> "carousel"
                                                                            }
                                                                        }
                                                                    ) { section ->
                                                                        HomeSectionRow(
                                                                            section = section,
                                                                            currentOnlineSong = currentOnlineSong,
                                                                            onSongTouchDown = { song ->
                                                                                playerSharedViewModel.specPrefetch(
                                                                                    SongItem.createOnlineSong(
                                                                                        song.videoId,
                                                                                        song.title,
                                                                                        song.artist,
                                                                                        "",
                                                                                        song.durationMs,
                                                                                        song.thumbnailUrl,
                                                                                        song.artistId
                                                                                    )
                                                                                )
                                                                            },
                                                                            onSongClick = { song ->
                                                                                val sectionSongs =
                                                                                    section.items.filterIsInstance<SearchItem.Song>()
                                                                                        .map { it.song }
                                                                                val idx =
                                                                                    sectionSongs.indexOfFirst { it.videoId == song.videoId }
                                                                                viewModel.playOnlineSongWithQueue(
                                                                                    song,
                                                                                    sectionSongs,
                                                                                    idx
                                                                                )
                                                                            },
                                                                            onAlbumClick = {
                                                                                viewModel.loadAlbum(
                                                                                    it.browseId
                                                                                )
                                                                            },
                                                                            onArtistClick = {
                                                                                viewModel.loadArtist(
                                                                                    it.browseId,
                                                                                    it.thumbnailUrl
                                                                                )
                                                                            },
                                                                            onPlaylistClick = {
                                                                                viewModel.loadPlaylist(
                                                                                    it.playlistId
                                                                                )
                                                                            },
                                                                            onSongMenuClick = {
                                                                                selectedSongForMenu =
                                                                                    it
                                                                            },
                                                                            onSectionClick = { browseId, params, title ->
                                                                                viewModel.loadSectionDetails(
                                                                                    browseId,
                                                                                    params,
                                                                                    title
                                                                                )
                                                                            },
                                                                            onRadioClick = { item ->
                                                                                viewModel.startRadioForItem(item)
                                                                            },
                                                                            onSaveClick = { item ->
                                                                                viewModel.saveItemToLibrary(item)
                                                                            }
                                                                        )
                                                                    }
                                                                    item {
                                                                        val isLoadingMore by viewModel.isLoadingMoreHome.collectAsStateWithLifecycle()
                                                                        if (isLoadingMore) {
                                                                            Box(
                                                                                modifier = Modifier.fillMaxWidth()
                                                                                    .padding(24.dp),
                                                                                contentAlignment = Alignment.Center
                                                                            ) {
                                                                                LoadingIndicator(
                                                                                    modifier = Modifier.size(
                                                                                        32.dp
                                                                                    )
                                                                                )
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                                LaunchedEffect(homeListState) {
                                                                    snapshotFlow { homeListState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
                                                                        .collect { lastVisibleIndex ->
                                                                            if (lastVisibleIndex != null && lastVisibleIndex >= homeSections.size) {
                                                                                viewModel.loadMoreHomeSections()
                                                                            }
                                                                        }
                                                                }
                                                            }

                                                            isLoadingHome -> HomeFeedSkeleton()

                                                            else -> {
                                                                Box(
                                                                    modifier = Modifier.fillMaxSize()
                                                                        .verticalScroll(
                                                                            rememberScrollState()
                                                                        ),
                                                                    contentAlignment = Alignment.Center
                                                                ) {
                                                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                                        Icon(
                                                                            Icons.Default.MusicNote,
                                                                            null,
                                                                            modifier = Modifier.size(
                                                                                64.dp
                                                                            ),
                                                                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                                                                alpha = 0.5f
                                                                            )
                                                                        )
                                                                        Spacer(
                                                                            modifier = Modifier.height(
                                                                                16.dp
                                                                            )
                                                                        )
                                                                        Text(androidx.compose.ui.res.stringResource(com.codetrio.overdrive.R.string.text_search_for_music),
                                                                            style = MaterialTheme.typography.titleMedium,
                                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                                        )
                                                                        Spacer(
                                                                            modifier = Modifier.height(
                                                                                8.dp
                                                                            )
                                                                        )
                                                                        OutlinedButton(onClick = { viewModel.refreshHomeFeed() }) {
                                                                            Text(androidx.compose.ui.res.stringResource(com.codetrio.overdrive.R.string.text_load_home_feed))
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        // Error Snackbar
        error?.let { errorMsg ->
            Snackbar(
                modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
                action = { TextButton(onClick = { viewModel.clearError() }) { Text(androidx.compose.ui.res.stringResource(com.codetrio.overdrive.R.string.action_ok)) } }
            ) { Text(errorMsg) }
        }
    }

    // Song 3-Dot Menu Bottom Sheet
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

@Composable
fun SongCreditsScreen(song: OnlineSong, onBack: () -> Unit) {
    val configuration = LocalConfiguration.current
        val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

        Surface(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Top Action Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // Mini Top Header: Circular Avatar + Artist + Info
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(song.thumbnailUrl?.resize(80))
                                    .crossfade(true)
                                    .build(),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                            Text(
                                text = song.artist,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                        Text(
                            text = "Song • ${
                                java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
                            }",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // Placeholder to center the middle content perfectly
                    Spacer(modifier = Modifier.size(48.dp))
                }

                val contributors = remember(song.artist) {
                    song.artist.split(Regex("[,&]| and "), 0)
                        .map { it.trim() }
                        .filter { it.isNotEmpty() }
                }

                if (isLandscape) {
                    Row(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // LEFT: Sticky Cover Art
                        Box(
                            modifier = Modifier.weight(0.45f).fillMaxHeight(),
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(song.thumbnailUrl?.resize(544))
                                    .crossfade(true)
                                    .build(),
                                contentDescription = song.title,
                                modifier = Modifier
                                    .size(240.dp)
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(16.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }

                        // RIGHT: Scrollable Credits
                        Column(
                            modifier = Modifier
                                .weight(0.55f)
                                .fillMaxHeight()
                                .verticalScroll(rememberScrollState())
                                .padding(vertical = 24.dp, horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(24.dp)
                        ) {
                            Text(
                                text = song.title,
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = (-0.5).sp
                                ),
                                color = MaterialTheme.colorScheme.onBackground
                            )

                            CreditsTextSection(title = "Performed by", content = contributors)

                            CreditsTextSection(
                                title = "Written by",
                                content = listOf(contributors.firstOrNull() ?: song.artist)
                            )

                            CreditsTextSection(
                                title = "Produced by",
                                content = listOf(if (contributors.size > 1) contributors.last() else "OverDrive Engine")
                            )

                            CreditsTextSection(
                                title = "Music metadata provided by",
                                content = listOf(
                                    contributors.firstOrNull() ?: "Online Stream Analytics"
                                )
                            )

                            Spacer(modifier = Modifier.height(32.dp))
                        }
                    }
                } else {
                    // Scrollable Credits Body (PORTRAIT)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(24.dp))

                        // Center Cover Art
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(song.thumbnailUrl?.resize(544))
                                .crossfade(true)
                                .build(),
                            contentDescription = song.title,
                            modifier = Modifier
                                .size(320.dp)
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(16.dp)),
                            contentScale = ContentScale.Crop
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        // Large Bold Title
                        Text(
                            text = song.title,
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 34.sp,
                                letterSpacing = (-0.5).sp
                            ),
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onBackground
                        )

                        Spacer(modifier = Modifier.height(40.dp))

                        // Left-aligned dynamic section lists
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(28.dp),
                            horizontalAlignment = Alignment.Start
                        ) {
                            CreditsTextSection(title = "Performed by", content = contributors)

                            CreditsTextSection(
                                title = "Written by",
                                content = listOf(contributors.firstOrNull() ?: song.artist)
                            )

                            CreditsTextSection(
                                title = "Produced by",
                                content = listOf(if (contributors.size > 1) contributors.last() else "OverDrive Engine")
                            )

                            CreditsTextSection(
                                title = "Music metadata provided by",
                                content = listOf(
                                    contributors.firstOrNull() ?: "Online Stream Analytics"
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(64.dp))
                    }
                }
            }
        }
    }

@Composable
fun CreditsTextSection(title: String, content: List<String>) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        content.forEach { item ->
            Text(
                text = item,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

fun getMoodIcon(mood: String): String = when {
    mood.contains("リラックス", ignoreCase = true) || mood.contains("relax", ignoreCase = true) -> "☕"
    mood.contains("悲しい", ignoreCase = true) || mood.contains("sad", ignoreCase = true) || mood.contains("メランコリー", ignoreCase = true) -> "🌧️"
    mood.contains("ポジティブ", ignoreCase = true) || mood.contains("positive", ignoreCase = true) || mood.contains("feel good", ignoreCase = true) -> "✨"
    mood.contains("睡眠", ignoreCase = true) || mood.contains("sleep", ignoreCase = true) -> "🌙"
    mood.contains("エナジー", ignoreCase = true) || mood.contains("energy", ignoreCase = true) -> "⚡"
    mood.contains("ワークアウト", ignoreCase = true) || mood.contains("workout", ignoreCase = true) -> "🏃"
    mood.contains("集中", ignoreCase = true) || mood.contains("focus", ignoreCase = true) || mood.contains("フォーカス", ignoreCase = true) -> "🎧"
    mood.contains("ロマンス", ignoreCase = true) || mood.contains("romance", ignoreCase = true) -> "💖"
    mood.contains("パーティ", ignoreCase = true) || mood.contains("party", ignoreCase = true) -> "🎉"
    mood.contains("ドライブ", ignoreCase = true) || mood.contains("commute", ignoreCase = true) -> "🚗"
    else -> "🎵"
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SearchHeader(
        searchQuery: String,
        onQueryChange: (String) -> Unit,
        onSearch: (String) -> Unit,
        isSearchActive: Boolean,
        accountHistory: List<OnlineSong>,
        onAccountHistorySongClick: (OnlineSong, List<OnlineSong>, Int) -> Unit,
        onSearchActiveChange: (Boolean) -> Unit,
        searchResults: List<SearchItem>,
        searchHistory: List<String>,
        onClearSearchHistory: () -> Unit,
        onRemoveFromSearchHistory: (String) -> Unit,
        onHistoryItemClick: (String) -> Unit,
        suggestions: List<String>,
        isLoadingSuggestions: Boolean,
        onSuggestionClick: (String) -> Unit,
        onAccountVisibleChange: (Boolean) -> Unit,
        userProfile: UserProfile?,
        homeMoods: List<String>,
        currentMood: String?,
        onMoodClick: (String?) -> Unit,
        currentFilter: SearchFilter?,
        onFilterClick: (SearchFilter?) -> Unit,
        isLandscape: Boolean,
        onClearSearch: () -> Unit,
        headerTitle: String = "OverDrive",
        castState: com.codetrio.overdrive.cast.CastState = com.codetrio.overdrive.cast.CastState.Disconnected,
        onCastClick: () -> Unit = {}
    ) {
        // Use WindowInsets instead of hardcoded statusBarsPadding so it adapts to any device
        val voiceLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
            contract = androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == android.app.Activity.RESULT_OK) {
                val data = result.data
                val results = data?.getStringArrayListExtra(android.speech.RecognizerIntent.EXTRA_RESULTS)
                val spokenText = results?.firstOrNull()
                if (!spokenText.isNullOrBlank()) {
                    onQueryChange(spokenText)
                    onSearch(spokenText)
                }
            }
        }
        val statusBarPadding = WindowInsets.statusBars.asPaddingValues()
        val focusRequester = remember { androidx.compose.ui.focus.FocusRequester() }
        val keyboardController = androidx.compose.ui.platform.LocalSoftwareKeyboardController.current

        // Auto request focus when search becomes active to open keyboard
        LaunchedEffect(isSearchActive) {
            if (isSearchActive) {
                kotlinx.coroutines.delay(100)
                try {
                    focusRequester.requestFocus()
                    keyboardController?.show()
                } catch (e: Exception) {
                    // Ignore if not attached
                }
            }
        }

        // Animate horizontal/vertical padding on search expand/collapse — no hardcoded values
        val horizontalPad by androidx.compose.animation.core.animateDpAsState(
            targetValue = if (isSearchActive) 0.dp else 12.dp,
            animationSpec = tween(
                durationMillis = 300,
                easing = FastOutSlowInEasing
            ),
            label = "SearchHorizontalPad"
        )
        val verticalPad by androidx.compose.animation.core.animateDpAsState(
            targetValue = if (isSearchActive) 0.dp else 6.dp,
            animationSpec = tween(
                durationMillis = 300,
                easing = FastOutSlowInEasing
            ),
            label = "SearchVerticalPad"
        )

        Column(
            modifier = Modifier.padding(
                top = if (isSearchActive) 0.dp else statusBarPadding.calculateTopPadding()
            )
        ) {
            if (!isSearchActive) {
                if (headerTitle.equals("Search", ignoreCase = true)) {
                    Surface(
                        onClick = { onSearchActiveChange(true) },
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                            .height(48.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = if (searchQuery.isNotBlank()) searchQuery else "曲、アーティスト、アルバムを検索...",
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (searchQuery.isNotBlank()) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (searchQuery.isNotBlank()) {
                                IconButton(
                                    onClick = { onClearSearch() },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Clear",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            val avatarSize = 28.dp
                            IconButton(
                                onClick = { onAccountVisibleChange(true) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                if (userProfile?.avatarUrl != null) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(LocalContext.current)
                                            .data(userProfile.avatarUrl.resize(80))
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = "Account",
                                        modifier = Modifier.size(avatarSize).clip(CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.AccountCircle,
                                        contentDescription = "Account",
                                        modifier = Modifier.size(avatarSize),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (searchQuery.isNotBlank()) searchQuery else headerTitle,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            if (searchQuery.isNotBlank()) {
                                IconButton(onClick = { onClearSearch() }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Clear Search",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            com.codetrio.overdrive.ui.components.CastButton(
                                castState = castState,
                                tint = MaterialTheme.colorScheme.onSurface,
                                activeTint = MaterialTheme.colorScheme.primary,
                                size = 24.dp,
                                onClick = onCastClick
                            )
                            IconButton(onClick = { onSearchActiveChange(true) }) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search",
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                            val avatarSize = 32.dp
                            IconButton(onClick = { onAccountVisibleChange(true) }) {
                                if (userProfile?.avatarUrl != null) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(LocalContext.current)
                                            .data(userProfile.avatarUrl.resize(80))
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = "Account",
                                        modifier = Modifier.size(avatarSize).clip(CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.AccountCircle,
                                        contentDescription = "Account",
                                        modifier = Modifier.size(avatarSize),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = horizontalPad.coerceAtLeast(0.dp),
                            vertical = verticalPad.coerceAtLeast(0.dp)
                        ),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SearchBar(
                        inputField = {
                            SearchBarDefaults.InputField(
                                query = searchQuery,
                                onQueryChange = onQueryChange,
                                onSearch = onSearch,
                                expanded = isSearchActive,
                                onExpandedChange = onSearchActiveChange,
                                modifier = Modifier.focusRequester(focusRequester),
                                placeholder = {
                                    Text(androidx.compose.ui.res.stringResource(com.codetrio.overdrive.R.string.text_search_music),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                },
                                leadingIcon = {
                                    IconButton(onClick = {
                                        onSearchActiveChange(false); onClearSearch()
                                    }) {
                                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                },
                                trailingIcon = {
                                    when {
                                        searchQuery.isNotBlank() -> IconButton(onClick = { onQueryChange("") }) {
                                            Icon(Icons.Default.Close, "Clear",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        else -> {
                                            IconButton(
                                                onClick = {
                                                    val intent = android.content.Intent(android.speech.RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                                        putExtra(android.speech.RecognizerIntent.EXTRA_LANGUAGE_MODEL, android.speech.RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                                        putExtra(android.speech.RecognizerIntent.EXTRA_PROMPT, "Speak to search...")
                                                    }
                                                    try {
                                                        voiceLauncher.launch(intent)
                                                    } catch (e: Exception) {
                                                        com.codetrio.overdrive.ui.SnackbarController.showMessage("Voice search is not supported on this device")
                                                    }
                                                }
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Mic,
                                                    contentDescription = "Voice Search",
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                },
                                colors = TextFieldDefaults.colors(
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    disabledIndicatorColor = Color.Transparent,
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent
                                )
                            )
                        },
                        expanded = isSearchActive,
                        onExpandedChange = onSearchActiveChange,
                        modifier = Modifier.fillMaxWidth(),
                        windowInsets = SearchBarDefaults.windowInsets,
                        colors = SearchBarDefaults.colors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                            dividerColor = Color.Transparent
                        )
                    ) {
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(bottom = 24.dp)
                        ) {
                            if (searchQuery.isBlank()) {
                                if (searchHistory.isNotEmpty()) {
                                    item(key = "recent_header") {
                                        Row(
                                            modifier = Modifier.fillMaxWidth()
                                                .padding(horizontal = 16.dp, vertical = 8.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = androidx.compose.ui.res.stringResource(com.codetrio.overdrive.R.string.text_recent_searches),
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            TextButton(onClick = onClearSearchHistory) { Text(androidx.compose.ui.res.stringResource(com.codetrio.overdrive.R.string.text_clear_all)) }
                                        }
                                    }
                                    items(searchHistory, key = { "hist_$it" }) { historyItem ->
                                        ListItem(
                                            headlineContent = { Text(historyItem) },
                                            leadingContent = { Icon(Icons.Default.History, null) },
                                            trailingContent = {
                                                IconButton(onClick = { onRemoveFromSearchHistory(historyItem) }) {
                                                    Icon(Icons.Default.Close, "Remove",
                                                        modifier = Modifier.size(18.dp))
                                                }
                                            },
                                            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                                            modifier = Modifier.clickable { onHistoryItemClick(historyItem) }
                                        )
                                    }
                                }

                                if (userProfile != null && accountHistory.isNotEmpty()) {
                                    item(key = "yt_history_header") {
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text(
                                            text = androidx.compose.ui.res.stringResource(com.codetrio.overdrive.R.string.text_recently_played_youtube_music),
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                        )
                                    }
                                    itemsIndexed(
                                        items = accountHistory,
                                        key = { _, song -> "yt_${song.videoId}" }
                                    ) { idx, song ->
                                        ListItem(
                                            headlineContent = {
                                                Text(song.title, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            },
                                            supportingContent = {
                                                Text(song.artist, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            },
                                            leadingContent = {
                                                AsyncImage(
                                                    model = ImageRequest.Builder(LocalContext.current)
                                                        .data(song.thumbnailUrl?.resize(120))
                                                        .crossfade(true)
                                                        .build(),
                                                    contentDescription = null,
                                                    modifier = Modifier.size(40.dp).clip(RoundedCornerShape(6.dp)),
                                                    contentScale = ContentScale.Crop
                                                )
                                            },
                                            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                                            modifier = Modifier.clickable {
                                                onSearchActiveChange(false)
                                                onAccountHistorySongClick(song, accountHistory, idx)
                                            }
                                        )
                                    }
                                }
                            } else {
                                item(key = "suggestions_animated_content") {
                                    AnimatedContent(
                                        targetState = Pair(isLoadingSuggestions, suggestions),
                                        transitionSpec = {
                                            fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
                                        },
                                        label = "suggestions_animation",
                                        modifier = Modifier.fillMaxWidth()
                                    ) { (loading, sugs) ->
                                        Column(modifier = Modifier.fillMaxWidth()) {
                                            if (loading && sugs.isEmpty()) {
                                                UnifiedShimmerProvider {
                                                    Column(modifier = Modifier.fillMaxWidth()) {
                                                        repeat(5) { i ->
                                                            SuggestionSkeletonLoader()
                                                        }
                                                    }
                                                }
                                            } else {
                                                sugs.forEach { suggestion ->
                                                    ListItem(
                                                        headlineContent = { Text(suggestion, modifier = Modifier.fillMaxWidth()) },
                                                        leadingContent = { Icon(Icons.Default.Search, null) },
                                                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                                                        modifier = Modifier.fillMaxWidth().clickable { onSuggestionClick(suggestion) }
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
            }
            // Mood chips — hidden during active search input
            if (!isSearchActive && searchQuery.isBlank() && searchResults.isEmpty() && homeMoods.isNotEmpty()) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(homeMoods, key = { "mood_$it" }) { label ->
                        val selected = currentMood == label
                        FilterChip(
                            selected = selected,
                            onClick = { onMoodClick(if (selected) null else label) },
                            label = {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold
                                    )
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
            }

            // Filter chips — shown after search results load
            if (!isSearchActive && searchQuery.isNotBlank()) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val filters = listOf(
                        null to "All",
                        SearchFilter.SONGS to "Songs",
                        SearchFilter.ALBUMS to "Albums",
                        SearchFilter.ARTISTS to "Artists",
                        SearchFilter.PLAYLISTS to "Playlists"
                    )
                    items(filters, key = { (_, label) -> "filter_$label" }) { (filter, label) ->
                        val selected = currentFilter == filter
                        FilterChip(
                            selected = selected,
                            onClick = { onFilterClick(filter) },
                            label = { Text(label, fontWeight = FontWeight.SemiBold) },
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
            }
        }
    }

@Composable
fun CategoryCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    gradientColors: List<Color>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier
            .height(90.dp)
            .clip(RoundedCornerShape(12.dp)),
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = gradientColors
                    )
                )
                .padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.align(Alignment.BottomStart)
            )
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.8f),
                modifier = Modifier
                    .size(36.dp)
                    .align(Alignment.TopEnd)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun GenresScreen(
    genresSections: List<HomeSection>,
    onBack: () -> Unit,
    onGenreClick: (title: String, browseId: String, params: String?) -> Unit
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val columns = if (isLandscape) 4 else 2

    // Pre-calculate chunked sections to avoid LazyListScope shadowing issues
    val chunkedSections = remember(genresSections, columns) {
        genresSections.map { section ->
            val sectionItems = section.items
            section.title to sectionItems.chunked(columns)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        // Sleek Premium Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = androidx.compose.ui.res.stringResource(com.codetrio.overdrive.R.string.text_moods_genres),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
        )

        if (genresSections.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                LoadingIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Transparent),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                chunkedSections.forEach { (title, chunkedItems) ->
                    item(key = "genre-section-$title") {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            chunkedItems.forEach { rowItems ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    rowItems.forEach { item ->
                                        if (item is SearchItem.Playlist) {
                                            val parts = item.playlist.playlistId.split("::")
                                            if (parts.size >= 2 && parts[0] == "GENRE") {
                                                val browseId = parts[1]
                                                val params = parts.getOrNull(2)?.takeIf { it.isNotEmpty() }
                                                
                                                val (fallbackGradient, fallbackImageUrl) = getMoodVisuals(item.playlist.title)
                                                val imageUrl = item.playlist.thumbnailUrl?.takeIf { it.isNotEmpty() }
                                                    ?: rememberGenreArtworkUrl(browseId, params, fallbackImageUrl)
                                                
                                                val gradientColors = item.playlist.color?.let { colorLong ->
                                                    val alpha = ((colorLong shr 24) and 0xFF).toInt().let { if (it == 0) 0xFF else it }
                                                    val r = ((colorLong shr 16) and 0xFF).toInt()
                                                    val g = ((colorLong shr 8) and 0xFF).toInt()
                                                    val b = (colorLong and 0xFF).toInt()
                                                    val baseColor = Color(android.graphics.Color.argb(alpha, r, g, b))
                                                    createBrightGradientFromBase(baseColor)
                                                } ?: fallbackGradient

                                                GenreCard(
                                                    title = item.playlist.title,
                                                    imageUrl = imageUrl,
                                                    gradientColors = gradientColors,
                                                    onClick = {
                                                        onGenreClick(item.playlist.title, browseId, params)
                                                    },
                                                    modifier = Modifier.weight(1f)
                                                )
                                            }
                                        }
                                    }
                                    // Filler spacing for partial row
                                    if (rowItems.size < columns) {
                                        repeat(columns - rowItems.size) {
                                            Spacer(modifier = Modifier.weight(1f))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                
                // Extra spacer at bottom to avoid player overlapping
                item {
                    Spacer(modifier = Modifier.height(100.dp))
                }
            }
        }
    }
}

private val genreArtworkCache = ConcurrentHashMap<String, String>()

@Composable
fun rememberGenreArtworkUrl(browseId: String, params: String?, fallbackUrl: String): String {
    val cacheKey = "$browseId::${params ?: ""}"
    val cached = genreArtworkCache[cacheKey]
    if (cached != null) return cached

    val urlState by produceState(initialValue = fallbackUrl, key1 = cacheKey) {
        withContext(Dispatchers.IO) {
            try {
                val result = YouTubeMusic.moodCategory(browseId, params)
                result.onSuccess { homePage ->
                    val resolved = homePage.sections
                        .flatMap { it.items }
                        .firstNotNullOfOrNull { item ->
                            when (item) {
                                is SearchItem.Playlist -> item.playlist.thumbnailUrl
                                is SearchItem.Album -> item.album.thumbnailUrl
                                is SearchItem.Song -> item.song.thumbnailUrl
                                is SearchItem.Artist -> item.artist.thumbnailUrl
                                is SearchItem.TopResult -> item.thumbnailUrl
                                is SearchItem.Header -> null
                            }?.takeIf { it.isNotEmpty() }
                        }
                    if (!resolved.isNullOrEmpty()) {
                        genreArtworkCache[cacheKey] = resolved
                        value = resolved
                    }
                }
            } catch (e: Exception) {
                // Ignore
            }
        }
    }
    return urlState
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun GenreCard(
    title: String,
    imageUrl: String,
    gradientColors: List<Color>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    // Smooth micro-animation for scaling down when pressed
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "GenreCardPressScale"
    )

    val borderColors = listOf(
        Color.White.copy(alpha = 0.15f),
        Color.White.copy(alpha = 0.03f)
    )

    Surface(
        onClick = onClick,
        interactionSource = interactionSource,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            width = 1.dp,
            brush = Brush.verticalGradient(borderColors)
        ),
        modifier = modifier
            .aspectRatio(1.6f)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            gradientColors.getOrElse(0) { Color(0xFF1E1E1E) },
                            gradientColors.getOrElse(1) { Color(0xFF121212) }
                        )
                    )
                )
        ) {
            // Radial illumination glow behind the title
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.10f),
                                Color.Transparent
                            ),
                            radius = 220f
                        )
                    )
            )

            // Title Text placed elegantly at the top-left (YT Music style) with punchy typography
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-0.5).sp,
                    lineHeight = 18.sp
                ),
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 14.dp, top = 14.dp, end = 64.dp) // Avoid overlapping with the image on the right
            )

            // Distinct, elevated, rotated cover on the bottom-right
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 10.dp, y = 10.dp) // bleed slightly out of bottom-right
                    .graphicsLayer {
                        rotationZ = -14f // beautiful rotation angle
                        shadowElevation = 12f // drop shadow
                        shape = RoundedCornerShape(10.dp)
                        clip = true
                    }
                    .size(68.dp)
                    .border(
                        width = 1.dp,
                        brush = Brush.verticalGradient(
                            listOf(
                                Color.White.copy(alpha = 0.25f),
                                Color.Transparent
                            )
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                    .background(Color.Black.copy(alpha = 0.3f))
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(imageUrl.resize(240))
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
private fun SuggestionSkeletonLoader(modifier: Modifier = Modifier) {
    ListItem(
        headlineContent = {
            ShimmerModifier(width = 240.dp, height = 20.dp, shape = RoundedCornerShape(4.dp))
        },
        leadingContent = {
            ShimmerModifier(width = 28.dp, height = 28.dp, shape = androidx.compose.foundation.shape.CircleShape)
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        modifier = modifier
    )
}
