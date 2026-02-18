package com.thesunnahrevival.sunnahassistant.views.resourcesScreens.adhkaar

import android.os.Bundle
import android.view.*
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.thesunnahrevival.sunnahassistant.R
import com.thesunnahrevival.sunnahassistant.data.model.embedded.BookmarkedAdhkaarDataEmbedded
import com.thesunnahrevival.sunnahassistant.theme.SunnahAssistantTheme
import com.thesunnahrevival.sunnahassistant.viewmodels.AdhkaarBookmarksViewModel
import com.thesunnahrevival.sunnahassistant.views.home.MenuBarFragment
import com.thesunnahrevival.sunnahassistant.views.home.resourcesSection.ResourceCard

class AdhkaarBookmarksFragment : MenuBarFragment() {

    private val viewModel: AdhkaarBookmarksViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)

        return ComposeView(requireContext()).apply {
            setContent {
                val bookmarks by viewModel.bookmarkedItemsFlow.collectAsState(initial = emptyList())
                AdhkaarBookmarksScreen(
                    items = bookmarks,
                    onClick = { item ->
                        val args = Bundle().apply {
                            putInt("chapterId", item.chapterId)
                            putInt("scrollToItemId", item.itemId)
                        }
                        findNavController().navigate(R.id.adhkaarReaderFragment, args)
                    }
                )
            }
        }
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.adhkaar_bookmarks_menu, menu)

        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as androidx.appcompat.widget.SearchView
        searchView.queryHint = getString(R.string.search_adhkaar_bookmarks)
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
        return super.onMenuItemSelected(item)
    }
}

@Composable
private fun AdhkaarBookmarksScreen(
    items: List<BookmarkedAdhkaarDataEmbedded>,
    onClick: (BookmarkedAdhkaarDataEmbedded) -> Unit
) {
    SunnahAssistantTheme {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(start = 16.dp, end = 16.dp)
        ) {
            if (items.isEmpty()) {
                Text(
                    text = stringResource(R.string.no_bookmarked_adhkaar),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.body2
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 16.dp)
                ) {
                    items(
                        count = items.size,
                        key = { index ->
                            val item = items[index]
                            "adhkaar_${item.chapterId}_${item.itemId}_$index"
                        }
                    ) { index ->
                        val item = items[index]
                        ResourceCard(
                            title = item.chapterName,
                            subtitle = item.itemTranslation,
                            resourceNumber = item.itemId.toString()
                        ) {
                            onClick(item)
                        }
                    }
                }
            }
        }
    }
}
