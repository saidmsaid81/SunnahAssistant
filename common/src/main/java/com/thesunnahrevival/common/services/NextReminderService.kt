package com.thesunnahrevival.common.services

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.net.Uri
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.thesunnahrevival.common.R
import com.thesunnahrevival.common.data.SunnahAssistantRepository
import com.thesunnahrevival.common.data.model.AppSettings
import com.thesunnahrevival.common.data.model.Reminder
import com.thesunnahrevival.common.utilities.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*


class NextReminderService : Service() {

    private lateinit var mRepository: SunnahAssistantRepository

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        mRepository = SunnahAssistantRepository.getInstance(this.application)

        CoroutineScope(Dispatchers.IO).launch {
            val settings = mRepository.getAppSettingsValue()
            val isForegroundEnabled = settings?.showNextReminderNotification ?: false

            val timeInMilliseconds = System.currentTimeMillis()
            var dayString = getString(R.string.at)

            val nextTimeForReminderToday = mRepository.getNextTimeForReminderToday(
                calculateOffsetFromMidnight(),
                dayOfTheWeek.toString(),
                getDayDate(timeInMilliseconds),
                getMonthNumber(timeInMilliseconds), Integer.parseInt(getYear(timeInMilliseconds))
            )

            val nextTimeForReminderTomorrow = mRepository.getNextTimeForReminderTomorrow(
                calculateOffsetFromMidnight(),
                tomorrowDayOfTheWeek.toString(),
                getDayDate(timeInMilliseconds + 86400000),
                getMonthNumber(timeInMilliseconds + 86400000),
                Integer.parseInt(getYear(timeInMilliseconds + 86400000))
            )

            val nextScheduledReminders = arrayListOf<Reminder>()

            //Check to see if tomorrows reminders trigger time is offset to earlier than today reminders
            if (nextTimeForReminderTomorrow != null &&
                nextTimeForReminderTomorrow < 0 &&
                ((24 * 60 * 60) + nextTimeForReminderTomorrow) < (nextTimeForReminderToday
                    ?: (24 * 60 * 60))
            ) {
                nextScheduledReminders.addAll(
                    getTomorrowsReminders(nextTimeForReminderTomorrow, timeInMilliseconds)
                )
            } else if (nextTimeForReminderToday != null) {
                //Get Today Reminders
                nextScheduledReminders.addAll(
                    mRepository.getNextScheduledReminderToday(
                        nextTimeForReminderToday,
                        dayOfTheWeek.toString(),
                        getDayDate(timeInMilliseconds),
                        getMonthNumber(timeInMilliseconds),
                        Integer.parseInt(getYear(timeInMilliseconds))
                    )
                )

                //Check to see if tomorrows reminders trigger time is offset to same as today reminders
                if (nextTimeForReminderTomorrow != null &&
                    ((24 * 60 * 60) + nextTimeForReminderTomorrow) == nextTimeForReminderToday
                ) {
                    nextScheduledReminders.addAll(
                        getTomorrowsReminders(nextTimeForReminderTomorrow, timeInMilliseconds)
                    )
                }
            }

            if (nextTimeForReminderToday == null && nextTimeForReminderTomorrow != null) {
                dayString = getString(R.string.tomorrow_at_notification)
            }

            withContext(Dispatchers.Main) {
                if (settings != null) {
                    scheduleTheNextReminder(
                        settings,
                        nextScheduledReminders,
                        this@NextReminderService,
                        isForegroundEnabled,
                        dayString
                    )
                }
            }
        }
        return START_REDELIVER_INTENT
    }

    private suspend fun getTomorrowsReminders(
        nextTimeForReminderTomorrow: Long,
        timeInMilliseconds: Long
    ) = mRepository.getNextScheduledReminderTomorrow(
        nextTimeForReminderTomorrow,
        tomorrowDayOfTheWeek.toString(),
        getDayDate(timeInMilliseconds + 86400000),
        getMonthNumber(timeInMilliseconds + 86400000),
        Integer.parseInt(getYear(timeInMilliseconds + 86400000))
    )

    private fun scheduleTheNextReminder(
        settings: AppSettings, nextScheduledReminders: List<Reminder>,
        context: NextReminderService, isForegroundEnabled: Boolean, dayString: String
    ) {
        val title: String
        val text = getString(R.string.tap_to_disable_sticky_notification)
        val notificationToneUri: Uri? = settings.notificationToneUri
        val isVibrate: Boolean = settings.isVibrate

        val names = Array(nextScheduledReminders.size) { "" }
        val categories = Array(nextScheduledReminders.size) { "" }

        nextScheduledReminders.forEachIndexed { index, nextScheduledReminder: Reminder ->
            nextScheduledReminder.reminderName?.let { name ->
                names[index] = name
                categories[index] = nextScheduledReminder.category.toString()
            }

        }

        if (nextScheduledReminders.isNotEmpty()) {
            val nextScheduledReminder = nextScheduledReminders.first()
            title = getString(
                R.string.next_reminder_dhuhr_prayer_at_12_45,
                nextScheduledReminder.reminderName,
                dayString,
                formatTimeInMilliseconds(context, nextScheduledReminder.timeInMilliseconds)
            )

            notificationToneUri?.let {
                ReminderManager.getInstance().scheduleReminder(
                    context = context,
                    title = getString(R.string.reminder),
                    texts = names,
                    categories = categories,
                    timeInMilliseconds = nextScheduledReminder.timeInMilliseconds + (nextScheduledReminder.offsetInMinutes * 60 * 1000),
                    notificationUri = it,
                    isVibrate = isVibrate,
                    doNotDisturbMinutes = settings.doNotDisturbMinutes,
                    useReliableAlarms = settings.useReliableAlarms
                )

            }
        } else {
            //A dummy notification which enables scheduling reminders for the next day

            title = getString(R.string.no_upcoming_reminder_today)
            notificationToneUri?.let {
                ReminderManager.getInstance().scheduleReminder(
                    context = context,
                    title = "",
                    texts = arrayOf(""),
                    categories = arrayOf("null"),
                    timeInMilliseconds = (-TimeZone.getDefault().rawOffset + 10).toLong(),
                    notificationUri = it,
                    isVibrate = isVibrate,
                    doNotDisturbMinutes = settings.doNotDisturbMinutes,
                    useReliableAlarms = settings.useReliableAlarms
                )
            }
        }

        val stickyNotification: Notification = createNotification(
            context, title, text, NotificationCompat.PRIORITY_LOW, notificationToneUri, isVibrate
        )
        if (isForegroundEnabled)
            startForeground(1, stickyNotification)
        else
            context.stopForeground(true)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}