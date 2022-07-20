package com.thesunnahrevival.sunnahassistant.views.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.analytics.FirebaseAnalytics
import com.thesunnahrevival.sunnahassistant.R
import com.thesunnahrevival.sunnahassistant.data.model.AppSettings
import com.thesunnahrevival.sunnahassistant.data.model.Reminder
import com.thesunnahrevival.sunnahassistant.databinding.TodayFragmentBinding
import com.thesunnahrevival.sunnahassistant.utilities.generateDateText
import com.thesunnahrevival.sunnahassistant.utilities.updateHijriDateWidgets
import com.thesunnahrevival.sunnahassistant.utilities.updateTodayRemindersWidgets
import com.thesunnahrevival.sunnahassistant.views.MainActivity
import com.thesunnahrevival.sunnahassistant.views.SwipeToDeleteCallback
import com.thesunnahrevival.sunnahassistant.views.adapters.ReminderListAdapter
import com.thesunnahrevival.sunnahassistant.views.dialogs.ReminderDetailsFragment
import com.thesunnahrevival.sunnahassistant.views.interfaces.OnDeleteReminderListener
import com.thesunnahrevival.sunnahassistant.views.interfaces.ReminderItemInteractionListener
import com.thesunnahrevival.sunnahassistant.views.showOnBoardingTutorial
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

