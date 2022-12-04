package com.thesunnahrevival.sunnahassistant.views.settings

import android.view.Menu
import android.view.MenuInflater
import com.thesunnahrevival.sunnahassistant.views.others.WebViewFragment

class PrivacyPolicyFragment : WebViewFragment() {

    override fun getLink() = "file:///android_asset/policy.html"

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {

    }
}