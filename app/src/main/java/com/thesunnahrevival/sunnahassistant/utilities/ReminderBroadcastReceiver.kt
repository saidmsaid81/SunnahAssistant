package com.thesunnahrevival.sunnahassistant.utilities

import android.app.Notification
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.text.TextUtils

class ReminderBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (!(action != null && action.matches("android.intent.action.BOOT_COMPLETED".toRegex()))) {
            val notificationManager =
                    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val notificationTitle = intent.getStringExtra(ReminderManager.NOTIFICATION_TITLE)
            val notificationText = intent.getStringExtra(ReminderManager.NOTIFICATION_TEXT)
            val notificationToneUri: Uri? =
                    if (intent.getStringExtra(ReminderManager.NOTIFICATION_TONE_URI) != null)
                        Uri.parse(intent.getStringExtra(ReminderManager.NOTIFICATION_TONE_URI))
                    else
                        null
            val isVibrate = intent.getBooleanExtra(ReminderManager.NOTIFICATION_VIBRATE, false)
            if (!TextUtils.isEmpty(notificationTitle)) {
                notificationManager.notify(0,
                        createNotification(
                                context, notificationTitle, notificationText,
                                Notification.PRIORITY_DEFAULT, notificationToneUri, isVibrate))
            }
        }
        val service = Intent(context, NextReminderService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            context.startForegroundService(service)
        else
            context.startService(service)
        updateHijriDateWidgets(context)
    }
}