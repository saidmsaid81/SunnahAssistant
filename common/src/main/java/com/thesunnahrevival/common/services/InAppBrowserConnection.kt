package com.thesunnahrevival.common.services

import android.content.ComponentName
import androidx.browser.customtabs.CustomTabsClient
import androidx.browser.customtabs.CustomTabsServiceConnection

class InAppBrowserConnection : CustomTabsServiceConnection() {
    override fun onServiceDisconnected(name: ComponentName?) {

    }

    override fun onCustomTabsServiceConnected(name: ComponentName, client: CustomTabsClient) {
        client.warmup(0)
    }
}