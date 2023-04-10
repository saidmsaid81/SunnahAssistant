package com.thesunnahrevival.sunnahassistant.utilities

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.core.app.NotificationCompat
import androidx.navigation.NavDeepLinkBuilder
import com.thesunnahrevival.sunnahassistant.R
import com.thesunnahrevival.sunnahassistant.data.model.prayerTimeRemindersId
import com.thesunnahrevival.sunnahassistant.receivers.MARK_AS_COMPLETE
import com.thesunnahrevival.sunnahassistant.receivers.TO_DO_ID
import com.thesunnahrevival.sunnahassistant.receivers.ToDoBroadcastReceiver
import com.thesunnahrevival.sunnahassistant.views.MainActivity
import com.thesunnahrevival.sunnahassistant.views.SHARE

fun createNotification(
    context: Context,
    id: Int?,
    title: String?,
    text: String?,
    priority: Int,
    notificationToneUri: Uri?,
    isVibrate: Boolean,
    isFCMMessage: Boolean = false
): Notification {
    var category = ""

    if (priority == -1) {
        category = "Next Reminder"
    } else {
        if (!isFCMMessage) {
            val reminderNotificationChannel = getReminderNotificationChannel(context)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
                reminderNotificationChannel != null
            ) {
                category = reminderNotificationChannel.id
            }
        } else
            category = "Developer"
    }

    val intent = Intent(context, MainActivity::class.java)

    val flag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        PendingIntent.FLAG_IMMUTABLE
    } else {
        0
    }

    val activity = if (priority != -1)
        PendingIntent.getActivity(context, 0, intent, flag)
    else
        NavDeepLinkBuilder(context)
            .setGraph(R.navigation.navigation)
            .setDestination(R.id.notificationSettingsFragment)
            .createPendingIntent()
    val builder: NotificationCompat.Builder = NotificationCompat.Builder(context, category)
        .setSmallIcon(R.drawable.ic_alarm)
        .setContentTitle(title)
        .setContentText(text)
        .setContentIntent(activity)
        .setPriority(priority)
        .setTicker(title)
        .setStyle(
            NotificationCompat.BigTextStyle()
                .bigText(text)
                .setBigContentTitle(title)
        )
            .setAutoCancel(true)
    if (priority != -1 && id != null) {
        if (notificationToneUri != null) builder.setSound(notificationToneUri)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //Important to set prayer time reminders to Notification.CATEGORY_ALARM so it triggers even when Sunnah Assistant initiated DND is on
            builder.setCategory(if (id > prayerTimeRemindersId) Notification.CATEGORY_REMINDER else Notification.CATEGORY_ALARM)
        }
        builder.addAction(
            R.drawable.ic_check,
            context.getString(R.string.mark_as_complete),
            getMarkAsCompletePendingIntent(context, id)
        )
        builder.addAction(
            R.drawable.ic_check,
            context.getString(R.string.snooze),
            snoozeNotification(context, id)
        )
        builder.addAction(
            R.drawable.ic_check,
            context.getString(R.string.share),
            shareToDo(context, id)
        )
    }
    if (isVibrate)
        builder.setVibrate(longArrayOf(1000, 1000, 1000, 1000, 1000))
    return builder.build()
}

fun getMarkAsCompletePendingIntent(context: Context, id: Int): PendingIntent? {
    val markAsCompleteIntent = Intent(context, ToDoBroadcastReceiver::class.java).apply {
        action = MARK_AS_COMPLETE
        putExtra(TO_DO_ID, id)
    }
    val flag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        PendingIntent.FLAG_IMMUTABLE
    } else {
        0
    }
    return PendingIntent.getBroadcast(context, id, markAsCompleteIntent, flag)

}

fun snoozeNotification(context: Context, id: Int): PendingIntent {
    return NavDeepLinkBuilder(context)
        .setGraph(R.navigation.navigation)
        .setArguments(Bundle().apply { putInt(TO_DO_ID, id) })
        .setDestination(R.id.snooze_options)
        .createPendingIntent()
}

fun shareToDo(context: Context, id: Int): PendingIntent? {
    val shareToDoIntent = Intent(context, MainActivity::class.java).apply {
        action = SHARE
        putExtra(TO_DO_ID, id)
    }
    val flag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        PendingIntent.FLAG_IMMUTABLE
    } else {
        0
    }
    return PendingIntent.getActivity(context, id, shareToDoIntent, flag)
}

/**
 * Creates The NotificationUtil Channel Required for displaying notifications in Android 8.0+
 */
fun createNotificationChannels(context: Context) {
    // Create the NotificationChannel, but only on API 26+ because
    // the NotificationChannel class is new and not in the support library
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        deleteToDoNotificationChannel(context)
        val notificationManager = context.getSystemService(NotificationManager::class.java)
        val channel = NotificationChannel(
            "remindersDefault",
            context.getString(R.string.to_dos),
            NotificationManager.IMPORTANCE_DEFAULT
        )
        notificationManager.createNotificationChannel(channel)

        val nextReminderChannel = NotificationChannel(
            "Next Reminder",
            context.getString(R.string.next_to_do_sticky_notification),
            NotificationManager.IMPORTANCE_LOW
        )
        nextReminderChannel.description =
            context.getString(R.string.sticky_notification_description)
        notificationManager.createNotificationChannel(nextReminderChannel)

        val messagesFromDeveloper = NotificationChannel(
            "Developer",
            context.getString(R.string.developers_messages),
            NotificationManager.IMPORTANCE_DEFAULT
        )
        nextReminderChannel.description = context.getString(R.string.developer_messages_description)
        notificationManager.createNotificationChannel(messagesFromDeveloper)
    }
}

fun getReminderNotificationChannel(context: Context): NotificationChannel? {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val manager = context.getSystemService(NotificationManager::class.java)
        for (channel in manager.notificationChannels) {
            if (!channel.id.matches("Next Reminder".toRegex()))
                return channel
        }
    }
    return null
}

fun deleteToDoNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val manager = context.getSystemService(NotificationManager::class.java)
        for (channel in manager.notificationChannels) {
            if (!channel.id.matches("Next Reminder".toRegex()))
                manager.deleteNotificationChannel(channel.id)
        }
    }
}

fun createToDoNotificationChannel(
    context: Context,
    toneUri: Uri?,
    isVibrate: Boolean,
    priority: Int
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            System.currentTimeMillis().toString(),
            "Reminders",
            priority
        )
        val attributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
            .build()
        channel.setSound(toneUri, attributes)
        channel.enableVibration(isVibrate)
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }
}
