package com.codetrio.overdrive.ui.player

import android.graphics.Bitmap
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.graphics.applyCanvas
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.drawable.toDrawable
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil.compose.AsyncImage
import coil.imageLoader
import coil.request.ImageRequest
import com.flaviofaria.kenburnsview.KenBurnsView
import com.flaviofaria.kenburnsview.RandomTransitionGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun SpatialWrapper(content: @Composable () -> Unit) = key(content.hashCode()) {
    content()
}

@Stable
private enum class Option {
    Set,
    Pause,
    Resume,
    Init
}

/**
 * SpatialFlow full-screen player blurred artwork canvas with KenBurns slow pan/zoom motion engine,
 * 2.0x saturation boost, RenderScript hardware blur, and page transition dimming overlay.
 */
@Composable
fun SpatialFloatingLight(
    modifier: Modifier = Modifier,
    album: () -> Any?,
    isPlaying: () -> Boolean,
    isLyricsPage: () -> Boolean = { false },
    showMiniPlayer: () -> Boolean = { false },
    backgroundEffectEnabled: Boolean = true
) {
    val drawable = remember(album()) {
        mutableStateOf<Drawable?>(null)
    }

    val context = LocalContext.current
    val imageLoader = context.imageLoader

    SpatialWrapper {
        LaunchedEffect(album()) {
            val albumData = album() ?: return@LaunchedEffect
            withContext(Dispatchers.IO) {
                try {
                    val request = ImageRequest.Builder(context)
                        .data(albumData)
                        .allowHardware(false)
                        .build()

                    val rawBitmap = imageLoader.execute(request).drawable?.toBitmap()
                    if (rawBitmap != null) {
                        val compressed = BitmapResolver.bitmapCompress(
                            bitmap = rawBitmap,
                            kenBurnsMode = backgroundEffectEnabled
                        )

                        val resolved = imageResolve(compressed)

                        drawable.value = resolved.toDrawable(context.resources)
                    }
                } catch (_: Exception) {
                    drawable.value = null
                }
            }
        }
    }

    SpatialWrapper {
        val lossEffect = remember("SpatialFloatingLight_lossEffect") {
            derivedStateOf {
                !isLyricsPage()
            }
        }

        val useBackground = remember(album()) {
            derivedStateOf {
                album() == null
            }
        }

        if (backgroundEffectEnabled) {
            val lastOption = remember("SpatialFloatingLight_lastOption") {
                mutableStateOf(Option.Init.name)
            }
            SpatialWrapper {
                val lifecycleState = LocalLifecycleOwner.current.lifecycle.currentStateFlow.collectAsState()
                val active = lifecycleState.value.isAtLeast(Lifecycle.State.RESUMED) && !showMiniPlayer()

                AndroidView(
                    factory = { ctx ->
                        KenBurnsView(ctx).apply {
                            setTransitionGenerator(
                                RandomTransitionGenerator(
                                    12000,
                                    AccelerateDecelerateInterpolator()
                                )
                            )
                        }
                    },
                    modifier = modifier.drawWithCache {
                        onDrawBehind {
                            if (useBackground.value) {
                                drawRect(Color.Black)
                            }
                        }
                    }
                ) { view ->
                    if (drawable.value != null) {
                        if (view.drawable != drawable.value) {
                            val thisOptionType = Option.Set.name
                            if (lastOption.value == thisOptionType) return@AndroidView
                            view.setImageDrawable(drawable.value!!)
                            lastOption.value = thisOptionType
                        } else if (!isPlaying() || !active) {
                            val thisOptionType = Option.Pause.name
                            if (lastOption.value == thisOptionType) return@AndroidView
                            view.pause()
                            lastOption.value = thisOptionType
                        } else {
                            val thisOptionType = Option.Resume.name
                            if (lastOption.value == thisOptionType) return@AndroidView
                            view.resume()
                            lastOption.value = thisOptionType
                        }
                    }
                }
            }
        } else {
            SpatialWrapper {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(data = drawable.value)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = modifier
                        .graphicsLayer {
                            compositingStrategy = CompositingStrategy.Offscreen
                        }
                        .drawWithCache {
                            onDrawBehind {
                                if (useBackground.value) {
                                    drawRect(Color.Black)
                                }
                            }
                        }
                )
            }
        }

        // Animated Darkness Overlay for non-lyrics page states
        SpatialWrapper {
            val alpha = animateFloatAsState(
                targetValue = if (lossEffect.value) 0.618f else 0f,
                animationSpec = tween(
                    durationMillis = 300,
                    easing = FastOutSlowInEasing
                ),
                label = "lossEffectAlpha"
            )
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(data = drawable.value)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        compositingStrategy = CompositingStrategy.Offscreen
                        this.alpha = alpha.value
                    },
                colorFilter = ColorFilter.tint(Color(0x33000000), BlendMode.Overlay)
            )
        }
    }
}

