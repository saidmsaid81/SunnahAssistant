package com.thesunnahrevival.common.views.home

import android.app.AlertDialog
import android.app.PendingIntent
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.*
import android.widget.Toast
import androidx.browser.customtabs.CustomTabsClient
import androidx.browser.customtabs.CustomTabsIntent
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.thesunnahrevival.common.R
import com.thesunnahrevival.common.data.model.Frequency
import com.thesunnahrevival.common.data.model.Reminder
import com.thesunnahrevival.common.receivers.InAppBrowserBroadcastReceiver
import com.thesunnahrevival.common.receivers.MESSAGE
import com.thesunnahrevival.common.services.InAppBrowserConnection
import com.thesunnahrevival.common.utilities.*
import com.thesunnahrevival.common.views.FragmentWithPopups
import com.thesunnahrevival.common.views.MainActivity
import com.thesunnahrevival.common.views.dialogs.AddCategoryDialogFragment
import com.thesunnahrevival.common.views.dialogs.DatePickerFragment
import com.thesunnahrevival.common.views.dialogs.SelectDaysDialogFragment
import com.thesunnahrevival.common.views.dialogs.TimePickerFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.IllegalArgumentException
import java.lang.StringBuilder
import java.text.DateFormatSymbols
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.*

open class ReminderDetailsFragment : FragmentWithPopups(), View.OnClickListener,
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
    private var mShareIcon: Bitmap? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)

        mBinding = DataBindingUtil.inflate(
            inflater, R.layout.reminder_details_fragment, container, false
        )
        setHasOptionsMenu(true)

        return mBinding.root
    }

    override fun onResume() {
        super.onResume()

        CustomTabsClient.bindCustomTabsService(
            requireContext(),
            requireContext().packageName,
            InAppBrowserConnection()
        )
        CoroutineScope(Dispatchers.Default).launch {
            mShareIcon = BitmapFactory.decodeResource(resources, android.R.drawable.ic_menu_share)
        }

        mReminder = mViewModel.selectedReminder
        (requireActivity() as MainActivity).supportActionBar?.setTitle(
            if (mReminder.id == 0)
                R.string.add_new_reminder
            else
                R.string.edit_reminder
        )
        mBinding.remindersName = mReminder.reminderName
        mBinding.reminderInfo = mReminder.reminderInfo
        mBinding.offsetInMinutes = mReminder.offsetInMinutes
        mBinding.predefinedReminderInfo = mReminder.predefinedReminderInfo.ifBlank { null }

        if (mReminder.isAutomaticPrayerTime()) {
            mBinding.isAutomaticPrayerTime = true

            val timeInMillis = GregorianCalendar.getInstance()
                .apply { set(mReminder.year, mReminder.month, mReminder.day) }
                .timeInMillis

            val formattedDate = SimpleDateFormat("EEEE d MMMM, yyyy", getLocale())
                .format(Date(timeInMillis))

            mBinding.tip.text = getString(
                R.string.automatic_prayer_time_reminder,
                mReminder.reminderName,
                formattedDate
            )
        } else
            mBinding.isAutomaticPrayerTime = false

        mBinding.lifecycleOwner = viewLifecycleOwner

        mCustomScheduleDays.clear()
        mReminder.customScheduleDays?.let {
            mCustomScheduleDays.addAll(it)
        }

        updateView()
    }

    private fun updateView() {
        updateReminderFrequencyView(mReminder.frequency?.ordinal ?: 0)
        updateReminderCategoryView(mReminder.category)
        updateReminderTimeView(formatTimeInMilliseconds(context, mReminder.timeInMilliseconds))
        updateMarkAsCompleteView(if (mReminder.isComplete) 1 else 0)
        if (mReminder.predefinedReminderInfo.isNotBlank())
            mBinding.tip.text = mReminder.predefinedReminderInfo

        if (Patterns.WEB_URL.matcher(mReminder.predefinedReminderLink).matches())
            mBinding.tipView.setOnClickListener(this)
    }

    private fun updateReminderFrequencyView(frequencyOrdinal: Int) {
        mBinding.reminderFrequencyValue.text =
            resources.getStringArray(R.array.frequency)[frequencyOrdinal]
        mBinding.selectedFrequency = frequencyOrdinal
        mBinding.reminderFrequency.setOnClickListener(this)

        updateReminderDateView(mReminder.day, mReminder.month, mReminder.year)
        updateSelectedDaysView()
    }


    private fun updateReminderFrequencyView(frequencyString: String) {
        val ordinal = resources.getStringArray(R.array.frequency).indexOf(frequencyString)
        updateReminderFrequencyView(ordinal)
    }

    private fun updateReminderCategoryView(selectedCategory: String?) {
        mBinding.reminderCategoryValue.text = selectedCategory
        AddCategoryDialogFragment.category.value = ""
        AddCategoryDialogFragment.category.observe(viewLifecycleOwner) {
            if (it.isNotBlank())
                mBinding.reminderCategoryValue.text = it
        }
        mBinding.reminderCategory.setOnClickListener(this)
    }

    private fun updateReminderDateView(day: Int, month: Int, year: Int) {
        when (Frequency.values()[mBinding.selectedFrequency]) {
            Frequency.OneTime -> {//No repeat
                mMonth = if (month in 0..11) month else LocalDate.now().month.ordinal
                mYear = if (year > 0) year else LocalDate.now().year

                val lengthOfMonth = LocalDate.of(mYear, mMonth + 1, 1).lengthOfMonth()
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

    private fun updateSelectedDaysView() {
        val frequencyOrdinal = mBinding.selectedFrequency

        if (Frequency.values()[frequencyOrdinal] == Frequency.Weekly) {
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

            mBinding.selectDays.setOnClickListener(this)
        }
    }

    private fun updateReminderTimeView(timeString: String) {
        mTimeString = timeString
        mBinding.reminderTimeValue.text = timeString
        mBinding.reminderTime.setOnClickListener(this)
    }

    private fun updateMarkAsCompleteView(markAsCompleteString: String) {
        mBinding.markAsCompleteValue.text = markAsCompleteString
        mBinding.markAsComplete.setOnClickListener(this)
    }

    private fun updateMarkAsCompleteView(markAsCompleteOption: Int) {
        val markAsCompleteString =
            resources.getStringArray(R.array.mark_as_complete_options)[markAsCompleteOption]
        updateMarkAsCompleteView(markAsCompleteString)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.reminder_frequency -> {
                if (!isAutomaticPrayerTime(R.string.repeat_cannot_be_changed))
                    showPopup(
                        resources.getStringArray(R.array.frequency), R.id.reminder_frequency_value,
                        R.id.reminder_frequency
                    )
            }
            R.id.reminder_category -> {
                if (!isAutomaticPrayerTime(R.string.category_cannot_be_changed))
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
            R.id.reminder_date -> {
                val datePickerFragment = DatePickerFragment()
                val bundle = Bundle().apply {
                    putInt(DatePickerFragment.DAY, mDay)
                    putInt(DatePickerFragment.MONTH, mMonth)
                    putInt(DatePickerFragment.YEAR, mYear)
                    putBoolean(
                        DatePickerFragment.SHOWALLMONTHS,
                        mBinding.selectedFrequency == Frequency.OneTime.ordinal
                    )
                }
                datePickerFragment.arguments = bundle
                datePickerFragment.setListener(this)
                val fragmentManager = requireActivity().supportFragmentManager
                datePickerFragment.show(fragmentManager, "datePicker")
            }
            R.id.select_days -> {
                val dialogFragment = SelectDaysDialogFragment()
                val bundle = Bundle().apply {
                    putSerializable(SelectDaysDialogFragment.DAYS, mCustomScheduleDays)
                }
                dialogFragment.arguments = bundle
                dialogFragment.setListener(this)
                dialogFragment.show(requireActivity().supportFragmentManager, "selectDays")
            }
            R.id.reminder_time -> {
                if (!isAutomaticPrayerTime(R.string.time_cannot_be_changed)) {
                    val timePickerFragment = TimePickerFragment()
                    timePickerFragment.arguments =
                        Bundle().apply { putString(TimePickerFragment.TIMESET, mTimeString) }

                    timePickerFragment.setListener(this)
                    val fragmentManager = requireActivity().supportFragmentManager
                    timePickerFragment.show(fragmentManager, "timePicker")
                }
            }
            R.id.mark_as_complete -> {
                showPopup(
                    resources.getStringArray(R.array.mark_as_complete_options),
                    R.id.mark_as_complete_value, R.id.mark_as_complete
                )
            }
            R.id.tip_view -> launchInAppBrowser()
        }
    }

    private fun launchInAppBrowser() {
        val builder = CustomTabsIntent.Builder()

        val actionIntent = Intent(
            requireContext(), InAppBrowserBroadcastReceiver::class.java
        )
        actionIntent.putExtra(
            Intent.EXTRA_REFERRER,
            Uri.parse("android-app://" + requireContext().packageName)
        );
        actionIntent.putExtra(MESSAGE, mReminder.predefinedReminderInfo)

        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(), 0, actionIntent, 0
        )
        builder.addMenuItem(
            getString(R.string.share_message),
            pendingIntent
        )

        val shareIcon = mShareIcon
        if (shareIcon != null)
            builder.setActionButton(
                shareIcon,
                getString(R.string.share_message),
                pendingIntent,
                true
            )
        val customTabsIntent = builder.build()
        customTabsIntent.launchUrl(requireContext(), Uri.parse(mReminder.predefinedReminderLink))
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        return when (item?.groupId) {
            R.id.reminder_frequency -> {
                updateReminderFrequencyView(item.title.toString())
                true
            }
            R.id.reminder_category -> {
                val createNewCategory = getString(R.string.create_new_categories)
                if (item.title.toString().matches(createNewCategory.toRegex())) {
                    val dialogFragment = AddCategoryDialogFragment()
                    dialogFragment.show(
                        requireActivity().supportFragmentManager,
                        "addCategoryDialog"
                    )
                } else
                    updateReminderCategoryView(item.title.toString())

                return true
            }
            R.id.mark_as_complete -> {
                updateMarkAsCompleteView(item.title.toString())
                true
            }
            else -> false
        }
    }

    override fun onSelectDaysDialogPositiveClick(checkedDays: TreeSet<Int>) {
        if (checkedDays.isNotEmpty()) {
            mCustomScheduleDays.clear()
            mCustomScheduleDays.addAll(checkedDays)
        }
        updateSelectedDaysView()
    }

    private fun isAutomaticPrayerTime(stringId: Int): Boolean {
        if (mBinding.isAutomaticPrayerTime) {
            Toast.makeText(
                requireContext(),
                stringId,
                Toast.LENGTH_LONG
            ).show()
            return true
        }
        return false
    }

    override fun onDateSelected(day: Int, month: Int, year: Int) {
        updateReminderDateView(day, month, year)
    }

    override fun onTimeSet(timeString: String) {
        updateReminderTimeView(timeString)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.reminder_menu, menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        if (mReminder.id == 0 || mReminder.isAutomaticPrayerTime()) //New or Automatic prayer time
            menu.findItem(R.id.delete_reminder).title = getString(R.string.cancel)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.delete_reminder -> {
                if (mReminder.isAutomaticPrayerTime() || mReminder.id == 0) {
                    isReminderDeleted = true
                    findNavController().navigateUp()
                } else
                    deleteReminder()
                true
            }
            else -> false
        }
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

    override fun onPause() {
        super.onPause()
        if (isReminderDeleted)
            return

        val newReminder = createNewReminder() ?: return

        if (mReminder.isAutomaticPrayerTime()) { //Automatic prayer time
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
                ).show()
            else
                Toast.makeText(
                    requireContext(), R.string.successfully_updated, Toast.LENGTH_LONG
                ).show()
        }
    }

    private fun createNewReminder(): Reminder? {
        val frequency = Frequency.values()[mBinding.selectedFrequency]

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
                    .indexOf(mBinding.markAsCompleteValue.text) == 1,
                mReminder.predefinedReminderInfo,
                mReminder.predefinedReminderLink
            )
        } catch (exception: IllegalArgumentException) {
            Log.e("Exception", exception.toString())
            return null
        }
    }

    open fun isReminderEnabled(timeSet: String?) =
        timeSet?.matches(getString(R.string.time_not_set).toRegex()) == false

    open fun calculateOffsetForReminder(): Int {
        return try {
            Integer.parseInt(mBinding.prayerOffsetValue.text.toString())
        } catch (error: NumberFormatException) {
            0
        }
    }
}