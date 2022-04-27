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
import kotlinx.android.synthetic.main.sunnah_assistant_configure_widget.*

abstract class SunnahAssistantConfigureWidgetActivity : AppCompatActivity(), View.OnClickListener,
    PopupMenu.OnMenuItemClickListener {
    var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID


    public override fun onCreate(bundle: Bundle?) {
        super.onCreate(bundle)

        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if the user presses the back button.
        setResult(RESULT_CANCELED)

        setContentView(R.layout.sunnah_assistant_configure_widget)
        setSupportActionBar(findViewById(R.id.toolbar))
        add_button.setOnClickListener(this)
        theme_settings.setOnClickListener(this)

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
            theme_settings -> {
                val popupMenu = PopupMenu(context, findViewById(R.id.theme))
                val options = resources.getStringArray(R.array.widget_theme_options)
                for ((index, method) in options.withIndex()) {
                    popupMenu.menu.add(R.id.theme_settings, Menu.NONE, index, method)
                }
                popupMenu.setOnMenuItemClickListener(this)
                popupMenu.show()
            }
            add_button -> {
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