package com.thesunnahrevival.common.utilities

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.util.Log
import com.thesunnahrevival.common.R

const val MESSAGE = "MESSAGE"

class InAppBrowserBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val url = intent.dataString

        if (url != null) {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(
                Intent.EXTRA_TEXT,
                "$url\n\n" +
                        "Sent from Sunnah Assistant App.\n\n" +
                        "Get Sunnah Assistant App at\n" +
                        "https://play.google.com/store/apps/details?id=com.thesunnahrevival.sunnahassistant "
            )
            val chooserIntent = Intent.createChooser(
                shareIntent,
                context.getString(R.string.share_message)
            )
            chooserIntent.flags = FLAG_ACTIVITY_NEW_TASK
            context.startActivity(chooserIntent)
        }
    }

}