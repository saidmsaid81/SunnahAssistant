package com.thesunnahrevival.sunnahassistant.utilities;


import android.content.Context;
import android.util.Log;

import com.github.msarhan.ummalqura.calendar.UmmalquraCalendar;

import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class TimeDateUtil {

    public static final String NOT_SET = "Time Not Set";

    public static String getNameOfTheDay(long timeInMilliseconds) {
        return new SimpleDateFormat("EE", Locale.ENGLISH).format(timeInMilliseconds);
    }

    public static String getFullNameOfTheDay(long timeInMilliseconds) {
        return new SimpleDateFormat("EEEE", Locale.ENGLISH).format(timeInMilliseconds);
    }


    public static int getDayDate(long timeInMilliseconds) {
        return Integer.parseInt(new SimpleDateFormat("dd", Locale.ENGLISH).format(timeInMilliseconds));
    }

    public static int getMonthNumber(long timeInMilliseconds) {
        return Integer.parseInt(new SimpleDateFormat("MM", Locale.ENGLISH).format(timeInMilliseconds)) - 1;
    }

    public static String getYear(long timeInMilliseconds) {
        return new SimpleDateFormat("yyyy", Locale.ENGLISH).format(timeInMilliseconds);
    }

    public static String formatTimeInMilliseconds(Context context, long timeInMilliseconds) {
        if (timeInMilliseconds != 172800000) {
            SimpleDateFormat sdf = android.text.format.DateFormat.is24HourFormat(context) ?
                    new SimpleDateFormat("HH:mm", Locale.getDefault()) :
                    new SimpleDateFormat("hh:mm a", Locale.getDefault());

            return sdf.format(timeInMilliseconds);
        }
        return NOT_SET;
    }

    public static long calculateOffsetFromMidnight() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        try {
            return sdf.parse(sdf.format(new Date())).getTime() / 1000;
        } catch (ParseException e) {
            return 0;
        }
    }

    public static long getTimestampInSeconds(String timeString) {
        SimpleDateFormat format = new SimpleDateFormat("HH:mm", Locale.getDefault());
        try {
            java.util.Date date = format.parse(timeString);
            return date.getTime() / 1000;
        } catch (ParseException e) {
            Log.v("ParseException", e.getMessage());
            return 172800;
        }

    }

    public static String formatDaysFromCustomScheduledDays(ArrayList<String>customScheduleDays){
        StringBuilder days = new StringBuilder();
        if (customScheduleDays == null)
            return days.toString();
        for (String day : customScheduleDays){
            days.append(day);
            if (customScheduleDays.indexOf(day) != customScheduleDays.size() - 1)
                days.append(", ");
        }
        return days.toString();
    }

    public static String getMonthName(int month) {
        return new DateFormatSymbols().getMonths()[month];
    }

    public static int getLastDayOfMonth() {
        Calendar calendar = Calendar.getInstance();
        return calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
    }

    public static String getIslamicMonthName(int monthNumber){
        switch (monthNumber) {
            case 0:
                return "Muharram";
            case 1:
                return "Safar";
            case 3:
                return "Rabi' al-Awwal";
            case 4:
                return "Rabi' al-Thani";
            case 5:
                return "Jumada al-Ula";
            case 6:
                return "Rajab";
            case 7:
                return "Sha'ban";
            case 8:
                return "Ramadhan";
            case 9:
                return "Shawwal";
            case 10:
                return "Dhul-Qa'dah";
            case 11:
                return "Dhul-Hijjah";
        }
        return "Unknown";
    }

    public static String getHijriDate(){
        Calendar uCal = new UmmalquraCalendar();
        int year = uCal.get(Calendar.YEAR);
        int month = uCal.get(Calendar.MONTH);
        int day = uCal.get(Calendar.DAY_OF_MONTH);
        return getFullNameOfTheDay(System.currentTimeMillis()) + " " + day + " " + getIslamicMonthName(month) + ", " + year;
    }
}
