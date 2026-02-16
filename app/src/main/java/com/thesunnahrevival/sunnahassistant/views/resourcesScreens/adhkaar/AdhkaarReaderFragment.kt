package com.thesunnahrevival.sunnahassistant.views.resourcesScreens.adhkaar

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Snackbar
import androidx.compose.material.SnackbarDuration
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.thesunnahrevival.sunnahassistant.R
import com.thesunnahrevival.sunnahassistant.data.model.entity.AppSettings
import com.thesunnahrevival.sunnahassistant.theme.SunnahAssistantTheme
import com.thesunnahrevival.sunnahassistant.utilities.getLocale
import com.thesunnahrevival.sunnahassistant.utilities.getSunnahAssistantAppLink
import com.thesunnahrevival.sunnahassistant.utilities.toAnnotatedString
import com.thesunnahrevival.sunnahassistant.viewmodels.AdhkaarViewModel
import com.thesunnahrevival.sunnahassistant.views.MainActivity
import com.thesunnahrevival.sunnahassistant.views.home.MenuBarFragment
import com.thesunnahrevival.sunnahassistant.views.resourcesScreens.ADHKAAR_CHAPTER_ID
import com.thesunnahrevival.sunnahassistant.views.resourcesScreens.ResourcesNextActionFragment
import com.thesunnahrevival.sunnahassistant.views.utilities.ArabicTextWithTranslation
import com.thesunnahrevival.sunnahassistant.views.utilities.ArabicTextWithTranslationShimmer
import com.thesunnahrevival.sunnahassistant.views.utilities.TranslationText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AdhkaarReaderFragment : MenuBarFragment() {

    private val viewModel: AdhkaarViewModel by viewModels()
    private val args: AdhkaarReaderFragmentArgs by navArgs()
    private var currentChapterId: Int = 1
    private var currentChapterName: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)

        val notificationId = args.notificationId

        if (notificationId != -1) {
            val notificationManager =
                requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(notificationId)
        }

        val initialChapterId = args.chapterId
        val scrollToItemId = args.scrollToItemId
        currentChapterId = initialChapterId

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {

                val pagerState = rememberPagerState(initialPage = initialChapterId - 1) { 133 }
                val settings by mainActivityViewModel.getSettings().observeAsState()
                
                val snackbarHostState = remember { SnackbarHostState() }
                val swipeAdhkaarTutorialStatus by viewModel.swipeAdhkaarTutorialStatus().collectAsState(initial = 0)


                LaunchedEffect(pagerState) {
                    snapshotFlow { pagerState.currentPage }.collect { currentPage ->
                        currentChapterId = currentPage + 1
                        val chapterName = withContext(Dispatchers.IO) {
                            viewModel.getChapterNameByChapterId(currentChapterId) ?: ""
                        }

                        withContext(Dispatchers.Main) {
                            currentChapterName = chapterName
                            (activity as? MainActivity)?.supportActionBar?.title = chapterName
                        }

                        if (currentChapterId != initialChapterId && swipeAdhkaarTutorialStatus != 1) {
                            viewModel.setHasSeenSwipeAdhkaarTutorial()
                        }
                    }
                }


                LaunchedEffect(swipeAdhkaarTutorialStatus) {
                    if (snackbarHostState.currentSnackbarData == null && swipeAdhkaarTutorialStatus != 1) {
                        snackbarHostState.showSnackbar(
                            getString(R.string.swipe_to_navigate_between_adhkaar_chapters),
                            getString(R.string.got_it),
                            duration = SnackbarDuration.Indefinite
                        )
                    }
                }


                SunnahAssistantTheme {
                    Scaffold(
                        snackbarHost = {
                            SnackbarHost(
                                hostState = snackbarHostState
                            ) {
                                Snackbar(
                                    snackbarData = it,
                                    backgroundColor = MaterialTheme.colors.primary,
                                    contentColor = MaterialTheme.colors.onPrimary,
                                    actionColor = Color.Black
                                )
                            }
                        },
                        backgroundColor = MaterialTheme.colors.surface
                    ) { paddingValues ->
                        HorizontalPager(
                            state = pagerState,
                            beyondViewportPageCount = 1,
                            modifier = Modifier.padding(paddingValues)
                                .fillMaxHeight()
                                .fillMaxWidth()
                        ) { pageIndex ->

                            AdhkaarReaderPage(pageIndex, scrollToItemId, initialChapterId, settings)
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun AdhkaarReaderPage(
        pageIndex: Int,
        scrollToItemId: Int,
        initialChapterId: Int,
        settings: AppSettings?
    ) {
        val chapterId = pageIndex + 1
        val uiState by viewModel.getAdhkaarItemsByChapterId(chapterId).collectAsState()
        var chapterName by remember { mutableStateOf("") }
        val listState = rememberLazyListState()

        LaunchedEffect(chapterId) {
            chapterName = withContext(Dispatchers.IO) {
                viewModel.getChapterNameByChapterId(chapterId) ?: ""
            }
        }

        LaunchedEffect(chapterId, scrollToItemId) {
            if (scrollToItemId != -1 && chapterId == initialChapterId) {
                while (uiState.adhkaarGroups.isEmpty() || uiState.isLoading) {
                    delay(50)
                }

                val targetIndex = uiState.adhkaarGroups.indexOfFirst { group ->
                    group.itemIds.contains(scrollToItemId)
                }
                if (targetIndex >= 0) {
                    listState.animateScrollToItem(targetIndex)
                }
            }
        }

        LazyColumn(
            modifier = Modifier.padding(16.dp),
            state = listState
        ) {
            if (uiState.isLoading || settings == null) {
                items(6) { index ->
                    ArabicTextWithTranslationShimmer(index)
                }
            } else {
                items(uiState.adhkaarGroups.size) { index ->

                    if (index == 0) {
                        Text(
                            text = chapterName,
                            style = MaterialTheme.typography.h6,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    }

                    AdhkaarGroup(
                        context = requireContext(),
                        uiState = uiState,
                        groupIndex = index,
                        chapterName = chapterName,
                        settings = settings
                    )

                    if (index == (uiState.adhkaarGroups.size - 1)) {
                        uiState.nextAction?.let {
                            Button(
                                onClick = {
                                    val fragment = ResourcesNextActionFragment()
                                    fragment.show(
                                        requireActivity().supportFragmentManager,
                                        "resources_next_action"
                                    )
                                    val bundle = Bundle()
                                    bundle.putInt(ADHKAAR_CHAPTER_ID, chapterId)
                                    fragment.arguments = bundle
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .wrapContentWidth(align = Alignment.End)
                                    .padding(top = 16.dp),
                                elevation = ButtonDefaults.elevation(
                                    defaultElevation = 4.dp,
                                ),
                                shape = MaterialTheme.shapes.medium.copy(all = CornerSize(24.dp))
                            ) {
                                Text(
                                    text = "${getString(R.string.next)}: ${getString(it.actionResId)}",
                                    modifier = Modifier.padding(vertical = 4.dp),
                                    fontSize = 12.sp
                                )
                            }
                        }

                    }

                }
            }
        }
    }

    @Composable
    private fun AdhkaarGroup(
        context: Context,
        uiState: AdhkaarViewModel.AdhkaarUiState,
        groupIndex: Int,
        chapterName: String,
        settings: AppSettings?
    ) {
        val adhkaarGroup = uiState.adhkaarGroups[groupIndex]
        val currentLocale = context.getLocale()
        val isArabicLocale = currentLocale.language == "ar"

        val itemTimes = adhkaarGroup.items.map { item ->
            val textToParse = if (isArabicLocale) {
                item.arabicText
            } else {
                item.englishText
            }
            extractTimesFromText(textToParse)
        }
        val groupedFrequency = if (itemTimes.distinct().size == 1) itemTimes.firstOrNull() else null
        val shareFrequency = groupedFrequency ?: "Multiple Times"

        adhkaarGroup.items.forEachIndexed { itemIndex, adhkaarItem ->
            val translationTexts = if (adhkaarItem.englishText != null) {
                listOf(
                    TranslationText(
                        title = context.getLocale().displayLanguage,
                        text = adhkaarItem.englishText.toAnnotatedString(),
                        footnoteLabel = stringResource(R.string.reference),
                        footnotes = if (itemIndex == (adhkaarGroup.items.size - 1) && adhkaarGroup.reference != null) {
                            listOf(adhkaarGroup.reference.toAnnotatedString())
                        } else {
                            listOf()
                        }
                    )
                )
            } else {
                listOf()
            }

            val sectionMarker = groupedFrequency ?: itemTimes[itemIndex]
            val shouldShowGroupActions = itemIndex == (adhkaarGroup.items.size - 1)
            val shouldShowSectionMarker = groupedFrequency == null || itemIndex == 0

            ArabicTextWithTranslation(
                context = requireContext(),
                index = groupIndex,
                sectionMarker = sectionMarker,
                arabicText = adhkaarItem.arabicText ?: "",
                translationTexts = translationTexts,
                textToShare = getGroupShareText(
                    chapterName = chapterName,
                    times = shareFrequency,
                    group = adhkaarGroup
                ),
                bookmarked = adhkaarGroup.bookmarked,
                onBookmarkClick = {
                    viewModel.setBookmarks(
                        items = adhkaarGroup.items,
                        shouldBookmark = !adhkaarGroup.bookmarked
                    )
                },
                arabicTextFontSize = settings?.arabicTextFontSize ?: 18,
                translationTextFontSize = settings?.translationTextFontSize ?: 16,
                footnoteTextFontSize = settings?.footnoteTextFontSize ?: 12,
                showTopDivider = groupIndex > 0 && itemIndex == 0,
                showSectionMarker = shouldShowSectionMarker,
                showActions = shouldShowGroupActions
            )
        }
    }

    @Composable
    private fun getGroupShareText(
        chapterName: String,
        times: String,
        group: AdhkaarViewModel.AdhkaarDisplayGroup
    ): String {
        val referenceText = group.reference ?: ""
        val content = buildString {
            group.items.forEachIndexed { index, item ->
                item.arabicText?.takeIf { text -> text.isNotBlank() }?.let { arabic ->
                    appendLine(arabic)
                    appendLine()
                }
                item.englishText?.takeIf { text -> text.isNotBlank() }?.let { english ->
                    appendLine(english)
                    appendLine()
                }
                if (index < group.items.size - 1) {
                    appendLine()
                }
            }
        }.trim()

        return "$chapterName ($times)\n\n" +
                "$content\n\n" +
                stringResource(R.string.reference) + ": $referenceText"
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


    override fun onMenuItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.font_settings -> {
                findNavController().navigate(R.id.fontSettingsFragment)
                true
            }
            R.id.share_all -> {
                shareAllAdhkaar()
                true
            }
            else -> false
        }
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.adhkaar_reader_menu, menu)
    }

    private fun shareAllAdhkaar() {
        lifecycleScope.launch {
            try {
                val adhkaarGroups = viewModel.getAdhkaarItemsByChapterId(currentChapterId)
                    .filter { !it.isLoading && it.adhkaarGroups.isNotEmpty() }
                    .first()
                    .adhkaarGroups

                if (adhkaarGroups.isEmpty()) {
                    return@launch
                }

                val shareText = StringBuilder().apply {
                    appendLine(currentChapterName)
                    appendLine("=".repeat(currentChapterName.length))
                    appendLine()

                    adhkaarGroups.forEachIndexed { index, group ->
                        val currentLocale = requireContext().getLocale()
                        val times = group.items.map { item ->
                            val textToParse = if (currentLocale.language == "ar") {
                                item.arabicText
                            } else {
                                item.englishText
                            }
                            extractTimesFromText(textToParse)
                        }
                        val groupedFrequency = if (times.distinct().size == 1) times.firstOrNull() else null

                        val frequencyLabel = groupedFrequency ?: "Multiple Times"
                        appendLine("${if (adhkaarGroups.size > 1) "${index + 1}." else ""} ($frequencyLabel)")
                        appendLine()
                        group.items.forEach { item ->
                            item.arabicText?.let {
                                appendLine(it)
                                appendLine()
                            }
                            item.englishText?.let {
                                appendLine(it)
                                appendLine()
                            }
                        }
                        group.reference?.let {
                            appendLine("${getString(R.string.reference)}: $it")
                            appendLine()
                        }
                        appendLine("---")
                        appendLine()
                    }

                    appendLine()
                    appendLine(getString(R.string.app_promotional_message, getSunnahAssistantAppLink(
                        utmSource = "app",
                        utmMedium = "share", 
                        utmCampaign = "adhkaar_share"
                    )))
                }.toString()

                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, shareText)
                    putExtra(Intent.EXTRA_SUBJECT, currentChapterName)
                }

                startActivity(Intent.createChooser(shareIntent, getString(R.string.share_via)))

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
