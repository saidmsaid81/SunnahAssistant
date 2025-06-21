package com.thesunnahrevival.sunnahassistant.views.resourcesScreens.quran_reader

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.paging.compose.collectAsLazyPagingItems
import com.thesunnahrevival.sunnahassistant.theme.SunnahAssistantTheme
import com.thesunnahrevival.sunnahassistant.viewmodels.SurahListViewModel
import com.thesunnahrevival.sunnahassistant.views.SunnahAssistantFragment
import com.thesunnahrevival.sunnahassistant.views.home.resourcesSection.SurahItem

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
                val configuration = LocalConfiguration.current
                val isArabic = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    configuration.locales[0].language == "ar"
                } else {
                    configuration.locale.language == "ar"
                }
                SunnahAssistantTheme {
                    Surface {
                        Column(modifier = Modifier.padding(top = 16.dp)) {
                            val surahs =
                                viewModel.getAllSurahs().collectAsLazyPagingItems()
                            LazyColumn {
                                items(
                                    count = surahs.itemCount,
                                    key = { index ->
                                        val surah = surahs[index]
                                        "surah_${surah?.id ?: "null"}_$index"
                                    }) { index ->
                                    val surah = surahs[index]

                                    surah?.let {
                                        SurahItem(surah, isArabic) {
                                            findNavController().navigate(
                                                SurahListFragmentDirections.toQuranReaderFragment(
                                                    surah
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}