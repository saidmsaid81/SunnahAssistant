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
import com.thesunnahrevival.sunnahassistant.data.model.AdhkaarChapter
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
                    adhkaarChapters = viewModel.getAllAdhkaarChapters().collectAsLazyPagingItems(),
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
        menuInflater.inflate(R.menu.bookmark_menu, menu)
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
    adhkaarChapters: LazyPagingItems<AdhkaarChapter>,
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
                        count = adhkaarChapters.itemCount,
                        key = { index ->
                            val chapter = adhkaarChapters[index]
                            "adhkaar_chapter_${chapter?.chapterId ?: "null"}_$index"
                        }
                    ) { index ->
                        val chapter = adhkaarChapters[index]
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