package com.thesunnahrevival.sunnahassistant.views.resourcesScreens

import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.fragment.navArgs
import com.thesunnahrevival.sunnahassistant.databinding.FragmentQuranReaderBinding
import com.thesunnahrevival.sunnahassistant.views.MainActivity
import com.thesunnahrevival.sunnahassistant.views.SunnahAssistantFragment
import com.thesunnahrevival.sunnahassistant.views.adapters.QuranPageAdapter
import com.thesunnahrevival.sunnahassistant.views.listeners.QuranPageClickListener
import com.thesunnahrevival.sunnahassistant.views.reduceDragSensitivity

private const val defaultPadding = 16

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
        quranReaderBinding.top.layoutParams.height = (activity.supportActionBar?.height)?.plus(
            defaultPadding
        ) ?: 0
        quranReaderBinding.bottom.layoutParams.height = getNavigationBarHeight(quranReaderBinding.root)
    }

    private fun leaveImmersiveMode() {
        val activity = activity as MainActivity
        quranReaderBinding.top.layoutParams.height = 0
        quranReaderBinding.bottom.layoutParams.height = 0
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

    fun getNavigationBarHeight(view: View): Int {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowInsets = ViewCompat.getRootWindowInsets(view)
            val insets = windowInsets?.getInsets(WindowInsetsCompat.Type.navigationBars())
            return insets?.bottom ?: 0
        } else {
            return getNavigationBarHeightLegacy(view.context)
        }
    }

    fun getNavigationBarHeightLegacy(context: android.content.Context): Int {
        val resources: Resources = context.resources
        val resourceId: Int = resources.getIdentifier("navigation_bar_height", "dimen", "android")
        return if (resourceId > 0) {
            resources.getDimensionPixelSize(resourceId)
        } else {
            0
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
        (activity as? MainActivity)?.supportActionBar?.show()
        _quranReaderBinding = null
    }
}
