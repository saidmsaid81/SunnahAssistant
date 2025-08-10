package com.thesunnahrevival.sunnahassistant.views.resourcesScreens.quran_reader

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.text.*
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.thesunnahrevival.sunnahassistant.data.model.Translation
import com.thesunnahrevival.sunnahassistant.theme.SunnahAssistantTheme
import com.thesunnahrevival.sunnahassistant.utilities.getLocale
import com.thesunnahrevival.sunnahassistant.viewmodels.PageTranslationViewModel
import com.thesunnahrevival.sunnahassistant.viewmodels.TranslationViewModel
import com.thesunnahrevival.sunnahassistant.views.MainActivity
import com.thesunnahrevival.sunnahassistant.views.SunnahAssistantFragment
import com.thesunnahrevival.sunnahassistant.views.utilities.ArabicTextWithTranslation
import com.thesunnahrevival.sunnahassistant.views.utilities.TranslationText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private val FOOTNOTE_PATTERN = "\\[(\\d+)]".toRegex()

class PageTranslationFragment : SunnahAssistantFragment() {

    private val viewModel: PageTranslationViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        val initialPage = mainActivityViewModel.getCurrentQuranPage()


        mainActivityViewModel.selectedSurah.observe(viewLifecycleOwner) { surah ->
            val locale = context?.getLocale() ?: return@observe


            val title = if (locale.language.equals("ar", ignoreCase = true)) {
                surah.arabicName
            } else {
                surah.transliteratedName
            }
            (activity as? MainActivity)?.supportActionBar?.title = title
        }

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                SunnahAssistantTheme {
                    val pagerState = rememberPagerState(initialPage = 604 - initialPage) { 604 }
                    val currentPage = 604 - pagerState.currentPage
                    val expanded = remember { mutableStateOf(false) }

                    LaunchedEffect(currentPage) {
                        viewModel.setSelectedPage(currentPage)
                        mainActivityViewModel.updateCurrentPage(currentPage)
                    }

                    Surface(
                        modifier = Modifier.fillMaxSize()
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
                            val translationUiState by viewModel.translationUiState.collectAsState(initial = TranslationViewModel.TranslationUiState())
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

                                    val translationTexts = ayahFullDetail.ayahTranslations
                                        .map { (ayahTranslation, translation) ->
                                            TranslationText(
                                                title = translation.name,
                                                text = getTranslationText(ayahTranslation.id, ayahTranslation.text)  { ayahTranslationId, footnoteNumber ->
                                                    viewModel.toggleFootnote(
                                                        ayahTranslationId,
                                                        footnoteNumber
                                                    )
                                                },
                                                footnotes = viewModel.visibleFootnotes
                                                    .filter { footnote -> footnote.key.startsWith("${ayahTranslation.id}-") }
                                                    .map { footnote ->
                                                        buildAnnotatedString {
                                                            withStyle(
                                                                style = SpanStyle(
                                                                    baselineShift = BaselineShift.Superscript,
                                                                    fontSize = 12.sp,
                                                                    color = MaterialTheme.colors.primary
                                                                )
                                                            ) {
                                                                append(footnote.value.number.toString())
                                                            }
                                                            append(" ${footnote.value.text}")
                                                        }
                                                    }
                                            )
                                        }

                                    ArabicTextWithTranslation(
                                        context = requireContext(),
                                        index = index,
                                        sectionMarker = "${ayahFullDetail.surah.id}:${ayahFullDetail.ayah.number}",
                                        arabicText = ayahFullDetail.ayah.arabicText,
                                        translationTexts = translationTexts,
                                        textToShare = getAyahText(ayahFullDetail, selectedTranslations, ayahFullDetail.surah.id.toString()),
                                        bookmarked = ayahFullDetail.ayah.bookmarked
                                    ) {
                                        lifecycleScope.launch(Dispatchers.IO) {
                                            mainActivityViewModel.toggleAyahBookmark(ayahFullDetail.ayah)
                                            viewModel.setSelectedPage(currentPage)
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


    @Composable
    private fun getTranslationText(
        ayahTranslationId: Int,
        text: String,
        onFootnoteClick: (ayahTranslationId: Int, footnoteNumber: Int) -> Unit
    ): AnnotatedString {
        return buildAnnotatedString {
            var lastIndex = 0

            FOOTNOTE_PATTERN.findAll(text).forEach { matchResult ->
                // Add text before the footnote
                append(
                    text.substring(
                        lastIndex,
                        matchResult.range.first
                    )
                )

                withLink(
                    LinkAnnotation.Clickable(
                        tag = "footnote",
                        linkInteractionListener = {
                            onFootnoteClick(
                                ayahTranslationId,
                                matchResult.groupValues[1].toInt()
                            )
                        })
                ) {
                    withStyle(
                        style = SpanStyle(
                            baselineShift = BaselineShift.Superscript,
                            fontSize = 12.sp,
                            color = MaterialTheme.colors.primary
                        )
                    ) {
                        append(matchResult.groupValues[1])
                    }
                }

                lastIndex = matchResult.range.last + 1
            }

            // Append remaining text
            if (lastIndex < text.length) {
                append(text.substring(lastIndex))
            }
        }
    }
}