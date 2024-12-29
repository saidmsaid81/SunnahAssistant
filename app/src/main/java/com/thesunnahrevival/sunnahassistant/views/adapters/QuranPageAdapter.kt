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
                }

                quranPageView.setOnClickListener {
                    listener.onQuranPageClick(it)
                }

                quranPageView.setOnLongClickListener { view ->
                    listener.onQuranPageLongClick(view, highlightOverlay)
                    true
                }

                quranPageView.setOnTouchListener { v, event ->
                    listener.setLastTouchCoordinates(event.rawX, event.rawY)
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
}

data class QuranPage(
    val number: Int,
    val ayahs: List<Ayah>
)

data class Surah(
    val number: Int,
    val name: String
)

data class Ayah(
    val id: Int,
    val number: Int,
    val surah: Surah? = null,
    val lines: List<Line> = listOf(),
    val arabicText: String = "",
    val ayahTranslations: List<AyahTranslation> = listOf()
)

data class Line(
    val number: Int,
    val minX: Float,
    val minY: Float,
    val maxX: Float,
    val maxY: Float
)

data class AyahTranslation(
    val id: Int,
    val source: String,
    val text: String
)


