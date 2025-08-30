package com.thesunnahrevival.sunnahassistant.views.resourcesScreens.quran_reader

import android.content.res.Configuration
import android.os.Bundle
import android.view.*
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.thesunnahrevival.sunnahassistant.R
import com.thesunnahrevival.sunnahassistant.data.model.Surah
import com.thesunnahrevival.sunnahassistant.theme.SunnahAssistantTheme
import com.thesunnahrevival.sunnahassistant.viewmodels.SurahListViewModel
import com.thesunnahrevival.sunnahassistant.views.home.MenuBarFragment
import com.thesunnahrevival.sunnahassistant.views.home.resourcesSection.SurahItem
import com.thesunnahrevival.sunnahassistant.views.utilities.isArabic
import kotlinx.coroutines.flow.collectLatest

class SurahListFragment : MenuBarFragment() {
    private val viewModel: SurahListViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        return ComposeView(requireContext()).apply {
            setContent {
                SurahListScreen(
                    surahs = viewModel.surahsFlow.collectAsLazyPagingItems(),
                    firstVisiblePosition = viewModel.firstVisiblePosition,
                    onScroll = { index -> viewModel.firstVisiblePosition = index },
                    onSurahClick = { surah ->
                        findNavController().navigate(
                            R.id.quranReaderFragment
                        )
                        mainActivityViewModel.updateCurrentPage(surah.startPage)
                    },
                    onSurahPinClick = { surahId ->
                        viewModel.toggleSurahPin(surahId) { result ->
                            when (result) {
                                com.thesunnahrevival.sunnahassistant.data.repositories.SurahRepository.PinResult.LimitReached -> {
                                    // Show toast message for limit reached
                                    android.widget.Toast.makeText(
                                        requireContext(),
                                        getString(R.string.pinned_surahs_limit_reached),
                                        android.widget.Toast.LENGTH_LONG
                                    ).show()
                                }
                                else -> {}
                            }
                        }
                    }
                )
            }
        }
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.surah_list_menu, menu)

        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as androidx.appcompat.widget.SearchView
        searchView.queryHint = getString(R.string.search_surah)
        searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                viewModel.setSearchQuery(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.setSearchQuery(newText)
                return true
            }
        })
    }

    override fun onMenuItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.bookmarks -> {
                findNavController().navigate(R.id.bookmarksFragment)
                true
            }
            else -> super.onMenuItemSelected(item)
        }
    }
}

@Composable
private fun SurahListScreen(
    surahs: LazyPagingItems<Surah>,
    firstVisiblePosition: Int,
    onScroll: (Int) -> Unit,
    onSurahClick: (Surah) -> Unit,
    onSurahPinClick: ((Int) -> Unit)? = null
) {
    SunnahAssistantTheme {
        Surface {
            Column(modifier = Modifier.padding(top = 16.dp)) {
                val lazyListState =
                    rememberLazyListState(initialFirstVisibleItemIndex = firstVisiblePosition)

                LaunchedEffect(lazyListState) {
                    snapshotFlow {
                        lazyListState.firstVisibleItemIndex
                    }.collectLatest { index ->
                        onScroll(index)
                    }
                }

                LazyColumn(state = lazyListState) {
                    items(
                        count = surahs.itemCount,
                        key = { index ->
                            val surah = surahs[index]
                            "surah_${surah?.id ?: "null"}_$index"
                        }
                    ) { index ->
                        val surah = surahs[index]
                        surah?.let {
                            SurahItem(surah, isArabic(), onSurahPinClick) { onSurahClick(surah) }
                        }
                    }
                }
            }
        }
    }
}

@Preview(name = "Light Mode")
@Composable
private fun SurahListPreview() {
    PreviewSurahListScreen()
}

@Preview(
    name = "Dark Mode",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    locale = "en"
)
@Composable
private fun SurahListPreviewDark() {
    PreviewSurahListScreen()
}

@Preview(
    name = "Arabic Dark Mode",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    locale = "ar"
)
@Composable
private fun SurahListPreviewDarkArabic() {
    PreviewSurahListScreen()
}

@Composable
private fun PreviewSurahListScreen() {
    SunnahAssistantTheme {
        Surface {
            Column(modifier = Modifier.padding(top = 16.dp)) {
                LazyColumn {
                    items(previewSurahs().size) { index ->
                        val surah = previewSurahs()[index]
                        SurahItem(surah, false, { }) { }
                    }
                }
            }
        }
    }
}

private fun previewSurahs() = listOf(
    Surah(1, "سورة الفاتحة", "Suratul Fatiha", true, 7, 1, 1),
    Surah(2, "سورة البقرة", "Suratul Baqarah", false, 286, 2, null),
    Surah(3, "سورة آل عمران", "Suratul Aal-Imran", false, 200, 50, 2),
    Surah(4, "سورة النساء", "Suratul Nisa", false, 176, 77, null),
    Surah(5, "سورة المائدة", "Suratul Maidah", false, 120, 106, null),
    Surah(6, "سورة الأنعام", "Suratul An'am", true, 165, 128, null)
)