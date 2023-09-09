package com.thesunnahrevival.sunnahassistant.views.home

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.CombinedLoadStates
import androidx.paging.PagingData
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.ItemTouchHelper
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import com.sergivonavi.materialbanner.BannerInterface
import com.thesunnahrevival.sunnahassistant.BuildConfig
import com.thesunnahrevival.sunnahassistant.R
import com.thesunnahrevival.sunnahassistant.data.model.AppSettings
import com.thesunnahrevival.sunnahassistant.data.model.Frequency
import com.thesunnahrevival.sunnahassistant.data.model.ToDo
import com.thesunnahrevival.sunnahassistant.databinding.FragmentTodayBinding
import com.thesunnahrevival.sunnahassistant.utilities.generateDateText
import com.thesunnahrevival.sunnahassistant.views.MainActivity
import com.thesunnahrevival.sunnahassistant.views.SwipeGesturesCallback
import com.thesunnahrevival.sunnahassistant.views.adapters.ToDoListAdapter
import com.thesunnahrevival.sunnahassistant.views.dialogs.DeleteToDoFragment
import com.thesunnahrevival.sunnahassistant.views.listeners.ToDoItemInteractionListener
import com.thesunnahrevival.sunnahassistant.views.showBanner
import com.thesunnahrevival.sunnahassistant.views.utilities.ShowcaseView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.TreeSet

open class TodayFragment : MenuBarFragment(), ToDoItemInteractionListener {

