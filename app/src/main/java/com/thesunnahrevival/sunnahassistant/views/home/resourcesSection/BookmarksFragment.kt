package com.thesunnahrevival.sunnahassistant.views.home.resourcesSection

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.thesunnahrevival.sunnahassistant.R
import com.thesunnahrevival.sunnahassistant.data.model.Ayah
import com.thesunnahrevival.sunnahassistant.data.model.AyahWithSurah
import com.thesunnahrevival.sunnahassistant.data.model.Surah
import com.thesunnahrevival.sunnahassistant.theme.SunnahAssistantTheme
import com.thesunnahrevival.sunnahassistant.utilities.toArabicNumbers
import com.thesunnahrevival.sunnahassistant.viewmodels.BookmarksViewModel
import com.thesunnahrevival.sunnahassistant.views.home.MenuBarFragment
import com.thesunnahrevival.sunnahassistant.views.utilities.isArabic
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOf

class BookmarksFragment : MenuBarFragment() {

    private val bookmarksViewModel: BookmarksViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)

        return ComposeView(requireContext()).apply {
            setContent {
                val bookmarkedAyahs =
                    bookmarksViewModel.getBookmarkedAyahs().collectAsLazyPagingItems()

                BookmarksScreen(
                    bookmarkedAyahs = bookmarkedAyahs,
                    firstVisiblePosition = bookmarksViewModel.firstVisiblePosition,
                    onAyahClick = { ayahWithSurah ->
                        mainActivityViewModel.setSelectedAyahId(ayahWithSurah.ayah.id)
                        findNavController().navigate(R.id.quranReaderFragment)
                    },
                    onScroll = { index -> bookmarksViewModel.firstVisiblePosition = index }
                )
            }
        }
    }
}

@Composable
fun BookmarksScreen(
    bookmarkedAyahs: LazyPagingItems<AyahWithSurah>,
    firstVisiblePosition: Int = 0,
    onAyahClick: (ayah: AyahWithSurah) -> Unit = {},
    onScroll: (Int) -> Unit = {}
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf(
        stringResource(R.string.ayahs),
        stringResource(R.string.pages)
    )

    SunnahAssistantTheme {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(start = 16.dp, end = 16.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Tab Row
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = MaterialTheme.colors.surface
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = { Text(title) }
                        )
                    }
                }

                // Tab Content
                when (selectedTabIndex) {
                    0 -> AyahBookmarksTab(
                        bookmarkedAyahs = bookmarkedAyahs,
                        firstVisiblePosition = firstVisiblePosition,
                        onAyahClick = onAyahClick,
                        onScroll = onScroll
                    )

                    1 -> PageBookmarksTab()
                }
            }
        }
    }
}

@Composable
private fun AyahBookmarksTab(
    bookmarkedAyahs: LazyPagingItems<AyahWithSurah>,
    firstVisiblePosition: Int = 0,
    onAyahClick: (ayahWithSurah: AyahWithSurah) -> Unit,
    onScroll: (Int) -> Unit
) {
    val lazyListState = rememberLazyListState(initialFirstVisibleItemIndex = firstVisiblePosition)

    LaunchedEffect(lazyListState) {
        snapshotFlow {
            lazyListState.firstVisibleItemIndex
        }.collectLatest { index ->
            onScroll(index)
        }
    }

    if (bookmarkedAyahs.itemCount == 0) {
        Text(
            text = stringResource(R.string.no_bookmarked_ayahs),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.body2
        )
    } else {
        LazyColumn(
            state = lazyListState,
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 16.dp)
        ) {
            items(
                count = bookmarkedAyahs.itemCount,
                key = { index ->
                    val ayahWithSurah = bookmarkedAyahs[index]
                    "surah_${ayahWithSurah?.ayah?.id ?: "null"}_$index"
                }
            ) { index ->
                val ayahWithSurah = bookmarkedAyahs[index]
                if (ayahWithSurah != null) {
                    AyahBookmarkItem(
                        ayah = ayahWithSurah.ayah,
                        surah = ayahWithSurah.surah,
                        isArabic = isArabic(),
                        onClick = { onAyahClick(ayahWithSurah) }
                    )
                }
            }
        }
    }
}

@Composable
private fun PageBookmarksTab() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Page bookmarks coming soon...",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.body2
        )
    }
}

@Composable
private fun AyahBookmarkItem(
    ayah: Ayah,
    surah: Surah,
    isArabic: Boolean,
    onClick: () -> Unit
) {

    ResourceCard(
        title = if (isArabic) surah.arabicName else surah.transliteratedName,
        subtitle = stringResource(R.string.ayah_number, ayah.number),
        resourceNumber = if (isArabic) surah.id.toArabicNumbers() else surah.id.toString()
    ) {
        onClick()
    }
}

@Composable
@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    locale = "en"
)
fun BookmarksScreenPreviewDark() {
    BookmarksScreenPreview(previewAyahBookmarksData())
}

@Composable
@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    locale = "en"
)
fun BookmarksScreenPreviewLight() {
    BookmarksScreenPreview(previewAyahBookmarksData())
}

@Composable
@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    locale = "ar"
)
fun BookmarksScreenPreviewArabic() {
    BookmarksScreenPreview(previewAyahBookmarksData())
}

@Composable
private fun BookmarksScreenPreview(previewData: Flow<PagingData<AyahWithSurah>>) {
    BookmarksScreen(
        bookmarkedAyahs = previewData.collectAsLazyPagingItems(),

        )
}

fun previewAyahBookmarksData(): Flow<PagingData<AyahWithSurah>> {
    val previewAyahs = listOf(
        AyahWithSurah(
            ayah = Ayah(1, 1, 1, "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ", true),
            surah = Surah(1, "سورة الفاتحة", "Suratul Fatiha", true, 7, 1)
        ),
        AyahWithSurah(
            ayah = Ayah(255, 255, 2, "اللَّهُ لَا إِلَٰهَ إِلَّا هُوَ الْحَيُّ الْقَيُّومُ", true),
            surah = Surah(2, "سورة البقرة", "Suratul Baqarah", false, 286, 2)
        )
    )

    return flowOf(PagingData.from(previewAyahs))
}