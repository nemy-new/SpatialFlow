package com.codetrio.overdrive.ui.settings

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.codetrio.overdrive.model.BottomNavTab
import com.codetrio.overdrive.viewmodel.PlayerSharedViewModel
import java.util.Collections

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomNavCustomizeScreen(
    playerViewModel: PlayerSharedViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("AppSettings", android.content.Context.MODE_PRIVATE) }
    
    var currentTabs by remember { 
        mutableStateOf(BottomNavTab.parse(prefs.getString("bottom_nav_tabs", null)))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Customize Bottom Nav") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            item {
                Text(
                    text = "Reorder or hide tabs in the bottom navigation bar.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }
            
            itemsIndexed(currentTabs) { index, tab ->
                val tabName = when (tab.route) {
                    "explore" -> "Home"
                    "search" -> "Search"
                    "library" -> "Library"
                    "statistics" -> "Stats"
                    "effects" -> "Effects"
                    "settings" -> "Settings"
                    else -> tab.route.replaceFirstChar { it.uppercase() }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Text(
                        text = tabName,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f)
                    )
                    
                    // Visible toggle
                    Switch(
                        checked = tab.isVisible,
                        onCheckedChange = { isVisible ->
                            val newTabs = currentTabs.toMutableList()
                            newTabs[index] = tab.copy(isVisible = isVisible)
                            // ensure at least one is visible
                            if (newTabs.none { it.isVisible }) {
                                Toast.makeText(context, "At least one tab must be visible", Toast.LENGTH_SHORT).show()
                            } else {
                                currentTabs = newTabs
                                prefs.edit().putString("bottom_nav_tabs", BottomNavTab.serialize(newTabs)).apply()
                            }
                        }
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    // Move up
                    IconButton(
                        onClick = {
                            if (index > 0) {
                                val newTabs = currentTabs.toMutableList()
                                Collections.swap(newTabs, index, index - 1)
                                currentTabs = newTabs
                                prefs.edit().putString("bottom_nav_tabs", BottomNavTab.serialize(newTabs)).apply()
                            }
                        },
                        enabled = index > 0
                    ) {
                        Icon(Icons.Default.ArrowUpward, contentDescription = "Up")
                    }
                    
                    // Move down
                    IconButton(
                        onClick = {
                            if (index < currentTabs.size - 1) {
                                val newTabs = currentTabs.toMutableList()
                                Collections.swap(newTabs, index, index + 1)
                                currentTabs = newTabs
                                prefs.edit().putString("bottom_nav_tabs", BottomNavTab.serialize(newTabs)).apply()
                            }
                        },
                        enabled = index < currentTabs.size - 1
                    ) {
                        Icon(Icons.Default.ArrowDownward, contentDescription = "Down")
                    }
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
            }
        }
    }
}
