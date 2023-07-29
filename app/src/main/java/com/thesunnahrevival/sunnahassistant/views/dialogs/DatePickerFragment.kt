package com.thesunnahrevival.sunnahassistant.views.dialogs


import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.kizitonwose.calendarview.model.CalendarDay
import com.thesunnahrevival.sunnahassistant.databinding.FragmentDatePickerBinding
import com.thesunnahrevival.sunnahassistant.views.adapters.DayViewContainer
import com.thesunnahrevival.sunnahassistant.views.customviews.CalendarView
import java.time.LocalDate
import java.time.YearMonth

class DatePickerFragment : BottomSheetDialogFragment(), CalendarView.Listeners {

    private lateinit var mCalendarDay: CalendarDay
    private var mListener: OnDateSelectedListener? = null
    private var _datePickerFragmentBinding: FragmentDatePickerBinding? = null
    private val datePickerFragmentBinding get() = _datePickerFragmentBinding!!

    override fun onStart() {
        super.onStart()
        //this forces the sheet to appear at max height even on landscape
        val bottomSheetBehavior = BottomSheetBehavior.from(requireView().parent as View)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _datePickerFragmentBinding = FragmentDatePickerBinding.inflate(inflater)
        return datePickerFragmentBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if (mListener == null) {
            Log.e("Date Picker Error", "Please implement OnDateSelectedListener ")
            dismiss()
        }
        val now = LocalDate.now()
        val dayArgument = arguments?.getInt(DAY) ?: now.dayOfMonth

        //Plus One for month argument because previous date pickers were based on the default Android Picker
        val monthArgument = arguments?.getInt(MONTH)?.plus(1) ?: now.monthValue

        val yearArgument = arguments?.getInt(YEAR) ?: now.year
        val showAllMonths = arguments?.getBoolean(SHOWALLMONTHS) ?: true
        val showHijriDate = arguments?.getBoolean(SHOWHIJRIDATE) ?: true

        val month = if (monthArgument in 1..12)
            monthArgument
        else {
            Log.e("Date Picker Error", "Please provide a valid month (From 0..11)")
            now.monthValue
        }

        val year = if (yearArgument > 0)
            yearArgument
        else {
            Log.e("Date Picker Error", "Please provide a valid year (Should be more than 0)")
            now.year
        }

        val lengthOfMonth = LocalDate.of(year, month, 1).lengthOfMonth()
        val day = if ((showAllMonths && dayArgument in 1..lengthOfMonth) ||
            (!showAllMonths && dayArgument in 1..31)
        )
            dayArgument
        else {
            Log.e("Date Picker Error", "Please provide a valid day of month between 1 and 28/31")
            1
        }

        if (showAllMonths) {
            val startMonth = YearMonth.of(year, month)
            datePickerFragmentBinding.calendarView.setupWithListeners(
                startMonth,
                startMonth,
                listeners = this,
                showHijriDate = showHijriDate
            )
            datePickerFragmentBinding.calendarView.scrollToSpecificDate(
                LocalDate.of(
                    year,
                    month,
                    day
                )
            )
        } else {
            datePickerFragmentBinding.calendarView.setupMonthWithListeners(this)
            datePickerFragmentBinding.calendarView.scrollToSpecificDate(LocalDate.of(2017, 1, day))
        }

        datePickerFragmentBinding.ok.setOnClickListener {
            mListener?.onDateSelected(
                mCalendarDay.day,
                if (showAllMonths) mCalendarDay.date.month.ordinal else 12,
                if (showAllMonths) mCalendarDay.date.year else 0
            )
            dismiss()
        }
        datePickerFragmentBinding.cancel.setOnClickListener {
            dismiss()
        }
    }

    override fun onDateSelected(day: CalendarDay) {
        mCalendarDay = day
    }

    override fun getEvents(container: DayViewContainer, day: CalendarDay) {

    }

    fun setListener(listener: OnDateSelectedListener) {
        mListener = listener
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _datePickerFragmentBinding = null
    }

    interface OnDateSelectedListener {
        fun onDateSelected(day: Int, month: Int, year: Int)
    }

    companion object {
        const val DAY = "DAY"
        const val MONTH = "MONTH"
        const val YEAR = "YEAR"
        const val SHOWALLMONTHS = "SHOWALLMONTHS"
        const val SHOWHIJRIDATE = "SHOWHIJRIDATE"
    }
}