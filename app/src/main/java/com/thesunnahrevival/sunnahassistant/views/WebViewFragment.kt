package com.thesunnahrevival.sunnahassistant.views

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.*
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import com.thesunnahrevival.sunnahassistant.R
import kotlinx.android.synthetic.main.web_view_fragment.*

open class WebViewFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.web_view_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        webview.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                progress_bar.visibility = View.VISIBLE
                webview.visibility = View.GONE
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                progress_bar.visibility = View.GONE
                webview.visibility = View.VISIBLE
            }
        }
        webview.loadUrl(getLink().toString())
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.web_view_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.share_page -> {
                val intent = Intent(Intent.ACTION_SEND)
                intent.type = "text/plain"
                intent.putExtra(
                    Intent.EXTRA_TEXT,
                    "${getLink()} \n\nSent from Sunnah Assistant App.\n\nGet Sunnah Assistant App at https://play.google.com/store/apps/details?id=com.thesunnahrevival.sunnahassistant "
                )
                startActivity(
                    Intent.createChooser(
                        intent,
                        getString(R.string.share_message)
                    )
                )
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    open fun getLink() = arguments?.get("link") ?: "https://thesunnahrevival.com"

}