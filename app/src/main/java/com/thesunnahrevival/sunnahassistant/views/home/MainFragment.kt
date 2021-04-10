package com.thesunnahrevival.sunnahassistant.views.home

import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import com.google.android.material.snackbar.Snackbar
import com.thesunnahrevival.sunnahassistant.R
import com.thesunnahrevival.sunnahassistant.data.model.AppSettings
import com.thesunnahrevival.sunnahassistant.data.model.Reminder
import com.thesunnahrevival.sunnahassistant.databinding.ContentMainBinding
import com.thesunnahrevival.sunnahassistant.utilities.*
import com.thesunnahrevival.sunnahassistant.viewmodels.SunnahAssistantViewModel
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

class MainFragment : MenuBarFragment(), OnItemSelectedListener, OnDeleteReminderListener, View.OnClickListener, ReminderItemInteractionListener {

    private lateinit var mBinding: ContentMainBinding
    private lateinit var mReminderRecyclerAdapter: ReminderListAdapter
    private var mAllReminders: ArrayList<Reminder> = arrayListOf()
    private var mSpinner: Spinner? = null
    private var isRescheduleAtLaunch = true
    var nextScheduledReminder: Reminder? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = DataBindingUtil.inflate(
                inflater, R.layout.content_main, container, false)
        setHasOptionsMenu(true)

