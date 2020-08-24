package com.thesunnahrevival.sunnahassistant.views

import android.view.View
import android.widget.Spinner
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.RecyclerView
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetSequence
import com.getkeepsafe.taptargetview.TapTargetView
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.review.ReviewManagerFactory
import com.thesunnahrevival.sunnahassistant.R
import com.thesunnahrevival.sunnahassistant.views.adapters.ReminderListAdapter

val requestCodeForUpdate: Int = 1

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

fun showInAppReviewPrompt(activity: MainActivity) {
    val manager = ReviewManagerFactory.create(activity)
    val request = manager.requestReviewFlow()
    request.addOnCompleteListener {
        if (it.isSuccessful) {
            // We got the ReviewInfo object
            val reviewInfo = it.result
            manager.launchReviewFlow(activity, reviewInfo)
        }
    }
}

fun checkForUpdates(activity: MainActivity) {
    // Creates instance of the manager.
    val appUpdateManager = AppUpdateManagerFactory.create(activity)

    // Returns an intent object that you use to check for an update.
    val appUpdateInfoTask = appUpdateManager.appUpdateInfo

    // Checks that the platform will allow the specified type of update.
    appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
        if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)
        ) {

            appUpdateManager.registerListener { state ->
                if (state.installStatus() == InstallStatus.DOWNLOADED) {
                    // After the update is downloaded, show a notification
                    // and request user confirmation to restart the app.
                    popupSnackbar(activity, activity.getString(R.string.update_downloaded),  Snackbar.LENGTH_INDEFINITE, activity.getString(R.string.restart), View.OnClickListener { appUpdateManager.completeUpdate() } )
                }
            }
            // Request the update.
            appUpdateManager.startUpdateFlowForResult(
                    appUpdateInfo,
                    AppUpdateType.FLEXIBLE,
                    activity,
                    requestCodeForUpdate)
        }
    }
}

fun popupSnackbar(activity: MainActivity, message: String, duration: Int, actionMessage: String, listener: View.OnClickListener) {
    Snackbar.make(
            activity.findViewById(R.id.coordinator_layout),
            message,
            duration
    ).apply {
        setAction(actionMessage, listener)
        show()
    }
}