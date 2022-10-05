package com.thesunnahrevival.sunnahassistant.views.others

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.text.HtmlCompat
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.thesunnahrevival.sunnahassistant.BuildConfig
import com.thesunnahrevival.sunnahassistant.R
import com.thesunnahrevival.sunnahassistant.utilities.generateEmailIntent

class AboutAppFragment : BottomSheetDialogFragment(), View.OnClickListener {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_about_app, container, false)
        (view.findViewById<View>(R.id.version) as TextView).text =
            String.format(getString(R.string.version), BuildConfig.VERSION_NAME)
        val apiCredit = view.findViewById<TextView>(R.id.about_app)
        apiCredit.text = HtmlCompat.fromHtml(
            getString(R.string.about),
            HtmlCompat.FROM_HTML_MODE_LEGACY
        )
        apiCredit.movementMethod = LinkMovementMethod.getInstance()
        val appIconCredit = view.findViewById<TextView>(R.id.app_icon_credit)
        appIconCredit.text = HtmlCompat.fromHtml(
            getString(R.string.app_icon_credit),
            HtmlCompat.FROM_HTML_MODE_LEGACY
        )
        appIconCredit.movementMethod = LinkMovementMethod.getInstance()
        view.findViewById<View>(R.id.twitter).setOnClickListener(this)
        view.findViewById<View>(R.id.instagram).setOnClickListener(this)
        view.findViewById<View>(R.id.telegram).setOnClickListener(this)
        view.findViewById<View>(R.id.facebook).setOnClickListener(this)
        view.findViewById<View>(R.id.contact_us).setOnClickListener(this)
        return view
    }

    override fun onClick(v: View) {
        var intent: Intent? = null
        when (v.id) {
            R.id.twitter -> intent = Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://www.twitter.com/thesunahrevival"))
            R.id.facebook -> intent = Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://www.facebook.com/thesunnahrevival"))
            R.id.instagram -> intent = Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://www.instagram.com/thesunnahrevival"))
            R.id.telegram -> intent = Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://t.me/thesunnahrevival"))
            R.id.contact_us -> intent = generateEmailIntent()
        }
        intent?.let { startActivity(it) }
    }
}