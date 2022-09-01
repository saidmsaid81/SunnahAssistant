package com.thesunnahrevival.common.views.home

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.View.*
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.thesunnahrevival.common.R
import com.thesunnahrevival.common.data.model.Frequency
import com.thesunnahrevival.common.data.model.Reminder
import com.thesunnahrevival.common.utilities.daySuffixes
import com.thesunnahrevival.common.utilities.formatTimeInMilliseconds
import com.thesunnahrevival.common.utilities.getLocale
import com.thesunnahrevival.common.utilities.getTimestampInSeconds
import com.thesunnahrevival.common.views.FragmentWithPopups
import com.thesunnahrevival.common.views.MainActivity
import com.thesunnahrevival.common.views.dialogs.AddCategoryDialogFragment
import com.thesunnahrevival.common.views.dialogs.DatePickerFragment
import com.thesunnahrevival.common.views.dialogs.DatePickerFragment.Companion.DAY
import com.thesunnahrevival.common.views.dialogs.DatePickerFragment.Companion.MONTH
import com.thesunnahrevival.common.views.dialogs.DatePickerFragment.Companion.SHOWALLMONTHS
import com.thesunnahrevival.common.views.dialogs.DatePickerFragment.Companion.YEAR
import com.thesunnahrevival.common.views.dialogs.SelectDaysDialogFragment
import com.thesunnahrevival.common.views.dialogs.SelectDaysDialogFragment.Companion.DAYS
import com.thesunnahrevival.common.views.dialogs.TimePickerFragment
import com.thesunnahrevival.common.views.dialogs.TimePickerFragment.Companion.TIMESET
import java.lang.IllegalArgumentException
import java.lang.Integer.parseInt
import java.lang.StringBuilder
import java.text.DateFormatSymbols
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.*
import kotlin.collections.ArrayList


