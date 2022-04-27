@file:JvmName("SunnahAssistantUtil")

package com.thesunnahrevival.sunnahassistant.utilities

import android.appwidget.AppWidgetManager
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.net.Uri
import android.os.Build
import com.thesunnahrevival.sunnahassistant.BuildConfig
import com.thesunnahrevival.sunnahassistant.R
import com.thesunnahrevival.sunnahassistant.data.model.AppSettings
import com.thesunnahrevival.sunnahassistant.data.model.Frequency
import com.thesunnahrevival.sunnahassistant.data.model.Reminder
import com.thesunnahrevival.sunnahassistant.widgets.HijriDateWidget
import com.thesunnahrevival.sunnahassistant.widgets.TodayRemindersWidget
import java.util.*

val supportedLocales = arrayOf("en", "ar")

fun generateEmailIntent(): Intent {
    val intent = Intent(Intent.ACTION_SENDTO)
    intent.data = Uri.parse("mailto:")
    intent.putExtra(Intent.EXTRA_EMAIL, arrayOf("thesunnahrevival.tsr@gmail.com"))
    intent.putExtra(Intent.EXTRA_SUBJECT, "Sunnah Assistant App" + " - Version " + BuildConfig.VERSION_NAME)
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
        Device Language: ${Resources.getSystem().configuration.locale.language}
        """.trimIndent()

fun openPlayStore(context: Context, appPackageName: String) {
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$appPackageName")))
    } catch (e: ActivityNotFoundException) {
        context.startActivity(
                Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")))
    }
}

fun openDeveloperPage(context: Context) {
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://dev?id=6919675665650793025")))
    } catch (e: ActivityNotFoundException) {
        context.startActivity(Intent(Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/dev?id=6919675665650793025")))
    }
}

fun sunnahReminders(context: Context): ArrayList<Reminder> {
    val sunnah = context.resources.getStringArray(R.array.categories)[1]
    val listOfReminders = ArrayList<Reminder>()
    listOfReminders.add(createReminder(name = context.getString(R.string.dhuha_prayer), frequency = Frequency.Daily, category = sunnah, id =  -1001,
            info = "<a href=\"https://thesunnahrevival.wordpress.com/2015/11/18/sunnah-of-the-weekduha-prayer-its-importance-and-practical-tips\"> ${context.getString(R.string.read_more) }</a> ${context.getString(R.string.read_more_on_dhuha_prayer)}"))

    listOfReminders.add(
            createReminder(id = -1002, name = context.getString(R.string.morning_adhkar), category = sunnah, frequency = Frequency.Daily))

    listOfReminders.add(
            createReminder(id = -1003, name = context.getString(R.string.evening_adhkar), category = sunnah, frequency = Frequency.Daily))

    listOfReminders.add(
            createReminder(id = -1004, name = context.getString(R.string.tahajjud), category = sunnah, frequency = Frequency.Daily,
                    info = "<a href=\"https://thesunnahrevival.wordpress.com/2014/04/09/tahajjud/\">${context.getString(R.string.read_more) }</a> ${context.getString(R.string.read_more_on_tahajjud_prayer)}"))

    listOfReminders.add(
            createReminder(id  = -1005, name = context.getString(R.string.reading_the_quran), category = sunnah, frequency = Frequency.Daily))

    var listOfDays = ArrayList<Int?>()
    listOfDays.add(Calendar.FRIDAY)
    listOfReminders.add(
            createReminder(id = -1006, name = context.getString(R.string.reading_suratul_kahf), category = sunnah,
                    frequency = Frequency.Weekly, customScheduleList = listOfDays,
                    info = "<a href=\"https://thesunnahrevival.wordpress.com/2020/03/06/2769/\"> ${context.getString(R.string.read_more) }</a> ${context.getString(R.string.read_more_on_suratul_kahf)}"))

    listOfDays = ArrayList()
    listOfDays.add(Calendar.SUNDAY)
    listOfDays.add(Calendar.WEDNESDAY)
    listOfReminders.add(
            createReminder(id = -1007, name =  context.getString(R.string.fasting_on_monday_and_thursday), category = sunnah,
                    frequency = Frequency.Weekly, customScheduleList =  listOfDays,
                    info = "<a href=\"https://thesunnahrevival.wordpress.com/2016/01/06/revive-a-sunnah-fasting-on-monday-and-thursday/\">${context.getString(R.string.read_more) }</a> ${context.getString(R.string.read_more_on_fasting_mondays_and_thursdays)}"))
    return listOfReminders
}

fun createReminder(name: String, frequency: Frequency, category: String, customScheduleList: ArrayList<Int?> = ArrayList(), id: Int = 0, info: String = ""): Reminder {
    return Reminder(reminderName = name, reminderInfo = info,
            category = category, frequency =  frequency, isEnabled =  false,
            id = id, customScheduleDays = customScheduleList)
}

fun demoReminder(name: String, category: String): Reminder {
    return createReminder(name, Frequency.Daily, category)
}

fun initialSettings(categories: TreeSet<String>): AppSettings {
    val appSettings = AppSettings()
    appSettings.categories = categories
    return appSettings
}


fun updateHijriDateWidgets(context: Context?) {
    if (context != null){
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
}

fun updateTodayRemindersWidgets(context: Context?) {
    if (context != null){
        //Update Widgets
        val appWidgetManager = AppWidgetManager.getInstance(context)

        // Use an array and EXTRA_APPWIDGET_IDS instead of AppWidgetManager.EXTRA_APPWIDGET_ID,
        // since it seems the onUpdate() is only fired on that:
        val ids = AppWidgetManager.getInstance(context)
            .getAppWidgetIds(ComponentName(context, TodayRemindersWidget::class.java))
        appWidgetManager.notifyAppWidgetViewDataChanged(ids, R.id.widgetListView)
    }

}


fun getLocale(): Locale {
    return if (supportedLocales.contains(Locale.getDefault().language))
        Locale.getDefault()
    else
        Locale("en")
}
