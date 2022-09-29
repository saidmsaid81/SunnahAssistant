package com.thesunnahrevival.common.views.home

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
import androidx.recyclerview.widget.ItemTouchHelper
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.analytics.FirebaseAnalytics
import com.thesunnahrevival.common.R
import com.thesunnahrevival.common.data.model.AppSettings
import com.thesunnahrevival.common.data.model.Frequency
import com.thesunnahrevival.common.data.model.ToDo
import com.thesunnahrevival.common.databinding.TodayFragmentBinding
import com.thesunnahrevival.common.utilities.generateDateText
import com.thesunnahrevival.common.views.MainActivity
import com.thesunnahrevival.common.views.SwipeToDeleteCallback
import com.thesunnahrevival.common.views.adapters.ToDoListAdapter
import com.thesunnahrevival.common.views.listeners.ToDoItemInteractionListener
import com.thesunnahrevival.common.views.showOnBoardingTutorial
import java.time.LocalDate

open class TodayFragment : MenuBarFragment(), ToDoItemInteractionListener {

    private lateinit var mBinding: TodayFragmentBinding
    private lateinit var mToDoRecyclerAdapter: ToDoListAdapter
    private var fabAnimator: ObjectAnimator? = null
    private var categoryToDisplay = ""

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
                                    (activity as MainActivity), mToDoRecyclerAdapter,
                                    mBinding.toDoList
                                )
                                settings.showOnBoardingTutorial = false
                                mViewModel.updateSettings(settings)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun setupCategoryChips() {
        mBinding.categoryChips.removeAllViews()
        val categories = mAppSettings?.categories
        if (categories != null) {
            val displayAllCategoriesChip = createCategoryChip(getString(R.string.display_all))
            displayAllCategoriesChip.isChecked = true
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
        categoryChip.setOnCheckedChangeListener { _: CompoundButton, isChecked: Boolean ->
            if (isChecked) {
                if (category.matches(getString(R.string.display_all).toRegex()))
                    this.categoryToDisplay = ""
                else
                    this.categoryToDisplay = category
                mViewModel.setToDoParameters(category = categoryToDisplay)
            }
        }
        return categoryChip
    }

    private fun setupTheRecyclerView() {
        val toDoRecyclerView = mBinding.toDoList

        //Setup the RecyclerView Adapter
        mToDoRecyclerAdapter = ToDoListAdapter(requireContext())
        mToDoRecyclerAdapter.setOnItemInteractionListener(this)
        mToDoRecyclerAdapter.addLoadStateListener { loadState: CombinedLoadStates ->
            if (loadState.append.endOfPaginationReached) {
                if (mToDoRecyclerAdapter.itemCount < 1) {
                    mBinding.noTasksView.root.visibility = View.VISIBLE
                    mBinding.appBar.setExpanded(true)
                } else
                    mBinding.noTasksView.root.visibility = View.GONE

                animateAddToDoButton()
                mBinding.progressBar.visibility = View.GONE
            }
        }
        toDoRecyclerView.adapter = mToDoRecyclerAdapter

        //Set the swipe getsures
        val itemTouchHelper = ItemTouchHelper(SwipeToDeleteCallback(mToDoRecyclerAdapter))
        itemTouchHelper.attachToRecyclerView(null)
        itemTouchHelper.attachToRecyclerView(toDoRecyclerView)

        mViewModel.getToDos()
            .observe(viewLifecycleOwner) { toDos: PagingData<ToDo> ->
                mToDoRecyclerAdapter.submitData(viewLifecycleOwner.lifecycle, toDos)
                if (this !is CalendarFragment)
                    displayHijriDate()
            }

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
            mToDoRecyclerAdapter.notifyItemRemoved(position)
            mToDoRecyclerAdapter.notifyItemInserted(position)
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
        isPressed: Boolean,
        isChecked: Boolean?,
        toDo: ToDo
    ) {
        if (isPressed) {
            val toDoCopy = toDo.copy(isComplete = isChecked ?: !toDo.isComplete)
            mViewModel.insertToDo(toDoCopy, false)
        }
    }

    override fun launchToDoDetailsFragment(v: View, toDo: ToDo?) {
        mViewModel.selectedToDo = toDo
            ?: ToDo(
                name = "", frequency = Frequency.OneTime,
                category = resources.getStringArray(R.array.categories)[0], //Uncategorized
                day = LocalDate.now().dayOfMonth,
                month = LocalDate.now().month.ordinal,
                year = LocalDate.now().year
            )

        findNavController().navigate(R.id.toDoDetailsFragment)
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