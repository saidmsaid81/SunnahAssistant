package com.thesunnahrevival.common.data.local

import androidx.lifecycle.LiveData
import androidx.paging.PagingSource
import androidx.room.*
import com.thesunnahrevival.common.data.model.AppSettings
import com.thesunnahrevival.common.data.model.ToDo
import com.thesunnahrevival.common.data.model.ToDoDate
import kotlinx.coroutines.flow.Flow

@Dao
interface ToDoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertToDo(toDo: ToDo): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertToDosList(toDosList: List<ToDo>)

    @Query("UPDATE reminders_table SET reminderName =:name, reminderInfo =:info, category =:category WHERE id =:id")
    suspend fun updateToDo(id: Int, name: String, info: String, category: String)

    @Delete
    suspend fun deleteToDo(toDo: ToDo)

    @Query(
        "SELECT EXISTS (SELECT * FROM reminders_table WHERE (" +
                "category != :excludeCategory " +
                "AND ((day == :day AND month == :month AND year == :year) OR (day == :day AND month == 12 AND year == 0) OR day == 0 OR customScheduleDays LIKE '%' || :numberOfTheWeekDay || '%')) LIMIT 1)"
    )
    fun thereToDosOnDay(
        excludeCategory: String,
        numberOfTheWeekDay: String,
        day: Int,
        month: Int,
        year: Int
    ): Boolean

    @Query("SELECT * FROM reminders_table WHERE id = :id")
    fun getToDo(id: Int): LiveData<ToDo?>

    @Query("SELECT * FROM reminders_table WHERE ((day == :day AND month == :month AND year == :year) OR (day == :day AND month == 12 AND year == 0) OR day == 0 OR customScheduleDays LIKE '%' || :numberOfTheWeekDay || '%') AND (category LIKE '%' || :category || '%') ORDER BY isComplete, timeInSeconds")
    fun getToDosOnDay(
        numberOfTheWeekDay: String,
        day: Int,
        month: Int,
        year: Int,
        category: String
    ): PagingSource<Int, ToDo>

    @Query("SELECT * FROM reminders_table WHERE ((day == :day AND month == :month AND year == :year) OR (day == :day AND month == 12 AND year == 0) OR day == 0 OR customScheduleDays LIKE '%' || :numberOfTheWeekDay || '%') AND isEnabled ORDER BY timeInSeconds")
    fun getToDosOnDayValue(
        numberOfTheWeekDay: String,
        day: Int,
        month: Int,
        year: Int
    ): List<ToDo>

    @Query("SELECT (timeInSeconds + (offsetInMinutes * 60)) AS time FROM reminders_table WHERE time > :offsetFromMidnight AND ((day == :day AND month == :month AND year == :year) OR (day == :day AND month == 12 AND year == 0) OR day == 0 OR customScheduleDays LIKE '%' || :numberOfTheWeekDay || '%') AND isEnabled ORDER BY time")
    suspend fun getNextTimeForToDoForDay(
        offsetFromMidnight: Long,
        numberOfTheWeekDay: String,
        day: Int,
        month: Int,
        year: Int
    ): Long?


    @Query("SELECT * FROM reminders_table WHERE (timeInSeconds + (offsetInMinutes * 60)) == :timeForToDo AND ((day == :day AND month == :month AND year == :year) OR (day == :day AND month == 12 AND year == 0) OR day == 0 OR customScheduleDays LIKE '%' || :numberOfTheWeekDay || '%') AND isEnabled ORDER BY (timeInSeconds + (offsetInMinutes * 60))")
    suspend fun getNextScheduledToDosForDay(
        timeForToDo: Long,
        numberOfTheWeekDay: String,
        day: Int,
        month: Int,
        year: Int
    ): List<ToDo>

    @Query(
        "SELECT EXISTS " +
                "(SELECT * FROM reminders_table WHERE category == :prayerCategory AND " +
                "id LIKE '-' || :id || '%')"
    )
    fun therePrayerToDosOnDay(prayerCategory: String, id: String): Boolean

    @Query("SELECT * FROM reminders_table WHERE (category ==:categoryName AND (day == :day AND month == :month AND year =:year)) ORDER BY timeInSeconds")
    fun getPrayerTimesValue(day: Int, month: Int, year: Int, categoryName: String): List<ToDo>

    @Query(
        "SELECT day, month, year FROM reminders_table WHERE " +
                "((day >= :day AND month == :month AND year == :year) OR " +
                "(day >= 1 AND month > :month AND year == :year) OR " +
                "(day >= 1 AND month >= 1 AND year > :year)) " +
                "AND id <= -1019700 "
    )
    fun getUpcomingPrayerDates(day: Int, month: Int, year: Int): List<ToDoDate>

    @Query(
        "UPDATE reminders_table SET offsetInMinutes =:offsetValue, reminderInfo =:additionalInfo," +
                " isEnabled = :isEnabled, isComplete = :isComplete" +
                " WHERE id == :id"
    )
    suspend fun updatePrayerTimeDetails(
        additionalInfo: String?,
        offsetValue: Int,
        isEnabled: Boolean,
        isComplete: Boolean,
        id: Int
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

    @Query("UPDATE app_settings SET isShowHijriDateWidget =:isShowHijriDateWidget, isShowNextReminderWidget =:isDisplayNextToDo")
    suspend fun updateWidgetSettings(isShowHijriDateWidget: Boolean, isDisplayNextToDo: Boolean)
}