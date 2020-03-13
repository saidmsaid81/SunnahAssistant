package com.thesunnahrevival.sunnahassistant.views;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.thesunnahrevival.sunnahassistant.R;
import com.thesunnahrevival.sunnahassistant.data.Reminder;
import com.thesunnahrevival.sunnahassistant.data.SelectDays;
import com.thesunnahrevival.sunnahassistant.databinding.ReminderDetailsBottomSheetBinding;
import com.thesunnahrevival.sunnahassistant.utilities.SunnahAssistantUtil;
import com.thesunnahrevival.sunnahassistant.utilities.TimeDateUtil;
import com.thesunnahrevival.sunnahassistant.viewmodels.RemindersViewModel;
import com.thesunnahrevival.sunnahassistant.views.adapters.SelectDaysSpinnerAdapter;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProviders;

public class ReminderDetailsFragment extends BottomSheetDialogFragment implements View.OnClickListener, AdapterView.OnItemSelectedListener {

    private ReminderDetailsBottomSheetBinding mBinding;
    private Reminder mReminder;
    private RemindersViewModel mViewModel;
    private SelectDaysSpinnerAdapter mSelectedDaysAdapter;
    private int mDay = 0;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mReminder = getArguments() != null ? getArguments().getParcelable("Reminder") : null;
        boolean isNew = getArguments().getBoolean("isNew");

        mBinding = DataBindingUtil.inflate(inflater,
                R.layout.reminder_details_bottom_sheet, container, false);
        if (getActivity() != null) {
            mViewModel = ViewModelProviders.of(getActivity()).get(RemindersViewModel.class);
        }

        mBinding.setReminder(mReminder);
        mBinding.setIsNew(isNew);
        mBinding.setLifecycleOwner(this);

        observeReminderTimeChange();
        setCategorySpinnerData();
        setFrequencySpinnerData();

