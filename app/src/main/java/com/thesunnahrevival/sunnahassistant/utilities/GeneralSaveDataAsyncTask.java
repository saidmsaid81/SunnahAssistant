package com.thesunnahrevival.sunnahassistant.utilities;

import android.database.sqlite.SQLiteConstraintException;
import android.os.AsyncTask;
import android.util.Log;

import com.thesunnahrevival.sunnahassistant.data.AppSettings;
import com.thesunnahrevival.sunnahassistant.data.ReminderDAO;

import java.util.List;

public class GeneralSaveDataAsyncTask extends AsyncTask<List, Void, Void> {
    public static final int ADD_HIJRI_DATA = 0;
    public static final int DELETE_HIJRI_DATA = 1;
    public static final int ADD_LIST_OF_REMINDERS = 2;
    public static final int DELETE_LIST_OF_REMINDERS = 4;
    public static final int UPDATE_SETTINGS = 5;
    public static final int ADD_SETTINGS = 6;
    public static final int SET_IS_LIGHT_MODE = 7;
    public static boolean prayerDeleteTaskComplete = false;
    public static boolean hijriDeleteTaskComplete = false;

    private ReminderDAO mReminderDAO;
    private int mId;

    public GeneralSaveDataAsyncTask(int id, ReminderDAO reminderDAO) {
        mId = id;
        mReminderDAO = reminderDAO;
    }


    @Override
    protected Void doInBackground(List... lists) {
        switch (mId) {
            case ADD_HIJRI_DATA:
                try {
                    mReminderDAO.addHijriDate(lists[0]);
                } catch (SQLiteConstraintException e) {
                    Log.v("SQLite Exception", "Duplicate Value");
                }

                break;
            case DELETE_HIJRI_DATA:
                mReminderDAO.deleteAllHijriData();
                break;
            case ADD_LIST_OF_REMINDERS:
                mReminderDAO.addRemindersList(lists[0]);
                break;
            case DELETE_LIST_OF_REMINDERS:
                mReminderDAO.deleteAllPrayerTimes();
                break;
            case UPDATE_SETTINGS:
                mReminderDAO.updateAppSettings((AppSettings) lists[0].get(0));
                break;
            case ADD_SETTINGS:
                mReminderDAO.insertSettings((AppSettings) lists[0].get(0));
                break;
            case SET_IS_LIGHT_MODE:
                mReminderDAO.setIsLightMode((Boolean) lists[0].get(0));
                break;
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        if (mId == DELETE_HIJRI_DATA)
            hijriDeleteTaskComplete = true;
        else if (mId == DELETE_LIST_OF_REMINDERS)
            prayerDeleteTaskComplete = true;
    }

}
