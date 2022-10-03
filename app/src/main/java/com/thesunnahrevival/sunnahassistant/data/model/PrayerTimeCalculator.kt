package com.thesunnahrevival.sunnahassistant.data.model

import com.batoulapps.adhan.*
import com.batoulapps.adhan.data.DateComponents
import com.thesunnahrevival.sunnahassistant.utilities.getTimestampInSeconds
import java.lang.Integer.parseInt
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.*


class PrayerTimeCalculator(
    private val latitude: Double,
    private val longitude: Double,
    private val calculationMethod: CalculationMethod,
    private val asrCalculationMethod: Madhab,
    private val latitudeAdjustmentMethod: Int,
    private val prayerNames: Array<String>,
    private val categoryName: String
) {

    /**
     * @return [ArrayList] of prayer time [ToDo] for the given date based on [day], [month] and [year]
     * @param day is the day of the month
     * @param month should be 0 based. That is January is 0 and December is 11
     * @param year should be between 1970 and 2069
     * @param enablePrayerTimeAlertsFor [BooleanArray] of size 5 with index 0 being Fajr Prayer and ndex 4 being isha prayer
     * @param offsetInMinutes [IntArray] of size 5 with index 0 being Fajr Prayer and ndex 4 being isha prayer
     * @throws IllegalArgumentException
     */
    fun getPrayerTimeToDos(
        day: Int,
        month: Int,
        year: Int,
        enablePrayerTimeAlertsFor: BooleanArray,
        offsetInMinutes: IntArray
    ): ArrayList<ToDo> {
        when {
            year !in 1970..2069 ->
                throw IllegalArgumentException("Year should be between 1970 and 2069")
            month !in 0..11 ->
                throw IllegalArgumentException(
                    "Month should be from 0 to 11. 0 being January and 11 being December "
                )
            else -> {
                val lengthOfMonth = LocalDate.of(year, month + 1, 1).lengthOfMonth()
                if (day !in 1..lengthOfMonth) {
                    throw IllegalArgumentException(
                        "Invalid day. Day of Month for Month $month cannot be $day. " +
                                "Valid days are from 1 to $lengthOfMonth "
                    )
                }
            }
        }

        val toDos = arrayListOf<ToDo>()
        val prayerTimesStrings = prayerTimeStrings(day, month, year)

        for ((index, prayerTimeString) in prayerTimesStrings.withIndex()) {
            val isEnabled = enablePrayerTimeAlertsFor.getOrElse(index) { true }
            val offset = offsetInMinutes.getOrElse(index) { 0 }
            val toDo =
                createToDo(index, day, month, year, prayerTimeString, isEnabled, offset)
            toDos.add(toDo)

        }

        return toDos
    }

    private fun prayerTimeStrings(day: Int, month: Int, year: Int): Array<String> {
        val dateObject = GregorianCalendar(year, month, day).time
        val coordinates = Coordinates(latitude, longitude)
        val date = DateComponents.from(dateObject)

        val params = calculationMethod.parameters
        params.madhab = asrCalculationMethod
        params.highLatitudeRule = HighLatitudeRule.values()[latitudeAdjustmentMethod]

        val prayerTimes = PrayerTimes(coordinates, date, params)

        val formatter = SimpleDateFormat("HH:mm", Locale.ENGLISH)
        return arrayOf(
            formatter.format(prayerTimes.fajr),
            formatter.format(prayerTimes.dhuhr), formatter.format(prayerTimes.asr),
            formatter.format(prayerTimes.maghrib), formatter.format(prayerTimes.isha)
        )
    }

    private fun createToDo(
        index: Int,
        day: Int,
        month: Int,
        year: Int,
        prayerTimeString: String,
        isEnabled: Boolean,
        offsetInMinutes: Int
    ): ToDo {

        return ToDo(
            prayerNames[index],
            "",
            getTimestampInSeconds(prayerTimeString),
            categoryName,
            Frequency.Daily,
            isEnabled,
            day,
            month,
            year,
            id = parseInt("-$day$month$year$index"),
            offsetInMinutes = offsetInMinutes
        )
    }
}