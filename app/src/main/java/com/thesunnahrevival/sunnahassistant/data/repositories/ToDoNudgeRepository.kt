package com.thesunnahrevival.sunnahassistant.data.repositories

import android.content.Context
import com.thesunnahrevival.sunnahassistant.R
import com.thesunnahrevival.sunnahassistant.utilities.READING_SURATUL_MULK_ID
import com.thesunnahrevival.sunnahassistant.utilities.TemplateToDos
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.LocalDate

const val NUDGE_DISMISSED_DATE_KEY = "nudge-dismissed-date"

class ToDoNudgeRepository private constructor(
    applicationContext: Context
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

    private fun buildNudge(
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
        if (missingTemplates.isNotEmpty()) {
            val randomMissing = missingTemplates.entries.random()
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
