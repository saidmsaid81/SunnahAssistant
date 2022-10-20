package com.thesunnahrevival.sunnahassistant.data.local

import androidx.lifecycle.LiveData
import androidx.paging.PagingSource
import androidx.room.*
import com.thesunnahrevival.sunnahassistant.data.model.AppSettings
import com.thesunnahrevival.sunnahassistant.data.model.ToDo
import com.thesunnahrevival.sunnahassistant.data.model.ToDoDate
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.util.*

@Dao
interface ToDoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertToDo(toDo: ToDo): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertToDosList(toDosList: List<ToDo>)

    @Update
    suspend fun updateToDo(toDo: ToDo)

    @Delete
    suspend fun deleteToDo(toDo: ToDo)

    @Delete
    suspend fun deleteListOfToDos(toDosList: List<ToDo>)

    @Query(
        "SELECT EXISTS (SELECT * FROM reminders_table WHERE (" +
                "category != :excludeCategory " +
                "AND ((day == :day AND month == :month AND year == :year) OR " +
                " (day == :day AND month == 12 AND year == 0 AND repeatsFromDate <= :date ) OR " +
                " (day == 0 AND repeatsFromDate <= :date) OR " +
                " (customScheduleDays LIKE '%' || :numberOfTheWeekDay || '%' AND repeatsFromDate <= :date)" +
                ")) LIMIT 1)"
    )
    fun thereToDosOnDay(
        excludeCategory: String,
        numberOfTheWeekDay: String,
        day: Int,
        month: Int,
        year: Int,
        date: String = LocalDate.of(year, month + 1, day).toString(),
    ): Boolean

    @Query("SELECT * FROM reminders_table WHERE id = :id")
    fun getToDo(id: Int): LiveData<ToDo?>

    @Query("SELECT * FROM reminders_table WHERE id = :id")
    fun getToDoById(id: Int): ToDo?

    @Query("SELECT * FROM reminders_table WHERE day == 1 AND month == 0 AND year == 1 AND (frequency == 3 OR frequency == 0)")
    fun getMalformedToDos(): Flow<List<ToDo>>

    @Query("SELECT id FROM reminders_table WHERE id <= -1000 AND id >= -1999")
    fun getTemplateToDoIds(): LiveData<List<Int>>

    @Query(
        "SELECT * FROM reminders_table WHERE (" +
                "(day == :day AND month == :month AND year == :year) OR " +
                " (day == :day AND month == 12 AND year == 0 AND repeatsFromDate <= :localDate) OR " +
                " (day == 0 AND repeatsFromDate <= :localDate) OR " +
                " (customScheduleDays LIKE '%' || :numberOfTheWeekDay || '%' AND repeatsFromDate <= :localDate)" +
                ") " +
                " AND (category LIKE '%' || :category || '%') AND (completedDates NOT LIKE '%' || :localDate || '%') ORDER BY timeInSeconds"
    )
    fun getIncompleteToDosOnDay(
        numberOfTheWeekDay: String,
        day: Int,
        month: Int,
        year: Int,
        category: String,
        localDate: String
    ): PagingSource<Int, ToDo>

    @Query(
        "SELECT * FROM reminders_table WHERE (" +
                "(day == :day AND month == :month AND year == :year) OR " +
                " (day == :day AND month == 12 AND year == 0 AND repeatsFromDate <= :localDate) OR " +
                " (day == 0 AND repeatsFromDate <= :localDate) OR " +
                " (customScheduleDays LIKE '%' || :numberOfTheWeekDay || '%' AND repeatsFromDate <= :localDate)" +
                ") " +
                " AND (category LIKE '%' || :category || '%') " +
                " AND (completedDates LIKE '%' || :localDate || '%') ORDER BY timeInSeconds"
    )
    fun getCompleteToDosOnDay(
        numberOfTheWeekDay: String,
        day: Int,
        month: Int,
        year: Int,
        category: String,
        localDate: String
    ): PagingSource<Int, ToDo>

    @Query(
        "SELECT * FROM reminders_table WHERE (" +
                " (day == :day AND month == :month AND year == :year) OR " +
                " (day == :day AND month == 12 AND year == 0 AND repeatsFromDate <= :date) OR " +
                " (day == 0 AND repeatsFromDate <= :date) OR " +
                " (customScheduleDays LIKE '%' || :numberOfTheWeekDay || '%' AND repeatsFromDate <= :date)" +
                ") AND " +
                " isEnabled " +
                " ORDER BY timeInSeconds"
    )
    fun getToDosOnDayValue(
        numberOfTheWeekDay: String,
        day: Int,
        month: Int,
        year: Int,
        date: String = LocalDate.of(year, month + 1, day).toString()
    ): List<ToDo>

    @Query(
        "SELECT (timeInSeconds + (offset * 60)) AS time FROM reminders_table WHERE " +
                " time > :offsetFromMidnight AND " +
                " ((day == :day AND month == :month AND year == :year) OR " +
                " (day == :day AND month == 12 AND year == 0 AND repeatsFromDate <= :date) OR " +
                " (day == 0 AND repeatsFromDate <= :date) OR " +
                " (customScheduleDays LIKE '%' || :numberOfTheWeekDay || '%' AND repeatsFromDate <= :date)" +
                " ) AND isEnabled ORDER BY time"
    )
    suspend fun getNextTimeForToDoForDay(
        offsetFromMidnight: Long,
        numberOfTheWeekDay: String,
        day: Int,
        month: Int,
        year: Int,
        date: String = LocalDate.of(year, month + 1, day).toString()
    ): Long?


    @Query(
        "SELECT * FROM reminders_table WHERE " +
                " (timeInSeconds + (offset * 60)) == :timeForToDo AND " +
                " ((day == :day AND month == :month AND year == :year) OR " +
                " (day == :day AND month == 12 AND year == 0 AND repeatsFromDate <= :date) OR " +
                " (day == 0 AND repeatsFromDate <= :date) OR " +
                " (customScheduleDays LIKE '%' || :numberOfTheWeekDay || '%' AND repeatsFromDate <= :date)" +
                ") " +
                " AND isEnabled ORDER BY (timeInSeconds + (offset * 60))"
    )
    suspend fun getNextScheduledToDosForDay(
        timeForToDo: Long,
        numberOfTheWeekDay: String,
        day: Int,
        month: Int,
        year: Int,
        date: String = LocalDate.of(year, month + 1, day).toString()
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
        "UPDATE reminders_table SET offset =:offsetValue, reminderInfo =:additionalInfo," +
                " isEnabled = :isEnabled, completedDates = :completedDates" +
                " WHERE id == :id"
    )
    suspend fun updatePrayerTimeDetails(
        additionalInfo: String?,
        offsetValue: Int,
        isEnabled: Boolean,
        completedDates: TreeSet<String>,
        id: Int
    )

    @Query(
        "UPDATE reminders_table SET reminderName = :newPrayerName WHERE " +
                "reminderName == :oldPrayerName"
    )
    suspend fun updatePrayerNames(oldPrayerName: String, newPrayerName: String)

    @Query(
        "UPDATE reminders_table SET timeInSeconds = :timeInSeconds, isEnabled = :isEnabled," +
                " offset = :offsetInMinutes WHERE id == :id"
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