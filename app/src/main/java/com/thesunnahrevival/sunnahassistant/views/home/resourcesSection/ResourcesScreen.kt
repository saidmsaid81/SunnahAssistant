package com.thesunnahrevival.sunnahassistant.views.home.resourcesSection

import android.content.res.Configuration
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.Bookmarks
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.thesunnahrevival.sunnahassistant.R
import com.thesunnahrevival.sunnahassistant.data.model.AdhkaarChapter
import com.thesunnahrevival.sunnahassistant.data.model.ResourceItem
import com.thesunnahrevival.sunnahassistant.data.model.Surah
import com.thesunnahrevival.sunnahassistant.theme.SunnahAssistantTheme
import com.thesunnahrevival.sunnahassistant.utilities.toArabicNumbers
import com.thesunnahrevival.sunnahassistant.viewmodels.ResourcesUIState
import com.thesunnahrevival.sunnahassistant.views.utilities.isArabic
import com.valentinilk.shimmer.shimmer
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ResourcesScreen(
    findNavController: NavController? = null,
    resourcesUIState: ResourcesUIState,
    surahItemOnClick: (surah: Surah) -> Unit = {},
    onSurahPin: ((Int) -> Unit)? = null,
    onQuranBookmarksClick: () -> Unit = {},
    onAdhkaarBookmarksClick: () -> Unit = {},
    adhkaarChapterOnClick: (adhkaarChapter: AdhkaarChapter) -> Unit = {},
    onAdhkaarChapterPin: ((Int) -> Unit)? = null
) {

    val hasFinishedLoading = !resourcesUIState.isLoading
    val surahs = resourcesUIState.surahs
    val lastReadSurah = resourcesUIState.lastReadSurah
    val resourceItemList = resourcesUIState.resourceItems
    val adhkaarChapters = resourcesUIState.adhkaarChapters

    SunnahAssistantTheme {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(start = 16.dp, end = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {

                if (hasFinishedLoading && lastReadSurah != null) {
                    ResourceTitle(title = stringResource(R.string.last_read))

                    SurahItem(lastReadSurah, isArabic(), onSurahPin) {
                        surahItemOnClick(lastReadSurah)
                    }
                }

                ResourceTitle(
                    title = stringResource(R.string.quran),
                    trailingIcon = {
                        IconButton(onClick = onQuranBookmarksClick) {
                            Icon(
                                imageVector = Icons.Outlined.Bookmarks,
                                contentDescription = stringResource(R.string.bookmarks)
                            )
                        }
                    }
                )

                if (hasFinishedLoading) {
                    Column {
                        surahs.forEachIndexed { index, surah ->
                            SurahItem(surah, isArabic(), onSurahPin) {
                                surahItemOnClick(surah)
                            }

                            if (index == surahs.lastIndex) {
                                ResourceCard(
                                    title = stringResource(R.string.more),
                                    subtitle = stringResource(R.string.tap_to_view_all_surahs),
                                    resourceNumber = ""
                                ) {
                                    findNavController?.navigate(R.id.surahList)
                                }
                            }
                        }
                    }
                } else {
                    Column {
                        repeat(5) {
                            ShimmerResourceCard()
                        }
                    }
                }

                ResourceTitle(title = stringResource(R.string.hadith))

                if (hasFinishedLoading) {
                    Column {
                        resourceItemList.forEach { item ->
                            ResourceCard(title = stringResource(item.titleResourceKey), subtitle = stringResource(item.descriptionResourceKey)) {
                                findNavController?.navigate(item.destination)
                            }
                        }
                    }
                } else {
                    Column {
                        ShimmerResourceCard()
                    }
                }

                ResourceTitle(
                    title = stringResource(R.string.adhkaar),
                    trailingIcon = {
                        IconButton(onClick = onAdhkaarBookmarksClick) {
                            Icon(
                                imageVector = Icons.Outlined.Bookmarks,
                                contentDescription = stringResource(R.string.bookmarks)
                            )
                        }
                    }
                )

                if (hasFinishedLoading) {
                    Column {
                        adhkaarChapters.forEachIndexed { index, adhkaarChapter ->
                            AdhkaarChapterItem(adhkaarChapter, isArabic(), onAdhkaarChapterPin) {
                                adhkaarChapterOnClick(adhkaarChapter)
                            }

                            if (index == adhkaarChapters.lastIndex) {
                                ResourceCard(
                                    title = stringResource(R.string.more),
                                    subtitle = stringResource(R.string.tap_to_view_all_adhkaar_chapters),
                                    resourceNumber = ""
                                ) {
                                    findNavController?.navigate(R.id.adhkaarChaptersList)
                                }
                            }
                        }
                    }
                } else {
                    Column {
                        repeat(4) {
                            ShimmerResourceCard()
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SurahItem(
    surah: Surah,
    isArabic: Boolean = false,
    onSurahPin: ((Int) -> Unit)? = null,
    onClick: () -> Unit
) {
    val verseCount = if (isArabic) surah.verseCount.toArabicNumbers() else surah.verseCount
    ResourceCard(
        title = if (isArabic) surah.arabicName else surah.transliteratedName,
        subtitle = if (surah.isMakki) stringResource(
            R.string.makki_verse_count,
            verseCount
        ) else stringResource(R.string.madani_verse_count, verseCount),
        resourceNumber = if (isArabic) surah.id.toArabicNumbers() else surah.id.toString(),
        pageNumber = if (isArabic) surah.startPage.toArabicNumbers() else surah.startPage.toString(),
        isPinned = surah.pinOrder != null,
        onDoubleClick = { onSurahPin?.invoke(surah.id) }
    ) { onClick() }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun AdhkaarChapterItem(
    adhkaarChapter: AdhkaarChapter,
    isArabic: Boolean = false,
    onAdhkaarChapterPin: ((Int) -> Unit)? = null,
    onClick: () -> Unit
) {
    ResourceCard(
        title = adhkaarChapter.chapterName,
        subtitle = adhkaarChapter.categoryName,
        resourceNumber = if (isArabic) adhkaarChapter.chapterId.toArabicNumbers() else adhkaarChapter.chapterId.toString(),
        isPinned = adhkaarChapter.pinOrder != null,
        onDoubleClick = { onAdhkaarChapterPin?.invoke(adhkaarChapter.chapterId) }
    ) { onClick() }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ResourceCard(
    title: String,
    subtitle: String,
    resourceNumber: String? = null,
    pageNumber: String? = null,
    isPinned: Boolean = false,
    onDoubleClick: () -> Unit = {},
    onClick: () -> Unit
) {
    var showPinOverlay by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val overlayAlpha by animateFloatAsState(
        targetValue = if (showPinOverlay) 1f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "pin_overlay_alpha"
    )

    Box {
        Card(
            elevation = 4.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 4.dp)
                .combinedClickable(onDoubleClick = {
                    showPinOverlay = true
                    coroutineScope.launch {
                        delay(1100)
                        showPinOverlay = false
                        delay(300)
                        onDoubleClick()
                    }
                }) {
                    onClick()
                }
        ) {
            Row(
                modifier = Modifier.padding(
                    top = 8.dp,
                    bottom = 8.dp,
                    start = 16.dp,
                    end = 16.dp
                )
            ) {
                if (resourceNumber != null) {
                    Text(
                        text = resourceNumber,
                        style = MaterialTheme.typography.h5,
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .weight(1f)
                    )
                }
                Column(modifier = Modifier.weight(4F)) {
                    Text(
                        text = title,
                        fontSize = 16.sp,
                        style = MaterialTheme.typography.subtitle1,
                        fontWeight = FontWeight.W500
                    )
                    Row {
                        Text(
                            text = subtitle,
                            fontSize = 13.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        if (isPinned) {
                            Icon(
                                imageVector = Icons.Filled.PushPin,
                                contentDescription = "Pinned Surah",
                                modifier = Modifier
                                    .size(size = 12.dp)
                                    .align(Alignment.CenterVertically)
                            )
                        }
                    }
                }
                if (pageNumber != null) {
                    Text(
                        text = pageNumber,
                        style = MaterialTheme.typography.caption,
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                    )
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "",
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .height(36.dp)
                        .width(36.dp)
                )
            }
        }

        if (overlayAlpha > 0f) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .alpha(overlayAlpha)
                    .background(
                        color = Color.Black.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(4.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isPinned) Icons.Outlined.PushPin else Icons.Filled.PushPin,
                    contentDescription = "Pin Action",
                    tint = MaterialTheme.colors.primary,
                    modifier = Modifier.size(48.dp)
                )
            }
        }
    }
}

@Composable
private fun ResourceTitle(
    title: String,
    modifier: Modifier = Modifier,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp, top = 16.dp, start = 8.dp, end = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.body1.copy(
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            ),
        )
        trailingIcon?.invoke()
    }
}


@Composable
fun ShimmerResourceCard() {
    Card(
        elevation = 4.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 4.dp)
            .shimmer()
    ) {
        Row(
            modifier = Modifier.padding(
                top = 8.dp,
                bottom = 8.dp,
                start = 16.dp,
                end = 16.dp
            )
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(
                        Color.Gray.copy(alpha = 0.3f),
                        RoundedCornerShape(4.dp)
                    )
                    .align(Alignment.CenterVertically)
                    .weight(1f)
            )
            
            Column(modifier = Modifier.weight(4F)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(16.dp)
                        .background(
                            Color.Gray.copy(alpha = 0.3f),
                            RoundedCornerShape(4.dp)
                        )
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .height(12.dp)
                        .background(
                            Color.Gray.copy(alpha = 0.3f),
                            RoundedCornerShape(4.dp)
                        )
                )
            }
            
            Box(
                modifier = Modifier
                    .size(width = 20.dp, height = 12.dp)
                    .background(
                        Color.Gray.copy(alpha = 0.3f),
                        RoundedCornerShape(4.dp)
                    )
                    .align(Alignment.CenterVertically)
            )
            
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        Color.Gray.copy(alpha = 0.3f),
                        RoundedCornerShape(4.dp)
                    )
                    .align(Alignment.CenterVertically)
            )
        }
    }
}

