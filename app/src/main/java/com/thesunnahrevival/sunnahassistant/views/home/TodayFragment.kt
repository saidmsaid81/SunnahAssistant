package com.thesunnahrevival.sunnahassistant.views.home

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import androidx.paging.CombinedLoadStates
import androidx.paging.PagingData
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.ItemTouchHelper
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.analytics.FirebaseAnalytics
import com.thesunnahrevival.sunnahassistant.R
import com.thesunnahrevival.sunnahassistant.data.model.AppSettings
import com.thesunnahrevival.sunnahassistant.data.model.Frequency
import com.thesunnahrevival.sunnahassistant.data.model.ToDo
import com.thesunnahrevival.sunnahassistant.databinding.TodayFragmentBinding
import com.thesunnahrevival.sunnahassistant.utilities.generateDateText
import com.thesunnahrevival.sunnahassistant.views.MainActivity
import com.thesunnahrevival.sunnahassistant.views.SwipeGesturesCallback
import com.thesunnahrevival.sunnahassistant.views.adapters.ToDoListAdapter
import com.thesunnahrevival.sunnahassistant.views.listeners.ToDoItemInteractionListener
import com.thesunnahrevival.sunnahassistant.views.showOnBoardingTutorial
import java.util.*

open class TodayFragment : MenuBarFragment(), ToDoItemInteractionListener {

    private lateinit var mBinding: TodayFragmentBinding
    private lateinit var concatAdapter: ConcatAdapter
    private var fabAnimator: ObjectAnimator? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        mBinding = DataBindingUtil.inflate(
            inflater, R.layout.today_fragment, container, false
        )
        setHasOptionsMenu(true)

        mBinding.lifecycleOwner = viewLifecycleOwner
        mBinding.toDoInteractionListener = this
        setupTheRecyclerView()
        getSettings()

        return mBinding.root
    }

    private fun getSettings() {
        mViewModel.getSettings().observe(viewLifecycleOwner) { settings: AppSettings? ->
            if (settings != null) {
                mAppSettings = settings
                mViewModel.settingsValue = settings
                setupCategoryChips()
                mBinding.toDoList.visibility = View.VISIBLE

                if (this !is CalendarFragment) {
                    when {
                        settings.isFirstLaunch -> findNavController().navigate(R.id.welcomeFragment)
                        settings.isAfterUpdate -> findNavController().navigate(R.id.changelogFragment)
                        else -> {
                            if (settings.showOnBoardingTutorial) {
                                showOnBoardingTutorial(
                                    (activity as MainActivity), concatAdapter, mBinding.toDoList
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
        val categoryChip = Chip(requireContext())
        categoryChip.isCheckable = true
        categoryChip.isChecked = mViewModel.categoryToDisplay.matches(category.toRegex())
        categoryChip.text = category
        categoryChip.checkedIcon = null
        categoryChip.chipBackgroundColor =
            ContextCompat.getColorStateList(
                requireContext(),
                R.color.background_color_chip_state_list
            )
        categoryChip.setTextColor(
            ContextCompat.getColorStateList(
                requireContext(),
                R.color.text_color_chip_state_list
            )
        )
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
                incompleteToDoRecyclerAdapter.submitData(viewLifecycleOwner.lifecycle, toDos)
                if (this !is CalendarFragment)
                    displayHijriDate()
            }

        mViewModel.getCompleteToDos()
            .observe(viewLifecycleOwner) { toDos: PagingData<ToDo> ->
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
        if (mAppSettings?.isDisplayHijriDate == true) {
            mBinding.hijriDate.text = HtmlCompat.fromHtml(
                getString(R.string.hijri_date, generateDateText()),
                HtmlCompat.FROM_HTML_MODE_LEGACY
            )
            mBinding.hijriDate.visibility = View.VISIBLE
        } else
            mBinding.hijriDate.visibility = View.GONE
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
        if (toDo.isAutomaticPrayerTime()) {
            Snackbar.make(
                mBinding.root, getString(R.string.cannot_delete_prayer_time),
                Snackbar.LENGTH_LONG
            ).apply {
                view.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.fabColor))
                show()
            }
            return
        }

        mViewModel.deleteToDo(toDo)
        Snackbar.make(
            mBinding.root, getString(R.string.delete_to_do),
            Snackbar.LENGTH_LONG
        ).apply {
            view.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.fabColor))
            setAction(getString(R.string.undo_delete)) { mViewModel.insertToDo(toDo) }
            setActionTextColor(ContextCompat.getColor(requireContext(), android.R.color.black))
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
            mViewModel.insertToDo(toDoCopy, false)
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

    override fun onResume() {
        super.onResume()
        if (this !is CalendarFragment) {
            mViewModel.setToDoParameters(System.currentTimeMillis())
        }

        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, this.javaClass.simpleName)
        bundle.putString(FirebaseAnalytics.Param.SCREEN_CLASS, this.javaClass.simpleName)
        (activity as MainActivity).firebaseAnalytics.logEvent(
            FirebaseAnalytics.Event.SCREEN_VIEW,
            bundle
        )
    }
}