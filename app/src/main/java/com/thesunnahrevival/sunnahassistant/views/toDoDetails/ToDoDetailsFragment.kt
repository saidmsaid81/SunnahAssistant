package com.thesunnahrevival.sunnahassistant.views.toDoDetails

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.*
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_SHORT
import com.google.android.material.snackbar.Snackbar
import com.thesunnahrevival.sunnahassistant.R
import com.thesunnahrevival.sunnahassistant.data.model.Frequency
import com.thesunnahrevival.sunnahassistant.data.model.ToDo
import com.thesunnahrevival.sunnahassistant.utilities.*
import com.thesunnahrevival.sunnahassistant.views.FragmentWithPopups
import com.thesunnahrevival.sunnahassistant.views.MainActivity
import com.thesunnahrevival.sunnahassistant.views.dialogs.AddCategoryDialogFragment
import com.thesunnahrevival.sunnahassistant.views.dialogs.DatePickerFragment
import com.thesunnahrevival.sunnahassistant.views.dialogs.SelectDaysDialogFragment
import com.thesunnahrevival.sunnahassistant.views.dialogs.TimePickerFragment
import java.net.MalformedURLException
import java.text.DateFormatSymbols
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.*

open class ToDoDetailsFragment : FragmentWithPopups(), View.OnClickListener,
    SelectDaysDialogFragment.SelectDaysDialogListener, DatePickerFragment.OnDateSelectedListener,
    TimePickerFragment.OnTimeSetListener {

    private lateinit var mBinding: com.thesunnahrevival.sunnahassistant.databinding.ToDoDetailsFragmentBinding
    private lateinit var mToDo: ToDo
    private var mToDoCategories: ArrayList<String> = arrayListOf()
    private var mCustomScheduleDays: TreeSet<Int> = TreeSet()
    private var mTimeString: String? = null
    private var mDay: Int = LocalDate.now().dayOfMonth
    private var mMonth: Int = LocalDate.now().month.ordinal
    private var mYear: Int = LocalDate.now().year
    private var inAppBrowser: InAppBrowser? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)

        mBinding = DataBindingUtil.inflate(
            inflater, R.layout.to_do_details_fragment, container, false
        )

        mToDo = mViewModel.selectedToDo
        (requireActivity() as MainActivity).supportActionBar?.setTitle(
            if (mToDo.id == 0)
                R.string.add_new_to_do
            else
                R.string.edit_to_do
        )
        mCustomScheduleDays.clear()
        mToDo.customScheduleDays?.let {
            mCustomScheduleDays.addAll(it)
        }

        setHasOptionsMenu(true)

        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mBinding.toDosName = mToDo.name
        mBinding.toDosAdditionalInfo = mToDo.additionalInfo
        mBinding.offsetInMinutes = mToDo.offsetInMinutes
        mBinding.lifecycleOwner = viewLifecycleOwner

        updateView()

        if (mToDo.predefinedToDoInfo.isNotBlank()) {
            inAppBrowser = InAppBrowser(requireContext(), lifecycleScope)
        }

        if (mToDo.isAutomaticPrayerTime()) {
            automaticPrayerTimeView()
        } else
            mBinding.isAutomaticPrayerTime = false
    }

    private fun automaticPrayerTimeView() {
        mBinding.isAutomaticPrayerTime = true

        val timeInMillis = GregorianCalendar.getInstance()
            .apply { set(mToDo.year, mToDo.month, mToDo.day) }
            .timeInMillis

        val formattedDate = SimpleDateFormat("EEEE d MMMM, yyyy", getLocale())
            .format(Date(timeInMillis))

        mBinding.tip.text = getString(
            R.string.automatic_prayer_time_info,
            mToDo.name,
            formattedDate
        )
        mBinding.tip.setOnClickListener {
            findNavController().navigate(R.id.prayerTimeSettingsFragment)
        }
        mViewModel.getToDo(mViewModel.selectedToDo.id).observe(viewLifecycleOwner) {
            if (it != null) {
                val timeString = formatTimeInMilliseconds(requireContext(), it.timeInMilliseconds)
                updateToDoTimeView(timeString)
                updateNotifyView(it)
                mBinding.offsetInMinutes = it.offsetInMinutes
            } else {
                findNavController().navigate(R.id.todayFragment)
                Toast.makeText(
                    requireContext(),
                    R.string.automatic_prayer_alerts_disabled,
                    Toast.LENGTH_LONG
                )
                    .show()
            }
        }
    }


    private fun updateView() {
        updateToDoFrequencyView(mToDo.frequency?.ordinal ?: 0)
        updateToDoCategoryView(mToDo.category)
        updateToDoTimeView(formatTimeInMilliseconds(context, mToDo.timeInMilliseconds))
        updateNotifyView()
        updateMarkAsCompleteView(if (mToDo.isComplete(mViewModel.selectedToDoDate)) 1 else 0)
        updateTipView()
    }

    private fun updateToDoFrequencyView(frequencyOrdinal: Int) {
        mBinding.toDoFrequencyValue.text =
            resources.getStringArray(R.array.frequency)[frequencyOrdinal]
        mBinding.selectedFrequency = frequencyOrdinal
        mBinding.toDoFrequency.setOnClickListener(this)

        updateToDoDateView(mToDo.day, mToDo.month, mToDo.year)
        updateSelectedDaysView()
    }


    private fun updateToDoFrequencyView(frequencyString: String) {
        val ordinal = resources.getStringArray(R.array.frequency).indexOf(frequencyString)
        updateToDoFrequencyView(ordinal)
    }

    private fun updateToDoCategoryView(selectedCategory: String?) {
        mBinding.toDoCategoryValue.text = selectedCategory
        AddCategoryDialogFragment.category.value = ""
        AddCategoryDialogFragment.category.observe(viewLifecycleOwner) {
            if (it.isNotBlank())
                mBinding.toDoCategoryValue.text = it
        }
        mBinding.toDoCategory.setOnClickListener(this)
    }

    private fun updateToDoDateView(day: Int, month: Int, year: Int) {
        mBinding.toDoDate.setOnClickListener(this)
        when (Frequency.values()[mBinding.selectedFrequency]) {
            Frequency.OneTime -> {//No repeat
                mMonth = if (month in 0..11) month else LocalDate.now().month.ordinal
                if (year >= 1970)
                    mYear = year
                else {
                    mMonth = LocalDate.now().month.ordinal
                    mDay = LocalDate.now().dayOfMonth
                    mYear = LocalDate.now().year
                    updateNoRepeatDate()
                    return
                }
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
        mBinding.toDoDateValue.text =
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
        mBinding.toDoDateValue.text =
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

    private fun updateToDoTimeView(timeString: String) {

        if (timeString.matches(getString(R.string.time_not_set).toRegex())) {
            mBinding.notify.isEnabled = false
            mBinding.notifyLabel.isEnabled = false
            mBinding.notifyValue.isEnabled = false
        } else {
            mBinding.notify.isEnabled = true
            mBinding.notifyLabel.isEnabled = true
            mBinding.notifyValue.isEnabled = true
        }

        mBinding.isEnabled =
            if (mTimeString?.matches(getString(R.string.time_not_set).toRegex()) == true && mTimeString != timeString)
                true
            else
                mToDo.isReminderEnabled

        mTimeString = timeString
        mBinding.toDoTimeValue.text = timeString
        mBinding.toDoTime.setOnClickListener(this)
    }

    private fun updateNotifyView(toDo: ToDo = mToDo) {
        mBinding.isEnabled = if (toDo.id == 0)
            false
        else
            toDo.isReminderEnabled
        mBinding.notify.setOnClickListener(this)
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

    private fun updateTipView() {
        if (mToDo.predefinedToDoInfo.isNotBlank()) {
            mBinding.tip.text = mToDo.predefinedToDoInfo
            if (Patterns.WEB_URL.matcher(mToDo.predefinedToDoLink).matches())
                mBinding.tipView.setOnClickListener(this)

        } else if (!mToDo.isAutomaticPrayerTime())
            mBinding.tipView.visibility = View.GONE
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.to_do_frequency -> {
                if (!isAutomaticPrayerTime(R.string.repeat_cannot_be_changed))
                    showPopup(
                        resources.getStringArray(R.array.frequency), R.id.to_do_frequency_value,
                        R.id.to_do_frequency
                    )
            }
            R.id.to_do_category -> {
                if (!isAutomaticPrayerTime(R.string.category_cannot_be_changed))
                    mViewModel.settingsValue?.categories?.let {
                        mToDoCategories.clear()
                        mToDoCategories.addAll(it)
                        val createNewCategory = getString(R.string.create_new_categories)
                        if (!mToDoCategories.last().matches(createNewCategory.toRegex())) {
                            mToDoCategories.add(createNewCategory)
                        }
                        showPopup(
                            mToDoCategories.toTypedArray(),
                            R.id.to_do_category_value, R.id.to_do_category
                        )
                    }
            }
            R.id.to_do_date -> {
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
            R.id.to_do_time -> {
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
            R.id.tip_view ->
                try {
                    inAppBrowser?.launchInAppBrowser(
                        mToDo.predefinedToDoLink,
                        findNavController()
                    )
                } catch (exception: MalformedURLException) {
                    Log.e("MalformedURLException", exception.message.toString())
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.something_wrong),
                        Toast.LENGTH_LONG
                    ).show()
                }
            R.id.notify -> {
                onNotifyClick()
            }
        }
    }

    protected open fun onNotifyClick() {
        val notifyOptions = arrayOf(getString(R.string.yes), getString(R.string.no))
        showPopup(
            notifyOptions,
            R.id.notify_value, R.id.notify
        )
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        return when (item?.groupId) {
            R.id.to_do_frequency -> {
                updateToDoFrequencyView(item.title.toString())
                true
            }
            R.id.to_do_category -> {
                val createNewCategory = getString(R.string.create_new_categories)
                if (item.title.toString().matches(createNewCategory.toRegex())) {
                    val dialogFragment = AddCategoryDialogFragment()
                    dialogFragment.show(
                        requireActivity().supportFragmentManager,
                        "addCategoryDialog"
                    )
                } else
                    updateToDoCategoryView(item.title.toString())

                return true
            }
            R.id.mark_as_complete -> {
                updateMarkAsCompleteView(item.title.toString())
                true
            }
            R.id.notify -> {
                onNotifyValueUpdate(item)
                true
            }
            else -> false
        }
    }

    protected open fun onNotifyValueUpdate(item: MenuItem) {
        mBinding.isEnabled =
            item.title.toString().matches(getString(R.string.yes).toRegex())
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
        updateToDoDateView(day, month, year)
    }

    override fun onTimeSet(timeString: String) {
        updateToDoTimeView(timeString)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.to_do_menu, menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        if (mToDo.id == 0 || mToDo.isAutomaticPrayerTime()) //New or Automatic prayer time
            menu.findItem(R.id.delete_to_do).title = getString(R.string.cancel)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.delete_to_do -> {
                if (mToDo.isAutomaticPrayerTime() || mToDo.id == 0) {
                    findNavController().navigateUp()
                } else
                    deleteToDo()
                true
            }
            R.id.save_to_do -> {
                saveToDo()
                true
            }
            R.id.share_to_do -> {
                val shareIntent = Intent(Intent.ACTION_SEND)
                shareIntent.type = "text/plain"
                shareIntent.putExtra(
                    Intent.EXTRA_TEXT, shareToDoText()
                )
                val chooserIntent = Intent.createChooser(
                    shareIntent,
                    getString(R.string.share_to_do)
                )
                requireContext().startActivity(chooserIntent)
                true
            }

            else -> false
        }
    }

    private fun shareToDoText(): String {

        val toDoName = mBinding.toDoNameValue.text.toString()
        val frequencies = resources.getStringArray(R.array.frequency)
        val frequencyValue = mBinding.toDoFrequencyValue.text

        val date = if (frequencies.indexOf(frequencyValue) == Frequency.OneTime.ordinal)
            mBinding.toDoDateValue.text
        else if (frequencies.indexOf(frequencyValue) == Frequency.Daily.ordinal)
            frequencyValue
        else if (frequencies.indexOf(frequencyValue) == Frequency.Weekly.ordinal)
            mBinding.selectDaysValue.text
        else if (frequencies.indexOf(frequencyValue) == Frequency.Monthly.ordinal)
            mBinding.toDoDateValue.text
        else
            ""

        val time = mBinding.toDoTimeValue.text
        val category = mBinding.toDoCategoryValue.text
        val completed = mBinding.markAsCompleteValue.text


        return "${getString(R.string.to_do)}: $toDoName\n" +
                "${getString(R.string.to_do_category)}: $category\n" +
                "${getString(R.string.date)}: $date\n" +
                "${getString(R.string.time_label)}: $time\n" +
                "${getString(R.string.completed)}: $completed\n\n" +
                "${getString(R.string.powered_by_sunnah_assistant)}\n" +
                "Get Sunnah Assistant App at\n" +
                "https://play.google.com/store/apps/details?id=com.thesunnahrevival.sunnahassistant "

    }

    private fun deleteToDo() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.delete_to_do_title)
            .setMessage(R.string.delete_to_do_confirmation)
            .setPositiveButton(R.string.yes) { _, _ ->
                mViewModel.deleteToDo(mToDo)
                Toast.makeText(requireContext(), R.string.delete_to_do, Toast.LENGTH_LONG).show()
                findNavController().navigateUp()
            }
            .setNegativeButton(R.string.no) { _, _ -> }
            .show()

    }

    private fun saveToDo() {
        val newToDo = createNewToDo() ?: return
        if (mToDo.isAutomaticPrayerTime()) { //Automatic prayer time
            if (newToDo.additionalInfo != mToDo.additionalInfo ||
                mToDo.isReminderEnabled != newToDo.isReminderEnabled ||
                mToDo.offsetInMinutes != newToDo.offsetInMinutes ||
                mToDo.completedDates != newToDo.completedDates
            ) {
                mViewModel.updatePrayerTimeDetails(mToDo, newToDo)
                Toast.makeText(
                    requireContext(), R.string.successfully_updated, Toast.LENGTH_LONG
                )
                    .show()
            }
        } else if (mToDo != newToDo || mViewModel.isToDoTemplate) {
            mViewModel.insertToDo(newToDo)
            if (newToDo.id == 0 || mViewModel.isToDoTemplate) {
                mViewModel.isToDoTemplate = false
                Toast.makeText(
                    requireContext(), R.string.successfuly_added_sunnah_to_dos, Toast.LENGTH_LONG
                ).show()
            } else
                Toast.makeText(
                    requireContext(), R.string.successfully_updated, Toast.LENGTH_LONG
                ).show()
        }
        findNavController().navigateUp()
    }

    private fun createNewToDo(): ToDo? {
        val frequency = Frequency.values()[mBinding.selectedFrequency]
        if (frequency == Frequency.Weekly && mCustomScheduleDays.isEmpty()) {
            showSnackbar(R.string.select_atleast_one_day)
            return null
        }

        val toDoName = mBinding.toDoNameValue.text.toString()
        if (toDoName.isBlank()) {
            showSnackbar(R.string.name_cannot_be_empty)
            return null
        }

        val completedDates = TreeSet<String>()
        completedDates.addAll(mToDo.completedDates)
        if (resources.getStringArray(R.array.mark_as_complete_options)
                .indexOf(mBinding.markAsCompleteValue.text) == 1
        ) {
            //Marked as Yes
            completedDates.add(mViewModel.selectedToDoDate.toString())
        } else
            completedDates.remove(mViewModel.selectedToDoDate.toString())

        try {
            return ToDo(
                toDoName,
                mBinding.additionalDetails.text.toString(),
                getTimestampInSeconds(requireContext(), mTimeString),
                mBinding.toDoCategoryValue.text.toString(),
                frequency,
                isToDoEnabled(),
                mDay,
                mMonth,
                mYear,
                calculateOffsetForToDo(),
                mToDo.id,
                mCustomScheduleDays,
                completedDates,
                mToDo.predefinedToDoInfo,
                mToDo.predefinedToDoLink,
                mToDo.repeatsFromDate
            )
        } catch (exception: IllegalArgumentException) {
            Log.e("Exception", exception.toString())
            Toast.makeText(
                requireContext(),
                "An error occurred. Please try again. Error",
                Toast.LENGTH_LONG
            )
                .show()
            return null
        }
    }

    private fun showSnackbar(stringId: Int) {
        Snackbar.make(
            requireContext(),
            mBinding.root,
            getString(stringId),
            LENGTH_SHORT
        ).apply {
            view.setBackgroundColor(ContextCompat.getColor(requireActivity(), R.color.fabColor))
            show()
        }
    }

    protected open fun isToDoEnabled(): Boolean {
        if (mTimeString?.matches(getString(R.string.time_not_set).toRegex()) == true)
            return false

        return mBinding.notifyValue.text.matches(getString(R.string.yes).toRegex())
    }

    protected open fun calculateOffsetForToDo(): Int {
        return try {
            Integer.parseInt(mBinding.prayerOffsetValue.text.toString())
        } catch (error: NumberFormatException) {
            0
        }
    }
}