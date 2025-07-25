package com.thesunnahrevival.sunnahassistant.views.resourcesScreens.quran_reader

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.thesunnahrevival.sunnahassistant.data.model.Translation
import com.thesunnahrevival.sunnahassistant.theme.SunnahAssistantTheme
import com.thesunnahrevival.sunnahassistant.viewmodels.PageTranslationViewModel
import com.thesunnahrevival.sunnahassistant.viewmodels.TranslationViewModel
import com.thesunnahrevival.sunnahassistant.views.SunnahAssistantFragment

class PageTranslationFragment : SunnahAssistantFragment() {

    private val viewModel: PageTranslationViewModel by viewModels()
    private val args: PageTranslationFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        val initialPage = args.pageNumber
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                SunnahAssistantTheme {
                    val pagerState = rememberPagerState(initialPage = 604 - initialPage) { 604 }
                    val currentPage = 604 - pagerState.currentPage
                    val expanded = remember { mutableStateOf(false) }

                    LaunchedEffect(currentPage) {
                        viewModel.setSelectedPage(currentPage)
                    }

                    Surface(modifier = Modifier.fillMaxSize()
                        .clickable(
                            onClick = { expanded.value = false },
                            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                            indication = null
                        )
                    ) {
                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier.fillMaxSize()
                        ) { _ ->
                            val translationUiState by
                                viewModel.translationUiState.collectAsState(initial = TranslationViewModel.TranslationUiState())
                            val allTranslations = translationUiState.allTranslations
                            val selectedTranslations = translationUiState.selectedTranslations
                            val translationsDownloadInProgress = translationUiState.translationsDownloadInProgress
                            val ayahFullDetailsList by viewModel.ayahDetails.collectAsState()

                            LazyColumn(modifier = Modifier.padding(16.dp)) {
                                item {
                                    TranslationDropdown(
                                        allTranslations,
                                        selectedTranslations,
                                        translationsDownloadInProgress,
                                        expanded
                                    ) { translation: Translation ->
                                        viewModel.toggleTranslationSelection(
                                            translation,
                                            translationUiState.selectedTranslations.size
                                        ) {
                                            mainActivityViewModel.refreshSelectedAyahId()
                                        }
                                    }
                                }

                                items(ayahFullDetailsList.size) { index ->
                                    val ayahFullDetail = ayahFullDetailsList[index]

                                    if (index == 0) {
                                        Divider(
                                            color = Color.LightGray,
                                            thickness = 1.dp,
                                            modifier = Modifier.padding(bottom = 16.dp)
                                        )
                                    }

                                    Text(
                                        text = "${ayahFullDetail.surah.id}:${ayahFullDetail.ayah.number}",
                                        style = MaterialTheme.typography.subtitle1,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colors.primary
                                    )
                                    AyahTranslations(
                                        ayahFullDetail,
                                        selectedTranslations,
                                        translationsDownloadInProgress,
                                        viewModel.visibleFootnotes,
                                        { ayahTranslationId, footnoteNumber ->
                                            viewModel.toggleFootnote(
                                                ayahTranslationId,
                                                footnoteNumber
                                            )
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    AyahInteractionRow(
                                        ayahFullDetail, selectedTranslations, context, Modifier
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
}