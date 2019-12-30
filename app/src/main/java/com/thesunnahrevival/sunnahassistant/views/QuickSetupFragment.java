package com.thesunnahrevival.sunnahassistant.views;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.thesunnahrevival.sunnahassistant.R;
import com.thesunnahrevival.sunnahassistant.data.AppSettings;
import com.thesunnahrevival.sunnahassistant.databinding.QuickSetupLayoutBinding;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.thesunnahrevival.sunnahassistant.viewmodels.SettingsViewModel;

import java.util.ArrayList;
import java.util.Arrays;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;

public class QuickSetupFragment extends BottomSheetDialogFragment implements
        View.OnClickListener, AdapterView.OnItemSelectedListener {

    private QuickSetupLayoutBinding mBinding;
    private SettingsViewModel mViewModel;
    private Spinner mCalculationMethodSpinner;
    private Spinner mAsrMethodSpinner;
    private AppSettings mAppSettings;
    private boolean mIsAutoSettings = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.quick_setup_layout, container, false);
        mBinding.setLifecycleOwner(this);
        mViewModel = ViewModelProviders.of(this).get(SettingsViewModel.class);
        mBinding.setSettingsViewModel(mViewModel);
        mBinding.updatePrayerSettings.setOnClickListener(this);
        setCalculationMethodSpinnerData();
        setAsrJuristicSpinnerData();
        getSettings();
        dismissWhenSettingsUpdated();
        return mBinding.getRoot();
    }


    private void setCalculationMethodSpinnerData() {
        String[] calculationMethods = getResources().getStringArray(R.array.calculation_methods);
        mCalculationMethodSpinner = mBinding.calculationMethodSpinner;
        ArrayAdapter<CharSequence> methodAdapter = new ArrayAdapter<>(
                mCalculationMethodSpinner.getContext(), android.R.layout.simple_spinner_item,
                new ArrayList<>(Arrays.asList(calculationMethods)));
        methodAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mCalculationMethodSpinner.setAdapter(methodAdapter);
        mCalculationMethodSpinner.setOnItemSelectedListener(this);
    }

    private void setAsrJuristicSpinnerData() {
        String[] asrJuristics = getResources().getStringArray(R.array.asr_juristic_method);
        mAsrMethodSpinner = mBinding.asrMethodSpinner;
        ArrayAdapter<CharSequence> juristicMethodAdapter = new ArrayAdapter<>(
                mAsrMethodSpinner.getContext(), android.R.layout.simple_spinner_item,
                new ArrayList<>(Arrays.asList(asrJuristics)));
        juristicMethodAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mAsrMethodSpinner.setAdapter(juristicMethodAdapter);
        mAsrMethodSpinner.setOnItemSelectedListener(this);
    }

    private void getSettings() {
        mViewModel.mSettings.observe(this, settings -> {
            if (settings != null) {
                mAppSettings = settings;
                //Position -1 because the first method provided by the API is ignored
                mCalculationMethodSpinner.setSelection(settings.getMethod() - 1, false);

                //Tag is set to prevent onItemSelectedListener from running if its initiated by a view.
                // OnItemSelected should only run if user initiated
                mCalculationMethodSpinner.setTag(settings.getMethod() - 1);
                mAsrMethodSpinner.setSelection(settings.getAsrCalculationMethod(), false);
                mAsrMethodSpinner.setTag(settings.getAsrCalculationMethod());
                mBinding.manualOption.setOnClickListener(this);
                mBinding.automaticOption.setOnClickListener(this);
                if (settings.isAutomatic()) {
                    mBinding.automaticOption.setChecked(true);
                    mBinding.setAutoPrayerSettings.setVisibility(View.VISIBLE);
                    mIsAutoSettings = true;
                } else
                    mBinding.manualOption.setChecked(true);
            } else {
                //Default Choices
                mCalculationMethodSpinner.setSelection(2, false);
                mCalculationMethodSpinner.setTag(2);
                mAsrMethodSpinner.setSelection(0, false);
                mAsrMethodSpinner.setTag(0);
            }

        });
    }

    private void dismissWhenSettingsUpdated() {
        mViewModel.closeWelcomeScreen.observe(this, aBoolean -> {
            if (aBoolean != null && aBoolean) {
                dismiss();
                Intent intent = new Intent(getActivity(), MainActivity.class);
                startActivity(intent);
            }

        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.update_prayer_settings:
                String address = mBinding.locationEditText.getText().toString().trim();
                if (!mIsAutoSettings)
                    mViewModel.setPrayerSettingsManually();
                else
                    mViewModel.initiateFetchingGeocodingData(address);
                break;
            case R.id.automatic_option:
                mBinding.setAutoPrayerSettings.setVisibility(View.VISIBLE);
                mIsAutoSettings = true;
                break;
            case R.id.manual_option:
                mBinding.setAutoPrayerSettings.setVisibility(View.GONE);
                mIsAutoSettings = false;
                break;

        }
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (mAppSettings != null) {
            switch (parent.getId()) {
                case R.id.calculation_method_spinner:
                    //Checks to ensure that this selection was user initiated and not by the view itself
                    if ((int) mCalculationMethodSpinner.getTag() != position) {
                        mAppSettings.setMethod(position + 1);
                        mViewModel.mCounter = 0;
                        mCalculationMethodSpinner.setTag(position);//Sets the tag to new position
                    }
                    break;
                case R.id.asr_method_spinner:
                    //Checks to ensure that this selection was user initiated and not by the view itself
                    if ((int) mAsrMethodSpinner.getTag() != position) {
                        mAppSettings.setAsrCalculationMethod(position);
                        mViewModel.mCounter = 0;
                        mAsrMethodSpinner.setTag(position);
                    }
                    break;
            }

        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

}
