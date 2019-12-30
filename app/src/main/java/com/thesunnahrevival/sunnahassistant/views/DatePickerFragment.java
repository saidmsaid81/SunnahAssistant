package com.thesunnahrevival.sunnahassistant.views;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.widget.DatePicker;

import com.thesunnahrevival.sunnahassistant.utilities.TimeDateUtil;

import java.util.Calendar;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;


public class DatePickerFragment extends DialogFragment
        implements DatePickerDialog.OnDateSetListener {

    static int mDay = 0;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current date as the default date in the picker
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        if (mDay != 0)
            day = mDay;
        int lastDayOfMonth = c.getActualMaximum(Calendar.DAY_OF_MONTH);
        // Create a new instance of DatePickerDialog and return it
        DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), this, year, month, day);
        month++;
        datePickerDialog.getDatePicker().setMinDate(TimeDateUtil.getMillisecondsFromDate("01-" + month + "-" + year));
        datePickerDialog.getDatePicker().setMaxDate(
                TimeDateUtil.getMillisecondsFromDate(lastDayOfMonth + "-" + month + "-" + year));
        return datePickerDialog;
    }

    public void onDateSet(DatePicker view, int year, int month, int day) {
        mDay = day;
    }
}