@Composable
@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    locale = "en"
)
fun ResourcesScreenPreviewDark() {
    ResourcesScreenPreview(previewAdhkaarChapters())
}

@Composable
@Preview
fun ResourcesScreenPreviewLight() {
    ResourcesScreenPreview(previewAdhkaarChapters())
}

@Composable
@Preview(
    name = "Arabic Dark Mode",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    locale = "ar"
)
fun ResourcesScreenPreviewDarkArabic() {
    ResourcesScreenPreview(previewArabicAdhkaarChapters())
}

@Composable
private fun ResourcesScreenPreview(chapters: List<AdhkaarChapter>) {
    SunnahAssistantTheme {
        ResourcesScreen(
            resourcesUIState = ResourcesUIState(
                isLoading = false,
                surahs = previewSurahs(),
                lastReadSurah = previewSurahs().first(),
                resourceItems = listOf(
                    ResourceItem(
                        id = 1,
                        titleResourceKey = R.string.daily_hadith,
                        descriptionResourceKey = R.string.from_the_sunnah_revival_blog,
                        destination = R.id.dailyHadithFragment
                    )
                ),
                adhkaarChapters = chapters,
                error = null
            ),
            onQuranBookmarksClick = {},
            onAdhkaarBookmarksClick = {}
        )
    }
}

