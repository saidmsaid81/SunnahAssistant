package com.thesunnahrevival.common.data.local

import androidx.paging.PagingSource
import androidx.room.*
import com.thesunnahrevival.common.data.model.AppSettings
import com.thesunnahrevival.common.data.model.Reminder
import com.thesunnahrevival.common.data.model.ReminderDate
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: Reminder): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertRemindersList(remindersList: List<Reminder>)

    @Query("UPDATE reminders_table SET reminderName =:name, reminderInfo =:info, category =:category WHERE id =:id")
    suspend fun updateReminder(id: Int, name: String, info: String, category: String)

    @Delete
    suspend fun deleteReminder(reminder: Reminder)

    @Query(
        "SELECT EXISTS (SELECT * FROM reminders_table WHERE (" +
                "category != :excludeCategory " +
                "AND ((day == :day AND month == :month AND year == :year) OR (day == :day AND month == 12 AND year == 0) OR day == 0 OR customScheduleDays LIKE '%' || :numberOfTheWeekDay || '%')) LIMIT 1)"
    )
    fun thereRemindersOnDay(
        excludeCategory: String,
        numberOfTheWeekDay: String,
        day: Int,
        month: Int,
        year: Int
    ): Boolean

    @Query("SELECT * FROM reminders_table WHERE ((day == :day AND month == :month AND year == :year) OR (day == :day AND month == 12 AND year == 0) OR day == 0 OR customScheduleDays LIKE '%' || :numberOfTheWeekDay || '%') AND (category LIKE '%' || :category || '%') ORDER BY isComplete, timeInSeconds")
    fun getRemindersOnDay(
        numberOfTheWeekDay: String,
        day: Int,
        month: Int,
        year: Int,
        category: String
    ): PagingSource<Int, Reminder>

    @Query("SELECT * FROM reminders_table WHERE ((day == :day AND month == :month AND year == :year) OR (day == :day AND month == 12 AND year == 0) OR day == 0 OR customScheduleDays LIKE '%' || :numberOfTheWeekDay || '%') AND isEnabled ORDER BY timeInSeconds")
    fun getRemindersOnDayValue(
        numberOfTheWeekDay: String,
        day: Int,
        month: Int,
        year: Int
    ): List<Reminder>

    @Query("SELECT (timeInSeconds + (offsetInMinutes * 60)) AS time FROM reminders_table WHERE time > :offsetFromMidnight AND ((day == :day AND month == :month AND year == :year) OR (day == :day AND month == 12 AND year == 0) OR day == 0 OR customScheduleDays LIKE '%' || :numberOfTheWeekDay || '%') AND isEnabled ORDER BY time")
    suspend fun getNextTimeForReminderForDay(
        offsetFromMidnight: Long,
        numberOfTheWeekDay: String,
        day: Int,
        month: Int,
        year: Int
    ): Long?


    @Query("SELECT * FROM reminders_table WHERE (timeInSeconds + (offsetInMinutes * 60)) == :timeForReminder AND ((day == :day AND month == :month AND year == :year) OR (day == :day AND month == 12 AND year == 0) OR day == 0 OR customScheduleDays LIKE '%' || :numberOfTheWeekDay || '%') AND isEnabled ORDER BY (timeInSeconds + (offsetInMinutes * 60))")
    suspend fun getNextScheduledRemindersForDay(
        timeForReminder: Long,
        numberOfTheWeekDay: String,
        day: Int,
        month: Int,
        year: Int
    ): List<Reminder>

    @Query(
        "SELECT EXISTS " +
                "(SELECT * FROM reminders_table WHERE category == :prayerCategory AND " +
                "id LIKE '-' || :id || '%')"
    )
    fun therePrayerRemindersOnDay(prayerCategory: String, id: String): Boolean

    @Query("SELECT * FROM reminders_table WHERE (category ==:categoryName AND (day == :day AND month == :month AND year =:year)) ORDER BY timeInSeconds")
    fun getPrayerTimesValue(day: Int, month: Int, year: Int, categoryName: String): List<Reminder>

    @Query(
        "SELECT day, month, year FROM reminders_table WHERE " +
                "((day >= :day AND month == :month AND year == :year) OR " +
                "(day >= 1 AND month > :month AND year == :year) OR " +
                "(day >= 1 AND month >= 1 AND year > :year)) " +
                "AND id <= -1019700 "
    )
    fun getUpcomingPrayerDates(day: Int, month: Int, year: Int): List<ReminderDate>

    @Query(
        "UPDATE reminders_table SET offsetInMinutes =:offsetValue, reminderInfo =:reminderInfo," +
                " isEnabled = :isEnabled, isComplete = :isComplete" +
                " WHERE id == :reminderId"
    )
    suspend fun updatePrayerTimeDetails(
        reminderInfo: String?,
        offsetValue: Int,
        isEnabled: Boolean,
        isComplete: Boolean,
        reminderId: Int
    )

    @Query(
        "UPDATE reminders_table SET reminderName = :newPrayerName WHERE " +
                "reminderName == :oldPrayerName"
    )
    suspend fun updatePrayerNames(oldPrayerName: String, newPrayerName: String)

    @Query(
        "UPDATE reminders_table SET timeInSeconds = :timeInSeconds, isEnabled = :isEnabled," +
                " offsetInMinutes = :offsetInMinutes WHERE id == :id"
    )
    suspend fun updateGeneratedPrayerTime(
        id: Int,
        timeInSeconds: Long,
        isEnabled: Boolean,
        offsetInMinutes: Int
    )

    @Query("DELETE FROM reminders_table WHERE id < -1019700")
    suspend fun deleteAllPrayerTimes()

    @Query("UPDATE reminders_table SET category =:newCategory WHERE category == :deletedCategory")
    suspend fun updateCategory(deletedCategory: String, newCategory: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettings(settings: AppSettings)

    @Query("SELECT * FROM app_settings")
    fun getAppSettings(): Flow<AppSettings?>

    @Query("SELECT * FROM app_settings")
    fun getAppSettingsValue(): AppSettings?

    @Update
    suspend fun updateAppSettings(appSettings: AppSettings)

    @Query("UPDATE app_settings SET isShowHijriDateWidget =:isShowHijriDateWidget, isShowNextReminderWidget =:isDisplayNextReminder")
    suspend fun updateWidgetSettings(isShowHijriDateWidget: Boolean, isDisplayNextReminder: Boolean)
}