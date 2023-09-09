@file:JvmName("SunnahAssistantUtil")

package com.thesunnahrevival.sunnahassistant.utilities

import android.appwidget.AppWidgetManager
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import com.thesunnahrevival.sunnahassistant.BuildConfig
import com.thesunnahrevival.sunnahassistant.R
import com.thesunnahrevival.sunnahassistant.data.model.AppSettings
import com.thesunnahrevival.sunnahassistant.data.model.Frequency
import com.thesunnahrevival.sunnahassistant.data.model.ToDo
import com.thesunnahrevival.sunnahassistant.widgets.HijriDateWidget
import com.thesunnahrevival.sunnahassistant.widgets.PrayerTimesWidget
import com.thesunnahrevival.sunnahassistant.widgets.TodayToDosWidget
import java.net.URL
import java.time.LocalDate
import java.util.*

val supportedLocales = arrayOf("en", "ar")
const val retryAfterFlagKey = "Retry-After"
const val supportEmail = "apps@thesunnahrevival.com"
const val expectedUserAgent = "SunnahAssistant-Android-App"
const val requestNotificationPermissionCode = 100
const val notificationPermissionRequestsCountKey = "notification-permission-requests-count"


fun generateEmailIntent(): Intent {
    val intent = Intent(Intent.ACTION_SENDTO)
    intent.data = Uri.parse("mailto:")
    intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(supportEmail))
    intent.putExtra(
        Intent.EXTRA_SUBJECT,
        "Sunnah Assistant App - Version ${BuildConfig.VERSION_NAME}"
    )
    intent.putExtra(Intent.EXTRA_TEXT, emailText)
    return intent
}

private val emailText: String
    get() = """
        Please write your feedback below in English





        Additional Info
        App Name: Sunnah Assistant
        App Version: ${BuildConfig.VERSION_NAME}
        Brand: ${Build.BRAND}
        Model: ${Build.MODEL}
        Android Version: ${Build.VERSION.RELEASE}
        Device Language: ${Locale.getDefault().language}
        """.trimIndent()

fun openPlayStore(context: Context, appPackageName: String) {
    try {
        context.startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("market://details?id=$appPackageName")
            )
        )
    } catch (e: ActivityNotFoundException) {
        context.startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")
            )
        )
    }
}

fun openDeveloperPage(context: Context) {
    try {
        context.startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("market://dev?id=6919675665650793025")
            )
        )
    } catch (e: ActivityNotFoundException) {
        context.startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/dev?id=6919675665650793025")
            )
        )
    }
}

fun demoToDo(name: String, category: String): ToDo {
    val now = LocalDate.now()
    return ToDo(
        name = name,
        frequency = Frequency.OneTime,
        category = category,
        day = now.dayOfMonth,
        month = now.month.ordinal,
        year = now.year,
        id = 1
    )
}

fun initialSettings(categories: TreeSet<String>): AppSettings {
    val appSettings = AppSettings()
    appSettings.categories = categories
    return appSettings
}

fun updateWidgets(context: Context) {
    updateHijriDateWidgets(context)
    updateTodayRemindersWidgets(context)
    updatePrayerTimesWidgets(context)
}

private fun updateHijriDateWidgets(context: Context) {
    //Update Widgets
    val widgetIntent = Intent(context, HijriDateWidget::class.java)
    widgetIntent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE

    // Use an array and EXTRA_APPWIDGET_IDS instead of AppWidgetManager.EXTRA_APPWIDGET_ID,
    // since it seems the onUpdate() is only fired on that:
    val ids = AppWidgetManager.getInstance(context)
        .getAppWidgetIds(ComponentName(context, HijriDateWidget::class.java))
    widgetIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
    context.sendBroadcast(widgetIntent)

}

private fun updateTodayRemindersWidgets(context: Context) {
    //Update Widgets
    val appWidgetManager = AppWidgetManager.getInstance(context)

    // Use an array and EXTRA_APPWIDGET_IDS instead of AppWidgetManager.EXTRA_APPWIDGET_ID,
    // since it seems the onUpdate() is only fired on that:
    val ids = AppWidgetManager.getInstance(context)
        .getAppWidgetIds(ComponentName(context, TodayToDosWidget::class.java))
    appWidgetManager.notifyAppWidgetViewDataChanged(ids, R.id.widgetListView)
}

private fun updatePrayerTimesWidgets(context: Context) {
    //Update Widgets
    val appWidgetManager = AppWidgetManager.getInstance(context)

    // Use an array and EXTRA_APPWIDGET_IDS instead of AppWidgetManager.EXTRA_APPWIDGET_ID,
    // since it seems the onUpdate() is only fired on that:
    val ids = AppWidgetManager.getInstance(context)
        .getAppWidgetIds(ComponentName(context, PrayerTimesWidget::class.java))
    appWidgetManager.notifyAppWidgetViewDataChanged(ids, R.id.widgetListView)

}


fun getLocale(): Locale {
    return if (supportedLocales.contains(Locale.getDefault().language))
        Locale.getDefault()
    else
        Locale("en")
}

fun isValidUrl(link: String): Boolean {
    return try {
        URL(link).toURI()
        true
    } catch (exception: Exception) {
        false
    }
}
