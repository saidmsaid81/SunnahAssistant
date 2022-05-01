package com.thesunnahrevival.sunnahassistant.widgets

import android.appwidget.AppWidgetManager
import android.graphics.Color
import android.widget.TextView
import com.thesunnahrevival.sunnahassistant.R

class TodayRemindersWidgetConfigureActivity : SunnahAssistantConfigureWidgetActivity() {
    override fun createWidget(context: SunnahAssistantConfigureWidgetActivity) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val options = resources.getStringArray(R.array.widget_theme_options)
        when (findViewById<TextView>(R.id.theme).text) {
            options[1] -> {
                updateAppWidget(
                    context,
                    appWidgetManager,
                    appWidgetId,
                    Color.BLACK,
                    Color.WHITE
                )
            }
            options[2] -> {
                updateAppWidget(
                    context,
                    appWidgetManager,
                    appWidgetId,
                    Color.TRANSPARENT,
                    Color.WHITE
                )
            }
            else -> {
                updateAppWidget(
                    context,
                    appWidgetManager,
                    appWidgetId,
                    Color.WHITE,
                    Color.BLACK
                )
            }
        }
    }
}