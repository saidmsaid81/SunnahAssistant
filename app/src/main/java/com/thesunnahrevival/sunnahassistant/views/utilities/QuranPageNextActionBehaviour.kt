package com.thesunnahrevival.sunnahassistant.views.utilities

import android.view.View
import androidx.fragment.app.FragmentActivity
import com.google.android.material.button.MaterialButton
import com.thesunnahrevival.sunnahassistant.R
import com.thesunnahrevival.sunnahassistant.data.repositories.ResourcesNextActionRepository.NextAction
import com.thesunnahrevival.sunnahassistant.views.resourcesScreens.quran_reader.ResourcesNextActionFragment

fun showQuranPageNextAction(activity: FragmentActivity, view: View, nextAction: NextAction?) {
    val nextActionView = view.findViewById<MaterialButton>(R.id.next_action)

    if (nextAction != null) {
        nextActionView.visibility = View.VISIBLE
        nextActionView.text = "${activity.resources.getString(R.string.next)}: ${activity.resources.getString(nextAction.actionResId)}"
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