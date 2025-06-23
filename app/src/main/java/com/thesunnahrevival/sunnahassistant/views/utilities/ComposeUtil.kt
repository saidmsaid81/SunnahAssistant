package com.thesunnahrevival.sunnahassistant.views.utilities

import androidx.compose.foundation.layout.Row
import androidx.compose.material.Checkbox
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment

@Composable
fun SunnahAssistantCheckbox(
    text: String,
    checked: Boolean = false,
    maxLines: Int = 1,
    enabled: Boolean = true,
    onSelection: () -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(
            enabled = enabled,
            checked = checked,
            onCheckedChange = {
                onSelection()
            },
        )
        Text(text = text, maxLines = maxLines)
    }
}