package com.thesunnahrevival.sunnahassistant.views

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.thesunnahrevival.sunnahassistant.R

class WelcomeActivity : AppCompatActivity(), View.OnClickListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)
    }

    override fun onClick(v: View) {
        startActivity(Intent(this,MainActivity::class.java))
    }
}