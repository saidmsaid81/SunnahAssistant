package com.thesunnahrevival.sunnahassistant.utilities;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;

import com.thesunnahrevival.sunnahassistant.data.local.ReminderDAO;
import com.thesunnahrevival.sunnahassistant.data.local.SunnahAssistantDatabase;
import com.thesunnahrevival.sunnahassistant.data.model.Reminder;

import java.lang.ref.WeakReference;
import java.util.TimeZone;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class NextReminderService extends Service {

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new ServiceAsyncTask(this).execute();
        return START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    /*
        Inner Class
     */
    private static class ServiceAsyncTask extends AsyncTask<Void, Void, Reminder> {

        private WeakReference<NextReminderService> mServiceWeakReference;
        private boolean mIsForegroundEnabled;

        private ServiceAsyncTask(NextReminderService context) {
            mServiceWeakReference = new WeakReference<>(context);
        }

        @Override
        protected Reminder doInBackground(Void... voids) {
            ReminderDAO reminderDAO = SunnahAssistantDatabase.getInstance(mServiceWeakReference.get()).reminderDao();
            mIsForegroundEnabled = reminderDAO.getIsForegroundEnabled();
            return reminderDAO.getNextScheduledReminder(
                    TimeDateUtil.calculateOffsetFromMidnight(),
                    TimeDateUtil.getDayDate(System.currentTimeMillis()),
                    TimeDateUtil.getNameOfTheDay(System.currentTimeMillis()));
        }

        @Override
        protected void onPostExecute(Reminder reminder) {
            String title;
            String text;

            if (reminder != null && reminder.isEnabled()) {
                title = "Next Reminder Today at "
                        + TimeDateUtil.formatTimeInMilliseconds(
                        mServiceWeakReference.get(),
                        reminder.getTimeInMilliSeconds());

                text = reminder.getReminderName();
                ReminderManager.getInstance().scheduleReminder(
                        mServiceWeakReference.get(), reminder.getId(), reminder.getCategory() + " Reminder",
                        reminder.getReminderName(), reminder.getCategory(), reminder.getTimeInMilliSeconds());
            } else {
                title = "No Scheduled Reminder Today";
                text = "Tap to view other day reminders";
                ReminderManager.getInstance().scheduleReminder(
                        mServiceWeakReference.get(), -100, "", "", "",
                        -TimeZone.getDefault().getRawOffset() + 10
                );
            }

            Notification notification = NotificationUtil.createNotification(
                    mServiceWeakReference.get(), title, text, NotificationCompat.PRIORITY_LOW);

            if (mIsForegroundEnabled)
                mServiceWeakReference.get().startForeground(1, notification);
        }
    }

}

