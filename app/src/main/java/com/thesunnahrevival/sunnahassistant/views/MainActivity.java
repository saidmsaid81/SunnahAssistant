package com.thesunnahrevival.sunnahassistant.views;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.oss.licenses.OssLicensesMenuActivity;
import com.thesunnahrevival.sunnahassistant.R;
import com.thesunnahrevival.sunnahassistant.data.model.AppSettings;
import com.thesunnahrevival.sunnahassistant.utilities.SunnahAssistantUtil;
import com.thesunnahrevival.sunnahassistant.viewmodels.SettingsViewModel;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProviders;

import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO;
import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES;

public class MainActivity extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener{

    public SettingsViewModel mViewModel;
    public MutableLiveData<String> mFilteredReminderCategories = new MutableLiveData<>();
    private AppSettings mSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setSupportActionBar(findViewById(R.id.toolbar));
        mViewModel = ViewModelProviders.of(this).get(SettingsViewModel.class);
        mViewModel.getSettings().observe(this, settings -> mSettings = settings);
        mFilteredReminderCategories.setValue("");
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, new MainFragment());
        fragmentTransaction.commit();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
            menu.findItem(R.id.dark_mode_switch).setVisible(false);

        }

        if (mSettings != null && !mSettings.isExpandedLayout())
            menu.findItem(R.id.layout).setTitle("Switch To Expanded View");
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.dark_mode_switch:
                if (AppCompatDelegate.getDefaultNightMode() == MODE_NIGHT_NO) {
                    mViewModel.updateTheme(false);
                    AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_YES);
                } else {
                    mViewModel.updateTheme(true);
                    AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_NO);
                }
                return true;
            case R.id.layout:
                if (mSettings != null){
                    mViewModel.updateLayout(!mSettings.isExpandedLayout());
                    startActivity(new Intent(this, MainActivity.class));
                }
                break;
            case R.id.settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            case R.id.filter:
                filterReminderByCategory();
                return true;
            case R.id.about:
                AboutAppFragment fragment = new AboutAppFragment();
                fragment.show(getSupportFragmentManager(), "about");
                return true;
            case R.id.feedback:
                Intent intent = SunnahAssistantUtil.generateEmailIntent();
                if (intent.resolveActivity(getPackageManager()) != null)
                    startActivity(intent);
                else
                    Toast.makeText(this, getString(R.string.no_email_app_installed), Toast.LENGTH_LONG).show();
                return true;
            case R.id.rate_this_app:
                SunnahAssistantUtil.openPlayStore(this, getPackageName());
                return true;
            case R.id.support_developer:
                SunnahAssistantUtil.openPlayStore(this, "com.thesunnahrevival.supportdeveloper");
                return true;
            case R.id.more_apps:
                SunnahAssistantUtil.openDeveloperPage(this);
                return true;
            case R.id.oss_licenses:
                startActivity(new Intent(this, OssLicensesMenuActivity.class));
               return true;

        }
        return super.onOptionsItemSelected(item);

    }

    private void filterReminderByCategory() {
        PopupMenu popup = new PopupMenu(MainActivity.this, findViewById(R.id.filter));
        popup.setOnMenuItemClickListener(MainActivity.this);
        popup.inflate(R.menu.filter_category);

        MenuItem displayAllMenuItem = popup.getMenu().add(
                R.id.category_display_filter, Menu.NONE, Menu.NONE, getString(R.string.display_all))
                .setCheckable(true);

        if (mFilteredReminderCategories.getValue() != null && mFilteredReminderCategories.getValue().matches(""))
            displayAllMenuItem.setChecked(true);
        if (mSettings != null){
            for (String categoryTitle : mSettings.getCategories()){
                MenuItem categoryItem = popup.getMenu().add(
                        R.id.category_display_filter, Menu.NONE, Menu.NONE, categoryTitle
                );
                categoryItem.setCheckable(true);

                if (mFilteredReminderCategories.getValue().contains(categoryTitle))
                    categoryItem.setChecked(true);
            }
        }
        popup.show();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        if (!item.getTitle().toString().matches(getString(R.string.display_all)))
            mFilteredReminderCategories.setValue(item.getTitle().toString());
        else
            mFilteredReminderCategories.setValue("");
        item.setChecked(!item.isChecked());
        return true;
    }

}
