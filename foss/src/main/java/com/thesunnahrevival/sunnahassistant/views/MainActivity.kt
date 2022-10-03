package com.thesunnahrevival.sunnahassistant.views

import android.os.Bundle
import com.thesunnahrevival.lite.utilities.versionName
import com.thesunnahrevival.lite.views.MainActivity
import com.thesunnahrevival.sunnahassistant.BuildConfig

class MainActivity : MainActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        versionName = BuildConfig.VERSION_NAME
    }
}