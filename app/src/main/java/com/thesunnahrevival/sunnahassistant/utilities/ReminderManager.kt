package com.thesunnahrevival.sunnahassistant.utilities

import android.app.AlarmManager
import android.app.AlarmManager.AlarmClockInfo
import android.app.AlarmManager.RTC_WAKEUP
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.provider.Settings
import androidx.annotation.RequiresApi
import com.thesunnahrevival.sunnahassistant.R
import com.thesunnahrevival.sunnahassistant.data.SunnahAssistantRepository
import com.thesunnahrevival.sunnahassistant.data.model.AppSettings
import com.thesunnahrevival.sunnahassistant.data.model.ToDo
import com.thesunnahrevival.sunnahassistant.receivers.ToDoBroadcastReceiver
import java.util.Calendar
import java.util.GregorianCalendar
import java.util.TimeZone
import androidx.core.net.toUri

class ReminderManager private constructor() {

    suspend fun refreshScheduledReminders(applicationContext: Context) {
        val repository = SunnahAssistantRepository.getInstance(applicationContext)
        val settings = repository.getAppSettingsValue()
        val isForegroundEnabled = settings?.showNextToDoNotification ?: false

        val timeInMilliseconds = System.currentTimeMillis()
        var dayString = applicationContext.getString(R.string.at)

        val nextTimeForToDoToday = repository.getNextTimeForToDosForDay(
            calculateOffsetFromMidnight(),
            dayOfTheWeek.toString(),
            getDayDate(timeInMilliseconds),
            getMonthNumber(timeInMilliseconds), Integer.parseInt(getYear(timeInMilliseconds))
        )

        val nextTimeForToDoTomorrow = repository.getNextTimeForToDosForDay(
            -(86400 - calculateOffsetFromMidnight()),
            tomorrowDayOfTheWeek.toString(),
            getDayDate(timeInMilliseconds + 86400000),
            getMonthNumber(timeInMilliseconds + 86400000),
            Integer.parseInt(getYear(timeInMilliseconds + 86400000))
        )

        val nextScheduledToDos = arrayListOf<ToDo>()

        when {
            //Check to see if tomorrows reminders trigger time is offset to earlier than today reminders
            nextTimeForToDoTomorrow != null &&
                    nextTimeForToDoTomorrow < 0 &&
                    ((24 * 60 * 60) + nextTimeForToDoTomorrow) < (nextTimeForToDoToday
                ?: (24 * 60 * 60)) -> {
                nextScheduledToDos.addAll(
                    getTomorrowsReminders(repository, nextTimeForToDoTomorrow, timeInMilliseconds)
                )
            }

            nextTimeForToDoToday != null -> {
                //Get Today Reminders
                nextScheduledToDos.addAll(
                    repository.getNextScheduledToDosForDay(
                        nextTimeForToDoToday,
                        dayOfTheWeek.toString(),
                        getDayDate(timeInMilliseconds),
                        getMonthNumber(timeInMilliseconds),
                        Integer.parseInt(getYear(timeInMilliseconds))
                    )
                )

                //Check to see if tomorrows reminders trigger time is offset to same as today reminders
                if (nextTimeForToDoTomorrow != null &&
                    ((24 * 60 * 60) + nextTimeForToDoTomorrow) == nextTimeForToDoToday
                ) {
                    nextScheduledToDos.addAll(
                        getTomorrowsReminders(
                            repository,
                            nextTimeForToDoTomorrow,
                            timeInMilliseconds
                        )
                    )
                }
            }

            nextTimeForToDoTomorrow != null -> {
                nextScheduledToDos.addAll(
                    getTomorrowsReminders(repository, nextTimeForToDoTomorrow, timeInMilliseconds)
                )
            }
        }

        if (nextTimeForToDoToday == null && nextTimeForToDoTomorrow != null) {
            dayString = applicationContext.getString(R.string.tomorrow_at_notification)
        }

        if (settings != null) {
            scheduleTheNextReminder(
                settings,
                nextScheduledToDos,
                applicationContext,
                isForegroundEnabled,
                dayString
            )
            updateWidgets(applicationContext)
        }
    }

    private suspend fun getTomorrowsReminders(
        repository: SunnahAssistantRepository,
        nextTimeForReminderTomorrow: Long,
        timeInMilliseconds: Long
    ) = repository.getNextScheduledToDosForDay(
        nextTimeForReminderTomorrow,
        tomorrowDayOfTheWeek.toString(),
        getDayDate(timeInMilliseconds + 86400000),
        getMonthNumber(timeInMilliseconds + 86400000),
        Integer.parseInt(getYear(timeInMilliseconds + 86400000))
    )

