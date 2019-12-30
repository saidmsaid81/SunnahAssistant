package com.thesunnahrevival.sunnahassistant.utilities;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class ReminderManager {

    static String NOTIFICATION_ID = "notification_id";
    static String NOTIFICATION_TITLE = "notification_title";
    static String NOTIFICATION_TEXT = "notification_text";
    static String NOTIFICATION_CATEGORY = "notification_category";
    private static ReminderManager mRemManagerInstance = null;

    private ReminderManager() {
    }

    /**
     * Method for getting the instance of @ReminderManager
     */
    public static ReminderManager getInstance() {
        if (mRemManagerInstance == null)
            mRemManagerInstance = new ReminderManager();
        return mRemManagerInstance;
    }

    /**
     * Creates The NotificationUtil Channel Required for displaying notifications in Android 8.0+
     */
    public void createNotificationChannel(Context context) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String[] channelIds = {"Prayer", "Sunnah", "Other"};

            //Register all the three channels
            for (String id : channelIds) {
                CharSequence name = id + " Alerts";
                String description = "Alerts about various " + id + " to do";
                int importance = NotificationManager.IMPORTANCE_DEFAULT;

                NotificationChannel channel = new NotificationChannel(id, name, importance);
                channel.setDescription(description);

                NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
                notificationManager.createNotificationChannel(channel);
            }

            NotificationChannel channel = new NotificationChannel(
                    "Next Reminder", "Next Reminder Sticky Notification", NotificationManager.IMPORTANCE_LOW);
            channel.setDescription("Sticky Notification to display the next scheduled reminder");
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }



    /**
     * Method for creating notification and PendingIntent
     */
    private PendingIntent createNotification(Context context, int notificationId, String title, String text, String category) {

        Intent notificationIntent = new Intent(context, ReminderBroadcastReceiver.class);
        notificationIntent.putExtra(NOTIFICATION_ID, notificationId);
        notificationIntent.putExtra(NOTIFICATION_TITLE, title);
        notificationIntent.putExtra(NOTIFICATION_TEXT, text);
        notificationIntent.putExtra(NOTIFICATION_CATEGORY, category);
        return PendingIntent.getBroadcast(context, notificationId, notificationIntent, 0);

    }


    /**
     * Schedule A reminder to fire at a later time
     */
    void scheduleReminder(Context context, int id, String title, String text, String category, long timeInMilliseconds) {
        PendingIntent pendingIntent = createNotification(
                context, id, title, text, category
        );

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        AlarmManager.AlarmClockInfo alarm = new AlarmManager.AlarmClockInfo(calculateDelayFromMidnight(timeInMilliseconds), null);
        alarmManager.setAlarmClock(alarm, pendingIntent);

    }

    private long calculateDelayFromMidnight(long timeInMilliseconds) {
        // today
        Calendar midnight = new GregorianCalendar();
        // reset hour, minutes, seconds and millis to midnight of that day
        midnight.set(Calendar.HOUR_OF_DAY, 0);
        midnight.set(Calendar.MINUTE, 0);
        midnight.set(Calendar.SECOND, 0);
        midnight.set(Calendar.MILLISECOND, 0);
        midnight.setTimeZone(TimeZone.getTimeZone("UTC"));
        long delay = midnight.getTimeInMillis() + timeInMilliseconds;
        if (delay <= System.currentTimeMillis())//Time Has Passed
            return delay + 86400000; //Schedule it the next day
        else
            return delay;

    }

    /**
     * Method for cancelling scheduled notifications
     */
    public void cancelScheduledReminder(Context context, int notificationId, String title, String text, String category) {
        PendingIntent pendingIntent = createNotification(context, notificationId, title, text, category);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }

}
