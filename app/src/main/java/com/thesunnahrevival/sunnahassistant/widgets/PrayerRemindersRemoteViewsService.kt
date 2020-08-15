package com.thesunnahrevival.sunnahassistant.widgets

import android.content.Intent
import android.widget.RemoteViewsService
import java.util.*

class PrayerRemindersRemoteViewsService(val locale: Locale): RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent?): RemoteViewsFactory {
        return PrayerTimesRemoteViewsFactory(this.applicationContext, intent)
    }
}