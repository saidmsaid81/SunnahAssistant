package com.thesunnahrevival.sunnahassistant.views.resourcesScreens.quran_reader

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material.Surface
import androidx.compose.runtime.collectAsState
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
                        val selectedAyah = viewModel.selectedAyah.collectAsState()
                        selectedAyah.value?.let {
                            val translationUiState =
                                viewModel.translationUiState.collectAsState(initial = TranslationViewModel.TranslationUiState())
                            SheetContent(
                                it,
                                translationUiState.value.allTranslations,
                                translationUiState.value.selectedTranslations,
                                { mainActivityViewModel.nextAyah() },
                                { mainActivityViewModel.previousAyah() },
                                { translation: Translation ->
                                    viewModel.toggleTranslationSelection(
                                        translation
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

                // Set the height to 60% of screen height
                val displayMetrics = requireContext().resources.displayMetrics
                val height = (displayMetrics.heightPixels * 0.6).toInt()
                it.layoutParams.height = height
                dialog.window?.setDimAmount(0f)
            }
        }

        return dialog
    }
}