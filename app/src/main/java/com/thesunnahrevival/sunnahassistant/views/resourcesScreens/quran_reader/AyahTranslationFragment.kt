package com.thesunnahrevival.sunnahassistant.views.resourcesScreens.quran_reader

import android.app.Dialog
import android.content.res.Configuration
import android.content.res.Configuration.ORIENTATION_LANDSCAPE
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.thesunnahrevival.sunnahassistant.R
import com.thesunnahrevival.sunnahassistant.data.model.Translation
import com.thesunnahrevival.sunnahassistant.theme.SunnahAssistantTheme
import com.thesunnahrevival.sunnahassistant.viewmodels.AyahTranslationViewModel
import com.thesunnahrevival.sunnahassistant.viewmodels.SunnahAssistantViewModel
import com.thesunnahrevival.sunnahassistant.viewmodels.TranslationViewModel
import com.thesunnahrevival.sunnahassistant.views.MainActivity
import com.thesunnahrevival.sunnahassistant.views.utilities.ArabicTextWithTranslationShimmer
import com.thesunnahrevival.sunnahassistant.views.utilities.GrayLine
import com.thesunnahrevival.sunnahassistant.views.utilities.TranslationDropdown
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AyahTranslationFragment : BottomSheetDialogFragment() {

    private val viewModel: AyahTranslationViewModel by viewModels()
    private val mainActivityViewModel: SunnahAssistantViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            mainActivityViewModel.selectedAyahId.observe(viewLifecycleOwner) { ayahId ->
                if (ayahId != null) {
                    lifecycleScope.launch(Dispatchers.IO) {
                        val ayah = viewModel.getAyahById(ayahId)
                        ayah?.let {
                            viewModel.setSelectedAyah(it)
                        }
                    }
                }
            }

            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                SunnahAssistantTheme {
                    Surface {
                        val selectedAyah by viewModel.selectedAyah.collectAsState()
                        val translationUiState by viewModel.translationUiState.collectAsState(initial = TranslationViewModel.TranslationUiState())
                        val expanded = remember { mutableStateOf(false) }
                        val settings by mainActivityViewModel.getSettings().observeAsState()

                        if (settings == null) {
                            ArabicTextWithTranslationShimmer(0)
                        } else {
                            selectedAyah?.let {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                        .clickable(
                                            onClick = { expanded.value = false },
                                            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                            indication = null
                                        )
                                ) {
                                    GrayLine(modifier = Modifier.align(Alignment.CenterHorizontally))

                                    Spacer(modifier = Modifier.height(16.dp))

                                    AyahTitle(
                                        it.surah.transliteratedName,
                                        stringResource(R.string.ayah_number, it.ayah.number),
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    TranslationDropdown(
                                        translationUiState.allTranslations,
                                        translationUiState.selectedTranslations,
                                        translationUiState.translationsDownloadInProgress,
                                        expanded
                                    ) { translation: Translation ->
                                        viewModel.toggleTranslationSelection(
                                            translation,
                                            translationUiState.selectedTranslations.size
                                        ) {
                                            withContext(Dispatchers.Main) {
                                                mainActivityViewModel.refreshSelectedAyahId()
                                            }
                                        }
                                    }

                                    LazyColumn(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxWidth()
                                    ) {
                                        item {

                                            AyahTranslation(
                                                context = requireContext(),
                                                ayahFullDetail = it,
                                                index = 0,
                                                selectedTranslations = translationUiState.selectedTranslations,
                                                translationsDownloadInProgress = translationUiState.translationsDownloadInProgress,
                                                visibleFootnotes = viewModel.visibleFootnotes,
                                                onFootnoteClick = { ayahTranslationId, footnoteNumber ->
                                                    viewModel.toggleFootnote(
                                                        ayahTranslationId,
                                                        footnoteNumber
                                                    )
                                                },
                                                arabicTextFontSize = settings?.arabicTextFontSize ?: 18,
                                                translationTextFontSize = settings?.translationTextFontSize ?: 16,
                                                footnoteTextFontSize = settings?.footnoteTextFontSize ?: 12
                                            ) {
                                                lifecycleScope.launch(Dispatchers.IO) {
                                                    mainActivityViewModel.toggleAyahBookmark(
                                                        it.ayah,
                                                        updateSelectedAyahId = true
                                                    )
                                                }
                                            }
                                        }
                                    }


                                    Spacer(modifier = Modifier.height(16.dp))

                                    Row(modifier = Modifier.fillMaxWidth()) {
                                        Next {
                                            mainActivityViewModel.nextAyah()
                                        }

                                        Spacer(modifier = Modifier.weight(1f))

                                        FontSettings {
                                            findNavController().navigate(R.id.fontSettingsFragment)
                                            leaveImmersiveMode()
                                            dismiss()
                                        }

                                        Spacer(modifier = Modifier.weight(1f))

                                        Previous {
                                            mainActivityViewModel.previousAyah()
                                        }
                                    }


                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun leaveImmersiveMode() {
        val activity = activity as MainActivity
        activity.supportActionBar?.show()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val controller = activity.window.insetsController
            if (controller != null) {
                controller.show(WindowInsets.Type.statusBars())
                controller.show(WindowInsets.Type.navigationBars())
            }
        } else {
            activity.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
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

               val displayMetrics = requireContext().resources.displayMetrics

               if (resources.configuration.orientation != ORIENTATION_LANDSCAPE) {
                   val height = (displayMetrics.heightPixels * 0.6).toInt()
                   it.layoutParams.height = height
                   dialog.window?.setDimAmount(0f)
               } else {
                   val height = (displayMetrics.heightPixels * 0.8).toInt()
                   it.layoutParams.height = height
                   behavior.state = BottomSheetBehavior.STATE_EXPANDED
                    dialog.window?.setDimAmount(0f)
               }
           }
       }

        return dialog
    }

    @Composable
    private fun Next(modifier: Modifier = Modifier, onClick: () -> Unit) {
        Icon(
            imageVector = Icons.Filled.KeyboardArrowDown,
            contentDescription = stringResource(R.string.next),
            modifier = modifier
                .size(36.dp)
                .clickable { onClick() }
        )
    }

    @Composable
    private fun Previous(modifier: Modifier = Modifier, onClick: () -> Unit) {
        Icon(
            imageVector = Icons.Filled.KeyboardArrowUp,
            contentDescription = stringResource(R.string.previous),
            modifier = modifier
                .size(36.dp)
                .clickable { onClick() }
        )
    }

    @Composable
    private fun FontSettings(modifier: Modifier = Modifier, onClick: () -> Unit) {
        Icon(
            imageVector = Icons.Filled.Settings,
            contentDescription = stringResource(R.string.font_settings),
            modifier = modifier
                .size(36.dp)
                .clickable { onClick() }
        )
    }

    @Composable
    private fun AyahTitle(
        title: String,
        subTitle: String,
        modifier: Modifier
    ) {
        val configuration = LocalConfiguration.current
        val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

        if (isLandscape) {
            Row(
                modifier = modifier
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = stringResource(id = R.string.ayah_number, subTitle.split(" ")[1].toInt()),
                    fontSize = 16.sp,
                )
            }
        } else {
            Column(
                modifier = modifier
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 16.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                )

                Text(
                    text = stringResource(id = R.string.ayah_number, subTitle.split(" ")[1].toInt()),
                    fontSize = 12.sp,
                )
            }
        }
    }
}