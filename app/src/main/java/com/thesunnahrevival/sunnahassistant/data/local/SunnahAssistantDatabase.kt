package com.thesunnahrevival.sunnahassistant.data.local

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.thesunnahrevival.sunnahassistant.BuildConfig
import com.thesunnahrevival.sunnahassistant.R
import com.thesunnahrevival.sunnahassistant.data.model.entity.AdhkaarChapter
import com.thesunnahrevival.sunnahassistant.data.model.entity.AdhkaarItem
import com.thesunnahrevival.sunnahassistant.data.model.entity.AdhkaarItemBookmark
import com.thesunnahrevival.sunnahassistant.data.model.entity.AppSettings
import com.thesunnahrevival.sunnahassistant.data.model.entity.Ayah
import com.thesunnahrevival.sunnahassistant.data.model.entity.AyahBookmark
import com.thesunnahrevival.sunnahassistant.data.model.entity.AyahTranslation
import com.thesunnahrevival.sunnahassistant.data.model.entity.DailyHadith
import com.thesunnahrevival.sunnahassistant.data.model.entity.Footnote
import com.thesunnahrevival.sunnahassistant.data.model.entity.Language
import com.thesunnahrevival.sunnahassistant.data.model.entity.Line
import com.thesunnahrevival.sunnahassistant.data.model.entity.PageBookmark
import com.thesunnahrevival.sunnahassistant.data.model.entity.PinnedAdhkaarChapter
import com.thesunnahrevival.sunnahassistant.data.model.entity.PinnedSurah
import com.thesunnahrevival.sunnahassistant.data.model.entity.Surah
import com.thesunnahrevival.sunnahassistant.data.model.entity.ToDo
import com.thesunnahrevival.sunnahassistant.data.model.entity.Translation
import com.thesunnahrevival.sunnahassistant.data.typeconverters.RoomTypeConverter
import com.thesunnahrevival.sunnahassistant.utilities.DB_NAME
import com.thesunnahrevival.sunnahassistant.utilities.DB_NAME_TEMP
import com.thesunnahrevival.sunnahassistant.utilities.demoToDo
import com.thesunnahrevival.sunnahassistant.utilities.initialSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.TreeSet

@Database(
    entities = [
        ToDo::class, AppSettings::class, DailyHadith::class, Surah::class, Ayah::class, AyahTranslation::class,
        Footnote::class, Language::class, Line::class, Translation::class, AdhkaarChapter::class, AdhkaarItem::class,
        PageBookmark::class, AyahBookmark::class, AdhkaarItemBookmark::class, PinnedSurah::class, PinnedAdhkaarChapter::class
   ],
    version = 10,
    autoMigrations = [AutoMigration(from = 7, to = 8), AutoMigration(from = 8, to = 9)],
    exportSchema = true
    )
@TypeConverters(RoomTypeConverter::class)
abstract class SunnahAssistantDatabase : RoomDatabase() {
    abstract fun toDoDao(): ToDoDao

    abstract fun dailyHadithDao(): DailyHadithDao

    abstract fun surahDao(): SurahDao
    abstract fun ayahDao(): AyahDao

    abstract fun lineDao(): LineDao

    abstract fun ayahTranslationDao(): AyahTranslationDao

    abstract fun footnoteDao(): FootnoteDao

    abstract fun languageDao(): LanguageDao

    abstract fun translationDao(): TranslationDao

    abstract fun adhkaarChapterDao(): AdhkaarChapterDao

    abstract fun adhkaarItemDao(): AdhkaarItemDao

    abstract fun appSettingsDao(): AppSettingsDao

    abstract fun pageBookmarkDao(): PageBookmarkDao

    abstract fun ayahBookmarkDao(): AyahBookmarkDao
    abstract fun adhkaarItemBookmarkDao(): AdhkaarItemBookmarkDao
    abstract fun pinnedSurahDao(): PinnedSurahDao
    abstract fun pinnedAdhkaarChapterDao(): PinnedAdhkaarChapterDao

    fun closeDB() {
        INSTANCE?.close()
        INSTANCE = null
    }

