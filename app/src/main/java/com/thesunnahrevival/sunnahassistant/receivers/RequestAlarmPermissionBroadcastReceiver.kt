package com.thesunnahrevival.sunnahassistant.receivers

import android.app.AlarmManager.ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import com.thesunnahrevival.sunnahassistant.utilities.REQUEST_ALARM_PERMISSION_CODE
import com.thesunnahrevival.sunnahassistant.workers.ReminderSchedulerWorker

class RequestAlarmPermissionBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context != null) {
            val action = intent?.action

            if (action?.matches(ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED.toRegex()) == true) {
                val notificationManager =
                    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.cancel(REQUEST_ALARM_PERMISSION_CODE)

                val refreshRemindersRequest = OneTimeWorkRequestBuilder<ReminderSchedulerWorker>()
                    .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                    .build()

                WorkManager.getInstance(context)
                    .enqueue(refreshRemindersRequest)
            }
        }
    }
}