package com.thesunnahrevival.sunnahassistant.widgets

import android.content.Intent
import android.widget.RemoteViewsService

class PrayerRemindersRemoteViewsService: RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent?): RemoteViewsFactory {
        return PrayerTimesRemoteViewsFactory(this.applicationContext, intent)
    }
}