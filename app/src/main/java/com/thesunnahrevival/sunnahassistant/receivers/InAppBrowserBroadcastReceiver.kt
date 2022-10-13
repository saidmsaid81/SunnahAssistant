package com.thesunnahrevival.sunnahassistant.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import com.thesunnahrevival.sunnahassistant.R

const val LINK = "link"

class InAppBrowserBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val link = intent.getStringExtra(LINK)
        if (link != null) {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(
                Intent.EXTRA_TEXT,
                "$link\n\n" +
                        "${context.getString(R.string.get_sunnah_assistant)}\n" +
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