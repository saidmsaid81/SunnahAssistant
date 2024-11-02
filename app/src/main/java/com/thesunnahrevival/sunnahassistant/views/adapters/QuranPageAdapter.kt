package com.thesunnahrevival.sunnahassistant.views.adapters

import android.content.res.Configuration
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AppCompatDelegate
import androidx.recyclerview.widget.RecyclerView
import com.thesunnahrevival.sunnahassistant.R

class QuranPageAdapter(private val pageNumbers: List<Int>) :
    RecyclerView.Adapter<QuranPageAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.quran_page_view, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = pageNumbers.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(pageNumbers[position])
    }

    inner class ViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        fun bind(pageNumber: Int) {
            val quranPage = view.findViewById<ImageView>(R.id.quran_page)
            try {
                val inputStream = view.context.assets.open("$pageNumber.png")
                val drawable = Drawable.createFromStream(inputStream, null)
                quranPage.setImageDrawable(drawable)
                setupDarkMode(quranPage)
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
