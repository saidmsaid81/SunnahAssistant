package com.thesunnahrevival.sunnahassistant.views.resourcesScreens.quran_reader

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.thesunnahrevival.sunnahassistant.R
import com.thesunnahrevival.sunnahassistant.data.model.Surah
import com.thesunnahrevival.sunnahassistant.theme.SunnahAssistantTheme
import com.thesunnahrevival.sunnahassistant.viewmodels.SurahListViewModel
import com.thesunnahrevival.sunnahassistant.views.SunnahAssistantFragment
import com.thesunnahrevival.sunnahassistant.views.home.resourcesSection.SurahItem
import com.thesunnahrevival.sunnahassistant.views.utilities.isArabic
import kotlinx.coroutines.flow.collectLatest

class SurahListFragment : SunnahAssistantFragment() {
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
                    surahs = viewModel.getAllSurahs().collectAsLazyPagingItems(),
                    firstVisiblePosition = viewModel.firstVisiblePosition,
                    onScroll = { index -> viewModel.firstVisiblePosition = index },
                    onSurahClick = { surah ->
                        findNavController().navigate(
                            R.id.quranReaderFragment
                        )
                        mainActivityViewModel.updateCurrentPage(surah.startPage)
                    }
                )
            }
        }
    }
}

@Composable
private fun SurahListScreen(
    surahs: LazyPagingItems<Surah>,
    firstVisiblePosition: Int,
    onScroll: (Int) -> Unit,
    onSurahClick: (Surah) -> Unit
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
                            SurahItem(surah, isArabic()) { onSurahClick(surah) }
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
                        SurahItem(surah, false) { }
                    }
                }
            }
        }
    }
}

private fun previewSurahs() = listOf(
    Surah(1, "سورة الفاتحة", "Suratul Fatiha", true, 7, 1),
    Surah(2, "سورة البقرة", "Suratul Baqarah", false, 286, 2),
    Surah(3, "سورة آل عمران", "Suratul Aal-Imran", false, 200, 50),
    Surah(4, "سورة النساء", "Suratul Nisa", false, 176, 77),
    Surah(5, "سورة المائدة", "Suratul Maidah", false, 120, 106),
    Surah(6, "سورة الأنعام", "Suratul An'am", true, 165, 128)
)