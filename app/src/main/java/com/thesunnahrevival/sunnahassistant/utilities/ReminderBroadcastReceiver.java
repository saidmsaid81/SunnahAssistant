package com.thesunnahrevival.sunnahassistant.utilities;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.text.TextUtils;

import static com.thesunnahrevival.sunnahassistant.utilities.ReminderManager.NOTIFICATION_CATEGORY;
import static com.thesunnahrevival.sunnahassistant.utilities.ReminderManager.NOTIFICATION_ID;
import static com.thesunnahrevival.sunnahassistant.utilities.ReminderManager.NOTIFICATION_TEXT;
import static com.thesunnahrevival.sunnahassistant.utilities.ReminderManager.NOTIFICATION_TITLE;

public class ReminderBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, Intent intent) {
        if (!(intent.getAction() != null && intent.getAction().matches("android.intent.action.BOOT_COMPLETED"))) {
            NotificationManager notificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            int notificationId = intent.getIntExtra(NOTIFICATION_ID, 0);
            String notificationTitle = intent.getStringExtra(NOTIFICATION_TITLE);
            String notificationText = intent.getStringExtra(NOTIFICATION_TEXT);
            String category = intent.getStringExtra(NOTIFICATION_CATEGORY);
            if (!TextUtils.isEmpty(notificationTitle)) {
                notificationManager.notify(notificationId,
                        NotificationUtil.createNotification(
                                context, notificationTitle, notificationText, Notification.PRIORITY_DEFAULT));
            }
        }

        Intent service = new Intent(context, NextReminderService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            context.startForegroundService(service);
        else
            context.startService(service);

    }

}




