package com.thesunnahrevival.sunnahassistant.widgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.widget.RemoteViews
import androidx.core.app.TaskStackBuilder
import com.thesunnahrevival.sunnahassistant.R
import com.thesunnahrevival.sunnahassistant.views.MainActivity


/**
 * Implementation of App Widget functionality.
 */
class TodayToDosWidget : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId, Color.WHITE, Color.BLACK)
        }
    }

}

const val TEXT_COLOR = "text_color"
internal fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int, backgroundColor: Int, textColor :Int) {

    val intent = Intent(context, TodaysToDosRemoteViewsService::class.java)
    intent.putExtra(TEXT_COLOR, textColor)

    // Construct the RemoteViews object
    val views = RemoteViews(context.packageName, R.layout.today_to_dos_widget)
    views.setRemoteAdapter(R.id.widgetListView, intent)
    views.setInt(R.id.widget, "setBackgroundColor", backgroundColor)
    views.setInt(R.id.widgetTitleLabel, "setTextColor", textColor)


    val titleIntent = Intent(context, MainActivity::class.java)

    val flag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        PendingIntent.FLAG_IMMUTABLE
    } else {
        0
    }

    val titlePendingIntent = PendingIntent.getActivity(context, 0, titleIntent, flag)
    views.setOnClickPendingIntent(R.id.widgetTitleLabel, titlePendingIntent)

    // template to handle the click listener for each item
    val clickIntentTemplate = Intent(context, MainActivity::class.java)
    val updateCurrentFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    } else {
        PendingIntent.FLAG_UPDATE_CURRENT
    }
    val clickPendingIntentTemplate: PendingIntent? = TaskStackBuilder.create(context)
        .addNextIntentWithParentStack(clickIntentTemplate)
        .getPendingIntent(0, updateCurrentFlag)
    views.setPendingIntentTemplate(R.id.widgetListView, clickPendingIntentTemplate)


    // Instruct the widget manager to update the widget
    appWidgetManager.updateAppWidget(appWidgetId, views)
}