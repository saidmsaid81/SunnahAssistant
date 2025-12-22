package com.thesunnahrevival.sunnahassistant.views.resourcesScreens.quran_reader

import android.app.NotificationManager
import android.content.Context
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
import android.widget.FrameLayout
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.viewModels
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.snackbar.Snackbar
import com.thesunnahrevival.sunnahassistant.R
import com.thesunnahrevival.sunnahassistant.data.model.entity.Line
import com.thesunnahrevival.sunnahassistant.data.repositories.ResourcesNextActionRepository.NextAction
import com.thesunnahrevival.sunnahassistant.databinding.FragmentQuranReaderBinding
import com.thesunnahrevival.sunnahassistant.utilities.NOTIFICATION_ID
import com.thesunnahrevival.sunnahassistant.utilities.QURAN_PAGE_FROM_NOTIFICATION
import com.thesunnahrevival.sunnahassistant.utilities.getLocale
import com.thesunnahrevival.sunnahassistant.viewmodels.QuranReaderViewModel
import com.thesunnahrevival.sunnahassistant.views.MainActivity
import com.thesunnahrevival.sunnahassistant.views.SunnahAssistantFragment
import com.thesunnahrevival.sunnahassistant.views.adapters.QuranPageAdapter
import com.thesunnahrevival.sunnahassistant.views.customviews.HighlightOverlayView
import com.thesunnahrevival.sunnahassistant.views.listeners.QuranPageInteractionListener
import com.thesunnahrevival.sunnahassistant.views.reduceDragSensitivity
import com.thesunnahrevival.sunnahassistant.views.utilities.showQuranPageNextAction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.sqrt

class QuranReaderFragment : SunnahAssistantFragment(), QuranPageInteractionListener, MenuProvider {
    private var _quranReaderBinding: FragmentQuranReaderBinding? = null
    private val quranReaderBinding get() = _quranReaderBinding

    private var stayInImmersiveMode = false

    private var lastTouchX: Float = 0f
    private var lastTouchY: Float = 0f

    private var isAyahTranslationShowing = false

    private val viewmodel by viewModels<QuranReaderViewModel>()

    private var tapTutorialSnackbar: Snackbar? = null
    private var longPressTutorialSnackbar: Snackbar? = null