        val myActivity = activity
        if (myActivity != null) {
            mViewModel = ViewModelProviders.of(myActivity).get(SunnahAssistantViewModel::class.java)

            mBinding.lifecycleOwner = this
            mBinding.reminderInteractionListener = this

            getSettings()
        }
        return mBinding.root
    }


    private fun getSettings() {
        mViewModel.getSettings().observe(viewLifecycleOwner, Observer { settings: AppSettings? ->
            if (settings != null) {
                mAppSettings = settings
                mViewModel.settingsValue = settings
                if (settings.isFirstLaunch) {
                    findNavController().navigate(R.id.welcomeFragment)
                    return@Observer
                } else if (settings.isAfterUpdate) {
                    findNavController().navigate(R.id.changelogFragment)
                    return@Observer
                }

                if (!settings.language.matches(Locale.getDefault().language.toRegex())) {
                    mViewModel.localeUpdate()
                }

                populateTheSpinner(settings.savedSpinnerPosition)
                setupTheRecyclerView()

                if (settings.isDisplayHijriDate) {
                    mBinding.hijriDate.text = Html.fromHtml(
                            getString(R.string.hijri_date, hijriDate))
                    mBinding.hijriDate.visibility = View.VISIBLE
                }

                //Safe to call every time the app launches prayer times will only be generated once a month
                mViewModel.updateGeneratedPrayerTimes(settings)

                if (settings.showOnBoardingTutorial) {
                    mSpinner?.let {
                        showOnBoardingTutorial((activity as MainActivity), mReminderRecyclerAdapter,
                                it, mBinding.reminderList)
                    }
                    settings.showOnBoardingTutorial = false
                    mViewModel.updateSettings(settings)
                }
            }
        })
    }

    private fun setupTheRecyclerView() {
        //Setup the RecyclerView Adapter
        context?.let {
            mReminderRecyclerAdapter = ReminderListAdapter(it, mAppSettings?.isExpandedLayout
                    ?: true)
            mReminderRecyclerAdapter.setOnItemClickListener(this)

            val reminderRecyclerView = mBinding.reminderList
            reminderRecyclerView.adapter = mReminderRecyclerAdapter

            mReminderRecyclerAdapter.setDeleteReminderListener(this)

            //Set the swipe to delete action
            val itemTouchHelper = ItemTouchHelper(SwipeToDeleteCallback(mReminderRecyclerAdapter))
            itemTouchHelper.attachToRecyclerView(reminderRecyclerView)

            //Setup the layout of the next reminder card view
            if (mAppSettings?.isExpandedLayout == false)
                mBinding.nextCardView.viewStub?.layoutResource = R.layout.alt_next_reminder_card_view
            mBinding.nextCardView.viewStub?.inflate()
        }

        mViewModel.getStatusOfAddingListOfReminders().observe(viewLifecycleOwner, Observer {
            if (!it) {
                mBinding.reminderList.visibility = View.GONE
                mBinding.progressBar.visibility = View.VISIBLE
            }
            else {
                mBinding.reminderList.visibility = View.VISIBLE
                mBinding.progressBar.visibility = View.GONE
            }
        })

    }

    private fun populateTheSpinner(savedSpinnerPosition: Int) {
        context?.let {
            val adapter = ArrayAdapter.createFromResource(it,
                    R.array.reminder_filter, android.R.layout.simple_spinner_item)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            mSpinner = mBinding.spinner
            mSpinner?.onItemSelectedListener = this
            mSpinner?.adapter = adapter
            mSpinner?.setSelection(savedSpinnerPosition)
        }
    }

    /**
     * Changes The Displayed data
     *
     * @param tag spinner Item
     */
    private fun changeDisplayedData(data: ArrayList<Reminder>?, tag: String) {
        //Checks to see if the data that changed affects what is being displayed by spinner selection
        //Returns early if it does not affect displayed data
        if (!(mSpinner?.selectedItem as String).matches(tag.toRegex()))
            return

        val myActivity = activity
        if (myActivity != null && data != null && data.isNotEmpty()) {
            displayTheReminders(data)
        }

        else {
            if (data != null) {
                mReminderRecyclerAdapter.setData(data, mBinding.spinner.selectedItemPosition)
            }
            mBinding.allDoneView.root.visibility = View.VISIBLE
            mBinding.progressBar.visibility = View.GONE
        }
        attachListenersToRecommendedReminders()

    }

    private fun displayTheReminders(data: ArrayList<Reminder>) {
        mViewModel.viewModelScope.launch(Dispatchers.IO) {
            var dayString = getString(R.string.today_at)
            nextScheduledReminder = mViewModel.getNextScheduledReminderToday(calculateOffsetFromMidnight(),
                     getDayDate(System.currentTimeMillis()),
                    getMonthNumber(System.currentTimeMillis()), Integer.parseInt(getYear(System.currentTimeMillis())))
            if (nextScheduledReminder == null) {
                val timeInMilliseconds = System.currentTimeMillis() + 86400000
                nextScheduledReminder = mViewModel.getNextScheduledReminderTomorrow(
                        getDayDate(timeInMilliseconds),
                        getMonthNumber(timeInMilliseconds), Integer.parseInt(getYear(timeInMilliseconds)))
                dayString = getString(R.string.tomorrow_at)
            }
            withContext(Dispatchers.Main) {
                if (mSpinner?.selectedItemPosition == 0)
                    data.remove(nextScheduledReminder)
                mAllReminders = data
                mBinding.nextReminder = nextScheduledReminder
                mBinding.dayString = dayString
                filterData()
            }
        }
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
            //Filter data in background thread because if the list is very large it may slow down the main thread
            CoroutineScope(Dispatchers.Default).launch {
                val filteredData = mAllReminders.filter {
                    it.category?.matches(categoryToDisplay.toRegex()) == true
                }
                withContext(Dispatchers.Main) {
                    mBinding.progressBar.visibility = View.GONE
                    when {
                        filteredData.isNotEmpty() -> {
                            mReminderRecyclerAdapter.setData(filteredData, mBinding.spinner.selectedItemPosition)
                            mBinding.allDoneView.root.visibility = View.GONE
                        }
                        else -> {
                            mReminderRecyclerAdapter.setData(listOf(), mBinding.spinner.selectedItemPosition)
                            mBinding.allDoneView.root.visibility = View.VISIBLE
                        }
                    }
                }
            }
        } else {
            mBinding.progressBar.visibility = View.GONE
            mReminderRecyclerAdapter.setData(mAllReminders, mBinding.spinner.selectedItemPosition)
        }

    }

    override fun onPause() {
        super.onPause()
        //Save the spinner position which will be used when the app is launched again
        mAppSettings?.savedSpinnerPosition = mSpinner?.selectedItemPosition ?: 0
        mAppSettings?.let { mViewModel.updateSettings(it) }
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        mViewModel.getReminders(position)
                .observe(this, Observer { reminders: List<Reminder> ->
                    changeDisplayedData(reminders as ArrayList<Reminder>, mSpinner?.getItemAtPosition(position) as String)
                })
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {}

    override fun deleteReminder(position: Int) {
        val mDeletedReminder = mAllReminders[position]
        val prayer = resources.getStringArray(R.array.categories)[2]

        if (mDeletedReminder.category?.matches(prayer.toRegex()) == true && mAppSettings?.isAutomatic == true) {
            val snackbar = Snackbar.make(mBinding.mainLayout, getString(R.string.cannot_delete_prayer_time),
                    Snackbar.LENGTH_LONG)
            snackbar.show()
            mReminderRecyclerAdapter.notifyDataSetChanged()
            return
        }
        if (mDeletedReminder.isEnabled)
            mViewModel.cancelScheduledReminder(mDeletedReminder)

        if (mDeletedReminder == nextScheduledReminder) {
            mAllReminders.remove(mDeletedReminder)
            displayTheReminders(mAllReminders)
        }

        mViewModel.delete(mDeletedReminder)
        val snackbar = Snackbar.make(mBinding.mainLayout, getString(R.string.delete_reminder),
                Snackbar.LENGTH_LONG)
        snackbar.setAction(getString(R.string.undo_delete)) { mViewModel.insert(mDeletedReminder) }
        snackbar.show()

    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.sunnah_reminders_link -> mViewModel.addInitialReminders()
            R.id.add_prayer_time_alerts -> findNavController().navigate(R.id.prayerTimeSettingsFragment)
        }
    }

    override fun onToggleButtonClick(buttonView: CompoundButton, isChecked: Boolean, reminder: Reminder?) {
        if (reminder != null &&(buttonView.isPressed || isRescheduleAtLaunch)) {
            if (isChecked) {
                mViewModel.scheduleReminder(reminder)
            }
            else {
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
}