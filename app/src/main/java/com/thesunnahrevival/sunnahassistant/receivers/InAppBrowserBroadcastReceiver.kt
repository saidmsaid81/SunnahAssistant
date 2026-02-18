package com.thesunnahrevival.sunnahassistant.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import com.thesunnahrevival.sunnahassistant.R
import com.thesunnahrevival.sunnahassistant.utilities.PREDEFINED_TO_DO_ID
import com.thesunnahrevival.sunnahassistant.utilities.SET_REMINDER_FROM_BROWSER
import com.thesunnahrevival.sunnahassistant.utilities.LINK
import com.thesunnahrevival.sunnahassistant.utilities.getSunnahAssistantAppLink
import com.thesunnahrevival.sunnahassistant.views.MainActivity

class InAppBrowserBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == SET_REMINDER_FROM_BROWSER) {
            val toDoId = intent.getIntExtra(PREDEFINED_TO_DO_ID, 0)
            if (toDoId != 0) {
                val openIntent = Intent(context, MainActivity::class.java).apply {
                    putExtra(PREDEFINED_TO_DO_ID, toDoId)
                    addFlags(FLAG_ACTIVITY_NEW_TASK or FLAG_ACTIVITY_CLEAR_TOP)
                }
                context.startActivity(openIntent)
            }
            return
        }

        val link = intent.getStringExtra(LINK)
        if (link != null) {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(
                Intent.EXTRA_TEXT,
                "$link\n\n" +
                        context.getString(R.string.app_promotional_message, getSunnahAssistantAppLink(utmCampaign = "In-App-Browser"))
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
