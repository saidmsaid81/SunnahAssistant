package com.thesunnahrevival.sunnahassistant.data.local;

import android.content.Context;

import com.thesunnahrevival.sunnahassistant.data.model.AppSettings;
import com.thesunnahrevival.sunnahassistant.data.model.HijriDateData;
import com.thesunnahrevival.sunnahassistant.data.model.Reminder;
import com.thesunnahrevival.sunnahassistant.data.typeconverters.RoomTypeConverter;
import com.thesunnahrevival.sunnahassistant.utilities.GeneralSaveDataAsyncTask;
import com.thesunnahrevival.sunnahassistant.utilities.SunnahAssistantUtil;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(entities = {Reminder.class, HijriDateData.Hijri.class, AppSettings.class}, version = 1)
@TypeConverters(RoomTypeConverter.class)
public abstract class SunnahAssistantDatabase extends RoomDatabase {

    private static SunnahAssistantDatabase sDatabase;

    public static synchronized SunnahAssistantDatabase getInstance(Context context) {
        if (sDatabase == null) {

            Callback roomCallback = new Callback() {
                @Override
                public void onCreate(@NonNull SupportSQLiteDatabase db) {
                    super.onCreate(db);
                    new GeneralSaveDataAsyncTask(GeneralSaveDataAsyncTask.ADD_LIST_OF_REMINDERS,
                            sDatabase.reminderDao()).execute(SunnahAssistantUtil.demoReminder());
                    new GeneralSaveDataAsyncTask(GeneralSaveDataAsyncTask.ADD_SETTINGS, sDatabase.reminderDao())
                            .execute(SunnahAssistantUtil.initialSettings());
                }
            };

            sDatabase = Room.databaseBuilder(context.getApplicationContext(),
                    SunnahAssistantDatabase.class, "SunnahAssistant.db")
                    .addCallback(roomCallback)
                    .build();
        }

        return sDatabase;
    }

    public abstract ReminderDAO reminderDao();

}



