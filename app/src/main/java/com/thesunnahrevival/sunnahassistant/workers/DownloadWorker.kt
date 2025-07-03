package com.thesunnahrevival.sunnahassistant.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.thesunnahrevival.sunnahassistant.R
import com.thesunnahrevival.sunnahassistant.utilities.DownloadManager
import com.thesunnahrevival.sunnahassistant.utilities.DownloadManager.Cancelled
import com.thesunnahrevival.sunnahassistant.utilities.DownloadManager.Completed
import com.thesunnahrevival.sunnahassistant.utilities.DownloadManager.Downloading
import com.thesunnahrevival.sunnahassistant.utilities.DownloadManager.Extracting
import com.thesunnahrevival.sunnahassistant.utilities.DownloadManager.Preparing
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.launch

class DownloadWorker(context: Context, parameters: WorkerParameters) :
    CoroutineWorker(context, parameters) {

    private val downloadManager = DownloadManager.getInstance()

    override suspend fun doWork(): Result {
        setForeground(createForegroundInfo(0, 0, true))

        CoroutineScope(Dispatchers.Main).launch {
            downloadManager.downloadProgress.takeWhile { downloadProgress ->
                !isStopped && downloadProgress !is Completed && downloadProgress !is Cancelled
            }.collect { downloadProgress ->
                when (downloadProgress) {
                    Preparing -> setForeground(createForegroundInfo(0, 0, true))
                    is Downloading -> {
                        val totalFileSize = downloadProgress.totalFileSize
                        val totalDownloadedSize = downloadProgress.totalDownloadedSize
                        val progress = Math.round((totalDownloadedSize / totalFileSize) * 100)
                        setForeground(createForegroundInfo(100, progress, false))
                    }

                    Extracting -> setForeground(createForegroundInfo(0, 0, true))
                    else -> {}
                }
            }
        }

        downloadManager.downloadFile(applicationContext)


        CoroutineScope(Dispatchers.Main).launch {
            val finalStatus = downloadManager.downloadProgress.first { downloadProgress ->
                downloadProgress is Completed || downloadProgress is Cancelled
            }

            when (finalStatus) {
                Completed -> Result.success()
                Cancelled -> Result.failure()
                else -> Result.failure()
            }
        }

        return Result.success()
    }

    private fun createForegroundInfo(
        max: Int,
        progress: Int,
        indeterminate: Boolean
    ): ForegroundInfo {
        val intent = WorkManager.getInstance(applicationContext)
            .createCancelPendingIntent(id)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val downloadsChannel = NotificationChannel(
                "Downloads",
                applicationContext.getString(R.string.file_downloads_notification_channel),
                NotificationManager.IMPORTANCE_LOW
            )
            downloadsChannel.description =
                "Used in showing download progress when downloading files in Sunnah Assistant"

            val notificationManager =
                applicationContext.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(downloadsChannel)
        }

        val notification = NotificationCompat.Builder(applicationContext, "Downloads")
            .setContentTitle(applicationContext.getString(R.string.downloading_quran_files_please_wait))
            .setTicker(applicationContext.getString(R.string.app_name))
            .setProgress(max, progress, indeterminate)
            .setSmallIcon(R.drawable.ic_info)
            .setOngoing(true)
            .addAction(android.R.drawable.ic_delete, "Cancel", intent)
            .build()

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ForegroundInfo(
                -6,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            ForegroundInfo(-6, notification)
        }
    }
}