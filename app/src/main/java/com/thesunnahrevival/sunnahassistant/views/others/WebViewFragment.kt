package com.thesunnahrevival.sunnahassistant.views.others

import android.content.Intent
import android.graphics.Bitmap
import android.net.MailTo
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.thesunnahrevival.sunnahassistant.R
import com.thesunnahrevival.sunnahassistant.databinding.FragmentWebViewBinding
import com.thesunnahrevival.sunnahassistant.utilities.InAppBrowser
import com.thesunnahrevival.sunnahassistant.utilities.PREDEFINED_TO_DO_ID
import com.thesunnahrevival.sunnahassistant.utilities.getSunnahAssistantAppLink
import com.thesunnahrevival.sunnahassistant.views.SunnahAssistantFragment

open class WebViewFragment : SunnahAssistantFragment() {

    private var _webViewFragmentBinding: FragmentWebViewBinding? = null
    private val webViewFragmentBinding get() = _webViewFragmentBinding!!
    private var predefinedToDoId: Int? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        _webViewFragmentBinding = FragmentWebViewBinding.inflate(inflater)
        setHasOptionsMenu(true)
        // Inflate the layout for this fragment
        return webViewFragmentBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        predefinedToDoId = if (arguments?.containsKey(PREDEFINED_TO_DO_ID) == true) {
            arguments?.getInt(PREDEFINED_TO_DO_ID)
        } else {
            null
        }
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
        menu.findItem(R.id.set_reminder)?.isVisible = predefinedToDoId != null
    }

    @Deprecated("Deprecated in Java")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.set_reminder -> {
                predefinedToDoId?.let { toDoId ->
                    val templateToDos = mainActivityViewModel.getTemplateToDos()
                    templateToDos[toDoId]?.let { template ->
                        mainActivityViewModel.selectedToDo = template.second
                        mainActivityViewModel.isToDoTemplate = true
                        findNavController().navigate(R.id.toDoDetailsFragment)
                    }
                }
                return true
            }
            R.id.share_page -> {
                val intent = Intent(Intent.ACTION_SEND)
                intent.type = "text/plain"
                intent.putExtra(
                    Intent.EXTRA_TEXT,
                    "${getLink()} \n\n" +
                            getString(R.string.app_promotional_message, getSunnahAssistantAppLink(utmCampaign = "Share-Page-Link"))
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
