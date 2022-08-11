package com.thesunnahrevival.common.views.settings

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import com.google.firebase.analytics.FirebaseAnalytics
import com.thesunnahrevival.common.views.MainActivity
import com.thesunnahrevival.common.views.others.WebViewFragment

class PrivacyPolicyFragment : WebViewFragment() {

    override fun getLink() = "file:///android_asset/policy.html"

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {

    }

    override fun onResume() {
        super.onResume()
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, this.javaClass.simpleName)
        bundle.putString(FirebaseAnalytics.Param.SCREEN_CLASS, this.javaClass.simpleName)
        (activity as MainActivity).firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
    }



}