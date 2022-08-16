package com.thesunnahrevival.common.utilities

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.net.Uri
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.thesunnahrevival.common.R
import com.thesunnahrevival.common.data.local.ReminderDao
import com.thesunnahrevival.common.data.local.SunnahAssistantDatabase.Companion.getInstance
import com.thesunnahrevival.common.data.model.AppSettings
import com.thesunnahrevival.common.data.model.PrayerTimeCalculator
import com.thesunnahrevival.common.data.model.Reminder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*


class NextReminderService : Service() {

    private lateinit var mReminderDAO: ReminderDao

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val context = this
        mReminderDAO = getInstance(context).reminderDao()

        CoroutineScope(Dispatchers.IO).launch {
            val settings = mReminderDAO.getAppSettingsValue()
            val isForegroundEnabled = settings?.showNextReminderNotification ?: false

            if (settings != null && settings.month != getMonthNumber(System.currentTimeMillis())) {
                updateGeneratedPrayerTimes(settings)
            }

            var timeInMilliseconds = System.currentTimeMillis()
            var dayString = getString(R.string.at)

            var nextScheduledReminder = mReminderDAO.getNextScheduledReminderToday(
                    calculateOffsetFromMidnight(),
                    dayOfTheWeek.toString(),
                    getDayDate(timeInMilliseconds),
                    getMonthNumber(timeInMilliseconds), Integer.parseInt(getYear(timeInMilliseconds)))
            if (nextScheduledReminder == null) {
                timeInMilliseconds = System.currentTimeMillis() + 86400000
                dayString = getString(R.string.tomorrow_at_notification)
                nextScheduledReminder = mReminderDAO.getNextScheduledReminderTomorrow(
                        tomorrowDayOfTheWeek.toString(), getDayDate(timeInMilliseconds),
                        getMonthNumber(timeInMilliseconds), Integer.parseInt(getYear(timeInMilliseconds)))
            }

            withContext(Dispatchers.Main) {
                if (settings != null) {
                    scheduleTheNextReminder(settings, nextScheduledReminder, context, isForegroundEnabled, dayString)
                }
            }
        }
        return START_REDELIVER_INTENT
    }

    private suspend fun updateGeneratedPrayerTimes(settings: AppSettings) {
        val prayerTimesReminders = PrayerTimeCalculator(
                settings.latitude.toDouble(), settings.longitude.toDouble(), settings.calculationMethod,
                settings.asrCalculationMethod, settings.latitudeAdjustmentMethod,
                application.resources.getStringArray(R.array.prayer_names),
                application.resources.getStringArray(R.array.categories)[2]).getPrayerTimeReminders()

        for (prayerTimeReminder in prayerTimesReminders) {
            mReminderDAO.updateGeneratedPrayerTimes(prayerTimeReminder.id,
                    getMonthNumber(System.currentTimeMillis()),
                    getYear(System.currentTimeMillis()).toInt(), prayerTimeReminder.timeInSeconds)
        }

        settings.month = getMonthNumber(System.currentTimeMillis())
        mReminderDAO.updateAppSettings(settings)

    }

    private fun scheduleTheNextReminder(settings: AppSettings, nextScheduledReminder: Reminder?,
                                        context: NextReminderService, isForegroundEnabled: Boolean, dayString: String) {
        val title: String
        val text = getString(R.string.tap_to_disable_sticky_notification)
        val notificationToneUri: Uri? = settings.notificationToneUri
        val isVibrate: Boolean = settings.isVibrate

        if (nextScheduledReminder != null && nextScheduledReminder.isEnabled) {
            title = getString(R.string.next_reminder_dhuhr_prayer_at_12_45,
                    nextScheduledReminder.reminderName,
                    dayString,
                    formatTimeInMilliseconds(context, nextScheduledReminder.timeInMilliseconds))
            notificationToneUri?.let {
                nextScheduledReminder.reminderName?.let { name ->
                    ReminderManager.getInstance().scheduleReminder(
                        context = context,
                        title = getString(R.string.reminder),
                        text = name,
                        category = nextScheduledReminder.category,
                        timeInMilliseconds = nextScheduledReminder.timeInMilliseconds + (nextScheduledReminder.offsetInMinutes * 60 * 1000),
                        notificationUri = it,
                        isVibrate = isVibrate,
                        doNotDisturbMinutes = settings.doNotDisturbMinutes,
                        useReliableAlarms = settings.useReliableAlarms
                    )
                }
            }
        } else {

            //A dummy notification which enables scheduling reminders for the next day

            title = getString(R.string.no_upcoming_reminder_today)
            notificationToneUri?.let {
                ReminderManager.getInstance().scheduleReminder(
                        context = context, title = "", text = "", category = "null",
                        timeInMilliseconds = (-TimeZone.getDefault().rawOffset + 10).toLong(), notificationUri = it, isVibrate = isVibrate, doNotDisturbMinutes = settings.doNotDisturbMinutes,
                        useReliableAlarms = settings.useReliableAlarms
                )
            }
        }


        val stickyNotification: Notification = createNotification(
                context, title, text, NotificationCompat.PRIORITY_LOW, notificationToneUri, isVibrate)
        if (isForegroundEnabled)
            startForeground(1, stickyNotification)
        else
            context.stopForeground(true)
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }
}