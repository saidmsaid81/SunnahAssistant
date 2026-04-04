package com.thesunnahrevival.sunnahassistant.data.repositories

import android.content.Context
import com.thesunnahrevival.sunnahassistant.R
import com.thesunnahrevival.sunnahassistant.data.model.dto.PrayerTimeCalculator
import com.thesunnahrevival.sunnahassistant.data.repositories.ResourcesNextActionRepository.ActionType
import com.thesunnahrevival.sunnahassistant.data.repositories.ResourcesNextActionRepository.NextAction
import com.thesunnahrevival.sunnahassistant.data.repositories.ResourcesNextActionRepository.NextActionsData
import com.thesunnahrevival.sunnahassistant.utilities.BEFORE_SLEEPING_ADHKAAR_CHAPTER_ID
import com.thesunnahrevival.sunnahassistant.utilities.EVENING_ADHKAAR_CHAPTER_ID
import com.thesunnahrevival.sunnahassistant.utilities.MORNING_ADHKAAR_CHAPTER_ID
import com.thesunnahrevival.sunnahassistant.utilities.READING_EVENING_ADHKAAR_ID
import com.thesunnahrevival.sunnahassistant.utilities.READING_MORNING_ADHKAAR_ID
import com.thesunnahrevival.sunnahassistant.utilities.READING_SLEEPING_ADHKAAR_ID
import com.thesunnahrevival.sunnahassistant.utilities.TemplateToDos
import com.thesunnahrevival.sunnahassistant.utilities.getPrayerTimes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.time.LocalDate

