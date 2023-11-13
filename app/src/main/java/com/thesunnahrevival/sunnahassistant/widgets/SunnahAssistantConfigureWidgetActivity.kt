package com.thesunnahrevival.sunnahassistant.widgets

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.thesunnahrevival.sunnahassistant.R
import com.thesunnahrevival.sunnahassistant.databinding.ActivitySunnahAssistantConfigureWidgetBinding

abstract class SunnahAssistantConfigureWidgetActivity : AppCompatActivity(), View.OnClickListener,
    PopupMenu.OnMenuItemClickListener {
    var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    private lateinit var sunnahAssistantConfigureWidgetActivityBinding: ActivitySunnahAssistantConfigureWidgetBinding

    public override fun onCreate(bundle: Bundle?) {
        super.onCreate(bundle)

        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if the user presses the back button.
        setResult(RESULT_CANCELED)

        sunnahAssistantConfigureWidgetActivityBinding =
            ActivitySunnahAssistantConfigureWidgetBinding.inflate(layoutInflater)
        setContentView(sunnahAssistantConfigureWidgetActivityBinding.root)
        setSupportActionBar(findViewById(R.id.toolbar))
        sunnahAssistantConfigureWidgetActivityBinding.addButton.setOnClickListener(this)
        sunnahAssistantConfigureWidgetActivityBinding.themeSettings.setOnClickListener(this)

        // Find the widget id from the intent.
        val intent = intent
        val extras = intent.extras
        if (extras != null) {
            appWidgetId = extras.getInt(
                AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID
            )
        }
        // If this activity was started with an intent without an app widget ID, finish with an error.
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }
    }

    override fun onClick(v: View?) {
        val context = this
        when (v) {
            sunnahAssistantConfigureWidgetActivityBinding.themeSettings -> {
                val popupMenu = PopupMenu(context, findViewById(R.id.theme))
                val options = resources.getStringArray(R.array.widget_theme_options)
                for ((index, method) in options.withIndex()) {
                    popupMenu.menu.add(R.id.theme_settings, Menu.NONE, index, method)
                }
                popupMenu.setOnMenuItemClickListener(this)
                popupMenu.show()
            }

            sunnahAssistantConfigureWidgetActivityBinding.addButton -> {
                createWidget(context)
                val resultValue = Intent()
                resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                setResult(RESULT_OK, resultValue)
                finish()
            }
        }
    }

    abstract fun createWidget(context: SunnahAssistantConfigureWidgetActivity)

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        when (item?.groupId) {
            R.id.theme_settings -> {
                findViewById<TextView>(R.id.theme).text = item.title.toString()
            }
        }
        return true
    }

}