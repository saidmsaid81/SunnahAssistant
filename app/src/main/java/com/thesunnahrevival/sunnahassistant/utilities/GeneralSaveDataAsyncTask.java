package com.thesunnahrevival.sunnahassistant.utilities;

import android.net.Uri;
import android.os.AsyncTask;

import com.thesunnahrevival.sunnahassistant.data.local.ReminderDAO;
import com.thesunnahrevival.sunnahassistant.data.model.AppSettings;

import java.util.ArrayList;
import java.util.List;

public class GeneralSaveDataAsyncTask extends AsyncTask<List, Void, Void> {
    public static final int ADD_LIST_OF_REMINDERS = 2;
    public static final int DELETE_LIST_OF_REMINDERS = 4;
    public static final int UPDATE_SETTINGS = 5;
    public static final int ADD_SETTINGS = 6;
    public static final int UPDATE_DELETED_CATEGORIES = 8;
    public static final int UPDATE_NOTIFICATION_SETTINGS = 9;
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
            case UPDATE_DELETED_CATEGORIES:
                for (String category : (ArrayList<String>) lists[0]){
                    mReminderDAO.updateCategory(category, SunnahAssistantUtil.UNCATEGORIZED);
                }
                break;
            case UPDATE_NOTIFICATION_SETTINGS:
                mReminderDAO.updateNotificationSettings((Uri)lists[0].get(0), (boolean) lists[0].get(1),(int) lists[0].get(2));
                break;

        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        if (mId == DELETE_LIST_OF_REMINDERS)
            prayerDeleteTaskComplete = true;
    }

}
