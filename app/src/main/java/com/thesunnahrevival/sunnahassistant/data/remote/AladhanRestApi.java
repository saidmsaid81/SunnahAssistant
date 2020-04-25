package com.thesunnahrevival.sunnahassistant.data.remote;

import com.thesunnahrevival.sunnahassistant.data.local.ReminderDAO;
import com.thesunnahrevival.sunnahassistant.data.model.HijriDateData;
import com.thesunnahrevival.sunnahassistant.data.model.HijriDateData.Hijri;
import com.thesunnahrevival.sunnahassistant.data.model.HijriDateData.Month;
import com.thesunnahrevival.sunnahassistant.data.model.PrayerTimes;
import com.thesunnahrevival.sunnahassistant.data.model.PrayerTimes.Timings;
import com.thesunnahrevival.sunnahassistant.data.model.Reminder;
import com.thesunnahrevival.sunnahassistant.utilities.GeneralSaveDataAsyncTask;
import com.thesunnahrevival.sunnahassistant.utilities.SunnahAssistantUtil;
import com.thesunnahrevival.sunnahassistant.utilities.TimeDateUtil;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class AladhanRestApi {

    private static final String[] PRAYER_NAMES = {"Fajr Prayer", "Dhuhr Prayer", "Asr Prayer", "Maghrib Prayer", "Isha Prayer"};
    public static MutableLiveData<String> errorMessages = new MutableLiveData<>();
    private static AladhanRestApi instance;
    private static ReminderDAO mReminderDAO;
    private final AladhanInterface mAladhanInterface;

    private AladhanRestApi() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.aladhan.com/v1/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        mAladhanInterface = retrofit.create(AladhanInterface.class);
    }

    public static AladhanRestApi getInstance(ReminderDAO reminderDAO) {
        if (instance == null) {
            instance = new AladhanRestApi();
            mReminderDAO = reminderDAO;
        }
        return instance;
    }

    public void fetchHijriData(String monthNumber, String year, int adjustment) {
        errorMessages.setValue("Refreshing Hijri date data...");
        mAladhanInterface.getHijriCalendar(monthNumber, year, adjustment).enqueue(new Callback<HijriDateData>() {
            @Override
            public void onResponse(@NonNull Call<HijriDateData> call, @NonNull Response<HijriDateData> response) {
                if (!response.isSuccessful()) {
                    return;
                }
                ProcessAladhanData.processHijriDateData(response.body());
            }

            @Override
            public void onFailure(@NonNull Call<HijriDateData> call, @NonNull Throwable t) {
                errorMessages.setValue("Error fetching Hijri Date. Please Check Your Internet Connection");
            }
        });

    }


    public void fetchPrayerTimes(float latitude, float longitude, final String month, final String year, int method, int asrCalculationMethod, int latitudeAdjustmentMethod) {
        errorMessages.setValue("Refreshing Prayer Times data...");
        mAladhanInterface.getPrayerTimes(latitude, longitude, month, year, method,
                asrCalculationMethod, latitudeAdjustmentMethod).enqueue(new Callback<PrayerTimes>() {
            @Override
            public void onResponse(@NonNull Call<PrayerTimes> call, @NonNull Response<PrayerTimes> response) {
                if (!response.isSuccessful()) {
                    return;
                }
                if (response.body() != null) {
                    ProcessAladhanData.processPrayerTimesData(response.body());
                } else {
                    errorMessages.setValue("Error fetching Prayer times data. Please Check Your Internet Connection");
                }
            }

            @Override
            public void onFailure(@NonNull Call<PrayerTimes> call, @NonNull Throwable t) {
                errorMessages.setValue("Error fetching Prayer times data. Please Check Your Internet Connection");
            }
        });
    }

    /*
        Inner Class for processing fetched data
     */
    private static class ProcessAladhanData {

        private static void processHijriDateData(HijriDateData hijriDateData) {
            int size = 0;
            if (hijriDateData != null) {
                size = hijriDateData.getData().size();
            }
            ArrayList<Hijri> list = new ArrayList<>();
            ArrayList importantMonthlyReminders = new ArrayList();
            for (int i = 0; i < size; i++) {
                String day = hijriDateData.getData().get(i).getHijri().getDay();
                Month month = hijriDateData.getData().get(i).getHijri().getMonth();
                String year = hijriDateData.getData().get(i).getHijri().getYear();

                Hijri hijri = new Hijri(day, month.getEn(), year);
                hijri.setId(i + 1);
                list.add(hijri);

                //Add the important monthly reminders
                int calendarDay = Integer.parseInt(day);
                if ((calendarDay >= 13) && (calendarDay <= 15) && !month.getEn().matches("Ramaḍān")){
                    if (month.getEn().matches("Dhū al-Ḥijjah") && calendarDay == 13)
                        continue;
                    Reminder reminder = new Reminder(
                            "Fasting Ayyamul Beidh (White Days)",
                            "",
                            null,
                            SunnahAssistantUtil.SUNNAH,
                            SunnahAssistantUtil.ONE_TIME,
                            false, hijri.getId(),
                            TimeDateUtil.getMonthNumber(System.currentTimeMillis()) - 1, Integer.parseInt(TimeDateUtil.getYear(System.currentTimeMillis())), 0 ,
                            new ArrayList<>());
                    reminder.setId(-calendarDay);

                    importantMonthlyReminders.add(reminder);
                }
            }

            new GeneralSaveDataAsyncTask(GeneralSaveDataAsyncTask.ADD_HIJRI_DATA, mReminderDAO).execute(list);
            new GeneralSaveDataAsyncTask(GeneralSaveDataAsyncTask.ADD_LIST_OF_REMINDERS,
                    mReminderDAO).execute(importantMonthlyReminders);
            errorMessages.setValue("Successful");
        }

        private static void processPrayerTimesData(PrayerTimes prayerTimes) {
            ArrayList listOfPrayerTimes = new ArrayList();

            List<PrayerTimes.Datum> rawPrayerTimes = prayerTimes.getData();

            for (int i = 0; i < rawPrayerTimes.size(); i++) {
                Timings timings = rawPrayerTimes.get(i).getTimings();
                int day = rawPrayerTimes.get(i).getDate().getGregorian().getDay();
                String fajrTime = timings.getFajr().substring(0, 5);
                String dhuhrTime = timings.getDhuhr().substring(0, 5);
                String asrTime = timings.getAsr().substring(0, 5);
                String maghribTime = timings.getMaghrib().substring(0, 5);
                String ishaTime = timings.getIsha().substring(0, 5);

                String[] times = {fajrTime, dhuhrTime, asrTime, maghribTime, ishaTime};

                for (int j = 0; j < times.length; j++) {
                    Reminder reminder = new Reminder(
                            PRAYER_NAMES[j],
                            "",
                            TimeDateUtil.getTimestampInSeconds(times[j]),
                            SunnahAssistantUtil.PRAYER,
                            SunnahAssistantUtil.DAILY,
                            false, day,
                            null, null, 0,
                            null);
                    listOfPrayerTimes.add(reminder);
                }

            }

            new GeneralSaveDataAsyncTask(GeneralSaveDataAsyncTask.ADD_LIST_OF_REMINDERS,
                    mReminderDAO).execute(listOfPrayerTimes);
            errorMessages.setValue("Successful");
        }
    }

}
