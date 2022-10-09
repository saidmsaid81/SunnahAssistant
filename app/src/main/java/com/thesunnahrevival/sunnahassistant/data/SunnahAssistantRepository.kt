package com.thesunnahrevival.sunnahassistant.data

import android.content.Context
import androidx.paging.PagingSource
import com.batoulapps.adhan.CalculationMethod
import com.batoulapps.adhan.Madhab
import com.thesunnahrevival.sunnahassistant.ApiKey
import com.thesunnahrevival.sunnahassistant.R
import com.thesunnahrevival.sunnahassistant.data.local.SunnahAssistantDatabase
import com.thesunnahrevival.sunnahassistant.data.local.ToDoDao
import com.thesunnahrevival.sunnahassistant.data.model.*
import com.thesunnahrevival.sunnahassistant.data.remote.GeocodingInterface
import com.thesunnahrevival.sunnahassistant.data.remote.SunnahAssistantApiInterface
import com.thesunnahrevival.sunnahassistant.utilities.generateLocalDatefromDate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.LocalDate
import java.util.*

class SunnahAssistantRepository private constructor(private val applicationContext: Context) {
    private val mToDoDao: ToDoDao
        get() = SunnahAssistantDatabase.getInstance(applicationContext).toDoDao()

    private val mGeocodingRestApi: GeocodingInterface
    private val mSunnahAssistantApi: SunnahAssistantApiInterface
    private val prayerNames: Array<String>
    private val prayerCategory: String

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://maps.googleapis.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        mGeocodingRestApi = retrofit.create(GeocodingInterface::class.java)

        mSunnahAssistantApi = Retrofit.Builder()
            .baseUrl("https://us-central1-sunnah-assistant.cloudfunctions.net/")
            .build()
            .create(SunnahAssistantApiInterface::class.java)

