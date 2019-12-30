package com.thesunnahrevival.sunnahassistant.views;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import com.thesunnahrevival.sunnahassistant.R;

public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
    }

    public void openSettings(View v) {
        QuickSetupFragment quickSetupFragment = new QuickSetupFragment();
        quickSetupFragment.show(getSupportFragmentManager(), "setup");
    }

}
