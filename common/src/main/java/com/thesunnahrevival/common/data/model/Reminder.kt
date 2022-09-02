package com.thesunnahrevival.common.data.model

import androidx.recyclerview.widget.DiffUtil
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.util.*

@Entity(tableName = "reminders_table")
data class Reminder(
    var reminderName: String?,
    var reminderInfo: String? = "",
    var timeInSeconds: Long = 172800,
    var category: String?,
    var frequency: Frequency?,
    var isEnabled: Boolean = false,
    var day: Int,
    var month: Int,
    var year: Int,
    var offsetInMinutes: Int = 0,
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    var customScheduleDays: TreeSet<Int>? = TreeSet(),
    var isComplete: Boolean = false
) {

    @Ignore
    var timeInMilliseconds: Long = timeInSeconds * 1000

    init {
        if (reminderName?.isBlank() == true)
            reminderName = null
        if (reminderInfo == null)
            reminderInfo = ""
        if (category?.isBlank() == true)
            category = null
        if (frequency == null) {
            frequency = Frequency.OneTime
        }

        when (frequency) {
            Frequency.OneTime -> {
                if (month !in 0..11)
                    throw IllegalArgumentException(
                        "Month should be from 0 to 11. " +
                                "0 being January and 11 being December "
                    )
                if (year < 1)
                    throw IllegalArgumentException("Year should not be less than 1.")

                val lengthOfMonth =
                    LocalDate.of(year, month + 1, 1).lengthOfMonth()

                if (day !in 1..lengthOfMonth)
                    throw IllegalArgumentException(
                        "Invalid day. Day of Month for Month ${month + 1} cannot be $day. " +
                                "Valid days are from 1 to $lengthOfMonth "
                    )
            }
            Frequency.Monthly -> {
                if (day !in 1..31)
                    throw IllegalArgumentException(
                        "Invalid day. Day of the Month should be from 1 to 31"
                    )
            }
            Frequency.Weekly -> {
                day = -1 // day = 0 is reserved for non automatic prayer time daily reminders
            }
            Frequency.Daily -> {
                if (!isAutomaticPrayerTime())
                    day = 0
            }
            else -> {}
        }

        if (frequency != Frequency.OneTime && !isAutomaticPrayerTime()) {
            month = 12
            year = 0
        }

        if (frequency != Frequency.Weekly)
            customScheduleDays = null
        else {
            if (customScheduleDays?.isEmpty() == true)
                throw IllegalArgumentException(
                    "Please select at least one day"
                )

        }
    }

    fun isAutomaticPrayerTime() = this.id in -1 downTo -999
}

class RemindersDiffCallback(
    private val oldRemindersList: List<Reminder>,
    private val newRemindersList: List<Reminder>
) : DiffUtil.Callback() {
    override fun getOldListSize(): Int = oldRemindersList.size

    override fun getNewListSize(): Int = newRemindersList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldRemindersList[oldItemPosition].id == newRemindersList[newItemPosition].id
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldRemindersList[oldItemPosition] === newRemindersList[newItemPosition]
    }

}

enum class Frequency {
    OneTime, Daily, Weekly, Monthly
}