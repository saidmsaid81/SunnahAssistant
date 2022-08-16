package com.thesunnahrevival.common.views.dialogs

import android.os.Bundle
import android.text.TextUtils
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.text.HtmlCompat
import androidx.core.view.get
import androidx.core.view.size
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.nex3z.togglebuttongroup.button.CircularToggle
import com.thesunnahrevival.common.R
import com.thesunnahrevival.common.data.model.Frequency
import com.thesunnahrevival.common.data.model.Reminder
import com.thesunnahrevival.common.databinding.ReminderDetailsBottomSheetBinding
import com.thesunnahrevival.common.utilities.daySuffixes
import com.thesunnahrevival.common.utilities.formatTimeInMilliseconds
import com.thesunnahrevival.common.utilities.getLocale
import com.thesunnahrevival.common.utilities.getTimestampInSeconds
import com.thesunnahrevival.common.viewmodels.SunnahAssistantViewModel
import java.lang.Integer.parseInt
import java.text.DateFormatSymbols
import java.text.SimpleDateFormat
import java.util.*

open class ReminderDetailsFragment : BottomSheetDialogFragment(), View.OnClickListener,
    OnItemSelectedListener {
    private lateinit var mBinding: ReminderDetailsBottomSheetBinding
    private lateinit var mViewModel: SunnahAssistantViewModel
    private lateinit var mReminder: Reminder
    private lateinit var mCategoryAdapter: ArrayAdapter<String>
    private var mCustomScheduleDays: ArrayList<Int?>? = arrayListOf()
    private var isUserChangingFrequency = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.reminder_details_bottom_sheet, container, false
        )

        mViewModel = ViewModelProvider(requireActivity()).get(SunnahAssistantViewModel::class.java)

        mReminder = mViewModel.selectedReminder
        mBinding.reminder = mViewModel.selectedReminder
        mBinding.isNew = mReminder.reminderName.isNullOrBlank()
        mBinding.isAutomaticPrayerTime =
            (mBinding.reminder?.category?.matches(getString(R.string.prayer).toRegex()) == true &&
                    mViewModel.settingsValue?.isAutomatic == true)
        mBinding.lifecycleOwner = viewLifecycleOwner

        observeReminderTimeChange()
        setFrequencySpinnerData()
        setCategorySpinnerData()
        setMarkAsCompleteData()

        mBinding.reminderTime.text = formatTimeInMilliseconds(
            context,
            mReminder.timeInMilliseconds
        )
        mBinding.tip.text = HtmlCompat.fromHtml(
            mReminder.reminderInfo ?: "",
            HtmlCompat.FROM_HTML_MODE_LEGACY
        ) //For in built reminders
        mBinding.tip.movementMethod = LinkMovementMethod.getInstance()

        DatePickerFragment.mDay = mReminder.day
        DatePickerFragment.mMonth = mReminder.month
        DatePickerFragment.mYear = mReminder.year
        if (!mReminder.reminderName.isNullOrBlank())
            DatePickerFragment.dateSet.value =
                "${mReminder.day}/${mReminder.month + 1}/${mReminder.year}"
        else
            DatePickerFragment.dateSet.value = null
        mBinding.timePicker.setOnClickListener(this)
        mBinding.saveButton.setOnClickListener(this)
        mBinding.moreDetailsTextView.setOnClickListener(this)

        return mBinding.root
    }

    private fun observeReminderTimeChange() {
        TimePickerFragment.timeSet.value = formatTimeInMilliseconds(
            context,
            mReminder.timeInMilliseconds
        )
        TimePickerFragment.timeSet.observe(this) { s: String? -> mBinding.reminderTime.text = s }
    }

    private fun setFrequencySpinnerData() {
        val frequencyAdapter = ArrayAdapter.createFromResource(
            mBinding.bottomSheet.context, R.array.frequency, android.R.layout.simple_spinner_item
        )
        frequencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        mBinding.frequencySpinner.adapter = frequencyAdapter
        mBinding.frequencySpinner.onItemSelectedListener = this
        mBinding.frequencySpinner.setSelection(mReminder.frequency?.ordinal ?: 1)
    }

    private fun setCategorySpinnerData() {
        val settings = mViewModel.settingsValue
        if (settings?.categories != null) {
            mCategoryAdapter = ArrayAdapter(
                mBinding.bottomSheet.context,
                android.R.layout.simple_spinner_item,
                ArrayList(settings.categories!!)
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

    private fun setMarkAsCompleteData() {
        val markAsCompleteAdapter = ArrayAdapter.createFromResource(
            mBinding.bottomSheet.context, R.array.mark_as_complete_options, android.R.layout.simple_spinner_item
        )
        markAsCompleteAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        mBinding.markAsCompletePicker.adapter = markAsCompleteAdapter
        mBinding.markAsCompletePicker.setSelection(if (mReminder.isComplete) 1 else 0)
    }


    override fun onClick(v: View) {
        activity?.let { myActivity: FragmentActivity ->
            if (v.id == R.id.time_picker) {
                val selectedItem = mBinding.frequencySpinner.selectedItemPosition
                if (selectedItem == Frequency.Daily.ordinal ||
                    selectedItem == Frequency.Weekly.ordinal
                ) { //Launch the Time picker
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
    }

    override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
        when (parent.id) {
            R.id.frequency_spinner -> {
                mBinding.selectDaysToggleGroup.visibility = View.GONE
                mBinding.selectDayError.visibility = View.GONE
                mBinding.timePicker.setText(R.string.pick_time)
                observeReminderDateChange(position)

                if (position == 2) {
                    showSelectDaysSpinner()
                }
                else if (position == 3 || position == 0) {
                    mBinding.timePicker.setText(R.string.pick_date_and_time)
                    if (isUserChangingFrequency) {
                        if (position == 0) {
                            val simpleDateFormat = SimpleDateFormat("d/M/yyyy", Locale.US)
                            val date = simpleDateFormat.format(Date()).split("/")
                            DatePickerFragment.mDay = date[0].toInt()
                            DatePickerFragment.mMonth = date[1].toInt() - 1
                            DatePickerFragment.mYear = date[2].toInt()
                            DatePickerFragment.dateSet.value =
                                "${DatePickerFragment.mDay}/${(DatePickerFragment.mMonth + 1)}/${DatePickerFragment.mYear}"
                        } else {
                            DatePickerFragment.mDay = 1
                            DatePickerFragment.mMonth = 12
                            DatePickerFragment.mYear = 0
                            DatePickerFragment.dateSet.value =
                                "${DatePickerFragment.mDay}/${(DatePickerFragment.mMonth)}/${DatePickerFragment.mYear}"
                        }

                    }
                }
                isUserChangingFrequency = true
            }
            R.id.category_spinner ->
                if ((parent.selectedItem as String).matches(getString(R.string.create_new_categories).toRegex())) {
                    val dialogFragment = AddCategoryDialogFragment()
                    requireActivity().supportFragmentManager.let {
                        dialogFragment.show(
                            it,
                            "dialog"
                        )
                    }
                    AddCategoryDialogFragment.category.value = ""
                    val observer = Observer { category: String? ->
                        if (category?.isNotBlank() == true) {
                            mCategoryAdapter.remove(getString(R.string.create_new_categories))
                            mCategoryAdapter.add(category)
                            mCategoryAdapter.notifyDataSetChanged()
                        }
                    }
                    AddCategoryDialogFragment.category.observe(this, observer)
                }
        }
    }

    private fun observeReminderDateChange(spinnerPosition: Int) {
        DatePickerFragment.dateSet.observe(this) { dateString: String? ->
            val simpleDateFormat = SimpleDateFormat("", getLocale())
            simpleDateFormat.applyPattern("dd/MM/yyyy")
            val date = if (dateString != null) simpleDateFormat.parse(dateString) else null
            mBinding.reminderDateLabel.visibility = View.VISIBLE
            mBinding.reminderDateValue.visibility = View.VISIBLE
            when {
                spinnerPosition == 1 || spinnerPosition == 2 -> {
                    mBinding.reminderDateLabel.visibility = View.GONE
                    mBinding.reminderDateValue.visibility = View.GONE
                }
                date == null -> mBinding.reminderDateValue.text =
                    getString(R.string.date_not_set)
                spinnerPosition == 0 -> { // No repeat
                    simpleDateFormat.applyPattern("d")
                    val dayDateFormatted = simpleDateFormat.format(date)
                    simpleDateFormat.applyPattern("MMM, yyyy")
                    mBinding.reminderDateValue.text =
                        getString(
                            R.string.one_time_frequency_display,
                            "${daySuffixes[dayDateFormatted.toInt()]} ${simpleDateFormat.format(date)}"
                        )
                }
                spinnerPosition == 3 -> { //Monthly
                    simpleDateFormat.applyPattern("d")
                    val dayDateFormatted = simpleDateFormat.format(date)
                    mBinding.reminderDateValue.text =
                        getString(
                            R.string.monthly_frequency_display,
                            daySuffixes[dayDateFormatted.toInt()]
                        )
                }
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
            Toast.makeText(context, getString(R.string.name_cannot_be_empty), Toast.LENGTH_SHORT)
                .show()
            return
        }

        val selection = mBinding.frequencySpinner.selectedItemPosition
        var day = 0
        var month = 12
        var year = 0
        val offset = try {
            parseInt(mBinding.prayerOffsetValue.text.toString())
        } catch (error: NumberFormatException) {
            0
        }
        val timeSet = TimePickerFragment.timeSet.value

        when (selection) {
            Frequency.Weekly.ordinal -> {
                day = -1
                if (mCustomScheduleDays.isNullOrEmpty()) {
                    //error
                    Toast.makeText(
                        context,
                        getString(R.string.select_atleast_one_day),
                        Toast.LENGTH_LONG
                    ).show()
                    mBinding.selectDayError.visibility = View.VISIBLE
                    return
                }
            }
            Frequency.Monthly.ordinal -> {
                if (DatePickerFragment.mDay == 0) {
                    Toast.makeText(context, R.string.please_pick_date_and_time, Toast.LENGTH_LONG)
                        .show()
                    return
                }
                day = DatePickerFragment.mDay
            }
            Frequency.OneTime.ordinal -> {
                if (DatePickerFragment.mDay == 0 ||
                    DatePickerFragment.mMonth == 12 || DatePickerFragment.mYear == 0 ||
                    timeSet?.matches(getString(R.string.time_not_set).toRegex()) == true
                ) {
                    Toast.makeText(context, R.string.please_pick_date_and_time, Toast.LENGTH_LONG)
                        .show()
                    return
                }
                day = DatePickerFragment.mDay
                month = DatePickerFragment.mMonth
                year = DatePickerFragment.mYear
            }
        }
        val reminder = createNewReminder(
            timeSet,
            Frequency.values()[selection],
            day,
            month,
            year,
            offset,
            mCustomScheduleDays
        )
        saveReminder(reminder)
    }

    private fun createNewReminder(
        timeSet: String?,
        frequency: Frequency,
        day: Int,
        month: Int,
        year: Int,
        offset: Int,
        customScheduleDays: ArrayList<Int?>?
    ): Reminder {
        val category =
            if ((mBinding.categorySpinner.selectedItem as String)
                    .matches(getString(R.string.create_new_categories).toRegex())
            )
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
            if (frequency == Frequency.Weekly && customScheduleDays != null) customScheduleDays else arrayListOf(),
            mBinding.markAsCompletePicker.selectedItemPosition != 0
        )
    }

    private fun saveReminder(reminder: Reminder) {
        val prayerCategory = resources.getStringArray(R.array.categories)[2]
        if (reminder != mReminder && reminder.category?.matches(prayerCategory.toRegex()) == false) {
            mViewModel.addReminder(reminder)
            if (mBinding.isNew)
                Toast.makeText(
                    context,
                    getString(R.string.successfuly_added_sunnah_reminders),
                    Toast.LENGTH_LONG
                ).show()
            else
                Toast.makeText(context, getString(R.string.successfully_updated), Toast.LENGTH_LONG)
                    .show()
        } else if (mBinding.isNew && reminder != mReminder && reminder.category?.matches(
                prayerCategory.toRegex()
            ) == true &&
            mViewModel.settingsValue?.isAutomatic == true
        ) {
            Toast.makeText(
                context,
                getString(R.string.error_adding_prayer_reminders),
                Toast.LENGTH_LONG
            ).show()
            return
        } else if (!mBinding.isNew && reminder != mReminder && reminder.category?.matches(
                prayerCategory.toRegex()
            ) == true &&
            mViewModel.settingsValue?.isAutomatic == true
        ) {
            mViewModel.updatePrayerTimeDetails(mReminder, reminder)
        } else if (reminder != mReminder && reminder.category?.matches(prayerCategory.toRegex()) == true &&
            mViewModel.settingsValue?.isAutomatic == false
        ) {
            mViewModel.addReminder(reminder)
        } else
            Toast.makeText(context, getString(R.string.no_changes), Toast.LENGTH_SHORT).show()
        dismiss()
    }
}