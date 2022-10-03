package com.thesunnahrevival.sunnahassistant.widgets

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.thesunnahrevival.sunnahassistant.R
import com.thesunnahrevival.sunnahassistant.data.SunnahAssistantRepository
import com.thesunnahrevival.sunnahassistant.data.model.ToDo
import com.thesunnahrevival.sunnahassistant.utilities.*
import java.lang.Integer.parseInt


class TodaysToDosRemoteViewsFactory(
    private val context: Context,
    private val intent: Intent?
) : RemoteViewsService.RemoteViewsFactory {

    private lateinit var mTodayToDos: List<ToDo>

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

        mTodayToDos = repository.getToDosOnDayValue(
            dayOfTheWeek.toString(),
            getDayDate(System.currentTimeMillis()),
            getMonthNumber(System.currentTimeMillis()),
            parseInt(getYear(System.currentTimeMillis()))
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
                    mTodayToDos[position].timeInMilliseconds
                )
            }: ${mTodayToDos[position].name}"
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
        return mTodayToDos.size
    }

    override fun getViewTypeCount(): Int {
        return 1
    }

    override fun onDestroy() {

    }

}