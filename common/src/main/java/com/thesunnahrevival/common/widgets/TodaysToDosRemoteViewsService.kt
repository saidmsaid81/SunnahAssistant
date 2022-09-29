package com.thesunnahrevival.common.widgets

import android.content.Intent
import android.widget.RemoteViewsService

class TodaysToDosRemoteViewsService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent?): RemoteViewsFactory {
        return TodaysToDosRemoteViewsFactory(this.applicationContext, intent)
    }
}