package com.thesunnahrevival.sunnahassistant.views.utilities

import android.view.View
import androidx.fragment.app.FragmentActivity
import com.google.android.material.button.MaterialButton
import com.thesunnahrevival.sunnahassistant.R
import com.thesunnahrevival.sunnahassistant.views.resourcesScreens.quran_reader.ResourcesNextActionFragment
import java.util.Calendar

fun showQuranPageNextAction(activity: FragmentActivity, view: View, page: Int) {
    val nextActionView = view.findViewById<MaterialButton>(R.id.next_action)

    val calendar = Calendar.getInstance()
    val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
    val isFriday = dayOfWeek == Calendar.FRIDAY

    if (page == 304) {
        nextActionView.visibility = View.VISIBLE
        nextActionView.text = activity.resources.getString(R.string.next_set_weekly_reminder)
    } else {
        nextActionView.visibility = View.GONE
    }

    nextActionView.setOnClickListener {
        val fragment = ResourcesNextActionFragment()
        fragment.show(
            activity.supportFragmentManager,
            "resources_next_action"
        )

    }
}