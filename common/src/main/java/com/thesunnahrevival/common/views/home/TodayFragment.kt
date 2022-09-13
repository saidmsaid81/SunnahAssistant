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
import androidx.recyclerview.widget.ItemTouchHelper
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.analytics.FirebaseAnalytics
import com.thesunnahrevival.common.R
import com.thesunnahrevival.common.data.model.AppSettings
import com.thesunnahrevival.common.data.model.Frequency
import com.thesunnahrevival.common.data.model.Reminder
import com.thesunnahrevival.common.databinding.TodayFragmentBinding
import com.thesunnahrevival.common.utilities.generateDateText
import com.thesunnahrevival.common.views.MainActivity
import com.thesunnahrevival.common.views.SwipeToDeleteCallback
import com.thesunnahrevival.common.views.adapters.ReminderListAdapter
import com.thesunnahrevival.common.views.listeners.ReminderItemInteractionListener
import com.thesunnahrevival.common.views.showOnBoardingTutorial
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

open class TodayFragment : MenuBarFragment(), ReminderItemInteractionListener {

    private lateinit var mBinding: TodayFragmentBinding
    private lateinit var mReminderRecyclerAdapter: ReminderListAdapter
    private var mAllReminders: ArrayList<Reminder> = arrayListOf()
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
        mBinding.reminderInteractionListener = this
        getSettings()

        return mBinding.root
    }

    private fun getSettings() {
        mViewModel.getSettings().observe(viewLifecycleOwner) { settings: AppSettings? ->
            if (settings != null) {
                mAppSettings = settings
                setupTheRecyclerView()
                setupCategoryChips()

                if (this !is CalendarFragment) {
                    mViewModel.settingsValue = settings

                    when {
                        settings.isFirstLaunch -> findNavController().navigate(R.id.welcomeFragment)
                        settings.isAfterUpdate -> findNavController().navigate(R.id.changelogFragment)
                        else -> {
                            if (settings.isDisplayHijriDate) {
                                mBinding.hijriDate.text = HtmlCompat.fromHtml(
                                    getString(R.string.hijri_date, generateDateText()),
                                    HtmlCompat.FROM_HTML_MODE_LEGACY
                                )
                                mBinding.hijriDate.visibility = View.VISIBLE
                            }

                            if (settings.showOnBoardingTutorial) {
                                showOnBoardingTutorial(
                                    (activity as MainActivity), mReminderRecyclerAdapter,
                                    mBinding.reminderList
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

            for (category in categories) {
                val categoryChip = createCategoryChip(category)
                mBinding.categoryChips.addView(categoryChip)
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
                filterData()
            }
        }
        return categoryChip
    }

    private fun setupTheRecyclerView() {
        //Setup the RecyclerView Adapter
        mReminderRecyclerAdapter = ReminderListAdapter(requireContext())
        mReminderRecyclerAdapter.setOnItemInteractionListener(this)

        val reminderRecyclerView = mBinding.reminderList
        reminderRecyclerView.adapter = mReminderRecyclerAdapter

        //Set the swipe to delete action
        val itemTouchHelper = ItemTouchHelper(SwipeToDeleteCallback(mReminderRecyclerAdapter))
        itemTouchHelper.attachToRecyclerView(reminderRecyclerView)

        mViewModel.setDateOfReminders(System.currentTimeMillis())
        mViewModel.getReminders()
            .observe(viewLifecycleOwner) { reminders: List<Reminder> ->
                displayTheReminders(reminders as ArrayList<Reminder>)
            }
    }

    private fun displayTheReminders(data: ArrayList<Reminder>?) {

        if (data != null && data.isNotEmpty()) {
            mAllReminders = data
            filterData()
        } else {
            mReminderRecyclerAdapter.setData(data ?: listOf())
            mBinding.noTasksView.root.visibility = View.VISIBLE
            animateAddReminderButton()
            mBinding.progressBar.visibility = View.GONE
        }
    }


    private fun filterData() {
        mBinding.noTasksView.root.visibility = View.GONE
        mBinding.progressBar.visibility = View.VISIBLE

        //Refresh the RecyclerView
        if (!categoryToDisplay.matches("".toRegex())) {
            //Filter data in background thread because if the list is very large
            // it may slow down the main thread
            CoroutineScope(Dispatchers.Default).launch {
                val filteredData = mAllReminders.filter {
                    it.category?.matches(categoryToDisplay.toRegex()) == true
                }
                withContext(Dispatchers.Main) {
                    mBinding.progressBar.visibility = View.GONE
                    when {
                        filteredData.isNotEmpty() -> {
                            mReminderRecyclerAdapter.setData(filteredData)
                            mBinding.noTasksView.root.visibility = View.GONE
                        }
                        else -> {
                            mReminderRecyclerAdapter.setData(listOf())
                            mBinding.noTasksView.root.visibility = View.VISIBLE
                        }
                    }
                }
            }
        } else {
            mBinding.progressBar.visibility = View.GONE
            mReminderRecyclerAdapter.setData(mAllReminders)
        }
        animateAddReminderButton()
    }

    private fun animateAddReminderButton() {
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

    override fun onSwipeToDelete(position: Int) {
        val mDeletedReminder = mAllReminders[position]
        val prayer = resources.getStringArray(R.array.categories)[2]

        if (mDeletedReminder.category?.matches(prayer.toRegex()) == true && mAppSettings?.isAutomatic == true) {
            Snackbar.make(
                mBinding.root, getString(R.string.cannot_delete_prayer_time),
                Snackbar.LENGTH_LONG
            ).apply {
                view.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.fabColor))
                show()
            }
            mReminderRecyclerAdapter.notifyItemRemoved(position)
            mReminderRecyclerAdapter.notifyItemInserted(position)
            return
        }

        mViewModel.delete(mDeletedReminder)
        Snackbar.make(
            mBinding.root, getString(R.string.delete_reminder),
            Snackbar.LENGTH_LONG
        ).apply {
            view.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.fabColor))
            setAction(getString(R.string.undo_delete)) { mViewModel.addReminder(mDeletedReminder) }
            setActionTextColor(ContextCompat.getColor(requireContext(), android.R.color.black))
            show()
        }
    }

    override fun onMarkAsComplete(
        isPressed: Boolean,
        isChecked: Boolean?,
        position: Int
    ) {
        if (isPressed) {
            val reminder = mAllReminders[position]
            reminder.isComplete = isChecked ?: !reminder.isComplete
            mViewModel.addReminder(reminder, false)
        }
    }

    override fun launchReminderDetailsFragment(v: View, reminder: Reminder?) {
        mViewModel.selectedReminder = reminder
            ?: Reminder(
                reminderName = "", frequency = Frequency.OneTime,
                category = resources.getStringArray(R.array.categories)[0], //Uncategorized
                day = LocalDate.now().dayOfMonth,
                month = LocalDate.now().month.ordinal,
                year = LocalDate.now().year
            )

        mViewModel.settingsValue = mAppSettings

        findNavController().navigate(R.id.reminderDetailsFragment, null)
    }

    override fun onResume() {
        super.onResume()
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, this.javaClass.simpleName)
        bundle.putString(FirebaseAnalytics.Param.SCREEN_CLASS, this.javaClass.simpleName)
        (activity as MainActivity).firebaseAnalytics.logEvent(
            FirebaseAnalytics.Event.SCREEN_VIEW,
            bundle
        )
    }
}