package com.thesunnahrevival.sunnahassistant.views

import android.view.View
import android.widget.Spinner
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.RecyclerView
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetSequence
import com.getkeepsafe.taptargetview.TapTargetView
import com.thesunnahrevival.sunnahassistant.R
import com.thesunnahrevival.sunnahassistant.views.adapters.ReminderListAdapter

fun showOnBoardingTutorial(activity: MainActivity, reminderRecyclerAdapter: ReminderListAdapter, spinner: Spinner, recyclerView: RecyclerView) {
    TapTargetSequence(activity)
            .targets(
                    TapTarget.forView(activity.findViewById(R.id.fab), activity.getString(R.string.add_new_reminder), activity.getString(R.string.add_new_reminder_description))
                            .outerCircleColor(android.R.color.holo_blue_dark)
                            .cancelable(false)
                            .textColor(R.color.bottomSheetColor)
                            .transparentTarget(true),
                    TapTarget.forView(spinner, activity.getString(R.string.spinner_tutorial), activity.getString(R.string.spinner_tutorial_description))
                            .outerCircleColor(android.R.color.holo_blue_dark)
                            .cancelable(false)
                            .textColor(R.color.bottomSheetColor)
                            .transparentTarget(true),
                    TapTarget.forToolbarOverflow(activity?.findViewById<View>(R.id.toolbar) as Toolbar,
                            activity.getString(R.string.change_theme),
                            activity.getString(R.string.change_theme_description))
                            .outerCircleColor(android.R.color.holo_blue_dark)
                            .transparentTarget(true)
                            .cancelable(false)
                            .textColor(R.color.bottomSheetColor)
                            .tintTarget(true),
                    TapTarget.forView(
                            recyclerView,
                            activity.getString(R.string.edit_reminder),
                            activity.getString(R.string.edit_reminder_description))
                            .outerCircleColor(android.R.color.holo_blue_dark)
                            .cancelable(false)
                            .tintTarget(true)
                            .textColor(R.color.bottomSheetColor)
                            .transparentTarget(true))
            .listener(object : TapTargetSequence.Listener {
                override fun onSequenceFinish() {
                    showFilterReminderOnBoarding(activity, reminderRecyclerAdapter)
                }
                override fun onSequenceStep(lastTarget: TapTarget, targetClicked: Boolean) {}
                override fun onSequenceCanceled(lastTarget: TapTarget) {}
            })
            .start()

}

private fun showFilterReminderOnBoarding(activity: MainActivity, reminderRecyclerAdapter: ReminderListAdapter) {
    TapTargetView.showFor(
            activity,
            TapTarget.forToolbarMenuItem(
                    activity.findViewById<View>(R.id.toolbar) as Toolbar,
                    R.id.filter,
                    activity.getString(R.string.filter_displayed_reminders),
                    activity.getString(R.string.filter_displayed_reminders_description))
                    .cancelable(false)
                    .outerCircleColor(android.R.color.holo_blue_dark)
                    .textColor(R.color.bottomSheetColor)
                    .tintTarget(true), object : TapTargetView.Listener() {
        override fun onTargetClick(view: TapTargetView) {
            reminderRecyclerAdapter.deleteReminder(0)
            view.dismiss(true)
        }
    })
}