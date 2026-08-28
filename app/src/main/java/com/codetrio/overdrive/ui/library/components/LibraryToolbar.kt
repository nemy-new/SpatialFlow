package com.codetrio.overdrive.ui.library.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ViewModule
import androidx.compose.material.icons.filled.ViewStream
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.codetrio.overdrive.R
import com.codetrio.overdrive.ui.library.model.LibrarySortOrder
import com.codetrio.overdrive.ui.library.model.LibraryViewMode

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LibraryToolbar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    sortOrder: LibrarySortOrder,
    onSortOrderChange: (LibrarySortOrder) -> Unit,
    viewMode: LibraryViewMode,
    onViewModeChange: (LibraryViewMode) -> Unit,
    onCreatePlaylist: () -> Unit,
    modifier: Modifier = Modifier,
    showSearch: Boolean = true,
    showNewPlaylist: Boolean = true,
    availableSortOrders: List<LibrarySortOrder> = LibrarySortOrder.entries
) {
    var sortMenuExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        if (showSearch) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = {
                    Text(
                        stringResource(R.string.lib_search_placeholder),
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = { onSearchQueryChange("") },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                },
                singleLine = true,
                shape = CircleShape,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                    focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                    unfocusedBorderColor = Color.Transparent
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Action controls row: Sort Selector & M3 SingleChoiceSegmentedButtonRow & New Playlist
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Sort Selector Chip with Dropdown
            Box {
                FilterChip(
                    selected = true,
                    onClick = { sortMenuExpanded = true },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Sort,
                            contentDescription = "Sort",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    label = {
                        Text(
                            text = stringResource(sortOrder.titleRes),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    },
                    shape = RoundedCornerShape(12.dp),
                    border = null,
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                        labelColor = MaterialTheme.colorScheme.onSurface,
                        selectedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier.height(36.dp)
                )

                DropdownMenu(
                    expanded = sortMenuExpanded,
                    onDismissRequest = { sortMenuExpanded = false },
                    shape = RoundedCornerShape(16.dp)
                ) {
                    availableSortOrders.forEach { order ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = stringResource(order.titleRes),
                                    fontWeight = if (order == sortOrder) FontWeight.Bold else FontWeight.Normal,
                                    color = if (order == sortOrder) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                            },
                            onClick = {
                                onSortOrderChange(order)
                                sortMenuExpanded = false
                            }
                        )
                    }
                }
            }

            // Right Group: M3 Segmented Button Row (4-way view mode) & Add Playlist
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val modes = listOf(
                    LibraryViewMode.LARGE_GRID to Pair(Icons.Default.GridView, stringResource(R.string.lib_view_large_grid)),
                    LibraryViewMode.COMPACT_GRID to Pair(Icons.Default.ViewModule, stringResource(R.string.lib_view_compact_grid)),
                    LibraryViewMode.STANDARD_LIST to Pair(Icons.AutoMirrored.Filled.FormatListBulleted, stringResource(R.string.lib_view_standard_list)),
                    LibraryViewMode.COMPACT_LIST to Pair(Icons.Default.ViewStream, stringResource(R.string.lib_view_compact_list))
                )

                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier.height(36.dp)
                ) {
                    modes.forEachIndexed { index, (mode, iconAndDesc) ->
                        val (icon, desc) = iconAndDesc
                        val isSelected = viewMode == mode
                        SegmentedButton(
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = modes.size),
                            onClick = { onViewModeChange(mode) },
                            selected = isSelected,
                            icon = {},
                            label = {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = desc,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            colors = SegmentedButtonDefaults.colors(
                                activeContainerColor = MaterialTheme.colorScheme.primary,
                                activeContentColor = MaterialTheme.colorScheme.onPrimary,
                                inactiveContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                                inactiveContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }

                if (showNewPlaylist) {
                    IconButton(
                        onClick = onCreatePlaylist,
                        modifier = Modifier.size(36.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = stringResource(R.string.lib_hero_new_playlist),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
