package com.thesunnahrevival.common.data.local

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.room.*
import com.thesunnahrevival.common.data.model.AppSettings
import com.thesunnahrevival.common.data.model.Reminder

@Dao
interface ReminderDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: Reminder)

    @Delete
    suspend fun deleteReminder(reminder: Reminder)

    @Query("SELECT * FROM reminders_table WHERE ((day == :day AND month == :month AND year == :year) OR (day == :day AND month == 12 AND year == 0) OR day == 0 OR customScheduleDays LIKE '%' || :numberOfTheWeekDay || '%')  ORDER BY timeInSeconds")
    fun getRemindersOnDay(
        numberOfTheWeekDay: String,
        day: Int,
        month: Int,
        year: Int
    ): LiveData<List<Reminder>>

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


    @Query("SELECT * FROM reminders_table WHERE ((day == :day AND month == :month AND year == :year) OR (day == :day AND month == 12 AND year == 0) OR day == 0 OR customScheduleDays LIKE '%' || :numberOfTheWeekDay || '%') AND isEnabled ORDER BY timeInSeconds")
    fun getRemindersOnDayValue(numberOfTheWeekDay: String, day: Int, month: Int, year: Int): List<Reminder>

    @Query("SELECT * FROM reminders_table WHERE (category ==:categoryName AND (day == :day AND month == :month AND year =:year)) ORDER BY timeInSeconds")
    fun getPrayerTimesValue(day: Int, month: Int, year: Int, categoryName: String): List<Reminder>

    @Query("UPDATE reminders_table SET isEnabled =:isEnabled WHERE id ==:id")
    suspend fun setEnabled(id: Int, isEnabled: Boolean)

    @Query("UPDATE reminders_table SET isEnabled =:isEnabled WHERE reminderName ==:prayerName")
    suspend fun setPrayerTimeEnabled(prayerName: String, isEnabled: Boolean)

    @Query("UPDATE reminders_table SET offsetInMinutes =:offsetValue, reminderName =:newPrayerName, reminderInfo =:reminderInfo, isEnabled = :isEnabled WHERE reminderName == :prayerName")
    suspend fun updatePrayerTimeDetails(
        prayerName: String?,
        newPrayerName: String?,
        reminderInfo: String?,
        offsetValue: Int,
        isEnabled: Boolean
    )

    @Query("UPDATE reminders_table SET month =:month, year =:year, timeInSeconds =:timeInSeconds WHERE id == :id")
    suspend fun updateGeneratedPrayerTimes(id: Int, month: Int, year: Int, timeInSeconds: Long)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addRemindersList(remindersList: List<Reminder>)

    @Query("DELETE FROM reminders_table WHERE category ==:categoryName ")
    suspend fun deleteAllPrayerTimes(categoryName: String)

    @Query("SELECT * FROM reminders_table WHERE timeInSeconds > :offsetFromMidnight AND ((day == :day AND month == :month AND year == :year) OR (day == :day AND month == 12 AND year == 0) OR day == 0 OR customScheduleDays LIKE '%' || :numberOfTheWeekDay || '%') AND isEnabled ORDER BY timeInSeconds")
    suspend fun getNextScheduledReminderToday(offsetFromMidnight: Long, numberOfTheWeekDay: String, day: Int, month: Int, year: Int): Reminder?

    @Query("SELECT * FROM reminders_table WHERE ((day == :day AND month == :month AND year == :year) OR (day == :day AND month == 12 AND year == 0) OR day == 0 OR customScheduleDays LIKE '%' || :numberOfTheWeekDay || '%') AND isEnabled ORDER BY timeInSeconds")
    suspend fun getNextScheduledReminderTomorrow(numberOfTheWeekDay: String, day: Int, month: Int, year: Int): Reminder?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettings(settings: AppSettings)

    @Query("SELECT * FROM app_settings")
    fun getAppSettings(): LiveData<AppSettings?>

    @Query("SELECT * FROM app_settings")
    suspend fun getAppSettingsValue(): AppSettings?

    @Update
    suspend fun updateAppSettings(appSettings: AppSettings)

    @Query("SELECT showNextReminderNotification FROM app_settings")
    suspend fun  isForegroundEnabled(): Boolean

    @Query("UPDATE reminders_table SET category =:newCategory WHERE category == :deletedCategory")
    suspend fun updateCategory(deletedCategory: String, newCategory: String)

    @Query("UPDATE reminders_table SET reminderName =:name, reminderInfo =:info, category =:category WHERE id =:id")
    suspend fun updateReminder(id: Int, name: String, info: String, category: String)

    @Query("UPDATE app_settings SET notificationToneUri =:notificationToneUri, isVibrate =:isVibrate, priority =:priority")
    suspend fun updateNotificationSettings(notificationToneUri: Uri?, isVibrate: Boolean, priority: Int)

    @Query("UPDATE app_settings SET isShowHijriDateWidget =:isShowHijriDateWidget, isShowNextReminderWidget =:isDisplayNextReminder")
    suspend fun updateWidgetSettings(isShowHijriDateWidget: Boolean, isDisplayNextReminder: Boolean)
}