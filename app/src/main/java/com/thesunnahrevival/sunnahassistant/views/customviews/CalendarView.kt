package com.thesunnahrevival.sunnahassistant.views.customviews

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.github.msarhan.ummalqura.calendar.UmmalquraCalendar
import com.kizitonwose.calendarview.CalendarView
import com.kizitonwose.calendarview.model.CalendarDay
import com.kizitonwose.calendarview.model.CalendarMonth
import com.kizitonwose.calendarview.model.DayOwner
import com.kizitonwose.calendarview.ui.DayBinder
import com.kizitonwose.calendarview.ui.MonthHeaderFooterBinder
import com.kizitonwose.calendarview.utils.next
import com.kizitonwose.calendarview.utils.previous
import com.thesunnahrevival.sunnahassistant.R
import com.thesunnahrevival.sunnahassistant.utilities.getHijriMonthName
import com.thesunnahrevival.sunnahassistant.utilities.getLocale
import com.thesunnahrevival.sunnahassistant.views.adapters.DayViewContainer
import com.thesunnahrevival.sunnahassistant.views.adapters.MonthHeaderViewContainer
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.util.*

class CalendarView : CalendarView {

    private var selectedDate: LocalDate? = LocalDate.now()
    private val todayMonth: String
    private var listeners: Listeners? = null

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(
        context, attrs
    )

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context, attrs, defStyleAttr
    )

    init {
        val now = LocalDate.now()
        todayMonth = "${now.month.value}/${now.year}"
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        bindHeaderToCalendar()
        bindDaysViewToCalendar()
    }

    fun setupWithListeners(
        startMonth: YearMonth,
        endMonth: YearMonth,
        firstDayOfWeek: DayOfWeek,
        listeners: Listeners
    ) {
        setup(startMonth, endMonth, firstDayOfWeek)
        this.listeners = listeners
    }

    private fun bindHeaderToCalendar() {
        monthHeaderBinder =
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
                        findFirstVisibleMonth()?.let {
                            smoothScrollToMonth(it.yearMonth.previous)
                        }
                    }

                    container.nextMonth.setOnClickListener {
                        findFirstVisibleMonth()?.let {
                            smoothScrollToMonth(it.yearMonth.next)
                        }
                    }

                    if ("${month.month}/${month.year}".matches(todayMonth.toRegex()))
                        container.goToToday.visibility = View.GONE
                    else {
                        container.goToToday.visibility = View.VISIBLE
                        container.goToToday.setOnClickListener {
                            selectedDate = LocalDate.now()
                            scrollToDate(LocalDate.now())
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
            hijriCalendar.getHijriMonthName()
        )

        //Set date to last day of month to see if its a different hijri month
        gregorianCalendar.set(Calendar.DAY_OF_MONTH, month.yearMonth.lengthOfMonth())
        hijriCalendar.time = gregorianCalendar.time

        val nextHijriMonthName =
            hijriCalendar.getHijriMonthName()

        if (!nextHijriMonthName.matches(hijriMonthName.toString().toRegex())) {
            if (hijriYear != hijriCalendar.get(Calendar.YEAR))
                hijriMonthName.append(" ${String.format(getLocale(), "%d", hijriYear)} ")
            hijriMonthName.append("- $nextHijriMonthName ")
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

    private fun bindDaysViewToCalendar() {
        dayBinder = object : DayBinder<DayViewContainer> {
            // Called only when a new container is needed.
            override fun create(view: View) = DayViewContainer(view)

            // Called every time we need to reuse a container.
            override fun bind(container: DayViewContainer, day: CalendarDay) {
                val hijriDay = getHijriDay(day)

                if (day.owner == DayOwner.THIS_MONTH) {
                    container.gregorianCalendarDay.text =
                        String.format(getLocale(), "%d", day.date.dayOfMonth)
                    container.hijriCalendarDay.text =
                        String.format(getLocale(), "(%d)", hijriDay)

                    container.view.setOnClickListener {
                        onDateSelected(day)
                    }

                    setCalendarDayBackground(container, day)
                    container.gregorianCalendarDay.visibility = View.VISIBLE
                    container.hijriCalendarDay.visibility = View.VISIBLE

                    if (day.date == selectedDate)
                        listeners?.setDateOfReminders(day.date.toEpochDay() * 86400000)
                    listeners?.getEvents(container, day)

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

            val gregorianCalendar = GregorianCalendar(
                day.date.year,
                day.date.month.ordinal,
                day.date.dayOfMonth
            )
            listeners?.displaySelectedDateAsText(gregorianCalendar)
        } else {
            if (day.date != LocalDate.now())
                container.view.setBackgroundResource(R.color.calendar_day_bg_color)
        }
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

    private fun onDateSelected(day: CalendarDay) {
        if (day.owner == DayOwner.THIS_MONTH) {
            val currentSelection = selectedDate
            if (currentSelection != day.date) {
                selectedDate = day.date
                notifyDateChanged(day.date)
                if (currentSelection != null) {
                    notifyDateChanged(currentSelection)
                }
            }
        }
    }

    interface Listeners {
        fun displaySelectedDateAsText(gregorianCalendar: GregorianCalendar)
        fun getEvents(container: DayViewContainer, day: CalendarDay)
        fun setDateOfReminders(timeInMilliseconds: Long)
    }
}
