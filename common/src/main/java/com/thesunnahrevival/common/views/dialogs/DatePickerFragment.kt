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
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.YearMonth
import java.util.*

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
        if (showAllMonths) { //No repeat reminder
            if (mDay in 1..31 && mMonth in 1..12 && mYear > 0) { //No repeat existing reminder
                val startMonth = YearMonth.of(mYear, mMonth)
                calendar_view.setupWithListeners(
                    startMonth,
                    startMonth,
                    listeners = this
                )
                calendar_view.scrollToSpecificDate(LocalDate.of(mYear, mMonth, mDay))
            } else { //New reminder
                calendar_view.setupWithListeners(
                    listeners = this
                )
                calendar_view.scrollToSpecificDate(LocalDate.now())
            }
        } else { //Monthly reminder
            calendar_view.setupMonthWithListeners(this)
            if (mDay in 1..31)
                calendar_view.scrollToSpecificDate(LocalDate.of(2017, 1, mDay))
            else
                calendar_view.scrollToSpecificDate(
                    LocalDate.of(2017, 1, LocalDate.now().dayOfMonth)
                )

        }

        ok.setOnClickListener {
            mDay = mCalendarDay.day
            mMonth = if (showAllMonths) mCalendarDay.date.monthValue else 13
            mYear = if (showAllMonths) mCalendarDay.date.year else 0
            dateSet.value = "$mDay/${(mMonth)}/$mYear"
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
        var mMonth = 13
        var mYear = 0
        var dateSet = MutableLiveData<String>(null)
        var showAllMonths = true
    }
}