class AdhkaarResourcesNextActionRepository private constructor(
    private val applicationContext: Context
) {
    companion object {
        @Volatile
        private var instance: AdhkaarResourcesNextActionRepository? = null

        fun getInstance(context: Context): AdhkaarResourcesNextActionRepository {
            return instance ?: synchronized(this) {
                instance ?: AdhkaarResourcesNextActionRepository(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }

    private val toDoRepository = SunnahAssistantRepository.getInstance(applicationContext)

    private val flagRepository = FlagRepository.getInstance(applicationContext)

    private val templateToDos = TemplateToDos().getTemplateToDos(applicationContext)

    fun getAdhkaarNextActions(chapterId: Int): Flow<NextActionsData> = flow {
        trackReadAdhkaar(chapterId)
        val nextActions = buildList {
            when (chapterId) {
                MORNING_ADHKAAR_CHAPTER_ID -> populateMorningAdhkaarActions()
                EVENING_ADHKAAR_CHAPTER_ID -> populateEveningAdhkaarActions()
                BEFORE_SLEEPING_ADHKAAR_CHAPTER_ID -> populateBeforeSleepingAdhkaarActions()
            }
        }

        val firstActionWithToDoId = nextActions.find { it.toDoId != null && templateToDos.containsKey(it.toDoId) }
        val templateToDoId = firstActionWithToDoId?.toDoId
        val templateToDo = templateToDoId?.let { templateToDos[it]?.second }

        emit(NextActionsData(
            predefinedReminderInfo = templateToDo?.predefinedToDoInfo ?: "",
            predefinedReminderLink = templateToDo?.predefinedToDoLink ?: "",
            predefinedReminderToDoId = templateToDoId,
            nextActions = nextActions
        ))
    }.flowOn(Dispatchers.IO)

    private suspend fun MutableList<NextAction>.populateEveningAdhkaarActions() {
        val prayerTimes = getPrayerTimes(
            context = applicationContext,
            toDoRepository = toDoRepository
        )

        val asrOffset = prayerTimes.find { it.id.toString().endsWith(ASR_INDEX) }?.timeInMilliseconds
        val asrTime = asrOffset?.let { getMidnightTime() + it } ?: return

        val ishaOffset = prayerTimes.find { it.id.toString().endsWith(ISHA_INDEX) }?.timeInMilliseconds
        val ishaTime = ishaOffset?.let { getMidnightTime() + it } ?: return

        val currentTime = System.currentTimeMillis()

        if (currentTime !in asrTime..ishaTime) return

        val readingEveningAdhkaarId = READING_EVENING_ADHKAAR_ID
        val eveningAdhkaarReminder = toDoRepository.getToDoById(readingEveningAdhkaarId)

        if (eveningAdhkaarReminder == null || eveningAdhkaarReminder.isAutomaticToDo) {
            add(
                NextAction(
                    titleResId = R.string.evening_adhkaar_daily_reminder,
                    subtitleResId = R.string.set_daily_reminder,
                    actionResId = R.string.set_daily_reminder,
                    actionType = ActionType.NavigateToTodo
                )
            )
        }

        add(
            NextAction(
                toDoId = readingEveningAdhkaarId,
                titleResId = R.string.remind_others,
                subtitleResId = R.string.remind_others_to_read_evening_adhkaar,
                actionResId = R.string.remind_others,
                actionType = ActionType.ShareText,
                shareTextResId = R.string.morning_evening_adhkaar_reminder_message
            )
        )

        addMarkAsCompleteIfEligible(eveningAdhkaarReminder)
    }

    private suspend fun MutableList<NextAction>.populateMorningAdhkaarActions() {
        val prayerTimeCalculator = getPrayerTimesCalculator() ?: return

        val now = LocalDate.now()
        val sunrise = prayerTimeCalculator.getSunrise(
            day = now.dayOfMonth,
            month = now.month.ordinal, // Repo expects 0-indexed months
            year = now.year
        )

        val prayerTimes = getPrayerTimes(
            context = applicationContext,
            toDoRepository = toDoRepository
        )
        val fajrOffset = prayerTimes.find { it.id.toString().endsWith(FAJR_INDEX) }?.timeInMilliseconds
        val fajrTime = fajrOffset?.let { getMidnightTime() + it } ?: return

        val currentTime = System.currentTimeMillis()

        if (currentTime !in fajrTime..sunrise) return

        val readingMorningAdhkaarId = READING_MORNING_ADHKAAR_ID
        val isMorningAdhkaar = toDoRepository.getToDoById(readingMorningAdhkaarId)

        if (isMorningAdhkaar == null || isMorningAdhkaar.isAutomaticToDo) {
            add(
                NextAction(
                    titleResId = R.string.morning_adhkaar_daily_reminder,
                    subtitleResId = R.string.set_daily_reminder,
                    actionResId = R.string.set_daily_reminder,
                    actionType = ActionType.NavigateToTodo,
                    toDoId = readingMorningAdhkaarId
                )
            )
        }

        add(
            NextAction(
                titleResId = R.string.remind_others,
                subtitleResId = R.string.remind_others_to_read_morning_adhkaar,
                actionResId = R.string.remind_others,
                actionType = ActionType.ShareText,
                toDoId = readingMorningAdhkaarId,
                shareTextResId = R.string.morning_evening_adhkaar_reminder_message
            )
        )

        addMarkAsCompleteIfEligible(isMorningAdhkaar)
    }

    private suspend fun trackReadAdhkaar(chapterId: Int) {
        if (chapterId == BEFORE_SLEEPING_ADHKAAR_CHAPTER_ID) {
            flagRepository.setFlag(TRACK_READ_SLEEPING_ADHKAAR_KEY, 1)
        }
    }

    private suspend fun MutableList<NextAction>.populateBeforeSleepingAdhkaarActions() {
        if (!isNightTime(applicationContext, toDoRepository)) return

        if (flagRepository.getIntFlag(TRACK_READ_SURAH_MULK_KEY) == null) {
            add(getSuratulMulkNavigateAction())
        }

        if (flagRepository.getIntFlag(TRACK_READ_SURAH_SAJDAH_KEY) == null) {
            add(getSuratulSajdahNavigateAction())
        }

        val sleepingAdhkaarToDoId = READING_SLEEPING_ADHKAAR_ID
        val sleepingAdhkaarReminder = toDoRepository.getToDoById(sleepingAdhkaarToDoId)

        if (sleepingAdhkaarReminder == null || sleepingAdhkaarReminder.isAutomaticToDo) {
            add(
                NextAction(
                    titleResId = R.string.sleeping_adhkaar_daily_reminder,
                    subtitleResId = R.string.set_daily_reminder,
                    actionResId = R.string.set_daily_reminder,
                    actionType = ActionType.NavigateToTodo,
                    toDoId = sleepingAdhkaarToDoId
                )
            )
        }

        addMarkAsCompleteIfEligible(sleepingAdhkaarReminder)
    }

    private suspend fun getPrayerTimesCalculator(): PrayerTimeCalculator? {
        val settings = toDoRepository.getAppSettingsValue() ?: return null
        return PrayerTimeCalculator(
            settings = settings,
            prayerNames = applicationContext.resources.getStringArray(R.array.categories),
            categoryName = applicationContext.resources.getStringArray(R.array.categories)[2]
        )
    }
}
