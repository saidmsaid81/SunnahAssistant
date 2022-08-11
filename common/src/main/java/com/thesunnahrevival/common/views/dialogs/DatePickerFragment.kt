package com.thesunnahrevival.common.views.dialogs


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.MutableLiveData
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.kizitonwose.calendarview.model.CalendarDay
import com.thesunnahrevival.common.R
import com.thesunnahrevival.common.views.adapters.DayViewContainer
import com.thesunnahrevival.common.views.customviews.CalendarView
import kotlinx.android.synthetic.main.date_picker_fragment.*
import java.time.LocalDate
import java.time.YearMonth

class DatePickerFragment : BottomSheetDialogFragment(), CalendarView.Listeners {

    private lateinit var mCalendarDay: CalendarDay

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.date_picker_fragment, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if (mDay > 0 && mMonth < 12 && mYear > 0) { //no repeat reminder
            val startMonth = YearMonth.of(mYear, (mMonth + 1))
            calendar_view.setupWithListeners(
                startMonth,
                startMonth,
                listeners = this
            )
            calendar_view.scrollToSpecificDate(LocalDate.of(mYear, (mMonth + 1), mDay))
        } else if (mDay > 0) { // monthly reminder
            calendar_view.setupMonthWithListeners(this)
            calendar_view.scrollToSpecificDate(LocalDate.of(2017, 1, mDay))
        } else { //New reminder
            calendar_view.setupWithListeners(
                listeners = this
            )
            calendar_view.scrollToSpecificDate(LocalDate.now())
        }
        ok.setOnClickListener {
            mDay = mCalendarDay.day
            mMonth = mCalendarDay.date.month.ordinal
            mYear = mCalendarDay.date.year
            dateSet.value = "$mDay/${(mMonth + 1)}/$mYear"

            val timePickerFragment: DialogFragment = TimePickerFragment()
            val fm = requireActivity().supportFragmentManager
            timePickerFragment.show(fm, "timePicker")
            dismiss()
        }
        cancel.setOnClickListener {
            dismiss()
        }
    }

    override fun onDateSelected(day: CalendarDay) {
        mCalendarDay = day
    }

    override fun getEvents(container: DayViewContainer, day: CalendarDay) {

    }

    companion object {
        var mDay = 0
        var mMonth = 12
        var mYear = 0
        var dateSet = MutableLiveData<String>(null)
    }
}