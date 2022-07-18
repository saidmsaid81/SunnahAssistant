package com.thesunnahrevival.sunnahassistant.views.home

import android.os.Bundle
import android.view.View
import com.github.msarhan.ummalqura.calendar.UmmalquraCalendar
import com.kizitonwose.calendarview.model.CalendarDay
import com.kizitonwose.calendarview.model.CalendarMonth
import com.kizitonwose.calendarview.model.DayOwner
import com.kizitonwose.calendarview.ui.DayBinder
import com.kizitonwose.calendarview.ui.MonthHeaderFooterBinder
import com.kizitonwose.calendarview.utils.next
import com.kizitonwose.calendarview.utils.previous
import com.thesunnahrevival.sunnahassistant.R
import com.thesunnahrevival.sunnahassistant.utilities.supportedLocales
import com.thesunnahrevival.sunnahassistant.views.adapters.DayViewContainer
import com.thesunnahrevival.sunnahassistant.views.adapters.MonthHeaderViewContainer
import kotlinx.android.synthetic.main.calendar_view.*
import kotlinx.android.synthetic.main.today_fragment.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.WeekFields
import java.util.*


class CalendarFragment : TodayFragment() {

    private var selectedDate: LocalDate? = LocalDate.now()
    private val todayMonth: String

    init {
        val now = LocalDate.now()
        todayMonth = "${now.month.value}/${now.year}"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view_stub.inflate()

        bindHeaderToCalendar()
        bindDaysViewToCalendar()

        val firstDayOfWeek = WeekFields.of(Locale.US).firstDayOfWeek
        calendar_view.setup(
            YearMonth.of(1970, 1), YearMonth.of(2069, 12), firstDayOfWeek
        )
        calendar_view.scrollToDate(LocalDate.now())


        mViewModel.triggerCalendarUpdate.observe(viewLifecycleOwner) {
            val yearMonth = calendar_view.findFirstVisibleMonth()?.yearMonth
            if (yearMonth != null) {
                calendar_view.notifyMonthChanged(yearMonth)
            }
        }
    }

    private fun bindHeaderToCalendar() {
        calendar_view.monthHeaderBinder =
            object : MonthHeaderFooterBinder<MonthHeaderViewContainer> {

                override fun create(view: View) = MonthHeaderViewContainer(view)

                override fun bind(container: MonthHeaderViewContainer, month: CalendarMonth) {
                    val gregorianMonthName = resources.getStringArray(
                        R.array.gregorian_month_names
                    )[month.yearMonth.month.ordinal]
                    val gregorianYear = String.format(getLocale(), "%d", month.year)

                    val hijriMonthName = getHijriMonthName(month)

                    container.gregorianMonthName.text =
                        "$gregorianMonthName $gregorianYear"
                    container.hijriMonthName.text = hijriMonthName

                    container.prevMonth.setOnClickListener {
                        calendar_view.findFirstVisibleMonth()?.let {
                            calendar_view.smoothScrollToMonth(it.yearMonth.previous)
                        }
                    }

                    container.nextMonth.setOnClickListener {
                        calendar_view.findFirstVisibleMonth()?.let {
                            calendar_view.smoothScrollToMonth(it.yearMonth.next)
                        }
                    }

                    if ("${month.month}/${month.year}".matches(todayMonth.toRegex()))
                        container.goToToday.visibility = View.GONE
                    else {
                        container.goToToday.visibility = View.VISIBLE
                        container.goToToday.setOnClickListener {
                            selectedDate = LocalDate.now()
                            calendar_view.scrollToDate(LocalDate.now())
                        }
                    }
                }
            }
    }

