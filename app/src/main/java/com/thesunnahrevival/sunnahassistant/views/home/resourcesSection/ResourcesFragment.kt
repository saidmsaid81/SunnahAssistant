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
import com.thesunnahrevival.sunnahassistant.data.model.dto.TrainingSection
import com.thesunnahrevival.sunnahassistant.data.repositories.AdhkaarChapterRepository.PinResult
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
                val uiState by viewModel.uiState.collectAsState()

                ResourcesScreen(
                    findNavController = findNavController(),
                    resourcesUIState = uiState,
                    surahItemOnClick = { surah ->
                        mainActivityViewModel.updateCurrentPage(surah.startPage)
                        viewModel.onTrainingActionCompleted(TrainingSection.QURAN_RESOURCE_SECTION)
                        findNavController().navigate(R.id.quranReaderFragment)
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
                        viewModel.onTrainingActionCompleted(TrainingSection.QURAN_RESOURCE_SECTION)
                    },
                    onQuranBookmarksClick = {
                        findNavController().navigate(R.id.to_bookmarks_fragment)
                    },
                    onAdhkaarBookmarksClick = {
                        findNavController().navigate(R.id.adhkaarBookmarksFragment)
                    },
                    adhkaarChapterOnClick = { adhkaarChapter ->
                        viewModel.onTrainingActionCompleted(TrainingSection.ADHKAAR_RESOURCE_SECTION)
                        val action = ResourcesFragmentDirections
                            .toAdhkaarReaderFragment(adhkaarChapter.chapterId)
                        findNavController().navigate(action)
                    },
                    onAdhkaarChapterPin = { chapterId ->
                        viewModel.toggleChapterPin(chapterId) { result ->
                            when (result) {
                                PinResult.LimitReached -> {
                                    android.widget.Toast.makeText(
                                        requireContext(),
                                        getString(R.string.you_can_only_pin_up_to_5_chapters),
                                        android.widget.Toast.LENGTH_LONG
                                    ).show()
                                }
                                else -> {
                                }
                            }
                        }
                        viewModel.onTrainingActionCompleted(TrainingSection.ADHKAAR_RESOURCE_SECTION)
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