open class TodayFragment : MenuBarFragment(), OnDeleteReminderListener, View.OnClickListener,
    ReminderItemInteractionListener {

    private lateinit var mBinding: TodayFragmentBinding
    private lateinit var mReminderRecyclerAdapter: ReminderListAdapter
    private var mAllReminders: ArrayList<Reminder> = arrayListOf()
    private var isRescheduleAtLaunch = true

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

        val myActivity = activity
        if (myActivity != null) {
            mBinding.lifecycleOwner = this
            mBinding.reminderInteractionListener = this

            getSettings()
        }
        return mBinding.root
    }

    private fun getSettings() {
        mViewModel.getSettings().observe(viewLifecycleOwner, Observer { settings: AppSettings? ->
            if (settings != null) {
                if (this !is CalendarFragment) {
                    mViewModel.settingsValue = settings
                    if (settings.isFirstLaunch) {
                        findNavController().navigate(R.id.welcomeFragment)
                        return@Observer
                    } else if (settings.isAfterUpdate) {
                        findNavController().navigate(R.id.changelogFragment)
                        return@Observer
                    }

                    if (settings.isDisplayHijriDate) {
                        mBinding.hijriDate.text = HtmlCompat.fromHtml(
                            getString(R.string.hijri_date, generateDateText()),
                            HtmlCompat.FROM_HTML_MODE_LEGACY
                        )
                        mBinding.hijriDate.visibility = View.VISIBLE
                    }

                    //Safe to call every time the app launches prayer times will only be generated once a month
                    mViewModel.updateGeneratedPrayerTimes(settings)

                    if (settings.showOnBoardingTutorial) {
                        showOnBoardingTutorial(
                            (activity as MainActivity), mReminderRecyclerAdapter,
                            mBinding.reminderList
                        )
                        settings.showOnBoardingTutorial = false
                        mViewModel.updateSettings(settings)
                    }
                }

                if (!settings.language.matches(Locale.getDefault().language.toRegex())) {
                    mViewModel.localeUpdate()
                }

                mAppSettings = settings
                setupTheRecyclerView()

                mViewModel.setDateOfReminders(System.currentTimeMillis())
                mViewModel.getReminders()
                    .observe(viewLifecycleOwner) { reminders: List<Reminder> ->
                        displayTheReminders(reminders as ArrayList<Reminder>)
                    }
            }
        })
    }

    private fun setupTheRecyclerView() {
        //Setup the RecyclerView Adapter
        context?.let {
            mReminderRecyclerAdapter = ReminderListAdapter(
                it, mAppSettings?.isExpandedLayout
                    ?: true
            )
            mReminderRecyclerAdapter.setOnItemClickListener(this)

            val reminderRecyclerView = mBinding.reminderList
            reminderRecyclerView.adapter = mReminderRecyclerAdapter
            reminderRecyclerView.itemAnimator = null

            mReminderRecyclerAdapter.setDeleteReminderListener(this)

            //Set the swipe to delete action
            val itemTouchHelper = ItemTouchHelper(SwipeToDeleteCallback(mReminderRecyclerAdapter))
            itemTouchHelper.attachToRecyclerView(reminderRecyclerView)
        }

        mViewModel.getStatusOfAddingListOfReminders().observe(viewLifecycleOwner) {
            if (!it) {
                mBinding.reminderList.visibility = View.GONE
                mBinding.progressBar.visibility = View.VISIBLE
            } else {
                mBinding.reminderList.visibility = View.VISIBLE
                mBinding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun displayTheReminders(data: ArrayList<Reminder>?) {

        val myActivity = activity
        if (myActivity != null && data != null && data.isNotEmpty()) {
            mViewModel.viewModelScope.launch(Dispatchers.IO) {
                withContext(Dispatchers.Main) {
                    mAllReminders = data
                    filterData()
                }
            }
        }
        else {
            if (data != null) {
                mReminderRecyclerAdapter.setData(data)
            }
            mBinding.allDoneView.root.visibility = View.VISIBLE
            mBinding.progressBar.visibility = View.GONE
        }
        attachListenersToRecommendedReminders()
    }

    private fun attachListenersToRecommendedReminders() {
        mBinding.allDoneView
            .sunnahRemindersLink
            .setOnClickListener(this)

        mBinding.allDoneView
            .addPrayerTimeAlerts
            .setOnClickListener(this)
    }

    override fun filterData() {
        mBinding.allDoneView.root.visibility = View.GONE
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
                            mBinding.allDoneView.root.visibility = View.GONE
                        }
                        else -> {
                            mReminderRecyclerAdapter.setData(listOf())
                            mBinding.allDoneView.root.visibility = View.VISIBLE
                        }
                    }
                }
            }
        } else {
            mBinding.progressBar.visibility = View.GONE
            mReminderRecyclerAdapter.setData(mAllReminders)
        }
    }

    override fun onPause() {
        super.onPause()
        //Save the spinner position which will be used when the app is launched again
        mAppSettings?.let { mViewModel.updateSettings(it) }
    }

    override fun deleteReminder(position: Int) {
        val mDeletedReminder = mAllReminders[position]
        val prayer = resources.getStringArray(R.array.categories)[2]

        if (mDeletedReminder.category?.matches(prayer.toRegex()) == true && mAppSettings?.isAutomatic == true) {
            Snackbar.make(
                mBinding.mainLayout, getString(R.string.cannot_delete_prayer_time),
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
            mBinding.mainLayout, getString(R.string.delete_reminder),
            Snackbar.LENGTH_LONG
        ).apply {
            view.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.fabColor))
            setAction(getString(R.string.undo_delete)) { mViewModel.addReminder(mDeletedReminder) }
            setActionTextColor(ContextCompat.getColor(requireContext(), android.R.color.black))
            show()
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.sunnah_reminders_link -> mViewModel.addInitialReminders()
            R.id.add_prayer_time_alerts -> findNavController().navigate(R.id.prayerTimeSettingsFragment)
        }
    }

    override fun onToggleButtonClick(
        buttonView: CompoundButton,
        isChecked: Boolean,
        reminder: Reminder?
    ) {
        if (reminder != null && (buttonView.isPressed || isRescheduleAtLaunch)) {
            if (isChecked) {
                mViewModel.scheduleReminder(reminder)
            } else {
                mViewModel.cancelScheduledReminder(reminder)
            }
        }
        isRescheduleAtLaunch = false
        updateHijriDateWidgets(context)
        updateTodayRemindersWidgets(context)
    }

    override fun openBottomSheet(v: View, reminder: Reminder?) {
        val bottomSheetFragment = ReminderDetailsFragment()
        mViewModel.selectedReminder = reminder
        mViewModel.settingsValue = mAppSettings
        val fm = activity?.supportFragmentManager
        fm?.let { bottomSheetFragment.show(it, "bottomSheetFragment") }


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