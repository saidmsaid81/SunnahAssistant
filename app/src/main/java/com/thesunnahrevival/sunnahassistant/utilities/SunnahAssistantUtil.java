package com.thesunnahrevival.sunnahassistant.utilities;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import com.thesunnahrevival.sunnahassistant.BuildConfig;
import com.thesunnahrevival.sunnahassistant.data.AppSettings;
import com.thesunnahrevival.sunnahassistant.data.Reminder;

import java.util.ArrayList;


public class SunnahAssistantUtil {

    public static Intent generateEmailIntent() {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:"));
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"thesunnahrevival.tsr@gmail.com"});
        intent.putExtra(Intent.EXTRA_SUBJECT, "Sunnah Assistant App" + " - Version " + BuildConfig.VERSION_NAME);
        intent.putExtra(Intent.EXTRA_TEXT, getEmailText());
        return intent;
    }

    private static String getEmailText() {
        return "Please write your feedback below in English" +
                "\n\n\n\n\n\n" +
                "Additional Info\n" +
                "App Name: Sunnah Assistant\n" +
                "App Version: " +
                BuildConfig.VERSION_NAME +
                "\nBrand: " +
                Build.BRAND +
                "\nModel: " +
                Build.MODEL +
                "\nAndroid Version: " +
                Build.VERSION.RELEASE +
                "\nDevice Language: " +
                Resources.getSystem().getConfiguration().locale.getLanguage();
    }

    public static Intent showShareMenu(Intent intent) {
        String title = intent.getStringExtra("title");
        String text = intent.getStringExtra("text");
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT,
                title + "\n" + text + "\n\n" + "Sent from Sunnah Assistant App - Set reminders that help you become a better person\nDownload Link: https://play.google.com/store/apps/details?id=com.thesunnahrevival.sunnahassistant");
        sendIntent.setType("text/plain");

        return Intent.createChooser(sendIntent, null);
    }

    public static void openPlayStore(Context context, String appPackageName) {
        try {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
        } catch (android.content.ActivityNotFoundException e) {
            context.startActivity(
                    new Intent(Intent.ACTION_VIEW,
                            Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
        }
    }

    public static void openDeveloperPage(Context context) {
        try {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://dev?id=6919675665650793025")));
        } catch (android.content.ActivityNotFoundException e) {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/dev?id=6919675665650793025")));
        }
    }

    public static ArrayList<Reminder> initialReminders() {
        ArrayList<Reminder> listOfReminders = new ArrayList<>();

        listOfReminders.add(
                (new Reminder("Praying Dhuha", "", "Not Set",
                        "Sunnah", "Daily", 0, false, new ArrayList<>()))
        );
        listOfReminders.add(
                (new Reminder("Morning Adhkar", "", "Not Set", "Sunnah", "Daily", 0, false, new ArrayList<>()))
        );
        listOfReminders.add(
                (new Reminder("Evening Adhkar", "", "Not Set", "Sunnah", "Daily", 0, false, new ArrayList<>()))
        );
        listOfReminders.add(
                (new Reminder("Tahajjud", "", "Not Set", "Sunnah", "Daily", 0, false, new ArrayList<>()))
        );

        ArrayList<String> listOfDays = new ArrayList<>();
        listOfDays.add("Thu");
        listOfReminders.add(
                new Reminder("Reading Suratul Kahf", "", "21:00", "Sunnah", "Weekly", -1, false, listOfDays)
        );
        listOfDays = new ArrayList<>();
        listOfDays.add("Sun");
        listOfDays.add("Wed");
        listOfReminders.add(
                new Reminder("Fasting On Monday And Thursday", "", "21:00", "Sunnah", "Weekly", -1, false, listOfDays)
        );

        return listOfReminders;
    }

    public static ArrayList initialSettings() {
        AppSettings initialSettings = new AppSettings(
                "", (float) 0, (float) 0, 3, 0, true);
        ArrayList list = new ArrayList();
        list.add(initialSettings);
        return list;
    }

}
