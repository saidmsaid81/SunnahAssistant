package com.thesunnahrevival.sunnahassistant.data.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "reminders_table")
public class Reminder implements Parcelable {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String reminderName;
    private String reminderInfo;
    private long timeInSeconds;
    private String category;
    private String frequency;
    private boolean isEnabled;

    //Only For Prayer Times
    private int day;
    private int month;
    private int year;
    private int offset = 0;
    //For Weekly and Monthly Reminders Only
    private ArrayList<String> customScheduleDays;

    public static final Creator<Reminder> CREATOR = new Creator<Reminder>() {
        @Override
        public Reminder createFromParcel(Parcel in) {
            return new Reminder(in);
        }

        @Override
        public Reminder[] newArray(int size) {
            return new Reminder[size];
        }
    };

    /**
     * Create a reminder
     * @param reminderName the name of the reminder
     * @param reminderInfo info on the reminder
     * @param timeInSeconds Hours passed since last midnight in seconds for example (21:00) will be 21 * 3600 which is equal to 75600
     * @param category Either of the three categories (Sunnah, Prayer or Other)
     * @param frequency Either of the three frequencies (Daily, Weekly or Monthly)
     * @param isEnabled True if reminder is enabled, false if reminder is disabled
     * @param day  Day in the month. If its daily pass 0, if weekly pass -1.
     * @param month Only for one time reminders(Value should be from 0-11) . Pass null if daily, weekly or monthly.
     * @param year Only for one time reminders(Value should be the year that reminder should trigger). Pass null if daily, weekly or monthly.
     * @param offset Offset for the reminder to trigger either +/- number.
     * @param customScheduleDays Used for weekly reminders. Should Contain the name of the days represented as three characters Such as Sunday will be Sun. Pass null if its not weekly.
     */
    public Reminder(String reminderName, String reminderInfo, @Nullable Long timeInSeconds, String category, String frequency, boolean isEnabled, int day, @Nullable Integer month, @Nullable Integer year, int offset, @Nullable ArrayList<String> customScheduleDays) {
        this.reminderName = reminderName;
        this.reminderInfo = reminderInfo;
        if (month != null)
            this.month = month;
        else
            this.month = 12;
        if (year != null)
            this.year = year;
        else
            this.year = 0;
        if (timeInSeconds != null)
            this.timeInSeconds = timeInSeconds;
        else
            this.timeInSeconds = 172800; //48hrs
        this.category = category;
        this.frequency = frequency;
        this.day = day;
        this.offset = offset;
        this.isEnabled = isEnabled;
        if (customScheduleDays != null)
            this.customScheduleDays = customScheduleDays;
        else
            this.customScheduleDays = new ArrayList<>();
    }


    protected Reminder(Parcel in) {
        id = in.readInt();
        reminderName = in.readString();
        reminderInfo = in.readString();
        timeInSeconds = in.readLong();
        category = in.readString();
        frequency = in.readString();
        year = in.readInt();
        month = in.readInt();
        day = in.readInt();
        offset = in.readInt();
        isEnabled = in.readByte() != 0;
        customScheduleDays = in.createStringArrayList();
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getReminderName() {
        return reminderName;
    }

    public void setReminderName(String reminderName) {
        this.reminderName = reminderName;
    }

    public String getReminderInfo() {
        return reminderInfo;
    }


    public long getTimeInSeconds() {
        return timeInSeconds;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }

    public ArrayList<String> getCustomScheduleDays() {
        return customScheduleDays;
    }

    public long getTimeInMilliSeconds() {
        return (timeInSeconds + (offset * 60)) * 1000;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(reminderName);
        dest.writeString(reminderInfo);
        dest.writeLong(timeInSeconds);
        dest.writeString(category);
        dest.writeString(frequency);
        dest.writeInt(year);
        dest.writeInt(month);
        dest.writeInt(day);
        dest.writeInt(offset);
        dest.writeValue(isEnabled);
        dest.writeValue(customScheduleDays);
    }

    @Override
    public boolean equals(@Nullable Object otherReminder) {
        if (otherReminder == this)
            return true;
        if (!(otherReminder instanceof Reminder))
            return false;

        Reminder newReminder = (Reminder) otherReminder;
        if (!this.getReminderName().matches(newReminder.getReminderName()))
            return false;

        if (!this.getReminderInfo().matches(newReminder.getReminderInfo()))
            return false;

        if ((newReminder.getFrequency().matches("Weekly"))) {
            ArrayList<String> checkedDaysTemp = new ArrayList<>(newReminder.getCustomScheduleDays());
            if (this.getCustomScheduleDays().size() != newReminder.getCustomScheduleDays().size()) {
                return false;
            }
            if (!checkedDaysTemp.removeAll(this.getCustomScheduleDays()))
                return false;
        }
        if (!this.getCategory().matches(newReminder.getCategory()))
            return false;
        if (!this.getFrequency().matches(newReminder.getFrequency()))
            return false;
        if (this.getTimeInMilliSeconds() != newReminder.getTimeInMilliSeconds())
            return false;
        if (this.getDay() != newReminder.getDay())
            return false;
        return this.offset == newReminder.offset;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }
}