/**
 * RenderScript Hardware Blur & Saturation Processing Pipeline.
 * 1. Saturation boosted by 2.0x via ColorMatrix.
 * 2. Overlay tinting applied for text contrast.
 * 3. RenderScript Toolkit.blur (radius 12) executed on downscaled bitmap.
 */
fun imageResolve(image: Bitmap, moreLight: Boolean = false): Bitmap {
    val processedBitmap = image.copy(Bitmap.Config.ARGB_8888, true)
    processedBitmap.applyCanvas {
        val paint = Paint().apply {
            isAntiAlias = true
            isFilterBitmap = true
            isDither = true
        }

        val saturationMatrix = ColorMatrix()
        saturationMatrix.setSaturation(2.2f)

        paint.colorFilter = ColorMatrixColorFilter(saturationMatrix)
        drawBitmap(processedBitmap, 0f, 0f, paint)

        if (moreLight) {
            drawColor(0x26FFFFFF.toInt())
            drawColor(0x4DFFFFFF.toInt(), PorterDuff.Mode.OVERLAY)
        } else {
            drawColor(0x4D000000.toInt(), PorterDuff.Mode.OVERLAY)
            drawColor(0x66000000.toInt())
        }
    }

    return fastBlur(processedBitmap, 1.0f, 15)
}

/**
 * A fast, lightweight box/stack blur implementation for bitmaps.
 */
