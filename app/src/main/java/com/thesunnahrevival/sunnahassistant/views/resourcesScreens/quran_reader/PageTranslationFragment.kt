package com.thesunnahrevival.sunnahassistant.views.resourcesScreens.quran_reader

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Divider
import androidx.compose.material.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import androidx.fragment.app.viewModels
import com.thesunnahrevival.sunnahassistant.theme.SunnahAssistantTheme
import com.thesunnahrevival.sunnahassistant.viewmodels.AyahTranslationViewModel
import com.thesunnahrevival.sunnahassistant.views.SunnahAssistantFragment
import com.thesunnahrevival.sunnahassistant.views.adapters.AyahTranslation

class PageTranslationFragment : SunnahAssistantFragment() {

    private val viewModel: AyahTranslationViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                SunnahAssistantTheme {
                    val selectedTranslations by viewModel.selectedTranslations.collectAsState()
                    val allTranslations by viewModel.translations.collectAsState()

                    Surface {
                        LazyColumn(modifier = Modifier.padding(16.dp)) {
                            item {
                                TranslationDropdown(
                                    selectedTranslations,
                                    allTranslations
                                ) { translation: AyahTranslation ->
                                    viewModel.toggleTranslationSelection(translation)
                                }
                            }

                            items(viewModel.ayahs.size) { index ->
                                val ayah = viewModel.ayahs[index]

                                if (index == 0) {
                                    Divider(
                                        color = Color.LightGray,
                                        thickness = 1.dp,
                                        modifier = Modifier.padding(bottom = 16.dp)
                                    )
                                }

                                AyahTranslations(
                                    ayah,
                                    selectedTranslations,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                AyahInteractionRow(
                                    ayah, selectedTranslations, context, Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 16.dp)
                                )

                                Divider(
                                    color = Color.LightGray,
                                    thickness = 1.dp,
                                    modifier = Modifier.padding(bottom = 16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}