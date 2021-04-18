package com.thesunnahrevival.sunnahassistant.data.model

import com.batoulapps.adhan.*
import com.batoulapps.adhan.data.DateComponents
import com.thesunnahrevival.sunnahassistant.utilities.getMonthNumber
import com.thesunnahrevival.sunnahassistant.utilities.getTimestampInSeconds
import com.thesunnahrevival.sunnahassistant.utilities.getYear
import com.thesunnahrevival.sunnahassistant.utilities.lastDayOfMonth
import java.lang.Integer.parseInt
import java.text.SimpleDateFormat
import java.util.*


class PrayerTimeCalculator(
        private val latitude: Double, private val longitude: Double,
        private val calculationMethod: CalculationMethod, private val asrCalculationMethod: Madhab,
        private val latitudeAdjustmentMethod: Int, private val prayerNames: Array<String>, private val categoryName: String) {

    fun getPrayerTimeReminders(): ArrayList<Reminder>
    {
        val reminders = arrayListOf<Reminder>()
        for (day in 1..lastDayOfMonth){
            val prayerTimesStrings = prayerTimeStrings(day)

            for ((index, prayerTimeString) in prayerTimesStrings.withIndex()){
                val reminder = createReminder(index, day, prayerTimeString)
                reminders.add(reminder)
            }
        }

        return reminders
    }

    private fun prayerTimeStrings(day: Int): Array<String> {
        val dateObject  = GregorianCalendar(getYear(System.currentTimeMillis()).toInt(),
                getMonthNumber(System.currentTimeMillis()), day).time
        val coordinates = Coordinates(latitude, longitude)
        val date: DateComponents = DateComponents.from(dateObject)
        val params: CalculationParameters = calculationMethod.parameters
        params.madhab = asrCalculationMethod
        params.highLatitudeRule = HighLatitudeRule.values()[latitudeAdjustmentMethod]
        val prayerTimes = PrayerTimes(coordinates, date, params)
        val formatter = SimpleDateFormat("HH:mm", Locale.ENGLISH)
        return arrayOf(formatter.format(prayerTimes.fajr),
                formatter.format(prayerTimes.dhuhr), formatter.format(prayerTimes.asr),
                formatter.format(prayerTimes.maghrib), formatter.format(prayerTimes.isha))
    }

    private fun createReminder(index: Int, day :Int, prayerTimeString: String): Reminder {
        val reminder = Reminder(
                prayerNames[index],
                "",
                getTimestampInSeconds(prayerTimeString),
                categoryName,
                Frequency.Daily,
                false,
                day,
                getMonthNumber(System.currentTimeMillis()),
                getYear(System.currentTimeMillis()).toInt()
        )
        val id = "-$day$index"
        reminder.id = parseInt(id)
        return reminder
    }
}