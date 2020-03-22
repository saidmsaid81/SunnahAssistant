package com.thesunnahrevival.sunnahassistant.data;

import android.app.Application;

import com.thesunnahrevival.sunnahassistant.data.local.ReminderDAO;
import com.thesunnahrevival.sunnahassistant.data.local.SunnahAssistantDatabase;
import com.thesunnahrevival.sunnahassistant.data.model.AppSettings;
import com.thesunnahrevival.sunnahassistant.data.model.GeocodingData;
import com.thesunnahrevival.sunnahassistant.data.model.HijriDateData.Hijri;
import com.thesunnahrevival.sunnahassistant.data.model.Reminder;
import com.thesunnahrevival.sunnahassistant.data.remote.AladhanRestApi;
import com.thesunnahrevival.sunnahassistant.data.remote.GeocodingRestApi;
import com.thesunnahrevival.sunnahassistant.utilities.GeneralSaveDataAsyncTask;
import com.thesunnahrevival.sunnahassistant.utilities.ReminderAsyncTask;
import com.thesunnahrevival.sunnahassistant.utilities.SunnahAssistantUtil;
import com.thesunnahrevival.sunnahassistant.utilities.TimeDateUtil;

import java.util.ArrayList;
import java.util.List;

import androidx.lifecycle.LiveData;

import static com.thesunnahrevival.sunnahassistant.utilities.GeneralSaveDataAsyncTask.DELETE_HIJRI_DATA;
import static com.thesunnahrevival.sunnahassistant.utilities.GeneralSaveDataAsyncTask.DELETE_LIST_OF_REMINDERS;
import static com.thesunnahrevival.sunnahassistant.utilities.GeneralSaveDataAsyncTask.SET_IS_LIGHT_MODE;
import static com.thesunnahrevival.sunnahassistant.utilities.GeneralSaveDataAsyncTask.UPDATE_SETTINGS;
import static com.thesunnahrevival.sunnahassistant.utilities.ReminderAsyncTask.ADD_REMINDER;
import static com.thesunnahrevival.sunnahassistant.utilities.ReminderAsyncTask.DELETE_REMINDER;
import static com.thesunnahrevival.sunnahassistant.utilities.ReminderAsyncTask.SET_PRAYER_TIME_ENABLED;
import static com.thesunnahrevival.sunnahassistant.utilities.ReminderAsyncTask.SET_REMINDER_ENABLED;
import static com.thesunnahrevival.sunnahassistant.utilities.ReminderAsyncTask.UPDATE_PRAYER_DETAILS;

public class SunnahAssistantRepository {

    private static SunnahAssistantRepository sSunnahAssistantRepository = null;
    private final AladhanRestApi mAladhanRestApi;
    private final GeocodingRestApi mGeocodingRestApi;
    private ReminderDAO mReminderDAO;
    private int mDay;

    private SunnahAssistantRepository(Application application) {
        mReminderDAO = SunnahAssistantDatabase.getInstance(application).reminderDao();
        mAladhanRestApi = AladhanRestApi.getInstance(mReminderDAO);
        mGeocodingRestApi = GeocodingRestApi.getInstance();
    }

    public synchronized static SunnahAssistantRepository getInstance(Application application) {
        if (sSunnahAssistantRepository == null) {
            sSunnahAssistantRepository = new SunnahAssistantRepository(application);
        }
        return sSunnahAssistantRepository;
    }

    public void setDay(int day) {
        mDay = day;
    }

    public void addReminder(Reminder reminder) {
        new ReminderAsyncTask(ADD_REMINDER, mReminderDAO).execute(reminder);
    }

    public void updatePrayerDetails(Reminder oldPrayerDetails, Reminder newPrayerDetails) {
        new ReminderAsyncTask(UPDATE_PRAYER_DETAILS, mReminderDAO).execute(oldPrayerDetails, newPrayerDetails);
    }

    public void deleteReminder(Reminder reminder) {
        new ReminderAsyncTask(DELETE_REMINDER, mReminderDAO).execute(reminder);
    }


    public void deleteHijriDate() {
        new GeneralSaveDataAsyncTask(DELETE_HIJRI_DATA, mReminderDAO).execute(new ArrayList<Hijri>());
    }

    public void deletePrayerTimesData() {
        new GeneralSaveDataAsyncTask(DELETE_LIST_OF_REMINDERS, mReminderDAO).execute(new ArrayList<Hijri>());
    }

    public void setReminderIsEnabled(Reminder reminder) {
        if (reminder.getCategory().matches(SunnahAssistantUtil.PRAYER))
            new ReminderAsyncTask(SET_PRAYER_TIME_ENABLED, mReminderDAO).execute(reminder);
        else
            new ReminderAsyncTask(SET_REMINDER_ENABLED, mReminderDAO).execute(reminder);
    }

    public LiveData<List<Reminder>> getAllReminders(int filter, String nameOfTheDay) {
        switch (filter) {
            case 1:
                return mReminderDAO.getPastReminders(TimeDateUtil.calculateOffsetFromMidnight(), mDay, nameOfTheDay);
            case 2:
                return mReminderDAO.getRemindersOnDay(mDay, TimeDateUtil.getNameOfTheDay(System.currentTimeMillis()));
            case 3:
                return mReminderDAO.getRemindersOnDay(mDay + 1, TimeDateUtil.getNameOfTheDay(System.currentTimeMillis() + 86400000));
            case 4:
                return mReminderDAO.getPrayerTimes(mDay);
            case 5:
                return mReminderDAO.getWeeklyReminders();
            case 6:
                return mReminderDAO.getMonthlyReminder();
            default:
                return mReminderDAO.getUpcomingReminders(TimeDateUtil.calculateOffsetFromMidnight(), mDay, nameOfTheDay);
        }
    }

    public LiveData<Hijri> getHijriDate() {
        return mReminderDAO.getHijriDate(mDay);
    }

    public LiveData<AppSettings> getAppSettings() {
        return mReminderDAO.getAppSettings();
    }

    public void updateAppSettings(AppSettings settings) {
        ArrayList list = new ArrayList();
        list.add(settings);
        new GeneralSaveDataAsyncTask(UPDATE_SETTINGS, mReminderDAO).execute(list);
    }

    public void setIsLightMode(boolean isLightMode) {
        ArrayList list = new ArrayList();
        list.add(isLightMode);
        new GeneralSaveDataAsyncTask(SET_IS_LIGHT_MODE, mReminderDAO).execute(list);
    }

    public void fetchHijriData(String monthNumber, String year, int adjustment) {
        mAladhanRestApi.fetchHijriData(monthNumber, year, adjustment);
    }

    public void fetchPrayerTimes(float latitude, float longitude, final String month, final String year, int method, int asrCalculationMethod) {
        mAladhanRestApi.fetchPrayerTimes(latitude, longitude, month, year, method, asrCalculationMethod);
    }

    public LiveData<String> getErrorMessages() {
        return AladhanRestApi.errorMessages;
    }

    public void fetchGeocodingData(String address) {
        mGeocodingRestApi.fetchGeocodingData(address);
    }

    public LiveData<GeocodingData> getGeocodingData() {
        return mGeocodingRestApi.getData();
    }

}
