package com.thesunnahrevival.sunnahassistant.views.home.resourcesSection

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.thesunnahrevival.sunnahassistant.R
import com.thesunnahrevival.sunnahassistant.data.repositories.SurahRepository
import com.thesunnahrevival.sunnahassistant.viewmodels.ResourcesViewModel
import com.thesunnahrevival.sunnahassistant.views.home.MenuBarFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ResourcesFragment : MenuBarFragment() {

    private val viewModel: ResourcesViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        return ComposeView(requireContext()).apply {
            setContent {
                val surahs by viewModel.getFirst3Surahs().collectAsState(initial = listOf())
                val isDataReady by mainActivityViewModel.prepopulateQuranDataCompletionStatus.collectAsState(initial = true)
                val resourceItems = viewModel.resourceItems()
                val lastReadSurah by viewModel.lastReadSurah.collectAsState()

                ResourcesScreen(
                    findNavController = findNavController(),
                    isDataReady = isDataReady,
                    lastReadSurah = lastReadSurah,
                    surahs = surahs,
                    resourceItemList = resourceItems,
                    surahItemOnClick = { surah ->
                        findNavController().navigate(R.id.quranReaderFragment)
                        mainActivityViewModel.updateCurrentPage(surah.startPage)
                    },
                    onSurahPin = { surahId ->
                        viewModel.toggleSurahPin(surahId) { result ->
                            when (result) {
                                SurahRepository.PinResult.LimitReached -> {
                                    android.widget.Toast.makeText(
                                        requireContext(),
                                        getString(R.string.pinned_surahs_limit_reached),
                                        android.widget.Toast.LENGTH_LONG
                                    ).show()
                                }
                                else -> {
                                }
                            }
                        }
                    },
                    onBookmarksClick = {
                        findNavController().navigate(R.id.to_bookmarks_fragment)
                    }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch(Dispatchers.IO) {
            mainActivityViewModel.getAppSettingsValue()?.lastReadPage?.let {
                viewModel.setLastReadPage(it)
            }
        }
    }
}
