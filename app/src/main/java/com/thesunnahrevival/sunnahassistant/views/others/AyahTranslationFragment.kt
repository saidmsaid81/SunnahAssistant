package com.thesunnahrevival.sunnahassistant.views.others

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.thesunnahrevival.sunnahassistant.theme.SunnahAssistantTheme
import com.thesunnahrevival.sunnahassistant.viewmodels.AyahTranslationViewModel
import com.thesunnahrevival.sunnahassistant.views.adapters.AyahTranslation
import com.thesunnahrevival.sunnahassistant.views.resourcesScreens.quran_reader.SheetContent

class AyahTranslationFragment : BottomSheetDialogFragment() {

    private val viewModel: AyahTranslationViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                SunnahAssistantTheme {
                    Surface {
                        val selectedAyah by viewModel.selectedAyah.collectAsState()
                        selectedAyah?.let { ayah ->
                            SheetContent(
                                ayah,
                                viewModel.translations.collectAsState().value,
                                viewModel.selectedTranslations.collectAsState().value,
                                { viewModel.nextAyah() },
                                { viewModel.previousAyah() },
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


}