package com.thesunnahrevival.sunnahassistant.receivers

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.text.TextUtils
import androidx.core.app.NotificationCompat.PRIORITY_DEFAULT
import com.thesunnahrevival.sunnahassistant.R
import com.thesunnahrevival.sunnahassistant.data.SunnahAssistantRepository
import com.thesunnahrevival.sunnahassistant.services.NextToDoService
import com.thesunnahrevival.sunnahassistant.utilities.ReminderManager
import com.thesunnahrevival.sunnahassistant.utilities.createNotification
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

const val MARK_AS_COMPLETE = "com.thesunnahrevival.sunnahassitant.MARK_AS_COMPLETE"
const val TO_DO_ID = "com.thesunnahrevival.sunnahassitant.TO_DO_ID"

class ToDoBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action

        if (!(action != null && action.matches("android.intent.action.BOOT_COMPLETED".toRegex()))) {
            if (action == MARK_AS_COMPLETE) {
                val id = intent.getIntExtra(TO_DO_ID, 0)
                markAsComplete(context, id)
            } else
                showNotifications(context, intent)
        }

        val service = Intent(context, NextToDoService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            context.startForegroundService(service)
        else
            context.startService(service)
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

        val notificationTitle = intent.getStringExtra(ReminderManager.NOTIFICATION_TITLE)
        val notificationText =
            intent.getSerializableExtra(ReminderManager.NOTIFICATION_TEXT) as Map<Int, String>?
        val category =
            intent.getSerializableExtra(ReminderManager.NOTIFICATION_CATEGORY) as Map<Int, String>?
        val doNotDisturbMinutes =
            intent.getIntExtra(ReminderManager.NOTIFICATION_DND_MINUTES, 0)
        val notificationToneUri: Uri? =
            if (intent.getStringExtra(ReminderManager.NOTIFICATION_TONE_URI) != null)
                Uri.parse(intent.getStringExtra(ReminderManager.NOTIFICATION_TONE_URI))
            else
                null

        val isVibrate = intent.getBooleanExtra(ReminderManager.NOTIFICATION_VIBRATE, false)
        if (!TextUtils.isEmpty(notificationTitle)) {
            notificationText?.forEach { (id, text) ->
                notificationManager.notify(
                    id,
                    createNotification(
                        context, id, notificationTitle, text,
                        PRIORITY_DEFAULT, notificationToneUri, isVibrate
                    )
                )
                enableDoNotDisturbForPrayerReminders(
                    notificationManager, id, category?.get(id), context,
                    doNotDisturbMinutes, notificationTitle, notificationToneUri, isVibrate
                )
            }
        } else {
            val prayerCategory = context.resources.getStringArray(R.array.categories)[2]
            if (category?.containsValue(prayerCategory) == true &&
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                notificationManager.isNotificationPolicyAccessGranted
            ) {
                //Disable DND
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
            notificationManager.isNotificationPolicyAccessGranted &&
            category?.matches(context.resources.getStringArray(R.array.categories)[2].toRegex()) == true &&
            doNotDisturbMinutes > 0
        ) {

            val text = mapOf<Int, String>()
            val categories = mapOf<Int, String>(
                Pair(
                    id,
                    context.resources.getStringArray(R.array.categories)[2]
                )
            )

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
            }

        }
    }
}