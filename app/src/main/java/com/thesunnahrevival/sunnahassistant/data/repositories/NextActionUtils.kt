package com.thesunnahrevival.sunnahassistant.data.repositories

import android.content.Context
import com.thesunnahrevival.sunnahassistant.R
import com.thesunnahrevival.sunnahassistant.data.model.entity.ToDo
import com.thesunnahrevival.sunnahassistant.data.repositories.ResourcesNextActionRepository.ActionType
import com.thesunnahrevival.sunnahassistant.data.repositories.ResourcesNextActionRepository.NextAction
import com.thesunnahrevival.sunnahassistant.utilities.BEFORE_SLEEPING_ADHKAAR_CHAPTER_ID
import com.thesunnahrevival.sunnahassistant.utilities.getPrayerTimes
import java.time.Instant
import java.time.LocalDate
import java.time.temporal.ChronoUnit

const val FAJR_INDEX = '0'
const val DHUHR_INDEX = '1'
const val ASR_INDEX = '2'
const val MAGHRIB_INDEX = '3'
const val ISHA_INDEX = '4'

const val TRACK_READ_SURAH_PREFIX = "track-read-surah"

internal const val SURATUL_MULK_FIRST_PAGE = 562
internal const val SURATUL_SAJDAH_FIRST_PAGE = 415

private fun getTrackReadSurahKey(date: LocalDate = LocalDate.now()): String =
    "$TRACK_READ_SURAH_PREFIX-$date"

internal val TRACK_READ_SURAH_MULK_KEY: String
    get() = "${getTrackReadSurahKey()}-mulk"

internal val TRACK_READ_SURAH_SAJDAH_KEY: String
    get() = "${getTrackReadSurahKey()}-sajdah"

internal val TRACK_READ_SLEEPING_ADHKAAR_KEY: String
    get() = "${getTrackReadSurahKey()}-sleeping-adhkaar"

internal fun getMidnightTime(): Long =
    Instant.now().truncatedTo(ChronoUnit.DAYS).toEpochMilli()

internal fun isNightTime(context: Context, toDoRepository: SunnahAssistantRepository): Boolean {
    val prayerTimes = getPrayerTimes(context = context, toDoRepository = toDoRepository)
    val ishaOffset = prayerTimes.find { it.id.toString().endsWith(ISHA_INDEX) }?.timeInMilliseconds
    val ishaTime = ishaOffset?.let { getMidnightTime() + it } ?: return false
    val fajrOffset = prayerTimes.find { it.id.toString().endsWith(FAJR_INDEX) }?.timeInMilliseconds
    val fajrTime = fajrOffset?.let { getMidnightTime() + it } ?: return false
    val currentTime = System.currentTimeMillis()
    return currentTime !in (fajrTime + 1)..<ishaTime
}

internal fun getSuratulMulkNavigateAction(): NextAction = NextAction(
    titleResId = R.string.read_suratul_mulk,
    subtitleResId = R.string.recommended_every_night,
    actionResId = R.string.read_suratul_mulk,
    actionType = ActionType.NavigateToSurah,
    surahPageNumber = SURATUL_MULK_FIRST_PAGE
)

internal fun getSuratulSajdahNavigateAction(): NextAction = NextAction(
    titleResId = R.string.read_suratul_sajdah,
    subtitleResId = R.string.recommended_every_night,
    actionResId = R.string.read_suratul_sajdah,
    actionType = ActionType.NavigateToSurah,
    surahPageNumber = SURATUL_SAJDAH_FIRST_PAGE
)

internal fun MutableList<NextAction>.addMarkAsCompleteIfEligible(toDo: ToDo?) {
    if (toDo != null && !toDo.isAutomaticToDo && !toDo.isComplete(LocalDate.now())) {
        add(
            NextAction(
                titleResId = R.string.mark_as_complete,
                subtitleResId = R.string.mark_as_complete_subtitle,
                actionResId = R.string.mark_as_complete,
                actionType = ActionType.MarkAsComplete,
                toDoId = toDo.id
            )
        )
    }
}

internal fun getSleepingAdhkaarNavigateAction(): NextAction = NextAction(
    titleResId = R.string.read_adhkaar_before_sleeping,
    subtitleResId = R.string.recommended_every_night,
    actionResId = R.string.read_adhkaar_before_sleeping,
    actionType = ActionType.NavigateToAdhkaar,
    adhkaarChapterNumber = BEFORE_SLEEPING_ADHKAAR_CHAPTER_ID
)
