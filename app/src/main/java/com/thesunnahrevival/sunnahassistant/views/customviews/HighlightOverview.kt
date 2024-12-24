package com.thesunnahrevival.sunnahassistant.views.customviews

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

class HighlightOverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val highlightPaint = Paint().apply {
        color = Color.YELLOW
        alpha = 80
        style = Paint.Style.FILL
    }

    private val highlights = mutableListOf<List<RectF>>()
    private var imageWidth: Int = 0
    private var imageHeight: Int = 0
    private var viewWidth: Int = 0
    private var viewHeight: Int = 0
    private var scaleX: Float = 1f
    private var scaleY: Float = 1f
    private var offsetX: Float = 0f
    private var offsetY: Float = 0f

    fun setImageDimensions(
        originalWidth: Int,
        originalHeight: Int,
        displayedWidth: Int,
        displayedHeight: Int
    ) {
        imageWidth = originalWidth
        imageHeight = originalHeight
        viewWidth = displayedWidth
        viewHeight = displayedHeight

        scaleX = viewWidth.toFloat() / imageWidth.toFloat()
        scaleY = scaleX

        val scaledImageHeight = imageHeight * scaleY

        offsetY = (viewHeight - scaledImageHeight) / 2f
        offsetX = 0f

        invalidate()
    }

    fun setHighlightCoordinates(multiLineCoordinates: List<Coordinates>) {
        highlights.clear()

        val connectedRects = mutableListOf<RectF>()

        multiLineCoordinates.forEachIndexed { index, coord ->
            val scaledRect = RectF(
                coord.minX * scaleX + offsetX,
                coord.minY * scaleY + offsetY,
                coord.maxX * scaleX + offsetX,
                coord.maxY * scaleY + offsetY
            )

            if (index > 0) {
                val previousRect = connectedRects.last()
                val gap = scaledRect.top - previousRect.bottom
                if (gap < 5f) {
                    previousRect.bottom = (previousRect.bottom + scaledRect.top) / 2
                    scaledRect.top = previousRect.bottom
                }
            }

            connectedRects.add(scaledRect)
        }

        highlights.add(connectedRects)
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        highlights.forEach { connectedRects ->
            connectedRects.forEach { rect ->
                canvas.drawRect(rect, highlightPaint)
            }
        }
    }

    data class Coordinates(
        val minX: Float,
        val minY: Float,
        val maxX: Float,
        val maxY: Float
    )
}