package com.thesunnahrevival.sunnahassistant.services

import android.app.ForegroundServiceStartNotAllowedException
import android.app.Notification
import android.app.Service
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.thesunnahrevival.sunnahassistant.R
import com.thesunnahrevival.sunnahassistant.data.SunnahAssistantRepository
import com.thesunnahrevival.sunnahassistant.data.model.AppSettings
import com.thesunnahrevival.sunnahassistant.data.model.ToDo
import com.thesunnahrevival.sunnahassistant.utilities.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*


class NextToDoService : Service() {

    private lateinit var mRepository: SunnahAssistantRepository

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        mRepository = SunnahAssistantRepository.getInstance(this.application)

        CoroutineScope(Dispatchers.IO).launch {
            val settings = mRepository.getAppSettingsValue()
            val isForegroundEnabled = settings?.showNextToDoNotification ?: false

            val timeInMilliseconds = System.currentTimeMillis()
            var dayString = getString(R.string.at)

            val nextTimeForToDoToday = mRepository.getNextTimeForToDosForDay(
                calculateOffsetFromMidnight(),
                dayOfTheWeek.toString(),
                getDayDate(timeInMilliseconds),
                getMonthNumber(timeInMilliseconds), Integer.parseInt(getYear(timeInMilliseconds))
            )

            val nextTimeForToDoTomorrow = mRepository.getNextTimeForToDosForDay(
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
                        getTomorrowsReminders(nextTimeForToDoTomorrow, timeInMilliseconds)
                    )
                }
                nextTimeForToDoToday != null -> {
                    //Get Today Reminders
                    nextScheduledToDos.addAll(
                        mRepository.getNextScheduledToDosForDay(
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
                            getTomorrowsReminders(nextTimeForToDoTomorrow, timeInMilliseconds)
                        )
                    }
                }
                nextTimeForToDoTomorrow != null -> {
                    nextScheduledToDos.addAll(
                        getTomorrowsReminders(nextTimeForToDoTomorrow, timeInMilliseconds)
                    )
                }
            }

            if (nextTimeForToDoToday == null && nextTimeForToDoTomorrow != null) {
                dayString = getString(R.string.tomorrow_at_notification)
            }

            withContext(Dispatchers.Main) {
                if (settings != null) {
                    scheduleTheNextReminder(
                        settings,
                        nextScheduledToDos,
                        this@NextToDoService,
                        isForegroundEnabled,
                        dayString
                    )
                    updateWidgets(this@NextToDoService)
                }
            }
        }
        return START_REDELIVER_INTENT
    }

    private suspend fun getTomorrowsReminders(
        nextTimeForReminderTomorrow: Long,
        timeInMilliseconds: Long
    ) = mRepository.getNextScheduledToDosForDay(
        nextTimeForReminderTomorrow,
        tomorrowDayOfTheWeek.toString(),
        getDayDate(timeInMilliseconds + 86400000),
        getMonthNumber(timeInMilliseconds + 86400000),
        Integer.parseInt(getYear(timeInMilliseconds + 86400000))
    )

    private fun scheduleTheNextReminder(
        settings: AppSettings, nextScheduledToDos: List<ToDo>,
        context: NextToDoService, isForegroundEnabled: Boolean, dayString: String
    ) {
        val title: String
        val text = getString(R.string.tap_to_disable_sticky_notification)
        val notificationToneUri: Uri? = settings.notificationToneUri
        val isVibrate: Boolean = settings.isVibrate

        val names = Array(nextScheduledToDos.size) { "" }
        val categories = Array(nextScheduledToDos.size) { "" }

        nextScheduledToDos.forEachIndexed { index, nextScheduledToDo: ToDo ->
            nextScheduledToDo.name?.let { name ->
                names[index] = name
                categories[index] = nextScheduledToDo.category.toString()
            }

        }

        if (nextScheduledToDos.isNotEmpty()) {
            val nextScheduledReminder = nextScheduledToDos.first()
            title = getString(
                R.string.next_to_do_dhuhr_prayer_at_12_45,
                nextScheduledReminder.name,
                dayString,
                formatTimeInMilliseconds(context, nextScheduledReminder.timeInMilliseconds)
            )

            notificationToneUri?.let {
                ReminderManager.getInstance().scheduleReminder(
                    context = context,
                    title = getString(R.string.to_do),
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

            title = getString(R.string.no_upcoming_to_do_today)
            notificationToneUri?.let {
                ReminderManager.getInstance().scheduleReminder(
                    context = context,
                    title = "",
                    texts = arrayOf(""),
                    categories = arrayOf("null"),
                    timeInMilliseconds =
                    (-TimeZone.getDefault().rawOffset + 10).toLong() + (9 * 60 * 60 * 1000), //9AM local time
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
        when {
            isForegroundEnabled -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    try {
                        startForeground(1, stickyNotification)
                    } catch (exception: ForegroundServiceStartNotAllowedException) {
                        Log.e("Exception", exception.message.toString())
                    }
                } else
                    startForeground(1, stickyNotification)
            }
            else -> context.stopForeground(true)
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}