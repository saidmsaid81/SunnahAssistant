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
import com.thesunnahrevival.sunnahassistant.utilities.DOWNLOADS_NOTIFICATION_CHANNEL_ID
import com.thesunnahrevival.sunnahassistant.utilities.DOWNLOAD_NOTIFICATION_ID
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
        setForeground(
            createForegroundInfo(
                0,
                0,
                true,
                applicationContext.getString(R.string.calculating)
            )
        )

        CoroutineScope(Dispatchers.Main).launch {
            downloadManager.downloadProgress.takeWhile { downloadProgress ->
                !isStopped && downloadProgress !is Completed && downloadProgress !is Cancelled
            }.collect { downloadProgress ->
                when (downloadProgress) {
                    Preparing -> setForeground(
                        createForegroundInfo(
                            0,
                            0,
                            true,
                            applicationContext.getString(R.string.calculating)
                        )
                    )
                    is Downloading -> {
                        val fileSize = downloadProgress.fileSize
                        val totalDownloadedSize = downloadProgress.totalDownloadedSize
                        val progress = Math.round((totalDownloadedSize / fileSize) * 100)
                        setForeground(
                            createForegroundInfo(
                                100,
                                progress,
                                false,
                                applicationContext.getString(
                                    R.string.downloaded,
                                    totalDownloadedSize.toString(),
                                    fileSize.toString(),
                                    downloadProgress.unit
                                )
                            )
                        )
                    }

                    Extracting -> setForeground(
                        createForegroundInfo(
                            0,
                            0,
                            true,
                            applicationContext.getString(R.string.extracting)
                        )
                    )
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
        indeterminate: Boolean,
        message: String
    ): ForegroundInfo {
        val intent = WorkManager.getInstance(applicationContext)
            .createCancelPendingIntent(id)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val downloadsChannel = NotificationChannel(
                DOWNLOADS_NOTIFICATION_CHANNEL_ID,
                applicationContext.getString(R.string.file_downloads_notification_channel),
                NotificationManager.IMPORTANCE_LOW
            )
            downloadsChannel.description =
                applicationContext.getString(R.string.used_in_showing_download_progress_when_downloading_files_in_sunnah_assistant)

            val notificationManager =
                applicationContext.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(downloadsChannel)
        }

        val notification =
            NotificationCompat.Builder(applicationContext, DOWNLOADS_NOTIFICATION_CHANNEL_ID)
                .setContentTitle(applicationContext.getString(R.string.downloading_quran_files_please_wait))
                .setContentText(message)
                .setTicker(applicationContext.getString(R.string.app_name))
                .setProgress(max, progress, indeterminate)
                .setSmallIcon(R.drawable.ic_downloading)
                .setOngoing(true)
                .addAction(
                    android.R.drawable.ic_delete,
                    applicationContext.getString(R.string.cancel),
                    intent
                )
                .build()

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ForegroundInfo(
                DOWNLOAD_NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            ForegroundInfo(DOWNLOAD_NOTIFICATION_ID, notification)
        }
    }
}