package com.thesunnahrevival.sunnahassistant.views.resourcesScreens.quran_reader

import android.app.Dialog
import android.content.res.Configuration.ORIENTATION_LANDSCAPE
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.thesunnahrevival.sunnahassistant.data.model.Translation
import com.thesunnahrevival.sunnahassistant.theme.SunnahAssistantTheme
import com.thesunnahrevival.sunnahassistant.viewmodels.AyahTranslationViewModel
import com.thesunnahrevival.sunnahassistant.viewmodels.SunnahAssistantViewModel
import com.thesunnahrevival.sunnahassistant.viewmodels.TranslationViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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
                        selectedAyah?.let {
                            val translationUiState by viewModel.translationUiState.collectAsState(initial = TranslationViewModel.TranslationUiState())
                            SheetContent(
                                it,
                                translationUiState.allTranslations,
                                translationUiState.selectedTranslations,
                                translationUiState.translationsDownloadInProgress,
                                { mainActivityViewModel.nextAyah() },
                                { mainActivityViewModel.previousAyah() },
                                { translation: Translation ->
                                    viewModel.toggleTranslationSelection(
                                        translation,
                                        translationUiState.selectedTranslations.size
                                    ) {
                                        mainActivityViewModel.refreshSelectedAyahId() }
                                },
                                viewModel.visibleFootnotes,
                                { ayahTranslationId, footnoteNumber ->
                                    viewModel.toggleFootnote(
                                        ayahTranslationId,
                                        footnoteNumber
                                    )
                                }
                            )
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
}