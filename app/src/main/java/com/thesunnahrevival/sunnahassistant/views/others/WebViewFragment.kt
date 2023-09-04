package com.thesunnahrevival.sunnahassistant.views.others

import android.content.Intent
import android.graphics.Bitmap
import android.net.MailTo
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.thesunnahrevival.sunnahassistant.R
import com.thesunnahrevival.sunnahassistant.databinding.FragmentWebViewBinding
import com.thesunnahrevival.sunnahassistant.utilities.InAppBrowser

open class WebViewFragment : Fragment() {

    private var _webViewFragmentBinding: FragmentWebViewBinding? = null
    private val webViewFragmentBinding get() = _webViewFragmentBinding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _webViewFragmentBinding = FragmentWebViewBinding.inflate(inflater)
        setHasOptionsMenu(true)
        // Inflate the layout for this fragment
        return webViewFragmentBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        webViewFragmentBinding.webview.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                webViewFragmentBinding.progressBar.visibility = View.VISIBLE
                webViewFragmentBinding.webview.visibility = View.GONE
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                webViewFragmentBinding.progressBar.visibility = View.GONE
                webViewFragmentBinding.webview.visibility = View.VISIBLE
            }

            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                val url = request?.url.toString() ?: ""
                if (url.startsWith("mailto:")) {
                    val mailTo = MailTo.parse(url)
                    val intent = Intent(Intent.ACTION_SENDTO)
                    intent.data = Uri.parse("mailto:")
                    intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(mailTo.to))
                    view?.context?.startActivity(intent)
                    return true
                } else {
                    InAppBrowser(requireContext(), lifecycleScope).launchInAppBrowser(
                        url,
                        findNavController(),
                        false
                    )
                    return true
                }
                return false
            }
        }
        webViewFragmentBinding.webview.loadUrl(getLink().toString())
    }

    @Deprecated("Deprecated in Java")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.web_view_menu, menu)
    }

    @Deprecated("Deprecated in Java")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.share_page -> {
                val intent = Intent(Intent.ACTION_SEND)
                intent.type = "text/plain"
                intent.putExtra(
                    Intent.EXTRA_TEXT,
                    "${getLink()} \n\n${getString(R.string.sent_from_sunnah_assistant_app)}\n\n${
                        getString(
                            R.string.get_sunnah_assistant
                        )
                    } https://play.google.com/store/apps/details?id=com.thesunnahrevival.sunnahassistant "
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

    override fun onDestroyView() {
        super.onDestroyView()
        _webViewFragmentBinding = null
    }

}