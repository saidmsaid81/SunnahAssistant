package com.thesunnahrevival.sunnahassistant.views;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.TimePicker;

import java.util.Calendar;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.MutableLiveData;

public class TimePickerFragment extends DialogFragment
        implements TimePickerDialog.OnTimeSetListener {

    public static final String TAG = "TimePickerFragment";
    static MutableLiveData<String> timeSet = new MutableLiveData<>();

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current time as the default values for the picker
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);
        if (timeSet.getValue() != null) {
            try {
                String[] strings = timeSet.getValue().split(":");
                hour = Integer.parseInt(strings[0]);
                minute = Integer.parseInt(strings[1]);
            } catch (NumberFormatException e) {
                Log.v(TAG, "String cannot be converted to  valid time");
            }

        }


        // Create a new instance of TimePickerDialog and return it
        return new TimePickerDialog(getActivity(), this, hour, minute,
                DateFormat.is24HourFormat(getActivity()));
    }

    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        StringBuilder time = new StringBuilder();
        if (hourOfDay < 10)
            time.append("0");
        time.append(hourOfDay);
        time.append(":");
        if (minute < 10)
            time.append("0");
        time.append(minute);
        timeSet.setValue(time.toString());
    }
}
