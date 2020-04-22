package com.thesunnahrevival.sunnahassistant.views;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.widget.DatePicker;

import java.util.Calendar;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;


public class DatePickerFragment extends DialogFragment
        implements DatePickerDialog.OnDateSetListener {

    static int mDay = 0;
    static int mMonth = 12;
    static int mYear = 0;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current date as the default date in the picker
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        //Conditions for when the reminder already has a set date
        if (mDay != 0)
            day = mDay;
        if (mMonth != 12)
            month = mMonth;
        if (mYear != 0)
            year = mYear;

        int lastDayOfMonth = c.getActualMaximum(Calendar.DAY_OF_MONTH);
        // Create a new instance of DatePickerDialog and return it
        DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), this, year, month, day);
        return datePickerDialog;
    }

    public void onDateSet(DatePicker view, int year, int month, int day) {
        mDay = day;
        mMonth = month;
        mYear = year;
    }
}
