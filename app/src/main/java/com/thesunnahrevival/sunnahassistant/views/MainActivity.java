package com.thesunnahrevival.sunnahassistant.views;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.lifecycle.ViewModelProviders;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.oss.licenses.OssLicensesMenuActivity;
import com.thesunnahrevival.sunnahassistant.R;
import com.thesunnahrevival.sunnahassistant.utilities.SunnahAssistantUtil;
import com.thesunnahrevival.sunnahassistant.viewmodels.RemindersViewModel;

import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO;
import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private RemindersViewModel mViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setSupportActionBar(findViewById(R.id.toolbar));
        mViewModel = ViewModelProviders.of(this).get(RemindersViewModel.class);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P)
            menu.findItem(R.id.dark_mode_switch).setVisible(false);
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.dark_mode_switch:
                if (AppCompatDelegate.getDefaultNightMode() == MODE_NIGHT_NO) {
                    mViewModel.setIsLightMode(false);
                    AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_YES);
                } else {
                    mViewModel.setIsLightMode(true);
                    AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_NO);
                }
                return true;
            case R.id.settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            case R.id.about:
                AboutAppFragment fragment = new AboutAppFragment();
                fragment.show(getSupportFragmentManager(), "about");
                break;
            case R.id.feedback:
                Intent intent = SunnahAssistantUtil.generateEmailIntent();
                if (intent.resolveActivity(getPackageManager()) != null)
                    startActivity(intent);
                else
                    Toast.makeText(this, getString(R.string.no_email_app_installed), Toast.LENGTH_LONG).show();
                break;
            case R.id.rate_this_app:
                SunnahAssistantUtil.openPlayStore(this, getPackageName());
                break;
            case R.id.support_developer:
                SunnahAssistantUtil.openPlayStore(this, "com.thesunnahrevival.supportdeveloper");
                break;
            case R.id.more_apps:
                SunnahAssistantUtil.openDeveloperPage(this);
                break;
            case R.id.oss_licenses:
                startActivity(new Intent(this, OssLicensesMenuActivity.class));
                break;

        }
        return super.onOptionsItemSelected(item);

    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.fab) {
            mViewModel.openBottomSheet(v, null, false);
        }
    }
}
