package com.thesunnahrevival.sunnahassistant.data.repositories

import android.content.Context
import com.thesunnahrevival.sunnahassistant.R
import com.thesunnahrevival.sunnahassistant.utilities.READING_SURATUL_KAHF_ID
import com.thesunnahrevival.sunnahassistant.utilities.READING_SURATUL_MULK_ID
import com.thesunnahrevival.sunnahassistant.utilities.TemplateToDos
import com.thesunnahrevival.sunnahassistant.utilities.getPrayerTimes
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.temporal.ChronoUnit

const val FAJR_INDEX = '0'
const val DHUHR_INDEX = '1'
const val ASR_INDEX = '2'
const val MAGHRIB_INDEX = '3'
const val ISHA_INDEX = '4'

const val TRACK_READ_SURAH_PREFIX = "track-read-surah"

private fun getTrackReadSurahKey(date: LocalDate = LocalDate.now()): String =
    "$TRACK_READ_SURAH_PREFIX-$date"

private val TRACK_READ_SURAH_MULK_KEY: String
    get() = "${getTrackReadSurahKey()}-mulk"

private val TRACK_READ_SURAH_SAJDAH_KEY: String
    get() = "${getTrackReadSurahKey()}-sajdah"

private const val SURATUL_KAHF_LAST_PAGE = 304

private const val SURATUL_MULK_LAST_PAGE = 564

private const val SURATUL_SAJDAH_LAST_PAGE = 417

private const val SURATUL_MULK_FIRST_PAGE = 562

private const val SURATUL_SAJDAH_FIRST_PAGE = 415

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
        val templateToDo = firstActionWithToDoId?.toDoId?.let { templateToDos[it]?.second }

        return NextActionsData(
            predefinedReminderInfo = templateToDo?.predefinedToDoInfo ?: "",
            predefinedReminderLink = templateToDo?.predefinedToDoLink ?: "",
            nextActions = nextActions
        )
    }

    private suspend fun trackReadSurahs(page: Int) {
        when(page) {
            in SURATUL_MULK_FIRST_PAGE..SURATUL_MULK_LAST_PAGE -> flagRepository.setFlag(TRACK_READ_SURAH_MULK_KEY, 1)
            in SURATUL_SAJDAH_FIRST_PAGE..SURATUL_SAJDAH_LAST_PAGE -> flagRepository.setFlag(TRACK_READ_SURAH_SAJDAH_KEY , 1)
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

        val readingSuratulKahfReminder = toDoRepository.getToDoById(READING_SURATUL_KAHF_ID)

        if (readingSuratulKahfReminder == null || readingSuratulKahfReminder.isAutomaticToDo) {
            add(
                NextAction(
                    titleResId = R.string.weekly_friday_reminder,
                    subtitleResId = R.string.set_reminder_for_every_friday,
                    actionResId = R.string.set_weekly_reminder,
                    actionType = ActionType.NavigateToTodo,
                    toDoId = READING_SURATUL_KAHF_ID
                )
            )
        }

        add(
            NextAction(
                titleResId = R.string.remind_others,
                subtitleResId = R.string.remind_others_to_read_suratul_kahf,
                actionResId = R.string.remind_others,
                actionType = ActionType.ShareText,
                shareTextResId = R.string.suratul_kahf_reminder_message
            )
        )
    }

    private suspend fun MutableList<NextAction>.populateSuratulMulkActions() {
        if (!isNightTime()) return
        populateDailySurahActions(TRACK_READ_SURAH_SAJDAH_KEY, ::getSuratulSajdahNavigateAction, { getDailySuraReadingAction(R.string.suratul_mulk_daily_reminder) })
    }

    private suspend fun MutableList<NextAction>.populateSuratulSajdahActions() {
        if (!isNightTime()) return
        populateDailySurahActions(TRACK_READ_SURAH_MULK_KEY, ::getSuratulMulkNavigateAction, { getDailySuraReadingAction(R.string.suratul_sajdah_daily_reminder) })
    }

    private suspend fun MutableList<NextAction>.populateDailySurahActions(
        flagKey: String,
        firstAction: () -> NextAction,
        secondAction: () -> NextAction
    ) {

        if (flagRepository.getIntFlag(flagKey) == null) {
            add(firstAction())
        }

        val readingSuratulMulkReminder = toDoRepository.getToDoById(READING_SURATUL_MULK_ID)

        if (readingSuratulMulkReminder == null || readingSuratulMulkReminder.isAutomaticToDo){
            add(secondAction())
        }

        add(
            NextAction(
                titleResId = R.string.remind_others,
                subtitleResId = R.string.remind_others_to_read_suratul_mulk,
                actionResId = R.string.remind_others,
                actionType = ActionType.ShareText,
                shareTextResId = R.string.suratul_mulk_and_sajdah_reminder_message
            )
        )
    }

    private fun isNightTime(): Boolean {
        val prayerTimes = getPrayerTimes(
            context = applicationContext,
            toDoRepository = toDoRepository
        )

        val ishaOffset = prayerTimes.find { it.id.toString().endsWith(ISHA_INDEX) }?.timeInMilliseconds
        val ishaTime = ishaOffset?.let { getMidnightTime() + it } ?: return false

        val fajrOffset = prayerTimes.find { it.id.toString().endsWith(FAJR_INDEX) }?.timeInMilliseconds
        val fajrTime = fajrOffset?.let { getMidnightTime() + it } ?: return false

        val currentTime = System.currentTimeMillis()

        return currentTime !in (fajrTime + 1)..< ishaTime
    }

    private fun getDailySuraReadingAction(titleResId: Int): NextAction = NextAction(
        titleResId = titleResId,
        subtitleResId = R.string.set_daily_reminder,
        actionResId = R.string.set_daily_reminder,
        actionType = ActionType.NavigateToTodo,
        toDoId = READING_SURATUL_MULK_ID
    )

    private fun getSuratulMulkNavigateAction(): NextAction = NextAction(
        titleResId = R.string.read_suratul_mulk,
        subtitleResId = R.string.recommended_every_night,
        actionResId = R.string.read_suratul_mulk,
        actionType = ActionType.NavigateToSurah,
        surahPageNumber = SURATUL_MULK_FIRST_PAGE
    )

    private fun getSuratulSajdahNavigateAction(): NextAction = NextAction(
        titleResId = R.string.read_suratul_sajdah,
        subtitleResId = R.string.recommended_every_night,
        actionResId = R.string.read_suratul_sajdah,
        actionType = ActionType.NavigateToSurah,
        surahPageNumber = SURATUL_SAJDAH_FIRST_PAGE
    )


    private fun getMidnightTime(): Long =
        Instant.now()
            .truncatedTo(ChronoUnit.DAYS)
            .toEpochMilli()


    data class NextActionsData(
        val predefinedReminderInfo: String = "",
        val predefinedReminderLink: String = "",
        val nextActions: List<NextAction> = listOf()
    )

    data class NextAction(
        val titleResId: Int,
        val subtitleResId: Int,
        val actionResId: Int,
        val actionType: ActionType,
        val toDoId: Int? = null,
        val shareTextResId: Int? = null,
        val surahPageNumber: Int? = null
    )

    sealed class ActionType {
        data object NavigateToTodo : ActionType()
        data object ShareText : ActionType()
        data object NavigateToSurah : ActionType()
    }
}

