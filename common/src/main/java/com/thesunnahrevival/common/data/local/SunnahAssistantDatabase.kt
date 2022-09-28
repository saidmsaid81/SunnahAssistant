package com.thesunnahrevival.common.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.thesunnahrevival.common.R
import com.thesunnahrevival.common.data.model.AppSettings
import com.thesunnahrevival.common.data.model.Reminder
import com.thesunnahrevival.common.data.typeconverters.RoomTypeConverter
import com.thesunnahrevival.common.utilities.demoReminder
import com.thesunnahrevival.common.utilities.initialSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

@Database(entities = [Reminder::class, AppSettings::class], version = 7)
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
                database.execSQL("UPDATE app_settings SET isAfterUpdate = 1")
            }
        }

        private val MIGRATION_4_5: Migration = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("UPDATE app_settings SET isAfterUpdate = 1")
            }
        }

        private val MIGRATION_5_6: Migration = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
//                database.execSQL("UPDATE app_settings SET isAfterUpdate = 1")
                database.execSQL(
                    "ALTER TABLE app_settings ADD COLUMN generatePrayerRemindersAfter INTEGER DEFAULT 0 NOT NULL"
                )

                database.execSQL(
                    "ALTER TABLE app_settings ADD COLUMN generatePrayerTimeForPrayer TEXT DEFAULT \"\" NOT NULL"
                )

                database.execSQL(
                    "ALTER TABLE reminders_table ADD COLUMN isComplete INTEGER DEFAULT 0 NOT NULL"
                )
                database.execSQL(
                    "ALTER TABLE reminders_table RENAME COLUMN offset TO offsetInMinutes"
                )
                database.execSQL(
                    "ALTER TABLE reminders_table ADD COLUMN predefinedReminderInfo TEXT DEFAULT '' NOT NULL"
                )
                database.execSQL(
                    "ALTER TABLE reminders_table ADD COLUMN predefinedReminderLink TEXT DEFAULT '' NOT NULL"
                )

                database.execSQL(
                    "UPDATE reminders_table SET reminderInfo = ''" +
                            " WHERE reminderInfo LIKE '%href%'"
                )

                /**
                 * This is just for precaution to prevent malformed reminders from triggering
                 * IllegalArgumentException. Reset all malformed reminders to 1/1/1 then present
                 * them to the user to fix them
                 */
                //For one time reminders
                database.execSQL(
                    "UPDATE reminders_table SET day = 1, month = 0, year = 1" +
                            " WHERE (month NOT BETWEEN 0 AND 11)" +
                            " AND frequency = 0 "
                )
                database.execSQL(
                    "UPDATE reminders_table SET day = 1, month = 0, year = 1" +
                            " WHERE (year < 1) AND frequency = 0 "
                )

                //January, March, May, July, August, October, December
                database.execSQL(
                    "UPDATE reminders_table SET day = 1, month = 0, year = 1" +
                            " WHERE (day NOT BETWEEN 1 AND 31)" +
                            " AND month IN (0, 2, 4, 6, 7, 9, 11)" +
                            " AND frequency = 0 "
                )

                //April, June, September, November
                database.execSQL(
                    "UPDATE reminders_table SET day = 1, month = 0, year = 1" +
                            " WHERE (day NOT BETWEEN 1 AND 30)" +
                            " AND month IN (3, 5, 8, 10)" +
                            " AND frequency = 0 "
                )

                //February (Non-leap year)
                database.execSQL(
                    "UPDATE reminders_table SET day = 1, month = 0, year = 1" +
                            " WHERE (day NOT BETWEEN 1 AND 28)" +
                            " AND month = 1" +
                            " AND MOD(year, 4) != 0" +
                            " AND frequency = 0 "
                )

                //February (leap year)
                database.execSQL(
                    "UPDATE reminders_table SET day = 1, month = 0, year = 1 " +
                            " WHERE (day NOT BETWEEN 1 AND 29)" +
                            " AND month = 1" +
                            " AND MOD(year, 4) = 0" +
                            " AND frequency = 0 "
                )

                //Monthly Reminder
                database.execSQL(
                    "UPDATE reminders_table SET day = 1, month = 0, year = 1" +
                            " WHERE (day NOT BETWEEN 1 AND 31)" +
                            " AND frequency = 3 "
                )
            }
        }

        private val MIGRATION_6_7: Migration = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE app_settings ADD COLUMN prayerTimeOffsetsInMinutes TEXT DEFAULT \"\" NOT NULL"
                )
            }
        }

        fun getInstance(context: Context): SunnahAssistantDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }


        private fun buildDatabase(context: Context) = Room.databaseBuilder(
            context.applicationContext,
            SunnahAssistantDatabase::class.java, "SunnahAssistant.db"
        )
            .addCallback(
                object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        CoroutineScope(Dispatchers.IO).launch {
                            INSTANCE?.reminderDao()?.insertReminder(
                                demoReminder(
                                    context.getString(R.string.demo_reminder),
                                    context.resources.getStringArray(R.array.categories)[3]
                                )
                            )

                            val categories = TreeSet<String>()
                            categories.addAll(context.resources.getStringArray(R.array.categories))
                            INSTANCE?.reminderDao()?.insertSettings(initialSettings(categories))
                        }
                    }
                }
            )
            .addMigrations(
                MIGRATION_1_2,
                MIGRATION_2_3,
                MIGRATION_3_4,
                MIGRATION_4_5,
                MIGRATION_5_6,
                MIGRATION_6_7
            )
            .build()
    }
}