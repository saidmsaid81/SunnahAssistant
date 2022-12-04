package com.thesunnahrevival.sunnahassistant.utilities

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.text.TextUtils
import android.util.Log
import androidx.browser.customtabs.CustomTabsService


fun getPackageNameToUse(context: Context): String? {
    val packageManager = context.packageManager
    // Get default VIEW intent handler.
    val activityIntent = Intent(Intent.ACTION_VIEW, Uri.parse("http://www.example.com"))
    val defaultViewHandlerInfo: ResolveInfo? = packageManager.resolveActivity(activityIntent, 0)
    var defaultViewHandlerPackageName: String? = null
    if (defaultViewHandlerInfo != null) {
        defaultViewHandlerPackageName = defaultViewHandlerInfo.activityInfo.packageName;
    }

    // Get all apps that can handle VIEW intents.
    val resolvedActivityList = packageManager.queryIntentActivities(activityIntent, 0)
    val packagesSupportingCustomTabs: MutableList<String> = ArrayList()
    for (info in resolvedActivityList) {
        val serviceIntent = Intent()
        serviceIntent.action = CustomTabsService.ACTION_CUSTOM_TABS_CONNECTION
        serviceIntent.setPackage(info.activityInfo.packageName)
        if (packageManager.resolveService(serviceIntent, 0) != null) {
            packagesSupportingCustomTabs.add(info.activityInfo.packageName)
        }
    }


    // Now packagesSupportingCustomTabs contains all apps that can handle both VIEW intents
    // and service calls.
    return if (packagesSupportingCustomTabs.isEmpty())
        null
    else if (packagesSupportingCustomTabs.size == 1)
        packagesSupportingCustomTabs.first()
    else if (!TextUtils.isEmpty(defaultViewHandlerPackageName)
        && !hasSpecializedHandlerIntents(context, activityIntent)
        && packagesSupportingCustomTabs.contains(defaultViewHandlerPackageName)
    )
        defaultViewHandlerPackageName
    else if (packagesSupportingCustomTabs.size > 0)
        packagesSupportingCustomTabs.first()
    else
        null

}

/**
 * Used to check whether there is a specialized handler for a given intent.
 * @param intent The intent to check with.
 * @return Whether there is a specialized handler for the given intent.
 */
private fun hasSpecializedHandlerIntents(context: Context, intent: Intent): Boolean {
    try {
        val pm = context.packageManager
        val handlers = pm.queryIntentActivities(
            intent,
            PackageManager.GET_RESOLVED_FILTER
        )
        if (handlers.size == 0) {
            return false
        }
        for (resolveInfo in handlers) {
            val filter = resolveInfo.filter ?: continue
            if (filter.countDataAuthorities() == 0 || filter.countDataPaths() == 0) continue
            if (resolveInfo.activityInfo == null) continue
            return true
        }
    } catch (e: RuntimeException) {
        Log.e("InAppBrowser", "Runtime exception while getting specialized handlers")
    }
    return false
}
