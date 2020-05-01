package com.thesunnahrevival.sunnahassistant.views

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.ItemTouchHelper
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetSequence
import com.getkeepsafe.taptargetview.TapTargetView
import com.google.android.material.snackbar.Snackbar
import com.thesunnahrevival.sunnahassistant.R
import com.thesunnahrevival.sunnahassistant.data.model.AppSettings
import com.thesunnahrevival.sunnahassistant.data.model.Reminder
import com.thesunnahrevival.sunnahassistant.databinding.ContentMainBinding
import com.thesunnahrevival.sunnahassistant.utilities.NotificationUtil
import com.thesunnahrevival.sunnahassistant.utilities.SunnahAssistantUtil
import com.thesunnahrevival.sunnahassistant.utilities.TimeDateUtil
import com.thesunnahrevival.sunnahassistant.viewmodels.RemindersViewModel
import com.thesunnahrevival.sunnahassistant.views.adapters.ReminderListAdapter
import com.thesunnahrevival.sunnahassistant.views.interfaces.OnDeleteReminderListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainFragment : Fragment(), OnItemSelectedListener, OnDeleteReminderListener, View.OnClickListener {

    private lateinit var mBinding: ContentMainBinding
    private lateinit var mViewModel: RemindersViewModel
    private lateinit var mReminderRecyclerAdapter: ReminderListAdapter
    private lateinit var mAllReminders: List<Reminder>
    private var mAppSettings: AppSettings? = null
    private var mSpinner: Spinner? = null
    private var mIsFetchError = false
    private lateinit var mainActivity : MainActivity
    private var mIsFirstLaunch = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = DataBindingUtil.inflate(
                inflater, R.layout.content_main, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if (activity is MainActivity)
            mainActivity = activity as MainActivity
        val myActivity = activity
        if (myActivity != null) {
            mViewModel = ViewModelProviders.of(myActivity).get(RemindersViewModel::class.java)
            mBinding.lifecycleOwner = this
            mBinding.viewmodel = mViewModel

            getSettings()
            displayApiFetchMessages()
        }
    }

    private fun getSettings() {
        mainActivity.mViewModel?.settings?.observe(mainActivity, Observer { settings: AppSettings? ->
            if (settings != null) {
                mAppSettings = settings
                setTheme()
                populateTheSpinner(settings.savedSpinnerPosition)
                setupTheRecyclerView()
                if (settings.isDisplayHijriDate) {
                    mBinding.hijriDate.text = Html.fromHtml(
                            "<b>Today's Date:</b>  " +
                                    TimeDateUtil.getHijriDate() + " A.H")
                    mBinding.hijriDate.visibility = View.VISIBLE
                }
                else
                    mBinding.hijriDate.visibility = View.GONE


                if (settings.isFirstLaunch) {
                    NotificationUtil.createNotificationChannels(context)
                    mIsFirstLaunch = true
                    mainActivity.mViewModel.updateIsFirstLaunch(false)
                    startActivity(Intent(activity, WelcomeActivity::class.java))
                    return@Observer
                }
                if (!mIsFetchError && !mIsFirstLaunch )
                    mainActivity.mViewModel.fetchAllAladhanData()

                if (settings.isShowOnBoardingTutorial && !mIsFirstLaunch) {
                    showOnBoardingTutorial()
                    mainActivity.mViewModel.updateIsShowOnBoardingTutorial(false)
                }
            }
        })
        }

    private fun setupTheRecyclerView() {
        //Setup the RecyclerView Adapter
        context?.let {
            mReminderRecyclerAdapter = ReminderListAdapter(context, mAppSettings?.isExpandedLayout ?: true)
            mReminderRecyclerAdapter.setOnItemClickListener(mViewModel)
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

    }

    private fun showOnBoardingTutorial() {
        TapTargetSequence(activity)
                .targets(
                        TapTarget.forView(activity?.findViewById(R.id.fab), getString(R.string.add_new_reminder), getString(R.string.add_new_reminder_description))
                                .outerCircleColor(android.R.color.holo_blue_dark)
                                .cancelable(false)
                                .textColor(R.color.bottomSheetColor)
                                .transparentTarget(true),
                        TapTarget.forView(mSpinner, getString(R.string.spinner_tutorial), getString(R.string.spinner_tutorial_description))
                                .outerCircleColor(android.R.color.holo_blue_dark)
                                .cancelable(false)
                                .textColor(R.color.bottomSheetColor)
                                .transparentTarget(true),
                        TapTarget.forToolbarOverflow(activity?.findViewById<View>(R.id.toolbar) as Toolbar,
                                getString(R.string.change_theme),
                                getString(R.string.change_theme_description))
                                .outerCircleColor(android.R.color.holo_blue_dark)
                                .transparentTarget(true)
                                .cancelable(false)
                                .textColor(R.color.bottomSheetColor)
                                .tintTarget(true),
                            TapTarget.forView(
                                    mBinding.reminderList,
                                    getString(R.string.edit_reminder),
                                    getString(R.string.edit_reminder_description))
                                    .outerCircleColor(android.R.color.holo_blue_dark)
                                    .cancelable(false)
                                    .tintTarget(true)
                                    .textColor(R.color.bottomSheetColor)
                                    .transparentTarget(true))
                                    .listener(object : TapTargetSequence.Listener {
                                        override fun onSequenceFinish() {
                                            showFilterReminderOnBoarding()
                                        }
                                        override fun onSequenceStep(lastTarget: TapTarget, targetClicked: Boolean) {}
                                        override fun onSequenceCanceled(lastTarget: TapTarget) {}
                                    })
                .start()

    }

    private fun showFilterReminderOnBoarding() {
        TapTargetView.showFor(
                activity,
                TapTarget.forToolbarMenuItem(
                        activity?.findViewById<View>(R.id.toolbar) as Toolbar,
                        R.id.filter,
                        getString(R.string.filter_displayed_reminders),
                        getString(R.string.filter_displayed_reminders_description))
                        .cancelable(false)
                        .outerCircleColor(android.R.color.holo_blue_dark)
                        .textColor(R.color.bottomSheetColor)
                        .tintTarget(true), object : TapTargetView.Listener() {
                            override fun onTargetClick(view: TapTargetView) {
                                mReminderRecyclerAdapter.deleteReminder(0)
                                view.dismiss(true)
                            }
        })
    }

    private fun displayApiFetchMessages() {
        mViewModel.errorMessages.observe(this, Observer { s: String? ->
            val snackbar = Snackbar.make(mBinding.mainLayout, "", Snackbar.LENGTH_INDEFINITE)

            if (!(s == null || s.isEmpty() || s.matches("Successful".toRegex()) || s.contains("Refreshing"))) {
                //Error occurred
                mIsFetchError = true

                //Reset the month to 0 enabling refetching of the data when user selects refresh on the snackbar displayed
                mViewModel.month = 0
                mainActivity.mViewModel?.updateSavedMonth()

                snackbar.setText(s)
                snackbar.setAction("Refresh") {
                    mainActivity.mViewModel?.fetchAllAladhanData()
                    mIsFetchError = false
                }
                snackbar.show()
            } else if (s != null && s.matches("Successful".toRegex()) && mAppSettings != null) //Successful
                snackbar.dismiss() else if (s != null && s.contains("Refreshing")) //Ongoing
                Toast.makeText(context, s, Toast.LENGTH_LONG).show()
        })
    }

    private fun setTheme() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) //Android 10 and above
            return  //Follow Device Settings
        if (mAppSettings?.isLightMode != false)
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        else
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES) //Dark Mode
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
            mAllReminders = data
            if (mSpinner?.selectedItemPosition == 0 || mSpinner?.selectedItemPosition == 2)
                displayTheNextReminder(data)
            filterCategories(myActivity, data)

            //Used to maintain category filter when user changes frequency
            mainActivity.mFilteredReminderCategories.value = mainActivity.mFilteredReminderCategories.value
        }
        else  {
            mBinding.nextReminder = null
            mReminderRecyclerAdapter.setData(data, mBinding.spinner.selectedItemPosition)
            myActivity?.findViewById<View>(R.id.all_done_view)?.visibility = View.VISIBLE
            mBinding.progressBar.visibility = View.GONE
        }
        attachListenersToRecommendedReminders(myActivity)

    }

    private fun attachListenersToRecommendedReminders(myActivity: FragmentActivity?) {
        myActivity?.findViewById<View>(R.id.all_done_view)?.
                findViewById<TextView>(R.id.sunnah_reminders_link)
                ?.setOnClickListener(this)

        myActivity?.findViewById<View>(R.id.all_done_view)
                ?.findViewById<TextView>(R.id.add_prayer_time_alerts)
                ?.setOnClickListener(this)
    }

    private fun filterCategories(myActivity: FragmentActivity, data: ArrayList<Reminder>) {
        mainActivity.mFilteredReminderCategories?.observe(this, Observer { categoryToDisplay ->
            myActivity.findViewById<View>(R.id.all_done_view)?.visibility = View.GONE
            mBinding.progressBar.visibility = View.VISIBLE
            //Refresh the RecyclerView
            if (!categoryToDisplay.matches("".toRegex())) {
                //Filter data in background thread because if the list is very large it may slow down the main thread
                CoroutineScope(Dispatchers.Default).launch {
                    val filteredData = data.filter { it.category.matches(categoryToDisplay.toRegex()) }
                    withContext(Dispatchers.Main) {
                        mBinding.progressBar.visibility = View.GONE
                        when {
                            filteredData.isNotEmpty() -> {
                                mReminderRecyclerAdapter.setData(filteredData, mBinding.spinner.selectedItemPosition)
                                myActivity.findViewById<View>(R.id.all_done_view)?.visibility = View.GONE
                            }
                            else -> {
                                mReminderRecyclerAdapter.setData(listOf(), mBinding.spinner.selectedItemPosition)
                                myActivity.findViewById<View>(R.id.all_done_view)?.visibility = View.VISIBLE
                            }
                        }
                    }
                }
            }

            else {
                mBinding.progressBar.visibility = View.GONE
                mReminderRecyclerAdapter.setData(data, mBinding.spinner.selectedItemPosition)
            }
        })
    }

    private fun displayTheNextReminder(data: ArrayList<Reminder>) {
        mBinding.nextReminder = null

        if (data[0].isEnabled && data[0].timeInSeconds  > TimeDateUtil.calculateOffsetFromMidnight()) {
            mBinding.nextReminder = data[0]
            data.remove(data[0]) //Remove The Next Reminder
        }
        else {
            mBinding.nextReminder = data.firstOrNull { it.isEnabled && it.timeInSeconds  > TimeDateUtil.calculateOffsetFromMidnight() }
        }
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

    override fun onDestroy() {
        super.onDestroy()

        //Save the spinner position which will be used when the app is launched again
        mainActivity.mViewModel?.updateSavedSpinnerPosition(mSpinner?.selectedItemPosition ?: 0)
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        mViewModel.getReminders(position)
                .observe(this, Observer {
                    reminders: List<Reminder>? ->
                    changeDisplayedData(reminders as ArrayList<Reminder>?, mSpinner?.getItemAtPosition(position) as String) })
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {}

    override fun deleteReminder(position: Int) {
        val mDeletedReminder = mAllReminders[position]
        if (mDeletedReminder.category.matches(SunnahAssistantUtil.PRAYER.toRegex())) {
            val snackbar = Snackbar.make(mBinding.mainLayout, getString(R.string.cannot_delete_prayer_time),
                    Snackbar.LENGTH_LONG)
            snackbar.show()
            mReminderRecyclerAdapter.notifyDataSetChanged()
            return
        }
        if (mDeletedReminder.isEnabled)
            mViewModel.cancelScheduledReminder(mDeletedReminder)

        mViewModel.delete(mDeletedReminder)
        val snackbar = Snackbar.make(mBinding.mainLayout, getString(R.string.delete_reminder),
                Snackbar.LENGTH_LONG)
        snackbar.setAction(getString(R.string.undo_delete)) { mViewModel.insert(mDeletedReminder) }
        snackbar.show()
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.sunnah_reminders_link -> mViewModel.addInitialReminders()
            R.id.add_prayer_time_alerts -> {
                val intent = Intent(context, SettingsActivity::class.java)
                intent.putExtra("fragmentToShow", "prayer")
                startActivity(intent)
            }
        }
    }

}