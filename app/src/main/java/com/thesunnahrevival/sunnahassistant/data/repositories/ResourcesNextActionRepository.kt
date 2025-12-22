package com.thesunnahrevival.sunnahassistant.data.repositories

import android.content.Context
import com.thesunnahrevival.sunnahassistant.R
import com.thesunnahrevival.sunnahassistant.utilities.READING_SURATUL_KAHF_ID
import com.thesunnahrevival.sunnahassistant.utilities.SURATUL_KAHF_REMIND_OTHERS_ID

class ResourcesNextActionRepository private constructor(
    applicationContext: Context
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
                }
            }
    }

    private suspend fun MutableList<NextAction>.populateSuratulKahfActions() {
        if (toDoRepository.getToDoById(READING_SURATUL_KAHF_ID) == null) {
            add(
                NextAction(
                    READING_SURATUL_KAHF_ID,
                    R.string.weekly_friday_reminder,
                    R.string.set_reminder_for_every_friday,
                    R.string.set_weekly_reminder,
                    ActionType.NavigateToTodo,
                    null
                )
            )
        }

        add(
            NextAction(
                SURATUL_KAHF_REMIND_OTHERS_ID,
                R.string.remind_others,
                R.string.remind_others_to_read_suratul_kahf,
                R.string.remind_others,
                ActionType.ShareText,
                R.string.suratul_kahf_reminder_message
            )
        )
    }


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