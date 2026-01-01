package com.thesunnahrevival.sunnahassistant.data.repositories

import android.content.Context
import com.thesunnahrevival.sunnahassistant.R
import com.thesunnahrevival.sunnahassistant.data.model.dto.PrayerTimeCalculator
import com.thesunnahrevival.sunnahassistant.utilities.EXERCISE_ID
import com.thesunnahrevival.sunnahassistant.utilities.FASTING_MONDAYS_THURSDAYS_ID
import com.thesunnahrevival.sunnahassistant.utilities.PRAYING_DHUHA_ID
import com.thesunnahrevival.sunnahassistant.utilities.READING_EVENING_ADHKAAR_ID
import com.thesunnahrevival.sunnahassistant.utilities.READING_ID
import com.thesunnahrevival.sunnahassistant.utilities.READING_MORNING_ADHKAAR_ID
import com.thesunnahrevival.sunnahassistant.utilities.READING_QURAN_ID
import com.thesunnahrevival.sunnahassistant.utilities.READING_SLEEPING_ADHKAAR_ID
import com.thesunnahrevival.sunnahassistant.utilities.READING_SURATUL_KAHF_ID
import com.thesunnahrevival.sunnahassistant.utilities.READING_SURATUL_MULK_ID
import com.thesunnahrevival.sunnahassistant.utilities.TAHAJJUD_ID
import com.thesunnahrevival.sunnahassistant.utilities.TemplateToDos
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.withContext
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.temporal.ChronoUnit

const val NUDGE_DISMISSED_DATE_KEY = "nudge-dismissed-date"

