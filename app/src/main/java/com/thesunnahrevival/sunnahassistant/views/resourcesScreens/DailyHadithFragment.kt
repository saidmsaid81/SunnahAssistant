package com.thesunnahrevival.sunnahassistant.views.resourcesScreens

import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.paging.PagingData
import com.google.android.material.snackbar.Snackbar
import com.thesunnahrevival.sunnahassistant.R
import com.thesunnahrevival.sunnahassistant.data.DailyHadithRepository
import com.thesunnahrevival.sunnahassistant.data.model.DailyHadith
import com.thesunnahrevival.sunnahassistant.databinding.FragmentDailyHadithBinding
import com.thesunnahrevival.sunnahassistant.utilities.SUNNAH_ASSISTANT_APP_LINK
import com.thesunnahrevival.sunnahassistant.viewmodels.DailyHadithViewModel
import com.thesunnahrevival.sunnahassistant.views.SunnahAssistantFragment
import com.thesunnahrevival.sunnahassistant.views.adapters.DailyHadithAdapter
import com.thesunnahrevival.sunnahassistant.views.reduceDragSensitivity

class DailyHadithFragment: SunnahAssistantFragment(), MenuProvider {
    private var _dailyHadithFragmentBinding: FragmentDailyHadithBinding? = null

    private val dailyHadithFragmentBinding get() = _dailyHadithFragmentBinding!!

    private lateinit var mViewModel: DailyHadithViewModel

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        _dailyHadithFragmentBinding = FragmentDailyHadithBinding.inflate(inflater)

        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.STARTED)

        mViewModel = ViewModelProvider(this)[DailyHadithViewModel::class.java]

        fetchDailyHadith()

        return dailyHadithFragmentBinding.root
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.web_view_menu, menu)
    }

    private fun showDailyHadith() {
        val dailyHadithAdapter = DailyHadithAdapter(
            showHijriDate = mainActivityViewModel.settingsValue?.isDisplayHijriDate ?: true
        )
        dailyHadithFragmentBinding.viewPager.adapter = dailyHadithAdapter
        dailyHadithFragmentBinding.viewPager.reduceDragSensitivity(4)

        dailyHadithAdapter.addLoadStateListener {
            dailyHadithFragmentBinding.noHadithFound.visibility = View.GONE
            dailyHadithFragmentBinding.progressBar.visibility = View.GONE
            if (it.append.endOfPaginationReached) {
                if (dailyHadithAdapter.itemCount < 1) {
                    dailyHadithFragmentBinding.noHadithFound.visibility = View.VISIBLE
                }
            }
        }

        mViewModel.getDailyHadithList()
            .observe(viewLifecycleOwner) { pagingData: PagingData<DailyHadith> ->
                dailyHadithAdapter.submitData(lifecycle, pagingData)
            }
    }

    private fun fetchDailyHadith() {
        val networkUnavailableSnackbar = getNetworkUnavailableSnackbar()

        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                mViewModel.fetchDailyHadith()
            }

            override fun onLost(network: Network) {
                networkUnavailableSnackbar.show()
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val connectivityManager =
                requireActivity().getSystemService(ConnectivityManager::class.java)
            connectivityManager.registerDefaultNetworkCallback(networkCallback)
        }

        if (mainActivityViewModel.isDeviceOffline) {
            showDailyHadith()
            networkUnavailableSnackbar.show()
        }

        mViewModel.dailyHadithFetchingStatus.observe(viewLifecycleOwner) { status ->
            if (status == null) {
                return@observe
            }

            dailyHadithFragmentBinding.progressBar.visibility = View.GONE
            dailyHadithFragmentBinding.viewPager.visibility = View.VISIBLE
            networkUnavailableSnackbar.dismiss()

            if (status != DailyHadithRepository.DailyHadithFetchingStatus.LOADING) {
                showDailyHadith()
            }

            when (status) {
                DailyHadithRepository.DailyHadithFetchingStatus.LOADING -> {
                    dailyHadithFragmentBinding.progressBar.visibility = View.VISIBLE
                    dailyHadithFragmentBinding.viewPager.visibility = View.GONE
                }

                DailyHadithRepository.DailyHadithFetchingStatus.SUCCESSFUL -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        val connectivityManager =
                            requireActivity().getSystemService(ConnectivityManager::class.java)
                        connectivityManager.unregisterNetworkCallback(networkCallback)
                    }

                }

                DailyHadithRepository.DailyHadithFetchingStatus.FAILED -> {
                    showAnErrorOccurredSnackbar()
                }
            }
        }

    }

    private fun showAnErrorOccurredSnackbar() {
        Snackbar.make(
            dailyHadithFragmentBinding.root,
            getString(R.string.an_error_occurred_while_fetching_hadith),
            Snackbar.LENGTH_INDEFINITE
        ).apply {
            view.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.fabColor
                )
            )
            setAction(getString(R.string.try_again)) {
                mViewModel.fetchDailyHadith()
                dismiss()
            }
            setActionTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    android.R.color.black
                )
            )
            show()
        }
    }

    private fun getNetworkUnavailableSnackbar(): Snackbar {
        return Snackbar.make(
            dailyHadithFragmentBinding.root,
            getString(R.string.showing_cached_data_because_device_is_offline),
            Snackbar.LENGTH_INDEFINITE
        ).apply {
            this.view.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.fabColor
                )
            )
            this.setAction(R.string.ok) {
                dismiss()
            }
            this.setActionTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    android.R.color.black
                )
            )
        }
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.share_page -> {

                if (dailyHadithFragmentBinding.viewPager.adapter is DailyHadithAdapter) {
                    val currentVisibleHadith =
                        (dailyHadithFragmentBinding.viewPager.adapter as DailyHadithAdapter).peek(
                            dailyHadithFragmentBinding.viewPager.currentItem
                        )

                    val title = currentVisibleHadith?.title ?: ""
                    val content = currentVisibleHadith?.content ?: ""

                    val intent = Intent(Intent.ACTION_SEND)
                    intent.type = "text/plain"
                    intent.putExtra(
                        Intent.EXTRA_TEXT,
                        HtmlCompat.fromHtml(
                            "$title \n\n$content\n\n$SUNNAH_ASSISTANT_APP_LINK",
                            HtmlCompat.FROM_HTML_MODE_LEGACY
                        ).toString()
                    )
                    startActivity(
                        Intent.createChooser(
                            intent,
                            getString(R.string.share_app)
                        )
                    )
                    return true
                }
            }
        }

        return false
    }
}