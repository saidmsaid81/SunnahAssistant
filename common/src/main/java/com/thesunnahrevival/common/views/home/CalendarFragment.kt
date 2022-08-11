package com.thesunnahrevival.common.views.home

import android.os.Bundle
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import com.kizitonwose.calendarview.model.CalendarDay
import com.thesunnahrevival.common.utilities.generateDateText
import com.thesunnahrevival.common.views.adapters.DayViewContainer
import com.thesunnahrevival.common.views.customviews.CalendarView
import kotlinx.android.synthetic.main.calendar_view.*
import kotlinx.android.synthetic.main.today_fragment.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.util.*


class CalendarFragment : TodayFragment(), CalendarView.Listeners {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view_stub.inflate()

        calendar_view.setupWithListeners(listeners = this)
        calendar_view.scrollToSpecificDate(LocalDate.now())

        mViewModel.triggerCalendarUpdate.observe(viewLifecycleOwner) {
            val yearMonth = calendar_view.findFirstVisibleMonth()?.yearMonth
            if (yearMonth != null) {
                calendar_view.notifyMonthChanged(yearMonth)
            }
        }
    }


    override fun getEvents(container: DayViewContainer, day: CalendarDay) {
        CoroutineScope(Dispatchers.IO).launch {
            val calendar = Calendar.getInstance(Locale.US)
            calendar.set(day.date.year, day.date.month.ordinal, day.day)

            val thereRemindersOnDay = mViewModel.thereRemindersOnDay(
                calendar.get(Calendar.DAY_OF_WEEK).toString(),
                day.day,
                day.date.month.ordinal,
                day.date.year
            )

            withContext(Dispatchers.Main) {
                if (thereRemindersOnDay)
                    container.eventDot.visibility = VISIBLE
                else
                    container.eventDot.visibility = INVISIBLE
            }
        }
    }

    override fun onDateSelected(day: CalendarDay) {
        mViewModel.setDateOfReminders(day.date.toEpochDay() * 86400000)

        val gregorianCalendar = GregorianCalendar(
            day.date.year,
            day.date.month.ordinal,
            day.date.dayOfMonth
        )
        selected_date.text = generateDateText(gregorianCalendar)
    }
}