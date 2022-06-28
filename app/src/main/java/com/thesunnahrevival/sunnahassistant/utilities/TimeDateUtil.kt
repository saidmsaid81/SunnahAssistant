@file:JvmName("TimeDateUtil")
package com.thesunnahrevival.sunnahassistant.utilities

import android.content.Context
import android.text.format.DateFormat
import android.util.Log
import com.github.msarhan.ummalqura.calendar.UmmalquraCalendar
import com.thesunnahrevival.sunnahassistant.R
import java.text.DateFormatSymbols
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

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

fun calculateOffsetFromMidnight(): Long {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    return try {
        sdf.parse(sdf.format(Date())).time / 1000
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
        val date = format.parse(timeString)
        date.time / 1000
    } catch (e: ParseException) {
        Log.v("ParseException", e.message.toString())
        172800
    }
}

fun getTimestampInSeconds(timeString: String?): Long {
    val format = if (timeString?.contains("am".toRegex()) == false && !timeString.contains("pm".toRegex()))
        SimpleDateFormat("HH:mm", Locale.ENGLISH)
    else
        SimpleDateFormat("hh:mm a", Locale.ENGLISH)
    return try {
        val date = format.parse(timeString ?: "null")
        date.time / 1000
    } catch (e: ParseException) {
        Log.v("ParseException", e.message ?: "error")
        172800
    }
}


fun formatDaysFromCustomScheduledDays(customScheduleDays: ArrayList<Int?>?): String {
    if (customScheduleDays != null)
    {
        val weekdays = DateFormatSymbols.getInstance(getLocale()).shortWeekdays
        val days = StringBuilder()
        for ((index, number) in customScheduleDays.withIndex()) {
            if (number != null) {
                days.append(weekdays[number])
                if (index != customScheduleDays.size - 1)
                    days.append(", ")
                else
                    days.append(" ")
            }
        }
        return days.toString()
    }
    return ""
}


fun getMonthName(month: Int): String {
    return DateFormatSymbols().months[month]
}

val lastDayOfMonth: Int
    get() {
        val calendar = Calendar.getInstance()
        return calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    }


val hijriDate: String
    get() {
        val uCal =  UmmalquraCalendar()
        val dateFormat = SimpleDateFormat("", getLocale())
        dateFormat.calendar = uCal
        dateFormat.applyPattern("EEEE d")
        val day = dateFormat.format(uCal.time)

        dateFormat.applyPattern("y")
        val year = dateFormat.format(uCal.time)
        return "$day ${uCal.getDisplayName(Calendar.MONTH, Calendar.LONG, getLocale())}, $year"
    }

val dayOfTheWeek =  Calendar.getInstance().get(Calendar.DAY_OF_WEEK)

val tomorrowDayOfTheWeek =
        if (dayOfTheWeek == 7)
            1
        else
        Calendar.getInstance().get(Calendar.DAY_OF_WEEK) + 1