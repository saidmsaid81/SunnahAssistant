package com.thesunnahrevival.sunnahassistant.widgets

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.thesunnahrevival.sunnahassistant.R
import com.thesunnahrevival.sunnahassistant.data.SunnahAssistantRepository
import com.thesunnahrevival.sunnahassistant.data.model.ToDo
import com.thesunnahrevival.sunnahassistant.utilities.formatTimeInMilliseconds
import com.thesunnahrevival.sunnahassistant.utilities.getDayDate
import com.thesunnahrevival.sunnahassistant.utilities.getMonthNumber
import com.thesunnahrevival.sunnahassistant.utilities.getYear

class PrayerTimesRemoteViewsFactory(
    private val context: Context,
    private val intent: Intent?
) : RemoteViewsService.RemoteViewsFactory {

    private lateinit var mPrayerToDos: List<ToDo>

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

        mPrayerToDos = repository.getPrayerTimesValue(
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
        val remoteViews = RemoteViews(context.packageName, R.layout.widget_to_do_list_item)
        remoteViews.setTextViewText(
            R.id.to_do_name,
            "${
                formatTimeInMilliseconds(
                    context,
                    mPrayerToDos[position].timeInMilliseconds
                )
            }: ${mPrayerToDos[position].name}"
        )
        remoteViews.setTextColor(
            R.id.to_do_name,
            intent?.getIntExtra(TEXT_COLOR, Color.BLACK) ?: Color.BLACK
        )
        val fillInIntent = Intent()
        remoteViews.setOnClickFillInIntent(R.id.widget_item_container, fillInIntent)
        return remoteViews
    }

    override fun getCount(): Int {
        return mPrayerToDos.size
    }

    override fun getViewTypeCount(): Int {
        return 1
    }

    override fun onDestroy() {

    }

}