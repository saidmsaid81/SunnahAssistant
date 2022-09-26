package com.thesunnahrevival.common.widgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import com.thesunnahrevival.common.R
import com.thesunnahrevival.common.data.SunnahAssistantRepository
import com.thesunnahrevival.common.utilities.*
import com.thesunnahrevival.common.views.MainActivity
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
    isShowNextReminder: Boolean,
    hijriText: String?,
    reminderName: String?,
    reminderTime: String?
) {
    // Construct the RemoteViews object
    val views = RemoteViews(context.packageName, R.layout.hijri_date_widget)
    if (hijriText != null) {
        views.setViewVisibility(R.id.hijri_date_text, View.VISIBLE)
        views.setTextViewText(R.id.hijri_date_text, hijriText)
    } else {
        views.setViewVisibility(R.id.hijri_date_text, View.GONE)
    }

    if (isShowNextReminder) {
        views.setViewVisibility(R.id.next_reminder_text, View.VISIBLE)
        val next = context.getString(R.string.next_string)
        if (reminderName != null)
            views.setTextViewText(
                R.id.next_reminder_text,
                "$next $reminderName at $reminderTime"
            )
        else
            views.setTextViewText(
                R.id.next_reminder_text,
                context.getString(R.string.no_upcoming_reminder_today)
            )
    } else
        views.setViewVisibility(R.id.next_reminder_text, View.GONE)

    val widgetIntent = Intent(context, MainActivity::class.java)
    val widgetPendingIntent = PendingIntent.getActivity(context, 0, widgetIntent, 0)
    views.setOnClickPendingIntent(R.id.widget, widgetPendingIntent)

    // Instruct the widget manager to update the widget
    appWidgetManager.updateAppWidget(appWidgetId, views)
}

internal suspend fun updateWidgetSettings(
    context: Context,
    isDisplayHijriDate: Boolean,
    isDisplayNextReminder: Boolean
) {
    val repository = SunnahAssistantRepository.getInstance(context.applicationContext)
    repository.updateWidgetSettings(isDisplayHijriDate, isDisplayNextReminder)
}

internal suspend fun fetchDateFromDatabase(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetIds: Array<Int>
) {
    val repository = SunnahAssistantRepository.getInstance(context.applicationContext)
    val appSettings = repository.getAppSettingsValue()

    val hijriText = if (appSettings?.isShowHijriDateWidget == true)
        generateDateText(isOnlyHijriDate = true) else null

    val nextReminder = if (appSettings?.isShowNextReminderWidget == true) {
        val timeInMilliseconds = System.currentTimeMillis()

        val nextTimeForReminderToday = repository.getNextTimeForReminderForDay(
            calculateOffsetFromMidnight(),
            dayOfTheWeek.toString(),
            getDayDate(timeInMilliseconds),
            getMonthNumber(timeInMilliseconds),
            Integer.parseInt(getYear(timeInMilliseconds))
        )
        repository.getNextScheduledRemindersForDay(
            nextTimeForReminderToday ?: -1,
            dayOfTheWeek.toString(),
            getDayDate(timeInMilliseconds),
            getMonthNumber(timeInMilliseconds),
            getYear(timeInMilliseconds).toInt()
        ).firstOrNull()
    } else null
    val reminderName = nextReminder?.reminderName
    val reminderTime =
        nextReminder?.timeInMilliseconds?.let { formatTimeInMilliseconds(context, it) }

    withContext(Dispatchers.Main) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(
                context,
                appWidgetManager,
                appWidgetId,
                appSettings?.isShowNextReminderWidget ?: false,
                hijriText,
                reminderName,
                reminderTime
            )
        }
    }


}