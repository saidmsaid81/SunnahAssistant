package com.thesunnahrevival.sunnahassistant.views.home

import android.content.Intent
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.thesunnahrevival.lite.views.home.TodayFragment

class TodayFragment : TodayFragment() {
    override fun launchOSSLicensesActivity() {
        startActivity(Intent(context, OssLicensesMenuActivity::class.java))
    }
}