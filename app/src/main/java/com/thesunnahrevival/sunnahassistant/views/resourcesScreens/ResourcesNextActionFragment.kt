package com.thesunnahrevival.sunnahassistant.views.resourcesScreens

import android.app.Dialog
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.thesunnahrevival.sunnahassistant.R
import com.thesunnahrevival.sunnahassistant.data.model.entity.ToDo
import com.thesunnahrevival.sunnahassistant.data.repositories.ResourcesNextActionRepository
import com.thesunnahrevival.sunnahassistant.theme.SunnahAssistantTheme
import com.thesunnahrevival.sunnahassistant.utilities.InAppBrowser
import com.thesunnahrevival.sunnahassistant.utilities.getSunnahAssistantAppLink
import com.thesunnahrevival.sunnahassistant.viewmodels.ResourcesNextActionViewModel
import com.thesunnahrevival.sunnahassistant.viewmodels.SunnahAssistantViewModel
import com.thesunnahrevival.sunnahassistant.views.home.resourcesSection.ResourceCard
import com.thesunnahrevival.sunnahassistant.views.home.resourcesSection.ResourceTitle
import com.thesunnahrevival.sunnahassistant.views.home.resourcesSection.ShimmerResourceCard
import com.thesunnahrevival.sunnahassistant.views.utilities.GrayLine
import java.net.MalformedURLException

const val ADHKAAR_CHAPTER_ID = "adhkaarChapterId"

class ResourcesNextActionFragment : BottomSheetDialogFragment() {

    val mainActivityViewModel by activityViewModels<SunnahAssistantViewModel>()
    private val viewmodel by viewModels<ResourcesNextActionViewModel>()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)

        val adhkaarChapterId = arguments?.getInt(ADHKAAR_CHAPTER_ID)
        if (adhkaarChapterId != null) {
            viewmodel.loadAdhkaarNextActions(adhkaarChapterId)
        } else {
            viewmodel.loadQuranNextActions(mainActivityViewModel.getCurrentQuranPage())
        }

        return ComposeView(requireContext()).apply {
            setContent {
                val nextActionsData by viewmodel.nextActionsData.collectAsState()

                LaunchedEffect(nextActionsData) {
                    nextActionsData?.let {
                        if (it.nextActions.size == 1) {
                            onNextActionClick(it.nextActions.first())
                            dismiss()
                        }
                    }
                }

                NextActionScreen(
                    nextActionsData = nextActionsData,
                    onInfoClick = { link ->
                        onInfoClick(link)
                    }
                ) { nextAction ->
                    onNextActionClick(nextAction)
                }
            }
        }
    }

    private fun onInfoClick(link: String) {
        val inAppBrowser = InAppBrowser(requireContext(), lifecycleScope)
        try {
            inAppBrowser.launchInAppBrowser(
                link,
                findNavController()
            )
        } catch (exception: MalformedURLException) {
            Log.e("MalformedURLException", exception.message.toString())
            Toast.makeText(
                requireContext(),
                getString(R.string.something_wrong),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun onNextActionClick(nextAction: ResourcesNextActionRepository.NextAction) {
        when (nextAction.actionType) {
            ResourcesNextActionRepository.ActionType.NavigateToTodo -> {
                val selectedToDoTemplate =
                    mainActivityViewModel.getTemplateToDos()[nextAction.toDoId]?.second
                navigateToToDoDetails(selectedToDoTemplate)
            }

            ResourcesNextActionRepository.ActionType.ShareText -> {
                nextAction.shareTextResId?.let { textResId ->
                    val message = getString(textResId)
                    val promotionalMessage = getString(
                        R.string.app_promotional_message,
                        getSunnahAssistantAppLink()
                    )
                    val shareText = "$message\n\n$promotionalMessage"

                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, shareText)
                    }

                    startActivity(
                        Intent.createChooser(
                            shareIntent,
                            getString(nextAction.titleResId)
                        )
                    )
                }
            }

            ResourcesNextActionRepository.ActionType.NavigateToSurah -> {
                nextAction.surahPageNumber?.let {
                    mainActivityViewModel.updateCurrentPage(it)
                    requireActivity().findNavController(R.id.myNavHostFragment)
                        .navigate(R.id.quranReaderFragment)
                }
                dismiss()
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

                if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
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
fun NextActionScreen(
    nextActionsData: ResourcesNextActionRepository.NextActionsData? = null,
    onInfoClick: (String) -> Unit = {},
    onClick: (action: ResourcesNextActionRepository.NextAction) -> Unit
) {
    SunnahAssistantTheme {
        Surface {
            Column(
                modifier = Modifier.Companion
                    .fillMaxWidth()
                    .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 32.dp)
            ) {
                GrayLine(modifier = Modifier.Companion.align(Alignment.Companion.CenterHorizontally))

                ResourceTitle(
                    title = stringResource(R.string.suggested_actions)
                )

                if (nextActionsData == null) {
                    repeat(2) {
                        ShimmerResourceCard()
                    }
                } else {
                    for (action in nextActionsData.nextActions) {
                        ResourceCard(
                            title = stringResource(action.titleResId),
                            subtitle = stringResource(action.subtitleResId)
                        ) {
                            onClick(action)
                        }
                    }

                    if (nextActionsData.predefinedReminderInfo.isNotBlank()) {
                        Row(
                            modifier = Modifier.Companion
                                .fillMaxWidth()
                                .padding(top = 24.dp)
                                .clickable(
                                    enabled = Patterns.WEB_URL.matcher(nextActionsData.predefinedReminderLink)
                                        .matches()
                                ) {
                                    onInfoClick(nextActionsData.predefinedReminderLink)
                                },
                            verticalAlignment = Alignment.Companion.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_info),
                                contentDescription = stringResource(R.string.info),
                                modifier = Modifier.Companion.padding(start = 16.dp, end = 16.dp)
                            )

                            Text(
                                text = nextActionsData.predefinedReminderInfo,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Companion.Light,
                                fontFamily = FontFamily.Companion.SansSerif,
                                style = MaterialTheme.typography.caption
                            )
                        }
                    }
                }
            }

        }
    }
}

@Composable
@Preview
fun LightModePreview() {
    NextActionScreen(onClick = {})
}

@Composable
@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    locale = "en"
)
fun DarkModePreview() {
    NextActionScreen(onClick = {})
}