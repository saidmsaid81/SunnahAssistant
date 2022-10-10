package com.thesunnahrevival.sunnahassistant.views.utilities

import android.view.View
import android.view.ViewGroup.LayoutParams
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.RecyclerView
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetSequence
import com.thesunnahrevival.sunnahassistant.R
import com.thesunnahrevival.sunnahassistant.views.MainActivity
import com.thesunnahrevival.sunnahassistant.views.adapters.ToDoListAdapter

class ShowcaseView {
    fun showOnBoardingTutorial(activity: MainActivity, adapter: ConcatAdapter) {
        activity.findViewById<RecyclerView>(R.id.to_do_list).layoutParams.height =
            LayoutParams.WRAP_CONTENT
        TapTargetSequence(activity)
            .targets(
                TapTarget.forView(
                    activity.findViewById(R.id.fab),
                    activity.getString(R.string.add_new_to_do),
                    activity.getString(R.string.add_new_to_do_description)
                )
                    .outerCircleColor(android.R.color.holo_blue_dark)
                    .cancelable(false)
                    .textColor(android.R.color.white)
                    .transparentTarget(true),
                TapTarget.forView(
                    activity.findViewById(R.id.to_do_list),
                    activity.getString(R.string.edit_to_do),
                    activity.getString(R.string.edit_to_do_description)
                )
                    .outerCircleColor(android.R.color.holo_blue_dark)
                    .cancelable(false)
                    .tintTarget(true)
                    .textColor(android.R.color.white)
                    .transparentTarget(true),
                TapTarget.forView(
                    activity.findViewById(R.id.to_do_list),
                    activity.getString(R.string.gestures_demo),
                    activity.getString(R.string.gestures_description)
                )
                    .outerCircleColor(android.R.color.holo_blue_dark)
                    .cancelable(false)
                    .tintTarget(true)
                    .textColor(android.R.color.white)
                    .transparentTarget(true),
                TapTarget.forToolbarOverflow(
                    activity.findViewById<View>(R.id.toolbar) as Toolbar,
                    activity.getString(R.string.change_theme),
                    activity.getString(R.string.change_theme_description)
                )
                    .outerCircleColor(android.R.color.holo_blue_dark)
                    .transparentTarget(true)
                    .cancelable(false)
                    .textColor(android.R.color.white)
                    .tintTarget(true)
            )
            .listener(object : TapTargetSequence.Listener {
                override fun onSequenceFinish() {
                    activity.findViewById<RecyclerView>(R.id.to_do_list).layoutParams.height =
                        LayoutParams.MATCH_PARENT
                    (adapter.adapters[0] as ToDoListAdapter).deleteToDo(0)
                }

                override fun onSequenceStep(lastTarget: TapTarget, targetClicked: Boolean) {}
                override fun onSequenceCanceled(lastTarget: TapTarget) {}
            })
            .start()
    }
}