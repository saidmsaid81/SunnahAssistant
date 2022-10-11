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
import com.thesunnahrevival.sunnahassistant.data.model.ToDo
import com.thesunnahrevival.sunnahassistant.data.typeconverters.RoomTypeConverter
import com.thesunnahrevival.sunnahassistant.utilities.demoToDo
import com.thesunnahrevival.sunnahassistant.utilities.initialSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.*

const val DB_NAME = "SunnahAssistant.db"
const val DB_NAME_TEMP = "SunnahAssistant_temp.db"

@Database(entities = [ToDo::class, AppSettings::class], version = 6)
@TypeConverters(RoomTypeConverter::class)
abstract class SunnahAssistantDatabase : RoomDatabase() {
    abstract fun toDoDao(): ToDoDao

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
                    "ALTER TABLE app_settings ADD COLUMN enablePrayerTimeAlertsFor TEXT DEFAULT '' NOT NULL"
                )

                database.execSQL(
                    "ALTER TABLE app_settings ADD COLUMN prayerTimeOffsetsInMinutes TEXT DEFAULT '' NOT NULL"
                )

                database.execSQL(
                    "ALTER TABLE reminders_table ADD COLUMN completedDates TEXT DEFAULT '' NOT NULL"
                )
                database.execSQL(
                    "ALTER TABLE reminders_table ADD COLUMN predefinedReminderInfo TEXT DEFAULT '' NOT NULL"
                )
                database.execSQL(
                    "ALTER TABLE reminders_table ADD COLUMN predefinedReminderLink TEXT DEFAULT '' NOT NULL"
                )
                database.execSQL(
                    "ALTER TABLE reminders_table ADD COLUMN repeatsFromDate TEXT DEFAULT '' NOT NULL"
                )

                database.execSQL(
                    "UPDATE reminders_table SET reminderInfo = ''" +
                            " WHERE reminderInfo LIKE '%href%'"
                )

                /**
                 * This is just for precaution to prevent malformed to-dos from triggering
                 * IllegalArgumentException. Reset all malformed to-dos to 1/1/1 then present
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
                            " AND year % 4 != 0" +
                            " AND frequency = 0 "
                )

                //February (leap year)
                database.execSQL(
                    "UPDATE reminders_table SET day = 1, month = 0, year = 1 " +
                            " WHERE (day NOT BETWEEN 1 AND 29)" +
                            " AND month = 1" +
                            " AND year % 4 = 0" +
                            " AND frequency = 0 "
                )

                //Monthly Reminder
                database.execSQL(
                    "UPDATE reminders_table SET day = 1, month = 0, year = 1" +
                            " WHERE (day NOT BETWEEN 1 AND 31)" +
                            " AND frequency = 3 "
                )

                val now = LocalDate.now()
                val sql =
                    "UPDATE reminders_table SET repeatsFromDate = \"$now\" WHERE month == 12 AND year == 0"
                database.execSQL(sql)
            }
        }


        fun getInstance(context: Context): SunnahAssistantDatabase {
            if (INSTANCE?.isOpen == false)
                INSTANCE = null
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }
        }

        private fun buildDatabase(context: Context) = Room.databaseBuilder(
            context.applicationContext,
            SunnahAssistantDatabase::class.java, "SunnahAssistant.db"
        )
            .addCallback(
                object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        val tempDBBackup = context.getDatabasePath(DB_NAME_TEMP)
                        if (!tempDBBackup.exists()) {
                            CoroutineScope(Dispatchers.IO).launch {
                                INSTANCE?.toDoDao()?.insertToDo(
                                    demoToDo(
                                        context.getString(R.string.demo_to_dos),
                                        context.resources.getStringArray(R.array.categories)[3]
                                    )
                                )

                                val categories = TreeSet<String>()
                                categories.addAll(context.resources.getStringArray(R.array.categories))
                                INSTANCE?.toDoDao()?.insertSettings(initialSettings(categories))
                            }
                        }
                    }
                }
            )
            .addMigrations(
                MIGRATION_1_2,
                MIGRATION_2_3,
                MIGRATION_3_4,
                MIGRATION_4_5,
                MIGRATION_5_6
            )
            .build()
    }
}