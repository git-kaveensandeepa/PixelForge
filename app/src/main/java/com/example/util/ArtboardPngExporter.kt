package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import com.example.model.Artboard
import com.example.model.DesignElement
import com.example.model.ElementType
import com.example.model.TextAlignOption
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

object ArtboardPngExporter {

    suspend fun exportArtboardToPng(context: Context, artboard: Artboard): File = withContext(Dispatchers.IO) {
        // Render at 2x scale for crisp mobile resolution
        val scale = 2.0f
        val bitmapWidth = (artboard.width * scale).toInt().coerceAtLeast(100)
        val bitmapHeight = (artboard.height * scale).toInt().coerceAtLeast(100)

        val bitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Draw Artboard background
        val bgPaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.FILL
            color = parseColorSafe(artboard.backgroundColorHex, Color.WHITE)
        }
        canvas.drawRect(0f, 0f, bitmapWidth.toFloat(), bitmapHeight.toFloat(), bgPaint)

        // Render visible elements in layer order (index 0 is bottom, last is top)
        for (element in artboard.elements) {
            if (!element.isVisible) continue
            drawElementOnCanvas(canvas, element, scale)
        }

        // Save to cache exports folder
        val exportDir = File(context.cacheDir, "exports").apply { if (!exists()) mkdirs() }
        val sanitizedName = artboard.name.replace("[^a-zA-Z0-9_-]".toRegex(), "_")
        val outputFile = File(exportDir, "pixelforge_${sanitizedName}_${System.currentTimeMillis()}.png")

        FileOutputStream(outputFile).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        bitmap.recycle()

