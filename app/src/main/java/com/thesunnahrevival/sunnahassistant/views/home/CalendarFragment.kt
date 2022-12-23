package com.thesunnahrevival.sunnahassistant.views.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import com.google.android.material.appbar.AppBarLayout
import com.kizitonwose.calendarview.model.CalendarDay
import com.thesunnahrevival.sunnahassistant.R
import com.thesunnahrevival.sunnahassistant.utilities.generateDateText
import com.thesunnahrevival.sunnahassistant.views.adapters.DayViewContainer
import com.thesunnahrevival.sunnahassistant.views.customviews.CalendarView
import kotlinx.android.synthetic.main.fragment_today.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.YearMonth
import java.util.*


class CalendarFragment : TodayFragment(), CalendarView.Listeners {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        view.findViewById<AppBarLayout>(R.id.app_bar).visibility = VISIBLE
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val selectedToDoDate = mViewModel.selectedToDoDate
        calendar_view.setupWithListeners(
            listeners = this,
            startMonth = YearMonth.of(selectedToDoDate.year, selectedToDoDate.monthValue),
            endMonth = YearMonth.of(selectedToDoDate.year, selectedToDoDate.monthValue),
            showHijriDate = mViewModel.settingsValue?.includeHijriDateInCalendar ?: true
        )
        calendar_view.scrollToSpecificDate(selectedToDoDate)

        mViewModel.triggerCalendarUpdate.observe(viewLifecycleOwner) {
            calendar_view.notifyCalendarChanged()
        }
    }


    override fun getEvents(container: DayViewContainer, day: CalendarDay) {
        CoroutineScope(Dispatchers.IO).launch {
            val calendar = Calendar.getInstance(Locale.US)
            calendar.set(day.date.year, day.date.month.ordinal, day.day)

            val thereToDosOnDay = mViewModel.thereToDosOnDay(
                calendar.get(Calendar.DAY_OF_WEEK).toString(),
                day.day,
                day.date.month.ordinal,
                day.date.year
            )

            withContext(Dispatchers.Main) {
                if (thereToDosOnDay)
                    container.eventDot.visibility = VISIBLE
                else
                    container.eventDot.visibility = INVISIBLE
            }
        }
    }

    override fun onDateSelected(day: CalendarDay) {
        mViewModel.setToDoParameters(date = day.date.toEpochDay() * 86400000)

        val gregorianCalendar = GregorianCalendar(
            day.date.year,
            day.date.month.ordinal,
            day.date.dayOfMonth
        )

        selected_date.visibility = VISIBLE
        selected_date.text = generateDateText(
            gregorianCalendar,
            mViewModel.settingsValue?.isDisplayHijriDate ?: true
        )
    }
}