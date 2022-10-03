package com.thesunnahrevival.sunnahassistant.widgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.widget.RemoteViews
import androidx.core.app.TaskStackBuilder
import com.thesunnahrevival.sunnahassistant.R
import com.thesunnahrevival.sunnahassistant.views.MainActivity

class PrayerTimesWidget : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updatePrayerAppWidget(context, appWidgetManager, appWidgetId, Color.WHITE, Color.BLACK)
        }
    }

}

internal fun updatePrayerAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int, backgroundColor: Int, textColor :Int) {

    val intent = Intent(context, PrayerToDosRemoteViewsService::class.java)
    intent.putExtra(TEXT_COLOR, textColor)

    // Construct the RemoteViews object
    val views = RemoteViews(context.packageName, R.layout.today_to_dos_widget)
    views.setRemoteAdapter(R.id.widgetListView, intent)
    views.setInt(R.id.widget, "setBackgroundColor", backgroundColor)
    views.setInt(R.id.widgetTitleLabel, "setTextColor", textColor)
    views.setTextViewText(R.id.widgetTitleLabel, "Prayer Times")


    val titleIntent = Intent(context, MainActivity::class.java)
    val titlePendingIntent = PendingIntent.getActivity(context, 0, titleIntent, 0)
    views.setOnClickPendingIntent(R.id.widgetTitleLabel, titlePendingIntent)

    // template to handle the click listener for each item
    val clickIntentTemplate = Intent(context, MainActivity::class.java)
    val clickPendingIntentTemplate: PendingIntent? = TaskStackBuilder.create(context)
            .addNextIntentWithParentStack(clickIntentTemplate)
            .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
    views.setPendingIntentTemplate(R.id.widgetListView, clickPendingIntentTemplate)


    // Instruct the widget manager to update the widget
    appWidgetManager.updateAppWidget(appWidgetId, views)
}