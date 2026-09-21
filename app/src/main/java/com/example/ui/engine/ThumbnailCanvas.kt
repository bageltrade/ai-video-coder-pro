package com.example.ui.engine

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.nativeCanvas
import com.example.data.model.ThumbnailModel
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ThumbnailCanvas(
    thumbnail: ThumbnailModel,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (size.width < 10f || size.height < 10f) return@Canvas

            try {
                val primary = parseColorSafely(thumbnail.primaryColorHex, Color(0xFF00F0FF))
                val secondary = parseColorSafely(thumbnail.secondaryColorHex, Color(0xFFFF007F))

                // 1. Deep cinematic background gradient
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF080B10),
                            Color(0xFF101524),
                            Color(0xFF190C28)
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(size.width, size.height)
                    )
                )

                // 2. Neon glow aura
                val glowRad1 = (size.width * 0.5f).coerceAtLeast(1f)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(primary.copy(alpha = (0.45f * thumbnail.glowIntensity).coerceIn(0f, 1f)), Color.Transparent),
                        center = Offset(size.width * 0.75f, size.height * 0.45f),
                        radius = glowRad1
                    ),
                    center = Offset(size.width * 0.75f, size.height * 0.45f),
                    radius = glowRad1
                )
                val glowRad2 = (size.width * 0.45f).coerceAtLeast(1f)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(secondary.copy(alpha = (0.35f * thumbnail.glowIntensity).coerceIn(0f, 1f)), Color.Transparent),
                        center = Offset(size.width * 0.25f, size.height * 0.65f),
                        radius = glowRad2
                    ),
                    center = Offset(size.width * 0.25f, size.height * 0.65f),
                    radius = glowRad2
                )

                // 3. Focal Art Silhouette
                drawFocalArtwork(thumbnail.focalArt, primary, secondary, size)

                // 4. Subtle Anamorphic Grid Lines
                val gridStep = (size.height / 8f).coerceAtLeast(1f)
                for (i in 1..7) {
                    drawLine(
                        color = primary.copy(alpha = 0.08f),
                        start = Offset(0f, i * gridStep),
                        end = Offset(size.width, i * gridStep),
                        strokeWidth = 1f
                    )
                }

                // 5. Cinematic Vignette & Outer Tech Border
                val vignetteRad = (size.maxDimension * 0.7f).coerceAtLeast(1f)
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f)),
                        center = Offset(size.width * 0.5f, size.height * 0.5f),
                        radius = vignetteRad
                    )
                )

                drawRoundRect(
                    color = primary.copy(alpha = 0.4f),
                    topLeft = Offset(8f, 8f),
                    size = Size((size.width - 16f).coerceAtLeast(1f), (size.height - 16f).coerceAtLeast(1f)),
                    cornerRadius = CornerRadius(16f, 16f),
                    style = Stroke(width = 2f)
                )

                // Draw Corner Tech Accents
                val cornerLen = 28f
                // Top-left
                drawLine(primary, Offset(8f, 8f), Offset(8f + cornerLen, 8f), strokeWidth = 4f)
                drawLine(primary, Offset(8f, 8f), Offset(8f, 8f + cornerLen), strokeWidth = 4f)
                // Top-right
                drawLine(secondary, Offset(size.width - 8f, 8f), Offset(size.width - 8f - cornerLen, 8f), strokeWidth = 4f)
                drawLine(secondary, Offset(size.width - 8f, 8f), Offset(size.width - 8f, 8f + cornerLen), strokeWidth = 4f)

                // 6. Draw Text using native canvas
                drawThumbnailText(thumbnail, primary, secondary, size)
            } catch (e: Throwable) {
                drawRect(Color(0xFF080B10))
            }
        }
    }
}

private fun DrawScope.drawFocalArtwork(
    focalArt: String,
    primary: Color,
    secondary: Color,
    size: Size
) {
    val cx = size.width * 0.72f
    val cy = size.height * 0.5f
    val radius = (size.minDimension * 0.35f).coerceAtLeast(1f)

    when (focalArt.uppercase()) {
        "COSMIC_EYE", "SINGULARITY" -> {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(secondary, primary, Color.Transparent),
                    center = Offset(cx, cy),
                    radius = radius
                ),
                center = Offset(cx, cy),
                radius = radius
            )
            drawCircle(Color.Black, center = Offset(cx, cy), radius = radius * 0.45f)
            drawCircle(primary, center = Offset(cx, cy), radius = radius * 0.48f, style = Stroke(width = 3f))
        }
        "QUANTUM_SPHERE" -> {
            for (i in 0 until 4) {
                val rx = (radius * 0.9f).coerceAtLeast(1f)
                val ry = (radius * 0.35f).coerceAtLeast(1f)
                drawOval(
                    color = if (i % 2 == 0) primary else secondary,
                    topLeft = Offset(cx - rx, cy - ry),
                    size = Size(rx * 2, ry * 2),
                    style = Stroke(width = 3f)
                )
            }
            drawCircle(Color.White, center = Offset(cx, cy), radius = 12f)
        }
        "DEEP_LEVIATHAN" -> {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(primary, Color.Transparent),
                    center = Offset(cx, cy),
                    radius = radius
                ),
                center = Offset(cx, cy),
                radius = radius
            )
            for (w in 1..3) {
                drawCircle(
                    color = primary.copy(alpha = 0.4f),
                    center = Offset(cx, cy),
                    radius = radius * (0.3f * w),
                    style = Stroke(width = 2f)
                )
            }
        }
        else -> {
            val hexPath = Path().apply {
                for (i in 0 until 6) {
                    val angle = (i * 60f - 30f) * (PI / 180f).toFloat()
                    val hx = cx + cos(angle) * radius
                    val hy = cy + sin(angle) * radius
                    if (i == 0) moveTo(hx, hy) else lineTo(hx, hy)
                }
                close()
            }
            drawPath(
                path = hexPath,
                brush = Brush.linearGradient(listOf(primary, secondary)),
                style = Stroke(width = 6f)
            )
            drawCircle(
                color = primary.copy(alpha = 0.25f),
                center = Offset(cx, cy),
                radius = radius * 0.6f
            )
        }
    }
}

