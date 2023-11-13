package com.thesunnahrevival.sunnahassistant.firebase

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.thesunnahrevival.sunnahassistant.utilities.FIREBASE_NOTIFICATION_ID
import com.thesunnahrevival.sunnahassistant.utilities.createNotification
import com.thesunnahrevival.sunnahassistant.views.MainActivity

class SAFirebaseMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // Check if message contains a notification payload.
        remoteMessage.notification?.let {
            val notification = createNotification(
                applicationContext,
                null,
                it.title,
                it.body,
                NotificationCompat.PRIORITY_DEFAULT,
                null,
                isVibrate = false,
                isFCMMessage = true
            )

            // Check if message contains a data payload.
            if (remoteMessage.data.isNotEmpty() && remoteMessage.data["link"] != null) {
                val intent = Intent(applicationContext, MainActivity::class.java)
                intent.putExtra("link", remoteMessage.data["link"])
                val flag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    PendingIntent.FLAG_IMMUTABLE
                } else {
                    0
                }
                val activity =
                    PendingIntent.getActivity(applicationContext, 0, intent, flag)
                notification.contentIntent = activity
            }
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(FIREBASE_NOTIFICATION_ID, notification)
        }

    }

}