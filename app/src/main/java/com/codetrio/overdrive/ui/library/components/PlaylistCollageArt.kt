package com.codetrio.overdrive.ui.library.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.codetrio.overdrive.R

@Composable
fun PlaylistCollageArt(
    thumbnails: List<String>,
    modifier: Modifier = Modifier,
    isCircle: Boolean = false,
    shape: Shape = if (isCircle) CircleShape else RoundedCornerShape(12.dp),
    iconSize: Dp = 32.dp
) {
    val context = LocalContext.current
    val validThumbnails = thumbnails.filter { it.isNotBlank() }.distinct()

    Box(
        modifier = modifier
            .clip(shape)
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surfaceContainerHigh,
                        MaterialTheme.colorScheme.surfaceContainerHighest
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        when {
            validThumbnails.size >= 4 && !isCircle -> {
                // 2x2 Collage
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(modifier = Modifier.weight(1f)) {
                        AsyncImage(
                            model = ImageRequest.Builder(context).data(validThumbnails[0]).crossfade(true).build(),
                            contentDescription = null,
                            modifier = Modifier.weight(1f).fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        AsyncImage(
                            model = ImageRequest.Builder(context).data(validThumbnails[1]).crossfade(true).build(),
                            contentDescription = null,
                            modifier = Modifier.weight(1f).fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Row(modifier = Modifier.weight(1f)) {
                        AsyncImage(
                            model = ImageRequest.Builder(context).data(validThumbnails[2]).crossfade(true).build(),
                            contentDescription = null,
                            modifier = Modifier.weight(1f).fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        AsyncImage(
                            model = ImageRequest.Builder(context).data(validThumbnails[3]).crossfade(true).build(),
                            contentDescription = null,
                            modifier = Modifier.weight(1f).fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
            validThumbnails.isNotEmpty() -> {
                // Single Artwork
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(validThumbnails.first())
                        .crossfade(true)
                        .error(R.drawable.ic_music_note)
                        .build(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            else -> {
                // Placeholder
                Icon(
                    imageVector = if (isCircle) Icons.Default.Person else Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.size(iconSize)
                )
            }
        }
    }
}
