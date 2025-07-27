package com.thesunnahrevival.sunnahassistant.views.home.resourcesSection

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Bookmarks
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.thesunnahrevival.sunnahassistant.R
import com.thesunnahrevival.sunnahassistant.data.model.ResourceItem
import com.thesunnahrevival.sunnahassistant.data.model.Surah
import com.thesunnahrevival.sunnahassistant.theme.SunnahAssistantTheme
import com.thesunnahrevival.sunnahassistant.utilities.toArabicNumbers
import com.thesunnahrevival.sunnahassistant.views.utilities.isArabic

@Composable
fun ResourcesScreen(
    findNavController: NavController? = null,
    isDataReady: Boolean = true,
    surahs: List<Surah>,
    lastReadSurah: Surah? = null,
    resourceItemList: List<ResourceItem>,
    surahItemOnClick: (surah: Surah) -> Unit = {},
    onBookmarksClick: () -> Unit = {}
) {

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

                if (isDataReady && lastReadSurah != null) {
                    ResourceTitle(title = stringResource(R.string.last_read))

                    SurahItem(lastReadSurah, isArabic()) {
                        surahItemOnClick(lastReadSurah)
                    }
                }

                ResourceTitle(
                    title = stringResource(R.string.quran),
                    trailingIcon = {
                        IconButton(onClick = onBookmarksClick) {
                            Icon(
                                imageVector = Icons.Outlined.Bookmarks,
                                contentDescription = stringResource(R.string.bookmarks)
                            )
                        }
                    }
                )

                if (isDataReady) {
                    Column {
                        surahs.forEachIndexed { index, surah ->
                            SurahItem(surah, isArabic()) {
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
                    CircularProgressIndicator()
                }

                ResourceTitle(title = stringResource(R.string.hadith))

                Column {
                    resourceItemList.forEach { item ->
                        ResourceCard(title = stringResource(item.titleResourceKey), subtitle = stringResource(item.descriptionResourceKey)) {
                            findNavController?.navigate(item.destination)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SurahItem(surah: Surah, isArabic: Boolean = false, onClick: () -> Unit) {
    val verseCount = if (isArabic) surah.verseCount.toArabicNumbers() else surah.verseCount
    ResourceCard(
        title = if (isArabic) surah.arabicName else surah.transliteratedName,
        subtitle = if (surah.isMakki) stringResource(
            R.string.makki_verse_count,
            verseCount
        ) else stringResource(R.string.madani_verse_count, verseCount),
        resourceNumber = if (isArabic) surah.id.toArabicNumbers() else surah.id.toString(),
        pageNumber = if (isArabic) surah.startPage.toArabicNumbers() else surah.startPage.toString()
    ) { onClick() }
}

@Composable
fun ResourceCard(
    title: String,
    subtitle: String,
    resourceNumber: String? = null,
    pageNumber: String? = null,
    onClick: () -> Unit
) {
    Card(
        elevation = 4.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 4.dp)
            .clickable {
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
                Text(
                    text = subtitle,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
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
@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    locale = "en"
)
fun ResourcesScreenPreviewDark() {
    ResourcesScreenPreview()
}

@Composable
@Preview
fun ResourcesScreenPreviewLight() {
    ResourcesScreenPreview()
}

@Composable
@Preview(
    name = "Arabic Dark Mode",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    locale = "ar"
)
fun ResourcesScreenPreviewDarkArabic() {
    ResourcesScreenPreview()
}

@Composable
private fun ResourcesScreenPreview() {
    SunnahAssistantTheme {
        ResourcesScreen(
            surahs = previewSurahs(),
            resourceItemList = listOf(
                ResourceItem(
                    id = 1,
                    titleResourceKey = R.string.daily_hadith,
                    descriptionResourceKey = R.string.from_the_sunnah_revival_blog,
                    destination = R.id.dailyHadithFragment
                )
            ),
            onBookmarksClick = {}
        )
    }
}

fun previewSurahs() = listOf(
        Surah(1, "سورة الفاتحة", "Suratul Fatiha", true, 7, 1),
        Surah(2, "سورة البقرة", "Suratul Baqarah", false, 286, 2),
        Surah(3, "سورة آل عمران", "Suratul Aal-Imran", false, 200, 50),
        Surah(4, "سورة النساء", "Suratul Nisa", false, 176, 77),
        Surah(5, "سورة المائدة", "Suratul Maidah", false, 120, 106)
    )

