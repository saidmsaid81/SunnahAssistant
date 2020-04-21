package com.thesunnahrevival.sunnahassistant.utilities;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;

import com.thesunnahrevival.sunnahassistant.BuildConfig;
import com.thesunnahrevival.sunnahassistant.data.model.AppSettings;
import com.thesunnahrevival.sunnahassistant.data.model.Reminder;

import java.util.ArrayList;

public class SunnahAssistantUtil {

    public static final String SUNNAH = "Sunnah";
    public static final String PRAYER = "Prayer";
    public static final String OTHER = "Other";
    public static final String UNCATEGORIZED = "Uncategorized";
    public static final String DAILY = "Daily";
    public static final String WEEKLY = "Weekly";
    public static final String MONTHLY = "Monthly";

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


    public static ArrayList<Reminder> sunnahReminders() {
        ArrayList<Reminder> listOfReminders = new ArrayList<>();

        listOfReminders.add(
                createReminder(-101, "Praying Dhuha", "<a href=\"https://thesunnahrevival.wordpress.com/2015/11/18/sunnah-of-the-weekduha-prayer-its-importance-and-practical-tips\">Read more</a> on Dhuha Prayer and the best time to pray", SUNNAH, DAILY, 0, null)
        );
        listOfReminders.add(
                (createReminder(-102, "Morning Adhkar", "", SUNNAH, DAILY, 0, null))
        );
        listOfReminders.add(
                (createReminder(-103, "Evening Adhkar", "", SUNNAH, DAILY, 0, null))
        );
        listOfReminders.add(
                (createReminder(-104,"Tahajjud", "<a href=\"https://thesunnahrevival.wordpress.com/2014/04/09/tahajjud/\">Read more</a> on Tahjjud Prayer and the best time to pray", SUNNAH, DAILY, 0, null))
        );

        listOfReminders.add(
                (createReminder(-105, "Reading the Quran", "", SUNNAH, DAILY, 0, null))
        );

        ArrayList<String> listOfDays = new ArrayList<>();
        listOfDays.add("Fri");
        listOfReminders.add(
                createReminder(-106, "Reading Suratul Kahf", "<a href=\"https://thesunnahrevival.wordpress.com/2020/03/06/2769/\">Read more</a> on the importance of reading Suratul Kahf every Friday", SUNNAH, WEEKLY, -1, listOfDays)
        );
        listOfDays = new ArrayList<>();
        listOfDays.add("Sun");
        listOfDays.add("Wedy");
        listOfReminders.add(
                createReminder(-107, "Fasting On Monday And Thursday", "<a href=\"https://thesunnahrevival.wordpress.com/2016/01/06/revive-a-sunnah-fasting-on-monday-and-thursday/\">Read more</a> on the importance of reading fasting on Mondays and Thursday", SUNNAH, WEEKLY, -1, listOfDays)
        );

        return listOfReminders;
    }

    private static Reminder createReminder(int id,String name, String info, String category, String frequency, int offset, ArrayList<String> customScheduleList) {
        Reminder reminder = new Reminder(name, info, null,
                category, frequency, offset, 0, false, customScheduleList);
        reminder.setId(id);
        return reminder;
    }

    public static ArrayList<Reminder> demoReminder(){
        ArrayList<Reminder> listOfReminders = new ArrayList<>();
        listOfReminders.add(
                createReminder(-100, "Demo Reminder", "Demo", OTHER, DAILY, 0, null)
        );
        return listOfReminders;
    }

    public static ArrayList initialSettings() {
        AppSettings initialSettings = new AppSettings(
                "Location cannot be empty", (float) 0, (float) 0, 3, 0, false);
        ArrayList list = new ArrayList();
        list.add(initialSettings);
        return list;
    }

}
