package com.thesunnahrevival.sunnahassistant.views.utilities

import android.os.Build
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Checkbox
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration

@Composable
fun SunnahAssistantCheckbox(
    text: String,
    modifier: Modifier = Modifier,
    checked: Boolean = false,
    maxLines: Int = 1,
    enabled: Boolean = true,
    onSelection: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Checkbox(
            enabled = enabled,
            checked = checked,
            onCheckedChange = {
                onSelection()
            },
        )
        Text(
            text = text,
            maxLines = maxLines,
            modifier = Modifier
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }) { onSelection() })
    }
}

@Composable
fun isArabic(): Boolean {
    val configuration = LocalConfiguration.current
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        configuration.locales[0].language == "ar"
    } else {
        configuration.locale.language == "ar"
    }
}