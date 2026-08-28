package com.codetrio.overdrive.ui.statistics.dna

import android.content.Context
import android.content.Intent
import android.graphics.*
import androidx.core.content.FileProvider
import com.codetrio.overdrive.data.dna.MusicDnaProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

object MusicDnaShareManager {

    suspend fun generateAndShareCard(context: Context, profile: MusicDnaProfile) = withContext(Dispatchers.IO) {
        try {
            val width = 1080
            val height = 1920
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)

            // Deep M3 Surface Background
            val bgPaint = Paint().apply {
                isAntiAlias = true
                shader = LinearGradient(
                    0f, 0f, width.toFloat(), height.toFloat(),
                    intArrayOf(0xFF13151D.toInt(), 0xFF1B1F2A.toInt(), 0xFF12141A.toInt()),
                    floatArrayOf(0f, 0.5f, 1f),
                    Shader.TileMode.CLAMP
                )
            }
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

            // Subtle Tonal Ambient Glow
            val glowPaint = Paint().apply {
                isAntiAlias = true
                shader = RadialGradient(
                    width * 0.5f, 400f, 600f,
                    intArrayOf(0x33A8C7FA.toInt(), 0x00000000),
                    floatArrayOf(0f, 1f),
                    Shader.TileMode.CLAMP
                )
            }
            canvas.drawCircle(width * 0.5f, 400f, 600f, glowPaint)

            val primaryAccent = 0xFFA8C7FA.toInt()
            val textPrimary = 0xFFF1F0F4.toInt()
            val textSecondary = 0xFFC4C7D0.toInt()
            val surfaceCardBg = 0xFF1F232E.toInt()
            val surfaceCardBorder = 0xFF313746.toInt()

            var currentY = 90f

            // 1. Header (OverDrive Branding & Tag)
            val logoPaint = Paint().apply {
                isAntiAlias = true
                color = textPrimary
                textSize = 38f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            canvas.drawText("OverDrive", 60f, currentY + 30f, logoPaint)

            val tagBgPaint = Paint().apply {
                isAntiAlias = true
                color = 0xFF2D3240.toInt()
            }
            val tagRect = RectF(width - 320f, currentY - 5f, width - 60f, currentY + 45f)
            canvas.drawRoundRect(tagRect, 25f, 25f, tagBgPaint)

            val tagTextPaint = Paint().apply {
                isAntiAlias = true
                color = primaryAccent
                textSize = 22f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("MUSIC DNA", tagRect.centerX(), currentY + 28f, tagTextPaint)

            currentY += 100f

            // 2. Archetype Hero Card
            val heroCardHeight = 440f
            val heroRect = RectF(60f, currentY, width - 60f, currentY + heroCardHeight)
            val heroCardPaint = Paint().apply {
                isAntiAlias = true
                color = surfaceCardBg
            }
            val heroStrokePaint = Paint().apply {
                isAntiAlias = true
                style = Paint.Style.STROKE
                strokeWidth = 2f
                color = surfaceCardBorder
            }
            canvas.drawRoundRect(heroRect, 36f, 36f, heroCardPaint)
            canvas.drawRoundRect(heroRect, 36f, 36f, heroStrokePaint)

            // Archetype Titles
            val archeTitlePaint = Paint().apply {
                isAntiAlias = true
                color = textPrimary
                textSize = 52f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            canvas.drawText(profile.archetype.titleJa, 100f, currentY + 75f, archeTitlePaint)

            val archeSubTitlePaint = Paint().apply {
                isAntiAlias = true
                color = primaryAccent
                textSize = 28f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            canvas.drawText(profile.archetype.titleEn, 100f, currentY + 115f, archeSubTitlePaint)

            val descPaint = Paint().apply {
                isAntiAlias = true
                color = textSecondary
                textSize = 26f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            }
            val safeDesc = if (profile.archetype.subtitleJa.length > 34) {
                profile.archetype.subtitleJa.take(32) + "..."
            } else {
                profile.archetype.subtitleJa
            }
            canvas.drawText(safeDesc, 100f, currentY + 175f, descPaint)

            // 3 Stat Sub-cards
            val statY = currentY + 230f
            val statW = (width - 200f - 32f) / 3f
            val statH = 150f

            drawStatBox(canvas, 100f, statY, statW, statH, "${profile.totalMinutes}分", "総再生時間", primaryAccent, textPrimary, textSecondary)
            drawStatBox(canvas, 100f + statW + 16f, statY, statW, statH, "${profile.totalPlays}回", "再生回数", primaryAccent, textPrimary, textSecondary)
            drawStatBox(canvas, 100f + (statW + 16f) * 2f, statY, statW, statH, "${profile.uniqueArtistsCount}組", "アーティスト", primaryAccent, textPrimary, textSecondary)

            currentY += heroCardHeight + 36f

            // 3. DNA Parameters Card
            val dnaCardHeight = 310f
            val dnaRect = RectF(60f, currentY, width - 60f, currentY + dnaCardHeight)
            canvas.drawRoundRect(dnaRect, 32f, 32f, heroCardPaint)
            canvas.drawRoundRect(dnaRect, 32f, 32f, heroStrokePaint)

            val sectionTitlePaint = Paint().apply {
                isAntiAlias = true
                color = textPrimary
                textSize = 32f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            canvas.drawText("リスニング特性パラメータ", 100f, currentY + 55f, sectionTitlePaint)

            val barW = width - 200f
            drawDnaBar(canvas, 100f, currentY + 80f, "探索度 (Discovery)", profile.discoveryScore, primaryAccent, barW, textPrimary, textSecondary)
            drawDnaBar(canvas, 100f, currentY + 135f, "夜行性 (Night Owl)", profile.nightOwlScore, 0xFFD0BCFF.toInt(), barW, textPrimary, textSecondary)
            drawDnaBar(canvas, 100f, currentY + 190f, "熱中度 (Devotion)", profile.devotionScore, 0xFFF2B8B5.toInt(), barW, textPrimary, textSecondary)
            drawDnaBar(canvas, 100f, currentY + 245f, "スタミナ (Volume)", profile.staminaScore, primaryAccent, barW, textPrimary, textSecondary)

            currentY += dnaCardHeight + 36f

            // 4. 24h Rhythm & Top Tracks Row / Split
            val rhythmCardHeight = 270f
            val rhythmRect = RectF(60f, currentY, width - 60f, currentY + rhythmCardHeight)
            canvas.drawRoundRect(rhythmRect, 32f, 32f, heroCardPaint)
            canvas.drawRoundRect(rhythmRect, 32f, 32f, heroStrokePaint)

            canvas.drawText("24時間アクティビティ分布", 100f, currentY + 55f, sectionTitlePaint)

            val peakTagPaint = Paint().apply {
                isAntiAlias = true
                color = primaryAccent
                textSize = 24f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.RIGHT
            }
            canvas.drawText("ピーク: ${profile.peakHour}時台", width - 100f, currentY + 55f, peakTagPaint)

            draw24hBarChart(canvas, 100f, currentY + 85f, profile.hourlyDistribution, profile.peakHour, primaryAccent, barW, 140f, textSecondary)

            currentY += rhythmCardHeight + 36f

            // 5. Top Tracks Card
            if (profile.topSongs.isNotEmpty()) {
                val tracksCardHeight = 290f
                val tracksRect = RectF(60f, currentY, width - 60f, currentY + tracksCardHeight)
                canvas.drawRoundRect(tracksRect, 32f, 32f, heroCardPaint)
                canvas.drawRoundRect(tracksRect, 32f, 32f, heroStrokePaint)

                canvas.drawText("トップ再生トラック", 100f, currentY + 55f, sectionTitlePaint)

                var rowY = currentY + 100f
                profile.topSongs.take(3).forEachIndexed { index, song ->
                    drawTrackRow(canvas, 100f, rowY, index + 1, song.title, song.artist, song.count, barW, textPrimary, textSecondary, primaryAccent)
                    rowY += 56f
                }
            }

            // 6. Footer
            val footerPaint = Paint().apply {
                isAntiAlias = true
                color = textSecondary
                textSize = 22f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("Generated by OverDrive • ${profile.formattedRangeLabel}", width / 2f, height - 50f, footerPaint)

            // Save and Share
            val cachePath = File(context.cacheDir, "images")
            cachePath.mkdirs()
            val file = File(cachePath, "overdrive_dna_${profile.timeRange.name.lowercase()}.png")
            val stream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.close()

            val contentUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                putExtra(
                    Intent.EXTRA_TEXT,
                    "🎵 私のリスニングDNAは【${profile.archetype.titleJa}】でした！\n総再生時間: ${profile.totalMinutes}分 • ピーク: ${profile.peakHour}時台\n#OverDrive #MusicDNA #Wrapped"
                )
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
            }

            val chooser = Intent.createChooser(shareIntent, "リスニングDNAをシェア").apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(chooser)

        } catch (e: Exception) {
            android.util.Log.e("MusicDnaShare", "Failed to share card: ${e.message}", e)
        }
    }

    private fun drawStatBox(canvas: Canvas, x: Float, y: Float, w: Float, h: Float, value: String, label: String, accent: Int, textPrimary: Int, textSecondary: Int) {
        val boxPaint = Paint().apply {
            isAntiAlias = true
            color = 0xFF2A2F3D.toInt()
        }
        canvas.drawRoundRect(RectF(x, y, x + w, y + h), 20f, 20f, boxPaint)

        val valPaint = Paint().apply {
            isAntiAlias = true
            color = textPrimary
            textSize = 34f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(value, x + w / 2f, y + 62f, valPaint)

        val lblPaint = Paint().apply {
            isAntiAlias = true
            color = textSecondary
            textSize = 22f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(label, x + w / 2f, y + 105f, lblPaint)
    }

    private fun drawDnaBar(canvas: Canvas, x: Float, y: Float, label: String, score: Int, metricColor: Int, w: Float, textPrimary: Int, textSecondary: Int) {
        val lblPaint = Paint().apply {
            isAntiAlias = true
            color = textSecondary
            textSize = 24f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }
        canvas.drawText(label, x, y + 20f, lblPaint)

        val scorePaint = Paint().apply {
            isAntiAlias = true
            color = metricColor
            textSize = 24f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.RIGHT
        }
        canvas.drawText("$score%", x + w, y + 20f, scorePaint)

        val barY = y + 28f
        val barBgPaint = Paint().apply {
            color = 0xFF2A2F3D.toInt()
            isAntiAlias = true
        }
        canvas.drawRoundRect(RectF(x, barY, x + w, barY + 10f), 5f, 5f, barBgPaint)

        val fillW = (w * (score / 100f)).coerceAtLeast(10f)
        val barFillPaint = Paint().apply {
            isAntiAlias = true
            color = metricColor
        }
        canvas.drawRoundRect(RectF(x, barY, x + fillW, barY + 10f), 5f, 5f, barFillPaint)
    }

    private fun drawTrackRow(canvas: Canvas, x: Float, y: Float, rank: Int, title: String, artist: String, plays: Int, w: Float, textPrimary: Int, textSecondary: Int, accent: Int) {
        val rankPaint = Paint().apply {
            isAntiAlias = true
            color = if (rank == 1) accent else textSecondary
            textSize = 26f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText("#$rank", x, y + 22f, rankPaint)

        val titlePaint = Paint().apply {
            isAntiAlias = true
            color = textPrimary
            textSize = 26f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val safeTitle = if (title.length > 20) title.take(18) + "..." else title
        canvas.drawText(safeTitle, x + 55f, y + 22f, titlePaint)

        val artistPaint = Paint().apply {
            isAntiAlias = true
            color = textSecondary
            textSize = 22f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }
        val safeArtist = if (artist.length > 18) artist.take(16) + "..." else artist
        canvas.drawText(" • $safeArtist", x + 55f + titlePaint.measureText(safeTitle), y + 22f, artistPaint)

        val playsPaint = Paint().apply {
            isAntiAlias = true
            color = textSecondary
            textSize = 22f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textAlign = Paint.Align.RIGHT
        }
        canvas.drawText("${plays}回", x + w, y + 22f, playsPaint)
    }

    private fun draw24hBarChart(canvas: Canvas, x: Float, y: Float, distribution: Map<Int, Int>, peakHour: Int, highlightColor: Int, w: Float, h: Float, textSecondary: Int) {
        val maxVal = (distribution.values.maxOrNull() ?: 1).coerceAtLeast(1)
        val barWidth = (w / 24f) - 4f

        for (hour in 0..23) {
            val count = distribution[hour] ?: 0
            val barH = ((count.toFloat() / maxVal) * (h - 30f)).coerceAtLeast(6f)
            val barX = x + hour * (barWidth + 4f)
            val barY = y + h - barH - 20f

            val isPeak = hour == peakHour
            val paint = Paint().apply {
                isAntiAlias = true
                color = if (isPeak) highlightColor else 0xFF2A2F3D.toInt()
            }
            canvas.drawRoundRect(RectF(barX, barY, barX + barWidth, barY + barH), 4f, 4f, paint)

            if (hour % 6 == 0 || hour == 23) {
                val txtPaint = Paint().apply {
                    isAntiAlias = true
                    color = textSecondary
                    textSize = 18f
                    textAlign = Paint.Align.CENTER
                }
                canvas.drawText("${hour}:00", barX + barWidth / 2f, y + h, txtPaint)
            }
        }
    }
}