private fun fastBlur(sentBitmap: Bitmap, scale: Float, radius: Int): Bitmap {
    var bitmap = sentBitmap
    if (scale != 1.0f) {
        val width = (bitmap.width * scale).toInt()
        val height = (bitmap.height * scale).toInt()
        if (width > 0 && height > 0) {
            bitmap = Bitmap.createScaledBitmap(bitmap, width, height, true)
        }
    }
    if (radius < 1) {
        return bitmap
    }
    
    val w = bitmap.width
    val h = bitmap.height
    
    val pix = IntArray(w * h)
    bitmap.getPixels(pix, 0, w, 0, 0, w, h)
    
    val wm = w - 1
    val hm = h - 1
    val wh = w * h
    val div = radius + radius + 1
    
    val r = IntArray(wh)
    val g = IntArray(wh)
    val b = IntArray(wh)
    var rsum: Int; var gsum: Int; var bsum: Int; var x: Int; var y: Int; var i: Int; var p: Int; var yp: Int; var yi: Int; var yw: Int
    val vmin = IntArray(maxOf(w, h))
    
    var divsum = (div + 1) shr 1
    divsum *= divsum
    val dv = IntArray(256 * divsum)
    for (i in 0 until 256 * divsum) {
        dv[i] = (i / divsum)
    }
    
    yw = 0
    yi = 0
    
    val stack = Array(div) { IntArray(3) }
    var stackpointer: Int
    var stackstart: Int
    var sir: IntArray
    var rbs: Int
    val r1 = radius + 1
    var routsum: Int; var goutsum: Int; var boutsum: Int; var rinsum: Int; var ginsum: Int; var binsum: Int
    
    for (y in 0 until h) {
        rinsum = 0; ginsum = 0; binsum = 0; routsum = 0; goutsum = 0; boutsum = 0; rsum = 0; gsum = 0; bsum = 0
        for (i in -radius..radius) {
            p = pix[yi + minOf(wm, maxOf(i, 0))]
            sir = stack[i + radius]
            sir[0] = (p and 0xff0000) shr 16
            sir[1] = (p and 0x00ff00) shr 8
            sir[2] = (p and 0x0000ff)
            rbs = r1 - kotlin.math.abs(i)
            rsum += sir[0] * rbs
            gsum += sir[1] * rbs
            bsum += sir[2] * rbs
            if (i > 0) {
                rinsum += sir[0]
                ginsum += sir[1]
                binsum += sir[2]
            } else {
                routsum += sir[0]
                goutsum += sir[1]
                boutsum += sir[2]
            }
        }
        stackpointer = radius
        
        for (x in 0 until w) {
            r[yi] = dv[rsum]
            g[yi] = dv[gsum]
            b[yi] = dv[bsum]
            
            rsum -= routsum
            gsum -= goutsum
            bsum -= boutsum
            
            stackstart = stackpointer - radius + div
            sir = stack[stackstart % div]
            
            routsum -= sir[0]
            goutsum -= sir[1]
            boutsum -= sir[2]
            
            if (y == 0) {
                vmin[x] = minOf(x + radius + 1, wm)
            }
            p = pix[yw + vmin[x]]
            
            sir[0] = (p and 0xff0000) shr 16
            sir[1] = (p and 0x00ff00) shr 8
            sir[2] = (p and 0x0000ff)
            
            rinsum += sir[0]
            ginsum += sir[1]
            binsum += sir[2]
            
            rsum += rinsum
            gsum += ginsum
            bsum += binsum
            
            stackpointer = (stackpointer + 1) % div
            sir = stack[stackpointer % div]
            
            routsum += sir[0]
            goutsum += sir[1]
            boutsum += sir[2]
            
            rinsum -= sir[0]
            ginsum -= sir[1]
            binsum -= sir[2]
            
            yi++
        }
        yw += w
    }
    
    for (x in 0 until w) {
        rinsum = 0; ginsum = 0; binsum = 0; routsum = 0; goutsum = 0; boutsum = 0; rsum = 0; gsum = 0; bsum = 0
        yp = -radius * w
        for (i in -radius..radius) {
            yi = maxOf(0, yp) + x
            sir = stack[i + radius]
            sir[0] = r[yi]
            sir[1] = g[yi]
            sir[2] = b[yi]
            
            rbs = r1 - kotlin.math.abs(i)
            
            rsum += r[yi] * rbs
            gsum += g[yi] * rbs
            bsum += b[yi] * rbs
            
            if (i > 0) {
                rinsum += sir[0]
                ginsum += sir[1]
                binsum += sir[2]
            } else {
                routsum += sir[0]
                goutsum += sir[1]
                boutsum += sir[2]
            }
            if (i < hm) {
                yp += w
            }
        }
        yi = x
        stackpointer = radius
        for (y in 0 until h) {
            pix[yi] = (0xff000000.toInt() or (dv[rsum] shl 16) or (dv[gsum] shl 8) or dv[bsum])
            
            rsum -= routsum
            gsum -= goutsum
            bsum -= boutsum
            
            stackstart = stackpointer - radius + div
            sir = stack[stackstart % div]
            
            routsum -= sir[0]
            goutsum -= sir[1]
            boutsum -= sir[2]
            
            if (x == 0) {
                vmin[y] = minOf(y + r1, hm) * w
            }
            p = x + vmin[y]
            
            sir[0] = r[p]
            sir[1] = g[p]
            sir[2] = b[p]
            
            rinsum += sir[0]
            ginsum += sir[1]
            binsum += sir[2]
            
            rsum += rinsum
            gsum += ginsum
            bsum += binsum
            
            stackpointer = (stackpointer + 1) % div
            sir = stack[stackpointer % div]
            
            routsum += sir[0]
            goutsum += sir[1]
            boutsum += sir[2]
            
            rinsum -= sir[0]
            ginsum -= sir[1]
            binsum -= sir[2]
            
            yi += w
        }
    }
    bitmap.setPixels(pix, 0, w, 0, 0, w, h)
    return bitmap
}
