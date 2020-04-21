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
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(entities = {Reminder.class, HijriDateData.Hijri.class, AppSettings.class}, version = 2)
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

            final Migration MIGRATION_1_2 = new Migration(1, 2) {
                @Override
                public void migrate(SupportSQLiteDatabase database) {
                    database.execSQL("ALTER TABLE app_settings ADD COLUMN isDisplayHijriDate INTEGER DEFAULT 0 NOT NULL");
                    database.execSQL("ALTER TABLE app_settings ADD COLUMN savedSpinnerPosition INTEGER DEFAULT 0 NOT NULL");
                    database.execSQL("ALTER TABLE app_settings ADD COLUMN isExpandedLayout INTEGER DEFAULT 1 NOT NULL");
                    database.execSQL("ALTER TABLE app_settings ADD COLUMN notificationToneUri TEXT DEFAULT ''");
                    database.execSQL("ALTER TABLE app_settings ADD COLUMN isVibrate INTEGER DEFAULT 0 NOT NULL");
                    database.execSQL("ALTER TABLE app_settings ADD COLUMN priority INTEGER DEFAULT 3 NOT NULL");
                    database.execSQL("ALTER TABLE app_settings ADD COLUMN categories TEXT DEFAULT 'Uncategorized,Sunnah,Other,Prayer'");
                }
            };


            sDatabase = Room.databaseBuilder(context.getApplicationContext(),
                    SunnahAssistantDatabase.class, "SunnahAssistant.db")
                    .addCallback(roomCallback)
                    .addMigrations(MIGRATION_1_2)
                    .build();
        }

        return sDatabase;
    }

    public abstract ReminderDAO reminderDao();

}



