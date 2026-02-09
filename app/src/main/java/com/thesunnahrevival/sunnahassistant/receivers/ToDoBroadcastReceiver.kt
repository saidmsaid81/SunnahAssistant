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
import androidx.core.app.NotificationCompat
import androidx.core.os.bundleOf
import androidx.navigation.NavDeepLinkBuilder
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.thesunnahrevival.sunnahassistant.R
import com.thesunnahrevival.sunnahassistant.data.repositories.SunnahAssistantRepository
import com.thesunnahrevival.sunnahassistant.utilities.*
import com.thesunnahrevival.sunnahassistant.views.MainActivity
import com.thesunnahrevival.sunnahassistant.workers.ReminderSchedulerWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking


class ToDoBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action

        if (!(action != null && action.matches(ACTION_BOOT_COMPLETED.toRegex()))) {
            if (action == MARK_AS_COMPLETE) {
                val id = intent.getIntExtra(TO_DO_ID, 0)
                markAsComplete(context, id)
            } else if (action == DISABLE_NUDGE) {
                val id = intent.getIntExtra(TO_DO_ID, 0)
                disableNudge(context, id)
            } else if (action == DISABLE_ALL_NUDGES) {
                val id = intent.getIntExtra(TO_DO_ID, 0)
                disableAllNudges(context, id)
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

    private fun disableNudge(context: Context, id: Int) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(id)

        CoroutineScope(Dispatchers.IO).launch {
            val repository = SunnahAssistantRepository.getInstance(context)
            repository.disableReminder(id)
        }
    }

    private fun disableAllNudges(context: Context, id: Int) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (id != 0) {
            notificationManager.cancel(id)
        }

        CoroutineScope(Dispatchers.IO).launch {
            val repository = SunnahAssistantRepository.getInstance(context)
            repository.disableAllAutomaticToDos()
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
                val toDo = runBlocking {
                    SunnahAssistantRepository.getInstance(context).getToDoById(id)
                }
                val isAutomaticToDo = toDo?.isAutomaticToDo == true
                val markAsCompleteAction = NotificationCompat.Action(
                    R.drawable.ic_check,
                    context.getString(R.string.mark_as_complete),
                    getMarkAsCompletePendingIntent(context, id)
                )

                val disableNudgeAction = NotificationCompat.Action(
                    R.drawable.ic_notifications_off,
                    context.getString(R.string.disable_this_nudge),
                    getDisableNudgePendingIntent(context, id)
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

                val disableAllNudgesAction = NotificationCompat.Action(
                    R.drawable.ic_notifications_off,
                    context.getString(R.string.disable_all_nudges),
                    getDisableAllNudgesPendingIntent(context, id)
                )

                val quranReaderIntent = getQuranReaderIntent(context, id)
                val openQuranReaderFragmentAction = if (quranReaderIntent != null) {
                    NotificationCompat.Action(
                        R.drawable.ic_read,
                        context.getString(R.string.read_quran),
                        quranReaderIntent
                    )
                } else {
                    null
                }

                val adhkaarReaderIntent = getAdhkaarReaderIntent(context, id)
                val openAdhkaarReaderFragmentAction = if (adhkaarReaderIntent != null) {
                    NotificationCompat.Action(
                        R.drawable.ic_dua,
                        context.getString(R.string.read_adhkaar),
                        adhkaarReaderIntent
                    )
                } else {
                    null
                }

                val titleForNotification =
                    if (isAutomaticToDo) context.getString(R.string.suggested) else notificationTitle

                notificationManager.notify(
                    id,
                    createNotification(
                        context = context,
                        channel = if (isAutomaticToDo)
                            getNudgeNotificationChannel(context)
                        else
                            getToDoNotificationChannel(context),
                        title = titleForNotification,
                        text = text,
                        pendingIntent = quranReaderIntent ?: adhkaarReaderIntent ?: getMainActivityPendingIntent(context),
                        actions = when {
                            isAutomaticToDo -> listOf(
                                disableNudgeAction,
                                snoozeNotificationAction,
                                disableAllNudgesAction
                            )
                            openQuranReaderFragmentAction != null -> listOf(
                                openQuranReaderFragmentAction,
                                snoozeNotificationAction,
                                shareNotificationAction
                            )
                            openAdhkaarReaderFragmentAction != null -> listOf(
                                openAdhkaarReaderFragmentAction,
                                snoozeNotificationAction,
                                shareNotificationAction
                            )
                            else -> listOf(markAsCompleteAction, snoozeNotificationAction, shareNotificationAction)
                        }
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

    private fun getDisableNudgePendingIntent(context: Context, id: Int): PendingIntent? {
        val disableNudgeIntent = Intent(context, ToDoBroadcastReceiver::class.java).apply {
            action = DISABLE_NUDGE
            putExtra(TO_DO_ID, id)
        }
        val flag = PendingIntent.FLAG_IMMUTABLE
        return PendingIntent.getBroadcast(context, id, disableNudgeIntent, flag)
    }

    private fun getDisableAllNudgesPendingIntent(context: Context, id: Int): PendingIntent? {
        val disableAllIntent = Intent(context, ToDoBroadcastReceiver::class.java).apply {
            action = DISABLE_ALL_NUDGES
            putExtra(TO_DO_ID, id)
        }
        val flag = PendingIntent.FLAG_IMMUTABLE
        return PendingIntent.getBroadcast(context, id, disableAllIntent, flag)
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

    private fun getQuranReaderIntent(context: Context, notificationId: Int): PendingIntent? {
        val page = when (notificationId) {
            READING_SURATUL_KAHF_ID -> 293
            READING_SURATUL_MULK_ID -> 562
            READING_QURAN_ID -> runBlocking {
                SunnahAssistantRepository.getInstance(context).getAppSettings().first()?.lastReadPage
            }
            else -> return null
        } ?: return null

        return NavDeepLinkBuilder(context)
            .setGraph(R.navigation.navigation)
            .setDestination(R.id.quranReaderFragment)
            .setArguments(args = bundleOf(QURAN_PAGE_FROM_NOTIFICATION to page, NOTIFICATION_ID to notificationId))
            .createPendingIntent()
    }

    private fun getAdhkaarReaderIntent(context: Context, notificationId: Int): PendingIntent? {
        val chapterId = when (notificationId) {
            READING_MORNING_ADHKAAR_ID -> 27
            READING_EVENING_ADHKAAR_ID -> 28
            READING_SLEEPING_ADHKAAR_ID -> 29
            else -> return null
        }

        return NavDeepLinkBuilder(context)
            .setGraph(R.navigation.navigation)
            .setDestination(R.id.adhkaarReaderFragment)
            .setArguments(args = bundleOf(ADHKAAR_CHAPTER_FROM_NOTIFICATION to chapterId, NOTIFICATION_ID to notificationId))
            .createPendingIntent()
    }
}
