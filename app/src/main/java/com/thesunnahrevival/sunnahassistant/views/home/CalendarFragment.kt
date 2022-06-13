package com.thesunnahrevival.sunnahassistant.views.home

import android.os.Bundle
import android.view.View
import com.github.msarhan.ummalqura.calendar.UmmalquraCalendar
import com.thesunnahrevival.sunnahassistant.data.model.Reminder
import com.thesunnahrevival.sunnahassistant.utilities.getLocale
import kotlinx.android.synthetic.main.calendar_view.*
import kotlinx.android.synthetic.main.today_fragment.*
import java.text.SimpleDateFormat
import java.util.*

class CalendarFragment : TodayFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view_stub.inflate()
        displaySelectedDateLabel()

        mViewModel.getReminders()
            .observe(viewLifecycleOwner) { reminders: List<Reminder> ->
                displayTheReminders(reminders as ArrayList<Reminder>)
            }

        calendar_view.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val monthFormatted = month + 1
            val simpleDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)

            val dateInMilliseconds =
                simpleDateFormat.parse("$dayOfMonth/$monthFormatted/$year")?.time ?: 0
            displaySelectedDateLabel(dateInMilliseconds)

            mViewModel.setDateOfReminders(dateInMilliseconds)
        }
    }

    private fun displaySelectedDateLabel(dateInMilliseconds: Long? = null) {
        val simpleDateFormat = SimpleDateFormat("EEEE dd MMMM, yyyy", getLocale())
        val dateFormatted = simpleDateFormat.format(dateInMilliseconds ?: calendar_view.date)

        val ummalquraCalendar = UmmalquraCalendar()
        ummalquraCalendar.time = Date(dateInMilliseconds ?: calendar_view.date)
        val hijriDateFormat = SimpleDateFormat("", getLocale())
        hijriDateFormat.calendar = ummalquraCalendar
        hijriDateFormat.applyPattern("dd MMMM, yyyy")

        selected_date.text =
            "$dateFormatted / ${hijriDateFormat.format(ummalquraCalendar.time)} A.H"
    }
}