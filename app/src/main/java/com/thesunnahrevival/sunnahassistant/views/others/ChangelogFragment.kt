package com.thesunnahrevival.sunnahassistant.views.others

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.thesunnahrevival.sunnahassistant.BuildConfig
import com.thesunnahrevival.sunnahassistant.R
import com.thesunnahrevival.sunnahassistant.databinding.FragmentChangelogBinding
import com.thesunnahrevival.sunnahassistant.utilities.generateEmailIntent
import com.thesunnahrevival.sunnahassistant.utilities.openPlayStore
import com.thesunnahrevival.sunnahassistant.views.SunnahAssistantFragment
import com.thesunnahrevival.sunnahassistant.views.translateLink

class ChangelogFragment : SunnahAssistantFragment() {

    private var _changelogFragmentBinding: FragmentChangelogBinding? = null
    private val changelogFragmentBinding get() = _changelogFragmentBinding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        _changelogFragmentBinding = FragmentChangelogBinding.inflate(inflater)
        val view = changelogFragmentBinding.root


        mainActivityViewModel.settingsValue?.appVersion = BuildConfig.VERSION_NAME
        mainActivityViewModel.settingsValue?.appVersionCode = BuildConfig.VERSION_CODE
        mainActivityViewModel.settingsValue?.categories?.addAll(requireContext().resources.getStringArray(R.array.categories))
        mainActivityViewModel.settingsValue?.let { mainActivityViewModel.updateSettings(it) }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        changelogFragmentBinding.changelogVersion.text = getString(R.string.version, BuildConfig.VERSION_NAME)
        changelogFragmentBinding.helpTranslateApp.setOnClickListener {
            translateLink(this)
        }
        changelogFragmentBinding.sendFeedback.setOnClickListener {
            startActivity(generateEmailIntent())
        }
        changelogFragmentBinding.rateThisApp.setOnClickListener {
            openPlayStore(requireActivity(), requireActivity().packageName)
        }
        changelogFragmentBinding.privacyPolicy.setOnClickListener {
            findNavController().navigate(R.id.privacyPolicyFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _changelogFragmentBinding = null
    }

}
