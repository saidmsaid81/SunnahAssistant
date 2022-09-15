@file:JvmName("TimeDateUtil")

package com.thesunnahrevival.common.utilities

import android.content.Context
import android.text.format.DateFormat
import android.util.Log
import com.github.msarhan.ummalqura.calendar.UmmalquraCalendar
import com.thesunnahrevival.common.R
import com.thesunnahrevival.common.data.model.Reminder
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

val lastDayOfMonth: Int
    get() {
        val calendar = Calendar.getInstance()
        return calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    }

val dayOfTheWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)

val daySuffixes = arrayOf(
    "0th", "1st", "2nd", "3rd", "4th", "5th", "6th", "7th", "8th", "9th",
    "10th", "11th", "12th", "13th", "14th", "15th", "16th", "17th", "18th", "19th",
    "20th", "21st", "22nd", "23rd", "24th", "25th", "26th", "27th", "28th", "29th",
    "30th", "31st"
)

val tomorrowDayOfTheWeek =
    if (dayOfTheWeek == 7)
        1
    else
        Calendar.getInstance().get(Calendar.DAY_OF_WEEK) + 1

fun getDayDate(timeInMilliseconds: Long): Int {
    return SimpleDateFormat("dd", Locale.ENGLISH).format(timeInMilliseconds).toInt()
}

fun getMonthNumber(timeInMilliseconds: Long): Int {
    return SimpleDateFormat("MM", Locale.ENGLISH).format(timeInMilliseconds).toInt() - 1
}

fun getYear(timeInMilliseconds: Long): String {
    return SimpleDateFormat("yyyy", Locale.ENGLISH).format(timeInMilliseconds)
}

fun formatTimeInMilliseconds(context: Context?, timeInMilliseconds: Long): String {
    if (timeInMilliseconds != 172800000L) {
        val sdf = if (DateFormat.is24HourFormat(context))
            SimpleDateFormat("HH:mm", getLocale())
        else
            SimpleDateFormat("hh:mm a", getLocale())
        return sdf.format(timeInMilliseconds)
    }
    return context?.getString(R.string.time_not_set) ?: "Time Not Set"
}

fun isReminderDisabled(context: Context?, reminder: Reminder?): Boolean {
    return if (reminder != null) {
        val timeInMilliseconds = formatTimeInMilliseconds(context, reminder.timeInMilliseconds)
        return if (timeInMilliseconds == context?.getString(R.string.time_not_set)) false
        else !reminder.isEnabled
    } else
        true
}

fun calculateOffsetFromMidnight(): Long {
    val simpleDateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    return try {
        (simpleDateFormat.parse(simpleDateFormat.format(Date()))?.time ?: 0) / 1000
    } catch (e: ParseException) {
        0
    }
}

fun getTimestampInSeconds(context: Context, timeString: String?): Long {
    val format = if (DateFormat.is24HourFormat(context))
        SimpleDateFormat("HH:mm", getLocale())
    else
        SimpleDateFormat("hh:mm a", getLocale())
    return try {
        val date = format.parse(timeString ?: "")
        (date?.time ?: 0) / 1000
    } catch (e: ParseException) {
        Log.v("ParseException", e.message.toString())
        172800
    }
}

fun getTimestampInSeconds(timeString: String?): Long {
    val format =
        if (timeString?.contains("am".toRegex()) == false && !timeString.contains("pm".toRegex()))
            SimpleDateFormat("HH:mm", Locale.ENGLISH)
        else
            SimpleDateFormat("hh:mm a", Locale.ENGLISH)
    return try {
        val date = format.parse(timeString ?: "null")
        (date?.time ?: 0) / 1000
    } catch (e: ParseException) {
        Log.v("ParseException", e.message ?: "error")
        172800
    }
}

fun generateDateText(
    gregorianCalendar: GregorianCalendar = GregorianCalendar(),
    isOnlyHijriDate: Boolean = false
): String {
    val stringBuilder = StringBuilder()
    if (!isOnlyHijriDate) {
        val simpleDateFormat = SimpleDateFormat("EEEE dd MMMM, yyyy", getLocale())
        stringBuilder.append("${simpleDateFormat.format(gregorianCalendar.time)} / ")
    }

    val ummalquraCalendar = UmmalquraCalendar()
    ummalquraCalendar.time = gregorianCalendar.time
    val hijriDateFormat = SimpleDateFormat("", getLocale())
    hijriDateFormat.calendar = ummalquraCalendar
    if (isOnlyHijriDate)
        hijriDateFormat.applyPattern("EEEE dd")
    else
        hijriDateFormat.applyPattern("dd")
    val hijriDay = hijriDateFormat.format(ummalquraCalendar.time)
    val month = ummalquraCalendar.getHijriMonthName()
    val year = hijriDateFormat.apply {
        applyPattern("y")
    }.format(ummalquraCalendar.time)

    stringBuilder.append("$hijriDay $month, $year")

    return stringBuilder.toString()
}

fun UmmalquraCalendar.getHijriMonthName(): String {
    val hijriMonthName = this.getDisplayName(Calendar.MONTH, Calendar.LONG, getLocale()) ?: ""
    if (getLocale().language.contains("en")) {
        if (hijriMonthName.contains("Thul-Qi'dah")) {
            return "Dhul-Qa'dah"
        } else if (hijriMonthName.contains("Thul-Hijjah")) {
            return "Dhul-Hijjah"
        }
    }
    return hijriMonthName
}



