package com.thesunnahrevival.sunnahassistant.views.resourcesScreens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.snackbar.Snackbar
import com.thesunnahrevival.sunnahassistant.R
import com.thesunnahrevival.sunnahassistant.databinding.FragmentDailyHadithBinding
import com.thesunnahrevival.sunnahassistant.viewmodels.DailyHadithViewModel
import com.thesunnahrevival.sunnahassistant.views.MainActivity
import com.thesunnahrevival.sunnahassistant.views.SunnahAssistantFragment
import com.thesunnahrevival.sunnahassistant.views.adapters.DailyHadithAdapter

class DailyHadithFragment: SunnahAssistantFragment() {
    private var _dailyHadithFragmentBinding: FragmentDailyHadithBinding? = null

    private val dailyHadithFragmentBinding get() = _dailyHadithFragmentBinding!!

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        _dailyHadithFragmentBinding = FragmentDailyHadithBinding.inflate(inflater)

        val viewModel = ViewModelProvider(this)[DailyHadithViewModel::class.java]
        viewModel.showDailyHadithLoadingIndicator.observe(viewLifecycleOwner) {
            if (it == true) {
                dailyHadithFragmentBinding.progressBar.visibility = View.VISIBLE
                dailyHadithFragmentBinding.viewPager.visibility = View.GONE
            } else {
                dailyHadithFragmentBinding.progressBar.visibility = View.GONE
                dailyHadithFragmentBinding.viewPager.visibility = View.VISIBLE
            }
        }

        if (mainActivityViewModel.isDeviceOffline) {
            val mainActivity = requireActivity() as MainActivity
            mainActivity.popupSnackbar(
                mainActivity,
                getString(R.string.showing_cached_data_because_device_is_offline),
                Snackbar.LENGTH_INDEFINITE,
                getString(
                R.string.ok)
            ) {
                it.visibility = View.GONE
            }
        }

        val dailyHadithAdapter = DailyHadithAdapter()
        dailyHadithFragmentBinding.viewPager.adapter = dailyHadithAdapter
        dailyHadithFragmentBinding.viewPager.reduceDragSensitivity(4)

        viewModel.getDailyHadithList().observe(viewLifecycleOwner) {
            dailyHadithAdapter.submitData(lifecycle, it)
        }
        return dailyHadithFragmentBinding.root
    }

    private fun ViewPager2.reduceDragSensitivity(f: Int = 0) {
        val recyclerViewField = ViewPager2::class.java.getDeclaredField("mRecyclerView")
        recyclerViewField.isAccessible = true
        val recyclerView = recyclerViewField.get(this) as RecyclerView

        val touchSlopField = RecyclerView::class.java.getDeclaredField("mTouchSlop")
        touchSlopField.isAccessible = true
        val touchSlop = touchSlopField.get(recyclerView) as Int
        touchSlopField.set(recyclerView, touchSlop*f)
    }
}
