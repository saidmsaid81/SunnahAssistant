package com.thesunnahrevival.sunnahassistant.utilities;

import android.os.AsyncTask;

import com.thesunnahrevival.sunnahassistant.data.local.ReminderDAO;
import com.thesunnahrevival.sunnahassistant.data.model.Reminder;

public class ReminderAsyncTask extends AsyncTask<Reminder, Void, Void> {

    public static final int ADD_REMINDER = 1;
    public static final int DELETE_REMINDER = 2;
    public static final int SET_PRAYER_TIME_ENABLED = 3;
    public static final int SET_REMINDER_ENABLED = 4;
    public static final int UPDATE_PRAYER_DETAILS = 5;
    private int mId;
    private ReminderDAO mReminderDAO;

    public ReminderAsyncTask(int id, ReminderDAO reminderDAO) {
        mId = id;
        mReminderDAO = reminderDAO;
    }

    @Override
    protected Void doInBackground(Reminder... reminders) {
        switch (mId) {
            case ADD_REMINDER:
                mReminderDAO.insertReminder(reminders[0]);
                break;
            case DELETE_REMINDER:
                mReminderDAO.deleteReminder(reminders[0]);
                break;
            case SET_REMINDER_ENABLED:
                mReminderDAO.setEnabled(reminders[0].getId(), reminders[0].isEnabled());
                break;
            case SET_PRAYER_TIME_ENABLED:
                mReminderDAO.setPrayerTimeEnabled(reminders[0].getReminderName(), reminders[0].isEnabled());
                break;
            case UPDATE_PRAYER_DETAILS:
                mReminderDAO.updatePrayerTimeDetails(reminders[0].getReminderName(), reminders[1].getReminderName(), reminders[1].getReminderInfo(), reminders[1].getOffset());
                break;
        }
        return null;
    }

}

