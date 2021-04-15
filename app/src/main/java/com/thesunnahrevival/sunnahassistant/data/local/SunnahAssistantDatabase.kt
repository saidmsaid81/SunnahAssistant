package com.thesunnahrevival.sunnahassistant.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.thesunnahrevival.sunnahassistant.R
import com.thesunnahrevival.sunnahassistant.data.model.AppSettings
import com.thesunnahrevival.sunnahassistant.data.model.Reminder
import com.thesunnahrevival.sunnahassistant.data.typeconverters.RoomTypeConverter
import com.thesunnahrevival.sunnahassistant.utilities.demoReminder
import com.thesunnahrevival.sunnahassistant.utilities.initialSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

@Database(entities = [Reminder::class, AppSettings::class], version = 4)
@TypeConverters(RoomTypeConverter::class)
abstract class SunnahAssistantDatabase : RoomDatabase() {
    abstract fun reminderDao(): ReminderDao

    companion object {
        @Volatile
        private var INSTANCE: SunnahAssistantDatabase? = null
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

        private val MIGRATION_2_3: Migration = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                //Due to Localization
                database.execSQL("UPDATE reminders_table SET frequency = '0' WHERE frequency = 'One Time'")
                database.execSQL("UPDATE reminders_table SET frequency = '1' WHERE frequency = 'Daily'")
                database.execSQL("UPDATE reminders_table SET frequency = '2' WHERE frequency = 'Weekly'")
                database.execSQL("UPDATE reminders_table SET frequency = '3' WHERE frequency = 'Monthly'")

                database.execSQL("UPDATE reminders_table SET customScheduleDays = REPLACE(customScheduleDays,'Wedy','4');")
                database.execSQL("UPDATE reminders_table SET customScheduleDays = REPLACE(customScheduleDays,'Sun','1');")
                database.execSQL("UPDATE reminders_table SET customScheduleDays = REPLACE(customScheduleDays,'Mon','2');")
                database.execSQL("UPDATE reminders_table SET customScheduleDays = REPLACE(customScheduleDays,'Tue','3');")
                database.execSQL("UPDATE reminders_table SET customScheduleDays = REPLACE(customScheduleDays,'Wed','4');")
                database.execSQL("UPDATE reminders_table SET customScheduleDays = REPLACE(customScheduleDays,'Thu','5');")
                database.execSQL("UPDATE reminders_table SET customScheduleDays = REPLACE(customScheduleDays,'Fri','6');")
                database.execSQL("UPDATE reminders_table SET customScheduleDays = REPLACE(customScheduleDays,'Sat','7');")

                database.execSQL("UPDATE app_settings SET formattedAddress = '' WHERE formattedAddress = 'Location cannot be empty'")
                database.execSQL("UPDATE app_settings SET isAfterUpdate = 1")
                database.execSQL("ALTER TABLE app_settings ADD COLUMN language TEXT DEFAULT 'en' NOT NULL")
                database.execSQL("ALTER TABLE app_settings ADD COLUMN doNotDisturbMinutes INTEGER DEFAULT 0 NOT NULL")
                database.execSQL("ALTER TABLE app_settings ADD COLUMN useReliableAlarms INTEGER DEFAULT 1 NOT NULL")
                database.execSQL("ALTER TABLE app_settings ADD COLUMN numberOfLaunches INTEGER DEFAULT 0 NOT NULL")
            }
        }

        private val MIGRATION_3_4: Migration = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE app_settings ADD COLUMN shareAnonymousUsageData INTEGER DEFAULT 1 NOT NULL")
            }
        }


        fun getInstance(context: Context): SunnahAssistantDatabase =
                INSTANCE ?: synchronized(this) {
                    INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
                }


        private fun buildDatabase(context: Context) = Room.databaseBuilder(context.applicationContext,
                SunnahAssistantDatabase::class.java, "SunnahAssistant.db")
                .addCallback(
                        object : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                CoroutineScope(Dispatchers.IO).launch {
                    INSTANCE?.reminderDao()?.insertReminder(demoReminder(context.getString(R.string.demo_reminder), context.resources.getStringArray(R.array.categories)[3]))

                    val categories = TreeSet<String>()
                    categories.addAll(context.resources.getStringArray(R.array.categories))
                    INSTANCE?.reminderDao()?.insertSettings(initialSettings(categories))
                }
            }
        }
                )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                .build()
    }
}