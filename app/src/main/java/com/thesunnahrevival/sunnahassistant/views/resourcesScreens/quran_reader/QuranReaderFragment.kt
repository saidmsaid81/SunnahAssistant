package com.thesunnahrevival.sunnahassistant.views.resourcesScreens.quran_reader

import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.appbar.AppBarLayout
import com.thesunnahrevival.sunnahassistant.R
import com.thesunnahrevival.sunnahassistant.data.model.Line
import com.thesunnahrevival.sunnahassistant.databinding.FragmentQuranReaderBinding
import com.thesunnahrevival.sunnahassistant.viewmodels.QuranReaderViewModel
import com.thesunnahrevival.sunnahassistant.views.MainActivity
import com.thesunnahrevival.sunnahassistant.views.SunnahAssistantFragment
import com.thesunnahrevival.sunnahassistant.views.adapters.QuranPageAdapter
import com.thesunnahrevival.sunnahassistant.views.customviews.HighlightOverlayView
import com.thesunnahrevival.sunnahassistant.views.listeners.QuranPageClickListener
import com.thesunnahrevival.sunnahassistant.views.reduceDragSensitivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class QuranReaderFragment : SunnahAssistantFragment(), QuranPageClickListener, MenuProvider {
    private var _quranReaderBinding: FragmentQuranReaderBinding? = null
    private val quranReaderBinding get() = _quranReaderBinding!!

    private val args: QuranReaderFragmentArgs by navArgs()

    private var stayInImmersiveMode = false

    private var lastTouchX: Float = 0f
    private var lastTouchY: Float = 0f

    private val viewmodel by viewModels<QuranReaderViewModel>()

    private lateinit var quranPageAdapter: QuranPageAdapter


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)

        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.STARTED)

        _quranReaderBinding = FragmentQuranReaderBinding.inflate(inflater)
        val currentPage = args.resourceItem.startPage

        quranPageAdapter = QuranPageAdapter((1..604).toList(), this)
        quranReaderBinding.viewPager.adapter = quranPageAdapter
        quranReaderBinding.viewPager.reduceDragSensitivity(4)
        quranReaderBinding.viewPager.registerOnPageChangeCallback(pageChangeCallback)
        quranReaderBinding.viewPager.setCurrentItem((currentPage - 1), false)
        handleAyahSelection()
        handleBackPressed()

        return quranReaderBinding.root
    }

    private val pageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            viewmodel.getLinesByPageNumber(position + 1)
            val highlightOverlay: HighlightOverlayView? = quranReaderBinding.viewPager
                .findViewWithTag("overlay_$position")
            highlightOverlay?.clearHighlights()
        }
    }

    override fun onResume() {
        super.onResume()
        (activity as? MainActivity)?.supportActionBar?.title = args.resourceItem.title
    }
    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.quran_reader_menu, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.translations -> {
                val pageNumber = quranReaderBinding.viewPager.currentItem + 1
                val action =
                    QuranReaderFragmentDirections.toPageTranslationsFragment(pageNumber)
                findNavController().navigate(action)
            }
        }
        return true
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

        val location = IntArray(2)
        view.getLocationOnScreen(location)

        val rawX = lastTouchX - location[0]
        val rawY = lastTouchY - location[1]

        val x = rawX / highlightOverlay.getScaleX()
        val y = (rawY - highlightOverlay.getOffsetY()) / highlightOverlay.getScaleY()

        val lines = viewmodel.lines
        val selectedLine = lines.find { line ->
            (x >= line.minX) && (x <= line.maxX) &&
                    (y >= line.minY) && (y <= line.maxY)
        }
        selectedLine?.let { mainActivityViewModel.setSelectedAyahId(it.ayahId) }
    }

    override fun setLastTouchCoordinates(x: Float, y: Float) {
        lastTouchX = x
        lastTouchY = y
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mainActivityViewModel.setSelectedAyahId(null)
        quranReaderBinding.viewPager.unregisterOnPageChangeCallback(pageChangeCallback)
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
        val pageNumber = quranPageAdapter.pageNumbers[quranReaderBinding.viewPager.currentItem]
        return quranReaderBinding.viewPager
            .findViewWithTag("overlay_$pageNumber")
    }

    private fun handleAyahSelection() {
        mainActivityViewModel.selectedAyahId.observe(viewLifecycleOwner) { ayahId ->
            if (ayahId == null) {
                return@observe
            }
            lifecycleScope.launch(Dispatchers.IO) {
                val linesToHighlight = viewmodel.getLinesByAyahId(ayahId)

                if (linesToHighlight.isEmpty()) {
                    return@launch
                }

                val pageNumber = linesToHighlight.first().pageNumber

                withContext(Dispatchers.Main) {
                    if (quranReaderBinding.viewPager.currentItem == pageNumber - 1) {
                        highlightAyah(linesToHighlight)
                    } else {
                        val callback = object : ViewPager2.OnPageChangeCallback() {
                            override fun onPageScrollStateChanged(state: Int) {
                                if (state == ViewPager2.SCROLL_STATE_IDLE) {
                                    quranReaderBinding.viewPager.unregisterOnPageChangeCallback(this)
                                    view?.post {
                                        highlightAyah(linesToHighlight)
                                    }
                                }
                            }
                        }
                        quranReaderBinding.viewPager.registerOnPageChangeCallback(callback)
                        quranReaderBinding.viewPager.setCurrentItem(pageNumber - 1, true)
                    }
                }
            }
        }
    }

    private fun highlightAyah(linesToHighlight: List<Line>) {
        val coordinates = linesToHighlight.map {
            HighlightOverlayView.Coordinates(it.minX, it.minY, it.maxX, it.maxY)
        }.toList()

        if (coordinates.isNotEmpty()) {
            val highlightOverlay = getHighlightOverlay()
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