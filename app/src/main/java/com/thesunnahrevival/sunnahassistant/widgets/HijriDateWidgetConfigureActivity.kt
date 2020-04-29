package com.thesunnahrevival.sunnahassistant.widgets

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.google.android.material.switchmaterial.SwitchMaterial
import com.thesunnahrevival.sunnahassistant.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * The configuration screen for the [HijriDateWidget] AppWidget.
 */
class HijriDateWidgetConfigureActivity : Activity(), View.OnClickListener {
    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID


    public override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)

        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if the user presses the back button.
        setResult(RESULT_CANCELED)

        setContentView(R.layout.hijri_date_widget_configure)
        findViewById<View>(R.id.add_button).setOnClickListener(this)

        // Find the widget id from the intent.
        val intent = intent
        val extras = intent.extras
        if (extras != null) {
            appWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
        }

        // If this activity was started with an intent without an app widget ID, finish with an error.
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }
    }

    override fun onClick(v: View?) {
        val context = this

        // It is the responsibility of the configuration activity to update the app widget
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val hijriChecked = findViewById<SwitchMaterial>(R.id.display_hijri_date_switch).isChecked
        val nextReminderChecked = findViewById<SwitchMaterial>(R.id.display_next_reminder_switch).isChecked
        if (hijriChecked || nextReminderChecked){
            findViewById<TextView>(R.id.error).visibility = View.GONE
            CoroutineScope(Dispatchers.IO).launch {
                updateWidgetSettings(context, hijriChecked, nextReminderChecked)
                val appWidgetIds = arrayOf(appWidgetId)
                fetchDateFromDatabase(context, appWidgetManager, appWidgetIds )
            }
        }
        else
            findViewById<TextView>(R.id.error).visibility = View.VISIBLE

        // Make sure we pass back the original appWidgetId
        val resultValue = Intent()
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        setResult(RESULT_OK, resultValue)
        finish()
    }

}