        outputFile
    }

    private fun drawElementOnCanvas(canvas: Canvas, element: DesignElement, scale: Float) {
        val left = element.x * scale
        val top = element.y * scale
        val right = (element.x + element.width) * scale
        val bottom = (element.y + element.height) * scale
        val rect = RectF(left, top, right, bottom)
        val cx = (left + right) / 2f
        val cy = (top + bottom) / 2f

        val hasRotation = element.rotation != 0f
        if (hasRotation) {
            canvas.save()
            canvas.rotate(element.rotation, cx, cy)
        }

        val fillPaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.FILL
            color = parseColorSafe(element.fillColorHex, Color.GRAY)
            alpha = (element.opacity.coerceIn(0f, 1f) * 255).toInt()
        }

        val strokePaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
            color = parseColorSafe(element.strokeColorHex, Color.TRANSPARENT)
            strokeWidth = element.strokeWidth * scale
        }

        val cornerRadiusPx = element.cornerRadius * scale

        if (element.hasGradient) {
            val gradStart = parseColorSafe(element.gradientStartHex ?: element.fillColorHex, Color.GRAY)
            val gradEnd = parseColorSafe(element.gradientEndHex ?: element.fillColorHex, Color.GRAY)
            fillPaint.shader = android.graphics.LinearGradient(
                left, top, right, bottom,
                gradStart, gradEnd,
                android.graphics.Shader.TileMode.CLAMP
            )
        }

        when (element.type) {
            ElementType.RECTANGLE -> {
                if (cornerRadiusPx > 0f) {
                    canvas.drawRoundRect(rect, cornerRadiusPx, cornerRadiusPx, fillPaint)
                    if (element.strokeWidth > 0f) {
                        canvas.drawRoundRect(rect, cornerRadiusPx, cornerRadiusPx, strokePaint)
                    }
                } else {
                    canvas.drawRect(rect, fillPaint)
                    if (element.strokeWidth > 0f) {
                        canvas.drawRect(rect, strokePaint)
                    }
                }
            }
            ElementType.CIRCLE -> {
                val rx = (right - left) / 2f
                val ry = (bottom - top) / 2f
                val r = minOf(rx, ry)
                canvas.drawCircle(cx, cy, r, fillPaint)
                if (element.strokeWidth > 0f) {
                    canvas.drawCircle(cx, cy, r, strokePaint)
                }
            }
            ElementType.LINE -> {
                val linePaint = Paint().apply {
                    isAntiAlias = true
                    style = Paint.Style.STROKE
                    color = parseColorSafe(if (element.strokeWidth > 0f) element.strokeColorHex else element.fillColorHex, Color.GRAY)
                    strokeWidth = (if (element.strokeWidth > 0f) element.strokeWidth else element.height.coerceAtLeast(1f)) * scale
                }
                canvas.drawLine(left, cy, right, cy, linePaint)
            }
            ElementType.BUTTON -> {
                canvas.drawRoundRect(rect, cornerRadiusPx, cornerRadiusPx, fillPaint)
                if (element.strokeWidth > 0f) {
                    canvas.drawRoundRect(rect, cornerRadiusPx, cornerRadiusPx, strokePaint)
                }
                drawTextInRect(canvas, element.text, rect, element, scale, centerVertical = true)
            }
            ElementType.TEXT -> {
                if (element.strokeWidth > 0f) {
                    canvas.drawRect(rect, strokePaint)
                }
                drawTextInRect(canvas, element.text, rect, element, scale, centerVertical = false)
            }
            ElementType.IMAGE_PLACEHOLDER -> {
                canvas.drawRoundRect(rect, cornerRadiusPx, cornerRadiusPx, fillPaint)
                if (element.strokeWidth > 0f) {
                    canvas.drawRoundRect(rect, cornerRadiusPx, cornerRadiusPx, strokePaint)
                }
                drawTextInRect(canvas, "🖼 ${element.name}", rect, element.copy(fontSize = 12f), scale, centerVertical = true)
            }
            ElementType.ICON -> {
                if (element.fillColorHex != "#00000000" && !element.fillColorHex.uppercase().startsWith("#00")) {
                    canvas.drawRoundRect(rect, cornerRadiusPx, cornerRadiusPx, fillPaint)
                }
                if (element.strokeWidth > 0f) {
                    canvas.drawRoundRect(rect, cornerRadiusPx, cornerRadiusPx, strokePaint)
                }
                drawTextInRect(canvas, "✦ ${element.iconName}", rect, element.copy(fontSize = 12f), scale, centerVertical = true)
            }
            ElementType.TEXT_FIELD -> {
                canvas.drawRoundRect(rect, cornerRadiusPx, cornerRadiusPx, fillPaint)
                val fieldStrokePaint = if (element.strokeWidth > 0f) strokePaint else Paint().apply {
                    isAntiAlias = true
                    style = Paint.Style.STROKE
                    color = parseColorSafe(element.textColorHex, Color.GRAY)
                    strokeWidth = 1.5f * scale
                    alpha = 100
                }
                canvas.drawRoundRect(rect, cornerRadiusPx, cornerRadiusPx, fieldStrokePaint)
                val displayStr = if (element.text.isNotEmpty()) element.text else element.placeholder
                drawTextInRect(canvas, displayStr, rect, element.copy(fontSize = element.fontSize), scale, centerVertical = true)
            }
            ElementType.SWITCH -> {
                val trackRadius = (rect.height()) / 2f
                val trackPaint = Paint().apply {
                    isAntiAlias = true
                    style = Paint.Style.FILL
                    color = if (element.isChecked) parseColorSafe(element.fillColorHex, Color.BLUE) else Color.DKGRAY
                }
                canvas.drawRoundRect(rect, trackRadius, trackRadius, trackPaint)
                val thumbR = (rect.height() - (6f * scale)) / 2f
                val thumbCx = if (element.isChecked) right - thumbR - (3f * scale) else left + thumbR + (3f * scale)
                val thumbPaint = Paint().apply {
                    isAntiAlias = true
                    style = Paint.Style.FILL
                    color = if (element.isChecked) parseColorSafe(element.textColorHex, Color.WHITE) else Color.LTGRAY
                }
                canvas.drawCircle(thumbCx, cy, thumbR, thumbPaint)
            }
            ElementType.CHECKBOX -> {
                val boxSize = 20f * scale
                val boxRect = RectF(left, cy - (boxSize / 2f), left + boxSize, cy + (boxSize / 2f))
                val boxRadius = 4f * scale
                if (element.isChecked) {
                    canvas.drawRoundRect(boxRect, boxRadius, boxRadius, fillPaint)
                    val checkPaint = Paint().apply {
                        isAntiAlias = true
                        style = Paint.Style.STROKE
                        color = parseColorSafe(element.textColorHex, Color.WHITE)
                        strokeWidth = 2f * scale
                    }
                    canvas.drawText("✓", boxRect.centerX() - (4f * scale), boxRect.centerY() + (4f * scale), Paint().apply {
                        color = parseColorSafe(element.textColorHex, Color.WHITE)
                        textSize = 14f * scale
                        isAntiAlias = true
                    })
                } else {
                    val boxBorder = Paint().apply {
                        isAntiAlias = true
                        style = Paint.Style.STROKE
                        color = parseColorSafe(element.textColorHex, Color.GRAY)
                        strokeWidth = 1.5f * scale
                    }
                    canvas.drawRoundRect(boxRect, boxRadius, boxRadius, boxBorder)
                }
                if (element.text.isNotEmpty()) {
                    val labelRect = RectF(left + boxSize + (8f * scale), top, right, bottom)
                    drawTextInRect(canvas, element.text, labelRect, element, scale, centerVertical = true)
                }
            }
        }

        if (hasRotation) {
            canvas.restore()
        }
    }

    private fun drawTextInRect(
        canvas: Canvas,
        text: String,
        rect: RectF,
        element: DesignElement,
        scale: Float,
        centerVertical: Boolean
    ) {
        val textPaint = Paint().apply {
            isAntiAlias = true
            color = parseColorSafe(element.textColorHex, Color.BLACK)
            textSize = element.fontSize * scale
            typeface = when {
                element.isBold && element.isItalic -> Typeface.create(Typeface.DEFAULT, Typeface.BOLD_ITALIC)
                element.isBold -> Typeface.DEFAULT_BOLD
                element.isItalic -> Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
                else -> Typeface.DEFAULT
            }
            textAlign = when (element.textAlign) {
                TextAlignOption.LEFT -> Paint.Align.LEFT
                TextAlignOption.CENTER -> Paint.Align.CENTER
                TextAlignOption.RIGHT -> Paint.Align.RIGHT
            }
        }

        val fm = textPaint.fontMetrics
        val textHeight = fm.descent - fm.ascent

        val x = when (element.textAlign) {
            TextAlignOption.LEFT -> rect.left + (8f * scale)
            TextAlignOption.CENTER -> rect.centerX()
            TextAlignOption.RIGHT -> rect.right - (8f * scale)
        }

        val y = if (centerVertical) {
            rect.centerY() - (fm.ascent + fm.descent) / 2f
        } else {
            rect.top - fm.ascent + (4f * scale)
        }

        canvas.drawText(text, x, y, textPaint)
    }

    fun parseColorSafe(hex: String, defaultColor: Int): Int {
        return try {
            val cleanHex = if (hex.startsWith("#")) hex else "#$hex"
            Color.parseColor(cleanHex)
        } catch (_: Exception) {
            defaultColor
        }
    }
}