    companion object {
        @Volatile
        private var INSTANCE: SunnahAssistantDatabase? = null

        private fun hasColumn(database: SupportSQLiteDatabase, tableName: String, columnName: String): Boolean {
            database.query("PRAGMA table_info(`$tableName`)").use { cursor ->
                val nameColumnIndex = cursor.getColumnIndex("name")
                if (nameColumnIndex == -1) {
                    return false
                }
                while (cursor.moveToNext()) {
                    if (cursor.getString(nameColumnIndex) == columnName) {
                        return true
                    }
                }
            }
            return false
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
                database.execSQL(
                    "ALTER TABLE app_settings ADD COLUMN generatePrayerRemindersAfter INTEGER DEFAULT 0 NOT NULL"
                )

                database.execSQL(
                    "ALTER TABLE app_settings ADD COLUMN enablePrayerTimeAlertsFor TEXT DEFAULT '' NOT NULL"
                )

                database.execSQL(
                    "ALTER TABLE app_settings ADD COLUMN appVersion TEXT DEFAULT '${BuildConfig.VERSION_NAME}' NOT NULL"
                )

                database.execSQL(
                    "ALTER TABLE app_settings ADD COLUMN appVersionCode INTEGER DEFAULT '${BuildConfig.VERSION_CODE - 1}' NOT NULL"
                )

                database.execSQL(
                    "ALTER TABLE app_settings ADD COLUMN prayerTimeOffsetsInMinutes TEXT DEFAULT '' NOT NULL"
                )

                database.execSQL("ALTER TABLE app_settings ADD COLUMN includeHijriDateInCalendar INTEGER DEFAULT 1 NOT NULL")

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
                    "ALTER TABLE reminders_table ADD COLUMN endsOnDate TEXT DEFAULT '' NOT NULL"
                )
                database.execSQL(
                    "ALTER TABLE reminders_table ADD COLUMN deletedDates TEXT DEFAULT '' NOT NULL"
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

        private val MIGRATION_6_7: Migration = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("DELETE FROM reminders_table WHERE id < 0 AND id > -1000")
            }
        }

        private val MIGRATION_9_10: Migration = object : Migration(9, 10) {
            override fun migrate(database: SupportSQLiteDatabase) {
                if (!hasColumn(database, "translations", "order")) {
                    database.execSQL("ALTER TABLE translations ADD COLUMN `order` INTEGER")
                }
                if (!hasColumn(database, "app_settings", "lastReadPage")) {
                    database.execSQL("ALTER TABLE app_settings ADD COLUMN `lastReadPage` INTEGER DEFAULT null")
                }
                if (!hasColumn(database, "app_settings", "arabicTextFontSize")) {
                    database.execSQL("ALTER TABLE app_settings ADD COLUMN `arabicTextFontSize` INTEGER NOT NULL DEFAULT 18")
                }
                if (!hasColumn(database, "app_settings", "translationTextFontSize")) {
                    database.execSQL("ALTER TABLE app_settings ADD COLUMN `translationTextFontSize` INTEGER NOT NULL DEFAULT 16")
                }
                if (!hasColumn(database, "app_settings", "footnoteTextFontSize")) {
                    database.execSQL("ALTER TABLE app_settings ADD COLUMN `footnoteTextFontSize` INTEGER NOT NULL DEFAULT 12")
                }

                // Create adhkaar_chapters table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS adhkaar_chapters (
                        id INTEGER PRIMARY KEY NOT NULL,
                        chapter_id INTEGER NOT NULL,
                        language TEXT NOT NULL,
                        chapter_name TEXT NOT NULL,
                        category_name TEXT NOT NULL
                    )
                """)

                // Create adhkaar_items table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS adhkaar_items (
                        id INTEGER PRIMARY KEY NOT NULL,
                        item_id INTEGER NOT NULL,
                        language TEXT NOT NULL,
                        item_translation TEXT NOT NULL,
                        chapter_id INTEGER NOT NULL,
                        reference TEXT NULL,
                        item_order INTEGER
                    )
                """)

