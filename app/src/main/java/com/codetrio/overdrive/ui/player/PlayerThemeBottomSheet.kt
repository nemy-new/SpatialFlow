package com.codetrio.overdrive.ui.player

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PlayerThemeBottomSheet(
    onDismissRequest: () -> Unit,
    currentTheme: String,
    onThemeSelect: (String) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp)
        ) {
            Text(
                text = androidx.compose.ui.res.stringResource(com.codetrio.overdrive.R.string.text_player_theme),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Option 1: Fluid Theme
            ListItem(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onThemeSelect("fluid") },
                headlineContent = {
                    Text(
                        text = androidx.compose.ui.res.stringResource(com.codetrio.overdrive.R.string.text_fluid_theme),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                supportingContent = {
                    Text(
                        text = androidx.compose.ui.res.stringResource(com.codetrio.overdrive.R.string.text_organic_mesh_gradient_with_drifting_colors_and_video_motion_art_when_available),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingContent = {
                    RadioButton(
                        selected = currentTheme == "fluid",
                        onClick = { onThemeSelect("fluid") }
                    )
                },
                colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent)
            )

            HorizontalDivider(
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f),
                thickness = 1.dp,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // Option 2: Dynamic Fluid Canvas (Living Mesh Gradient)
            ListItem(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onThemeSelect("mesh") },
                headlineContent = {
                    Text(
                        text = androidx.compose.ui.res.stringResource(com.codetrio.overdrive.R.string.text_fluid_mesh_theme),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                supportingContent = {
                    Text(
                        text = androidx.compose.ui.res.stringResource(com.codetrio.overdrive.R.string.text_fluid_mesh_theme_desc),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingContent = {
                    RadioButton(
                        selected = currentTheme == "mesh",
                        onClick = { onThemeSelect("mesh") }
                    )
                },
                colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent)
            )

            HorizontalDivider(
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f),
                thickness = 1.dp,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // Option 2: Static Theme
            ListItem(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onThemeSelect("static") },
                headlineContent = {
                    Text(
                        text = androidx.compose.ui.res.stringResource(com.codetrio.overdrive.R.string.text_static_theme),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                supportingContent = {
                    Text(
                        text = androidx.compose.ui.res.stringResource(com.codetrio.overdrive.R.string.text_single_solid_background_color_with_video_motion_art_when_available),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingContent = {
                    RadioButton(
                        selected = currentTheme == "static",
                        onClick = { onThemeSelect("static") }
                    )
                },
                colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent)
            )

            HorizontalDivider(
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f),
                thickness = 1.dp,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // Option 3: Immersion Theme
            ListItem(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onThemeSelect("immersion") },
                headlineContent = {
                    Text(
                        text = androidx.compose.ui.res.stringResource(com.codetrio.overdrive.R.string.text_immersion_theme),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                supportingContent = {
                    Text(
                        text = androidx.compose.ui.res.stringResource(com.codetrio.overdrive.R.string.text_edge_to_edge_album_artwork_blending_seamlessly_into_an_ambient_gradient_with_redesigned_controls),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingContent = {
                    RadioButton(
                        selected = currentTheme == "immersion",
                        onClick = { onThemeSelect("immersion") }
                    )
                },
                colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent)
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Close Button
            Button(
                onClick = onDismissRequest,
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.filledTonalButtonColors(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Icon(Icons.Default.Close, contentDescription = "Close Theme Selector", modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(androidx.compose.ui.res.stringResource(com.codetrio.overdrive.R.string.text_close), fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
