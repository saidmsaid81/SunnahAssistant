package com.thesunnahrevival.sunnahassistant.views;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;

import com.thesunnahrevival.sunnahassistant.R;
import com.thesunnahrevival.sunnahassistant.utilities.NextReminderService;
import com.thesunnahrevival.sunnahassistant.viewmodels.SettingsViewModel;
import com.thesunnahrevival.sunnahassistant.databinding.ActivitySettingsBinding;

public class SettingsActivity extends AppCompatActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    private ActivitySettingsBinding mBinding;
    private SettingsViewModel mViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_settings);
        mBinding.setLifecycleOwner(this);
        mViewModel = ViewModelProviders.of(this).get(SettingsViewModel.class);
        mBinding.setSettingsViewModel(mViewModel);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSettings();
        mBinding.nexttReminderNotificationToggle.setOnCheckedChangeListener(this);
    }

    private void getSettings() {
        mViewModel.mSettings.observe(this, settings -> {
            if (settings != null) {
                mViewModel.setOffsetSavedValue(settings.getHijriOffSet());
                if (!settings.isShowNextReminderNotification())
                    mBinding.nexttReminderNotificationToggle.setChecked(false);
                if (!settings.isFirstLaunch())
                    mBinding.nexttReminderNotificationToggle.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.change_hijri_date) {
            int offset = Integer.parseInt(mBinding.hijriOffsetValue.getText().toString().trim());
            mViewModel.updateHijriDate(offset);
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (mViewModel.mSettings.getValue() != null) {
            if (isChecked) {
                mViewModel.mSettings.getValue().setShowNextReminderNotification(true);
                startService(new Intent(this, NextReminderService.class));
            } else {
                mViewModel.mSettings.getValue().setShowNextReminderNotification(false);
                stopService(new Intent(this, NextReminderService.class));
            }
            mViewModel.updateSettings();
        }
    }
}
