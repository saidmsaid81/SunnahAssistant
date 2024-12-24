package com.thesunnahrevival.sunnahassistant.views.resourcesScreens

import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.updateLayoutParams
import androidx.navigation.fragment.navArgs
import com.google.android.material.appbar.AppBarLayout
import com.thesunnahrevival.sunnahassistant.R
import com.thesunnahrevival.sunnahassistant.databinding.FragmentQuranReaderBinding
import com.thesunnahrevival.sunnahassistant.views.MainActivity
import com.thesunnahrevival.sunnahassistant.views.SunnahAssistantFragment
import com.thesunnahrevival.sunnahassistant.views.adapters.QuranPageAdapter
import com.thesunnahrevival.sunnahassistant.views.listeners.QuranPageClickListener
import com.thesunnahrevival.sunnahassistant.views.reduceDragSensitivity

class QuranReaderFragment : SunnahAssistantFragment(), QuranPageClickListener {
    private var _quranReaderBinding: FragmentQuranReaderBinding? = null
    private val quranReaderBinding get() = _quranReaderBinding!!

    private val args: QuranReaderFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        _quranReaderBinding = FragmentQuranReaderBinding.inflate(inflater)
        val pageNumbers = args.resourceItem.pageNumbers
        val quranPageAdapter = QuranPageAdapter(pageNumbers, this)
        quranReaderBinding.viewPager.adapter = quranPageAdapter
        quranReaderBinding.viewPager.reduceDragSensitivity(4)
        return quranReaderBinding.root
    }

    override fun onResume() {
        super.onResume()
        (activity as? MainActivity)?.supportActionBar?.title = args.resourceItem.title
    }


    private fun enterImmersiveMode() {
        val activity = activity as MainActivity
        activity.supportActionBar?.hide()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val controller = activity.window.insetsController
            if (controller != null) {
                controller.hide(WindowInsets.Type.statusBars())
                controller.hide(WindowInsets.Type.navigationBars())
            }
        } else {
            activity.window.decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            or View.SYSTEM_UI_FLAG_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    )
        }
    }

    private fun leaveImmersiveMode() {
        val activity = activity as MainActivity
        activity.supportActionBar?.show()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val controller = activity.window.insetsController
            if (controller != null) {
                controller.show(WindowInsets.Type.statusBars())
                controller.show(WindowInsets.Type.navigationBars())
            }
        } else {
            activity.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
        }
    }

    override fun onQuranPageClick() {
        val activity = activity as MainActivity
        if (activity.supportActionBar?.isShowing == true) {
            enterImmersiveMode()
        } else {
            leaveImmersiveMode()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        val mainActivity = activity as MainActivity

        val toolbar = mainActivity.findViewById<Toolbar>(R.id.toolbar)
        toolbar.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            topMargin = 0
        }

        val appBarLayout = mainActivity.findViewById<AppBarLayout>(R.id.app_bar)
        appBarLayout.setBackgroundColor(
            ContextCompat.getColor(
                requireContext(),
                android.R.color.transparent
            )
        )
        appBarLayout.elevation = 0f

        (activity as? MainActivity)?.supportActionBar?.show()
        _quranReaderBinding = null
    }

    override fun handleEdgeToEdge() {
        val mainActivity = activity as MainActivity

        val appBarLayout = mainActivity.findViewById<AppBarLayout>(R.id.app_bar)
        val typedValue = TypedValue()
        requireContext().theme.resolveAttribute(android.R.attr.colorPrimary, typedValue, true)
        appBarLayout.setBackgroundColor(typedValue.data)
        appBarLayout.elevation = 4f

        mainActivityViewModel.statusBarHeight.observe(viewLifecycleOwner) { statusBarHeight ->
            val toolbar = mainActivity.findViewById<Toolbar>(R.id.toolbar)
            toolbar.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                if (mainActivity.supportActionBar?.isShowing == true) {
                    topMargin = statusBarHeight
                }
            }
        }

        mainActivityViewModel.navBarHeight.observe(viewLifecycleOwner) { navBarHeight ->
            quranReaderBinding.root.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                if (mainActivity.supportActionBar?.isShowing == true) {
                    bottomMargin = navBarHeight
                }
            }
        }
    }
}
