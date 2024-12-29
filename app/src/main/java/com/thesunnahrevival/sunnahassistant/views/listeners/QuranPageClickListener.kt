package com.thesunnahrevival.sunnahassistant.views.listeners

import android.view.View
import com.thesunnahrevival.sunnahassistant.views.customviews.HighlightOverlayView

interface QuranPageClickListener {
    fun onQuranPageClick(view: View)
    fun onQuranPageLongClick(view: View, highlightOverlay: HighlightOverlayView)

    fun setLastTouchCoordinates(x: Float, y: Float)

}