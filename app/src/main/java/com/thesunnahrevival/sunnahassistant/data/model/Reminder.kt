package com.thesunnahrevival.sunnahassistant.data.model

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "reminders_table")
data class Reminder(
        var reminderName: String?,
        var reminderInfo: String? = "",
        var timeInSeconds: Long = 172800,
        var category: String?,
        var frequency: Frequency?,
        var isEnabled: Boolean = false,
        var day: Int = if (frequency?.ordinal == 1) 0 else -1,
        var month: Int = 12,
        var year: Int = 0,
        var offset: Int = 0,
        @PrimaryKey(autoGenerate = true) var id: Int = 0,
        var customScheduleDays: ArrayList<Int?>? = ArrayList()
) {

    @Ignore
    var timeInMilliseconds :Long = timeInSeconds * 1000
}

enum class Frequency {
    OneTime, Daily, Weekly, Monthly
}