package com.thesunnahrevival.sunnahassistant.views;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;
import com.google.android.material.snackbar.Snackbar;
import com.thesunnahrevival.sunnahassistant.R;
import com.thesunnahrevival.sunnahassistant.data.model.AppSettings;
import com.thesunnahrevival.sunnahassistant.data.model.Reminder;
import com.thesunnahrevival.sunnahassistant.databinding.ContentMainBinding;
import com.thesunnahrevival.sunnahassistant.utilities.SunnahAssistantUtil;
import com.thesunnahrevival.sunnahassistant.utilities.TimeDateUtil;
import com.thesunnahrevival.sunnahassistant.viewmodels.RemindersViewModel;
import com.thesunnahrevival.sunnahassistant.views.adapters.ReminderListAdapter;
import com.thesunnahrevival.sunnahassistant.views.interfaces.OnDeleteReminderListener;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO;
import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES;

public class MainFragment extends Fragment implements Spinner.OnItemSelectedListener, OnDeleteReminderListener {

    private ContentMainBinding mBinding;
    private RemindersViewModel mViewModel;
    private ReminderListAdapter mReminderRecyclerAdapter;
    private AppSettings mAppSettings;
    private Spinner mSpinner;
    private List<Reminder> mAllReminders;
    private boolean mIsFetchError;
    private Reminder mDeletedReminder;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(
                inflater, R.layout.content_main, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        if (getActivity() != null) {
            mViewModel = ViewModelProviders.of(getActivity()).get(RemindersViewModel.class);
            mBinding.setLifecycleOwner(this);
            mBinding.setViewmodel(mViewModel);
            getSettings();

            //Setup the RecyclerView Adapter
            mReminderRecyclerAdapter = new ReminderListAdapter(getContext());
            mReminderRecyclerAdapter.setOnItemClickListener(mViewModel);
            RecyclerView reminderRecyclerView = mBinding.reminderList;
            reminderRecyclerView.setAdapter(mReminderRecyclerAdapter);
            mReminderRecyclerAdapter.setDeleteReminderListener(this);
            ItemTouchHelper itemTouchHelper = new
                    ItemTouchHelper(new SwipeToDeleteCallback(mReminderRecyclerAdapter));
            itemTouchHelper.attachToRecyclerView(reminderRecyclerView);
            getErrorMessages();
            populateTheSpinner();
        }
    }

    private void getSettings() {
        mViewModel.mSettings.observe(this, settings -> {
            if (settings != null) {
                mAppSettings = settings;
                setTheme();

                if (settings.isFirstLaunch()) {
                    startActivity(new Intent(getActivity(), WelcomeActivity.class));
                    return;
                }
                if (!mIsFetchError)
                    mViewModel.fetchAllAladhanData();
                if (settings.isShowOnBoardingTutorial())
                    showOnBoardingTutorial();
            }
        });
    }

