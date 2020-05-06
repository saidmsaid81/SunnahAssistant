package com.thesunnahrevival.sunnahassistant.widgets

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.graphics.Color

class PrayerTimesWidgetTransparent : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updatePrayerAppWidget(context, appWidgetManager, appWidgetId, Color.TRANSPARENT, Color.WHITE)
        }
    }

}