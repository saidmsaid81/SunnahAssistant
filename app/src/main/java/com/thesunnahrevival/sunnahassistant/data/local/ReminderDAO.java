package com.thesunnahrevival.sunnahassistant.data.local;

import android.database.sqlite.SQLiteConstraintException;
import android.net.Uri;

import com.thesunnahrevival.sunnahassistant.data.model.AppSettings;
import com.thesunnahrevival.sunnahassistant.data.model.Reminder;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;


@Dao
public interface ReminderDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertReminder(Reminder reminder);

    @Delete
    void deleteReminder(Reminder reminder);

    @Query("SELECT * FROM reminders_table WHERE timeInSeconds >= :offsetFromMidnight AND ((day == :day AND month == :month AND year == :year) OR (day == :day AND month == 12 AND year == 0) OR day == 0 OR customScheduleDays LIKE '%' || :nameOfTheDay || '%')  ORDER BY isEnabled DESC, timeInSeconds")
    LiveData<List<Reminder>> getUpcomingReminders(long offsetFromMidnight, String nameOfTheDay, int day, int month, int year);

    @Query("SELECT * FROM reminders_table WHERE timeInSeconds <= :offsetFromMidnight AND ((day == :day AND month == :month AND year == :year) OR (day == :day AND month == 12 AND year == 0) OR day == 0 OR customScheduleDays LIKE '%' || :nameOfTheDay || '%')  ORDER BY isEnabled DESC, timeInSeconds")
    LiveData<List<Reminder>> getPastReminders(long offsetFromMidnight, String nameOfTheDay, int day, int month, int year);

    @Query("SELECT * FROM reminders_table WHERE ((day == :day AND month == :month AND year == :year) OR (day == :day AND month == 12 AND year == 0) OR day == 0 OR customScheduleDays LIKE '%' || :nameOfTheDay || '%')  ORDER BY isEnabled DESC, timeInSeconds")
    LiveData<List<Reminder>> getRemindersOnDay(String nameOfTheDay, int day, int month, int year);

    @Query("SELECT * FROM reminders_table WHERE (category == 'Prayer' AND (day == :day AND month == :month AND year =:year)) ORDER BY timeInSeconds")
    LiveData<List<Reminder>> getPrayerTimes(int day, int month, int year);

    @Query("SELECT * FROM reminders_table WHERE frequency == 'Weekly'")
    LiveData<List<Reminder>> getWeeklyReminders();

    @Query("SELECT * FROM reminders_table WHERE frequency == 'Monthly'")
    LiveData<List<Reminder>> getMonthlyReminder();

    @Query("SELECT * FROM reminders_table WHERE frequency == 'One Time'")
    LiveData<List<Reminder>> getOneTimeReminders();

    @Query("SELECT * FROM reminders_table WHERE ((day == :day AND month == :month AND year == :year) OR (day == :day AND month == 12 AND year == 0) OR day == 0 OR customScheduleDays LIKE '%' || :nameOfTheDay || '%') AND isEnabled ORDER BY timeInSeconds")
    List<Reminder> getRemindersOnDayValue(String nameOfTheDay, int day, int month, int year);

    @Query("SELECT * FROM reminders_table WHERE (category == 'Prayer' AND (day == :day AND month == :month AND year =:year)) ORDER BY timeInSeconds")
    List<Reminder> getPrayerTimesValue(int day, int month, int year);

    @Query("UPDATE reminders_table SET isEnabled =:isEnabled WHERE id ==:id")
    void setEnabled(int id, boolean isEnabled);

    @Query("UPDATE reminders_table SET isEnabled =:isEnabled WHERE reminderName ==:prayerName")
    void setPrayerTimeEnabled(String prayerName, boolean isEnabled);

    @Query("UPDATE reminders_table SET `offset` =:offsetValue, reminderName =:newPrayerName, reminderInfo =:reminderInfo WHERE reminderName == :prayerName")
    void updatePrayerTimeDetails(String prayerName, String newPrayerName, String reminderInfo, int offsetValue);

    @Query("UPDATE reminders_table SET month =:month, year =:year, timeInSeconds =:timeInSeconds WHERE id == :id")
    void updateGeneratedPrayerTimes(int id, int month, int year, long timeInSeconds);

    @Insert
    void addRemindersList(List<Reminder> remindersList);

    default String addRemindersListIfNotExists(List<Reminder> remindersList) {
        try {
            addRemindersList(remindersList);
        }
        catch (SQLiteConstraintException e){
            return "Reminders Already Added";
        }
        return "Successfully Added";
    }
    @Query("DELETE FROM reminders_table WHERE category == 'Prayer' ")
    void deleteAllPrayerTimes();

    @Query("SELECT * FROM reminders_table WHERE timeInSeconds > :offsetFromMidnight AND ((day == :day AND month == :month AND year == :year) OR (day == :day AND month == 12 AND year == 0) OR day == 0 OR customScheduleDays LIKE '%' || :nameOfTheDay || '%') AND isEnabled ORDER BY isEnabled DESC, timeInSeconds")
    Reminder getNextScheduledReminder(long offsetFromMidnight, String nameOfTheDay, int day, int month, int year);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertSettings(AppSettings settings);

    @Query("SELECT * FROM app_settings")
    LiveData<AppSettings> getAppSettings();

    @Query("SELECT * FROM app_settings")
    AppSettings getAppSettingsValue();

    @Update
    void updateAppSettings(AppSettings appSettings);


    @Query("SELECT showNextReminderNotification FROM app_settings")
    boolean getIsForegroundEnabled();

    @Query("UPDATE reminders_table SET category =:newCategory WHERE category == :deletedCategory")
    void updateCategory(String deletedCategory, String newCategory);

    @Query("UPDATE app_settings SET notificationToneUri =:notificationToneUri, isVibrate =:isVibrate, priority =:priority" )
    void updateNotificationSettings(Uri notificationToneUri, boolean isVibrate, int priority);

    @Query("UPDATE app_settings SET isShowHijriDateWidget =:isShowHijriDateWidget, isShowNextReminderWidget =:isDisplayNextReminder" )
    void updateWidgetSettings(boolean isShowHijriDateWidget, boolean isDisplayNextReminder);

}
