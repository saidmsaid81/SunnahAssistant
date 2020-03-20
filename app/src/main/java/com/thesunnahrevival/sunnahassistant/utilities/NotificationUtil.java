package com.thesunnahrevival.sunnahassistant.utilities;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.thesunnahrevival.sunnahassistant.R;
import com.thesunnahrevival.sunnahassistant.views.MainActivity;

import androidx.core.app.NotificationCompat;

class NotificationUtil {

    static Notification createNotification(Context context, String title, String text, int priority) {

        String category;
        if (priority == -1){
            category = "Next Reminder";
        }
        else {
            category = "remindersDefault";
        }

        final Resources res = context.getResources();
        // This image is used as the notification's large icon (thumbnail).
        final Bitmap picture = BitmapFactory.decodeResource(res, R.drawable.logo);
        Intent intent = new Intent(context, MainActivity.class);
        Intent shareIntent = new Intent(context, MainActivity.class);
        shareIntent.putExtra("show_share_menu", "share_menu");
        shareIntent.putExtra("title", title);
        shareIntent.putExtra("text", text);
        PendingIntent activity = PendingIntent.getActivity(context, 0, intent, 0);

        NotificationCompat.Builder builder;
        builder = new NotificationCompat.Builder(context, category)
                .setDefaults(Notification.DEFAULT_ALL)
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

        if (priority != -1)
            builder = builder.addAction(
                    0,
                    "Remind Others",
                    PendingIntent.getActivity(
                            context,
                            0,
                            shareIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT))
                    .setLargeIcon(picture);

        return builder.build();

    }


}
