package com.codetrio.overdrive.ui.components

import android.app.Activity
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Cast
import androidx.compose.material.icons.rounded.CastConnected
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.mediarouter.app.MediaRouteChooserDialogFragment
import androidx.mediarouter.app.MediaRouteControllerDialogFragment
import com.codetrio.overdrive.cast.CastState
import com.google.android.gms.cast.framework.CastContext

fun showCastDialog(activity: FragmentActivity, isConnected: Boolean) {
    try {
        if (isConnected) {
            val dialog = MediaRouteControllerDialogFragment()
            dialog.show(activity.supportFragmentManager, "MediaRouteControllerDialogFragment")
        } else {
            val castContext = CastContext.getSharedInstance(activity)
            val selector = castContext.mergedSelector
            if (selector != null) {
                val dialog = MediaRouteChooserDialogFragment()
                dialog.routeSelector = selector
                dialog.show(activity.supportFragmentManager, "MediaRouteChooserDialogFragment")
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

@Composable
fun CastButton(
    castState: CastState,
    modifier: Modifier = Modifier,
    tint: Color = Color.White,
    activeTint: Color = MaterialTheme.colorScheme.primary,
    size: Dp = 24.dp,
    onClick: (() -> Unit)? = null
) {
    val infiniteTransition = rememberInfiniteTransition(label = "cast_connecting")
    val connectingAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cast_alpha"
    )

    val connectedState = castState as? CastState.Connected
    val isConnected = connectedState != null
    val isConnecting = castState is CastState.Connecting

    val iconColor by animateColorAsState(
        targetValue = if (isConnected) activeTint else tint,
        animationSpec = tween(300),
        label = "cast_tint"
    )

    val currentAlpha = if (isConnecting) connectingAlpha else 1.0f

    IconButton(
        onClick = {
            onClick?.invoke()
        },
        modifier = modifier
    ) {
        Icon(
            imageVector = if (isConnected) Icons.Rounded.CastConnected else Icons.Rounded.Cast,
            contentDescription = if (connectedState != null) "Cast to ${connectedState.deviceName}" else "Cast",
            tint = iconColor,
            modifier = Modifier
                .size(size)
                .alpha(currentAlpha)
        )
    }
}

@Composable
fun CastStatusBadge(
    castState: CastState,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    if (castState !is CastState.Connected) return

    val deviceName = castState.deviceName

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f))
            .clickable {
                onClick?.invoke()
            }
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Icon(
            imageVector = Icons.Rounded.CastConnected,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = deviceName,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            maxLines = 1
        )
    }
}
