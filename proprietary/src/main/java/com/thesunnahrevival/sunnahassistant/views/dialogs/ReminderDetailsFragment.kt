package com.thesunnahrevival.sunnahassistant.views.dialogs

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.thesunnahrevival.common.R
import com.thesunnahrevival.common.views.dialogs.ReminderDetailsFragment

class ReminderDetailsFragment : ReminderDetailsFragment() {

    private lateinit var notifySpinnerAdapter: ArrayAdapter<CharSequence>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val isAutomaticPrayerTime = mBinding.isAutomaticPrayerTime
        //Set it to false even when is automatic prayer time
        // because the layout is different in proprietary module
        mBinding.isAutomaticPrayerTime = false

        if (isAutomaticPrayerTime)
            mBinding.timePicker.visibility = View.GONE

        val notifyOptions = ArrayList<CharSequence>()
        notifyOptions.addAll(resources.getStringArray(R.array.notify_options))

        notifySpinnerAdapter = ArrayAdapter(
            mBinding.bottomSheet.context,
            android.R.layout.simple_spinner_item,
            notifyOptions
        )
        notifySpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        mBinding.notifySpinner.adapter = notifySpinnerAdapter
        mBinding.notifySpinner.setSelection(resolveNotifyOptionForReminder())
        mBinding.notifySpinner.onItemSelectedListener = this

        mBinding.notifyLabel.visibility = View.VISIBLE
        mBinding.notifySpinner.visibility = View.VISIBLE

        CustomOffsetFragment.customOffsetInMinutes.value = null
        CustomOffsetFragment.customOffsetInMinutes.observe(this) {
            if (it != null && it != 0)
                mBinding.notifySpinner.setSelection(formatOffset(it))
            else if (it != null && it == 0)
                mBinding.notifySpinner.setSelection(1)
        }
    }

    private fun resolveNotifyOptionForReminder(): Int {
        if (!mBinding.isNew && mBinding.reminder?.isEnabled == false)
            return 0

        val reminder = mBinding.reminder
        if (reminder != null) {
            return when (reminder.offsetInMinutes) {
                -5 -> 2
                -15 -> 3
                -30 -> 4
                0 -> 1
                else -> {
                    val offsetInMinutes = reminder.offsetInMinutes
                    formatOffset(offsetInMinutes)
                }
            }
        }

        return 1
    }

    override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
        super.onItemSelected(parent, view, position, id)
        val custom = resources.getStringArray(R.array.notify_options)[5]
        if ((parent.selectedItem as String).matches(custom.toRegex()) && position > 4) {
            val dialogFragment = CustomOffsetFragment()
            requireActivity().supportFragmentManager.let {
                dialogFragment.show(
                    it,
                    "dialog"
                )
            }
        }
    }

    override fun calculateOffsetForReminder(): Int {
        return when (mBinding.notifySpinner.selectedItemPosition) {
            0 -> 0
            1 -> 0
            2 -> -5
            3 -> -15
            4 -> -30
            else -> {
                try {
                    return when (val selectedItem =
                        mBinding.notifySpinner.selectedItem.toString()) {
                        resources.getStringArray(R.array.notify_options)[5] -> 0
                        else -> {
                            val isBefore =
                                selectedItem.contains(
                                    resources.getStringArray
                                        (com.thesunnahrevival.sunnahassistant.R.array.offset_options)[0]
                                )

                            val selectedItemString = if (isBefore)
                                selectedItem.replace(
                                    resources.getStringArray(
                                        com.thesunnahrevival.sunnahassistant.R.array.offset_options
                                    )[0],
                                    ""
                                )
                            else
                                selectedItem.replace(
                                    resources.getStringArray(
                                        com.thesunnahrevival.sunnahassistant.R.array.offset_options
                                    )[1],
                                    ""
                                )

                            val replaceHours = selectedItemString.replace(
                                getString(com.thesunnahrevival.sunnahassistant.R.string.hours),
                                ":"
                            )
                            val timeString = replaceHours.replace(
                                getString(com.thesunnahrevival.sunnahassistant.R.string.minutes),
                                ""
                            ).filter { !it.isWhitespace() }

                            if (timeString.contains(":".toRegex())) {
                                val timeArray = timeString.split(":")
                                if (timeArray.size == 2) {
                                    if (timeArray[0].isNotBlank() && timeArray[1].isNotBlank()) {
                                        //Contains hours and minutes
                                        val offsetInMinutes =
                                            (timeArray[0].toInt() * 60) + timeArray[1].toInt()
                                        return if (isBefore) -offsetInMinutes else offsetInMinutes
                                    } else if (timeArray[0].isNotBlank() && timeArray[1].isBlank()) {
                                        //Contains hours but not minutes
                                        val offsetInMinutes = timeArray[0].toInt() * 60
                                        return if (isBefore) -offsetInMinutes else offsetInMinutes
                                    }
                                }
                            } else if (timeString.isNotBlank()) { //No hours
                                val offsetInMinutes = timeString.toInt()
                                return if (isBefore) -offsetInMinutes else offsetInMinutes
                            }
                            0
                        }
                    }
                } catch (exception: NumberFormatException) {
                    return 0
                }
            }
        }
    }

    override fun isReminderEnabled(timeSet: String?): Boolean {
        return if (mBinding.notifySpinner.selectedItemPosition != 0)
            super.isReminderEnabled(timeSet)
        else
            false
    }

    private fun formatOffset(offsetInMinutes: Int): Int {
        val unsignedOffsetInMinutes = if (offsetInMinutes < 0) {
            -(offsetInMinutes)
        } else {
            offsetInMinutes
        }

        val offsetOptions =
            resources.getStringArray(com.thesunnahrevival.sunnahassistant.R.array.offset_options)
        val offsetKeyword = if (offsetInMinutes < 0) offsetOptions[0] else offsetOptions[1]

        val hours = (unsignedOffsetInMinutes / 60)
        val minutes = unsignedOffsetInMinutes % 60

        val offsetString = StringBuilder()

        if (hours > 0) {
            offsetString.append("$hours ${getString(com.thesunnahrevival.sunnahassistant.R.string.hours)} ")
        }
        if (minutes > 0) {
            offsetString.append(
                "$minutes ${getString(com.thesunnahrevival.sunnahassistant.R.string.minutes)} "
            )
        }
        offsetString.append(offsetKeyword)

        val custom = resources.getStringArray(R.array.notify_options)[5]
        notifySpinnerAdapter.remove(custom)
        notifySpinnerAdapter.addAll(
            offsetString.toString(),
            custom
        )
        notifySpinnerAdapter.notifyDataSetChanged()
        return notifySpinnerAdapter.getPosition(custom) - 1
    }
}