        prayerNames = applicationContext.resources.getStringArray(R.array.prayer_names)
        prayerCategory = applicationContext.resources.getStringArray(R.array.categories)[2]

    }

    suspend fun insertToDo(toDo: ToDo) = mToDoDao.insertToDo(toDo)

    //TODO use insert reminder
    suspend fun updateToDo(id: Int, name: String?, info: String?, category: String?) {
        if (name != null && info != null && category != null)
            mToDoDao.updateToDo(id, name, info, category)
    }

    suspend fun deleteToDO(toDo: ToDo) = mToDoDao.deleteToDo(toDo)

    fun thereToDosOnDay(
        excludeCategory: String,
        dayOfWeek: String,
        dayOfMonth: Int,
        month: Int,
        year: Int
    ): Boolean {
        return mToDoDao.thereToDosOnDay(
            excludeCategory, dayOfWeek, dayOfMonth, month, year
        )
    }

    fun getToDo(id: Int) = mToDoDao.getToDo(id)

    fun getTemplateToDoIds() = mToDoDao.getTemplateToDoIds()

    fun getIncompleteToDosOnDay(date: Date, category: String): PagingSource<Int, ToDo> {
        CoroutineScope(Dispatchers.IO).launch {
            generatePrayerTimes(date)
        }

        val (day, month, year, dayOfWeek) = getToDoDate(date)
        return mToDoDao.getIncompleteToDosOnDay(
            dayOfWeek.toString(),
            day,
            month,
            year,
            category,
            generateLocalDatefromDate(date).toString()
        )
    }

    fun getCompleteToDosOnDay(date: Date, category: String): PagingSource<Int, ToDo> {
        CoroutineScope(Dispatchers.IO).launch {
            generatePrayerTimes(date)
        }

        val (day, month, year, dayOfWeek) = getToDoDate(date)
        return mToDoDao.getCompleteToDosOnDay(
            dayOfWeek.toString(),
            day,
            month,
            year,
            category,
            generateLocalDatefromDate(date).toString()
        )
    }

    fun getToDosOnDayValue(
        numberOfTheWeekDay: String,
        day: Int,
        month: Int,
        year: Int
    ): List<ToDo> {
        generatePrayerTimes(GregorianCalendar(year, month, day).time)
        return mToDoDao.getToDosOnDayValue(numberOfTheWeekDay, day, month, year)
    }

    suspend fun getNextTimeForToDosForDay(
        offsetFromMidnight: Long,
        numberOfTheWeekDay: String,
        day: Int,
        month: Int,
        year: Int
    ): Long? {
        generatePrayerTimes(GregorianCalendar(year, month, day).time)
        return mToDoDao.getNextTimeForToDoForDay(
            offsetFromMidnight,
            numberOfTheWeekDay,
            day,
            month,
            year
        )
    }

    suspend fun getNextScheduledToDosForDay(
        timeForToDos: Long,
        numberOfTheWeekDay: String,
        day: Int,
        month: Int,
        year: Int
    ) = mToDoDao.getNextScheduledToDosForDay(
        timeForToDos,
        numberOfTheWeekDay,
        day,
        month,
        year
    )


    suspend fun updatePrayerDetails(oldPrayerDetails: ToDo, newPrayerDetails: ToDo) {
        mToDoDao.updatePrayerTimeDetails(
            newPrayerDetails.additionalInfo,
            newPrayerDetails.offsetInMinutes,
            newPrayerDetails.isReminderEnabled,
            newPrayerDetails.completedDates,
            oldPrayerDetails.id
        )
    }

    suspend fun deletePrayerTimesData() =
        mToDoDao.deleteAllPrayerTimes()

    fun getPrayerTimesValue(day: Int, month: Int, year: Int, categoryName: String): List<ToDo> {
        generatePrayerTimes(GregorianCalendar(year, month, day).time)
        return mToDoDao.getPrayerTimesValue(day, month, year, categoryName)
    }

    private fun generatePrayerTimes(date: Date) {
        val settings = mToDoDao.getAppSettingsValue()
        if (settings != null && settings.isAutomaticPrayerAlertsEnabled && settings.formattedAddress?.isNotBlank() == true) {
            val (day, month, year) = getToDoDate(date)
            val therePrayerToDosOnDay =
                mToDoDao.therePrayerToDosOnDay(prayerCategory, "$day$month$year")
            if (!therePrayerToDosOnDay && date.after(settings.generatePrayerToDosAfter)) {
                val prayerTimes =
                    getPrayerRemindersList(
                        day,
                        month,
                        year,
                        settings.latitude,
                        settings.longitude,
                        settings.calculationMethod,
                        settings.asrCalculationMethod,
                        settings.latitudeAdjustmentMethod,
                        prayerNames,
                        prayerCategory,
                        settings.enablePrayerTimeAlertsFor,
                        settings.prayerTimeOffsetsInMinutes
                    )
                mToDoDao.insertToDosList(prayerTimes)
            }
        }
    }

    suspend fun updatePrayerReminders(prayerNames: Array<String>, prayerCategory: String) {
        val settings = mToDoDao.getAppSettingsValue()
        if (settings != null && settings.isAutomaticPrayerAlertsEnabled) {
            val todayDate = LocalDate.now()
            val upcomingPrayerDatesList = mToDoDao.getUpcomingPrayerDates(
                todayDate.dayOfMonth, todayDate.month.ordinal, todayDate.year
            )

            for (upcomingPrayerDate in upcomingPrayerDatesList) {
                val prayerRemindersList = getPrayerRemindersList(
                    upcomingPrayerDate.day,
                    upcomingPrayerDate.month,
                    upcomingPrayerDate.year,
                    settings.latitude,
                    settings.longitude,
                    settings.calculationMethod,
                    settings.asrCalculationMethod,
                    settings.latitudeAdjustmentMethod,
                    prayerNames,
                    prayerCategory,
                    settings.enablePrayerTimeAlertsFor,
                    settings.prayerTimeOffsetsInMinutes
                )
                for (prayerReminder in prayerRemindersList) {
                    mToDoDao.updateGeneratedPrayerTime(
                        prayerReminder.id,
                        prayerReminder.timeInSeconds,
                        prayerReminder.isReminderEnabled,
                        prayerReminder.offsetInMinutes
                    )
                }
            }
        }
    }

    suspend fun updatePrayerNames(oldPrayerNames: Array<String>, newPrayerNames: Array<String>) {
        if (oldPrayerNames.size == newPrayerNames.size) {
            oldPrayerNames.forEachIndexed { index, oldPrayerName ->
                mToDoDao.updatePrayerNames(oldPrayerName, newPrayerNames[index])
            }
        }
    }

    private fun getPrayerRemindersList(
        day: Int,
        month: Int,
        year: Int,
        latitude: Float,
        longitude: Float,
        calculationMethod: CalculationMethod,
        asrCalculationMethod: Madhab,
        latitudeAdjustmentMethod: Int,
        prayerNames: Array<String>,
        prayerCategory: String,
        enablePrayerTimeAlertsFor: BooleanArray,
        offsetInMinutesForPrayer: IntArray
    ): ArrayList<ToDo> {

        val prayerTimeCalculator = PrayerTimeCalculator(
            latitude.toDouble(),
            longitude.toDouble(),
            calculationMethod,
            asrCalculationMethod,
            latitudeAdjustmentMethod,
            prayerNames,
            prayerCategory
        )
        return prayerTimeCalculator.getPrayerTimeToDos(
            day,
            month,
            year,
            enablePrayerTimeAlertsFor,
            offsetInMinutesForPrayer
        )
    }

    private fun getToDoDate(date: Date): ToDoDate {
        val calendar = GregorianCalendar()
        calendar.time = date
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val month = calendar.get(Calendar.MONTH)
        val year = calendar.get(Calendar.YEAR)
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK).toString()
        return ToDoDate(day, month, year, dayOfWeek)
    }

    suspend fun updateCategory(deletedCategory: String, newCategory: String) {
        mToDoDao.updateCategory(deletedCategory, newCategory)
    }

    suspend fun updateDeletedCategories(
        deletedCategories: ArrayList<String>,
        uncategorized: String
    ) {
        for (deletedCategory in deletedCategories) {
            mToDoDao.updateCategory(
                deletedCategory,
                uncategorized
            )
        }
    }


    suspend fun updateAppSettings(settings: AppSettings) = mToDoDao.updateAppSettings(settings)

    fun getAppSettings() = mToDoDao.getAppSettings()

    suspend fun getAppSettingsValue(): AppSettings? {
        return mToDoDao.getAppSettingsValue()
    }

    suspend fun updateWidgetSettings(
        isShowHijriDateWidget: Boolean,
        isDisplayNextToDo: Boolean
    ) =
        mToDoDao.updateWidgetSettings(isShowHijriDateWidget, isDisplayNextToDo)

    suspend fun getGeocodingData(address: String, locale: String): GeocodingData? {
        return mGeocodingRestApi.getGeocodingData(address, ApiKey.API_KEY, locale)
    }

    suspend fun reportGeocodingServerError(status: String) {
        mSunnahAssistantApi.reportGeocodingError(status)
    }

    fun closeDB() = SunnahAssistantDatabase.getInstance(applicationContext).close()

    companion object {
        @Volatile
        private var INSTANCE: SunnahAssistantRepository? = null

        @JvmStatic
        fun getInstance(context: Context): SunnahAssistantRepository =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildRepository(context).also { INSTANCE = it }
            }

        private fun buildRepository(context: Context) =
            SunnahAssistantRepository(context.applicationContext)
    }
}