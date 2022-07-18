package com.thesunnahrevival.sunnahassistant.views.dialogs

import android.os.Bundle
import android.text.Html
import android.text.TextUtils
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.view.get
import androidx.core.view.size
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.nex3z.togglebuttongroup.button.CircularToggle
import com.thesunnahrevival.sunnahassistant.R
import com.thesunnahrevival.sunnahassistant.data.model.Frequency
import com.thesunnahrevival.sunnahassistant.data.model.Reminder
import com.thesunnahrevival.sunnahassistant.databinding.ReminderDetailsBottomSheetBinding
import com.thesunnahrevival.sunnahassistant.utilities.formatTimeInMilliseconds
import com.thesunnahrevival.sunnahassistant.utilities.getLocale
import com.thesunnahrevival.sunnahassistant.utilities.getTimestampInSeconds
import com.thesunnahrevival.sunnahassistant.viewmodels.SunnahAssistantViewModel
import java.lang.Integer.parseInt
import java.text.DateFormatSymbols

class ReminderDetailsFragment : BottomSheetDialogFragment(), View.OnClickListener, OnItemSelectedListener {
    private lateinit var mBinding: ReminderDetailsBottomSheetBinding
    private lateinit var mViewModel: SunnahAssistantViewModel
    private lateinit var mReminder: Reminder
    private lateinit var mCategoryAdapter: ArrayAdapter<String>
    private var mCustomScheduleDays: java.util.ArrayList<Int?>? = arrayListOf()



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = DataBindingUtil.inflate(inflater,
                R.layout.reminder_details_bottom_sheet, container, false)