class ToDoNudgeRepository private constructor(
    private val applicationContext: Context
) {

    companion object {
        @Volatile
        private var instance: ToDoNudgeRepository? = null

        fun getInstance(context: Context): ToDoNudgeRepository {
            return instance ?: synchronized(this) {
                instance ?: ToDoNudgeRepository(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }

    private val toDoRepository = SunnahAssistantRepository.getInstance(applicationContext)
    private val flagRepository = FlagRepository.getInstance(applicationContext)
    private val templateToDos = TemplateToDos().getTemplateToDos(applicationContext)

    fun getNudge(
        isPrayerAlertsEnabled: Boolean,
        hasLocation: Boolean
    ): Flow<Nudge?> {
        val templateToDoIdsFlow = toDoRepository.getTemplateToDoIdsFlow()
        val dismissedDateFlow = flagRepository.getLongFlagFlow(NUDGE_DISMISSED_DATE_KEY)

        return combine(templateToDoIdsFlow, dismissedDateFlow) { existingTemplateIds, dismissedDate ->
            val isDismissedToday = dismissedDate == LocalDate.now().toEpochDay()
            if (isDismissedToday) {
                null
            } else {
                buildNudge(isPrayerAlertsEnabled, hasLocation, existingTemplateIds)
            }
        }
    }

    private suspend fun buildNudge(
        isPrayerAlertsEnabled: Boolean,
        hasLocation: Boolean,
        existingTemplateIds: List<Int>
    ): Nudge? {
        if (!isPrayerAlertsEnabled || !hasLocation) {
            return Nudge(
                textResId = R.string.nudge_enable_prayer_alerts,
                actionType = NudgeActionType.NavigateToPrayerSettings
            )
        }

        val missingTemplates = templateToDos.filter { !existingTemplateIds.contains(it.key) }
        val appropriateTemplates = missingTemplates.filter {
            withContext(Dispatchers.IO) {
                isAppropriateTimeForTemplate(it.key)
            }
        }

        if (appropriateTemplates.isNotEmpty()) {
            val randomMissing = appropriateTemplates.entries.random()
            val templateName = randomMissing.value.second.name ?: return null
            return Nudge(
                textResId = R.string.nudge_add_template_todo,
                textArg = templateName,
                actionType = NudgeActionType.NavigateToToDoDetails,
                toDoId = randomMissing.key
            )
        }

        return null
    }

    private suspend fun isAppropriateTimeForTemplate(templateId: Int): Boolean {
        return when (templateId) {
            PRAYING_DHUHA_ID -> isDhuhaTime()
            READING_MORNING_ADHKAAR_ID -> isFajrToSunrise()
            READING_EVENING_ADHKAAR_ID -> isAsrToIsha()
            TAHAJJUD_ID, READING_SURATUL_MULK_ID, READING_SLEEPING_ADHKAAR_ID -> isIshaToFajr()
            READING_SURATUL_KAHF_ID -> isThursdayNightToFridayMaghrib()
            FASTING_MONDAYS_THURSDAYS_ID -> isSundayOrWednesday()
            READING_QURAN_ID, READING_ID, EXERCISE_ID -> true
            else -> false
        }
    }

    private suspend fun isDhuhaTime(): Boolean {
        val sunrise = getSunrise() ?: return false

        val prayerTimes = getPrayerTimes(applicationContext, toDoRepository)
        val dhuhrOffset = prayerTimes.find { it.id.toString().endsWith(DHUHR_INDEX) }?.timeInMilliseconds
        val dhuhrTime = dhuhrOffset?.let { getMidnightTime() + it } ?: return false

        val currentTime = System.currentTimeMillis()
        val twentyMinutes = (20 * 60 * 1000)
        return currentTime in (sunrise + twentyMinutes)..(dhuhrTime - twentyMinutes)
    }

    private suspend fun isFajrToSunrise(): Boolean {
        val sunrise = getSunrise() ?: return false

        val prayerTimes = getPrayerTimes(applicationContext, toDoRepository)
        val fajrOffset = prayerTimes.find { it.id.toString().endsWith(FAJR_INDEX) }?.timeInMilliseconds
        val fajrTime = fajrOffset?.let { getMidnightTime() + it } ?: return false

        val currentTime = System.currentTimeMillis()
        return currentTime in fajrTime..sunrise
    }

    private fun isAsrToIsha(): Boolean {
        val prayerTimes = getPrayerTimes(applicationContext, toDoRepository)

        val asrOffset = prayerTimes.find { it.id.toString().endsWith(ASR_INDEX) }?.timeInMilliseconds
        val asrTime = asrOffset?.let { getMidnightTime() + it } ?: return false

        val ishaOffset = prayerTimes.find { it.id.toString().endsWith(ISHA_INDEX) }?.timeInMilliseconds
        val ishaTime = ishaOffset?.let { getMidnightTime() + it } ?: return false

        val currentTime = System.currentTimeMillis()
        return currentTime in asrTime..ishaTime
    }

    private fun isIshaToFajr(): Boolean {
        val prayerTimes = getPrayerTimes(applicationContext, toDoRepository)

        val ishaOffset = prayerTimes.find { it.id.toString().endsWith(ISHA_INDEX) }?.timeInMilliseconds
        val ishaTime = ishaOffset?.let { getMidnightTime() + it } ?: return false

        val fajrOffset = prayerTimes.find { it.id.toString().endsWith(FAJR_INDEX) }?.timeInMilliseconds
        val fajrTime = fajrOffset?.let { getMidnightTime() + it } ?: return false

        val currentTime = System.currentTimeMillis()
        return currentTime !in (fajrTime + 1)..<ishaTime
    }

    private fun isThursdayNightToFridayMaghrib(): Boolean {
        val now = LocalDate.now()
        val day = now.dayOfWeek

        if (day != DayOfWeek.THURSDAY && day != DayOfWeek.FRIDAY) {
            return false
        }

        val prayerTimes = getPrayerTimes(applicationContext, toDoRepository)
        val maghribOffset = prayerTimes.find { it.id.toString().endsWith(MAGHRIB_INDEX) }?.timeInMilliseconds
        val maghribTime = maghribOffset?.let { getMidnightTime() + it } ?: return false

        val currentTime = System.currentTimeMillis()
        return when (day) {
            DayOfWeek.THURSDAY -> currentTime >= maghribTime
            DayOfWeek.FRIDAY -> currentTime <= maghribTime
            else -> false
        }
    }

    private fun isSundayOrWednesday(): Boolean {
        val day = LocalDate.now().dayOfWeek
        return day == DayOfWeek.SUNDAY || day == DayOfWeek.WEDNESDAY
    }

    private suspend fun getPrayerTimesCalculator(): PrayerTimeCalculator? {
        val settings = toDoRepository.getAppSettingsValue() ?: return null
        return PrayerTimeCalculator(
            settings = settings,
            prayerNames = applicationContext.resources.getStringArray(R.array.categories),
            categoryName = applicationContext.resources.getStringArray(R.array.categories)[2]
        )
    }

    private fun getMidnightTime(): Long =
        Instant.now()
            .truncatedTo(ChronoUnit.DAYS)
            .toEpochMilli()

    private suspend fun getSunrise(): Long? {
        val prayerTimeCalculator = getPrayerTimesCalculator() ?: return null

        val now = LocalDate.now()
        return prayerTimeCalculator.getSunrise(
            day = now.dayOfMonth,
            month = now.month.ordinal,
            year = now.year
        )
    }

    suspend fun dismissNudgesForToday() {
        flagRepository.setFlag(NUDGE_DISMISSED_DATE_KEY, LocalDate.now().toEpochDay())
    }

    data class Nudge(
        val textResId: Int,
        val textArg: String? = null,
        val actionType: NudgeActionType,
        val toDoId: Int? = null
    )

    sealed class NudgeActionType {
        data object NavigateToPrayerSettings : NudgeActionType()
        data object NavigateToToDoDetails : NudgeActionType()
    }
}
