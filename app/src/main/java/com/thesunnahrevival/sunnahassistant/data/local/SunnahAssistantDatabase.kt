package com.thesunnahrevival.sunnahassistant.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.thesunnahrevival.sunnahassistant.data.model.AppSettings
import com.thesunnahrevival.sunnahassistant.data.model.Reminder
import com.thesunnahrevival.sunnahassistant.data.typeconverters.RoomTypeConverter
import com.thesunnahrevival.sunnahassistant.utilities.SunnahAssistantUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [Reminder::class, AppSettings::class], version = 2)
@TypeConverters(RoomTypeConverter::class)
abstract class SunnahAssistantDatabase : RoomDatabase() {
    abstract fun reminderDao(): ReminderDAO

    companion object {
        @Volatile
        private var INSTANCE: SunnahAssistantDatabase? = null
        private val roomCallback: Callback = object : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                CoroutineScope(Dispatchers.IO).launch {
                    INSTANCE?.reminderDao()?.insertReminder(SunnahAssistantUtil.demoReminder())
                    INSTANCE?.reminderDao()?.insertSettings(SunnahAssistantUtil.initialSettings())
                }
            }
        }

        private val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE app_settings ADD COLUMN isDisplayHijriDate INTEGER DEFAULT 1 NOT NULL")
                database.execSQL("ALTER TABLE app_settings ADD COLUMN savedSpinnerPosition INTEGER DEFAULT 0 NOT NULL")
                database.execSQL("ALTER TABLE app_settings ADD COLUMN isExpandedLayout INTEGER DEFAULT 1 NOT NULL")
                database.execSQL("ALTER TABLE app_settings ADD COLUMN notificationToneUri TEXT DEFAULT ''")
                database.execSQL("ALTER TABLE app_settings ADD COLUMN isVibrate INTEGER DEFAULT 0 NOT NULL")
                database.execSQL("ALTER TABLE app_settings ADD COLUMN isAfterUpdate INTEGER DEFAULT 1 NOT NULL")
                database.execSQL("ALTER TABLE app_settings ADD COLUMN priority INTEGER DEFAULT 3 NOT NULL")
                database.execSQL("ALTER TABLE app_settings ADD COLUMN categories TEXT DEFAULT 'Uncategorized,Sunnah,Other,Prayer'")
                database.execSQL("ALTER TABLE app_settings ADD COLUMN isShowHijriDateWidget INTEGER DEFAULT 1 NOT NULL")
                database.execSQL("ALTER TABLE app_settings ADD COLUMN isShowNextReminderWidget INTEGER DEFAULT 1 NOT NULL")
                database.execSQL("ALTER TABLE app_settings ADD COLUMN latitudeAdjustmentMethod INTEGER DEFAULT 2 NOT NULL")
                database.execSQL("UPDATE app_settings SET method = 0")
                database.execSQL("UPDATE app_settings SET month = 12")
                database.execSQL("ALTER TABLE reminders_table ADD COLUMN month INTEGER DEFAULT 12 NOT NULL")
                database.execSQL("ALTER TABLE reminders_table ADD COLUMN year INTEGER DEFAULT 0 NOT NULL")
                database.execSQL("UPDATE reminders_table SET timeInSeconds = 172800 WHERE timeInSeconds = 86399")
                database.execSQL("DROP TABLE hijri_calendar")
            }
        }


        @JvmStatic
        fun getInstance(context: Context): SunnahAssistantDatabase =
                INSTANCE ?: synchronized(this) {
                    INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
                }


        private fun buildDatabase(context: Context) = Room.databaseBuilder(context.applicationContext,
                SunnahAssistantDatabase::class.java, "SunnahAssistant.db")
                .addCallback(roomCallback)
                .addMigrations(MIGRATION_1_2)
                .build()
    }
}