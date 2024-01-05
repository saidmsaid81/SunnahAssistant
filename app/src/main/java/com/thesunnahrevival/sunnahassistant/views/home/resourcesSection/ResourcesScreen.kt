package com.thesunnahrevival.sunnahassistant.views.home.resourcesSection

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.thesunnahrevival.sunnahassistant.R
import com.thesunnahrevival.sunnahassistant.utilities.ResourceItem


@Composable
@Preview
fun ResourcesScreenPreview() {
    ResourcesScreen(bottomNavViewHeight = 150.dp)
}

@Composable
fun ResourcesScreen(bottomNavViewHeight: Dp, findNavController: NavController? = null) {

    val selectedSurahs = selectedSurahs()
    val hadith = hadith()

    Surface(
            modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .padding(start = 16.dp, end = 16.dp)
    ) {
        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            ResourceSection(selectedSurahs, stringResource(R.string.selected_surahs), findNavController)
            ResourceSection(hadith, stringResource(R.string.hadith), findNavController)
            Spacer(modifier = Modifier.height(bottomNavViewHeight))
        }
    }
}


@Composable
private fun selectedSurahs(): List<ResourceItem> {
    return listOf(
            ResourceItem(
                    1,
                    stringResource(R.string.suratul_baqarah),
                    stringResource(R.string.recommended_to_read_regularly), R.id.dailyHadithFragment
            
            ),
            ResourceItem(
                    18,
                    stringResource(R.string.suratul_kahf),
                    stringResource(R.string.recommended_to_read_every_friday), R.id.dailyHadithFragment
            
            ),
            ResourceItem(
                    32,
                    stringResource(R.string.suratul_sajdah),
                    stringResource(R.string.recommended_to_read_before_sleeping), R.id.dailyHadithFragment
            
            ),
            ResourceItem(
                    67,
                    stringResource(R.string.suratulmulk),
                    stringResource(R.string.recommended_to_read_before_sleeping),
                    R.id.dailyHadithFragment
            
            ),
            ResourceItem(
                    112,
                    stringResource(R.string.suratul_ikhlaas),
                    stringResource(R.string.equivalent_in_reward_to_reciting_1_3_of_the_quran), R.id.dailyHadithFragment
            
            ),
            ResourceItem(
                    113,
                    stringResource(R.string.suratul_falaq),
                    stringResource(R.string.for_protection_from_jinn_and_evil_eye), R.id.dailyHadithFragment
            
            ),
            ResourceItem(
                    114,
                    stringResource(R.string.suratul_nas),
                    stringResource(R.string.for_protection_from_jinn_and_evil_eye),
                    R.id.dailyHadithFragment
            )
    )
}

@Composable
private fun hadith(): List<ResourceItem> {
    return listOf(
            ResourceItem(
                    1, stringResource(R.string.daily_hadith), stringResource(R.string.from_the_sunnah_revival_blog), R.id.dailyHadithFragment
            )
    )
}

@Composable
private fun ResourceSection(resourceItems: List<ResourceItem>, sectionTitle: String, findNavController: NavController?) {
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
                        .clickable {
                            Log.v("ResourcesScreen", "Clicked")
                            findNavController?.navigate(resourceItem.destination)
                        }
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