fun previewSurahs() = listOf(
    Surah(1, "سورة الفاتحة", "Suratul Fatiha", true, 7, 1, 1),
    Surah(2, "سورة البقرة", "Suratul Baqarah", false, 286, 2, null),
    Surah(3, "سورة آل عمران", "Suratul Aal-Imran", false, 200, 50, 2),
    Surah(4, "سورة النساء", "Suratul Nisa", false, 176, 77, null),
    Surah(5, "سورة المائدة", "Suratul Maidah", false, 120, 106, null)
)


fun previewArabicAdhkaarChapters() = listOf(
    AdhkaarChapter(134, 1, "ar", "أَذْكَارُ الاسْـتِيقَاظِ مِنَ النَّـومِ", "اليوم و الليلة", 1),
    AdhkaarChapter(135, 2, "ar", "دُعَـاءُ لُبْسِ الثَّـــوْبِ", "البيت و الأهل"),
    AdhkaarChapter(136, 3, "ar", "دُعَـاءُ لُبْسِ الثَّوْبِ الجَــدِيدِ", "البيت و الأهل")
)

fun previewAdhkaarChapters() = listOf(
    AdhkaarChapter(134, 1, "en", "When waking up", "Morning & Evening", 1),
    AdhkaarChapter(135, 2, "en", "When wearing a garment", "Home & Family"),
    AdhkaarChapter(136, 3, "en", "When wearing a new garment", "Home & Family")
)

@Composable
@Preview
fun ShimmerResourceCardPreview() {
    SunnahAssistantTheme {
        Column {
            repeat(3) {
                ShimmerResourceCard()
            }
        }
    }
}

@Composable
@Preview
fun ResourcesScreenLoadingPreview() {
    SunnahAssistantTheme {
        ResourcesScreen(
            resourcesUIState = ResourcesUIState(
                isLoading = true,
                surahs = emptyList(),
                lastReadSurah = null,
                resourceItems = listOf(
                    ResourceItem(
                        id = 1,
                        titleResourceKey = R.string.daily_hadith,
                        descriptionResourceKey = R.string.from_the_sunnah_revival_blog,
                        destination = R.id.dailyHadithFragment
                    )
                ),
                adhkaarChapters = emptyList(),
                error = null
            ),
            onQuranBookmarksClick = {},
            onAdhkaarBookmarksClick = {}
        )
    }
}

