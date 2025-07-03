package com.thesunnahrevival.sunnahassistant.views.resourcesScreens.quran_reader

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
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
import com.thesunnahrevival.sunnahassistant.utilities.DownloadManager
import com.thesunnahrevival.sunnahassistant.utilities.DownloadManager.DownloadProgress
import com.thesunnahrevival.sunnahassistant.utilities.DownloadManager.Downloading
import com.thesunnahrevival.sunnahassistant.utilities.DownloadManager.Extracting
import com.thesunnahrevival.sunnahassistant.utilities.DownloadManager.Preparing
import com.thesunnahrevival.sunnahassistant.viewmodels.DownloadFileViewModel
import com.thesunnahrevival.sunnahassistant.viewmodels.DownloadFileViewModel.DownloadCancelledState
import com.thesunnahrevival.sunnahassistant.viewmodels.DownloadFileViewModel.DownloadCompleteState
import com.thesunnahrevival.sunnahassistant.viewmodels.DownloadFileViewModel.DownloadInProgressState
import com.thesunnahrevival.sunnahassistant.viewmodels.DownloadFileViewModel.DownloadPromptState
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
                    val downloadUIState by viewModel.downloadUIState.collectAsState()

                    when (downloadUIState) {
                        DownloadPromptState -> PromptScreen()
                        is DownloadInProgressState -> {
                            if ((downloadUIState as DownloadInProgressState).downloadProgress == DownloadManager.NotInitiated) {
                                PromptScreen()
                            } else {
                                DownloadScreen((downloadUIState as DownloadInProgressState).downloadProgress)
                            }
                        }
                        DownloadCompleteState -> CompletionScreen()
                        DownloadCancelledState -> {
                            LaunchedEffect(Unit) {
                                dismiss()
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun PromptScreen() {
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
                    if (!hideDownloadFilesPrompt) {
                        viewModel.disableDownloadFilesPrompt()
                        hideDownloadFilesPrompt = true
                    } else {
                        viewModel.enableDownloadFilesPrompt()
                        hideDownloadFilesPrompt = false
                    }
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
                                viewModel.downloadQuranFiles()
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

    @Composable
    private fun DownloadScreen(downloadProgress: DownloadProgress) {
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
                    text = stringResource(R.string.downloading_quran_files_please_wait),
                    style = MaterialTheme.typography.subtitle2,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                val progress = when (downloadProgress) {
                    is Downloading -> {
                        (downloadProgress.totalDownloadedSize / downloadProgress.totalFileSize)
                    }

                    is Extracting -> {
                        100f
                    }

                    else -> {
                        0f
                    }
                }

                LinearProgressIndicator(
                    progress = progress, strokeCap = StrokeCap.Round, modifier = Modifier
                        .fillMaxWidth()
                        .height(16.dp)
                )

                when (downloadProgress) {
                    is Preparing -> {
                        //Calculating file size
                        Text(
                            text = stringResource(R.string.calculating),
                            style = MaterialTheme.typography.overline,
                            modifier = Modifier.padding(top = 16.dp)
                        )
                    }

                    is Downloading -> {
                        // Download progress
                        Text(
                            text = stringResource(
                                R.string.downloaded,
                                downloadProgress.totalDownloadedSize,
                                downloadProgress.totalFileSize,
                                downloadProgress.unit
                            ),
                            style = MaterialTheme.typography.overline,
                            modifier = Modifier.padding(top = 16.dp)
                        )
                    }

                    else -> {
                        // Extracting
                        Text(
                            text = stringResource(R.string.extracting),
                            style = MaterialTheme.typography.overline,
                            modifier = Modifier.padding(top = 16.dp)
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {

                    // Action Buttons
                    Row(modifier = Modifier.padding(top = 16.dp)) {
                        Spacer(modifier = Modifier.weight(1f))
                        OutlinedButton(
                            onClick = { viewModel.cancelDownload() },
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 16.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.cancel)
                            )
                        }
                        Button(
                            onClick = {
                                dismiss()
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(text = stringResource(R.string.background))
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun CompletionScreen() {
        Surface {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                GrayLine(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(bottom = 16.dp, top = 16.dp)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = stringResource(R.string.download_complete),
                        modifier = Modifier.size(80.dp),
                        tint = MaterialTheme.colors.primary
                    )
                }

                Text(
                    text = stringResource(R.string.download_complete),
                    style = MaterialTheme.typography.h6,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 32.dp)
                )

                Button(
                    onClick = { dismiss() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Text(stringResource(R.string.ok))
                }
            }
        }
    }

    @Preview(
        name = "Prompt Screen - Dark Mode",
        uiMode = Configuration.UI_MODE_NIGHT_YES,
        locale = "en"
    )
    @Composable
    private fun PromptScreenPreviewDark() {
        PromptScreenPreview()
    }

    @Preview(name = "Prompt Screen - Light Mode")
    @Composable
    private fun PromptScreenPreview() {
        SunnahAssistantTheme {
            PromptScreen()
        }
    }

    @Preview(
        name = "Prompt Screen - Arabic Dark Mode",
        uiMode = Configuration.UI_MODE_NIGHT_YES,
        locale = "ar"
    )
    @Composable
    private fun PromptScreenPreviewArabic() {
        PromptScreenPreview()
    }

    @Preview(
        name = "Download Screen - Dark Mode",
        uiMode = Configuration.UI_MODE_NIGHT_YES,
        locale = "en"
    )
    @Composable
    private fun DownloadScreenPreviewDark() {
        DownloadScreenPreview()
    }

    @Preview(name = "Download Screen - Light Mode")
    @Composable
    private fun DownloadScreenPreview() {
        SunnahAssistantTheme {
            DownloadScreen(Downloading(50f, 100f, "MB"))
        }
    }

    @Preview(
        name = "Download Screen - Arabic Dark Mode",
        uiMode = Configuration.UI_MODE_NIGHT_YES,
        locale = "ar"
    )
    @Composable
    private fun DownloadScreenPreviewArabic() {
        DownloadScreenPreview()
    }

    @Preview(
        name = "Completion Screen - Dark Mode",
        uiMode = Configuration.UI_MODE_NIGHT_YES,
        locale = "en"
    )
    @Composable
    private fun CompletionScreenPreviewDark() {
        CompletionScreenPreview()
    }

    @Preview(name = "Completion Screen - Light Mode")
    @Composable
    private fun CompletionScreenPreview() {
        SunnahAssistantTheme {
            CompletionScreen()
        }
    }

    @Preview(
        name = "Completion Screen - Arabic Dark Mode",
        uiMode = Configuration.UI_MODE_NIGHT_YES,
        locale = "ar"
    )
    @Composable
    private fun CompletionScreenPreviewArabic() {
        CompletionScreenPreview()
    }
}