    lateinit var mBinding: FragmentTodayBinding
    private lateinit var concatAdapter: ConcatAdapter
    private var fabAnimator: ObjectAnimator? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        mBinding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_today, container, false
        )

        mBinding.lifecycleOwner = viewLifecycleOwner
        mBinding.toDoInteractionListener = this
        setupTheRecyclerView()
        getSettings()
        checkIfThereAreMalformedToDos()
        checkIfNotificationPermissionIsGranted()

        return mBinding.root
    }

    private fun getSettings() {
        mViewModel.getSettings().observe(viewLifecycleOwner) { settings: AppSettings? ->
            if (settings != null) {
                mAppSettings = settings
                mViewModel.settingsValue = settings
                concatAdapter.adapters.getOrNull(0)?.let {
                    (it as ToDoListAdapter).setSunriseTime(mViewModel.getSunriseTime())
                }
                concatAdapter.adapters.getOrNull(1)?.let {
                    (it as ToDoListAdapter).setSunriseTime(mViewModel.getSunriseTime())
                }
                setupCategoryChips()
                mBinding.toDoList.visibility = View.VISIBLE

                if (this !is CalendarFragment) {
                    when {
                        settings.isFirstLaunch -> findNavController().navigate(R.id.welcomeFragment)
                        settings.appVersionCode < BuildConfig.VERSION_CODE ->
                            findNavController().navigate(R.id.changelogFragment)
                        else -> {
                            if (settings.showOnBoardingTutorial) {
                                ShowcaseView().showOnBoardingTutorial(
                                    (activity as MainActivity),
                                    concatAdapter
                                )
                                settings.showOnBoardingTutorial = false
                                mViewModel.updateSettings(settings)
                            }
                        }
                    }
                    displayHijriDate()
                }
            }
        }
    }

    private fun setupCategoryChips() {
        mBinding.categoryChips.removeAllViews()
        val categories = mAppSettings?.categories
        if (categories != null) {
            val displayAllCategoriesChip = createCategoryChip(getString(R.string.display_all))
            displayAllCategoriesChip.isChecked = mViewModel.categoryToDisplay.isBlank()
            mBinding.categoryChips.addView(displayAllCategoriesChip)

            val prayerCategory = resources.getStringArray(R.array.categories)[2]
            val prayerCategoryChip = createCategoryChip(prayerCategory)
            mBinding.categoryChips.addView(prayerCategoryChip)

            for (category in categories) {
                if (!category.matches(prayerCategory.toRegex())) {
                    val categoryChip = createCategoryChip(category)
                    mBinding.categoryChips.addView(categoryChip)
                }
            }
        }
    }

    private fun createCategoryChip(category: String): Chip {
        val categoryChip =
            layoutInflater.inflate(R.layout.choice_chip, mBinding.categoryChips, false) as Chip
        categoryChip.isChecked = mViewModel.categoryToDisplay.matches(category.toRegex())
        categoryChip.text = category
        categoryChip.setOnCheckedChangeListener { button: CompoundButton, isChecked: Boolean ->
            if (button.isPressed && isChecked) {
                val categoryToDisplay =
                    if (category.matches(getString(R.string.display_all).toRegex()))
                        ""
                    else
                        category
                mViewModel.setToDoParameters(category = categoryToDisplay)
            }
        }
        return categoryChip
    }

    private fun setupTheRecyclerView() {
        val toDoRecyclerView = mBinding.toDoList

        //Setup the RecyclerView Adapter
        val config = ConcatAdapter.Config.Builder().setIsolateViewTypes(true).build()
        concatAdapter = ConcatAdapter(config)

        val incompleteToDoRecyclerAdapter = ToDoListAdapter(requireContext())
        setUpAdapter(incompleteToDoRecyclerAdapter)
        val completeToDoRecyclerAdapter = ToDoListAdapter(requireContext(), true)
        setUpAdapter(completeToDoRecyclerAdapter)

        toDoRecyclerView.adapter = concatAdapter

        //Set the swipe gestures
        val itemTouchHelper = ItemTouchHelper(
            SwipeGesturesCallback(requireContext())
        )
        itemTouchHelper.attachToRecyclerView(null)
        itemTouchHelper.attachToRecyclerView(toDoRecyclerView)

        mViewModel.getIncompleteToDos()
            .observe(viewLifecycleOwner) { toDos: PagingData<ToDo> ->
                if (mAppSettings?.isAutomaticPrayerAlertsEnabled == true)
                    incompleteToDoRecyclerAdapter.setSunriseTime(mViewModel.getSunriseTime())
                incompleteToDoRecyclerAdapter.submitData(viewLifecycleOwner.lifecycle, toDos)
                if (this !is CalendarFragment)
                    displayHijriDate()
            }

        mViewModel.getCompleteToDos()
            .observe(viewLifecycleOwner) { toDos: PagingData<ToDo> ->
                if (mAppSettings?.isAutomaticPrayerAlertsEnabled == true)
                    completeToDoRecyclerAdapter.setSunriseTime(mViewModel.getSunriseTime())
                completeToDoRecyclerAdapter.submitData(viewLifecycleOwner.lifecycle, toDos)
                if (this !is CalendarFragment)
                    displayHijriDate()
            }
    }

    private fun setUpAdapter(adapter: ToDoListAdapter) {
        adapter.setOnItemInteractionListener(this)
        adapter.addLoadStateListener { loadState: CombinedLoadStates ->
            if (loadState.append.endOfPaginationReached) {
                if (concatAdapter.itemCount < 1) {
                    mBinding.toDoList.visibility = View.GONE
                    mBinding.noTasksView.root.visibility = View.VISIBLE
                    mBinding.appBar.setExpanded(true)
                } else {
                    mBinding.noTasksView.root.visibility = View.GONE
                    mBinding.toDoList.visibility = View.VISIBLE
                }

                animateAddToDoButton()
                mBinding.progressBar.visibility = View.GONE
            }
        }
        concatAdapter.addAdapter(adapter)
    }

    private fun displayHijriDate() {
        val includeHijriDate = mViewModel.settingsValue?.isDisplayHijriDate ?: true
        mBinding.date.text = HtmlCompat.fromHtml(
            getString(
                R.string.hijri_date,
                generateDateText(
                    includeHijriDate = includeHijriDate
                )
            ),
            HtmlCompat.FROM_HTML_MODE_LEGACY
        )
        mBinding.date.visibility = View.VISIBLE
        if (!includeHijriDate) {
            mBinding.date.textSize = 14F
        }
    }

    private fun animateAddToDoButton() {
        if (fabAnimator == null) {
            fabAnimator = ObjectAnimator.ofPropertyValuesHolder(
                mBinding.fab,
                PropertyValuesHolder.ofFloat("scaleX", 1.2f),
                PropertyValuesHolder.ofFloat("scaleY", 1.2f)
            )

            fabAnimator?.duration = 310
            fabAnimator?.repeatCount = ObjectAnimator.INFINITE
            fabAnimator?.repeatMode = ObjectAnimator.REVERSE
        }

        if (mBinding.noTasksView.root.isVisible)
            fabAnimator?.start()
        else
            fabAnimator?.cancel()
    }

    override fun onSwipeToDelete(position: Int, toDo: ToDo) {
        val dialogFragment = DeleteToDoFragment()
        dialogFragment.arguments = Bundle().apply {
            putSerializable(DeleteToDoFragment.TODO, toDo)
        }
        dialogFragment.setOnToDoDeleteListener(this)
        dialogFragment.show(requireActivity().supportFragmentManager, "dialog")
    }

    override fun showUndoDeleteSnackbar(toDo: ToDo) {
        Snackbar.make(
            mBinding.root, getString(R.string.delete_to_do),
            Snackbar.LENGTH_LONG
        ).apply {
            view.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.fabColor
                )
            )
            setAction(getString(R.string.undo_delete)) {
                Log.v("To-Do", toDo.toString())
                val newToDo = toDo.copy()
                mViewModel.insertToDo(newToDo)
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

    override fun showPrayerTimeDeletionError() {
        Snackbar.make(
            mBinding.root, getString(R.string.cannot_delete_prayer_time),
            Snackbar.LENGTH_LONG
        ).apply {
            view.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.fabColor
                )
            )
            show()
        }
    }

    override fun onMarkAsComplete(
        userInitiated: Boolean,
        isChecked: Boolean?,
        toDo: ToDo
    ) {
        if (userInitiated) {
            val completedDates = TreeSet<String>()
            completedDates.addAll(toDo.completedDates)
            if (isChecked == true)
                completedDates.add(mViewModel.selectedToDoDate.toString())
            else if (isChecked == false)
                completedDates.remove(mViewModel.selectedToDoDate.toString())
            else {
                if (completedDates.contains(mViewModel.selectedToDoDate.toString()))
                    completedDates.remove(mViewModel.selectedToDoDate.toString())
                else
                    completedDates.add(mViewModel.selectedToDoDate.toString())
            }

            val toDoCopy = toDo.copy(completedDates = completedDates)
            mViewModel.updateToDo(toDoCopy)
        }
    }

    override fun launchToDoDetailsFragment(v: View, toDo: ToDo?) {
        val categoriesTreeSet = mAppSettings?.categories
        val uncategorized = resources.getStringArray(R.array.categories).getOrNull(0)
        val category = if (mViewModel.categoryToDisplay.isBlank())
            categoriesTreeSet?.find { it == uncategorized }
        else {
            categoriesTreeSet?.find { it == mViewModel.categoryToDisplay } ?: uncategorized
        }
        if (category != null) {
            mViewModel.selectedToDo = toDo
                ?: ToDo(
                    name = "", frequency = Frequency.OneTime,
                    category = category,
                    day = mViewModel.selectedToDoDate.dayOfMonth,
                    month = mViewModel.selectedToDoDate.month.ordinal,
                    year = mViewModel.selectedToDoDate.year
                )

            findNavController().navigate(R.id.toDoDetailsFragment)
        }

    }

    private fun checkIfThereAreMalformedToDos() {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            mViewModel.getMalformedToDos().collectLatest {
                if (it.isNotEmpty()) {
                    findNavController().navigate(R.id.resolveMalformedToDosFragment)
                }
            }
        }
    }

    private fun checkIfNotificationPermissionIsGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_DENIED
        ) {
            lifecycleScope.launch(Dispatchers.IO) {
                if (mViewModel.getNotificationPermissionRequestsCount() > 0) {
                    withContext(Dispatchers.Main) {
                        val openSettingsListener = BannerInterface.OnClickListener {
                            val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                putExtra(Settings.EXTRA_APP_PACKAGE, requireActivity().packageName)
                            }
                            startActivity(intent)
                            mBinding.banner.dismiss()
                        }

                        val hideBannerListener = BannerInterface.OnClickListener {
                            mViewModel.hideFixNotificationsBanner()
                            mBinding.banner.dismiss()
                        }

                        showBanner(
                            mBinding.banner,
                            getString(R.string.fix_notifications),
                            R.drawable.ic_notifications_banner,
                            getString(R.string.go_to_settings),
                            openSettingsListener,
                            getString(R.string.don_t_show_this),
                            hideBannerListener
                        )
                    }
                }
            }
        }
    }


    override fun onResume() {
        super.onResume()
        if (this !is CalendarFragment) {
            mViewModel.setToDoParameters(System.currentTimeMillis())
        }
    }
}