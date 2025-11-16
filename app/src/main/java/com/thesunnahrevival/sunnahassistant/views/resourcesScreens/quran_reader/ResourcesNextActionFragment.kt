package com.thesunnahrevival.sunnahassistant.views.resourcesScreens.quran_reader

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.thesunnahrevival.sunnahassistant.R
import com.thesunnahrevival.sunnahassistant.theme.SunnahAssistantTheme
import com.thesunnahrevival.sunnahassistant.views.home.resourcesSection.ResourceCard
import com.thesunnahrevival.sunnahassistant.views.home.resourcesSection.ResourceTitle
import com.thesunnahrevival.sunnahassistant.views.utilities.GrayLine

class ResourcesNextActionFragment : BottomSheetDialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        return ComposeView(requireContext()).apply {
            setContent {
                NextActionScreen()
            }
        }
    }
}

@Composable
fun NextActionScreen() {
    SunnahAssistantTheme {
        Surface {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 32.dp)
            ) {
                GrayLine(modifier = Modifier.align(Alignment.CenterHorizontally))

                ResourceTitle(
                    title = stringResource(R.string.suggested_actions)
                )

                ResourceCard(
                    title = stringResource(R.string.weekly_friday_reminder),
                    subtitle = stringResource(R.string.set_reminder_for_every_friday)
                ) {}

                ResourceCard(
                    title = stringResource(R.string.remind_others),
                    subtitle = stringResource(R.string.remind_others_to_read_suratul_kahf)
                ) {}
            }

        }
    }
}

@Composable
@Preview
fun LightModePreview() {
    NextActionScreen()
}

@Composable
@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    locale = "en"
)
fun DarkModePreview() {
    NextActionScreen()
}