package com.thesunnahrevival.sunnahassistant.views.home.resourcesSection

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.thesunnahrevival.sunnahassistant.databinding.FragmentResourcesBinding
import com.thesunnahrevival.sunnahassistant.theme.SunnahAssistantTheme
import com.thesunnahrevival.sunnahassistant.views.home.MenuBarFragment

class ResourcesFragment : MenuBarFragment() {

    private var _resourcesFragmentBinding: FragmentResourcesBinding? = null

    private val resourcesFragmentBinding get() = _resourcesFragmentBinding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        _resourcesFragmentBinding = FragmentResourcesBinding.inflate(inflater).apply {
            composeView.setContent {
                SunnahAssistantTheme {
                    ResourcesScreen(findNavController())
                }

            }
        }
        return resourcesFragmentBinding.root
    }
}