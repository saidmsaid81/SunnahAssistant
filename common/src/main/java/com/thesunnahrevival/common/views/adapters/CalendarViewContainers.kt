package com.thesunnahrevival.common.views.adapters

import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.kizitonwose.calendarview.ui.ViewContainer
import com.thesunnahrevival.common.R

class DayViewContainer(view: View) : ViewContainer(view) {
    val gregorianCalendarDay = view.findViewById<TextView>(R.id.gregorian_calendar_day)
    val hijriCalendarDay = view.findViewById<TextView>(R.id.hijri_calendar_day)
    val eventDot = view.findViewById<View>(R.id.event_dot)
}

class MonthHeaderViewContainer(view: View) : ViewContainer(view) {
    val gregorianMonthName = view.findViewById<TextView>(R.id.gregorian_month_name)
    val hijriMonthName = view.findViewById<TextView>(R.id.hijri_month_name)
    val goToToday = view.findViewById<TextView>(R.id.go_to_today)
    val prevMonth = view.findViewById<ImageView>(R.id.prev_month)
    val nextMonth = view.findViewById<ImageView>(R.id.next_month)
    val monthHeaderView = view.findViewById<LinearLayout>(R.id.month_view)
    val oneMonthHeader = view.findViewById<LinearLayout>(R.id.one_month_header)
}
