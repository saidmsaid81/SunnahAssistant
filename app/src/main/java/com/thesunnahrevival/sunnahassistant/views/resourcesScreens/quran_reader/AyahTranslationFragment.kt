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
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.thesunnahrevival.sunnahassistant.data.model.Translation
import com.thesunnahrevival.sunnahassistant.theme.SunnahAssistantTheme
import com.thesunnahrevival.sunnahassistant.viewmodels.AyahTranslationViewModel
import com.thesunnahrevival.sunnahassistant.viewmodels.SunnahAssistantViewModel
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
                            SheetContent(
                                it,
                                viewModel.translations.collectAsState(initial = emptyList()).value,
                                viewModel.selectedTranslations.collectAsState().value,
                                { mainActivityViewModel.nextAyah() },
                                { mainActivityViewModel.previousAyah() },
                                { translation: Translation ->
                                    viewModel.toggleTranslationSelection(
                                        translation
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
        val dialog = super.onCreateDialog(savedInstanceState)

        dialog.setOnShowListener {
            val bottomSheetDialog = dialog as BottomSheetDialog
            val bottomSheet =
                bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.let {
                bottomSheetDialog.window?.setDimAmount(0f)
            }
        }

        return dialog
    }
}