package com.thesunnahrevival.sunnahassistant.views.resourcesScreens.quran_reader

import android.app.Dialog
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
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.thesunnahrevival.sunnahassistant.theme.SunnahAssistantTheme
import com.thesunnahrevival.sunnahassistant.viewmodels.AyahTranslationViewModel
import com.thesunnahrevival.sunnahassistant.viewmodels.SunnahAssistantViewModel
import com.thesunnahrevival.sunnahassistant.views.adapters.AyahTranslation

class AyahTranslationFragment : BottomSheetDialogFragment() {

    private val viewModel: AyahTranslationViewModel by viewModels()
    private val mainActivityViewModel: SunnahAssistantViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val ayahId = mainActivityViewModel.selectedAyah.value?.id
        if (ayahId != null) {
            viewModel.getAyah(ayahId)
                ?.let { mainActivityViewModel.setSelectedAyah(it) }
        }
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                SunnahAssistantTheme {
                    Surface {
                        val selectedAyah by mainActivityViewModel.selectedAyah.collectAsState()
                        selectedAyah?.let { ayah ->
                            SheetContent(
                                ayah,
                                viewModel.translations.collectAsState().value,
                                viewModel.selectedTranslations.collectAsState().value,
                                { mainActivityViewModel.nextAyah() },
                                { mainActivityViewModel.previousAyah() },
                                { translation: AyahTranslation ->
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