package com.codetrio.overdrive.ui.player

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.QueueMusic
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DragHandle
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.codetrio.overdrive.R
import com.codetrio.overdrive.model.SongItem
import com.codetrio.overdrive.viewmodel.PlayerSharedViewModel
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState
import kotlin.math.abs

@Composable
fun TabletQueuePane(
    songList: List<SongItem>,
    currentSongIndex: Int,
    isShuffleEnabled: Boolean,
    repeatMode: Int,
    sleepTimerMode: PlayerSharedViewModel.SleepTimerMode,
    onReorderQueue: (Int, Int) -> Unit,
    onPlaySongAtIndex: (Int) -> Unit,
    onRemoveSongAtIndex: (Int) -> Unit,
    onToggleShuffle: () -> Unit,
    onToggleLoopMode: () -> Unit,
    onShowSleepTimerDialog: () -> Unit,
    dynamicAccentColor: Color,
    contentColor: Color = Color.White,
    contentSecondary: Color = Color.White.copy(alpha = 0.7f),
    isDark: Boolean = true,
    isAutoplayEnabled: Boolean = false,
    onAutoplayToggle: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val lazyListState = rememberLazyListState()

    var localSongList by remember(songList) { mutableStateOf(songList) }
    var lastMovedFrom by remember { mutableStateOf<Int?>(null) }
    var lastMovedTo by remember { mutableStateOf<Int?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var isSearchExpanded by remember { mutableStateOf(false) }

    val filteredSongList = remember(localSongList, searchQuery) {
        if (searchQuery.isEmpty()) {
            localSongList
        } else {
            localSongList.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                it.artist.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    val reorderableState = rememberReorderableLazyListState(
        lazyListState = lazyListState,
        onMove = { from, to ->
            val fromIndex = localSongList.indexOfFirst { it.id == from.key }
            val toIndex = localSongList.indexOfFirst { it.id == to.key }
            if (fromIndex != -1 && toIndex != -1 && fromIndex != toIndex) {
                localSongList = localSongList.toMutableList().apply {
                    add(toIndex, removeAt(fromIndex))
                }
                if (lastMovedFrom == null) {
                    lastMovedFrom = fromIndex
                }
                lastMovedTo = toIndex
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            }
        }
    )

    LaunchedEffect(songList) {
        if (!reorderableState.isAnyItemDragging) {
            localSongList = songList
        }
    }

    LaunchedEffect(reorderableState.isAnyItemDragging) {
        if (!reorderableState.isAnyItemDragging) {
            val fromIdx = lastMovedFrom
            val toIdx = lastMovedTo
            lastMovedFrom = null
            lastMovedTo = null

            if (fromIdx != null && toIdx != null && fromIdx != toIdx) {
                onReorderQueue(fromIdx, toIdx)
            }
        }
    }

    // Auto-scroll to currently playing song
    LaunchedEffect(currentSongIndex) {
        if (currentSongIndex in songList.indices) {
            val currentSongId = songList[currentSongIndex].id
            val localIndex = localSongList.indexOfFirst { it.id == currentSongId }
            if (localIndex != -1) {
                val distance = abs(lazyListState.firstVisibleItemIndex - localIndex)
                if (distance > 16) {
                    lazyListState.scrollToItem(localIndex)
                } else {
                    lazyListState.animateScrollToItem(localIndex)
                }
            }
        }
    }

    val controlButtonBg = if (isDark) Color.White.copy(alpha = 0.08f) else Color(0xFF111215).copy(alpha = 0.05f)

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        // --- 1. Sleek Tablet Header & Action Toolbar ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Clean Track Count Badge
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isDark) dynamicAccentColor.copy(alpha = 0.14f) else Color(0xFF111215).copy(alpha = 0.06f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.AutoMirrored.Rounded.QueueMusic,
                        contentDescription = null,
                        tint = dynamicAccentColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = if (songList.isNotEmpty()) {
                            "${songList.size}曲 • 再生中: ${currentSongIndex + 1}/${songList.size}"
                        } else {
                            "0曲"
                        },
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = dynamicAccentColor
                        )
                    )
                }
            }

            // Right: Integrated Compact Controls Bar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Autoplay Compact Toggle
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(controlButtonBg)
                        .padding(start = 10.dp, end = 2.dp, top = 2.dp, bottom = 2.dp)
                ) {
                    Text(
                        text = "自動再生",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = contentSecondary
                    )
                    Switch(
                        checked = isAutoplayEnabled,
                        onCheckedChange = onAutoplayToggle,
                        thumbContent = {
                            if (isAutoplayEnabled) {
                                Icon(
                                    imageVector = androidx.compose.material.icons.Icons.Rounded.PlayArrow,
                                    contentDescription = null,
                                    modifier = Modifier.size(SwitchDefaults.IconSize)
                                )
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = if (isDark) Color.Black else Color.White,
                            checkedTrackColor = dynamicAccentColor,
                            checkedBorderColor = Color.Transparent,
                            checkedIconColor = if (isDark) Color.White else Color.Black,
                            uncheckedThumbColor = if (isDark) Color.White.copy(0.7f) else Color(0xFF111215).copy(0.6f),
                            uncheckedTrackColor = if (isDark) Color.White.copy(0.12f) else Color(0xFF111215).copy(0.10f),
                            uncheckedBorderColor = Color.Transparent
                        ),
                        modifier = Modifier.scale(0.65f)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Shuffle Button
                val shuffleBg = if (isShuffleEnabled) dynamicAccentColor.copy(alpha = if (isDark) 0.22f else 0.12f) else controlButtonBg
                val shuffleTint = if (isShuffleEnabled) dynamicAccentColor else contentSecondary
                IconButton(
                    onClick = {
                        onToggleShuffle()
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    },
                    modifier = Modifier.size(36.dp),
                    colors = IconButtonDefaults.iconButtonColors(containerColor = shuffleBg)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_shuffle),
                        contentDescription = "Shuffle",
                        tint = shuffleTint,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Repeat Mode Button
                val loopActive = repeatMode != PlayerSharedViewModel.REPEAT_OFF
                val loopBg = if (loopActive) dynamicAccentColor.copy(alpha = if (isDark) 0.22f else 0.12f) else controlButtonBg
                val loopTint = if (loopActive) dynamicAccentColor else contentSecondary
                val loopIcon = if (repeatMode == PlayerSharedViewModel.REPEAT_ONE) R.drawable.ic_repeat_one else R.drawable.ic_repeat
                IconButton(
                    onClick = {
                        onToggleLoopMode()
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    },
                    modifier = Modifier.size(36.dp),
                    colors = IconButtonDefaults.iconButtonColors(containerColor = loopBg)
                ) {
                    Icon(
                        painter = painterResource(id = loopIcon),
                        contentDescription = "Repeat",
                        tint = loopTint,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Sleep Timer Button
                val timerActive = sleepTimerMode != PlayerSharedViewModel.SleepTimerMode.OFF
                val timerBg = if (timerActive) dynamicAccentColor.copy(alpha = if (isDark) 0.22f else 0.12f) else controlButtonBg
                val timerTint = if (timerActive) dynamicAccentColor else contentSecondary
                IconButton(
                    onClick = {
                        onShowSleepTimerDialog()
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    },
                    modifier = Modifier.size(36.dp),
                    colors = IconButtonDefaults.iconButtonColors(containerColor = timerBg)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_timer),
                        contentDescription = "Sleep Timer",
                        tint = timerTint,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Search Filter Button
                IconButton(
                    onClick = {
                        isSearchExpanded = !isSearchExpanded
                        if (!isSearchExpanded) searchQuery = ""
                    },
                    modifier = Modifier.size(36.dp),
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = if (isSearchExpanded) dynamicAccentColor.copy(alpha = if (isDark) 0.22f else 0.12f) else controlButtonBg
                    )
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = "Search Queue",
                        tint = if (isSearchExpanded) dynamicAccentColor else contentSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // --- 2. Expandable Sleek Search Bar ---
        AnimatedVisibility(
            visible = isSearchExpanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                placeholder = {
                    Text(
                        text = "キュー内の楽曲を検索...",
                        style = MaterialTheme.typography.bodySmall,
                        color = contentSecondary.copy(alpha = 0.6f)
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = null,
                        tint = dynamicAccentColor,
                        modifier = Modifier.size(18.dp)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(28.dp)) {
                            Icon(
                                imageVector = Icons.Rounded.Clear,
                                contentDescription = "Clear",
                                tint = contentSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = contentColor,
                    unfocusedTextColor = contentColor,
                    focusedContainerColor = contentColor.copy(alpha = 0.08f),
                    unfocusedContainerColor = contentColor.copy(alpha = 0.04f),
                    focusedBorderColor = dynamicAccentColor,
                    unfocusedBorderColor = contentColor.copy(alpha = 0.12f),
                    cursorColor = dynamicAccentColor
                )
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // --- 3. Queue List with Reordering ---
        if (filteredSongList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.AutoMirrored.Rounded.QueueMusic,
                        contentDescription = null,
                        tint = contentSecondary.copy(alpha = 0.4f),
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text = if (searchQuery.isNotEmpty()) "一致する楽曲が見つかりません" else "キューは空です",
                        style = MaterialTheme.typography.bodyMedium,
                        color = contentSecondary.copy(alpha = 0.6f)
                    )
                }
            }
        } else {
            LazyColumn(
                state = lazyListState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val currentSongId = songList.getOrNull(currentSongIndex)?.id
                itemsIndexed(
                    items = filteredSongList,
                    key = { _, song -> song.id },
                    contentType = { _, _ -> "tablet-queue-item" }
                ) { index, song ->
                    val isPlaying = (song.id == currentSongId)
                    val originalIndex = songList.indexOfFirst { it.id == song.id }

                    if (searchQuery.isNotEmpty()) {
                        TabletQueueItem(
                            song = song,
                            isPlaying = isPlaying,
                            contentColor = contentColor,
                            contentSecondary = contentSecondary,
                            dynamicAccentColor = dynamicAccentColor,
                            onClick = {
                                if (originalIndex != -1) onPlaySongAtIndex(originalIndex)
                            },
                            onRemoveClick = {
                                if (originalIndex != -1) onRemoveSongAtIndex(originalIndex)
                            },
                            dragHandle = {}
                        )
                    } else {
                        ReorderableItem(
                            state = reorderableState,
                            key = song.id
                        ) { isDragging ->
                            val scale by animateFloatAsState(
                                targetValue = if (isDragging) 1.02f else 1f,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioNoBouncy,
                                    stiffness = Spring.StiffnessMediumLow
                                ),
                                label = "tabletQueueItemScale"
                            )

                            TabletQueueItem(
                                song = song,
                                isPlaying = isPlaying,
                                contentColor = contentColor,
                                contentSecondary = contentSecondary,
                                dynamicAccentColor = dynamicAccentColor,
                                isDark = isDark,
                                onClick = {
                                    if (originalIndex != -1) onPlaySongAtIndex(originalIndex)
                                },
                                onRemoveClick = {
                                    if (originalIndex != -1) onRemoveSongAtIndex(originalIndex)
                                },
                                dragHandle = {
                                    Box(
                                        modifier = Modifier
                                            .draggableHandle(
                                                onDragStarted = {
                                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                }
                                            )
                                            .padding(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.DragHandle,
                                            contentDescription = "Reorder",
                                            tint = contentSecondary.copy(alpha = 0.6f),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                },
                                modifier = Modifier
                                    .graphicsLayer {
                                        scaleX = scale
                                        scaleY = scale
                                        shadowElevation = if (isDragging) 8f else 0f
                                    }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TabletQueueItem(
    song: SongItem,
    isPlaying: Boolean,
    contentColor: Color,
    contentSecondary: Color,
    dynamicAccentColor: Color,
    onClick: () -> Unit,
    onRemoveClick: (() -> Unit)?,
    dragHandle: @Composable () -> Unit,
    isDark: Boolean = androidx.compose.foundation.isSystemInDarkTheme(),
    modifier: Modifier = Modifier
) {
    val albumArtModel = remember(song.id) { song.getAlbumArtUri() ?: R.drawable.default_album_art }
    val itemBgColor = if (isPlaying) {
        if (isDark) dynamicAccentColor.copy(alpha = 0.16f) else Color(0xFF111215).copy(alpha = 0.07f)
    } else {
        if (isDark) Color.White.copy(alpha = 0.04f) else Color(0xFF111215).copy(alpha = 0.025f)
    }

    Surface(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp)),
        color = itemBgColor,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 6.dp, end = 8.dp, top = 6.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Glowing Indicator Pill
            Box(
                modifier = Modifier
                    .width(3.5.dp)
                    .height(24.dp)
                    .clip(CircleShape)
                    .background(if (isPlaying) dynamicAccentColor else Color.Transparent)
            )

            Spacer(modifier = Modifier.width(6.dp))

            // Thumbnail container with animated equalizer for playing item
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = albumArtModel,
                    contentDescription = song.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                if (isPlaying) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.45f)),
                        contentAlignment = Alignment.Center
                    ) {
                        val transition = rememberInfiniteTransition(label = "equalizer")
                        val line1Scale by transition.animateFloat(
                            initialValue = 0.2f,
                            targetValue = 0.85f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(durationMillis = 460, easing = FastOutSlowInEasing),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "eq1"
                        )
                        val line2Scale by transition.animateFloat(
                            initialValue = 0.35f,
                            targetValue = 0.98f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(durationMillis = 380, easing = LinearOutSlowInEasing),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "eq2"
                        )
                        val line3Scale by transition.animateFloat(
                            initialValue = 0.15f,
                            targetValue = 0.75f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(durationMillis = 520, easing = FastOutSlowInEasing),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "eq3"
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(2.5.dp),
                            verticalAlignment = Alignment.Bottom,
                            modifier = Modifier
                                .height(14.dp)
                                .align(Alignment.Center)
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(3.dp)
                                    .fillMaxHeight(line1Scale)
                                    .background(dynamicAccentColor, RoundedCornerShape(1.5.dp))
                            )
                            Box(
                                modifier = Modifier
                                    .width(3.dp)
                                    .fillMaxHeight(line2Scale)
                                    .background(dynamicAccentColor, RoundedCornerShape(1.5.dp))
                            )
                            Box(
                                modifier = Modifier
                                    .width(3.dp)
                                    .fillMaxHeight(line3Scale)
                                    .background(dynamicAccentColor, RoundedCornerShape(1.5.dp))
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Song Info Column
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = if (isPlaying) FontWeight.Bold else FontWeight.SemiBold,
                        color = if (isPlaying) dynamicAccentColor else contentColor
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = song.artist,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = contentSecondary
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (song.duration > 0) {
                        val min = song.duration / 60
                        val sec = song.duration % 60
                        Text(
                            text = "•  %d:%02d".format(min, sec),
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = contentSecondary.copy(alpha = 0.5f),
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }

            // Remove Button
            if (onRemoveClick != null) {
                IconButton(
                    onClick = onRemoveClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Remove from Queue",
                        tint = contentSecondary.copy(alpha = 0.45f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Drag Handle
            dragHandle()
        }
    }
}
