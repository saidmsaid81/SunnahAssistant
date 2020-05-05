package com.thesunnahrevival.sunnahassistant.data.model

import com.batoulapps.adhan.*
import com.batoulapps.adhan.data.DateComponents
import com.thesunnahrevival.sunnahassistant.utilities.SunnahAssistantUtil.DAILY
import com.thesunnahrevival.sunnahassistant.utilities.SunnahAssistantUtil.PRAYER
import com.thesunnahrevival.sunnahassistant.utilities.TimeDateUtil
import java.lang.Integer.parseInt
import java.text.SimpleDateFormat
import java.util.*


class PrayerTimeCalculator(private val latitude :Double, private val longitude :Double, private val calculationMethod :Int, private val asrCalculationMethod:Int, private val latitudeAdjustmentMethod :Int   ) {
    private val prayerNames = arrayOf("Fajr Prayer", "Dhuhr Prayer", "Asr Prayer", "Maghrib Prayer", "Isha Prayer")

    fun getPrayerTimeReminders(): ArrayList<Reminder>
    {
        val reminders = arrayListOf<Reminder>()
        for (day in 1..TimeDateUtil.getLastDayOfMonth()){
            val prayerTimesStrings = prayerTimeStrings(day)

            for ((index, prayerTimeString) in prayerTimesStrings.withIndex()){
                val reminder = createReminder(index, day, prayerTimeString)
                reminders.add(reminder)
            }
        }

        return reminders
    }

    private fun prayerTimeStrings(day: Int): Array<String> {
        val dateObject  = GregorianCalendar(TimeDateUtil.getYear(System.currentTimeMillis()).toInt(), TimeDateUtil.getMonthNumber(System.currentTimeMillis()), day).time
        println(day)
        println(dateObject.time)
        val coordinates = Coordinates(latitude, longitude)
        val date: DateComponents = DateComponents.from(dateObject)
        val params: CalculationParameters = CalculationMethod.values()[calculationMethod].parameters
        params.madhab = Madhab.values()[asrCalculationMethod]
        params.highLatitudeRule = HighLatitudeRule.values()[latitudeAdjustmentMethod]
        val prayerTimes = PrayerTimes(coordinates, date, params)
        val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
        return arrayOf(formatter.format(prayerTimes.fajr),
                formatter.format(prayerTimes.dhuhr), formatter.format(prayerTimes.asr),
                formatter.format(prayerTimes.maghrib), formatter.format(prayerTimes.isha))
    }

    private fun createReminder(index: Int, day :Int, prayerTimeString: String): Reminder {
        val reminder = Reminder(
                prayerNames[index],
                "",
                TimeDateUtil.getTimestampInSeconds(prayerTimeString),
                PRAYER,
                DAILY,
                false,
                day,
                TimeDateUtil.getMonthNumber(System.currentTimeMillis()),
                TimeDateUtil.getYear(System.currentTimeMillis()).toInt(),
                0,
                null
        )
        val id = "-$day$index"
        reminder.id = parseInt(id)
        return reminder
    }
}