package com.thesunnahrevival.sunnahassistant.views.dialogs

import android.app.Dialog
import android.app.TimePickerDialog
import android.app.TimePickerDialog.OnTimeSetListener
import android.os.Bundle
import android.text.format.DateFormat
import android.util.Log
import android.widget.TimePicker
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.MutableLiveData
import com.thesunnahrevival.sunnahassistant.utilities.formatTimeInMilliseconds
import com.thesunnahrevival.sunnahassistant.utilities.getTimestampInSeconds
import java.util.*

class TimePickerFragment : DialogFragment(), OnTimeSetListener {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Use the current time as the default values for the picker
        val c = Calendar.getInstance()
        var hour = c[Calendar.HOUR_OF_DAY]
        var minute = c[Calendar.MINUTE]
        if (timeSet.value != null) {
            try {
                timeSet.value?.let {
                    val strings =  it.split(":").toTypedArray()
                    hour = strings[0].toInt()
                    minute = strings[1].toInt()
                }
            } catch (e: NumberFormatException) {
                Log.v(TAG, "String cannot be converted to  valid time")
            }
        }


        // Create a new instance of TimePickerDialog and return it
        return TimePickerDialog(activity, this, hour, minute,
                DateFormat.is24HourFormat(activity))
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
        timeSet.value = formatTimeInMilliseconds(requireContext(), timeInMilliseconds)
    }

    companion object {
        const val TAG = "TimePickerFragment"
        var timeSet = MutableLiveData<String?>(null)
    }
}