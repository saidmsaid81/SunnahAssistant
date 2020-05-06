package com.thesunnahrevival.sunnahassistant.utilities;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;

import com.thesunnahrevival.sunnahassistant.R;
import com.thesunnahrevival.sunnahassistant.views.MainActivity;
import com.thesunnahrevival.sunnahassistant.views.SettingsActivity;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import static com.thesunnahrevival.sunnahassistant.views.SettingsActivity.FRAGMENT_TO_SHOW;
import static com.thesunnahrevival.sunnahassistant.views.SettingsActivity.NOTIFICATION_SETTINGS;

public class NotificationUtil {

    static Notification createNotification(Context context, String title, String text, int priority, @Nullable Uri notificationToneUri, boolean isVibrate) {

        String category = "";
        if (priority == -1){
            category = "Next Reminder";
        }
        else {
            NotificationChannel reminderNotificationChannel = getReminderNotificationChannel(context);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && reminderNotificationChannel != null) {
                category = reminderNotificationChannel.getId();
            }
        }

        final Resources res = context.getResources();
        // This image is used as the notification's large icon (thumbnail).
        final Bitmap picture = BitmapFactory.decodeResource(res, R.drawable.logo);
        Intent intent = new Intent(context, MainActivity.class);
        Intent stickyNotificationIntent = new Intent(context, SettingsActivity.class);
        stickyNotificationIntent.putExtra(FRAGMENT_TO_SHOW, NOTIFICATION_SETTINGS);
        PendingIntent activity;
        if (priority != -1)
            activity = PendingIntent.getActivity(context, 0, intent, 0);
        else
            activity = PendingIntent.getActivity(context, 0, stickyNotificationIntent, 0);

        NotificationCompat.Builder builder;
        builder = new NotificationCompat.Builder(context, category)
                .setSmallIcon(R.drawable.ic_alarm)
                .setContentTitle(title)
                .setContentText(text)
                .setContentIntent(activity)
                .setPriority(priority)
                .setTicker(title)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(text)
                        .setBigContentTitle(title)
                        .setSummaryText("Reminder"))
                .setAutoCancel(true);

        if (priority != -1){
            builder = builder.setLargeIcon(picture);
            if (notificationToneUri !=  null)
                    builder.setSound(notificationToneUri);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                builder.setCategory(Notification.CATEGORY_REMINDER);
            }
        }

        if (isVibrate)
                    builder.setVibrate(new long[] { 1000, 1000, 1000, 1000, 1000 });

        return builder.build();

    }

    /**
     * Creates The NotificationUtil Channel Required for displaying notifications in Android 8.0+
     */
    public static void createNotificationChannels(Context context) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            deleteReminderNotificationChannel(context);
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);

            NotificationChannel channel = new NotificationChannel("remindersDefault", "Reminders", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);

            NotificationChannel nextReminderChannel = new NotificationChannel(
                    "Next Reminder", "Next Reminder Sticky Notification", NotificationManager.IMPORTANCE_LOW);
            nextReminderChannel.setDescription("Sticky Notification to display the next scheduled reminder");
            notificationManager.createNotificationChannel(nextReminderChannel);
        }
    }

    public static NotificationChannel getReminderNotificationChannel(Context context){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            for (NotificationChannel channel : manager.getNotificationChannels()){
                if (!channel.getId().matches("Next Reminder"))
                    return channel;
            }
        }
        return null;
    }

    public static void deleteReminderNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            for (NotificationChannel channel : manager.getNotificationChannels()){
                if (!channel.getId().matches("Next Reminder"))
                    manager.deleteNotificationChannel(channel.getId());
            }
        }
    }

    public static void createReminderNotificationChannel(Context context, Uri toneUri, boolean isVibrate,int priority ){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    String.valueOf(System.currentTimeMillis()),
                    "Reminders",
                    priority
            );
            AudioAttributes attributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build();
            channel.setSound(toneUri, attributes);
            channel.enableVibration(isVibrate);

            NotificationManager manager = context.getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }


}
