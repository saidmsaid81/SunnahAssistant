package com.thesunnahrevival.sunnahassistant.widgets

import android.content.Intent
import android.widget.RemoteViewsService

class TodaysToDosRemoteViewsService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent?): RemoteViewsFactory {
        return TodaysToDosRemoteViewsFactory(this.applicationContext, intent)
    }
}