package com.thesunnahrevival.sunnahassistant.services

import android.app.AlarmManager
import android.app.Notification
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.thesunnahrevival.sunnahassistant.utilities.NOTIFICATION_TEXT
import com.thesunnahrevival.sunnahassistant.utilities.NOTIFICATION_TITLE
import com.thesunnahrevival.sunnahassistant.utilities.NOTIFICATION_TONE_URI
import com.thesunnahrevival.sunnahassistant.utilities.NOTIFICATION_VIBRATE
import com.thesunnahrevival.sunnahassistant.utilities.ReminderManager
import com.thesunnahrevival.sunnahassistant.utilities.STICKY_NOTIFICATION_ID
import com.thesunnahrevival.sunnahassistant.utilities.createNotification


class NextToDoService : Service() {

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val notificationTitle = intent.getStringExtra(NOTIFICATION_TITLE)
        val notificationText = intent.getStringExtra(NOTIFICATION_TEXT)
        val notificationToneUri = intent.getParcelableExtra(NOTIFICATION_TONE_URI)
            ?: RingtoneManager.getActualDefaultRingtoneUri(
                this, RingtoneManager.TYPE_NOTIFICATION
            )
        val isVibrate = intent.getBooleanExtra(NOTIFICATION_VIBRATE, false)

        val alarmManager = this.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            ReminderManager.getInstance().showNotificationForRequestingAlarmPermission(
                this,
                notificationToneUri,
                isVibrate
            )
            stopService()
        } else {
            val stickyNotification: Notification = createNotification(
                applicationContext,
                null,
                notificationTitle,
                notificationText,
                NotificationCompat.PRIORITY_LOW,
                notificationToneUri,
                isVibrate
            )

            try {
                startForeground(STICKY_NOTIFICATION_ID, stickyNotification)
            } catch (exception: Exception) {
                stopService()
            }
        }

        return START_REDELIVER_INTENT
    }

    private fun NextToDoService.stopService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            stopForeground(true)
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}