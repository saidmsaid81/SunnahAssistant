package com.thesunnahrevival.sunnahassistant.views.resourcesScreens.quran_reader

import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.navArgs
import com.google.android.material.appbar.AppBarLayout
import com.thesunnahrevival.sunnahassistant.R
import com.thesunnahrevival.sunnahassistant.databinding.FragmentQuranReaderBinding
import com.thesunnahrevival.sunnahassistant.viewmodels.QuranReaderViewModel
import com.thesunnahrevival.sunnahassistant.views.MainActivity
import com.thesunnahrevival.sunnahassistant.views.SunnahAssistantFragment
import com.thesunnahrevival.sunnahassistant.views.adapters.QuranPageAdapter
import com.thesunnahrevival.sunnahassistant.views.customviews.HighlightOverlayView
import com.thesunnahrevival.sunnahassistant.views.listeners.QuranPageClickListener
import com.thesunnahrevival.sunnahassistant.views.reduceDragSensitivity
import kotlinx.coroutines.launch

class QuranReaderFragment : SunnahAssistantFragment(), QuranPageClickListener {
    private var _quranReaderBinding: FragmentQuranReaderBinding? = null
    private val quranReaderBinding get() = _quranReaderBinding!!

    private val args: QuranReaderFragmentArgs by navArgs()

    private var stayInImmersiveMode = false

    private var lastTouchX: Float = 0f
    private var lastTouchY: Float = 0f

    private val viewmodel by viewModels<QuranReaderViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)

        _quranReaderBinding = FragmentQuranReaderBinding.inflate(inflater)
        val pageNumbers = args.resourceItem.pageNumbers

        val quranPageAdapter = QuranPageAdapter(viewmodel.getQuranPages(pageNumbers), this)
        quranReaderBinding.viewPager.adapter = quranPageAdapter
        quranReaderBinding.viewPager.reduceDragSensitivity(4)

        handleAyahSelection()

        handleBackPressed()

        return quranReaderBinding.root
    }


    override fun onResume() {
        super.onResume()
        (activity as? MainActivity)?.supportActionBar?.title = args.resourceItem.title
    }

    override fun onQuranPageClick(view: View) {
        if (stayInImmersiveMode) {
            view.performLongClick()
            return
        }
        val activity = activity as MainActivity
        if (activity.supportActionBar?.isShowing == true) {
            enterImmersiveMode()
        } else {
            leaveImmersiveMode()
        }
    }

    override fun onQuranPageLongClick(
        view: View,
        highlightOverlay: HighlightOverlayView
    ) {
        view.parent.requestDisallowInterceptTouchEvent(true)

        if (!stayInImmersiveMode) {
            val activity = activity as MainActivity
            activity.supportActionBar?.show()
            if (activity.supportActionBar?.isShowing == true) {
                view.performClick()
            }
            stayInImmersiveMode = true
        }

        val currentPage = quranReaderBinding.viewPager.currentItem
        val page =
            (quranReaderBinding.viewPager.adapter as QuranPageAdapter).pageNumbers[currentPage]

//        val highlightOverlay = quranReaderBinding.viewPager
//            .findViewWithTag<HighlightOverlayView>("overlay_${page.number}")
//            ?: return

        val location = IntArray(2)
        view.getLocationOnScreen(location)

        val rawX = lastTouchX - location[0]
        val rawY = lastTouchY - location[1]

        val x = rawX / highlightOverlay.getScaleX()
        val y = (rawY - highlightOverlay.getOffsetY()) / highlightOverlay.getScaleY()

        page.ayahs.forEach { ayah ->
            ayah.lines.forEach { line ->
                if (x >= line.minX && x <= line.maxX &&
                    y >= line.minY && y <= line.maxY
                ) {
                    mainActivityViewModel.setSelectedAyah(ayah)
                }
            }
        }
    }

    override fun setLastTouchCoordinates(x: Float, y: Float) {
        lastTouchX = x
        lastTouchY = y
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

    private fun handleBackPressed() {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (stayInImmersiveMode) {
                    leaveImmersiveMode()
                    stayInImmersiveMode = false
                } else {
                    isEnabled = false
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
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

        getHighlightOverlay()?.clearHighlights()

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

    private fun getHighlightOverlay(): HighlightOverlayView? {
        val currentPage = quranReaderBinding.viewPager.currentItem
        val page =
            (quranReaderBinding.viewPager.adapter as QuranPageAdapter).pageNumbers[currentPage]
        return quranReaderBinding.viewPager
            .findViewWithTag("overlay_${page.number}")
    }

    private fun handleAyahSelection() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                mainActivityViewModel.selectedAyah.collect { ayah ->
                    if (ayah != null) {
                        val coordinates = ayah.lines.map {
                            HighlightOverlayView.Coordinates(it.minX, it.minY, it.maxX, it.maxY)
                        }.toList()
                        if (coordinates.isEmpty()) {
                            return@collect
                        }
                        val highlightOverlay = getHighlightOverlay()
                        println(highlightOverlay)
                        highlightOverlay?.clearHighlights()
                        highlightOverlay?.setHighlightCoordinates(coordinates)

                        if (requireActivity().supportFragmentManager.findFragmentByTag("ayah_translation") == null) {
                            val fragment = AyahTranslationFragment()
                            fragment.show(
                                requireActivity().supportFragmentManager,
                                "ayah_translation"
                            )
                        }
                    }
                }
            }
        }
    }
}