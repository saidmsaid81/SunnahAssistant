package com.thesunnahrevival.sunnahassistant.views.resourcesScreens.adhkaar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.thesunnahrevival.sunnahassistant.R
import com.thesunnahrevival.sunnahassistant.theme.SunnahAssistantTheme
import com.thesunnahrevival.sunnahassistant.viewmodels.AdhkaarViewModel
import com.thesunnahrevival.sunnahassistant.views.SunnahAssistantFragment
import com.thesunnahrevival.sunnahassistant.views.utilities.ArabicTextWithTranslation
import com.thesunnahrevival.sunnahassistant.views.utilities.ArabicTextWithTranslationShimmer
import com.thesunnahrevival.sunnahassistant.views.utilities.TranslationText
import com.valentinilk.shimmer.shimmer

class AdhkaarReaderFragment : SunnahAssistantFragment() {

    private val viewModel: AdhkaarViewModel by viewModels()
    private val args: AdhkaarReaderFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)

        val chapterId = args.chapterId


        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {

                val pagerState = rememberPagerState(initialPage = chapterId) { 133 }
                val uiState by viewModel.getAdhkaarItemsByChapterId(pagerState.currentPage).collectAsState()

                SunnahAssistantTheme {
                    Surface {
                        if (uiState.isLoading) {
                            LazyColumn(modifier = Modifier.padding(16.dp).shimmer()) {
                                items(6) { index ->
                                    ArabicTextWithTranslationShimmer(index)
                                }
                            }
                        } else {
                            HorizontalPager(state = pagerState) { _ ->
                                LazyColumn(modifier = Modifier.padding(16.dp)) {
                                    items(uiState.adhkaarItems.size) { index ->
                                        val adhkaarItem = uiState.adhkaarItems[index]

                                        val translationTexts = if (adhkaarItem.englishText != null) {
                                            listOf(
                                                TranslationText(
                                                    title = LocalConfiguration.current.locales[0].displayLanguage,
                                                    text = adhkaarItem.englishText,
                                                    footnoteLabel = stringResource(R.string.reference),
                                                    footnotes = if (adhkaarItem.reference != null) listOf(adhkaarItem.reference) else listOf()
                                                )
                                            )
                                        } else {
                                            listOf()
                                        }

                                        ArabicTextWithTranslation(
                                            context = requireContext(),
                                            index = index,
                                            sectionMarker = adhkaarItem.itemId.toString(),
                                            arabicText = adhkaarItem.arabicText ?: "",
                                            translationTexts = translationTexts,
                                            textToShare = "${adhkaarItem.arabicText}\n\n" +
                                                    "${adhkaarItem.englishText}\n\n" +
                                                    stringResource(R.string.reference) + ": ${adhkaarItem.reference}"
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
}