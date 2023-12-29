package com.thesunnahrevival.sunnahassistant.views.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thesunnahrevival.sunnahassistant.R
import com.thesunnahrevival.sunnahassistant.databinding.FragmentResourcesBinding
import com.thesunnahrevival.sunnahassistant.theme.SunnahAssistantTheme
import com.thesunnahrevival.sunnahassistant.utilities.ResourceItem

class ResourcesFragment : MenuBarFragment() {

    private var _resourcesFragmentBinding: FragmentResourcesBinding? = null

    private val resourcesFragmentBinding get() = _resourcesFragmentBinding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        _resourcesFragmentBinding = FragmentResourcesBinding.inflate(inflater).apply {
            composeView.setContent {
                SunnahAssistantTheme {
                    ResourcesScreen()
                }
            }
        }
        return resourcesFragmentBinding.root
    }

    @Composable
    @Preview
    fun ResourcesScreen() {

        val selectedSurahs = selectedSurahs()
        val hadith = hadith()

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(start = 16.dp, end = 16.dp)
        ) {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                ResourceSection(selectedSurahs, stringResource(R.string.selected_surahs))
//                ResourceSection(hadith, "Hadith")
            }
        }
    }


    @Composable
    private fun selectedSurahs(): List<ResourceItem> {
        return listOf<ResourceItem>(
            ResourceItem(
                1,
                stringResource(R.string.suratul_baqarah),
                stringResource(R.string.recommended_to_read_regularly), ""
            ),
            ResourceItem(
                18,
                stringResource(R.string.suratul_kahf),
                stringResource(R.string.recommended_to_read_every_friday), ""
            ),
            ResourceItem(
                32,
                stringResource(R.string.suratul_sajdah),
                stringResource(R.string.recommended_to_read_before_sleeping), ""
            ),
            ResourceItem(
                67,
                stringResource(R.string.suratulmulk),
                stringResource(R.string.recommended_to_read_before_sleeping),
                ""
            ),
            ResourceItem(
                112,
                stringResource(R.string.suratul_ikhlaas),
                stringResource(R.string.equivalent_in_reward_to_reciting_1_3_of_the_quran), ""
            ),
            ResourceItem(
                113,
                stringResource(R.string.suratul_falaq),
                stringResource(R.string.for_protection_from_jinn_and_evil_eye), ""
            ),
            ResourceItem(
                114,
                stringResource(R.string.suratul_nas),
                stringResource(R.string.for_protection_from_jinn_and_evil_eye),
                ""
            )
        )
    }

    @Composable
    private fun hadith(): List<ResourceItem> {
        return listOf(
            ResourceItem(
                1, "Daily Hadith", "From The Sunnah Revival Blog", ""
            )
        )
    }

    @Composable
    private fun ResourceSection(resourceItems: List<ResourceItem>, sectionTitle: String) {
        Text(
            text = sectionTitle,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp, top = 16.dp),
            style = MaterialTheme.typography.body1.copy(
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            ),
        )

        for (resourceItem in resourceItems) {
            Card(
                elevation = 4.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp)
            ) {
                Row(modifier = Modifier.padding(16.dp)) {
                    Column(modifier = Modifier.weight(1F)) {
                        Text(
                            text = resourceItem.title,
                            fontSize = 16.sp,
                            style = MaterialTheme.typography.subtitle1,
                            fontWeight = FontWeight.W500
                        )
                        Text(
                            text = resourceItem.description,
                            fontSize = 13.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowRight,
                        contentDescription = "",
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .height(36.dp)
                            .width(36.dp)
                    )
                }
            }
        }
    }
}