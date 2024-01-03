package com.thesunnahrevival.sunnahassistant.views.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import com.kizitonwose.calendarview.model.CalendarDay
import com.thesunnahrevival.sunnahassistant.R
import com.thesunnahrevival.sunnahassistant.utilities.generateDateText
import com.thesunnahrevival.sunnahassistant.views.adapters.DayViewContainer
import com.thesunnahrevival.sunnahassistant.views.customviews.CalendarView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.YearMonth
import java.util.Calendar
import java.util.GregorianCalendar
import java.util.Locale


class CalendarFragment : TodayFragment(), CalendarView.Listeners {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        view.findViewById<CalendarView>(R.id.calendar_view).visibility = VISIBLE
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val selectedToDoDate = mainActivityViewModel.selectedToDoDate
        mBinding.calendarView.setupWithListeners(
            listeners = this,
            startMonth = YearMonth.of(selectedToDoDate.year, selectedToDoDate.monthValue),
            endMonth = YearMonth.of(selectedToDoDate.year, selectedToDoDate.monthValue),
            showHijriDate = mainActivityViewModel.settingsValue?.includeHijriDateInCalendar ?: true
        )
        mBinding.calendarView.scrollToSpecificDate(selectedToDoDate)

        mainActivityViewModel.triggerCalendarUpdate.observe(viewLifecycleOwner) {
            mBinding.calendarView.notifyCalendarChanged()
        }
    }


    override fun getEvents(container: DayViewContainer, day: CalendarDay) {
        CoroutineScope(Dispatchers.IO).launch {
            val calendar = Calendar.getInstance(Locale.US)
            calendar.set(day.date.year, day.date.month.ordinal, day.day)

            val thereToDosOnDay = mainActivityViewModel.thereToDosOnDay(
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
        mainActivityViewModel.setToDoParameters(date = day.date.toEpochDay() * 86400000)

        val gregorianCalendar = GregorianCalendar(
            day.date.year,
            day.date.month.ordinal,
            day.date.dayOfMonth
        )

        mBinding.selectedDate.visibility = VISIBLE
        mBinding.selectedDate.text = generateDateText(
            gregorianCalendar,
            mainActivityViewModel.settingsValue?.isDisplayHijriDate ?: true
        )
    }
}