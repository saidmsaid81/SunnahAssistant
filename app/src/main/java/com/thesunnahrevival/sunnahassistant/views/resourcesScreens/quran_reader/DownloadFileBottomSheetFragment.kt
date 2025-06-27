package com.thesunnahrevival.sunnahassistant.views.resourcesScreens.quran_reader

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.thesunnahrevival.sunnahassistant.R
import com.thesunnahrevival.sunnahassistant.theme.SunnahAssistantTheme
import com.thesunnahrevival.sunnahassistant.viewmodels.DownloadFileViewModel
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
                    DownloadFileScreen()
                }
            }
        }
    }
    @Composable
    private fun DownloadFileScreen() {

        var hideDownloadFilesPrompt by rememberSaveable {
            mutableStateOf(false)
        }

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
                    text = buildAnnotatedString {
                        append(stringResource(R.string.download_file_info_part1))
                        append(" ")
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                            append(stringResource(R.string.download_file_info_part2))
                        }
                        append(" ")
                        append(stringResource(R.string.download_file_info_part3))
                        append(" ")
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                            append(stringResource(R.string.download_file_info_part4))
                        }
                        append(" ")
                        append(stringResource(R.string.download_file_info_part5))
                    },
                    style = MaterialTheme.typography.subtitle1
                )

                // Mobile data usage note
                Text(
                    text = stringResource(R.string.download_file_warning_message),
                    style = MaterialTheme.typography.caption,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp)
                )

                // Don's show this again checkbox
                SunnahAssistantCheckbox(
                    text = stringResource(R.string.don_t_show_this_again),
                    modifier = Modifier.padding(top = 16.dp),
                    checked = hideDownloadFilesPrompt
                ) {
                    viewModel.disableDownloadFilesPrompt()
                    hideDownloadFilesPrompt = true
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {

                    // Action Buttons
                    Row(modifier = Modifier.padding(top = 16.dp)) {
                        OutlinedButton(
                            onClick = { dismiss() },
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 16.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.continue_label)
                            )
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
            DownloadFileScreen()
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