private fun DrawScope.drawThumbnailText(
    thumbnail: ThumbnailModel,
    primary: Color,
    secondary: Color,
    size: Size
) {
    try {
        drawIntoCanvas { canvas ->
            val native = canvas.nativeCanvas

            val primaryInt = parseColorIntSafely(thumbnail.primaryColorHex, android.graphics.Color.CYAN)
            val secondaryInt = parseColorIntSafely(thumbnail.secondaryColorHex, android.graphics.Color.MAGENTA)

            // 1. Badge Pill (Top-Left)
            val badgePaint = android.graphics.Paint().apply {
                color = primaryInt
                isAntiAlias = true
                textSize = (size.height * 0.055f).coerceIn(24f, 42f)
                typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
            }

            val badgeText = thumbnail.badge
            val badgeWidth = badgePaint.measureText(badgeText)
            val padX = 24f
            val padY = 14f

            val pillPaint = android.graphics.Paint().apply {
                color = android.graphics.Color.argb(200, 10, 15, 25)
                style = android.graphics.Paint.Style.FILL
                isAntiAlias = true
            }
            val pillStrokePaint = android.graphics.Paint().apply {
                color = primaryInt
                style = android.graphics.Paint.Style.STROKE
                strokeWidth = 3f
                isAntiAlias = true
            }

            val pillRect = android.graphics.RectF(32f, 28f, 32f + badgeWidth + padX * 2, 28f + badgePaint.textSize + padY * 2)
            native.drawRoundRect(pillRect, 12f, 12f, pillPaint)
            native.drawRoundRect(pillRect, 12f, 12f, pillStrokePaint)

            native.drawText(
                badgeText,
                32f + padX,
                28f + padY + badgePaint.textSize * 0.85f,
                badgePaint
            )

            // 2. Main Title (Large, Punchy Display typography with glow drop shadow)
            val titleSize = (size.height * 0.14f).coerceIn(42f, 76f)
            val titleShadowPaint = android.graphics.Paint().apply {
                color = android.graphics.Color.argb(230, 0, 0, 0)
                isAntiAlias = true
                textSize = titleSize
                typeface = android.graphics.Typeface.create(android.graphics.Typeface.SANS_SERIF, android.graphics.Typeface.BOLD)
                setShadowLayer(16f, 4f, 8f, primaryInt)
            }

            val titlePaint = android.graphics.Paint().apply {
                color = android.graphics.Color.WHITE
                isAntiAlias = true
                textSize = titleSize
                typeface = android.graphics.Typeface.create(android.graphics.Typeface.SANS_SERIF, android.graphics.Typeface.BOLD)
            }

            val titleY = size.height * 0.65f
            val displayTitle = if (thumbnail.title.length > 20) thumbnail.title.take(18) + "…" else thumbnail.title

            native.drawText(displayTitle, 36f, titleY, titleShadowPaint)
            native.drawText(displayTitle, 32f, titleY - 4f, titlePaint)

            // 3. Subtitle Hook
            val subPaint = android.graphics.Paint().apply {
                color = secondaryInt
                isAntiAlias = true
                textSize = (size.height * 0.065f).coerceIn(26f, 38f)
                typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
            }
            val subY = titleY + subPaint.textSize * 1.5f
            native.drawText(thumbnail.subtitle, 32f, subY, subPaint)

            // 4. 4K Ultra HD & 60 FPS Badge on bottom-right
            val cornerBadgePaint = android.graphics.Paint().apply {
                color = android.graphics.Color.WHITE
                isAntiAlias = true
                textSize = (size.height * 0.048f).coerceIn(20f, 30f)
                typeface = android.graphics.Typeface.create(android.graphics.Typeface.MONOSPACE, android.graphics.Typeface.BOLD)
            }
            val cornerBadge = "4K • 60 FPS • AI CODER"
            val cornerWidth = cornerBadgePaint.measureText(cornerBadge)
            val cornerRect = android.graphics.RectF(
                size.width - cornerWidth - 56f,
                size.height - 64f,
                size.width - 24f,
                size.height - 24f
            )
            native.drawRoundRect(cornerRect, 8f, 8f, pillPaint)
            native.drawRoundRect(cornerRect, 8f, 8f, pillStrokePaint)
            native.drawText(cornerBadge, size.width - cornerWidth - 40f, size.height - 36f, cornerBadgePaint)
        }
    } catch (e: Throwable) {
        android.util.Log.e("ThumbnailCanvas", "drawThumbnailText error", e)
    }
}

private inline fun DrawScope.drawIntoCanvas(block: (androidx.compose.ui.graphics.Canvas) -> Unit) {
    drawContext.canvas.let(block)
}

private fun parseColorSafely(hex: String, fallback: Color): Color {
    return try {
        val clean = hex.removePrefix("#").trim()
        val colorInt = android.graphics.Color.parseColor("#$clean")
        Color(colorInt)
    } catch (e: Exception) {
        fallback
    }
}

private fun parseColorIntSafely(hex: String, fallbackInt: Int): Int {
    return try {
        val clean = hex.removePrefix("#").trim()
        android.graphics.Color.parseColor("#$clean")
    } catch (e: Exception) {
        fallbackInt
    }
}
