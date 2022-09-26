package com.thesunnahrevival.common.receivers

import android.app.Notification
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.text.TextUtils
import com.thesunnahrevival.common.R
import com.thesunnahrevival.common.services.NextReminderService
import com.thesunnahrevival.common.utilities.ReminderManager
import com.thesunnahrevival.common.utilities.createNotification

class ReminderBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (!(action != null && action.matches("android.intent.action.BOOT_COMPLETED".toRegex()))) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val notificationTitle = intent.getStringExtra(ReminderManager.NOTIFICATION_TITLE)
            val notificationText = intent.getStringArrayExtra(ReminderManager.NOTIFICATION_TEXT)
            val category = intent.getStringArrayExtra(ReminderManager.NOTIFICATION_CATEGORY)
            val doNotDisturbMinutes =
                intent.getIntExtra(ReminderManager.NOTIFICATION_DND_MINUTES, 0)
            val notificationToneUri: Uri? =
                if (intent.getStringExtra(ReminderManager.NOTIFICATION_TONE_URI) != null)
                    Uri.parse(intent.getStringExtra(ReminderManager.NOTIFICATION_TONE_URI))
                else
                    null

            val isVibrate = intent.getBooleanExtra(ReminderManager.NOTIFICATION_VIBRATE, false)
            if (!TextUtils.isEmpty(notificationTitle)) {
                notificationText?.forEachIndexed { index, text ->
                    notificationManager.notify(
                        index + 2, //Weird that id 1 does not trigger notification
                        createNotification(
                            context, notificationTitle, text,
                            Notification.PRIORITY_DEFAULT, notificationToneUri, isVibrate
                        )
                    )
                    enableDoNotDisturbForPrayerReminders(
                        notificationManager, category?.get(index), context,
                        doNotDisturbMinutes, notificationTitle, notificationToneUri, isVibrate
                    )
                }
            }
        }

        val service = Intent(context, NextReminderService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            context.startForegroundService(service)
        else
            context.startService(service)
    }

    /**
     * Used to enable Do Not Disturb Mode when prayer time reminders is triggered. The user has to allow the app to change DND settings.
     * @param doNotDisturbMinutes is the duration in minutes to enable DND.
     */
    private fun enableDoNotDisturbForPrayerReminders(
        notificationManager: NotificationManager, category: String?,
        context: Context, doNotDisturbMinutes: Int, notificationTitle: String?,
        notificationToneUri: Uri?, isVibrate: Boolean
    ) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
            notificationManager.isNotificationPolicyAccessGranted &&
            category?.matches(context.resources.getStringArray(R.array.categories)[2].toRegex()) == true &&
            doNotDisturbMinutes > 0
        ) {

            val text = arrayOf("")
            val categories = arrayOf(context.resources.getStringArray(R.array.categories)[2])

            if (!TextUtils.isEmpty(notificationTitle)) {
                notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_NONE)
                if (notificationToneUri != null) {
                    ReminderManager.getInstance().scheduleReminder(
                        context, "", text, categories,
                        (System.currentTimeMillis() + (doNotDisturbMinutes * 60000)),
                        notificationToneUri, isVibrate, doNotDisturbMinutes,
                        calculateDelayFromMidnight = false, isOneShot = true
                    )
                }
            } else
                notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
        }
    }
}