package com.thesunnahrevival.common.widgets

import android.content.Intent
import android.widget.RemoteViewsService

class TodaysRemindersRemoteViewsService: RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent?): RemoteViewsFactory {
        return TodaysRemindersRemoteViewsFactory(this.applicationContext, intent)
    }
}