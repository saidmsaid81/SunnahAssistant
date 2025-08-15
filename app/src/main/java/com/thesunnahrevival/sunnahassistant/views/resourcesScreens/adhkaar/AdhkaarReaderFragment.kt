package com.thesunnahrevival.sunnahassistant.views.resourcesScreens.adhkaar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.thesunnahrevival.sunnahassistant.R
import com.thesunnahrevival.sunnahassistant.theme.SunnahAssistantTheme
import com.thesunnahrevival.sunnahassistant.utilities.getLocale
import com.thesunnahrevival.sunnahassistant.utilities.toAnnotatedString
import com.thesunnahrevival.sunnahassistant.viewmodels.AdhkaarViewModel
import com.thesunnahrevival.sunnahassistant.views.MainActivity
import com.thesunnahrevival.sunnahassistant.views.SunnahAssistantFragment
import com.thesunnahrevival.sunnahassistant.views.utilities.ArabicTextWithTranslation
import com.thesunnahrevival.sunnahassistant.views.utilities.ArabicTextWithTranslationShimmer
import com.thesunnahrevival.sunnahassistant.views.utilities.TranslationText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AdhkaarReaderFragment : SunnahAssistantFragment() {

    private val viewModel: AdhkaarViewModel by viewModels()
    private val args: AdhkaarReaderFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)

        val initialChapterId = args.chapterId

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {

                val pagerState = rememberPagerState(initialPage = initialChapterId - 1) { 133 }

                LaunchedEffect(pagerState) {
                    snapshotFlow { pagerState.currentPage }.collect { currentPage ->
                        val currentChapterId = currentPage + 1
                        val chapterName = withContext(Dispatchers.IO) {
                            viewModel.getChapterNameByChapterId(currentChapterId) ?: ""
                        }

                        withContext(Dispatchers.Main) {
                            (activity as? MainActivity)?.supportActionBar?.title = chapterName
                        }
                    }
                }


                SunnahAssistantTheme {
                    Surface {
                        HorizontalPager(
                            state = pagerState,
                            beyondViewportPageCount = 1
                        ) { pageIndex ->

                            val chapterId = pageIndex + 1
                            val uiState by viewModel.getAdhkaarItemsByChapterId(chapterId).collectAsState()
                            var chapterName by remember { mutableStateOf("") }

                            LaunchedEffect(chapterId) {
                                chapterName = withContext(Dispatchers.IO) {
                                    viewModel.getChapterNameByChapterId(chapterId) ?: ""
                                }
                            }

                            LazyColumn(modifier = Modifier.padding(16.dp)) {
                                if (uiState.isLoading) {
                                    items(6) { index ->
                                        ArabicTextWithTranslationShimmer(index)
                                    }
                                } else {
                                    items(uiState.adhkaarItems.size) { index ->

                                        if (index == 0) {
                                            Text(
                                                text = chapterName,
                                                style = MaterialTheme.typography.h6,
                                                modifier = Modifier.padding(bottom = 16.dp)
                                            )
                                        }

                                        val adhkaarItem = uiState.adhkaarItems[index]

                                        val translationTexts = if (adhkaarItem.englishText != null) {
                                            listOf(
                                                TranslationText(
                                                    title = context.getLocale().displayLanguage,
                                                    text = adhkaarItem.englishText.toAnnotatedString(),
                                                    footnoteLabel = stringResource(R.string.reference),
                                                    footnotes = if (adhkaarItem.reference != null) listOf((adhkaarItem.reference).toAnnotatedString()) else listOf()
                                                )
                                            )
                                        } else {
                                            listOf()
                                        }

                                        val currentLocale = context.getLocale()
                                        val isArabicLocale = currentLocale.language == "ar"

                                        val textToParse = if (isArabicLocale) {
                                            adhkaarItem.arabicText
                                        } else {
                                            adhkaarItem.englishText
                                        }

                                        val sectionMarker = extractTimesFromText(textToParse)

                                        ArabicTextWithTranslation(
                                            context = requireContext(),
                                            index = index,
                                            sectionMarker = sectionMarker,
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

    private fun extractTimesFromText(text: String?): String {
        if (text.isNullOrBlank()) return "1 Time"

        fun wordToNumber(word: String): Int? {
            return when (word.lowercase()) {
                "once", "one" -> 1
                "twice", "two" -> 2
                "thrice", "three" -> 3
                "four" -> 4
                "five" -> 5
                "six" -> 6
                "seven" -> 7
                "eight" -> 8
                "nine" -> 9
                "ten" -> 10
                "eleven" -> 11
                "twelve" -> 12
                "thirteen" -> 13
                "fourteen" -> 14
                "fifteen" -> 15
                "sixteen" -> 16
                "seventeen" -> 17
                "eighteen" -> 18
                "nineteen" -> 19
                "twenty" -> 20
                "twenty-one" -> 21
                "twenty-two" -> 22
                "twenty-three" -> 23
                "twenty-four" -> 24
                "twenty-five" -> 25
                "twenty-six" -> 26
                "twenty-seven" -> 27
                "twenty-eight" -> 28
                "twenty-nine" -> 29
                "thirty" -> 30
                "thirty-one" -> 31
                "thirty-two" -> 32
                "thirty-three" -> 33
                "thirty-four" -> 34
                "thirty-five" -> 35
                "forty" -> 40
                "fifty" -> 50
                "sixty" -> 60
                "seventy" -> 70
                "eighty" -> 80
                "ninety" -> 90
                "hundred" -> 100
                else -> word.toIntOrNull()
            }
        }

        fun arabicWordToNumber(word: String): Int? {
            return when (word) {
                "واحدة", "واحد" -> 1
                "اثنان", "اثنتان" -> 2
                "ثلاث", "ثلاثة" -> 3
                "أربع", "أربعة" -> 4
                "خمس", "خمسة" -> 5
                "ست", "ستة" -> 6
                "سبع", "سبعة" -> 7
                "ثمان", "ثمانية" -> 8
                "تسع", "تسعة" -> 9
                "عشر", "عشرة" -> 10
                else -> word.toIntOrNull()
            }
        }

        val englishPatterns = listOf(
            Regex("\\b(\\d+) times?\\b", RegexOption.IGNORE_CASE),
            Regex(
                "\\b(thirty-three|thirty-two|thirty-one|twenty-nine|twenty-eight|twenty-seven|twenty-six|twenty-five|twenty-four|twenty-three|twenty-two|twenty-one) times?\\b",
                RegexOption.IGNORE_CASE
            ),
            Regex("\\b(once|one time?)s?\\b", RegexOption.IGNORE_CASE),
            Regex("\\b(twice|two times?)\\b", RegexOption.IGNORE_CASE),
            Regex("\\b(thrice|three times?)\\b", RegexOption.IGNORE_CASE),
            Regex(
                "\\b(eleven|twelve|thirteen|fourteen|fifteen|sixteen|seventeen|eighteen|nineteen|twenty|thirty|forty|fifty|sixty|seventy|eighty|ninety|hundred) times?\\b",
                RegexOption.IGNORE_CASE
            ),
            Regex("\\b(one|two|three|four|five|six|seven|eight|nine|ten) times?\\b", RegexOption.IGNORE_CASE)
        )

        val arabicPatterns = listOf(
            Regex("مرة\\s*(واحدة)?|مرة"),
            Regex("مرتان|مرتين"),
            Regex("(\\d+)\\s*مرات?"),
            Regex("(أربع|خمس|ست|سبع|ثمان|تسع|عشر|ثلاث)\\s*مرات?")
        )

        for (pattern in englishPatterns) {
            val match = pattern.find(text)
            if (match != null) {
                val matchedText = match.groupValues[1].lowercase()
                val number = when {
                    matchedText.matches("\\d+".toRegex()) -> matchedText.toIntOrNull() ?: 1
                    matchedText == "once" -> 1
                    matchedText == "twice" -> 2
                    matchedText == "thrice" -> 3
                    else -> wordToNumber(matchedText.replace(" times?".toRegex(), "").trim()) ?: 1
                }

                return if (number == 1) "1 Time" else "$number Times"
            }
        }

        for (pattern in arabicPatterns) {
            val match = pattern.find(text)
            if (match != null) {
                val matchedText = match.value
                val number = when {
                    matchedText.contains("مرة") && !matchedText.contains("مرتان") && !matchedText.contains("مرتين") -> 1
                    matchedText.contains("مرتان") || matchedText.contains("مرتين") -> 2
                    matchedText.contains("ثلاث") -> 3
                    else -> {
                        val arabicNumber = Regex("(\\d+)").find(matchedText)?.value?.toIntOrNull()
                        val arabicWord = Regex("(أربع|خمس|ست|سبع|ثمان|تسع|عشر)").find(matchedText)?.value
                        arabicNumber ?: arabicWordToNumber(arabicWord ?: "") ?: 1
                    }
                }

                return if (number == 1) "1 Time" else "$number Times"
            }
        }

        return "1 Time"
    }
}