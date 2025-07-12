package com.thesunnahrevival.sunnahassistant.receivers

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_BOOT_COMPLETED
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.navigation.NavDeepLinkBuilder
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.thesunnahrevival.sunnahassistant.R
import com.thesunnahrevival.sunnahassistant.data.SunnahAssistantRepository
import com.thesunnahrevival.sunnahassistant.utilities.MARK_AS_COMPLETE
import com.thesunnahrevival.sunnahassistant.utilities.NOTIFICATION_CATEGORY
import com.thesunnahrevival.sunnahassistant.utilities.NOTIFICATION_DND_MINUTES
import com.thesunnahrevival.sunnahassistant.utilities.NOTIFICATION_TEXT
import com.thesunnahrevival.sunnahassistant.utilities.NOTIFICATION_TITLE
import com.thesunnahrevival.sunnahassistant.utilities.NOTIFICATION_TONE_URI
import com.thesunnahrevival.sunnahassistant.utilities.NOTIFICATION_VIBRATE
import com.thesunnahrevival.sunnahassistant.utilities.ReminderManager
import com.thesunnahrevival.sunnahassistant.utilities.SHARE
import com.thesunnahrevival.sunnahassistant.utilities.TODO_REMINDER_SCHEDULER_WORK_TAG
import com.thesunnahrevival.sunnahassistant.utilities.TO_DO_ID
import com.thesunnahrevival.sunnahassistant.utilities.createNotification
import com.thesunnahrevival.sunnahassistant.utilities.getMainActivityPendingIntent
import com.thesunnahrevival.sunnahassistant.utilities.getToDoNotificationChannel
import com.thesunnahrevival.sunnahassistant.views.MainActivity
import com.thesunnahrevival.sunnahassistant.workers.ReminderSchedulerWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class ToDoBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action

        if (!(action != null && action.matches(ACTION_BOOT_COMPLETED.toRegex()))) {
            if (action == MARK_AS_COMPLETE) {
                val id = intent.getIntExtra(TO_DO_ID, 0)
                markAsComplete(context, id)
            } else {
                showNotifications(context, intent)
            }
        }

        val refreshRemindersRequest = OneTimeWorkRequestBuilder<ReminderSchedulerWorker>()
            .addTag(TODO_REMINDER_SCHEDULER_WORK_TAG)
            .build()

        WorkManager.getInstance(context)
            .enqueue(refreshRemindersRequest)

    }

    private fun markAsComplete(context: Context, id: Int) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(id)

        CoroutineScope(Dispatchers.IO).launch {
            val repository = SunnahAssistantRepository.getInstance(context)
            repository.markAsComplete(id)
        }
    }

    private fun showNotifications(context: Context, intent: Intent) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notificationTitle = intent.getStringExtra(NOTIFICATION_TITLE)
        val notificationText =
            intent.getSerializableExtra(NOTIFICATION_TEXT) as Map<Int, String>?
        val category =
            intent.getSerializableExtra(NOTIFICATION_CATEGORY) as Map<Int, String>?
        val doNotDisturbMinutes =
            intent.getIntExtra(NOTIFICATION_DND_MINUTES, 0)
        val notificationToneUri: Uri? =
            if (intent.getStringExtra(NOTIFICATION_TONE_URI) != null)
                Uri.parse(intent.getStringExtra(NOTIFICATION_TONE_URI))
            else
                null

        val isVibrate = intent.getBooleanExtra(NOTIFICATION_VIBRATE, false)

        if (!TextUtils.isEmpty(notificationTitle)) {
            notificationText?.forEach { (id, text) ->
                val markAsCompleteAction = NotificationCompat.Action(
                    R.drawable.ic_check,
                    context.getString(R.string.mark_as_complete),
                    getMarkAsCompletePendingIntent(context, id)
                )

                val snoozeNotificationAction = NotificationCompat.Action(
                    R.drawable.ic_alarm,
                    context.getString(R.string.snooze),
                    snoozeNotificationAction(context, id)
                )

                val shareNotificationAction = NotificationCompat.Action(
                    R.drawable.ic_share,
                    context.getString(R.string.share),
                    shareToDoNotificationAction(context, id)
                )
                notificationManager.notify(
                    id,
                    createNotification(
                        context = context,
                        channel = getToDoNotificationChannel(context),
                        title = notificationTitle,
                        text = text,
                        pendingIntent = getMainActivityPendingIntent(context),
                        actions = listOf(markAsCompleteAction, snoozeNotificationAction, shareNotificationAction)
                    )
                )
                Thread.sleep(5000) //Give time for the notification to ring
                enableDoNotDisturbForPrayerReminders(
                    notificationManager, id, category?.get(id), context,
                    doNotDisturbMinutes, notificationTitle, notificationToneUri, isVibrate
                )
            }
        } else {
            val prayerCategory = context.resources.getStringArray(R.array.categories)[2]
            if (category?.containsValue(prayerCategory) == true && notificationManager.isNotificationPolicyAccessGranted) {
                //Disable DND after the user specified time has passed
                notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
            }
        }
    }

    /**
     * Used to enable Do Not Disturb Mode when prayer time reminders is triggered. The user has to allow the app to change DND settings.
     * @param doNotDisturbMinutes is the duration in minutes to enable DND.
     */
    private fun enableDoNotDisturbForPrayerReminders(
        notificationManager: NotificationManager, id: Int,
        category: String?, context: Context, doNotDisturbMinutes: Int,
        notificationTitle: String?, notificationToneUri: Uri?, isVibrate: Boolean
    ) {

        val prayerCategory = context.resources.getStringArray(R.array.categories)[2]

        if (notificationManager.isNotificationPolicyAccessGranted &&
            category?.matches(prayerCategory.toRegex()) == true && doNotDisturbMinutes > 0
        ) {

            val text = mapOf<Int, String>()
            val categories = mapOf<Int, String>(
                Pair(
                    id,
                    prayerCategory
                )
            )

            if (!TextUtils.isEmpty(notificationTitle)) {
                notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALARMS)
                if (notificationToneUri != null) {
                    ReminderManager.getInstance().scheduleReminder(
                        context, "", text, categories,
                        (System.currentTimeMillis() + (doNotDisturbMinutes * 60 * 1000)),
                        notificationToneUri, isVibrate, doNotDisturbMinutes,
                        calculateDelayFromMidnight = false, isOneShot = true
                    )
                }
            }

        }
    }

    private fun getMarkAsCompletePendingIntent(context: Context, id: Int): PendingIntent? {
        val markAsCompleteIntent = Intent(context, ToDoBroadcastReceiver::class.java).apply {
            action = MARK_AS_COMPLETE
            putExtra(TO_DO_ID, id)
        }
        val flag = PendingIntent.FLAG_IMMUTABLE
        return PendingIntent.getBroadcast(context, id, markAsCompleteIntent, flag)

    }

    private fun snoozeNotificationAction(context: Context, id: Int): PendingIntent {
        return NavDeepLinkBuilder(context)
            .setGraph(R.navigation.navigation)
            .setArguments(Bundle().apply { putInt(TO_DO_ID, id) })
            .setDestination(R.id.snooze_options)
            .createPendingIntent()
    }

    private fun shareToDoNotificationAction(context: Context, id: Int): PendingIntent? {
        val shareToDoIntent = Intent(context, MainActivity::class.java).apply {
            action = SHARE
            putExtra(TO_DO_ID, id)
        }
        val flag = PendingIntent.FLAG_IMMUTABLE
        return PendingIntent.getActivity(context, id, shareToDoIntent, flag)
    }
}