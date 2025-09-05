package com.thesunnahrevival.sunnahassistant.views.resourcesScreens.quran_reader

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.*
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.thesunnahrevival.sunnahassistant.R
import com.thesunnahrevival.sunnahassistant.data.model.dto.FullAyahDetails
import com.thesunnahrevival.sunnahassistant.data.model.entity.Footnote
import com.thesunnahrevival.sunnahassistant.data.model.entity.Translation
import com.thesunnahrevival.sunnahassistant.theme.SunnahAssistantTheme
import com.thesunnahrevival.sunnahassistant.utilities.getLocale
import com.thesunnahrevival.sunnahassistant.viewmodels.PageTranslationViewModel
import com.thesunnahrevival.sunnahassistant.viewmodels.TranslationViewModel
import com.thesunnahrevival.sunnahassistant.views.MainActivity
import com.thesunnahrevival.sunnahassistant.views.home.MenuBarFragment
import com.thesunnahrevival.sunnahassistant.views.utilities.ArabicTextWithTranslation
import com.thesunnahrevival.sunnahassistant.views.utilities.ArabicTextWithTranslationShimmer
import com.thesunnahrevival.sunnahassistant.views.utilities.TranslationDropdown
import com.thesunnahrevival.sunnahassistant.views.utilities.TranslationText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val FOOTNOTE_PATTERN = "\\[(\\d+)]".toRegex()

class PageTranslationFragment : MenuBarFragment() {

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
            (activity as? MainActivity)?.supportActionBar?.title = "$title:" + getString(
                R.string.page,
                mainActivityViewModel.getCurrentQuranPage()
            )
        }

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                SunnahAssistantTheme {
                    val pagerState = rememberPagerState(initialPage = 604 - initialPage) { 604 }
                    val currentPage = 604 - pagerState.currentPage
                    val expanded = remember { mutableStateOf(false) }

                    LaunchedEffect(currentPage) {
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
                            beyondViewportPageCount = 1,
                            modifier = Modifier.fillMaxSize()
                        ) { pageIndex ->

                            val pageNumber = 604 - pageIndex

                            val translationUiState by viewModel.translationUiState.collectAsState(initial = TranslationViewModel.TranslationUiState())
                            val allTranslations = translationUiState.allTranslations
                            val selectedTranslations = translationUiState.selectedTranslations
                            val translationsDownloadInProgress = translationUiState.translationsDownloadInProgress
                            var ayahFullDetailsList by remember { mutableStateOf<List<FullAyahDetails>>(emptyList()) }
                            var bookmarksUpdated by remember { mutableStateOf(false) }
                            val settings by mainActivityViewModel.getSettings().observeAsState()

                            LaunchedEffect(pageNumber, translationUiState.allTranslations, translationUiState.selectedTranslations, translationUiState.translationsDownloadInProgress, bookmarksUpdated) {
                                ayahFullDetailsList = withContext(Dispatchers.IO) {
                                    viewModel.getFullAyahDetailsByPageNumber(pageNumber)
                                }
                            }



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
                                        }
                                    }
                                }

                                if (ayahFullDetailsList.isEmpty() || settings == null) {
                                    items(6) { index ->
                                        ArabicTextWithTranslationShimmer(index)
                                    }
                                } else {
                                    items(ayahFullDetailsList.size) { index ->
                                        val ayahFullDetail = ayahFullDetailsList[index]

                                        AyahTranslation(
                                            context = requireContext(),
                                            ayahFullDetail = ayahFullDetail,
                                            index = index,
                                            selectedTranslations = selectedTranslations,
                                            translationsDownloadInProgress = translationsDownloadInProgress,
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
                                                mainActivityViewModel.toggleAyahBookmark(ayahFullDetail.ayah)
                                                bookmarksUpdated = !bookmarksUpdated
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
    }

    override fun onMenuItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.font_settings -> {
                findNavController().navigate(R.id.fontSettingsFragment)
                true
            }
            else -> false
        }
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.page_translation_menu, menu)
    }
}

@Composable
fun AyahTranslation(
    context: Context,
    ayahFullDetail: FullAyahDetails,
    index: Int,
    selectedTranslations: List<Translation>,
    translationsDownloadInProgress: List<Translation>,
    visibleFootnotes: Map<String, Footnote>,
    onFootnoteClick: (ayahTranslationId: Int, footnoteNumber: Int) -> Unit,
    arabicTextFontSize: Int = 18,
    translationTextFontSize: Int = 16,
    footnoteTextFontSize: Int = 12,
    onBookmark: () -> Unit
) {
    val translationTexts = ayahFullDetail.ayahTranslations
        .map { (ayahTranslation, translation) ->
            TranslationText(
                title = translation.name,
                text = getTranslationText(
                    ayahTranslation.id,
                    ayahTranslation.text
                ) { ayahTranslationId, footnoteNumber ->
                    onFootnoteClick(ayahTranslationId, footnoteNumber)
                },
                footnotes = visibleFootnotes
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
        }.toMutableList()

    translationsDownloadInProgress
        .sortedBy { translation -> translation.order }
        .forEach { translation ->
            translationTexts.add(
                TranslationText(
                    title = translation.name,
                    text = getTranslationText(
                        -1,
                        stringResource(R.string.downloading_translation_please_wait, translation.name)
                    ) { _, _ -> },
                    footnotes = emptyList()
                )
            )
        }


    ArabicTextWithTranslation(
        context = context,
        index = index,
        sectionMarker = "${ayahFullDetail.surah.id}:${ayahFullDetail.ayah.number}",
        arabicText = ayahFullDetail.ayah.arabicText,
        translationTexts = translationTexts,
        textToShare = getAyahText(ayahFullDetail, selectedTranslations, ayahFullDetail.surah.id.toString()),
        bookmarked = ayahFullDetail.ayah.bookmarked,
        onBookmarkClick = { onBookmark() },
        arabicTextFontSize = arabicTextFontSize,
        translationTextFontSize = translationTextFontSize,
        footnoteTextFontSize = footnoteTextFontSize
    )
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

private fun getAyahText(
    ayah: FullAyahDetails,
    selectedTranslations: List<Translation>,
    surahNumber: String
): String {
    val selectedTranslationIds = selectedTranslations.map { it.id }.toSet()

    val translations = ayah.ayahTranslations
        .filter { it.translation.id in selectedTranslationIds }
        .joinToString(separator = "") {
            "${it.translation.name} \n" +
                    "${it.ayahTranslation.text} \n\n"
        }

    return "${ayah.surah.transliteratedName} ($surahNumber)\n\n" +
            "Ayah ${ayah.ayah.number}\n" +
            "${ayah.ayah.arabicText}\n\n" +
            translations
}