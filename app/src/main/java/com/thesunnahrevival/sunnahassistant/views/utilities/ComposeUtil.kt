package com.thesunnahrevival.sunnahassistant.views.utilities

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Checkbox
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

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