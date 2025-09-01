package com.thesunnahrevival.sunnahassistant.views.resourcesScreens.quran_reader

import android.content.res.Configuration
import android.os.Bundle
import android.view.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.thesunnahrevival.sunnahassistant.R
import com.thesunnahrevival.sunnahassistant.data.model.*
import com.thesunnahrevival.sunnahassistant.theme.SunnahAssistantTheme
import com.thesunnahrevival.sunnahassistant.utilities.toArabicNumbers
import com.thesunnahrevival.sunnahassistant.viewmodels.BookmarksViewModel
import com.thesunnahrevival.sunnahassistant.views.home.MenuBarFragment
import com.thesunnahrevival.sunnahassistant.views.home.resourcesSection.ResourceCard
import com.thesunnahrevival.sunnahassistant.views.utilities.isArabic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class QuranBookmarksFragment : MenuBarFragment() {

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
                    bookmarksViewModel.bookmarkedAyahsFlow.collectAsLazyPagingItems()
                val bookmarkedPagesWithSurah =
                    bookmarksViewModel.bookmarkedPagesFlow.collectAsLazyPagingItems()

                BookmarksScreen(
                    bookmarkedAyahs = bookmarkedAyahs,
                    bookmarkedPagesWithSurah = bookmarkedPagesWithSurah,
                    firstVisiblePosition = bookmarksViewModel.firstVisiblePosition,
                    onAyahClick = { ayahWithSurah ->
                        lifecycleScope.launch(Dispatchers.IO) {
                            val pageNumber = bookmarksViewModel.getPageNumberByAyahId(ayahWithSurah.ayah.id)
                            withContext(Dispatchers.Main) {
                                pageNumber?.let { mainActivityViewModel.updateCurrentPage(it) }
                                findNavController().navigate(R.id.quranReaderFragment)
                                mainActivityViewModel.setSelectedAyahId(ayahWithSurah.ayah.id)

                            }
                        }

                    },
                    onPageClick = { pageBookmarkWithSurah ->
                        mainActivityViewModel.updateCurrentPage(pageBookmarkWithSurah.pageBookmark.pageNumber)
                        findNavController().navigate(R.id.quranReaderFragment)
                    },
                    onScroll = { index -> bookmarksViewModel.firstVisiblePosition = index }
                )
            }
        }
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.quran_bookmarks_menu, menu)

        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as androidx.appcompat.widget.SearchView
        searchView.queryHint = getString(R.string.search_bookmarks)
        searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                bookmarksViewModel.setSearchQuery(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                bookmarksViewModel.setSearchQuery(newText)
                return true
            }
        })
    }

    override fun onMenuItemSelected(item: MenuItem): Boolean {
        return super.onMenuItemSelected(item)
    }
}

@Composable
fun BookmarksScreen(
    bookmarkedAyahs: LazyPagingItems<AyahWithSurah>,
    bookmarkedPagesWithSurah: LazyPagingItems<PageBookmarkWithSurah>,
    firstVisiblePosition: Int = 0,
    onAyahClick: (ayah: AyahWithSurah) -> Unit = {},
    onPageClick: (pageBookmarkWithSurah: PageBookmarkWithSurah) -> Unit = {},
    onScroll: (Int) -> Unit = {}
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf(
        stringResource(R.string.ayahs),
        stringResource(R.string.pages)
    )

    SunnahAssistantTheme {
        Surface(
            modifier = Modifier.Companion
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(start = 16.dp, end = 16.dp)
        ) {
            Column(modifier = Modifier.Companion.fillMaxSize()) {
                // Tab Row
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    modifier = Modifier.Companion.fillMaxWidth(),
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

                    1 -> PageBookmarksTab(
                        bookmarkedPagesWithSurah = bookmarkedPagesWithSurah,
                        onPageClick = onPageClick
                    )
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
            modifier = Modifier.Companion
                .fillMaxWidth()
                .padding(16.dp),
            textAlign = TextAlign.Companion.Center,
            style = MaterialTheme.typography.body2
        )
    } else {
        LazyColumn(
            state = lazyListState,
            modifier = Modifier.Companion
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
private fun PageBookmarksTab(
    bookmarkedPagesWithSurah: LazyPagingItems<PageBookmarkWithSurah>,
    onPageClick: (pageBookmarkWithSurah: PageBookmarkWithSurah) -> Unit
) {
    if (bookmarkedPagesWithSurah.itemCount == 0) {
        Text(
            text = stringResource(R.string.no_bookmarked_pages),
            modifier = Modifier.Companion
                .fillMaxWidth()
                .padding(16.dp),
            textAlign = TextAlign.Companion.Center,
            style = MaterialTheme.typography.body2
        )
    } else {
        LazyColumn(
            modifier = Modifier.Companion
                .fillMaxSize()
                .padding(top = 16.dp)
        ) {
            items(
                count = bookmarkedPagesWithSurah.itemCount,
                key = { index ->
                    val pageBookmarkWithSurah = bookmarkedPagesWithSurah[index]
                    "page_${pageBookmarkWithSurah?.pageBookmark?.pageNumber ?: "null"}_$index"
                }
            ) { index ->
                val pageBookmarkWithSurah = bookmarkedPagesWithSurah[index]
                if (pageBookmarkWithSurah != null) {
                    PageBookmarkItem(
                        pageBookmarkWithSurah = pageBookmarkWithSurah,
                        isArabic = isArabic(),
                        onClick = { onPageClick(pageBookmarkWithSurah) }
                    )
                }
            }
        }
    }
}

@Composable
private fun PageBookmarkItem(
    pageBookmarkWithSurah: PageBookmarkWithSurah,
    isArabic: Boolean,
    onClick: () -> Unit
) {
    val pageBookmark = pageBookmarkWithSurah.pageBookmark
    val surah = pageBookmarkWithSurah.surah

    ResourceCard(
        title = stringResource(R.string.page_number, pageBookmark.pageNumber),
        subtitle = if (isArabic) surah.arabicName else surah.transliteratedName,
        resourceNumber = if (isArabic) pageBookmark.pageNumber.toArabicNumbers() else pageBookmark.pageNumber.toString()
    ) {
        onClick()
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
        bookmarkedPagesWithSurah = previewPageBookmarksWithSurahData().collectAsLazyPagingItems()
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

    return flowOf(PagingData.Companion.from(previewAyahs))
}

fun previewPageBookmarksWithSurahData(): Flow<PagingData<PageBookmarkWithSurah>> {
    val previewPagesWithSurah = listOf(
        PageBookmarkWithSurah(
            PageBookmark(1, 1),
            Surah(1, "سورة الفاتحة", "Suratul Fatiha", true, 7, 1)
        ),
        PageBookmarkWithSurah(
            PageBookmark(2, 255),
            Surah(2, "سورة البقرة", "Suratul Baqarah", false, 286, 2)
        ),
        PageBookmarkWithSurah(
            PageBookmark(3, 604),
            Surah(114, "سورة الناس", "Suratul Nas", true, 6, 604)
        )
    )

    return flowOf(PagingData.Companion.from(previewPagesWithSurah))
}