    private fun scheduleTheNextReminder(
        settings: AppSettings, nextScheduledToDos: List<ToDo>,
        context: Context, isForegroundEnabled: Boolean, dayString: String
    ) {
        val title: String
        val text = context.getString(R.string.tap_to_disable_sticky_notification)
        val notificationToneUri: Uri? = settings.notificationToneUri
        val isVibrate: Boolean = settings.isVibrate

        val names = HashMap<Int, String>()
        val categories = HashMap<Int, String>()

        nextScheduledToDos.forEach { nextScheduledToDo: ToDo ->
            nextScheduledToDo.name?.let { name ->
                names[nextScheduledToDo.id] = name
                categories[nextScheduledToDo.id] = nextScheduledToDo.category.toString()
            }

        }

        if (nextScheduledToDos.isNotEmpty()) {
            val nextScheduledReminder = nextScheduledToDos.first()
            title = context.getString(
                R.string.next_to_do_dhuhr_prayer_at_12_45,
                nextScheduledReminder.name,
                dayString,
                formatTimeInMilliseconds(context, nextScheduledReminder.timeInMilliseconds)
            )

            notificationToneUri?.let {
                scheduleReminder(
                    context = context,
                    title = context.getString(R.string.reminder),
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

            title = context.getString(R.string.no_upcoming_to_do_today)
            notificationToneUri?.let {
                scheduleReminder(
                    context = context,
                    title = "",
                    texts = mapOf(),
                    categories = mapOf(),
                    timeInMilliseconds =
                    (-TimeZone.getDefault().rawOffset + 10).toLong() + (9 * 60 * 60 * 1000), //9AM local time
                    notificationUri = it,
                    isVibrate = isVibrate,
                    doNotDisturbMinutes = settings.doNotDisturbMinutes,
                    useReliableAlarms = settings.useReliableAlarms
                )
            }
        }
    }

    /**
     * Schedule A reminder to fire at a later time
     */
    fun scheduleReminder(
        context: Context,
        title: String,
        texts: Map<Int, String>,
        categories: Map<Int, String>,
        timeInMilliseconds: Long,
        notificationUri: Uri,
        isVibrate: Boolean,
        doNotDisturbMinutes: Int,
        calculateDelayFromMidnight: Boolean = true,
        isOneShot: Boolean = false,
        useReliableAlarms: Boolean = false,
        isSnooze: Boolean = false
    ) {

        val pendingIntent = createNotificationPendingIntent(
            context,
            title,
            texts,
            categories,
            notificationUri,
            isVibrate,
            doNotDisturbMinutes,
            isOneShot,
            isSnooze
        )

        val delay = if (calculateDelayFromMidnight)
            calculateDelayFromMidnight(timeInMilliseconds)
        else
            timeInMilliseconds

        if (delay - System.currentTimeMillis() < 0) //In the past
            return

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val alarm = AlarmClockInfo(delay, null)
        val canScheduleExactAlarms: Boolean = if (VERSION.SDK_INT >= VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }

        if (canScheduleExactAlarms) {
            if (useReliableAlarms) {
                alarmManager.setAlarmClock(alarm, pendingIntent)
            } else {
                if (VERSION.SDK_INT >= VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(RTC_WAKEUP, delay, pendingIntent)
                } else {
                    alarmManager.setExact(RTC_WAKEUP, delay, pendingIntent)
                }
            }
        } else {
            showNotificationForRequestingAlarmPermission(context)
        }
    }

    /**
     * Method for creating notification and PendingIntent
     */
    private fun createNotificationPendingIntent(
        context: Context,
        title: String,
        text: Map<Int, String>,
        category: Map<Int, String>,
        notificationUri: Uri,
        isVibrate: Boolean,
        doNotDisturbMinutes: Int,
        isOneShot: Boolean = false,
        isSnooze: Boolean = false
    ): PendingIntent {
        val notificationIntent = Intent(context, ToDoBroadcastReceiver::class.java)
        notificationIntent.putExtra(NOTIFICATION_TITLE, title)
        notificationIntent.putExtra(NOTIFICATION_TEXT, text as java.io.Serializable)
        notificationIntent.putExtra(NOTIFICATION_TONE_URI, notificationUri.toString())
        notificationIntent.putExtra(NOTIFICATION_VIBRATE, isVibrate)
        notificationIntent.putExtra(NOTIFICATION_CATEGORY, category as java.io.Serializable)
        notificationIntent.putExtra(NOTIFICATION_DND_MINUTES, doNotDisturbMinutes)
        return when {
            !isOneShot -> {
                val flags = if (VERSION.SDK_INT >= VERSION_CODES.M) {
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                } else {
                    PendingIntent.FLAG_UPDATE_CURRENT
                }
                PendingIntent.getBroadcast(
                    context,
                    if (isSnooze) System.currentTimeMillis().toInt() else -1,
                    notificationIntent,
                    flags
                )
            }

            else -> {
                val flags = if (VERSION.SDK_INT >= VERSION_CODES.M) {
                    PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
                } else {
                    PendingIntent.FLAG_ONE_SHOT
                }
                PendingIntent.getBroadcast(
                    context,
                    -2,
                    notificationIntent,
                    flags
                )
            }
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

    fun showNotificationForRequestingAlarmPermission(
        context: Context
    ) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(STICKY_NOTIFICATION_ID)
        notificationManager.notify(
            REQUEST_ALARM_PERMISSION_CODE,
            createNotification(
                context,
                getToDoNotificationChannel(context),
                context.getString(R.string.fix_alarms_and_reminders),
                context.getString(R.string.tap_to_fix_this),
                onlyAlertOnce = true,
                pendingIntent = if (VERSION.SDK_INT >= VERSION_CODES.S) {
                    getOpenAlarmSettingsPendingIntent(context)
                } else {
                    null
                }
            )
        )
    }

    @RequiresApi(VERSION_CODES.S)
    private fun getOpenAlarmSettingsPendingIntent(context: Context): PendingIntent {
        val openSettingsIntent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
            data = "package:${context.packageName}".toUri()
            addFlags(FLAG_ACTIVITY_NEW_TASK)
        }
        return PendingIntent.getActivity(context, -3, openSettingsIntent, PendingIntent.FLAG_IMMUTABLE)
    }

    companion object {


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