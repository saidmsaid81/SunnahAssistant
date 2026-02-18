package com.thesunnahrevival.sunnahassistant.views.utilities

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight

object FontLoader {
    private var uthmaniFont: FontFamily? = null

    @Composable
    fun loadUthmaniFont(): FontFamily {
        val context = LocalContext.current
        return remember {
            uthmaniFont ?: createUthmaniFont(context).also { uthmaniFont = it }
        }
    }

    private fun createUthmaniFont(context: Context): FontFamily {
        return FontFamily(
            Font(
                "uthmanic_hafs_ver12.otf",
                context.assets,
                weight = FontWeight.Normal
            )
        )
    }
}