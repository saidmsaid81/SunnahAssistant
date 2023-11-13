package com.thesunnahrevival.sunnahassistant.workers

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.thesunnahrevival.sunnahassistant.R
import com.thesunnahrevival.sunnahassistant.utilities.REFRESHING_NOTIFICATIONS_ID
import com.thesunnahrevival.sunnahassistant.utilities.ReminderManager
import com.thesunnahrevival.sunnahassistant.utilities.createNotification

class ReminderSchedulerWorker(context: Context, parameters: WorkerParameters) :
    CoroutineWorker(context, parameters) {
    override suspend fun doWork(): Result {
        ReminderManager.getInstance().refreshScheduledReminders(applicationContext)
        return Result.success()
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        val notification = createNotification(
            applicationContext,
            REFRESHING_NOTIFICATIONS_ID,
            applicationContext.getString(R.string.app_name),
            applicationContext.getString(R.string.refreshing_notifications),
            NotificationCompat.PRIORITY_LOW,
            null,
            false
        )
        return ForegroundInfo(REFRESHING_NOTIFICATIONS_ID, notification)
    }
}