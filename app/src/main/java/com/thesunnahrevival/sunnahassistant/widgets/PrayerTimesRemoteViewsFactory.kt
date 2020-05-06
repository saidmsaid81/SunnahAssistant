package com.thesunnahrevival.sunnahassistant.widgets

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.thesunnahrevival.sunnahassistant.R
import com.thesunnahrevival.sunnahassistant.data.local.SunnahAssistantDatabase
import com.thesunnahrevival.sunnahassistant.data.model.Reminder
import com.thesunnahrevival.sunnahassistant.utilities.TimeDateUtil

class PrayerTimesRemoteViewsFactory(
        private val context: Context,
        private val intent: Intent?
) : RemoteViewsService.RemoteViewsFactory  {

    private lateinit var mPrayerReminders: MutableList<Reminder>

    override fun onCreate() {

    }

    override fun getLoadingView(): RemoteViews? {
        return null
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun onDataSetChanged() {
        val reminderDao = SunnahAssistantDatabase.getInstance(context).reminderDao()
        mPrayerReminders = reminderDao.getPrayerTimesValue(TimeDateUtil.getDayDate(System.currentTimeMillis()),
                TimeDateUtil.getMonthNumber(System.currentTimeMillis()),
                TimeDateUtil.getYear(System.currentTimeMillis()).toInt())
    }

    override fun hasStableIds(): Boolean {
        return true;
    }

    override fun getViewAt(position: Int): RemoteViews {
        val remoteViews = RemoteViews(context.packageName, R.layout.widget_reminder_list_item)
        remoteViews.setTextViewText(R.id.reminder_name,
                "${TimeDateUtil.formatTimeInMilliseconds(context,
                        mPrayerReminders[position].timeInMilliSeconds)}: ${mPrayerReminders[position].reminderName}")
        remoteViews.setTextColor(R.id.reminder_name, intent?.getIntExtra(TEXT_COLOR, Color.BLACK) ?: Color.BLACK)
        val fillInIntent = Intent()
        remoteViews.setOnClickFillInIntent(R.id.widget_item_container, fillInIntent)
        return remoteViews
    }

    override fun getCount(): Int {
        return mPrayerReminders.size
    }

    override fun getViewTypeCount(): Int {
        return 1
    }

    override fun onDestroy() {

    }

}