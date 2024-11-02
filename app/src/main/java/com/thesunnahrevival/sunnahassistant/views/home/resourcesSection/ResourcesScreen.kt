package com.thesunnahrevival.sunnahassistant.views.home.resourcesSection

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.navigation.NavController
import com.thesunnahrevival.sunnahassistant.R
import com.thesunnahrevival.sunnahassistant.utilities.ResourceItem


@Composable
@Preview
fun ResourcesScreenPreview() {
    ResourcesScreen()
}

@Composable
fun ResourcesScreen(findNavController: NavController? = null) {

    val resourceItemList = resourceItems()

    Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(start = 16.dp, end = 16.dp)
    ) {
        Column {
            Text(
                text = stringResource(R.string.selected_surahs),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp, top = 16.dp),
                style = MaterialTheme.typography.body1.copy(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                ),
            )

            LazyColumn {
                itemsIndexed(resourceItemList) { index, item ->
                    Card(
                        elevation = 4.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp)
                            .clickable {
                                if (item.destination == R.id.quranReaderFragment) {
                                    findNavController?.navigate(
                                        ResourcesFragmentDirections.toQuranReaderFragment(
                                            item
                                        )
                                    )
                                } else {
                                    findNavController?.navigate(item.destination)
                                }
                            }
                    ) {
                        Row(modifier = Modifier.padding(16.dp)) {
                            Column(modifier = Modifier.weight(1F)) {
                                Text(
                                    text = item.title,
                                    fontSize = 16.sp,
                                    style = MaterialTheme.typography.subtitle1,
                                    fontWeight = FontWeight.W500
                                )
                                Text(
                                    text = item.description,
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

                    if (index == (resourceItemList.size - 2)) {
                        Text(
                            text = stringResource(R.string.hadith),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp, top = 16.dp),
                            style = MaterialTheme.typography.body1.copy(
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium
                            ),
                        )
                    }
                }
            }
        }
    }
}


@Composable
private fun resourceItems(): List<ResourceItem> {
    return listOf(
        ResourceItem(
            1,
            stringResource(R.string.suratul_fatiha),
            stringResource(R.string.recommended_to_read_regularly),
            R.id.quranReaderFragment,
            listOf(1)
        ),
        ResourceItem(
            2,
            stringResource(R.string.suratul_baqarah),
            stringResource(R.string.recommended_to_read_regularly),
            R.id.quranReaderFragment,
            (2..49).toList()
        ),
        ResourceItem(
            18,
            stringResource(R.string.suratul_kahf),
            stringResource(R.string.recommended_to_read_every_friday),
            R.id.quranReaderFragment,
            (293..304).toList()

        ),
        ResourceItem(
            32,
            stringResource(R.string.suratul_sajdah),
            stringResource(R.string.recommended_to_read_before_sleeping),
            R.id.quranReaderFragment,
            (415..417).toList()

        ),
        ResourceItem(
            67,
            stringResource(R.string.suratulmulk),
            stringResource(R.string.recommended_to_read_before_sleeping),
            R.id.quranReaderFragment,
            (562..564).toList()

        ),
        ResourceItem(
            112,
            stringResource(R.string.suratul_ikhlaas),
            stringResource(R.string.equivalent_in_reward_to_reciting_1_3_of_the_quran),
            R.id.quranReaderFragment,
            listOf(604)

        ),
        ResourceItem(
            113,
            stringResource(R.string.suratul_falaq),
            stringResource(R.string.for_protection_from_jinn_and_evil_eye),
            R.id.quranReaderFragment,
            listOf(604)
        ),
        ResourceItem(
            114,
            stringResource(R.string.suratul_nas),
            stringResource(R.string.for_protection_from_jinn_and_evil_eye),
            R.id.quranReaderFragment,
            listOf(604)
        ),
        ResourceItem(
            1,
            stringResource(R.string.daily_hadith),
            stringResource(R.string.from_the_sunnah_revival_blog),
            R.id.dailyHadithFragment
        )
    )
}