    private fun getHijriMonthName(month: CalendarMonth): String {
        val gregorianCalendar = GregorianCalendar(
            month.year, month.month - 1, 1
        )
        val hijriCalendar = UmmalquraCalendar().apply {
            time = gregorianCalendar.time
        }

        val hijriMonthName = StringBuilder()
        val hijriYear = hijriCalendar.get(Calendar.YEAR)

        hijriMonthName.append(
            hijriCalendar.getDisplayName(Calendar.MONTH, Calendar.LONG, getLocale())
        )

        //Set date to last day of month to see if its a different hijri month
        gregorianCalendar.set(Calendar.DAY_OF_MONTH, month.yearMonth.lengthOfMonth())
        hijriCalendar.time = gregorianCalendar.time

        val nextHijriMonthName =
            hijriCalendar.getDisplayName(Calendar.MONTH, Calendar.LONG, getLocale())

        if (nextHijriMonthName != null) {
            if (!nextHijriMonthName.matches(hijriMonthName.toString().toRegex())) {
                if (hijriYear != hijriCalendar.get(Calendar.YEAR))
                    hijriMonthName.append(" ${String.format(getLocale(), "%d", hijriYear)} ")
                hijriMonthName.append("- $nextHijriMonthName ")
            }
        }

        hijriMonthName.append(
            "(${
                String.format(
                    getLocale(),
                    "%d",
                    hijriCalendar.get(Calendar.YEAR)
                )
            })"
        )

        return hijriMonthName.toString()
    }

    private fun getLocale(): Locale {
        return if (supportedLocales.contains(Locale.getDefault().language))
            Locale.getDefault()
        else
            Locale.ENGLISH
    }

    private fun bindDaysViewToCalendar() {
        calendar_view.dayBinder = object : DayBinder<DayViewContainer> {
            // Called only when a new container is needed.
            override fun create(view: View) = DayViewContainer(view)

            // Called every time we need to reuse a container.
            override fun bind(container: DayViewContainer, day: CalendarDay) {
                val hijriDay = getHijriDay(day)

                if (day.owner == DayOwner.THIS_MONTH) {
                    container.gregorianCalendarDay.text =
                        "${String.format(getLocale(), "%d", day.date.dayOfMonth)}"
                    container.hijriCalendarDay.text =
                        "${String.format(getLocale(), "(%d)", hijriDay)}"

                    container.view.setOnClickListener {
                        onDateSelected(day)
                    }

                    setCalendarDayBackground(container, day)
                    container.gregorianCalendarDay.visibility = View.VISIBLE
                    container.hijriCalendarDay.visibility = View.VISIBLE

                    if (day.date == selectedDate)
                        mViewModel.setDateOfReminders(day.date.toEpochDay() * 86400000)

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
                                container.eventDot.visibility = View.VISIBLE
                            else
                                container.eventDot.visibility = View.INVISIBLE
                        }
                    }
                } else {
                    container.gregorianCalendarDay.visibility = View.GONE
                    container.hijriCalendarDay.visibility = View.GONE
                    container.eventDot.visibility = View.GONE
                }
            }
        }
    }

    private fun setCalendarDayBackground(container: DayViewContainer, day: CalendarDay) {
        if (day.date == LocalDate.now()) {
            container.view.setBackgroundResource(R.drawable.today_date_bg)
        }
        if (day.date == selectedDate) {
            container.view.setBackgroundResource(R.drawable.selected_date_bg)
            selected_date.text = generateSelectedDateText(day)
        } else {
            if (day.date != LocalDate.now())
                container.view.setBackgroundResource(R.color.calendar_day_bg_color)
        }
    }

    private fun onDateSelected(day: CalendarDay) {
        if (day.owner == DayOwner.THIS_MONTH) {
            val currentSelection = selectedDate
            if (currentSelection != day.date) {
                selectedDate = day.date
                calendar_view.notifyDateChanged(day.date)
                if (currentSelection != null) {
                    calendar_view.notifyDateChanged(currentSelection)
                }
            }
        }

    }

    private fun generateSelectedDateText(day: CalendarDay): String {
        val gregorianCalendar = GregorianCalendar(
            day.date.year,
            day.date.month.ordinal,
            day.date.dayOfMonth
        )
        val simpleDateFormat = SimpleDateFormat("EEEE dd MMMM, yyyy", getLocale())
        val dateFormatted = simpleDateFormat.format(gregorianCalendar.time)

        val ummalquraCalendar = UmmalquraCalendar()
        ummalquraCalendar.time = gregorianCalendar.time
        val hijriDateFormat = SimpleDateFormat("", getLocale())
        hijriDateFormat.calendar = ummalquraCalendar
        hijriDateFormat.applyPattern("dd MMMM, yyyy")

        return "$dateFormatted / ${hijriDateFormat.format(ummalquraCalendar.time)}"
    }

    private fun getHijriDay(day: CalendarDay): Int {
        val gregorianCalendar = GregorianCalendar(
            day.date.year,
            day.date.month.ordinal,
            day.date.dayOfMonth
        )
        val hijriDay = UmmalquraCalendar().apply {
            time = gregorianCalendar.time
        }.get(Calendar.DAY_OF_MONTH)
        return hijriDay
    }


}