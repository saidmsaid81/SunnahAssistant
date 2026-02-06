@file:JvmName("SunnahAssistantUtil")

package com.thesunnahrevival.sunnahassistant.utilities

import android.appwidget.AppWidgetManager
import android.content.*
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import com.thesunnahrevival.sunnahassistant.BuildConfig
import com.thesunnahrevival.sunnahassistant.R
import com.thesunnahrevival.sunnahassistant.data.model.dto.FullAyahDetails
import com.thesunnahrevival.sunnahassistant.data.model.entity.AppSettings
import com.thesunnahrevival.sunnahassistant.data.model.entity.Frequency
import com.thesunnahrevival.sunnahassistant.data.model.entity.ToDo
import com.thesunnahrevival.sunnahassistant.data.model.entity.Translation
import com.thesunnahrevival.sunnahassistant.data.remote.UserAgentInterceptor
import com.thesunnahrevival.sunnahassistant.data.repositories.SunnahAssistantRepository
import com.thesunnahrevival.sunnahassistant.widgets.HijriDateWidget
import com.thesunnahrevival.sunnahassistant.widgets.PrayerTimesWidget
import com.thesunnahrevival.sunnahassistant.widgets.TodayToDosWidget
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.URL
import java.time.LocalDate
import java.util.*
import kotlin.math.pow
import kotlin.math.round


fun generateEmailIntent(): Intent {
    val intent = Intent(Intent.ACTION_SENDTO)
    intent.data = Uri.parse("mailto:")
    intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(SUPPORT_EMAIL))
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
    return if (SUPPORTED_LOCALES.contains(Locale.getDefault().language))
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

fun Int.toArabicNumbers(): String {
    val arabicChars = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
    return this.toString().map { char ->
        if (char.isDigit()) arabicChars[char.digitToInt()] else char
    }.joinToString("")
}

fun Float.roundTo(decimals: Int): Float {
    val multiplier = 10.0.pow(decimals).toFloat()
    return round(this * multiplier) / multiplier
}

fun CharSequence.extractNumber(): Int? {
    val numberString = this.takeWhile { it.isDigit() } // "15"
    return numberString.toString().toIntOrNull()
}

fun String.toAnnotatedString(): AnnotatedString {
    return buildAnnotatedString {
        append(this@toAnnotatedString)
    }
}

fun Context.getLocale(): Locale {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        this.resources.configuration.locales[0]
    } else {
        this.resources.configuration.locale
    }
}

private fun getAyahText(
    ayah: FullAyahDetails,
    selectedTranslations: List<Translation>,
    surahNumber: String
): String {
    val selectedTranslationIds = selectedTranslations.map { it.id }.toSet()

    val translations = ayah.ayahTranslations
        .filter { it.translation.id in selectedTranslationIds }
        .joinToString(separator = "") {
            "${it.translation.name} \n" +
                    "${it.ayahTranslation.text} \n\n"
        }

    return "${ayah.surah.transliteratedName} ($surahNumber)\n\n" +
            "Ayah ${ayah.ayah.number}\n" +
            "${ayah.ayah.arabicText}\n\n" +
            translations
}

fun copyToClipboard(
    context: Context,
    textToCopy: String,
    label: String,
    message: String
) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

    val clip = ClipData.newPlainText(
        label,
        textToCopy
    )
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}

fun shareText(
    context: Context,
    textToShare: String,
    title: String
) {
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(
            Intent.EXTRA_TEXT,
            textToShare
        )
    }
    context.startActivity(Intent.createChooser(shareIntent, title))
}

private val okHttpClient = OkHttpClient.Builder()
    .addInterceptor(UserAgentInterceptor(BuildConfig.VERSION_CODE.toString()))
    .build()

val retrofit: Retrofit = Retrofit.Builder()
    .baseUrl("https://api.thesunnahrevival.com/")
    .addConverterFactory(GsonConverterFactory.create())
    .client(okHttpClient)
    .build()

fun getPrayerTimes(context: Context, toDoRepository: SunnahAssistantRepository): List<ToDo> {
    val now = LocalDate.now()
    val categories = context.resources.getStringArray(R.array.categories)
    val prayerTimes = toDoRepository.getPrayerTimesValue(
        day = now.dayOfMonth,
        month = now.month.ordinal, // Repo expects 0-indexed months
        year = now.year,
        categoryName = categories.getOrNull(2).orEmpty()
    )
    return prayerTimes
}