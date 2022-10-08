package com.thesunnahrevival.sunnahassistant.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.util.*

@Entity(tableName = "reminders_table")
data class ToDo(
    @ColumnInfo(name = "reminderName") var name: String?,
    @ColumnInfo(name = "reminderInfo") var additionalInfo: String? = "",
    var timeInSeconds: Long = 172800,
    var category: String?,
    var frequency: Frequency?,
    @ColumnInfo(name = "isEnabled") var isReminderEnabled: Boolean = false,
    var day: Int,
    var month: Int,
    var year: Int,
    @ColumnInfo(name = "offset") var offsetInMinutes: Int = 0,
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    var customScheduleDays: TreeSet<Int>? = TreeSet(),
    var completedDates: TreeSet<String> = TreeSet(),
    @ColumnInfo(name = "predefinedReminderInfo") var predefinedToDoInfo: String = "",
    @ColumnInfo(name = "predefinedReminderLink") var predefinedToDoLink: String = "",
    var repeatsFromDate: String = ""
) {

    @Ignore
    var timeInMilliseconds: Long = timeInSeconds * 1000

    init {
        if (name?.isBlank() == true)
            name = null
        if (additionalInfo == null)
            additionalInfo = ""
        if (category?.isBlank() == true)
            category = null
        if (frequency == null) {
            frequency = Frequency.OneTime
        }
        if (id > 0) { //User defined To-Do
            predefinedToDoInfo = ""
            predefinedToDoLink = ""
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
                day = -1 // day = 0 is reserved for non automatic prayer time daily to-dos
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
            if (id == 0 || repeatsFromDate.isBlank())
                repeatsFromDate = LocalDate.now().toString()
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

    fun isAutomaticPrayerTime() = this.id <= -1019700

    fun isComplete(date: LocalDate): Boolean {
        return completedDates.contains(date.toString())
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ToDo

        if (name != other.name) return false
        if (additionalInfo != other.additionalInfo) return false
        if (timeInSeconds != other.timeInSeconds) return false
        if (category != other.category) return false
        if (frequency != other.frequency) return false
        if (isReminderEnabled != other.isReminderEnabled) return false
        if (day != other.day) return false
        if (month != other.month) return false
        if (year != other.year) return false
        if (offsetInMinutes != other.offsetInMinutes) return false
        if (id != other.id) return false
        if (customScheduleDays != other.customScheduleDays) return false
        if (completedDates != other.completedDates) return false
        if (predefinedToDoInfo != other.predefinedToDoInfo) return false
        if (predefinedToDoLink != other.predefinedToDoLink) return false
        if (timeInMilliseconds != other.timeInMilliseconds) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name?.hashCode() ?: 0
        result = 31 * result + (additionalInfo?.hashCode() ?: 0)
        result = 31 * result + timeInSeconds.hashCode()
        result = 31 * result + (category?.hashCode() ?: 0)
        result = 31 * result + (frequency?.hashCode() ?: 0)
        result = 31 * result + isReminderEnabled.hashCode()
        result = 31 * result + day
        result = 31 * result + month
        result = 31 * result + year
        result = 31 * result + offsetInMinutes
        result = 31 * result + id
        result = 31 * result + (customScheduleDays?.hashCode() ?: 0)
        result = 31 * result + completedDates.hashCode()
        result = 31 * result + predefinedToDoInfo.hashCode()
        result = 31 * result + predefinedToDoLink.hashCode()
        result = 31 * result + timeInMilliseconds.hashCode()
        return result
    }


}

enum class Frequency {
    OneTime, Daily, Weekly, Monthly
}