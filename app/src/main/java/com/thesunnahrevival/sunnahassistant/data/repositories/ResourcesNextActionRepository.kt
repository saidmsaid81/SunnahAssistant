package com.thesunnahrevival.sunnahassistant.data.repositories

import android.content.Context
import com.thesunnahrevival.sunnahassistant.R
import com.thesunnahrevival.sunnahassistant.utilities.READING_SURATUL_KAHF_ID
import com.thesunnahrevival.sunnahassistant.utilities.READING_SURATUL_MULK_ID
import com.thesunnahrevival.sunnahassistant.utilities.TemplateToDos
import com.thesunnahrevival.sunnahassistant.utilities.getPrayerTimes
import java.time.DayOfWeek
import java.time.LocalDate

private const val SURATUL_KAHF_LAST_PAGE = 304
private const val SURATUL_MULK_LAST_PAGE = 564
private const val SURATUL_SAJDAH_LAST_PAGE = 417

class ResourcesNextActionRepository private constructor(
    private val applicationContext: Context
) {

    companion object {
        @Volatile
        private var instance: ResourcesNextActionRepository? = null

        fun getInstance(context: Context): ResourcesNextActionRepository {
            return instance ?: synchronized(this) {
                instance ?: ResourcesNextActionRepository(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }

    private val toDoRepository = SunnahAssistantRepository.getInstance(applicationContext)

    private val flagRepository = FlagRepository.getInstance(applicationContext)

    private val templateToDos = TemplateToDos().getTemplateToDos(applicationContext)

    suspend fun getNextActions(page: Int): NextActionsData {
        trackReadSurahs(page)
        val nextActions = buildList {
            when (page) {
                SURATUL_KAHF_LAST_PAGE -> populateSuratulKahfActions()
                SURATUL_MULK_LAST_PAGE -> populateSuratulMulkActions()
                SURATUL_SAJDAH_LAST_PAGE -> populateSuratulSajdahActions()
            }
        }

        val firstActionWithToDoId = nextActions.find { it.toDoId != null && templateToDos.containsKey(it.toDoId) }
        val templateToDoId = firstActionWithToDoId?.toDoId
        val templateToDo = templateToDoId?.let { templateToDos[it]?.second }

        return NextActionsData(
            predefinedReminderInfo = templateToDo?.predefinedToDoInfo ?: "",
            predefinedReminderLink = templateToDo?.predefinedToDoLink ?: "",
            predefinedReminderToDoId = templateToDoId,
            nextActions = nextActions
        )
    }

    private suspend fun trackReadSurahs(page: Int) {
        when (page) {
            in SURATUL_MULK_FIRST_PAGE..SURATUL_MULK_LAST_PAGE -> flagRepository.setFlag(TRACK_READ_SURAH_MULK_KEY, 1)
            in SURATUL_SAJDAH_FIRST_PAGE..SURATUL_SAJDAH_LAST_PAGE -> flagRepository.setFlag(TRACK_READ_SURAH_SAJDAH_KEY, 1)
        }
    }

    private suspend fun MutableList<NextAction>.populateSuratulKahfActions() {
        val now = LocalDate.now()
        val day = now.dayOfWeek

        if (day != DayOfWeek.THURSDAY && day != DayOfWeek.FRIDAY) return

        val prayerTimes = getPrayerTimes(
            context = applicationContext,
            toDoRepository = toDoRepository
        )

        val maghribOffset = prayerTimes.find { it.id.toString().endsWith(MAGHRIB_INDEX) }?.timeInMilliseconds
        val maghribTime = maghribOffset?.let { getMidnightTime() + it } ?: return

        val currentTime = System.currentTimeMillis()
        val isKahfTime = when (day) {
            DayOfWeek.THURSDAY -> currentTime >= maghribTime
            DayOfWeek.FRIDAY -> currentTime <= maghribTime
            else -> false
        }

        if (!isKahfTime) return

        val readingSuratulKahfToDoId = READING_SURATUL_KAHF_ID
        val readingSuratulKahfReminder = toDoRepository.getToDoById(readingSuratulKahfToDoId)

        if (readingSuratulKahfReminder == null || readingSuratulKahfReminder.isAutomaticToDo) {
            add(
                NextAction(
                    titleResId = R.string.weekly_friday_reminder,
                    subtitleResId = R.string.set_reminder_for_every_friday,
                    actionResId = R.string.set_weekly_reminder,
                    actionType = ActionType.NavigateToTodo,
                    toDoId = readingSuratulKahfToDoId
                )
            )
        }

        add(
            NextAction(
                toDoId = readingSuratulKahfToDoId,
                titleResId = R.string.remind_others,
                subtitleResId = R.string.remind_others_to_read_suratul_kahf,
                actionResId = R.string.remind_others,
                actionType = ActionType.ShareText,
                shareTextResId = R.string.suratul_kahf_reminder_message
            )
        )

        addMarkAsCompleteIfEligible(readingSuratulKahfReminder)
    }

    private suspend fun MutableList<NextAction>.populateSuratulMulkActions() {
        if (!isNightTime(applicationContext, toDoRepository)) return
        populateDailySurahActions(TRACK_READ_SURAH_SAJDAH_KEY, ::getSuratulSajdahNavigateAction, { getDailySuraReadingAction(R.string.suratul_mulk_daily_reminder) })
    }

    private suspend fun MutableList<NextAction>.populateSuratulSajdahActions() {
        if (!isNightTime(applicationContext, toDoRepository)) return
        populateDailySurahActions(TRACK_READ_SURAH_MULK_KEY, ::getSuratulMulkNavigateAction, { getDailySuraReadingAction(R.string.suratul_sajdah_daily_reminder) })
    }

    private suspend fun MutableList<NextAction>.populateDailySurahActions(
        flagKey: String,
        firstAction: () -> NextAction,
        secondAction: () -> NextAction
    ) {
        val companionSurahRead = flagRepository.getIntFlag(flagKey) != null

        if (!companionSurahRead) {
            add(firstAction())
        }

        if (flagRepository.getIntFlag(TRACK_READ_SLEEPING_ADHKAAR_KEY) == null) {
            add(getSleepingAdhkaarNavigateAction())
        }

        val suratulMulkToDoId = READING_SURATUL_MULK_ID
        val readingSuratulMulkReminder = toDoRepository.getToDoById(suratulMulkToDoId)

        if (readingSuratulMulkReminder == null || readingSuratulMulkReminder.isAutomaticToDo) {
            add(secondAction())
        }

        add(
            NextAction(
                toDoId = suratulMulkToDoId,
                titleResId = R.string.remind_others,
                subtitleResId = R.string.remind_others_to_read_suratul_mulk,
                actionResId = R.string.remind_others,
                actionType = ActionType.ShareText,
                shareTextResId = R.string.suratul_mulk_and_sajdah_reminder_message
            )
        )

        if (companionSurahRead) {
            addMarkAsCompleteIfEligible(readingSuratulMulkReminder)
        }
    }

    private fun getDailySuraReadingAction(titleResId: Int): NextAction = NextAction(
        titleResId = titleResId,
        subtitleResId = R.string.set_daily_reminder,
        actionResId = R.string.set_daily_reminder,
        actionType = ActionType.NavigateToTodo,
        toDoId = READING_SURATUL_MULK_ID
    )

    data class NextActionsData(
        val predefinedReminderInfo: String = "",
        val predefinedReminderLink: String = "",
        val predefinedReminderToDoId: Int? = null,
        val nextActions: List<NextAction> = listOf()
    )

    data class NextAction(
        val titleResId: Int,
        val subtitleResId: Int,
        val actionResId: Int,
        val actionType: ActionType,
        val toDoId: Int? = null,
        val shareTextResId: Int? = null,
        val surahPageNumber: Int? = null,
        val adhkaarChapterNumber: Int? = null
    )

    sealed class ActionType {
        data object NavigateToTodo : ActionType()
        data object ShareText : ActionType()
        data object NavigateToSurah : ActionType()
        data object NavigateToAdhkaar : ActionType()
        data object MarkAsComplete : ActionType()
    }

    suspend fun markToDoAsComplete(toDoId: Int) {
        toDoRepository.markAsComplete(toDoId)
    }
}