open class ReminderDetailsFragment : FragmentWithPopups(), OnClickListener,
    SelectDaysDialogFragment.SelectDaysDialogListener, DatePickerFragment.OnDateSelectedListener,
    TimePickerFragment.OnTimeSetListener {

    protected lateinit var mBinding: com.thesunnahrevival.common.databinding.ReminderDetailsFragmentBinding
    private lateinit var mReminder: Reminder
    private var mReminderCategories: ArrayList<String> = arrayListOf()
    private var mCustomScheduleDays: TreeSet<Int> = TreeSet()
    private var isReminderDeleted = false
    private var mTimeString: String? = null
    private var mDay: Int = LocalDate.now().dayOfMonth
    private var mMonth: Int = LocalDate.now().month.ordinal
    private var mYear: Int = LocalDate.now().year

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        mBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.reminder_details_fragment, container, false
        )
        setHasOptionsMenu(true)

        return mBinding.root
    }

    override fun onResume() {
        super.onResume()
        setupView()
    }

    private fun setupView() {
        mReminder = mViewModel.selectedReminder
        (requireActivity() as MainActivity).supportActionBar?.setTitle(
            if (mReminder.id == 0)
                R.string.add_new_reminder
            else
                R.string.edit_reminder
        )
        mBinding.reminder = mReminder

        if ((mBinding.reminder?.category?.matches(getString(R.string.prayer).toRegex()) == true &&
                    mReminder.id in -1 downTo -999)
        ) {
            mBinding.isAutomaticPrayerTime = true
            mBinding.tip.text = getString(R.string.automatic_prayer_time_reminder)
        } else
            mBinding.isAutomaticPrayerTime = false

        mBinding.lifecycleOwner = viewLifecycleOwner

        setupReminderFrequencyView()
        setupReminderCategoryView()
        setupReminderDateView(mReminder.day, mReminder.month, mReminder.year)
        setUpSelectDaysView()
        setupReminderTimeView(
            formatTimeInMilliseconds(
                context,
                mReminder.timeInMilliseconds
            )
        )
        setupMarkAsCompleteView()

    }

    private fun setUpSelectDaysView() {
        mCustomScheduleDays.clear()
        mReminder.customScheduleDays?.let {
            mCustomScheduleDays.addAll(it)
        }
        updateSelectedDaysView()

        mBinding.selectDays.setOnClickListener(this)
    }

    private fun setupReminderFrequencyView() {
        mBinding.reminderFrequencyValue.text =
            resources.getStringArray(R.array.frequency)[mReminder.frequency?.ordinal ?: 0]

        when (mReminder.frequency) {
            Frequency.OneTime, Frequency.Monthly -> {
                mBinding.reminderDate.visibility = VISIBLE
                mBinding.selectDays.visibility = GONE
            }
            Frequency.Weekly -> {
                mBinding.selectDays.visibility = VISIBLE
                mBinding.reminderDate.visibility = GONE
            }
            Frequency.Daily -> {
                mBinding.selectDays.visibility = GONE
                mBinding.reminderDate.visibility = GONE
            }
            else -> {}
        }
        mBinding.reminderFrequency.setOnClickListener(this)
    }

    private fun setupReminderCategoryView() {
        mBinding.reminderCategory.setOnClickListener(this)
        mBinding.reminderCategoryValue.text = mReminder.category
        AddCategoryDialogFragment.category.value = ""
        AddCategoryDialogFragment.category.observe(viewLifecycleOwner) {
            if (it.isNotBlank())
                mBinding.reminderCategoryValue.text = it
        }
    }

    private fun setupReminderDateView(day: Int, month: Int, year: Int) {
        val reminderFrequency = mBinding.reminderFrequencyValue.text
        val frequencyOptions = resources.getStringArray(R.array.frequency)

        when (Frequency.values()[frequencyOptions.indexOf(reminderFrequency)]) {
            Frequency.OneTime -> {//No repeat
                mMonth = if (month in 0..11) month else LocalDate.now().month.ordinal
                mYear = if (year > 0) year else LocalDate.now().year

                val lengthOfMonth = LocalDate.of(mYear, mMonth, 1).lengthOfMonth()
                mDay = if (day in 1..lengthOfMonth) day else 1
                updateNoRepeatDate()
            }
            Frequency.Monthly -> { //Monthly
                mDay = if (day in 1..31) day else LocalDate.now().dayOfMonth
                updateMonthlyDate()
            }
            else -> {}
        }

        mBinding.reminderDate.setOnClickListener(this)
    }


    private fun setupReminderTimeView(timeString: String) {
        this.mTimeString = timeString
        mBinding.reminderTimeValue.text = timeString
        mBinding.reminderTime.setOnClickListener(this)
    }

    private fun setupMarkAsCompleteView() {
        mBinding.markAsComplete.setOnClickListener(this)
        val markAsCompleteOption = if (mReminder.isComplete) 1 else 0
        mBinding.markAsCompleteValue.text =
            resources.getStringArray(R.array.mark_as_complete_options)[markAsCompleteOption]
    }



    override fun onSelectDaysDialogPositiveClick(checkedDays: TreeSet<Int>) {
        if (checkedDays.isNotEmpty()) {
            mCustomScheduleDays.clear()
            mCustomScheduleDays.addAll(checkedDays)
        }
        updateSelectedDaysView()
    }

    private fun updateSelectedDaysView() {
        val stringBuilder = StringBuilder()
        for (day in mCustomScheduleDays) {
            stringBuilder.append(DateFormatSymbols.getInstance(getLocale()).shortWeekdays[day])
            if (day != mCustomScheduleDays.last())
                stringBuilder.append(", ")
        }
        if (stringBuilder.isBlank())
            mBinding.selectDaysValue.text = getString(R.string.select_atleast_one_day)
        else
            mBinding.selectDaysValue.text = stringBuilder.toString()
    }

    private fun updateNoRepeatDate() {
        val dateString = "$mDay/${mMonth + 1}/$mYear"
        val simpleDateFormat = SimpleDateFormat("", getLocale())
        simpleDateFormat.applyPattern("dd/MM/yyyy")
        val date = try {
            simpleDateFormat.parse(dateString) ?: Date()
        } catch (exception: ParseException) {
            Date()
        }

        simpleDateFormat.applyPattern("d")
        val dayDateFormatted = simpleDateFormat.format(date)
        simpleDateFormat.applyPattern("MMM, yyyy")
        mBinding.reminderDateValue.text =
            getString(
                R.string.one_time_frequency_display,
                "${daySuffixes[dayDateFormatted.toInt()]} ${simpleDateFormat.format(date)}"
            )
    }

    private fun updateMonthlyDate() {
        val dateString = "$mDay/01/2017"

        val simpleDateFormat = SimpleDateFormat("", getLocale())
        simpleDateFormat.applyPattern("dd/MM/yyyy")
        val date = try {
            simpleDateFormat.parse(dateString) ?: Date()
        } catch (exception: ParseException) {
            Date()
        }

        simpleDateFormat.applyPattern("d")
        val dayDateFormatted = simpleDateFormat.format(date)
        mBinding.reminderDateValue.text =
            getString(
                R.string.monthly_frequency_display,
                daySuffixes[dayDateFormatted.toInt()]
            )
    }

    override fun onClick(v: View?) {
        val category = mBinding.reminderCategoryValue.text.toString()
        val prayerCategory = resources.getStringArray(R.array.categories)[2]

        when (v?.id) {
            R.id.reminder_frequency -> {
                onReminderFrequencyClick(category, prayerCategory)
            }
            R.id.reminder_category -> {
                onReminderCategoryClick(category, prayerCategory)
            }
            R.id.reminder_date -> {
                onReminderDateClick()
            }
            R.id.select_days -> {
                val dialogFragment = SelectDaysDialogFragment()
                val bundle = Bundle()
                bundle.putSerializable(DAYS, mCustomScheduleDays)
                dialogFragment.arguments = bundle
                dialogFragment.setListener(this)
                dialogFragment.show(requireActivity().supportFragmentManager, "selectDays")
            }
            R.id.reminder_time -> {
                onReminderTimeClick(category, prayerCategory)
            }
            R.id.mark_as_complete -> {
                showPopup(
                    resources.getStringArray(R.array.mark_as_complete_options),
                    R.id.mark_as_complete_value, R.id.mark_as_complete
                )
            }
        }
    }

    private fun onReminderFrequencyClick(category: String, prayerCategory: String) {
        if (category.matches(prayerCategory.toRegex()) &&
            mReminder.id in -1 downTo -999
        ) {
            Toast.makeText(
                requireContext(),
                R.string.repeat_cannot_be_changed,
                Toast.LENGTH_LONG
            )
                .show()
            return
        }
        showPopup(
            resources.getStringArray(R.array.frequency),
            R.id.reminder_frequency_value, R.id.reminder_frequency
        )
    }

    private fun onReminderCategoryClick(
        category: String,
        prayerCategory: String
    ) {
        if (category.matches(prayerCategory.toRegex()) &&
            mReminder.id in -1 downTo -999
        ) {
            Toast.makeText(
                requireContext(),
                R.string.category_cannot_be_changed,
                Toast.LENGTH_LONG
            )
                .show()
            return
        }
        mViewModel.settingsValue?.categories?.let {
            mReminderCategories.clear()
            mReminderCategories.addAll(it)
            val createNewCategory = getString(R.string.create_new_categories)
            if (!mReminderCategories.last().matches(createNewCategory.toRegex())) {
                mReminderCategories.add(createNewCategory)
            }

            showPopup(
                mReminderCategories.toTypedArray(),
                R.id.reminder_category_value, R.id.reminder_category
            )
        }
    }

    private fun onReminderDateClick() {
        val selectedFrequency = mBinding.reminderFrequencyValue.text.toString()
        val frequencyOptions = resources.getStringArray(R.array.frequency)
        val frequencyValue = Frequency.values()[frequencyOptions.indexOf(selectedFrequency)]

        val datePickerFragment = DatePickerFragment()
        val bundle = Bundle()
        bundle.putInt(DAY, mDay)
        bundle.putInt(MONTH, mMonth)
        bundle.putInt(YEAR, mYear)
        bundle.putBoolean(SHOWALLMONTHS, frequencyValue != Frequency.Monthly)
        datePickerFragment.arguments = bundle
        datePickerFragment.setListener(this)
        val fragmentManager = requireActivity().supportFragmentManager
        datePickerFragment.show(fragmentManager, "datePicker")
    }

    private fun onReminderTimeClick(category: String, prayerCategory: String) {
        if (category.matches(prayerCategory.toRegex()) &&
            mReminder.id in -1 downTo -999
        ) {
            Toast.makeText(
                requireContext(),
                R.string.time_cannot_be_changed,
                Toast.LENGTH_LONG
            )
                .show()
            return
        }
        val timePickerFragment = TimePickerFragment()
        timePickerFragment.arguments = Bundle().apply { putString(TIMESET, mTimeString) }
        timePickerFragment.setListener(this)
        val fragmentManager = requireActivity().supportFragmentManager
        timePickerFragment.show(fragmentManager, "timePicker")
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        return when (item?.groupId) {
            R.id.reminder_frequency -> onFrequencyMenuItemSelected(item)
            R.id.reminder_category -> onCategoryMenuItemSelected(item)
            R.id.mark_as_complete -> {
                mBinding.markAsCompleteValue.text = item.title.toString()
                true
            }
            else -> false
        }
    }

    private fun onCategoryMenuItemSelected(item: MenuItem): Boolean {
        val createNewCategory = getString(R.string.create_new_categories)
        if (item.title.toString().matches(createNewCategory.toRegex())) {
            val dialogFragment = AddCategoryDialogFragment()
            dialogFragment.show(
                requireActivity().supportFragmentManager,
                "addCategoryDialog"
            )
        } else
            mBinding.reminderCategoryValue.text = item.title.toString()

        return true
    }

    private fun onFrequencyMenuItemSelected(item: MenuItem): Boolean {
        mBinding.selectDays.visibility = GONE
        mBinding.reminderDate.visibility = GONE

        val frequencyOptions = resources.getStringArray(R.array.frequency)

        when (Frequency.values()[frequencyOptions.indexOf(item.title.toString())]) {
            Frequency.OneTime -> {
                updateNoRepeatDate()
                mBinding.reminderDate.visibility = VISIBLE
            }
            Frequency.Weekly -> mBinding.selectDays.visibility = VISIBLE
            Frequency.Monthly -> {
                updateMonthlyDate()
                mBinding.reminderDate.visibility = VISIBLE
            }
            else -> {}
        }
        mBinding.reminderFrequencyValue.text = item.title.toString()
        return true
    }

    private fun deleteReminder() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.delete_reminder_title)
            .setMessage(R.string.delete_reminder_confirmation)
            .setPositiveButton(R.string.yes) { _, _ ->
                isReminderDeleted = true
                mViewModel.delete(mReminder)
                Toast.makeText(requireContext(), R.string.delete_reminder, Toast.LENGTH_LONG).show()
                findNavController().navigateUp()
            }
            .setNegativeButton(R.string.no) { _, _ -> }
            .show()

    }

    open fun isReminderEnabled(timeSet: String?) =
        timeSet?.matches(getString(R.string.time_not_set).toRegex()) == false

    open fun calculateOffsetForReminder(): Int {
        return try {
            parseInt(mBinding.prayerOffsetValue.text.toString())
        } catch (error: NumberFormatException) {
            0
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.reminder_menu, menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        val prayerCategory = resources.getStringArray(R.array.categories)[2]
        if (mReminder.id == 0 || mReminder.category?.matches(prayerCategory.toRegex()) == true &&
            mReminder.id in -1 downTo -999
        ) //New or Automatic prayer time
            menu.findItem(R.id.delete_reminder).title = getString(R.string.cancel)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.delete_reminder -> {
                val prayerCategory = resources.getStringArray(R.array.categories)[2]
                if ((mReminder.category?.matches(prayerCategory.toRegex()) == true &&
                            mReminder.id in -1 downTo -999) || mReminder.id == 0
                ) {
                    isReminderDeleted = true
                    findNavController().navigateUp()
                } else
                    deleteReminder()
                true
            }
            else -> false
        }
    }

    override fun onPause() {
        super.onPause()
        if (isReminderDeleted)
            return

        val newReminder = createNewReminder() ?: return
        val prayerCategory = resources.getStringArray(R.array.categories)[2]

        if (newReminder.category?.matches(prayerCategory.toRegex()) == true &&
            mReminder.id in -1 downTo -999
        ) { //Automatic prayer time
            if (newReminder.reminderName != mReminder.reminderName ||
                newReminder.reminderInfo != mReminder.reminderInfo ||
                mReminder.isEnabled != newReminder.isEnabled ||
                mReminder.offsetInMinutes != newReminder.offsetInMinutes
            ) {
                mViewModel.updatePrayerTimeDetails(mReminder, newReminder)
                Toast.makeText(
                    requireContext(), R.string.successfully_updated, Toast.LENGTH_LONG
                )
                    .show()
            }
            return
        }

        if (mReminder != newReminder) {
            mViewModel.addReminder(newReminder)
            if (newReminder.id == 0)
                Toast.makeText(
                    requireContext(), R.string.successfuly_added_sunnah_reminders, Toast.LENGTH_LONG
                )
                    .show()
            else
                Toast.makeText(
                    requireContext(), R.string.successfully_updated, Toast.LENGTH_LONG
                )
                    .show()
        }
    }

    private fun createNewReminder(): Reminder? {

        val frequencyOptions = resources.getStringArray(R.array.frequency)
        val frequency = Frequency
            .values()[frequencyOptions.indexOf(mBinding.reminderFrequencyValue.text.toString())]

        try {
            return Reminder(
                mBinding.reminderNameValue.text.toString(),
                mBinding.additionalDetails.text.toString(),
                getTimestampInSeconds(requireContext(), mTimeString),
                mBinding.reminderCategoryValue.text.toString(),
                frequency,
                isReminderEnabled(mTimeString),
                mDay,
                mMonth,
                mYear,
                calculateOffsetForReminder(),
                mReminder.id,
                mCustomScheduleDays,
                resources.getStringArray(R.array.mark_as_complete_options)
                    .indexOf(mBinding.markAsCompleteValue.text) == 1
            )
        } catch (exception: IllegalArgumentException) {
            Log.e("Exception", exception.toString())
            return null
        }
    }

    override fun onDateSelected(day: Int, month: Int, year: Int) {
        setupReminderDateView(day, month, year)
    }

    override fun onTimeSet(timeString: String) {
        setupReminderTimeView(timeString)
    }
}