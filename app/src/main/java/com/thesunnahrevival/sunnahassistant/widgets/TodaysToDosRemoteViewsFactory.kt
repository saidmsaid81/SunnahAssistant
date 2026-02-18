package com.thesunnahrevival.sunnahassistant.widgets

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.view.View
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.thesunnahrevival.sunnahassistant.R
import com.thesunnahrevival.sunnahassistant.data.model.entity.ToDo
import com.thesunnahrevival.sunnahassistant.data.repositories.SunnahAssistantRepository
import com.thesunnahrevival.sunnahassistant.utilities.*
import java.lang.Integer.parseInt
import java.time.LocalDate


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
        ).sortedBy { it.isComplete(LocalDate.now()) }

    }

    override fun hasStableIds(): Boolean {
        return true
    }

    override fun getViewAt(position: Int): RemoteViews {
        val remoteViews = RemoteViews(context.packageName, R.layout.widget_to_do_list_item)

        if (mTodayToDos.isNotEmpty()) {
            remoteViews.setViewVisibility(R.id.no_data, View.GONE)
            remoteViews.setViewVisibility(R.id.to_do_name, View.VISIBLE)

            val formatTimeInMilliseconds = formatTimeInMilliseconds(
                context,
                mTodayToDos[position].timeInMilliseconds
            )
            val time = if (formatTimeInMilliseconds != context.getString(R.string.time_not_set))
                "$formatTimeInMilliseconds:"
            else "-"

            remoteViews.setTextViewText(
                R.id.to_do_name,
                "$time ${mTodayToDos[position].name}"
            )
            remoteViews.setTextColor(
                R.id.to_do_name,
                intent?.getIntExtra(TEXT_COLOR, Color.BLACK) ?: Color.BLACK
            )
            if (mTodayToDos[position].isComplete(LocalDate.now()))
                remoteViews.setInt(
                    R.id.to_do_name, "setPaintFlags",
                    Paint.STRIKE_THRU_TEXT_FLAG or Paint.ANTI_ALIAS_FLAG
                )
            else
                remoteViews.setInt(
                    R.id.to_do_name, "setPaintFlags",
                    Paint.ANTI_ALIAS_FLAG
                )
        } else {
            remoteViews.setViewVisibility(R.id.to_do_name, View.GONE)
            remoteViews.setViewVisibility(R.id.no_data, View.VISIBLE)

            remoteViews.setTextColor(
                R.id.no_data,
                intent?.getIntExtra(TEXT_COLOR, Color.BLACK) ?: Color.BLACK
            )
        }

        val fillInIntent = Intent()
        remoteViews.setOnClickFillInIntent(R.id.widget_item_container, fillInIntent)
        return remoteViews
    }

    override fun getCount(): Int {
        return if (mTodayToDos.isNotEmpty()) mTodayToDos.size else 1
    }

    override fun getViewTypeCount(): Int {
        return 1
    }

    override fun onDestroy() {

    }

}