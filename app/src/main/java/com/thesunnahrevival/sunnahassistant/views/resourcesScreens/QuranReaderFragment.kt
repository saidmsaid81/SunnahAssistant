package com.thesunnahrevival.sunnahassistant.views.resourcesScreens

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
import androidx.navigation.fragment.navArgs
import com.google.android.material.appbar.AppBarLayout
import com.thesunnahrevival.sunnahassistant.R
import com.thesunnahrevival.sunnahassistant.databinding.FragmentQuranReaderBinding
import com.thesunnahrevival.sunnahassistant.views.MainActivity
import com.thesunnahrevival.sunnahassistant.views.SunnahAssistantFragment
import com.thesunnahrevival.sunnahassistant.views.adapters.Ayah
import com.thesunnahrevival.sunnahassistant.views.adapters.Line
import com.thesunnahrevival.sunnahassistant.views.adapters.QuranPage
import com.thesunnahrevival.sunnahassistant.views.adapters.QuranPageAdapter
import com.thesunnahrevival.sunnahassistant.views.customviews.HighlightOverlayView
import com.thesunnahrevival.sunnahassistant.views.listeners.QuranPageClickListener
import com.thesunnahrevival.sunnahassistant.views.others.AyahTranslationFragment
import com.thesunnahrevival.sunnahassistant.views.reduceDragSensitivity

class QuranReaderFragment : SunnahAssistantFragment(), QuranPageClickListener {
    private var _quranReaderBinding: FragmentQuranReaderBinding? = null
    private val quranReaderBinding get() = _quranReaderBinding!!

    private val args: QuranReaderFragmentArgs by navArgs()

    private var stayInImmersiveMode = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        _quranReaderBinding = FragmentQuranReaderBinding.inflate(inflater)
        val pageNumbers = args.resourceItem.pageNumbers

        val list = mutableListOf<QuranPage>()
        pageNumbers.forEach {
            list.add(
                QuranPage(
                    it,
                    listOf(
                        Ayah(number = 1, lines = listOf(Line(1, 174f, 30f, 1239f, 149f))),
                        Ayah(number = 2, lines = listOf(Line(2, 60f, 181f, 1234f, 281f))),
                        Ayah(
                            number = 3, lines = listOf(
                                Line(3, 73f, 299f, 1235f, 418f),
                                Line(3, 701f, 440f, 1237f, 554f)
                            )
                        )
                    )
                )
            )
        }

        val quranPageAdapter = QuranPageAdapter(list, this)
        quranReaderBinding.viewPager.adapter = quranPageAdapter
        quranReaderBinding.viewPager.reduceDragSensitivity(4)

        handleBackPressed()

        return quranReaderBinding.root
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

        clearHighlights()

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

    private fun clearHighlights() {
        val currentPage = quranReaderBinding.viewPager.currentItem
        val page =
            (quranReaderBinding.viewPager.adapter as QuranPageAdapter).pageNumbers[currentPage]
        val highlightOverlay = quranReaderBinding.viewPager
            .findViewWithTag<HighlightOverlayView>("overlay_${page.number}")
            ?: return

        highlightOverlay.clearHighlights()
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

    override fun onQuranPageLongClick(view: View, x: Float, y: Float) {
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

        val highlightOverlay = quranReaderBinding.viewPager
            .findViewWithTag<HighlightOverlayView>("overlay_${page.number}")
            ?: return

        val coordinates = mutableListOf<HighlightOverlayView.Coordinates>()
        var selectedAyah: Ayah? = null

        page.ayahs.forEach { ayah ->
            ayah.lines.forEach { line ->
                if (x >= line.minX && x <= line.maxX &&
                    y >= line.minY && y <= line.maxY
                ) {
                    selectedAyah = ayah
                    ayah.lines.forEach {
                        coordinates.add(
                            HighlightOverlayView.Coordinates(
                                it.minX,
                                it.minY,
                                it.maxX,
                                it.maxY
                            )
                        )
                    }
                }
            }
        }

        if (coordinates.isNotEmpty()) {
            highlightOverlay.clearHighlights()
            highlightOverlay.setHighlightCoordinates(coordinates)
        }

        val fragment = AyahTranslationFragment()
        fragment.show(requireActivity().supportFragmentManager, "ayah_translation")
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