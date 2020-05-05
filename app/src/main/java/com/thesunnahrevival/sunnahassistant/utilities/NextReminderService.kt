package com.thesunnahrevival.sunnahassistant.utilities

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.net.Uri
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.thesunnahrevival.sunnahassistant.data.local.SunnahAssistantDatabase.Companion.getInstance
import com.thesunnahrevival.sunnahassistant.data.model.AppSettings
import com.thesunnahrevival.sunnahassistant.data.model.PrayerTimeCalculator
import com.thesunnahrevival.sunnahassistant.data.model.Reminder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*


class NextReminderService : Service() {

    private val mReminderDAO = getInstance(this).reminderDao()
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val context = this
        CoroutineScope(Dispatchers.IO).launch{
            val isForegroundEnabled = mReminderDAO.isForegroundEnabled
            val settings = mReminderDAO.appSettingsValue

            checkIfPrayerTimesNeedToBeUpdated(settings)

            val nextScheduledReminder = mReminderDAO.getNextScheduledReminder(
                    TimeDateUtil.calculateOffsetFromMidnight(),
                    TimeDateUtil.getNameOfTheDay(System.currentTimeMillis()), TimeDateUtil.getDayDate(System.currentTimeMillis()),
                    TimeDateUtil.getMonthNumber(System.currentTimeMillis()), Integer.parseInt(TimeDateUtil.getYear(System.currentTimeMillis())))

            withContext(Dispatchers.Main){
                scheduleTheNextReminder(settings, nextScheduledReminder, context, isForegroundEnabled)
            }
        }
        return START_REDELIVER_INTENT
    }

    private fun checkIfPrayerTimesNeedToBeUpdated(settings: AppSettings) {
        if (settings.month != TimeDateUtil.getMonthNumber(System.currentTimeMillis())) {
            val prayerTimesReminders = PrayerTimeCalculator(settings.latitude.toDouble(), settings.longitude.toDouble(), settings.method, settings.asrCalculationMethod, settings.latitudeAdjustmentMethod).getPrayerTimeReminders()
            for (prayerTimeReminder in prayerTimesReminders) {
                mReminderDAO.updateGeneratedPrayerTimes(prayerTimeReminder.id, TimeDateUtil.getMonthNumber(System.currentTimeMillis()), TimeDateUtil.getYear(System.currentTimeMillis()).toInt(), prayerTimeReminder.timeInSeconds)
            }
            settings.month = TimeDateUtil.getMonthNumber(System.currentTimeMillis())
            mReminderDAO.updateAppSettings(settings)
        }
    }

    private fun scheduleTheNextReminder(settings: AppSettings, nextScheduledReminder: Reminder?, context: NextReminderService, isForegroundEnabled: Boolean) {
        val title: String
        val text = "Tap to disable this Sticky Notification"
        val notificationToneUri: Uri = settings.notificationToneUri
        val isVibrate: Boolean = settings.isVibrate

        if (nextScheduledReminder != null && nextScheduledReminder.isEnabled) {
            title = "Next Reminder: " + nextScheduledReminder.reminderName
                    .toString() + " at " + TimeDateUtil.formatTimeInMilliseconds(
                    context,
                    nextScheduledReminder.timeInMilliSeconds)
            ReminderManager.getInstance().scheduleReminder(
                    context, "Reminder",
                    nextScheduledReminder.reminderName, nextScheduledReminder.timeInMilliSeconds, notificationToneUri, isVibrate)
        } else {
            title = "No Upcoming Reminder Today"
            ReminderManager.getInstance().scheduleReminder(
                    context, "", "",
                    (-TimeZone.getDefault().rawOffset + 10).toLong(), notificationToneUri, isVibrate
            )
        }


        val notification: Notification = NotificationUtil.createNotification(
                context, title, text, NotificationCompat.PRIORITY_LOW, notificationToneUri, isVibrate)

        if (isForegroundEnabled)
            context.startForeground(1, notification)
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }
}