package com.thesunnahrevival.sunnahassistant.data;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.thesunnahrevival.sunnahassistant.data.HijriDateData.Hijri;


@Dao
public interface ReminderDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertReminder(Reminder reminder);

    @Delete
    void deleteReminder(Reminder reminder);

    @Query("SELECT * FROM reminders_table WHERE timeInSeconds >= :offsetFromMidnight AND (day == :day OR day == 0 OR customScheduleDays LIKE '%' || :nameOfTheDay || '%')  ORDER BY timeInSeconds")
    LiveData<List<Reminder>> getTodayReminders(long offsetFromMidnight, int day, String nameOfTheDay);

    @Query("SELECT * FROM reminders_table WHERE timeInSeconds <= :offsetFromMidnight AND (day == :day OR day == 0 OR customScheduleDays LIKE '%' || :nameOfTheDay || '%')  ORDER BY timeInSeconds")
    LiveData<List<Reminder>> getPastReminders(long offsetFromMidnight, int day, String nameOfTheDay);

    @Query("SELECT * FROM reminders_table WHERE (day == :day OR day == 0 OR customScheduleDays LIKE '%' || :nameOfTheDay || '%')  ORDER BY timeInSeconds")
    LiveData<List<Reminder>> getTomorrowReminders(int day, String nameOfTheDay);

    @Query("SELECT * FROM reminders_table WHERE (category == 'Prayer' AND day == :day) ORDER BY timeInSeconds")
    LiveData<List<Reminder>> getPrayerTimes(int day);

    @Query("SELECT * FROM reminders_table WHERE frequency == 'Weekly'")
    LiveData<List<Reminder>> getWeeklyReminders();

    @Query("SELECT * FROM reminders_table WHERE frequency == 'Monthly'")
    LiveData<List<Reminder>> getMonthlyReminder();

    @Query("UPDATE reminders_table SET isEnabled =:isEnabled WHERE id ==:id")
    void setEnabled(int id, boolean isEnabled);

    @Query("UPDATE reminders_table SET isEnabled =:isEnabled WHERE reminderName ==:prayerName")
    void setPrayerTimeEnabled(String prayerName, boolean isEnabled);

    @Query("UPDATE reminders_table SET `offset` =:offsetValue, reminderName =:newPrayerName, reminderInfo =:reminderInfo WHERE reminderName == :prayerName")
    void updatePrayerTimeDetails(String prayerName, String newPrayerName, String reminderInfo, int offsetValue);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void addRemindersList(List<Reminder> remindersList);

    @Query("DELETE FROM reminders_table WHERE category == 'Prayer' ")
    void deleteAllPrayerTimes();

    @Query("SELECT * FROM reminders_table WHERE timeInSeconds > :offsetFromMidnight AND (day == :day OR day == 0 OR customScheduleDays LIKE '%' || :nameOfTheDay || '%')  ORDER BY isEnabled DESC, timeInSeconds")
    Reminder getNextScheduledReminder(long offsetFromMidnight, int day, String nameOfTheDay);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void addHijriDate(List<Hijri> hijriDate);

    @Query("SELECT * FROM hijri_calendar WHERE id = :id")
    LiveData<Hijri> getHijriDate(int id);

    @Query("DELETE FROM hijri_calendar")
    void deleteAllHijriData();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertSettings(AppSettings settings);

    @Query("SELECT * FROM app_settings")
    LiveData<AppSettings> getAppSettings();

    @Update
    void updateAppSettings(AppSettings appSettings);

    @Query("UPDATE app_settings SET isLightMode =:isLightMode")
    void setIsLightMode(boolean isLightMode);

    @Query("SELECT showNextReminderNotification FROM app_settings")
    boolean getIsForegroundEnabled();
}
