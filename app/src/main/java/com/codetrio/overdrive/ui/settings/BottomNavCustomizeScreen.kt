package com.codetrio.overdrive.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.codetrio.overdrive.viewmodel.PlayerSharedViewModel
import com.codetrio.overdrive.ui.components.SettingsDetailTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomNavCustomizeScreen(
    playerViewModel: PlayerSharedViewModel,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = { SettingsDetailTopBar("Customize Bottom Nav", onBack) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Coming Soon...",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
