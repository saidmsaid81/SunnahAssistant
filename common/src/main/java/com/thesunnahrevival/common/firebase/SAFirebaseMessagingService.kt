package com.thesunnahrevival.common.firebase

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_CANCEL_CURRENT
import android.content.Context
import android.content.Intent
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.thesunnahrevival.common.utilities.createNotification
import com.thesunnahrevival.common.views.MainActivity

class SAFirebaseMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // Check if message contains a notification payload.
        remoteMessage.notification?.let {
            val notification = createNotification(
                applicationContext,
                it.title,
                it.body,
                Notification.PRIORITY_DEFAULT,
                null,
                false
            )

            // Check if message contains a data payload.
            if (remoteMessage.data.isNotEmpty() && remoteMessage.data["link"] != null) {
                val intent = Intent(applicationContext, MainActivity::class.java)
                intent.putExtra("link", remoteMessage.data["link"])
                val activity =
                    PendingIntent.getActivity(applicationContext, 0, intent, FLAG_CANCEL_CURRENT)
                notification.contentIntent = activity
            }
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(-2, notification)
        }

    }

}