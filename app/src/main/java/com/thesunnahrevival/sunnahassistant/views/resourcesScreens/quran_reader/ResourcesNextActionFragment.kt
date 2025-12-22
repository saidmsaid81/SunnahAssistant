package com.thesunnahrevival.sunnahassistant.views.resourcesScreens.quran_reader

import android.app.Dialog
import android.content.Intent
import android.content.res.Configuration
import android.content.res.Configuration.ORIENTATION_LANDSCAPE
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.thesunnahrevival.sunnahassistant.R
import com.thesunnahrevival.sunnahassistant.data.model.entity.ToDo
import com.thesunnahrevival.sunnahassistant.theme.SunnahAssistantTheme
import com.thesunnahrevival.sunnahassistant.utilities.READING_SURATUL_KAHF_ID
import com.thesunnahrevival.sunnahassistant.utilities.SURATUL_KAHF_REMIND_OTHERS_ID
import com.thesunnahrevival.sunnahassistant.utilities.getSunnahAssistantAppLink
import com.thesunnahrevival.sunnahassistant.viewmodels.SunnahAssistantViewModel
import com.thesunnahrevival.sunnahassistant.views.home.resourcesSection.ResourceCard
import com.thesunnahrevival.sunnahassistant.views.home.resourcesSection.ResourceTitle
import com.thesunnahrevival.sunnahassistant.views.utilities.GrayLine

class ResourcesNextActionFragment : BottomSheetDialogFragment() {

    val mainActivityViewModel by activityViewModels<SunnahAssistantViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        return ComposeView(requireContext()).apply {
            setContent {
                NextActionScreen { resourceNumber ->
                    when(resourceNumber) {
                        READING_SURATUL_KAHF_ID -> {
                            val selectedToDoTemplate =
                                mainActivityViewModel.getTemplateToDos()[READING_SURATUL_KAHF_ID]?.second
                            navigateToToDoDetails(selectedToDoTemplate)
                        }
                        SURATUL_KAHF_REMIND_OTHERS_ID -> {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    getString(
                                        R.string.suratul_kahf_reminder_message,
                                        getString(R.string.app_name),
                                        getSunnahAssistantAppLink()
                                    )
                                )
                            }

                            startActivity(Intent.createChooser(shareIntent,
                                getString(R.string.reminder_to_read_suratul_kahf_title)))
                        }
                    }
                }
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog

        dialog.setOnShowListener {
            val bottomSheet =
                dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.let {
                val behavior = BottomSheetBehavior.from(it)
                behavior.isDraggable = false

                if (resources.configuration.orientation == ORIENTATION_LANDSCAPE) {
                    it.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
                    behavior.state = BottomSheetBehavior.STATE_EXPANDED
                    dialog.window?.setDimAmount(0f)
                }
            }
        }

        return dialog
    }

    private fun navigateToToDoDetails(selectedToDoTemplate: ToDo?) {
        if (selectedToDoTemplate != null) {
            mainActivityViewModel.isToDoTemplate = true
            mainActivityViewModel.selectedToDo = selectedToDoTemplate
            dismiss()
            requireActivity().findNavController(R.id.myNavHostFragment)
                .navigate(R.id.toDoDetailsFragment)
        }
    }
}

@Composable
fun NextActionScreen(onClick: (resourceNumber: Int) -> Unit) {
    SunnahAssistantTheme {
        Surface {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 32.dp)
            ) {
                GrayLine(modifier = Modifier.align(Alignment.CenterHorizontally))

                ResourceTitle(
                    title = stringResource(R.string.suggested_actions)
                )

                ResourceCard(
                    title = stringResource(R.string.weekly_friday_reminder),
                    subtitle = stringResource(R.string.set_reminder_for_every_friday)
                ) {
                    onClick(READING_SURATUL_KAHF_ID)
                }

                ResourceCard(
                    title = stringResource(R.string.remind_others),
                    subtitle = stringResource(R.string.remind_others_to_read_suratul_kahf)
                ) {
                    onClick(SURATUL_KAHF_REMIND_OTHERS_ID)
                }
            }

        }
    }
}

@Composable
@Preview
fun LightModePreview() {
    NextActionScreen{}
}

@Composable
@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    locale = "en"
)
fun DarkModePreview() {
    NextActionScreen{}
}