package com.thesunnahrevival.sunnahassistant.views.utilities

import android.view.View
import com.google.android.material.button.MaterialButton
import com.thesunnahrevival.sunnahassistant.R
import java.util.Calendar

fun showQuranPageNextAction(view: View, page: Int) {
    val nextActionView = view.findViewById<MaterialButton>(R.id.next_action)

    val calendar = Calendar.getInstance()
    val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
    val isFriday = dayOfWeek == Calendar.FRIDAY

    if (page == 304 && isFriday) {
        nextActionView.visibility = View.VISIBLE
    } else {
        nextActionView.visibility = View.GONE
    }
}