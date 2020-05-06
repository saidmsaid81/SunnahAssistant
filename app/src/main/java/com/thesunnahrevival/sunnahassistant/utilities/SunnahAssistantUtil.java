package com.thesunnahrevival.sunnahassistant.utilities;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;

import com.thesunnahrevival.sunnahassistant.BuildConfig;
import com.thesunnahrevival.sunnahassistant.R;
import com.thesunnahrevival.sunnahassistant.data.model.AppSettings;
import com.thesunnahrevival.sunnahassistant.data.model.Reminder;
import com.thesunnahrevival.sunnahassistant.widgets.HijriDateWidget;
import com.thesunnahrevival.sunnahassistant.widgets.TodayRemindersWidget;
import com.thesunnahrevival.sunnahassistant.widgets.TodaysRemindersWidgetDark;
import com.thesunnahrevival.sunnahassistant.widgets.TodaysRemindersWidgetTransparent;

import java.util.ArrayList;

public class SunnahAssistantUtil {

    public static final String SUNNAH = "Sunnah";
    public static final String PRAYER = "Prayer";
    public static final String OTHER = "Other";
    public static final String ONE_TIME = "One Time";
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
                createReminder(-1001, "Praying Dhuha", "<a href=\"https://thesunnahrevival.wordpress.com/2015/11/18/sunnah-of-the-weekduha-prayer-its-importance-and-practical-tips\">Read more</a> on Dhuha Prayer and the best time to pray", SUNNAH, DAILY, null, null , null)
        );
        listOfReminders.add(
                (createReminder(-1002, "Morning Adhkar", "", SUNNAH, DAILY, null, null , null))
        );
        listOfReminders.add(
                (createReminder(-1003, "Evening Adhkar", "", SUNNAH, DAILY, null, null , null))
        );
        listOfReminders.add(
                (createReminder(-1004,"Tahajjud", "<a href=\"https://thesunnahrevival.wordpress.com/2014/04/09/tahajjud/\">Read more</a> on Tahjjud Prayer and the best time to pray", SUNNAH, DAILY, null, null , null))
        );

        listOfReminders.add(
                (createReminder(-1005, "Reading the Quran", "", SUNNAH, DAILY, null, null , null))
        );

        ArrayList<String> listOfDays = new ArrayList<>();
        listOfDays.add("Fri");
        listOfReminders.add(
                createReminder(-1006, "Reading Suratul Kahf", "<a href=\"https://thesunnahrevival.wordpress.com/2020/03/06/2769/\">Read more</a> on the importance of reading Suratul Kahf every Friday", SUNNAH, WEEKLY, null, null, listOfDays)
        );
        listOfDays = new ArrayList<>();
        listOfDays.add("Sun");
        listOfDays.add("Wedy");
        listOfReminders.add(
                createReminder(-1007, "Fasting On Monday And Thursday", "<a href=\"https://thesunnahrevival.wordpress.com/2016/01/06/revive-a-sunnah-fasting-on-monday-and-thursday/\">Read more</a> on the importance of reading fasting on Mondays and Thursday", SUNNAH, WEEKLY, null, null, listOfDays)
        );

        return listOfReminders;
    }

    public static Reminder createReminder(int id, String name, String info, String category, String frequency, Integer month, Integer year, ArrayList<String> customScheduleList) {
        Reminder reminder = new Reminder(name, info, null,
                category, frequency, false, null, month, year, 0, customScheduleList);
        reminder.setId(id);
        return reminder;
    }

    public static Reminder demoReminder(){
        return createReminder(-1000, "Demo Reminder", "Demo", OTHER, DAILY, null, null , null);
    }

    public static AppSettings initialSettings() {
        return new AppSettings("Location cannot be empty", (float) 0, (float) 0, 0, 0, false);
    }

    public static void updateHijriDateWidgets(Context context) {
        //Update Widgets
        Intent widgetIntent = new Intent(context, HijriDateWidget.class);
        widgetIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        // Use an array and EXTRA_APPWIDGET_IDS instead of AppWidgetManager.EXTRA_APPWIDGET_ID,
        // since it seems the onUpdate() is only fired on that:
        int[] ids = AppWidgetManager.getInstance(context)
                .getAppWidgetIds(new ComponentName(context, HijriDateWidget.class));

        widgetIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        context.sendBroadcast(widgetIntent);
    }

    public static void updateTodayRemindersWidgets(Context context) {
        //Update Widgets
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        // Use an array and EXTRA_APPWIDGET_IDS instead of AppWidgetManager.EXTRA_APPWIDGET_ID,
        // since it seems the onUpdate() is only fired on that:
        int[] ids = AppWidgetManager.getInstance(context)
                .getAppWidgetIds(new ComponentName(context, TodayRemindersWidget.class));

       appWidgetManager.notifyAppWidgetViewDataChanged(ids, R.id.widgetListView);

        ids = AppWidgetManager.getInstance(context)
                .getAppWidgetIds(new ComponentName(context, TodaysRemindersWidgetDark.class));
        appWidgetManager.notifyAppWidgetViewDataChanged(ids, R.id.widgetListView);

        ids = AppWidgetManager.getInstance(context)
                .getAppWidgetIds(new ComponentName(context, TodaysRemindersWidgetTransparent.class));
        appWidgetManager.notifyAppWidgetViewDataChanged(ids, R.id.widgetListView);
    }

}
