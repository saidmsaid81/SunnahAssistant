package com.thesunnahrevival.sunnahassistant.views.adapters

import android.content.res.Configuration
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.thesunnahrevival.sunnahassistant.R
import com.thesunnahrevival.sunnahassistant.views.customviews.HighlightOverlayView
import com.thesunnahrevival.sunnahassistant.views.listeners.QuranPageInteractionListener
import com.thesunnahrevival.sunnahassistant.views.utilities.showQuranPageNextAction

class QuranPageAdapter(
    private val activity: FragmentActivity,
    var pageNumbers: List<Int>,
    private val listener: QuranPageInteractionListener
) :
    RecyclerView.Adapter<QuranPageAdapter.ViewHolder>() {

    companion object {
        private const val TIMEOUT_DELAY_MS = 5000L // 5 seconds
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.quran_page_view, parent, false)
        return ViewHolder(activity, view)
    }

    override fun getItemCount(): Int = pageNumbers.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(pageNumbers[position])
    }

    override fun onViewRecycled(holder: ViewHolder) {
        super.onViewRecycled(holder)
        holder.cleanup()
    }

    inner class ViewHolder(private val activity: FragmentActivity, private val view: View) : RecyclerView.ViewHolder(view) {
        // Timeout mechanism fields
        private var timeoutHandler: Handler? = null
        private var timeoutRunnable: Runnable? = null
        private var hasTimedOut: Boolean = false
        private var isLoading: Boolean = false
        private var currentPageNumber: Int = -1
        
        // UI components for timeout message
        private val timeoutMessageContainer: LinearLayout by lazy { 
            view.findViewById(R.id.timeout_message_container) 
        }
        private val downloadAllButton: Button by lazy { 
            view.findViewById(R.id.download_all_button) 
        }
        
        fun bind(pageNumber: Int) {
            cleanupTimeout()

            hasTimedOut = false
            isLoading = false
            currentPageNumber = pageNumber

            showQuranPageNextAction(activity, view, pageNumber)

            val progressBar = view.findViewById<ProgressBar>(R.id.progress_bar)
            progressBar.visibility = View.GONE

            val quranPageView = view.findViewById<ImageView>(R.id.quran_page)
            quranPageView.setImageDrawable(null)

            val pageNumberView = view.findViewById<TextView>(R.id.page_number)
            pageNumberView.text = null

            val highlightOverlay = view.findViewById<HighlightOverlayView>(R.id.highlight_overlay)
            highlightOverlay.tag = "overlay_$pageNumber"
            
            // Hide timeout message initially
            hideTimeoutMessage()

            try {
                val file = java.io.File(view.context.filesDir, "quran_pages/$pageNumber.png")

                if (!file.exists()) {
                    // Page not found - show progress bar and start timeout
                    progressBar.visibility = View.VISIBLE
                    startLoadingWithTimeout(pageNumber)
                    listener.onPageNotFound(pageNumber)
                    return
                }

                val inputStream = java.io.FileInputStream(file)

                val options = BitmapFactory.Options().apply {
                    inJustDecodeBounds = true
                }
                BitmapFactory.decodeStream(inputStream, null, options)
                inputStream.close()

                val actualWidth = options.outWidth
                val actualHeight = options.outHeight

                val imageInputStream = java.io.FileInputStream(file)
                val drawable = Drawable.createFromStream(imageInputStream, null)
                imageInputStream.close()

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

                pageNumberView.text = pageNumber.toString()
                
                onPageLoadedSuccessfully()
                listener.onPageLoaded(pageNumber)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }


        private fun startLoadingWithTimeout(pageNumber: Int) {
            // Prevent multiple timeout triggers for the same page
            if (isLoading && currentPageNumber == pageNumber) {
                return
            }
            
            isLoading = true
            hasTimedOut = false
            
            if (timeoutHandler == null) {
                timeoutHandler = Handler(Looper.getMainLooper())
            }
            
            timeoutRunnable = Runnable {
                if (isLoading && currentPageNumber == pageNumber && !hasTimedOut) {
                    hasTimedOut = true
                    showTimeoutMessage()
                }
            }
            
            timeoutHandler?.postDelayed(timeoutRunnable!!, TIMEOUT_DELAY_MS)
        }

        private fun showTimeoutMessage() {
            timeoutMessageContainer.visibility = View.VISIBLE
            
            downloadAllButton.setOnClickListener {
                listener.onDownloadAllPagesRequested()
            }
        }

        private fun hideTimeoutMessage() {
            timeoutMessageContainer.visibility = View.GONE
        }

        private fun onPageLoadedSuccessfully() {
            isLoading = false
            cleanupTimeout()
            hideTimeoutMessage()
        }

        private fun cleanupTimeout() {
            timeoutRunnable?.let { runnable ->
                timeoutHandler?.removeCallbacks(runnable)
            }
            timeoutRunnable = null
        }
        

        fun cleanup() {
            cleanupTimeout()
            isLoading = false
            hasTimedOut = false
            currentPageNumber = -1
            hideTimeoutMessage()
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