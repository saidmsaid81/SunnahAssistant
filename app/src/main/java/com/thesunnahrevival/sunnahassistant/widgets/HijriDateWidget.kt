package com.thesunnahrevival.sunnahassistant.widgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.Build
import android.view.View
import android.widget.RemoteViews
import com.thesunnahrevival.sunnahassistant.R
import com.thesunnahrevival.sunnahassistant.data.repositories.SunnahAssistantRepository
import com.thesunnahrevival.sunnahassistant.utilities.*
import com.thesunnahrevival.sunnahassistant.views.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in [HijriDateWidgetConfigureActivity]
 */
class HijriDateWidget : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            fetchDateFromDatabase(context, appWidgetManager, appWidgetIds.toTypedArray())
        }
    }
}

internal fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int,
    isShowNextToDo: Boolean,
    hijriText: String?,
    toDoName: String?,
    toDoTime: String?
) {
    // Construct the RemoteViews object
    val views = RemoteViews(context.packageName, R.layout.widget_hijri_date)
    if (hijriText != null) {
        views.setViewVisibility(R.id.hijri_date_text, View.VISIBLE)
        views.setTextViewText(R.id.hijri_date_text, hijriText)
    } else {
        views.setViewVisibility(R.id.hijri_date_text, View.GONE)
    }

    if (isShowNextToDo) {
        views.setViewVisibility(R.id.next_to_do_text, View.VISIBLE)
        val next = context.getString(R.string.next_string)
        if (toDoName != null)
            views.setTextViewText(
                R.id.next_to_do_text,
                "$next $toDoName at $toDoTime"
            )
        else
            views.setTextViewText(
                R.id.next_to_do_text,
                context.getString(R.string.no_upcoming_to_do_today)
            )
    } else
        views.setViewVisibility(R.id.next_to_do_text, View.GONE)

    val widgetIntent = Intent(context, MainActivity::class.java)

    val flag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        PendingIntent.FLAG_IMMUTABLE
    } else {
        0
    }

    val widgetPendingIntent = PendingIntent.getActivity(context, 0, widgetIntent, flag)
    views.setOnClickPendingIntent(R.id.widget, widgetPendingIntent)

    // Instruct the widget manager to update the widget
    appWidgetManager.updateAppWidget(appWidgetId, views)
}

internal suspend fun updateWidgetSettings(
    context: Context,
    isDisplayHijriDate: Boolean,
    isDisplayNextToDo: Boolean
) {
    val repository = SunnahAssistantRepository.getInstance(context.applicationContext)
    repository.updateWidgetSettings(isDisplayHijriDate, isDisplayNextToDo)
}

internal suspend fun fetchDateFromDatabase(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetIds: Array<Int>
) {
    val repository = SunnahAssistantRepository.getInstance(context.applicationContext)
    val appSettings = repository.getAppSettingsValue()

    val hijriText = if (appSettings?.isShowHijriDateWidget == true)
        generateDateText(includeGregorianDate = false) else null

    val nextToDo = if (appSettings?.isShowNextToDoWidget == true) {
        val timeInMilliseconds = System.currentTimeMillis()

        val nextTimeForToDoToday = repository.getNextTimeForToDosForDay(
            calculateOffsetFromMidnight(),
            dayOfTheWeek.toString(),
            getDayDate(timeInMilliseconds),
            getMonthNumber(timeInMilliseconds),
            Integer.parseInt(getYear(timeInMilliseconds))
        )
        repository.getNextScheduledToDosForDay(
            nextTimeForToDoToday ?: -1,
            dayOfTheWeek.toString(),
            getDayDate(timeInMilliseconds),
            getMonthNumber(timeInMilliseconds),
            getYear(timeInMilliseconds).toInt()
        ).firstOrNull()
    } else null
    val toDoName = nextToDo?.name
    val toDoTime =
        nextToDo?.timeInMilliseconds?.let { formatTimeInMilliseconds(context, it) }

    withContext(Dispatchers.Main) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(
                context,
                appWidgetManager,
                appWidgetId,
                appSettings?.isShowNextToDoWidget ?: false,
                hijriText,
                toDoName,
                toDoTime
            )
        }
    }


}