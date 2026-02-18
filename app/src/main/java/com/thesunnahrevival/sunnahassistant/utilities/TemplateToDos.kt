package com.thesunnahrevival.sunnahassistant.utilities

import android.content.Context
import com.thesunnahrevival.sunnahassistant.R
import com.thesunnahrevival.sunnahassistant.data.model.entity.Frequency
import com.thesunnahrevival.sunnahassistant.data.model.entity.ToDo
import java.util.Calendar
import java.util.TreeSet




class TemplateToDos {
    /**
     * @return [MutableMap] of template to-dos where the key is the id of the [ToDo] and the value is a [Pair]
     * of the icon id ([Int]) and the [ToDo]
     */
    fun getTemplateToDos(context: Context): MutableMap<Int, Pair<Int, ToDo>> {
        val sunnah = context.resources.getStringArray(R.array.categories)[1]
        val uncategorized = context.resources.getStringArray(R.array.categories)[0]
        val toDoMap = mutableMapOf<Int, Pair<Int, ToDo>>()

        toDoMap[PRAYING_DHUHA_ID] = Pair(
            R.drawable.ic_dhuha,
            createReminder(
                name = context.getString(R.string.dhuha_prayer),
                frequency = Frequency.Daily,
                category = sunnah,
                id = PRAYING_DHUHA_ID,
                predefinedReminderInfo = context.getString(R.string.read_more_on_dhuha_prayer),
                predefinedReminderLink = "https://thesunnahrevival.com/2015/11/18/sunnah-of-the-weekduha-prayer-its-importance-and-practical-tips"
            )
        )


        toDoMap[READING_MORNING_ADHKAAR_ID] = Pair(
            R.drawable.ic_dua,
            createReminder(
                id = READING_MORNING_ADHKAAR_ID,
                name = context.getString(R.string.morning_adhkar),
                category = sunnah,
                frequency = Frequency.Daily,
                predefinedReminderInfo = context.getString(R.string.tap_here_to_read_more_on_morning_adhkaar),
                predefinedReminderLink = "https://islamqa.info/en/answers/217496/morning-and-evening-adhkar"
            )
        )


        toDoMap[READING_EVENING_ADHKAAR_ID] = Pair(
            R.drawable.ic_dua,
            createReminder(
                id = READING_EVENING_ADHKAAR_ID,
                name = context.getString(R.string.evening_adhkar),
                category = sunnah,
                frequency = Frequency.Daily,
                predefinedReminderInfo = context.getString(R.string.tap_here_to_read_more_on_evening_adhkaar),
                predefinedReminderLink = "https://islamqa.info/en/answers/217496/morning-and-evening-adhkar"
            )
        )


        toDoMap[TAHAJJUD_ID] = Pair(
            R.drawable.ic_tahajjud,
            createReminder(
                id = TAHAJJUD_ID,
                name = context.getString(R.string.tahajjud),
                category = sunnah,
                frequency = Frequency.Daily,
                predefinedReminderInfo = context.getString(R.string.read_more_on_tahajjud_prayer),
                predefinedReminderLink = "https://thesunnahrevival.com/2014/04/09/tahajjud"
            )
        )


        toDoMap[READING_QURAN_ID] = Pair(
            R.drawable.ic_quran,
            createReminder(
                id = READING_QURAN_ID,
                name = context.getString(R.string.reading_the_quran),
                category = sunnah,
                frequency = Frequency.Daily,
                predefinedReminderInfo = context.getString(R.string.tap_here_to_read_more_on_quran),
                predefinedReminderLink = "https://thesunnahrevival.com/2020/06/07/virtues-of-reading-quran/"
            )
        )

        var listOfDays = TreeSet<Int>()
        listOfDays.add(Calendar.FRIDAY)
        toDoMap[READING_SURATUL_KAHF_ID] = Pair(
            R.drawable.ic_quran,
            createReminder(
                id = READING_SURATUL_KAHF_ID,
                name = context.getString(R.string.reading_suratul_kahf),
                category = sunnah,
                frequency = Frequency.Weekly,
                customScheduleList = listOfDays,
                predefinedReminderInfo = context.getString(R.string.read_more_on_suratul_kahf),
                predefinedReminderLink = "https://thesunnahrevival.com/2020/03/06/2769"
            )
        )


        listOfDays = TreeSet<Int>()
        listOfDays.add(Calendar.SUNDAY)
        listOfDays.add(Calendar.WEDNESDAY)
        toDoMap[FASTING_MONDAYS_THURSDAYS_ID] = Pair(
            R.drawable.ic_fasting,
            createReminder(
                id = FASTING_MONDAYS_THURSDAYS_ID,
                name = context.getString(R.string.fasting_on_monday_and_thursday),
                category = sunnah,
                frequency = Frequency.Weekly,
                customScheduleList = listOfDays,
                predefinedReminderInfo = context.getString(R.string.read_more_on_fasting_mondays_and_thursdays),
                predefinedReminderLink = "https://thesunnahrevival.com/2016/01/06/revive-a-sunnah-fasting-on-monday-and-thursday"
            )
        )

        toDoMap[READING_SURATUL_MULK_ID] = Pair(
            R.drawable.ic_quran,
            createReminder(
                id = READING_SURATUL_MULK_ID,
                name = context.getString(R.string.reading_suratul_mulk),
                category = sunnah,
                frequency = Frequency.Daily,
                predefinedReminderInfo = context.getString(R.string.read_more_on_suratul_mulk),
                predefinedReminderLink = "https://thesunnahrevival.com/2016/04/23/revive-a-sunnah-series-reciting-surah-al-mulk-and-al-sajdah-every-night/"
            )
        )

        toDoMap[READING_ID] = Pair(
            R.drawable.ic_read,
            createReminder(
                id = READING_ID,
                name = context.getString(R.string.reading),
                category = uncategorized,
                frequency = Frequency.Daily
            )
        )

        toDoMap[READING_SLEEPING_ADHKAAR_ID] = Pair(
            R.drawable.ic_sleep,
            createReminder(
                id = READING_SLEEPING_ADHKAAR_ID,
                name = context.getString(R.string.sleeping),
                category = uncategorized,
                frequency = Frequency.Daily
            )
        )
        toDoMap[EXERCISE_ID] = Pair(
            R.drawable.ic_exercise,
            createReminder(
                id = EXERCISE_ID,
                name = context.getString(R.string.exercising),
                category = uncategorized,
                frequency = Frequency.Daily
            )
        )
        toDoMap[PILL_ID] = Pair(
            R.drawable.ic_pill_reminder,
            createReminder(
                id = PILL_ID,
                name = context.getString(R.string.pill),
                category = uncategorized,
                frequency = Frequency.Daily
            )
        )
        toDoMap[DRINK_WATER_ID] = Pair(
            R.drawable.ic_water,
            createReminder(
                id = DRINK_WATER_ID,
                name = context.getString(R.string.drink_water),
                category = uncategorized,
                frequency = Frequency.Daily
            )
        )
        return toDoMap
    }

    private fun createReminder(
        name: String,
        frequency: Frequency,
        category: String,
        customScheduleList: TreeSet<Int> = TreeSet(),
        id: Int = 0,
        predefinedReminderInfo: String = "",
        predefinedReminderLink: String = ""
    ): ToDo {
        val day = if (frequency == Frequency.Daily) 0 else -1
        return ToDo(
            name = name, predefinedToDoInfo = predefinedReminderInfo,
            predefinedToDoLink = predefinedReminderLink,
            category = category, frequency = frequency, isReminderEnabled = false,
            id = id, customScheduleDays = customScheduleList, day = day, month = 12, year = 0
        )
    }
}