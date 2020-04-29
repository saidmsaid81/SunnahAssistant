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
import java.lang.Integer.parseInt


class TodaysRemindersRemoteViewsFactory(
        private val context: Context,
        private val intent: Intent?
) : RemoteViewsService.RemoteViewsFactory  {

    private lateinit var mTodayReminders: MutableList<Reminder>

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
        mTodayReminders = reminderDao.getRemindersOnDayValue(
                TimeDateUtil.getNameOfTheDay(System.currentTimeMillis()),
                TimeDateUtil.getDayDate(System.currentTimeMillis()),
                TimeDateUtil.getMonthNumber(System.currentTimeMillis()) - 1,
                parseInt(TimeDateUtil.getYear(System.currentTimeMillis())))

    }

    override fun hasStableIds(): Boolean {
        return true;
    }

    override fun getViewAt(position: Int): RemoteViews {
        val remoteViews = RemoteViews(context.packageName, R.layout.widget_reminder_list_item)
        remoteViews.setTextViewText(R.id.reminder_name,
                "${TimeDateUtil.formatTimeInMilliseconds(context, 
                        mTodayReminders[position].timeInMilliSeconds)}: ${mTodayReminders[position].reminderName}")
        remoteViews.setTextColor(R.id.reminder_name, intent?.getIntExtra(TEXT_COLOR, Color.BLACK) ?: Color.BLACK)
        val fillInIntent = Intent()
        remoteViews.setOnClickFillInIntent(R.id.widget_item_container, fillInIntent)
        return remoteViews
    }

    override fun getCount(): Int {
        return mTodayReminders.size
    }

    override fun getViewTypeCount(): Int {
        return 1
    }

    override fun onDestroy() {

    }

}