package com.thesunnahrevival.common.views.dialogs

import android.app.Dialog
import android.app.TimePickerDialog
import android.app.TimePickerDialog.OnTimeSetListener
import android.os.Bundle
import android.text.format.DateFormat
import android.util.Log
import android.widget.TimePicker
import androidx.fragment.app.DialogFragment
import com.thesunnahrevival.common.utilities.formatTimeInMilliseconds
import com.thesunnahrevival.common.utilities.getTimestampInSeconds
import java.util.*

class TimePickerFragment : DialogFragment(), OnTimeSetListener {

    private var mListener: OnTimeSetListener? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        if (mListener == null) {
            Log.e("Date Picker Error", "Please implement TimePickerFragment.OnTimeSetListener ")
            dismiss()
        }

        // Use the current time as the default values for the picker
        val calendar = Calendar.getInstance()
        var hour = calendar[Calendar.HOUR_OF_DAY]
        var minute = calendar[Calendar.MINUTE]

        val timeSet = arguments?.getString(TIMESET)
        if (timeSet != null) {
            try {
                val strings = timeSet.split(":").toTypedArray()
                hour = strings[0].toInt()
                minute = strings[1].toInt()

            } catch (e: NumberFormatException) {
                Log.e("TimePickerFragment", "String cannot be converted to  valid time")
            }
        }


        // Create a new instance of TimePickerDialog and return it
        return TimePickerDialog(
            activity, this, hour, minute,
            DateFormat.is24HourFormat(activity)
        )
    }

    override fun onTimeSet(view: TimePicker, hourOfDay: Int, minute: Int) {
        val hour: String
        val amOrPm: String

        if (view.is24HourView) {
            hour = hourOfDay.toString()
            amOrPm = ""

        } else if (!view.is24HourView && hourOfDay < 12) {
            hour = hourOfDay.toString()
            amOrPm = " am"
        } else {
            hour = (hourOfDay - 12).toString()
            amOrPm = " pm"
        }


        val time = StringBuilder()
        if (hourOfDay < 10) {
            time.append("0")
        }
        time.append(hour)
        time.append(":")
        if (minute < 10)
            time.append("0")
        time.append(minute)
        time.append(amOrPm)
        val timeInMilliseconds = getTimestampInSeconds(time.toString()) * 1000
        mListener?.onTimeSet(formatTimeInMilliseconds(requireContext(), timeInMilliseconds))
    }

    fun setListener(listener: OnTimeSetListener) {
        mListener = listener
    }

    interface OnTimeSetListener {
        fun onTimeSet(timeString: String)
    }

    companion object {
        const val TIMESET = "TIMESET"
    }
}