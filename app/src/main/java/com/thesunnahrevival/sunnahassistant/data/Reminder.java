package com.thesunnahrevival.sunnahassistant.data;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.Ignore;
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
    //Only For Prayer Times
    private int day;
    private int offset = 0;
    //For Weekly and Monthly Reminders Only
    private ArrayList<String> customScheduleDays;


    @Ignore
    public Reminder(String reminderName, String reminderInfo, String timeString, String category, String frequency, int day, boolean isEnabled, ArrayList<String> customScheduleDays) {
        this.reminderName = reminderName;
        this.reminderInfo = reminderInfo;
        if (!timeString.matches("Not Set"))
            this.timeInSeconds = getTimestampInSeconds(timeString);
        else
            this.timeInSeconds = 86399;
        this.category = category;
        this.frequency = frequency;
        this.day = day;
        this.isEnabled = isEnabled;
        this.customScheduleDays = customScheduleDays;
    }

    public Reminder(String reminderName, String reminderInfo, long timeInSeconds, String category, String frequency, int day, int offset, boolean isEnabled, ArrayList<String> customScheduleDays) {
        this.reminderName = reminderName;
        this.reminderInfo = reminderInfo;
        this.timeInSeconds = timeInSeconds;
        this.category = category;
        this.frequency = frequency;
        this.day = day;
        this.offset = offset;
        this.isEnabled = isEnabled;
        this.customScheduleDays = customScheduleDays;
    }


    protected Reminder(Parcel in) {
        id = in.readInt();
        reminderName = in.readString();
        reminderInfo = in.readString();
        timeInSeconds = in.readLong();
        category = in.readString();
        frequency = in.readString();
        day = in.readInt();
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

    public void setReminderInfo(String reminderInfo) {
        this.reminderInfo = reminderInfo;
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

    public void setCustomScheduleDays(ArrayList<String> customScheduleDays) {
        this.customScheduleDays = customScheduleDays;
    }

    public long getTimeInMilliSeconds() {
        return (timeInSeconds + (offset * 60)) * 1000;
    }

    public String getFrequencyAndCategory() {
        return frequency + " " + category;
    }

    private long getTimestampInSeconds(String timeString) {
        SimpleDateFormat format = new SimpleDateFormat("HH:mm", Locale.getDefault());
        try {
            java.util.Date date = format.parse(timeString);
            return date.getTime() / 1000;
        } catch (ParseException e) {
            Log.v("ParseException", e.getMessage());
            return 0;
        }

    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public void setTimeFromString(String timeString) {
        timeInSeconds = getTimestampInSeconds(timeString);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(reminderName);
        dest.writeString(reminderInfo);
        dest.writeLong(timeInSeconds);
        dest.writeString(category);
        dest.writeString(frequency);
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
}
