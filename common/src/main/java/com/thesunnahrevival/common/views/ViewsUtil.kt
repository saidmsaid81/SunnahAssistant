package com.thesunnahrevival.common.views

import android.content.Intent
import android.net.Uri
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetSequence
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.review.ReviewManagerFactory
import com.sergivonavi.materialbanner.Banner
import com.sergivonavi.materialbanner.BannerInterface
import com.thesunnahrevival.common.R
import com.thesunnahrevival.common.utilities.supportedLocales
import com.thesunnahrevival.common.views.adapters.ReminderListAdapter
import com.thesunnahrevival.common.views.home.TodayFragment
import kotlinx.android.synthetic.main.activity_main.coordinator_layout
import kotlinx.android.synthetic.main.today_fragment.*
import java.util.*

const val requestCodeForUpdate: Int = 1

fun showOnBoardingTutorial(
    activity: MainActivity,
    reminderRecyclerAdapter: ReminderListAdapter,
    recyclerView: RecyclerView
) {
    TapTargetSequence(activity)
        .targets(
            TapTarget.forView(
                activity.findViewById(R.id.fab),
                activity.getString(R.string.add_new_reminder),
                activity.getString(R.string.add_new_reminder_description)
            )
                .outerCircleColor(android.R.color.holo_blue_dark)
                .cancelable(false)
                .textColor(R.color.bottomSheetColor)
                .transparentTarget(true),
            TapTarget.forToolbarOverflow(
                activity.findViewById<View>(R.id.toolbar) as Toolbar,
                activity.getString(R.string.change_theme),
                activity.getString(R.string.change_theme_description)
            )
                .outerCircleColor(android.R.color.holo_blue_dark)
                .transparentTarget(true)
                .cancelable(false)
                .textColor(R.color.bottomSheetColor)
                .tintTarget(true),
            TapTarget.forView(
                recyclerView,
                activity.getString(R.string.edit_reminder),
                activity.getString(R.string.edit_reminder_description)
            )
                .outerCircleColor(android.R.color.holo_blue_dark)
                .cancelable(false)
                .tintTarget(true)
                .textColor(R.color.bottomSheetColor)
                .transparentTarget(true)
        )
        .listener(object : TapTargetSequence.Listener {
            override fun onSequenceFinish() {
                reminderRecyclerAdapter.deleteReminder(0)
            }

            override fun onSequenceStep(lastTarget: TapTarget, targetClicked: Boolean) {}
            override fun onSequenceCanceled(lastTarget: TapTarget) {}
        })
        .start()

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
                    popupSnackbar(
                        activity,
                        activity.getString(R.string.update_downloaded),
                        Snackbar.LENGTH_INDEFINITE,
                        activity.getString(R.string.restart)
                    ) { appUpdateManager.completeUpdate() }
                }
            }
            // Request the update.
            appUpdateManager.startUpdateFlowForResult(
                appUpdateInfo,
                AppUpdateType.FLEXIBLE,
                activity,
                requestCodeForUpdate
            )
        }
    }
}

fun popupSnackbar(
    activity: MainActivity,
    message: String,
    duration: Int,
    actionMessage: String,
    listener: View.OnClickListener
) {
    Snackbar.make(
        activity.coordinator_layout,
        message,
        duration
    ).apply {
        setAction(actionMessage, listener)
        view.setBackgroundColor(ContextCompat.getColor(activity, R.color.fabColor))
        setActionTextColor(activity.resources.getColor(android.R.color.black))
        show()
    }
}

fun showHelpTranslateSnackBar(todayFragment: TodayFragment) {
    if (!supportedLocales.contains(Locale.getDefault().language)) {
        val onClickListener = BannerInterface.OnClickListener {
            translateLink(todayFragment)
        }
        showBanner(
            todayFragment.banner,
            todayFragment.getString(
                R.string.help_translate_app,
                Locale.getDefault().displayLanguage
            ),
            R.drawable.help_translate,
            todayFragment.getString(R.string.translate),
            onClickListener
        )
    }
}

fun translateLink(fragment: Fragment) {
    val browserIntent = Intent(
        Intent.ACTION_VIEW,
        Uri.parse("https://crwd.in/sunnah-assistant")
    )
    if (fragment.activity?.packageManager?.let { it1 ->
            browserIntent.resolveActivity(
                it1
            )
        } != null) {
        fragment.startActivity(browserIntent)
    }
}

fun showSendFeedbackSnackBar(todayFragment: TodayFragment) {
    val onClickListener = BannerInterface.OnClickListener {
        val browserIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://forms.gle/78xZW7hqSE6SS4Ko6")
        )
        if (todayFragment.activity?.packageManager?.let { it1 -> browserIntent.resolveActivity(it1) } != null) {
            todayFragment.startActivity(browserIntent)
        }
    }
    showBanner(
        todayFragment.banner,
        todayFragment.getString(R.string.help_improve_app),
        R.drawable.feedback,
        todayFragment.getString(R.string.send_feedback),
        onClickListener
    )
}

fun showShareAppSnackBar(todayFragment: TodayFragment) {
    val onClickListener = BannerInterface.OnClickListener {
        val shareAppIntent = shareAppIntent()
        todayFragment.startActivity(
            Intent.createChooser(
                shareAppIntent,
                todayFragment.getString(R.string.share_app)
            )
        )
    }
    showBanner(
        todayFragment.banner,
        todayFragment.getString(R.string.help_us_grow),
        R.drawable.social_media,
        todayFragment.getString(R.string.share_app),
        onClickListener
    )
}

fun shareAppIntent(): Intent {
    val intent = Intent(Intent.ACTION_SEND)
    intent.type = "text/plain"
    intent.putExtra(
        Intent.EXTRA_TEXT,
        "I invite you to download Sunnah Assistant Android App. The app enables you: \n\n- To set any reminders\n- An option to receive Salah (prayer) time alerts. \n- An option to add Sunnah reminders such as Reminders to fast on Mondays and Thursdays and reading Suratul Kahf on Friday\n- Many more other features \n\nDownload here for free:- https://play.google.com/store/apps/details?id=com.thesunnahrevival.sunnahassistant "
    )
    return intent
}

private fun showBanner(
    banner: Banner,
    message: String,
    iconId: Int,
    rightButtonMessage: String,
    rightButtonListener: BannerInterface.OnClickListener,
    leftButtonMessage: String? = null,
    leftButtonListener: BannerInterface.OnClickListener? = null
) {
    banner.setMessage(message)
    banner.setIcon(iconId)
    banner.setRightButton(rightButtonMessage, rightButtonListener)
    if (leftButtonMessage != null && leftButtonListener != null)
        banner.setLeftButton(leftButtonMessage, leftButtonListener)
    else
        banner.setLeftButton(R.string.dismiss) { banner.dismiss() }
    banner.visibility = View.VISIBLE
}