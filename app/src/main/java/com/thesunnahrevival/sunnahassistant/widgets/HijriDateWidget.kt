package com.thesunnahrevival.sunnahassistant.widgets

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import com.thesunnahrevival.sunnahassistant.R
import com.thesunnahrevival.sunnahassistant.data.local.SunnahAssistantDatabase
import com.thesunnahrevival.sunnahassistant.utilities.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in [HijriDateWidgetConfigureActivity]
 */
class HijriDateWidget : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        CoroutineScope(Dispatchers.IO).launch {
            fetchDateFromDatabase(context, appWidgetManager, appWidgetIds.toTypedArray())
        }
    }
}

internal fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int, hijriText: String?, reminderName: String?, reminderTime: String?) {
    // Construct the RemoteViews object
    val views = RemoteViews(context.packageName, R.layout.hijri_date_widget)
    hijriText?.let { views.setTextViewText(R.id.hijri_date_text, hijriText) }
    reminderName?.let {
        views.setTextViewText(R.id.next_reminder_text, "$reminderTime: $it")
    }
    if (reminderName == null)
        views.setTextViewText(R.id.next_reminder_text, "")
    // Instruct the widget manager to update the widget
    appWidgetManager.updateAppWidget(appWidgetId, views)
}

internal suspend fun updateWidgetSettings(context: Context, isDisplayHijriDate: Boolean, isDisplayNextReminder: Boolean ){
    val reminderDao = SunnahAssistantDatabase.getInstance(context).reminderDao()
    reminderDao.updateWidgetSettings(isDisplayHijriDate, isDisplayNextReminder)
}

internal suspend fun fetchDateFromDatabase(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: Array<Int>){
    val reminderDao = SunnahAssistantDatabase.getInstance(context).reminderDao()
    val appSettings = reminderDao.getAppSettingsValue()

    val hijriText = if (appSettings?.isShowHijriDateWidget == true)
        hijriDate else null



    val nextReminder = if (appSettings?.isShowNextReminderWidget == true)
        reminderDao.getNextScheduledReminderToday(
            calculateOffsetFromMidnight(),
            dayOfTheWeek.toString(),
            getDayDate(System.currentTimeMillis()),
            getMonthNumber(System.currentTimeMillis()),
            getYear(System.currentTimeMillis()).toInt()) else null
    val reminderName = nextReminder?.reminderName
    val reminderTime = nextReminder?.timeInMilliseconds?.let { formatTimeInMilliseconds(context, it) }

    withContext(Dispatchers.Main){
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId, hijriText, reminderName, reminderTime)
        }
    }


}