    private void showOnBoardingTutorial() {
        if (getActivity() != null) {
            mAppSettings.setShowOnBoardingTutorial(false);
            mViewModel.updateSettings(mAppSettings);
            new TapTargetSequence(getActivity())
                    .targets(
                            TapTarget.forView(getActivity().findViewById(R.id.fab), getString(R.string.add_new_reminder), getString(R.string.add_new_reminder_description))
                                    .outerCircleColor(android.R.color.holo_blue_dark)
                                    .cancelable(false)
                                    .transparentTarget(true),
                            TapTarget.forView(mSpinner, getString(R.string.spinner_tutorial), getString(R.string.spinner_tutorial_description))
                                    .outerCircleColor(android.R.color.holo_blue_dark)
                                    .cancelable(false)
                                    .transparentTarget(true),
                            TapTarget.forToolbarOverflow((Toolbar) getActivity().findViewById(R.id.toolbar),
                                    getString(R.string.change_theme),
                                    getString(R.string.change_theme_description))
                                    .outerCircleColor(android.R.color.holo_blue_dark)
                                    .transparentTarget(true)
                                    .cancelable(false)
                                    .tintTarget(true))
                    .listener(new TapTargetSequence.Listener() {
                        @Override
                        public void onSequenceFinish() {
                            mReminderRecyclerAdapter.mShowOnBoardingTutorial = true;
                            mReminderRecyclerAdapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onSequenceStep(TapTarget lastTarget, boolean targetClicked) {

                        }

                        @Override
                        public void onSequenceCanceled(TapTarget lastTarget) {

                        }
                    })
                    .start();
        }

    }

    private void getErrorMessages() {
        mViewModel.getErrorMessages().observe(this, s -> {
            Snackbar snackbar = Snackbar.make(mBinding.mainLayout, "", Snackbar.LENGTH_INDEFINITE);
            if (!(s == null || s.isEmpty() || s.matches("Successful") || s.contains("Refreshing"))) {
                mIsFetchError = true;
                if (mAppSettings != null) {
                    //Reset the month to 0 enabling refetching of the data when user selects refresh on the snackbar displayed
                    mAppSettings.setMonth(0);
                    mViewModel.updateSettings(mAppSettings);
                }

                snackbar.setText(s);
                snackbar.setAction("Refresh", v -> {
                    mViewModel.fetchAllAladhanData();
                    mIsFetchError = false;
                });
                snackbar.show();
            } else if (s != null && s.matches("Successful") && mAppSettings != null)
                snackbar.dismiss();
            else if (s != null && s.contains("Refreshing"))
                Toast.makeText(getContext(), s, Toast.LENGTH_LONG).show();
        });
    }

    private void setTheme() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P)//Android 10 and above
            return; //Follow Device Settings
        if (mAppSettings.isLightMode())
            AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_NO);
        else
            AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_YES);//Dark Mode
    }

    /**
     * Changes The Displayed data
     *
     * @param tag spinner Item
     */
    private void changeData(List<Reminder> data, String tag) {
        //Checks to see if the data that changed affects what is being displayed by spinner selection
        //Returns early if it does not affect displayed data
        if (!((String) mSpinner.getSelectedItem()).matches(tag))
            return;

        if (getActivity() != null && data != null && !data.isEmpty()) {
            mAllReminders = data;
            if (mSpinner.getSelectedItemPosition() == 0) {
                mBinding.setNextReminder(null);
                for (int i = 0; i < data.size(); i++) {
                    //Find a reminder that is enabled and display it in the next reminder section
                    if (data.get(i).isEnabled()) {
                        mBinding.setNextReminder(data.get(i));
                        mBinding.nextCardView.reminderTime.setText(TimeDateUtil.formatTimeInMilliseconds(getContext(),
                                data.get(i).getTimeInMilliSeconds()));
                        data.remove(i);//Remove The Next Reminder
                        break;
                    }
                }
            }
            getActivity().findViewById(R.id.all_done_view).setVisibility(View.GONE);
        } else if (getActivity() != null) {
            mBinding.setNextReminder(null);
            getActivity().findViewById(R.id.all_done_view).setVisibility(View.VISIBLE);
        }
        mReminderRecyclerAdapter.setData(data); //Refresh the RecyclerView
    }

    private void populateTheSpinner() {
        if (getContext() != null) {
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                    R.array.reminder_filter, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            mSpinner = mBinding.spinner;
            mSpinner.setOnItemSelectedListener(this);
            mSpinner.setAdapter(adapter);
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        mViewModel.getReminders(position).observe(this,
                reminders -> changeData(reminders, (String) mSpinner.getItemAtPosition(position)));

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void deleteReminder(int position) {
        mDeletedReminder = mAllReminders.get(position);
        if (mDeletedReminder.getCategory().matches(SunnahAssistantUtil.PRAYER)) {
            Snackbar snackbar = Snackbar.make(mBinding.mainLayout, getString(R.string.cannot_delete_prayer_time),
                    Snackbar.LENGTH_LONG);
            snackbar.show();
            mReminderRecyclerAdapter.notifyDataSetChanged();
            return;
        }
        if (mDeletedReminder.isEnabled())
            mViewModel.cancelScheduledReminder(mDeletedReminder);
        mViewModel.delete(mDeletedReminder);
        Snackbar snackbar = Snackbar.make(mBinding.mainLayout, getString(R.string.delete_reminder),
                Snackbar.LENGTH_LONG);
        snackbar.setAction(getString(R.string.undo_delete), v -> mViewModel.insert(mDeletedReminder));
        snackbar.show();
    }
}
