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
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.thesunnahrevival.sunnahassistant.R
import com.thesunnahrevival.sunnahassistant.views.MainActivity

fun createNotification(
    context: Context,
    channel: NotificationChannel?,
    title: String?,
    text: String?,
    priority: Int = NotificationCompat.PRIORITY_DEFAULT,
    pendingIntent: PendingIntent? = null,
    onlyAlertOnce: Boolean = false,
    category: String = Notification.CATEGORY_REMINDER,
    actions: List<NotificationCompat.Action> = listOf()
): Notification {
    val builder: NotificationCompat.Builder = NotificationCompat.Builder(
        context,
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) channel?.id ?: "" else ""
    )
        .setSmallIcon(R.drawable.ic_alarm)
        .setContentTitle(title)
        .setContentText(text)
        .setContentIntent(pendingIntent ?: getMainActivityPendingIntent(context))
        .setPriority(priority)
        .setTicker(title)
        .setOnlyAlertOnce(onlyAlertOnce)
        .setStyle(
            NotificationCompat.BigTextStyle()
                .bigText(text)
                .setBigContentTitle(title)
        )
        .setAutoCancel(true)
        .setCategory(category)

    for (action in actions) {
        builder.addAction(action)
    }

    return builder.build()
}

fun getMainActivityPendingIntent(context: Context): PendingIntent? {
    val intent = Intent(context, MainActivity::class.java)
    val flag = PendingIntent.FLAG_IMMUTABLE
    return PendingIntent.getActivity(context, 0, intent, flag)
}

fun getToDoNotificationChannel(context: Context): NotificationChannel? {
    var notificationChannel: NotificationChannel? = null
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val manager = getNotificationManager(context)
        notificationChannel = manager.notificationChannels
            .find { it.id.startsWith(TODO_NOTIFICATION_CHANNEL_PREFIX) }

        if (notificationChannel == null) {
            notificationChannel = NotificationChannel(
                TODO_NOTIFICATION_CHANNEL_PREFIX + System.currentTimeMillis(),
                context.getString(R.string.to_dos),
                NotificationManager.IMPORTANCE_DEFAULT
            )
            manager.createNotificationChannel(notificationChannel)
            return notificationChannel
        }
    }

    return notificationChannel
}

fun getDeveloperMessagesNotificationChannel(context: Context): NotificationChannel? {
    var notificationChannel: NotificationChannel? = null
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val manager = getNotificationManager(context)
        notificationChannel = manager.notificationChannels
            .find { it.id.matches(DEVELOPER_MESSAGES_CHANNEL_ID.toRegex()) }

        if (notificationChannel == null) {
            notificationChannel = NotificationChannel(
                DEVELOPER_MESSAGES_CHANNEL_ID,
                context.getString(R.string.developers_messages),
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationChannel.description =
                context.getString(R.string.developer_messages_description)
            manager.createNotificationChannel(notificationChannel)
            return notificationChannel
        }
    }

    return notificationChannel
}

fun getMaintenanceNotificationsChannel(context: Context): NotificationChannel? {
    var notificationChannel: NotificationChannel? = null
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val manager = getNotificationManager(context)
        notificationChannel = manager.notificationChannels
            .find { it.id.matches(LOW_PRIORITY_MAINTENANCE_NOTIFICATION_CHANNEL_ID.toRegex()) }

        if (notificationChannel == null) {
            notificationChannel = NotificationChannel(
                LOW_PRIORITY_MAINTENANCE_NOTIFICATION_CHANNEL_ID,
                context.getString(R.string.maintenance_notifications_channel_title),
                NotificationManager.IMPORTANCE_LOW
            )
            notificationChannel.description =
                context.getString(R.string.maintenance_notifications_channel_description)
            manager.createNotificationChannel(notificationChannel)
            return notificationChannel
        }
    }

    return notificationChannel
}

@RequiresApi(Build.VERSION_CODES.O)
private fun getNotificationManager(context: Context): NotificationManager {
    val manager = context.getSystemService(NotificationManager::class.java)
    deleteObsoleteNotificationChannels(manager)
    return manager
}

@RequiresApi(Build.VERSION_CODES.O)
private fun deleteObsoleteNotificationChannels(notificationManager: NotificationManager?) {
    notificationManager?.let {
        for (channel in it.notificationChannels) {
            if (
                !channel.id.startsWith(TODO_NOTIFICATION_CHANNEL_PREFIX) &&
                !channel.id.matches(DEVELOPER_MESSAGES_CHANNEL_ID.toRegex()) &&
                !channel.id.matches(LOW_PRIORITY_MAINTENANCE_NOTIFICATION_CHANNEL_ID.toRegex())
            ) {
                notificationManager.deleteNotificationChannel(channel.id)
            }
        }
    }
}

fun updateToDoNotificationChannel(
    context: Context,
    notificationToneUri: Uri?,
    isVibrate: Boolean,
    priority: Int
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val notificationManager = getNotificationManager(context)

        // Delete existing to do channels
        notificationManager.notificationChannels
            .filter { it.id.startsWith(TODO_NOTIFICATION_CHANNEL_PREFIX) }
            .forEach { notificationManager.deleteNotificationChannel(it.id) }

        val notificationChannel = NotificationChannel(
            TODO_NOTIFICATION_CHANNEL_PREFIX + System.currentTimeMillis(),
            context.getString(R.string.to_dos),
            priority
        )

        val attributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
            .build()

        notificationChannel.setSound(notificationToneUri, attributes)
        notificationChannel.enableVibration(isVibrate)
        notificationManager.createNotificationChannel(notificationChannel)
    }
}
