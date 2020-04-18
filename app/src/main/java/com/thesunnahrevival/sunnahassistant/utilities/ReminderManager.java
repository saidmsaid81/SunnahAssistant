package com.thesunnahrevival.sunnahassistant.utilities;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class ReminderManager {

    static String NOTIFICATION_TITLE = "com.thesunnahrevival.sunnahassistant.utilities.notificationTitle";
    static String NOTIFICATION_TEXT = "com.thesunnahrevival.sunnahassistant.utilities.notificationText";
    static String NOTIFICATION_TONE_URI = "com.thesunnahrevival.sunnahassistant.utilities.notificationToneUri";
    static String NOTIFICATION_VIBRATE = "com.thesunnahrevival.sunnahassistant.utilities.notificationVibrate";
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
     * Method for creating notification and PendingIntent
     */
    private PendingIntent createNotificationPendingIntent(Context context, String title, String text, Uri notificationUri, boolean isVibrate) {

        Intent notificationIntent = new Intent(context, ReminderBroadcastReceiver.class);
        notificationIntent.putExtra(NOTIFICATION_TITLE, title);
        notificationIntent.putExtra(NOTIFICATION_TEXT, text);
        notificationIntent.putExtra(NOTIFICATION_TONE_URI, notificationUri.toString());
        notificationIntent.putExtra(NOTIFICATION_VIBRATE, isVibrate);
        return PendingIntent.getBroadcast(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);


    }


    /**
     * Schedule A reminder to fire at a later time
     */
    void scheduleReminder(Context context, String title, String text, long timeInMilliseconds, Uri notificationUri, boolean isVibrate) {
        PendingIntent pendingIntent = createNotificationPendingIntent(
                context, title, text, notificationUri, isVibrate
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

//    /**
//     * Method for cancelling scheduled notifications
//     */
//    public void cancelScheduledReminder(Context context, int notificationId, String title, String text, String category) {
//        PendingIntent pendingIntent = createNotificationPendingIntent(context, notificationId, title, text, category);
//        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
//        alarmManager.cancel(pendingIntent);
//    }

}
