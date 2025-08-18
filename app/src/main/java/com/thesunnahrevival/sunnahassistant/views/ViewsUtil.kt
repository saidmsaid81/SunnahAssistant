package com.thesunnahrevival.sunnahassistant.views

import android.content.Intent
import android.net.Uri
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.sergivonavi.materialbanner.Banner
import com.sergivonavi.materialbanner.BannerInterface
import com.thesunnahrevival.sunnahassistant.R
import com.thesunnahrevival.sunnahassistant.utilities.SUPPORTED_LOCALES
import com.thesunnahrevival.sunnahassistant.utilities.getSunnahAssistantAppLink
import com.thesunnahrevival.sunnahassistant.views.home.TodayFragment
import java.util.*

fun showHelpTranslateBanner(todayFragment: TodayFragment) {
    if (!SUPPORTED_LOCALES.contains(Locale.getDefault().language)) {
        val banner = todayFragment.view?.findViewById<Banner>(R.id.banner)
        val onClickListener = BannerInterface.OnClickListener {
            translateLink(todayFragment)
            banner?.dismiss()
        }
        showBanner(
            banner,
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

fun showSendFeedbackBanner(todayFragment: TodayFragment) {
    val banner = todayFragment.view?.findViewById<Banner>(R.id.banner)
    val onClickListener = BannerInterface.OnClickListener {
        val browserIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://forms.gle/78xZW7hqSE6SS4Ko6")
        )
        if (todayFragment.activity?.packageManager?.let { it1 -> browserIntent.resolveActivity(it1) } != null) {
            todayFragment.startActivity(browserIntent)
        }
        banner?.dismiss()
    }
    showBanner(
        banner,
        todayFragment.getString(R.string.help_improve_app),
        R.drawable.feedback,
        todayFragment.getString(R.string.send_feedback),
        onClickListener
    )
}

fun showShareAppBanner(todayFragment: TodayFragment) {
    val banner = todayFragment.view?.findViewById<Banner>(R.id.banner)
    val onClickListener = BannerInterface.OnClickListener {
        val shareAppIntent = shareAppIntent()
        todayFragment.startActivity(
            Intent.createChooser(
                shareAppIntent,
                todayFragment.getString(R.string.share_app)
            )
        )
        banner?.dismiss()
    }
    showBanner(
        banner,
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
        "I invite you to download Sunnah Assistant Android App. The app enables you: \n\n- To manage to-dos\n- An option to receive Salah (prayer) time alerts. \n- An option to add Sunnah reminders such as Reminders to fast on Mondays and Thursdays and reading Suratul Kahf on Friday\n- Many more other features \n\nDownload here for free:- ${getSunnahAssistantAppLink(utmCampaign = "Share-App-Link")} "
    )
    return intent
}

fun showBanner(
    banner: Banner?,
    message: String,
    iconId: Int,
    rightButtonMessage: String,
    rightButtonListener: BannerInterface.OnClickListener,
    leftButtonMessage: String? = null,
    leftButtonListener: BannerInterface.OnClickListener? = null
) {
    if (banner == null) {
        return
    }
    banner.setMessage(message)
    banner.setIcon(iconId)
    banner.setRightButton(rightButtonMessage, rightButtonListener)
    if (leftButtonMessage != null && leftButtonListener != null)
        banner.setLeftButton(leftButtonMessage, leftButtonListener)
    else
        banner.setLeftButton(R.string.dismiss) { banner.dismiss() }
    banner.visibility = View.VISIBLE
}

fun ViewPager2.reduceDragSensitivity(sensitivity: Int = 0) {
    val recyclerViewField = ViewPager2::class.java.getDeclaredField("mRecyclerView")
    recyclerViewField.isAccessible = true
    val recyclerView = recyclerViewField.get(this) as RecyclerView

    val touchSlopField = RecyclerView::class.java.getDeclaredField("mTouchSlop")
    touchSlopField.isAccessible = true
    val touchSlop = touchSlopField.get(recyclerView) as Int
    touchSlopField.set(recyclerView, touchSlop*sensitivity)
}