package com.thesunnahrevival.sunnahassistant.receivers

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.thesunnahrevival.sunnahassistant.utilities.DOWNLOAD_COMPLETE_NOTIFICATION_ID
import com.thesunnahrevival.sunnahassistant.viewmodels.DOWNLOAD_WORK_TAG
import com.thesunnahrevival.sunnahassistant.workers.DownloadWorker

class DownloadRetryReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(DOWNLOAD_COMPLETE_NOTIFICATION_ID)

        WorkManager.getInstance(context).cancelAllWorkByTag(DOWNLOAD_WORK_TAG)

        val downloadRequest = OneTimeWorkRequestBuilder<DownloadWorker>()
            .addTag(DOWNLOAD_WORK_TAG)
            .build()

        WorkManager.getInstance(context).enqueue(downloadRequest)
    }
}