package com.thesunnahrevival.sunnahassistant.utilities

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import androidx.browser.customtabs.CustomTabsClient
import androidx.browser.customtabs.CustomTabsIntent
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.navigation.NavController
import com.thesunnahrevival.sunnahassistant.R
import com.thesunnahrevival.sunnahassistant.receivers.InAppBrowserBroadcastReceiver
import com.thesunnahrevival.sunnahassistant.receivers.TEXTSUMMARY
import com.thesunnahrevival.sunnahassistant.services.InAppBrowserConnection
import kotlinx.coroutines.launch

class InAppBrowser(context: Context, lifecycleScope: LifecycleCoroutineScope) {

    private var mShareIcon: Bitmap? = null
    private var browserPackageName: String? = null

    init {
        prepareInAppBrowser(context, lifecycleScope)
    }

    private fun prepareInAppBrowser(context: Context, lifecycleScope: LifecycleCoroutineScope) {
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
        context: Context,
        link: String,
        findNavController: NavController,
        showShareIcon: Boolean = true
    ) {
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

            actionIntent.putExtra(TEXTSUMMARY, link)

            val pendingIntent = PendingIntent.getBroadcast(
                context, 0, actionIntent, PendingIntent.FLAG_UPDATE_CURRENT
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
}