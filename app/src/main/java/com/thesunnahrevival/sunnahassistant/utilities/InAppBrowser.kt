package com.thesunnahrevival.sunnahassistant.utilities

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.browser.customtabs.CustomTabsClient
import androidx.browser.customtabs.CustomTabsIntent
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.navigation.NavController
import com.thesunnahrevival.sunnahassistant.R
import com.thesunnahrevival.sunnahassistant.receivers.InAppBrowserBroadcastReceiver
import com.thesunnahrevival.sunnahassistant.receivers.LINK
import com.thesunnahrevival.sunnahassistant.services.InAppBrowserConnection
import kotlinx.coroutines.launch
import org.apache.commons.validator.routines.UrlValidator
import java.net.MalformedURLException

class InAppBrowser(private val context: Context, lifecycleScope: LifecycleCoroutineScope) {

    private var mShareIcon: Bitmap? = null
    private var browserPackageName: String? = null

    init {
        prepareInAppBrowser(lifecycleScope)
    }

    private fun prepareInAppBrowser(lifecycleScope: LifecycleCoroutineScope) {
        browserPackageName = getPackageNameToUse(context)
        CustomTabsClient.bindCustomTabsService(
            context,
            context.packageName,
            InAppBrowserConnection()
        )
        lifecycleScope.launch {
            mShareIcon =
                BitmapFactory.decodeResource(context.resources, android.R.drawable.ic_menu_share)
        }
    }

    fun launchInAppBrowser(
        link: String,
        findNavController: NavController,
        showShareIcon: Boolean = true
    ) {
        if (!isValidUrl(link))
            throw MalformedURLException("$link is an invalid url")
        if (browserPackageName == null) {//No supported browser
            val bundle = Bundle().apply {
                putString("link", link)
            }
            findNavController.navigate(R.id.webviewFragment, bundle)
            return
        }

        val builder = CustomTabsIntent.Builder()
        if (showShareIcon) {
            val actionIntent = Intent(
                context, InAppBrowserBroadcastReceiver::class.java
            )

            actionIntent.putExtra(LINK, link)

            val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context, 0, actionIntent, flags
            )
            builder.addMenuItem(
                context.getString(R.string.share_message),
                pendingIntent
            )

            val shareIcon = mShareIcon
            if (shareIcon != null)
                builder.setActionButton(
                    shareIcon,
                    context.getString(R.string.share_message),
                    pendingIntent,
                    true
                )
        }

        val customTabsIntent = builder.build()
        customTabsIntent.intent.setPackage(browserPackageName)
        customTabsIntent.intent.putExtra(
            Intent.EXTRA_REFERRER,
            Uri.parse("android-app://$browserPackageName")
        )
        customTabsIntent.launchUrl(context, Uri.parse(link))
    }

    private fun isValidUrl(link: String) = UrlValidator().isValid(link)
}