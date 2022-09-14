package com.thesunnahrevival.common.widgets

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.thesunnahrevival.common.R
import com.thesunnahrevival.common.data.SunnahAssistantRepository
import com.thesunnahrevival.common.data.model.Reminder
import com.thesunnahrevival.common.utilities.formatTimeInMilliseconds
import com.thesunnahrevival.common.utilities.getDayDate
import com.thesunnahrevival.common.utilities.getMonthNumber
import com.thesunnahrevival.common.utilities.getYear

class PrayerTimesRemoteViewsFactory(
    private val context: Context,
    private val intent: Intent?
) : RemoteViewsService.RemoteViewsFactory {

    private lateinit var mPrayerReminders: List<Reminder>

    override fun onCreate() {

    }

    override fun getLoadingView(): RemoteViews? {
        return null
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun onDataSetChanged() {
        val repository = SunnahAssistantRepository.getInstance(context.applicationContext)
        mPrayerReminders = repository.getPrayerTimesValue(
            getDayDate(System.currentTimeMillis()),
            getMonthNumber(System.currentTimeMillis()),
            getYear(System.currentTimeMillis()).toInt(),
            context.resources.getStringArray(R.array.categories)[2]
        )
    }

    override fun hasStableIds(): Boolean {
        return true
    }

    override fun getViewAt(position: Int): RemoteViews {
        val remoteViews = RemoteViews(context.packageName, R.layout.widget_reminder_list_item)
        remoteViews.setTextViewText(
            R.id.reminder_name,
            "${
                formatTimeInMilliseconds(
                    context,
                    mPrayerReminders[position].timeInMilliseconds
                )
            }: ${mPrayerReminders[position].reminderName}"
        )
        remoteViews.setTextColor(
            R.id.reminder_name,
            intent?.getIntExtra(TEXT_COLOR, Color.BLACK) ?: Color.BLACK
        )
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