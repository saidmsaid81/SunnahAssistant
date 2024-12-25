package com.thesunnahrevival.sunnahassistant.views.adapters

import android.content.res.Configuration
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import androidx.recyclerview.widget.RecyclerView
import com.thesunnahrevival.sunnahassistant.R
import com.thesunnahrevival.sunnahassistant.views.customviews.HighlightOverlayView
import com.thesunnahrevival.sunnahassistant.views.listeners.QuranPageClickListener

class QuranPageAdapter(
    val pageNumbers: List<QuranPage>,
    private val listener: QuranPageClickListener
) :
    RecyclerView.Adapter<QuranPageAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.quran_page_view, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = pageNumbers.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(pageNumbers[position].number)
    }

    inner class ViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        fun bind(pageNumber: Int) {
            val quranPageView = view.findViewById<ImageView>(R.id.quran_page)
            val highlightOverlay = view.findViewById<HighlightOverlayView>(R.id.highlight_overlay)
            highlightOverlay.tag = "overlay_$pageNumber"

            try {
                val inputStream = view.context.assets.open("$pageNumber.png")

                val options = BitmapFactory.Options().apply {
                    inJustDecodeBounds = true
                }
                BitmapFactory.decodeStream(inputStream, null, options)
                inputStream.reset()

                val actualWidth = options.outWidth
                val actualHeight = options.outHeight

                val drawable = Drawable.createFromStream(inputStream, null)
                quranPageView.setImageDrawable(drawable)

                quranPageView.post {
                    val displayedWidth = quranPageView.width
                    val displayedHeight = quranPageView.height

                    highlightOverlay.setImageDimensions(
                        actualWidth,
                        actualHeight,
                        displayedWidth,
                        displayedHeight
                    )

//                    val multiLineCoordinates = listOf(
//                        HighlightOverlayView.Coordinates(
//                            minX = 174f,
//                            minY = 30f,
//                            maxX = 1239f,
//                            maxY = 149f
//                        )
//                    )
//                    highlightOverlay.setHighlightCoordinates(multiLineCoordinates)
                }

                quranPageView.setOnClickListener {
                    listener.onQuranPageClick(it)
                }

                quranPageView.setOnLongClickListener { view ->
                    println("Long clicked")
                    view.parent.requestDisallowInterceptTouchEvent(true)
                    val location = IntArray(2)
                    view.getLocationOnScreen(location)

                    val rawX = lastTouchX - location[0]
                    val rawY = lastTouchY - location[1]

//                    highlightOverlay.clearHighlights()

                    val unscaledX = rawX / highlightOverlay.getScaleX()
                    val unscaledY =
                        (rawY - highlightOverlay.getOffsetY()) / highlightOverlay.getScaleY()

                    listener.onQuranPageLongClick(view, unscaledX, unscaledY)
                    true
                }

                quranPageView.setOnTouchListener { v, event ->
                    lastTouchX = event.rawX
                    lastTouchY = event.rawY
                    false
                }

                setupDarkMode(quranPageView)

                view.findViewById<TextView>(R.id.page_number).text = pageNumber.toString()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        private fun setupDarkMode(imageView: ImageView) {
            val systemTheme =
                imageView.context?.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)
            val appTheme = AppCompatDelegate.getDefaultNightMode()
            if (appTheme == AppCompatDelegate.MODE_NIGHT_YES || systemTheme == Configuration.UI_MODE_NIGHT_YES) {
                val nightModeTextBrightness = 300

                val matrix = floatArrayOf(
                    -1f, 0f, 0f, 0f, nightModeTextBrightness.toFloat(),  // Red
                    0f, -1f, 0f, 0f, nightModeTextBrightness.toFloat(),  // Green
                    0f, 0f, -1f, 0f, nightModeTextBrightness.toFloat(),  // Blue
                    0f, 0f, 0f, 1f, 0f  // Alpha
                )

                val filter = android.graphics.ColorMatrixColorFilter(matrix)
                imageView.colorFilter = filter
            }
        }
    }

    companion object {
        private var lastTouchX: Float = 0f
        private var lastTouchY: Float = 0f
    }
}

data class QuranPage(
    val number: Int,
    val ayahs: List<Ayah>
)

data class Ayah(
    val number: Int,
    val lines: List<Line>
)

data class Line(
    val number: Int,
    val minX: Float,
    val minY: Float,
    val maxX: Float,
    val maxY: Float
)


