package com.thesunnahrevival.sunnahassistant.viewmodels;

import android.app.Application;
import android.text.Html;
import android.text.Spanned;

import com.thesunnahrevival.sunnahassistant.data.SunnahAssistantRepository;
import com.thesunnahrevival.sunnahassistant.data.model.AppSettings;
import com.thesunnahrevival.sunnahassistant.data.model.HijriDateData;
import com.thesunnahrevival.sunnahassistant.utilities.TimeDateUtil;

import java.util.Date;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

public class SunnahAssistantViewModel extends AndroidViewModel {

    final SunnahAssistantRepository mRepository;
    public int month;
    public LiveData<AppSettings> mSettings;
    String mNameOfTheDay;
    String mYear;
    private LiveData<HijriDateData.Hijri> mHijriDate;


    public SunnahAssistantViewModel(@NonNull Application application) {
        super(application);
        mRepository = SunnahAssistantRepository.getInstance(application);
        setDateParameters();
        mHijriDate = mRepository.getHijriDate();
        mSettings = mRepository.getAppSettings();
    }

    private void setDateParameters() {
        long timeInMilliseconds = new Date().getTime();
        mNameOfTheDay = TimeDateUtil.getNameOfTheDay(timeInMilliseconds);
        int day = TimeDateUtil.getDayDate(timeInMilliseconds);
        mRepository.setDay(day);
        month = TimeDateUtil.getMonthNumber(timeInMilliseconds);
        mYear = TimeDateUtil.getYear(timeInMilliseconds);
    }

    public LiveData<Spanned> getHijriDateString() {
        return Transformations.map(mHijriDate, hijriDateString -> {
                    try {
                        return Html.fromHtml(
                                "<b>Today's Hijri Date:</b>  " +
                                        hijriDateString.getDay() + " " + hijriDateString.getMonthName() + ", " +
                                        hijriDateString.getYear() + " A.H");
                    } catch (NullPointerException e) {
                        return null;
                    }
                }
        );
    }

}
