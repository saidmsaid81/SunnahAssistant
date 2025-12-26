package com.thesunnahrevival.sunnahassistant.data.repositories

import android.content.Context
import com.thesunnahrevival.sunnahassistant.R
import com.thesunnahrevival.sunnahassistant.data.model.entity.ToDo
import com.thesunnahrevival.sunnahassistant.utilities.READING_SURATUL_KAHF_ID
import com.thesunnahrevival.sunnahassistant.utilities.READING_SURATUL_MULK_ID
import com.thesunnahrevival.sunnahassistant.utilities.SURATUL_KAHF_REMIND_OTHERS_ID
import com.thesunnahrevival.sunnahassistant.utilities.SURATUL_MULK_REMIND_OTHERS_ID
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.temporal.ChronoUnit

private const val FAJR_INDEX = '0'
private const val MAGHRIB_INDEX = '3'
private const val ISHA_INDEX = '4'

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

    suspend fun getNextActions(page: Int): List<NextAction> {
        return buildList {
            when (page) {
                304 -> {
                    populateSuratulKahfActions()
                }
                564 -> {
                    populateSuratulMulkActions()
                }
            }
        }
    }

    private suspend fun MutableList<NextAction>.populateSuratulKahfActions() {
        val now = LocalDate.now()
        val day = now.dayOfWeek

        if (day != DayOfWeek.THURSDAY && day != DayOfWeek.FRIDAY) return

        val prayerTimes = getPrayerTimes(now)

        val maghribOffset = prayerTimes.find { it.id.toString().endsWith(MAGHRIB_INDEX) }?.timeInMilliseconds
        val maghribTime = maghribOffset?.let { getMidnightTime() + it } ?: return

        val currentTime = System.currentTimeMillis()
        val isKahfTime = when (day) {
            DayOfWeek.THURSDAY -> currentTime >= maghribTime
            DayOfWeek.FRIDAY -> currentTime <= maghribTime
            else -> false
        }

        if (!isKahfTime) return

        if (toDoRepository.getToDoById(READING_SURATUL_KAHF_ID) == null) {
            add(
                NextAction(
                    actionId = READING_SURATUL_KAHF_ID,
                    titleResId = R.string.weekly_friday_reminder,
                    subtitleResId = R.string.set_reminder_for_every_friday,
                    actionResId = R.string.set_weekly_reminder,
                    actionType = ActionType.NavigateToTodo,
                    shareTextResId = null
                )
            )
        }

        add(
            NextAction(
                actionId = SURATUL_KAHF_REMIND_OTHERS_ID,
                titleResId = R.string.remind_others,
                subtitleResId = R.string.remind_others_to_read_suratul_kahf,
                actionResId = R.string.remind_others,
                actionType = ActionType.ShareText,
                shareTextResId = R.string.suratul_kahf_reminder_message
            )
        )
    }

    private suspend fun MutableList<NextAction>.populateSuratulMulkActions() {
        val now = LocalDate.now()

        val prayerTimes = getPrayerTimes(now)

        val ishaOffset = prayerTimes.find { it.id.toString().endsWith(ISHA_INDEX) }?.timeInMilliseconds
        val ishaTime = ishaOffset?.let { getMidnightTime() + it } ?: return

        val fajrOffset = prayerTimes.find { it.id.toString().endsWith(FAJR_INDEX) }?.timeInMilliseconds
        val fajrTime = fajrOffset?.let { getMidnightTime() + it } ?: return

        val currentTime = System.currentTimeMillis()

        if (currentTime in (fajrTime + 1)..<ishaTime) {
            return
        }

        if (toDoRepository.getToDoById(READING_SURATUL_MULK_ID) == null) {
            add(
                NextAction(
                    actionId = READING_SURATUL_MULK_ID,
                    titleResId = R.string.suratul_mulk_daily_reminder,
                    subtitleResId = R.string.set_daily_reminder,
                    actionResId = R.string.set_daily_reminder,
                    actionType = ActionType.NavigateToTodo,
                    shareTextResId = null
                )
            )
        }

        add(
            NextAction(
                actionId = SURATUL_MULK_REMIND_OTHERS_ID,
                titleResId = R.string.remind_others,
                subtitleResId = R.string.remind_others_to_read_suratul_mulk,
                actionResId = R.string.remind_others,
                actionType = ActionType.ShareText,
                shareTextResId = R.string.suratul_mulk_and_sajdah_reminder_message
            )
        )
    }

    private fun getPrayerTimes(now: LocalDate): List<ToDo> {
        val categories = applicationContext.resources.getStringArray(R.array.categories)
        val prayerTimes = toDoRepository.getPrayerTimesValue(
            day = now.dayOfMonth,
            month = now.month.ordinal, // Repo expects 0-indexed months
            year = now.year,
            categoryName = categories.getOrNull(2).orEmpty()
        )
        return prayerTimes
    }

    private fun getMidnightTime(): Long =
        Instant.now()
            .truncatedTo(ChronoUnit.DAYS)
            .toEpochMilli()


    data class NextAction(
        val actionId: Int,
        val titleResId: Int,
        val subtitleResId: Int,
        val actionResId: Int,
        val actionType: ActionType,
        val shareTextResId: Int?
    )

    sealed class ActionType {
        data object NavigateToTodo : ActionType()
        data object ShareText : ActionType()
    }
}