    private lateinit var quranPageAdapter: QuranPageAdapter


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        setCurrentPageFromArgumentIfAvailable()

        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.STARTED)

        _quranReaderBinding = FragmentQuranReaderBinding.inflate(inflater)

        quranPageAdapter = QuranPageAdapter(
            activity = requireActivity(),
            pageNumbers = (1..604).toList(),
            listener = this
        )
        val viewPager = quranReaderBinding?.viewPager
        viewPager?.offscreenPageLimit = 2
        viewPager?.adapter = quranPageAdapter
        viewPager?.reduceDragSensitivity(4)
        viewPager?.registerOnPageChangeCallback(pageChangeCallback)
        handleAyahSelection()
        handleBackPressed()

        mainActivityViewModel.selectedSurah.observe(viewLifecycleOwner) { surah ->
            val locale = context?.getLocale() ?: return@observe

            val title = if (locale.language.equals("ar", ignoreCase = true)) {
                surah.arabicName
            } else {
                surah.transliteratedName
            }
            (activity as? MainActivity)?.supportActionBar?.title = title
        }


        showTutorial()
        return quranReaderBinding?.root
    }


    override fun onResume() {
        super.onResume()
        val currentPage = mainActivityViewModel.getCurrentQuranPage()
        quranReaderBinding?.viewPager?.setCurrentItem((currentPage - 1), false)
    }

    private val pageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            viewmodel.getLinesByPageNumber(position + 1)
            mainActivityViewModel.updateCurrentPage(position + 1)
            val highlightOverlay: HighlightOverlayView? = quranReaderBinding?.viewPager
                ?.findViewWithTag("overlay_$position")
            highlightOverlay?.clearHighlights()
            updateBookmarkIconForCurrentPage()
        }
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.quran_reader_menu, menu)
        updateBookmarkIcon(menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.translations -> {
                findNavController().navigate(R.id.pageTranslationFragment)
            }
            R.id.bookmark -> {
                toggleCurrentPageBookmark()
            }
        }
        return true
    }

    override fun onQuranPageClick(view: View) {
        tapTutorialSnackbar?.let { snackbar ->
            snackbar.dismiss()
            tapTutorialSnackbar = null
            lifecycleScope.launch {
                viewmodel.setHasSeenTapTutorial()
                quranReaderBinding?.root?.let { rootView ->
                    showLongPressTutorialIfNeeded(rootView)
                }
            }
        }

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
        longPressTutorialSnackbar?.let { snackbar ->
            snackbar.dismiss()
            longPressTutorialSnackbar = null
            lifecycleScope.launch {
                viewmodel.setHasSeenLongPressTutorial()
            }
        }
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
        val selectedLine = getSelectedLine(lines, x, y)

        selectedLine?.let { mainActivityViewModel.setSelectedAyahId(it.ayahId) }
    }

    override fun setLastTouchCoordinates(x: Float, y: Float) {
        lastTouchX = x
        lastTouchY = y
    }

    override fun onPageNotFound(
        pageNumber: Int
    ) {
        lifecycleScope.launch(Dispatchers.IO) {
            if (!viewmodel.hasSeenDownloadFilesDialog && !viewmodel.isHideDownloadFilePrompt()) {
                viewmodel.hasSeenDownloadFilesDialog = true
                withContext(Dispatchers.Main) {
                    val fragment = DownloadFileBottomSheetFragment()
                    if (!fragment.isVisible) {
                        fragment.show(
                            requireActivity().supportFragmentManager,
                            "download_files"
                        )
                    }
                }
            }

            try {
                viewmodel.downloadQuranPage(pageNumber)
            } catch(e: Exception) {
                e.printStackTrace()
            }
            withContext(Dispatchers.Main) {
                quranReaderBinding?.viewPager?.adapter?.notifyItemChanged((pageNumber - 1))
            }
        }
    }

    override fun onDownloadAllPagesRequested() {
        val fragment = DownloadFileBottomSheetFragment()
        
        val args = Bundle().apply {
            putBoolean("auto_start_download", true)
        }
        fragment.arguments = args
        
        if (!fragment.isVisible) {
            fragment.show(
                requireActivity().supportFragmentManager,
                "download_files_from_timeout"
            )
        }
    }

    override fun onPageLoaded(pageNumber: Int) {
        if ((pageNumber - 1) == quranReaderBinding?.viewPager?.currentItem) {
            mainActivityViewModel.refreshSelectedAyahId()
        }
    }

    override fun showNextActionIfAvailable(view: View, pageNumber: Int) {
        viewLifecycleOwner.lifecycleScope.launch {
            var nextAction: NextAction? = null
            withContext(Dispatchers.IO) {
                nextAction = viewmodel.getNextAction(pageNumber)
            }
            showQuranPageNextAction(requireActivity(), view, nextAction)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        tapTutorialSnackbar?.dismiss()
        longPressTutorialSnackbar?.dismiss()
        tapTutorialSnackbar = null
        longPressTutorialSnackbar = null
        mainActivityViewModel.setSelectedAyahId(null)
        quranReaderBinding?.viewPager?.unregisterOnPageChangeCallback(pageChangeCallback)
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
        viewmodel.hasSeenDownloadFilesDialog = false
    }

    override fun handleEdgeToEdge() {
        val mainActivity = activity as MainActivity

        val appBarLayout = mainActivity.findViewById<AppBarLayout>(R.id.app_bar)
        val typedValue = TypedValue()
        requireContext().theme.resolveAttribute(android.R.attr.colorPrimary, typedValue, true)

        val translucentColor = createTranslucentColor(typedValue.data)
        appBarLayout.setBackgroundColor(translucentColor)
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
            quranReaderBinding?.root?.updateLayoutParams<ViewGroup.MarginLayoutParams> {
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
                            or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
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
            activity.window.decorView.postDelayed({
                var flags = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)


                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    flags = flags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                    flags = flags or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
                }

                activity.window.decorView.systemUiVisibility = flags
            }, 100)
        }
    }

    private fun getHighlightOverlay(): HighlightOverlayView? {
        val pageNumber = quranPageAdapter.pageNumbers[quranReaderBinding?.viewPager?.currentItem ?: 0]
        return quranReaderBinding?.viewPager
            ?.findViewWithTag("overlay_$pageNumber")
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
                    if (quranReaderBinding?.viewPager?.currentItem == pageNumber - 1) {
                        highlightAyah(linesToHighlight)
                    } else {
                        val callback = object : ViewPager2.OnPageChangeCallback() {
                            override fun onPageScrollStateChanged(state: Int) {
                                if (state == ViewPager2.SCROLL_STATE_IDLE) {
                                    quranReaderBinding?.viewPager?.unregisterOnPageChangeCallback(this)
                                    view?.post {
                                        highlightAyah(linesToHighlight)
                                    }
                                }
                            }
                        }
                        quranReaderBinding?.viewPager?.registerOnPageChangeCallback(callback)
                        quranReaderBinding?.viewPager?.setCurrentItem(pageNumber - 1, true)
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

            if (!isAyahTranslationShowing &&
                requireActivity().supportFragmentManager.findFragmentByTag("ayah_translation") == null) {
                
                isAyahTranslationShowing = true
                val fragment = AyahTranslationFragment()
                
                fragment.lifecycle.addObserver(object : DefaultLifecycleObserver {
                    override fun onDestroy(owner: LifecycleOwner) {
                        isAyahTranslationShowing = false
                        fragment.lifecycle.removeObserver(this)
                    }
                })
                
                fragment.show(
                    requireActivity().supportFragmentManager,
                    "ayah_translation"
                )
            }
        }
    }

    private fun toggleCurrentPageBookmark() {
        val currentPageNumber = (quranReaderBinding?.viewPager?.currentItem ?: 0) + 1
        lifecycleScope.launch {
            viewmodel.togglePageBookmark(currentPageNumber)
            updateBookmarkIconForCurrentPage()
        }
    }

    private fun updateBookmarkIcon(menu: Menu) {
        lifecycleScope.launch {
            val currentPageNumber = (quranReaderBinding?.viewPager?.currentItem ?: 0) + 1
            val isBookmarked = viewmodel.isPageBookmarked(currentPageNumber)
            val bookmarkIcon = if (isBookmarked) R.drawable.ic_bookmark_filled else R.drawable.ic_bookmark
            menu.findItem(R.id.bookmark)?.setIcon(bookmarkIcon)
        }
    }

    private fun updateBookmarkIconForCurrentPage() {
        val activity = activity as? MainActivity
        val toolbar = activity?.findViewById<Toolbar>(R.id.toolbar)
        val menu = toolbar?.menu
        menu?.let { updateBookmarkIcon(it) }
    }

    private fun setCurrentPageFromArgumentIfAvailable() {
        val page = arguments?.getInt(QURAN_PAGE_FROM_NOTIFICATION, -1) ?: -1

        if (page != -1) {
            mainActivityViewModel.updateCurrentPage(page, false)
            val notificationId = arguments?.getInt(NOTIFICATION_ID, -1) ?: -1
            if (notificationId != -1) {
                val notificationManager =
                    requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.cancel(notificationId)
            }
        }
    }

    private fun createTranslucentColor(baseColor: Int, opacityPercentage: Float = 70f): Int {
        val alpha = (255 * (opacityPercentage / 100f)).toInt().coerceIn(0, 255)
        return (baseColor and 0x00FFFFFF) or (alpha shl 24)
    }

    private fun showTutorial() {
        lifecycleScope.launch {
            val hasSeenTapTutorial = viewmodel.hasSeenTapTutorial()
            val hasSeenLongPressTutorial = viewmodel.hasSeenLongPressTutorial()

            quranReaderBinding?.root?.let { view ->
                if (!hasSeenTapTutorial) {
                    tapTutorialSnackbar = showTutorialSnackbar(
                        view,
                        getString(R.string.tap_the_page_to_enter_leave_full_screen_mode)
                    ) {
                        lifecycleScope.launch {
                            tapTutorialSnackbar = null
                            showLongPressTutorialIfNeeded(view)
                        }
                    }
                } else if (!hasSeenLongPressTutorial) {
                    showLongPressTutorialIfNeeded(view)
                }
            }
        }
    }

    private suspend fun showLongPressTutorialIfNeeded(view: FrameLayout) {
        if (!viewmodel.hasSeenLongPressTutorial()) {
            longPressTutorialSnackbar = showTutorialSnackbar(
                view,
                getString(R.string.hold_long_tap_ayah_to_view_its_translation)
            ) {
                lifecycleScope.launch {
                    longPressTutorialSnackbar = null
                }
            }
        }
    }

    private fun showTutorialSnackbar(layout: FrameLayout, string: String, onAcknowledge: (View) -> Unit): Snackbar = Snackbar.make(
        requireContext(),
        layout,
        string,
        Snackbar.LENGTH_INDEFINITE
    ).apply {
        view.setBackgroundColor(ContextCompat.getColor(requireActivity(), R.color.fabColor))
        setAction(getString(R.string.got_it), onAcknowledge)
        setActionTextColor(ContextCompat.getColor(requireContext(), android.R.color.black))
        show()
    }

    private fun getSelectedLine(
        lines: List<Line>,
        x: Float,
        y: Float
    ): Line? {
        val tolerance = 15f

        val selectedLine = lines.minByOrNull { line ->
            val distanceX = when {
                x < line.minX -> line.minX - x
                x > line.maxX -> x - line.maxX
                else -> 0f
            }

            val distanceY = when {
                y < line.minY -> line.minY - y
                y > line.maxY -> y - line.maxY
                else -> 0f
            }

            val totalDistance = sqrt(distanceX * distanceX + distanceY * distanceY)

            if (totalDistance <= tolerance) totalDistance else Float.MAX_VALUE
        }?.takeIf { line ->
            val distanceX = when {
                x < line.minX -> line.minX - x
                x > line.maxX -> x - line.maxX
                else -> 0f
            }
            val distanceY = when {
                y < line.minY -> line.minY - y
                y > line.maxY -> y - line.maxY
                else -> 0f
            }
            sqrt(distanceX * distanceX + distanceY * distanceY) <= tolerance
        }
        return selectedLine
    }
}