package com.thesunnahrevival.sunnahassistant.utilities

import android.app.AlarmManager
import android.app.AlarmManager.AlarmClockInfo
import android.app.AlarmManager.RTC_WAKEUP
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import java.util.*

class ReminderManager private constructor() {

    /**
     * Method for creating notification and PendingIntent
     */
    private fun createNotificationPendingIntent(context: Context, title: String, text: String, category: String?, notificationUri: Uri, isVibrate: Boolean, doNotDisturbMinutes: Int, isOneShot: Boolean = false): PendingIntent {
        val notificationIntent = Intent(context, ReminderBroadcastReceiver::class.java)
        notificationIntent.putExtra(NOTIFICATION_TITLE, title)
        notificationIntent.putExtra(NOTIFICATION_TEXT, text)
        notificationIntent.putExtra(NOTIFICATION_TONE_URI, notificationUri.toString())
        notificationIntent.putExtra(NOTIFICATION_VIBRATE, isVibrate)
        notificationIntent.putExtra(NOTIFICATION_CATEGORY, category)
        notificationIntent.putExtra(NOTIFICATION_DND_MINUTES, doNotDisturbMinutes)
        return if (!isOneShot)
            PendingIntent.getBroadcast(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        else
            PendingIntent.getBroadcast(context, 0, notificationIntent, PendingIntent.FLAG_ONE_SHOT)
    }

    /**
     * Schedule A reminder to fire at a later time
     */
    fun scheduleReminder(context: Context, title: String, text: String, category: String?, timeInMilliseconds: Long,
                         notificationUri: Uri, isVibrate: Boolean, doNotDisturbMinutes: Int,
                         calculateDelayFromMidnight: Boolean = true, isOneShot: Boolean = false, useReliableAlarms: Boolean = false) {

        val pendingIntent = createNotificationPendingIntent(
                context, title, text, category, notificationUri, isVibrate, doNotDisturbMinutes, isOneShot
        )

        val delay = if (calculateDelayFromMidnight)
            calculateDelayFromMidnight(timeInMilliseconds)
        else
            timeInMilliseconds

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val alarm = AlarmClockInfo(delay, null)
        if (useReliableAlarms)
            alarmManager.setAlarmClock(alarm, pendingIntent)
        else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(RTC_WAKEUP, delay, pendingIntent )
            }
            else
                alarmManager.setExact(RTC_WAKEUP, delay, pendingIntent)
        }
    }

    private fun calculateDelayFromMidnight(timeInMilliseconds: Long): Long {
        // today
        val midnight: Calendar = GregorianCalendar()
        // reset hour, minutes, seconds and millis to midnight of that day
        midnight[Calendar.HOUR_OF_DAY] = 0
        midnight[Calendar.MINUTE] = 0
        midnight[Calendar.SECOND] = 0
        midnight[Calendar.MILLISECOND] = 0
        midnight.timeZone = TimeZone.getTimeZone("UTC")
        val delay = midnight.timeInMillis + timeInMilliseconds
        return if (delay <= System.currentTimeMillis()) //Time Has Passed
            delay + 86400000 //Schedule it the next day
        else delay
    }

    companion object {
        val NOTIFICATION_TITLE = "com.thesunnahrevival.sunnahassistant.utilities.notificationTitle"
        val NOTIFICATION_TEXT = "com.thesunnahrevival.sunnahassistant.utilities.notificationText"
        val NOTIFICATION_TONE_URI = "com.thesunnahrevival.sunnahassistant.utilities.notificationToneUri"
        val NOTIFICATION_VIBRATE = "com.thesunnahrevival.sunnahassistant.utilities.notificationVibrate"
        val NOTIFICATION_CATEGORY = "com.thesunnahrevival.sunnahassistant.utilities.notificationCategory"
        val NOTIFICATION_DND_MINUTES = "com.thesunnahrevival.sunnahassistant.utilities.dndMinutes"

        @Volatile
        private var mRemManagerInstance: ReminderManager? = null

        /**
         * Method for getting the instance of @ReminderManager
         */
        fun getInstance(): ReminderManager =
                mRemManagerInstance ?: synchronized(this) {
                    mRemManagerInstance
                            ?: ReminderManager().also { mRemManagerInstance = it }
                }
    }
}