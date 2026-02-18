package com.thesunnahrevival.sunnahassistant.views.resourcesScreens.adhkaar

import android.content.res.Configuration
import android.os.Bundle
import android.view.*
import android.widget.Toast.LENGTH_LONG
import android.widget.Toast.makeText
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
import androidx.navigation.fragment.findNavController
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.thesunnahrevival.sunnahassistant.R
import com.thesunnahrevival.sunnahassistant.data.model.embedded.AdhkaarChapterWithPinEmbedded
import com.thesunnahrevival.sunnahassistant.data.model.entity.AdhkaarChapter
import com.thesunnahrevival.sunnahassistant.data.repositories.AdhkaarChapterRepository
import com.thesunnahrevival.sunnahassistant.theme.SunnahAssistantTheme
import com.thesunnahrevival.sunnahassistant.viewmodels.AdhkaarChapterListViewModel
import com.thesunnahrevival.sunnahassistant.views.home.MenuBarFragment
import com.thesunnahrevival.sunnahassistant.views.home.resourcesSection.AdhkaarChapterItem
import com.thesunnahrevival.sunnahassistant.views.utilities.isArabic
import kotlinx.coroutines.flow.collectLatest

class AdhkaarChapterListFragment : MenuBarFragment() {
    private val viewModel: AdhkaarChapterListViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        return ComposeView(requireContext()).apply {
            setContent {
                AdhkaarChapterListScreen(
                    adhkaarChaptersWithPin = viewModel.chaptersFlow.collectAsLazyPagingItems(),
                    firstVisiblePosition = viewModel.firstVisiblePosition,
                    onScroll = { index -> viewModel.firstVisiblePosition = index },
                    onAdhkaarChapterClick = { adhkaarChapter ->
                        val action = AdhkaarChapterListFragmentDirections
                            .actionAdhkaarChaptersListToAdhkaarReaderFragment(adhkaarChapter.chapterId)
                        findNavController().navigate(action)
                    },
                    onAdhkaarChapterPin = { chapterId ->
                        viewModel.toggleChapterPin(chapterId) { result ->
                            when (result) {
                                AdhkaarChapterRepository.PinResult.LimitReached -> {
                                    makeText(
                                        requireContext(),
                                        context.getString(R.string.you_can_only_pin_up_to_5_chapters),
                                        LENGTH_LONG
                                    ).show()
                                }
                                else -> {
                                }
                            }
                        }
                    }
                )
            }
        }
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.adhkaar_chapter_list_menu, menu)

        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as androidx.appcompat.widget.SearchView
        searchView.queryHint = getString(R.string.search_chapter_or_category)
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
                findNavController().navigate(R.id.adhkaarBookmarksFragment)
                true
            }
            else -> super.onMenuItemSelected(item)
        }
    }
}
@Composable
private fun AdhkaarChapterListScreen(
    adhkaarChaptersWithPin: LazyPagingItems<AdhkaarChapterWithPinEmbedded>,
    firstVisiblePosition: Int,
    onScroll: (Int) -> Unit,
    onAdhkaarChapterClick: (AdhkaarChapter) -> Unit,
    onAdhkaarChapterPin: ((Int) -> Unit)? = null
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
                        count = adhkaarChaptersWithPin.itemCount,
                        key = { index ->
                            val chapter = adhkaarChaptersWithPin[index]?.toAdhkaarChapter()
                            "adhkaar_chapter_${chapter?.chapterId ?: "null"}_$index"
                        }
                    ) { index ->
                        val chapter = adhkaarChaptersWithPin[index]?.toAdhkaarChapter()
                        chapter?.let {
                            AdhkaarChapterItem(chapter, isArabic(), onAdhkaarChapterPin) { 
                                onAdhkaarChapterClick(chapter) 
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(name = "Light Mode")
@Composable
private fun AdhkaarChapterListPreview() {
    PreviewAdhkaarChapterListScreen()
}

@Preview(
    name = "Dark Mode",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    locale = "en"
)
@Composable
private fun AdhkaarChapterListPreviewDark() {
    PreviewAdhkaarChapterListScreen()
}

@Preview(
    name = "Arabic Dark Mode",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    locale = "ar"
)
@Composable
private fun AdhkaarChapterListPreviewDarkArabic() {
    PreviewAdhkaarChapterListScreen()
}

@Composable
private fun PreviewAdhkaarChapterListScreen() {
    SunnahAssistantTheme {
        Surface {
            Column(modifier = Modifier.padding(top = 16.dp)) {
                LazyColumn {
                    items(previewAdhkaarChapters().size) { index ->
                        val chapter = previewAdhkaarChapters()[index]
                        AdhkaarChapterItem(chapter, false, null) { }
                    }
                }
            }
        }
    }
}

private fun previewAdhkaarChapters() = listOf(
    AdhkaarChapter(134, 1, "en", "When waking up", "Morning & Evening", 1),
    AdhkaarChapter(135, 2, "en", "When wearing a garment", "Home & Family"),
    AdhkaarChapter(136, 3, "en", "When wearing a new garment", "Home & Family"),
    AdhkaarChapter(137, 4, "en", "When undressing", "Home & Family"),
    AdhkaarChapter(138, 5, "en", "When going to the toilet", "Home & Family"),
    AdhkaarChapter(139, 6, "en", "When leaving the toilet", "Home & Family"),
)