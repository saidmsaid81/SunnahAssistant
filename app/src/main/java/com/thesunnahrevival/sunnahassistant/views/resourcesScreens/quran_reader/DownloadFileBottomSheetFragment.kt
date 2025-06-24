package com.thesunnahrevival.sunnahassistant.views.resourcesScreens.quran_reader

import android.app.Dialog
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.thesunnahrevival.sunnahassistant.R
import com.thesunnahrevival.sunnahassistant.data.model.Translation
import com.thesunnahrevival.sunnahassistant.theme.SunnahAssistantTheme
import com.thesunnahrevival.sunnahassistant.viewmodels.DownloadFileViewModel
import com.thesunnahrevival.sunnahassistant.viewmodels.TranslationViewModel
import com.thesunnahrevival.sunnahassistant.views.utilities.SunnahAssistantCheckbox

class DownloadFileBottomSheetFragment : BottomSheetDialogFragment() {

    private val viewModel: DownloadFileViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        return ComposeView(requireContext()).apply {
            setContent {
                SunnahAssistantTheme {
                    val translationUiState =
                        viewModel.translationUiState.collectAsState(initial = TranslationViewModel.TranslationUiState())
                    val translations = translationUiState.value.allTranslations
                    val selectedTranslations = translationUiState.value.selectedTranslations
                    DownloadFileScreen(
                        translations = translations,
                        selectedTranslations = selectedTranslations
                    )
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
            }
        }

        return dialog
    }

    @Composable
    private fun DownloadFileScreen(
        translations: List<Translation>, selectedTranslations: List<Translation>
    ) {
        val (translationSelected, setTranslationSelected) = rememberSaveable { mutableStateOf(false) }

        Surface {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp)
            ) {

                GrayLine(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(bottom = 16.dp, top = 16.dp)
                )

                Text(
                    text = stringResource(R.string.download_file_info),
                    style = MaterialTheme.typography.subtitle1
                )

                Text(
                    text = stringResource(R.string.select_files_to_download),
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.subtitle1
                )

                SunnahAssistantCheckbox(
                    text = stringResource(R.string._604_quran_pages),
                    checked = true,
                    enabled = false,
                    onSelection = {})
                SunnahAssistantCheckbox(
                    text = stringResource(R.string.translations_optional),
                    checked = translationSelected
                ) {
                    setTranslationSelected(!translationSelected)
                }

                // Translation Selection List
                if (translationSelected) {
                    TranslationDropdown(
                        translations = translations,
                        selectedTranslations = selectedTranslations
                    ) { translation -> viewModel.toggleTranslationSelection(translation) }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {

                    // Mobile data usage note
                    Text(
                        text = stringResource(R.string.download_file_warning_message),
                        style = MaterialTheme.typography.caption,
                        fontWeight = FontWeight.Bold
                    )

                    // Action Buttons
                    Row(modifier = Modifier.padding(top = 16.dp)) {
                        Spacer(modifier = Modifier.weight(1f))

                        OutlinedButton(
                            onClick = { dismiss() },
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 16.dp)
                        ) {
                            Text(stringResource(R.string.cancel))
                        }


                        Button(
                            onClick = {
                                dismiss()
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(stringResource(R.string.download))
                        }
                    }
                }
            }
        }
    }

    @Preview(
        uiMode = Configuration.UI_MODE_NIGHT_YES,
        locale = "en"
    )
    @Composable
    private fun DownloadFileScreenPreviewDark() {
        DownloadFileScreenPreview()
    }

    @Preview
    @Composable
    private fun DownloadFileScreenPreview() {
        SunnahAssistantTheme {
            DownloadFileScreen(translations = listOf(), selectedTranslations = listOf())
        }
    }

    @Preview(
        name = "Arabic Dark Mode",
        uiMode = Configuration.UI_MODE_NIGHT_YES,
        locale = "ar"
    )
    @Composable
    private fun DownloadFileScreenPreviewArabic() {
        DownloadFileScreenPreview()
    }
}