        val myActivity = activity
        if (myActivity != null) {
            mViewModel = ViewModelProviders.of(myActivity).get(SunnahAssistantViewModel::class.java)
            if (mViewModel.selectedReminder == null) {
                mViewModel.selectedReminder = Reminder(reminderName = "", frequency = Frequency.OneTime,
                        category = resources.getStringArray(R.array.categories)[0]) //Uncategorized
            }

            mReminder = mViewModel.selectedReminder!!
            mBinding.reminder = mViewModel.selectedReminder
            mBinding.isNew = mReminder.reminderName.isNullOrBlank()
            mBinding.isAutomaticPrayerTime =
                    (mBinding.reminder?.category?.matches(getString(R.string.prayer).toRegex()) == true &&
                            mViewModel.settingsValue?.isAutomatic == true)
            mBinding.lifecycleOwner = this

            observeReminderTimeChange()
            setFrequencySpinnerData()
            setCategorySpinnerData()

            mBinding.reminderTime.text = formatTimeInMilliseconds(context,
                    mReminder.timeInMilliseconds)
            mBinding.tip.text = Html.fromHtml(mReminder.reminderInfo) //For in built reminders
            mBinding.tip.movementMethod = LinkMovementMethod.getInstance()

            DatePickerFragment.mDay = mReminder.day
            DatePickerFragment.mMonth = mReminder.month
            DatePickerFragment.mYear = mReminder.year

            mBinding.timePicker.setOnClickListener(this)
            mBinding.saveButton.setOnClickListener(this)
            mBinding.moreDetailsTextView.setOnClickListener(this)
        }
        return mBinding.root
    }

    private fun observeReminderTimeChange() {
        TimePickerFragment.timeSet.value = formatTimeInMilliseconds(context,
                mReminder.timeInMilliseconds)
        TimePickerFragment.timeSet.observe(this, Observer { s: String? -> mBinding.reminderTime.text = s })
    }

    private fun setFrequencySpinnerData() {
        val frequencyAdapter = ArrayAdapter.createFromResource(
                mBinding.bottomSheet.context, R.array.frequency, android.R.layout.simple_spinner_item)
        frequencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        mBinding.frequencySpinner.adapter = frequencyAdapter
        mBinding.frequencySpinner.onItemSelectedListener = this
        mBinding.frequencySpinner.setSelection(mReminder.frequency?.ordinal ?: 1)
    }

    private fun setCategorySpinnerData() {
        val settings = mViewModel.settingsValue
        if (settings?.categories != null) {
            mCategoryAdapter = ArrayAdapter(
                    mBinding.bottomSheet.context, android.R.layout.simple_spinner_item, ArrayList(settings.categories!!)
            )
            mCategoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

            if (mCategoryAdapter.getPosition(getString(R.string.create_new_categories)) == -1)
                mCategoryAdapter.add(getString(R.string.create_new_categories))

            mBinding.categorySpinner.adapter = mCategoryAdapter

            val selectedCategory = mReminder.category
            mBinding.categorySpinner.setSelection(mCategoryAdapter.getPosition(selectedCategory))
            mBinding.categorySpinner.onItemSelectedListener = this

            val prayerCategory = resources.getStringArray(R.array.categories)[2]
            if (selectedCategory?.matches(prayerCategory.toRegex()) == true && settings.isAutomatic) {
                //Disable changing category and frequency for prayer times.
                mBinding.categorySpinner.isEnabled = false
                mBinding.frequencySpinner.isEnabled = false
            }
        }
    }

    override fun onClick(v: View) {
        activity?.let { myActivity: FragmentActivity ->
            if (v.id == R.id.time_picker) {
                val selectedItem = mBinding.frequencySpinner.selectedItemPosition
                if (selectedItem == Frequency.Daily.ordinal ||
                        selectedItem == Frequency.Weekly.ordinal) { //Launch the Time picker
                    val timePickerFragment: DialogFragment = TimePickerFragment()
                    val fm = myActivity.supportFragmentManager
                    timePickerFragment.show(fm, "timePicker")
                } else { // Launch the date picker first
                    val datePickerFragment: DialogFragment = DatePickerFragment()
                    val fm = myActivity.supportFragmentManager
                    datePickerFragment.show(fm, "datePicker")
                }
            }
        }

        if (v.id == R.id.save_button) {  //Toggle More details view
            validateAndSave()
        }
        else if (v.id == R.id.more_details_text_view) {
            //Toggle More details view
            mBinding.additionalDetails.visibility =
                    if (mBinding.additionalDetails.visibility == View.GONE)
                        View.VISIBLE
                    else View.GONE
        }
    }

    override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
        when (parent.id) {
            R.id.frequency_spinner -> {
                mBinding.selectDaysToggleGroup.visibility = View.GONE
                mBinding.selectDayError.visibility = View.GONE
                mBinding.timePicker.setText(R.string.pick_time)
                if (position == 2) {
                    showSelectDaysSpinner()
                } else if (position == 3 || position == 0) {
                    mBinding.timePicker.setText(R.string.pick_date_and_time)
                }
            }
            R.id.category_spinner ->
                if ((parent.selectedItem as String).matches(getString(R.string.create_new_categories).toRegex())) {
                    val dialogFragment = AddCategoryDialogFragment()
                    fragmentManager?.let { dialogFragment.show(it, "dialog") }
                    AddCategoryDialogFragment.category.value = ""
                    val observer = Observer { category: String? ->
                        if (category?.isNotBlank() == true) {
                            mCategoryAdapter.remove(getString(R.string.create_new_categories))
                            mCategoryAdapter.add(category)
                            mCategoryAdapter.notifyDataSetChanged()
                        }
                    }
                    AddCategoryDialogFragment.category.observe(viewLifecycleOwner, observer)
                }
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {}

    private fun showSelectDaysSpinner() {
        mBinding.days = DateFormatSymbols.getInstance(getLocale()).shortWeekdays
        mBinding.selectDaysToggleGroup.visibility = View.VISIBLE
        mBinding.selectDayError.visibility = View.VISIBLE

        mReminder.customScheduleDays?.forEach {
            if (it != null && it > 0 && it <= mBinding.selectDaysToggleGroup.size) {
                val view = (mBinding.selectDaysToggleGroup[it - 1] as CircularToggle)
                mBinding.selectDaysToggleGroup.check(view.id)
            }
        }
        mBinding.selectDaysToggleGroup.setOnCheckedChangeListener { group, checkedId, isChecked ->
            val text = group.findViewById<CircularToggle>(checkedId).text
            val index = mBinding.days?.indexOf(text)
            if (index != null && isChecked)
                mCustomScheduleDays?.add(index)
            else if (index != null && !isChecked)
                mCustomScheduleDays?.remove(index)
        }
    }

    private fun validateAndSave() {
        if (TextUtils.isEmpty(mBinding.reminderEditText.text.toString().trim { it <= ' ' })) {
            Toast.makeText(context, getString(R.string.name_cannot_be_empty), Toast.LENGTH_SHORT).show()
            return
        }

        val selection = mBinding.frequencySpinner.selectedItemPosition
        var day = 0
        var month = 12
        var year = 0
        val offset = try {
            parseInt(mBinding.prayerOffsetValue.text.toString())
        }
        catch (error: NumberFormatException){
            0
        }
        val timeSet = TimePickerFragment.timeSet.value

        when (selection) {
            Frequency.Weekly.ordinal -> {
                day = -1
                if (mCustomScheduleDays.isNullOrEmpty()) {
                    //error
                    Toast.makeText(context, getString(R.string.select_atleast_one_day), Toast.LENGTH_LONG).show()
                    mBinding.selectDayError.visibility = View.VISIBLE
                    return
                }
            }
            Frequency.Monthly.ordinal -> {
                if (DatePickerFragment.mDay == 0) {
                    Toast.makeText(context, R.string.please_pick_date_and_time, Toast.LENGTH_LONG).show()
                    return
                }
                day = DatePickerFragment.mDay
            }
            Frequency.OneTime.ordinal -> {
                if (DatePickerFragment.mDay == 0 ||
                        DatePickerFragment.mMonth == 12 || DatePickerFragment.mYear == 0 ||
                        timeSet?.matches(getString(R.string.time_not_set).toRegex()) == true) {
                    Toast.makeText(context, R.string.please_pick_date_and_time, Toast.LENGTH_LONG).show()
                    return
                }
                day = DatePickerFragment.mDay
                month = DatePickerFragment.mMonth
                year = DatePickerFragment.mYear
            }
        }
        val reminder = createNewReminder(timeSet, Frequency.values()[selection], day, month, year, offset, mCustomScheduleDays)
        saveReminder(reminder)
    }

    private fun createNewReminder(timeSet: String?, frequency: Frequency, day: Int, month: Int, year: Int, offset: Int, customScheduleDays: ArrayList<Int?>?): Reminder {
        val category =
                if((mBinding.categorySpinner.selectedItem as String)
                                .matches(getString(R.string.create_new_categories).toRegex()))
            resources.getStringArray(R.array.categories)[0]
        else
            mBinding.categorySpinner.selectedItem as String

        return Reminder(
                mBinding.reminderEditText.text.toString(),
                mBinding.additionalDetails.text.toString(),
                getTimestampInSeconds(requireContext(), timeSet),
                category,
                frequency,
                timeSet?.matches(getString(R.string.time_not_set).toRegex()) == false,
                day,
                month,
                year,
                offset,
                if (mBinding.isNew) 0 else mReminder.id,
                if (frequency == Frequency.Weekly && customScheduleDays != null) customScheduleDays else arrayListOf()
        )
    }

    private fun saveReminder(reminder: Reminder) {
        val prayerCategory = resources.getStringArray(R.array.categories)[2]
        if (reminder != mReminder && reminder.category?.matches(prayerCategory.toRegex()) == false) {
            mViewModel.addReminder(reminder)
            if (mBinding.isNew)
                Toast.makeText(context, getString(R.string.successfuly_added_sunnah_reminders), Toast.LENGTH_LONG).show()
            else
                Toast.makeText(context, getString(R.string.successfully_updated), Toast.LENGTH_LONG).show()
        }
        else if (mBinding.isNew && reminder != mReminder && reminder.category?.matches(prayerCategory.toRegex()) == true &&
                mViewModel.settingsValue?.isAutomatic == true) {
            Toast.makeText(context, getString(R.string.error_adding_prayer_reminders), Toast.LENGTH_LONG).show()
            return
        }
        else if (!mBinding.isNew && reminder != mReminder && reminder.category?.matches(prayerCategory.toRegex()) == true &&
                mViewModel.settingsValue?.isAutomatic == true) {
            mViewModel.updatePrayerTimeDetails(mReminder, reminder)
        }
        else if (reminder != mReminder && reminder.category?.matches(prayerCategory.toRegex()) == true &&
                mViewModel.settingsValue?.isAutomatic == false) {
            mViewModel.addReminder(reminder)
        }
        else
            Toast.makeText(context, getString(R.string.no_changes), Toast.LENGTH_SHORT).show()
        dismiss()
    }
}