        if (mReminder != null) {
            mBinding.reminderTime.setText(TimeDateUtil.formatTimeInMilliseconds(getContext(),
                    mReminder.getTimeInMilliSeconds()));
            mDay = mReminder.getDay();
        }
        mBinding.timePicker.setOnClickListener(this);
        mBinding.saveButton.setOnClickListener(this);
        mBinding.moreDetailsTextView.setOnClickListener(this);
        return mBinding.getRoot();
    }

    private void observeReminderTimeChange() {
        TimePickerFragment.timeSet.setValue(TimeDateUtil.formatTimeInMilliseconds(getContext(), mReminder.getTimeInMilliSeconds()));
        TimePickerFragment.timeSet.observe(this, s -> mBinding.reminderTime.setText(s));
    }

    private void setFrequencySpinnerData() {
        ArrayAdapter<CharSequence> frequencyAdapter = ArrayAdapter.createFromResource(
                mBinding.bottomSheet.getContext(), R.array.frequency, android.R.layout.simple_spinner_item);
        frequencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mBinding.frequencySpinner.setAdapter(frequencyAdapter);
        mBinding.frequencySpinner.setOnItemSelectedListener(this);
        mBinding.frequencySpinner.setSelection(frequencyAdapter.getPosition(mReminder.getFrequency()));
    }

    private void setCategorySpinnerData() {
        ArrayAdapter<CharSequence> categoryAdapter = ArrayAdapter.createFromResource(
                mBinding.bottomSheet.getContext(), R.array.categories, android.R.layout.simple_spinner_item);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mBinding.categorySpinner.setAdapter(categoryAdapter);
        String category = mReminder.getCategory();
        mBinding.categorySpinner.setSelection(categoryAdapter.getPosition(category));
        if (category.matches(SunnahAssistantUtil.PRAYER)) {
            mBinding.categorySpinner.setEnabled(false);
            mBinding.frequencySpinner.setEnabled(false);
        }
    }

    @Override
    public void onClick(View v) {
        if (getActivity() != null) {

            if (v.getId() == R.id.time_picker) {
                DialogFragment timePickerFragment = new TimePickerFragment();
                FragmentManager fm = getActivity().getSupportFragmentManager();
                timePickerFragment.show(fm, "timePicker");
            } else if (v.getId() == R.id.date_picker) {
                DialogFragment datePickerFragment = new DatePickerFragment();
                FragmentManager fm = getActivity().getSupportFragmentManager();
                datePickerFragment.show(fm, "datePicker");
                DatePickerFragment.mDay = mDay;
            }

        }
        if (v.getId() == R.id.save_button)
            save();

        else if (v.getId() == R.id.more_details_text_view) {
            //Toggle More details view
            if (mBinding.additionalDetails.getVisibility() == View.GONE)
                mBinding.additionalDetails.setVisibility(View.VISIBLE);

            else
                mBinding.additionalDetails.setVisibility(View.GONE);
        }
    }

    private void save() {
        if (TextUtils.isEmpty(mBinding.reminderEditText.getText().toString().trim())) {
            Toast.makeText(getContext(), getString(R.string.name_cannot_be_empty), Toast.LENGTH_SHORT).show();
            return;
        }

        ArrayList<String> checkedDays = new ArrayList<>();
        mDay = 0;
        if (((String) mBinding.frequencySpinner.getSelectedItem()).matches(SunnahAssistantUtil.WEEKLY)) {
            if (mSelectedDaysAdapter.getCheckedDays().isEmpty()) {
                Toast.makeText(getContext(), getString(R.string.select_atleast_one_day), Toast.LENGTH_LONG).show();
                mBinding.selectDayError.setVisibility(View.VISIBLE);
                return;
            }
            checkedDays = mSelectedDaysAdapter.getCheckedDays();
            mDay = -1; //To distinguish from daily and monthly reminders
        } else if (((String) mBinding.frequencySpinner.getSelectedItem()).matches(SunnahAssistantUtil.MONTHLY)) {
            mDay = DatePickerFragment.mDay;
        }
        Reminder newReminder = new Reminder(mBinding.reminderEditText.getText().toString().trim(),
                mBinding.additionalDetails.getText().toString().trim(),
                TimePickerFragment.timeSet.getValue() != null ?
                        TimeDateUtil.getTimestampInSeconds(TimePickerFragment.timeSet.getValue()) :
                        null,
                (String) mBinding.categorySpinner.getSelectedItem(),
                (String) mBinding.frequencySpinner.getSelectedItem(),
                mDay,
                0,
                false,
                checkedDays
        );
        newReminder.setId(mReminder.getId());
        newReminder.setOffset(Integer.parseInt(mBinding.prayerOffsetValue.getText().toString().trim()));
        if (mViewModel != null) {
            if (!mReminder.equals(newReminder)) {
                if (!newReminder.getCategory().matches(SunnahAssistantUtil.PRAYER))
                    mViewModel.insert(newReminder);
                else
                    mViewModel.updatePrayerTimeDetails(mReminder, newReminder);
                Toast.makeText(getContext(), getString(R.string.successfully_updated), Toast.LENGTH_LONG).show();
            } else
                Toast.makeText(getContext(), getString(R.string.no_changes), Toast.LENGTH_SHORT).show();
            dismiss();
        } else {
            Toast.makeText(getContext(), getString(R.string.error), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (position == 1) {
            showSelectDaysSpinner();
        } else if (position == 2) {
            mBinding.selectDaysSpinner.setVisibility(View.GONE);
            mBinding.datePicker.setVisibility(View.VISIBLE);
            mBinding.datePicker.setOnClickListener(this);
            return;
        } else {
            mBinding.selectDaysSpinner.setVisibility(View.GONE);
        }
        mBinding.datePicker.setVisibility(View.GONE);
    }


    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    private void showSelectDaysSpinner() {
        final String[] nameOfDays = getResources().getStringArray(R.array.select_days);

        ArrayList<SelectDays> listOfAllDays = new ArrayList<>();

        for (String nameOfDay : nameOfDays) {
            SelectDays day = new SelectDays();
            day.setTitle(nameOfDay);
            boolean isDayScheduled = mReminder.getCustomScheduleDays().contains(nameOfDay.substring(0, 3));
            day.setSelected(isDayScheduled);
            listOfAllDays.add(day);
        }
        if (mReminder.getFrequency().matches(SunnahAssistantUtil.WEEKLY))
            mSelectedDaysAdapter = new SelectDaysSpinnerAdapter(
                    getContext(), 0, listOfAllDays, mReminder.getCustomScheduleDays()
            );
        else
            mSelectedDaysAdapter = new SelectDaysSpinnerAdapter(
                    getContext(), 0, listOfAllDays, new ArrayList<>()
            );
        mBinding.selectDaysSpinner.setAdapter(mSelectedDaysAdapter);
        mBinding.selectDaysSpinner.setVisibility(View.VISIBLE);
    }
}
