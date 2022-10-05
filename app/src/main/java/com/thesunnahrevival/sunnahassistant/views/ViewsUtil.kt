package com.thesunnahrevival.sunnahassistant.views

import android.content.Intent
import android.net.Uri
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.RecyclerView
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetSequence
import com.sergivonavi.materialbanner.Banner
import com.sergivonavi.materialbanner.BannerInterface
import com.thesunnahrevival.sunnahassistant.R
import com.thesunnahrevival.sunnahassistant.utilities.supportedLocales
import com.thesunnahrevival.sunnahassistant.views.adapters.ToDoListAdapter
import com.thesunnahrevival.sunnahassistant.views.home.TodayFragment
import kotlinx.android.synthetic.main.today_fragment.*
import java.util.*

fun showOnBoardingTutorial(
    activity: MainActivity,
    adapter: ConcatAdapter,
    recyclerView: RecyclerView
) {
    TapTargetSequence(activity)
        .targets(
            TapTarget.forView(
                activity.findViewById(R.id.fab),
                activity.getString(R.string.add_new_to_do),
                activity.getString(R.string.add_new_to_do_description)
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
                activity.getString(R.string.edit_to_do),
                activity.getString(R.string.edit_to_do_description)
            )
                .outerCircleColor(android.R.color.holo_blue_dark)
                .cancelable(false)
                .tintTarget(true)
                .textColor(R.color.bottomSheetColor)
                .transparentTarget(true)
        )
        .listener(object : TapTargetSequence.Listener {
            override fun onSequenceFinish() {
                (adapter.adapters[0] as ToDoListAdapter).deleteToDo(0)
            }

            override fun onSequenceStep(lastTarget: TapTarget, targetClicked: Boolean) {}
            override fun onSequenceCanceled(lastTarget: TapTarget) {}
        })
        .start()

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
        "I invite you to download Sunnah Assistant Android App. The app enables you: \n\n- To manage to-dos\n- An option to receive Salah (prayer) time alerts. \n- An option to add Sunnah reminders such as Reminders to fast on Mondays and Thursdays and reading Suratul Kahf on Friday\n- Many more other features \n\nDownload here for free:- https://play.google.com/store/apps/details?id=com.thesunnahrevival.sunnahassistant "
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