                database.execSQL("DROP INDEX IF EXISTS `index_adhkaar_chapters_chapter_id`")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_adhkaar_chapters_chapter_id` ON `adhkaar_chapters` (`chapter_id`)")

                // Create page_bookmarks table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS page_bookmarks (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        page_number INTEGER NOT NULL,
                        created_at INTEGER NOT NULL,
                        note TEXT
                    )
                """)

                database.execSQL("DROP INDEX IF EXISTS `index_page_bookmarks_page_number`")
                
                // Create ayah_bookmarks table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS ayah_bookmarks (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        ayah_id INTEGER NOT NULL,
                        FOREIGN KEY(ayah_id) REFERENCES ayahs(id) ON DELETE CASCADE
                    )
                """)

                // Create adhkaar_item_bookmarks table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS adhkaar_item_bookmarks (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        adhkaar_item_id INTEGER NOT NULL
                )
                """)

                // Create pinned_surahs table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS pinned_surahs (
                        surah_id INTEGER PRIMARY KEY NOT NULL,
                        pin_order INTEGER NOT NULL,
                        FOREIGN KEY(surah_id) REFERENCES surahs(id) ON DELETE CASCADE
                    )
                """)

                // Create pinned_adhkaar_chapters table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS pinned_adhkaar_chapters (
                        adhkaar_chapter_id INTEGER PRIMARY KEY NOT NULL,
                        pin_order INTEGER NOT NULL
                    )
                """)

                if (!hasColumn(database, "app_settings", "fajrCustomTime")) {
                    database.execSQL("ALTER TABLE app_settings ADD COLUMN `fajrCustomTime` TEXT DEFAULT null")
                }
                if (!hasColumn(database, "app_settings", "dhuhrCustomTime")) {
                    database.execSQL("ALTER TABLE app_settings ADD COLUMN `dhuhrCustomTime` TEXT DEFAULT null")
                }
                if (!hasColumn(database, "app_settings", "asrCustomTime")) {
                    database.execSQL("ALTER TABLE app_settings ADD COLUMN `asrCustomTime` TEXT DEFAULT null")
                }
                if (!hasColumn(database, "app_settings", "maghribCustomTime")) {
                    database.execSQL("ALTER TABLE app_settings ADD COLUMN `maghribCustomTime` TEXT DEFAULT null")
                }
                if (!hasColumn(database, "app_settings", "ishaCustomTime")) {
                    database.execSQL("ALTER TABLE app_settings ADD COLUMN `ishaCustomTime` TEXT DEFAULT null")
                }
                if (!hasColumn(database, "reminders_table", "isAutomaticToDo")) {
                    database.execSQL("ALTER TABLE reminders_table ADD COLUMN `isAutomaticToDo` INTEGER DEFAULT 0 NOT NULL")
                }
                if (!hasColumn(database, "adhkaar_items", "item_order")) {
                    database.execSQL("ALTER TABLE adhkaar_items ADD COLUMN `item_order` INTEGER")
                }
                if (hasColumn(database, "adhkaar_items", "bookmarked")) {
                    database.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS `adhkaar_items_new` (
                            `id` INTEGER PRIMARY KEY NOT NULL,
                            `item_id` INTEGER NOT NULL,
                            `language` TEXT NOT NULL,
                            `item_translation` TEXT NOT NULL,
                            `chapter_id` INTEGER NOT NULL,
                            `reference` TEXT,
                            `item_order` INTEGER
                        )
                        """.trimIndent()
                    )
                    database.execSQL(
                        """
                        INSERT INTO `adhkaar_items_new` (
                            `id`, `item_id`, `language`, `item_translation`, `chapter_id`, `reference`, `item_order`
                        )
                        SELECT
                            `id`, `item_id`, `language`, `item_translation`, `chapter_id`, `reference`, `item_order`
                        FROM `adhkaar_items`
                        """.trimIndent()
                    )
                    database.execSQL("DROP TABLE `adhkaar_items`")
                    database.execSQL("ALTER TABLE `adhkaar_items_new` RENAME TO `adhkaar_items`")
                }
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `app_settings_new` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `formattedAddress` TEXT,
                        `latitude` REAL NOT NULL,
                        `longitude` REAL NOT NULL,
                        `method` INTEGER NOT NULL,
                        `asrCalculationMethod` INTEGER NOT NULL,
                        `isAutomatic` INTEGER NOT NULL,
                        `enablePrayerTimeAlertsFor` TEXT NOT NULL,
                        `prayerTimeOffsetsInMinutes` TEXT NOT NULL,
                        `month` INTEGER NOT NULL,
                        `hijriOffSet` INTEGER NOT NULL,
                        `isLightMode` INTEGER NOT NULL,
                        `isFirstLaunch` INTEGER NOT NULL,
                        `showNextReminderNotification` INTEGER NOT NULL,
                        `showOnBoardingTutorial` INTEGER NOT NULL,
                        `isDisplayHijriDate` INTEGER NOT NULL,
                        `savedSpinnerPosition` INTEGER NOT NULL,
                        `isExpandedLayout` INTEGER NOT NULL,
                        `notificationToneUri` TEXT,
                        `isVibrate` INTEGER NOT NULL,
                        `priority` INTEGER NOT NULL,
                        `latitudeAdjustmentMethod` INTEGER NOT NULL,
                        `isShowHijriDateWidget` INTEGER NOT NULL,
                        `isShowNextReminderWidget` INTEGER NOT NULL,
                        `isAfterUpdate` INTEGER NOT NULL,
                        `appVersionCode` INTEGER NOT NULL,
                        `appVersion` TEXT NOT NULL,
                        `categories` TEXT,
                        `language` TEXT NOT NULL,
                        `doNotDisturbMinutes` INTEGER NOT NULL,
                        `useReliableAlarms` INTEGER NOT NULL,
                        `numberOfLaunches` INTEGER NOT NULL,
                        `shareAnonymousUsageData` INTEGER NOT NULL,
                        `generatePrayerRemindersAfter` INTEGER NOT NULL,
                        `includeHijriDateInCalendar` INTEGER NOT NULL,
                        `hideDownloadFilePrompt` INTEGER NOT NULL DEFAULT 0,
                        `lastReadPage` INTEGER DEFAULT null,
                        `arabicTextFontSize` INTEGER NOT NULL DEFAULT 18,
                        `translationTextFontSize` INTEGER NOT NULL DEFAULT 16,
                        `footnoteTextFontSize` INTEGER NOT NULL DEFAULT 12,
                        `fajrCustomTime` TEXT DEFAULT null,
                        `dhuhrCustomTime` TEXT DEFAULT null,
                        `asrCustomTime` TEXT DEFAULT null,
                        `maghribCustomTime` TEXT DEFAULT null,
                        `ishaCustomTime` TEXT DEFAULT null
                    )
                    """.trimIndent()
                )
                database.execSQL(
                    """
                    INSERT INTO `app_settings_new` (
                        `id`, `formattedAddress`, `latitude`, `longitude`, `method`, `asrCalculationMethod`, `isAutomatic`,
                        `enablePrayerTimeAlertsFor`, `prayerTimeOffsetsInMinutes`, `month`, `hijriOffSet`, `isLightMode`,
                        `isFirstLaunch`, `showNextReminderNotification`, `showOnBoardingTutorial`, `isDisplayHijriDate`,
                        `savedSpinnerPosition`, `isExpandedLayout`, `notificationToneUri`, `isVibrate`, `priority`,
                        `latitudeAdjustmentMethod`, `isShowHijriDateWidget`, `isShowNextReminderWidget`, `isAfterUpdate`,
                        `appVersionCode`, `appVersion`, `categories`, `language`, `doNotDisturbMinutes`, `useReliableAlarms`,
                        `numberOfLaunches`, `shareAnonymousUsageData`, `generatePrayerRemindersAfter`,
                        `includeHijriDateInCalendar`, `hideDownloadFilePrompt`, `lastReadPage`, `arabicTextFontSize`,
                        `translationTextFontSize`, `footnoteTextFontSize`, `fajrCustomTime`, `dhuhrCustomTime`,
                        `asrCustomTime`, `maghribCustomTime`, `ishaCustomTime`
                    )
                    SELECT
                        `id`, `formattedAddress`, `latitude`, `longitude`, `method`, `asrCalculationMethod`, `isAutomatic`,
                        `enablePrayerTimeAlertsFor`, `prayerTimeOffsetsInMinutes`, `month`, `hijriOffSet`, `isLightMode`,
                        `isFirstLaunch`, `showNextReminderNotification`, `showOnBoardingTutorial`, `isDisplayHijriDate`,
                        `savedSpinnerPosition`, `isExpandedLayout`, `notificationToneUri`, `isVibrate`, `priority`,
                        `latitudeAdjustmentMethod`, `isShowHijriDateWidget`, `isShowNextReminderWidget`, `isAfterUpdate`,
                        `appVersionCode`, `appVersion`, `categories`, `language`, `doNotDisturbMinutes`, `useReliableAlarms`,
                        `numberOfLaunches`, `shareAnonymousUsageData`, `generatePrayerRemindersAfter`,
                        `includeHijriDateInCalendar`, COALESCE(`hideDownloadFilePrompt`, 0), `lastReadPage`,
                        COALESCE(`arabicTextFontSize`, 18), COALESCE(`translationTextFontSize`, 16),
                        COALESCE(`footnoteTextFontSize`, 12), `fajrCustomTime`, `dhuhrCustomTime`, `asrCustomTime`,
                        `maghribCustomTime`, `ishaCustomTime`
                    FROM `app_settings`
                    """.trimIndent()
                )
                database.execSQL("DROP TABLE `app_settings`")
                database.execSQL("ALTER TABLE `app_settings_new` RENAME TO `app_settings`")

            }
        }


        fun getInstance(context: Context): SunnahAssistantDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }
        }

        private fun buildDatabase(context: Context) = Room.databaseBuilder(
            context.applicationContext,
            SunnahAssistantDatabase::class.java, DB_NAME
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
                MIGRATION_5_6,
                MIGRATION_6_7,
                MIGRATION_9_10
            )
            .build()
    }
}
