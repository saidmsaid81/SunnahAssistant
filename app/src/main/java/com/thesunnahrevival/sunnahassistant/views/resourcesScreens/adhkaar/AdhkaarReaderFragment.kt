package com.thesunnahrevival.sunnahassistant.views.resourcesScreens.adhkaar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.thesunnahrevival.sunnahassistant.theme.SunnahAssistantTheme
import com.thesunnahrevival.sunnahassistant.viewmodels.AdhkaarViewModel
import com.thesunnahrevival.sunnahassistant.views.SunnahAssistantFragment
import com.thesunnahrevival.sunnahassistant.views.resourcesScreens.quran_reader.FootnoteText
import com.thesunnahrevival.sunnahassistant.views.utilities.ArabicTextUtils
import com.thesunnahrevival.sunnahassistant.views.utilities.FontLoader
import com.valentinilk.shimmer.shimmer

class AdhkaarReaderFragment : SunnahAssistantFragment() {

    private val viewModel: AdhkaarViewModel by viewModels()
    private val args: AdhkaarReaderFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)

        val chapterId = args.chapterId


        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {

                val pagerState = rememberPagerState(initialPage = chapterId) { 133 }
                val uiState by viewModel.getAdhkaarItemsByChapterId(pagerState.currentPage).collectAsState()
                val uthmaniFont = FontLoader.loadUthmaniFont()
                val baseTextSize = 18.sp
                val arabicTextScale = 1.4f


                SunnahAssistantTheme {
                    Surface {
                        if (uiState.isLoading) {
                            LazyColumn(modifier = Modifier.padding(16.dp).shimmer()) {
                                items(6) { index ->
                                    if (index > 0) {
                                        Divider(
                                            color = Color.LightGray.copy(alpha = 0.4f),
                                            thickness = 1.dp,
                                            modifier = Modifier.padding(vertical = 16.dp)
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(0.3f)
                                            .height(20.dp)
                                            .background(
                                                color = Color.Gray.copy(alpha = 0.2f),
                                                shape = MaterialTheme.shapes.small
                                            )
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    repeat(3) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(28.dp)
                                                .background(
                                                    color = Color.Gray.copy(alpha = 0.15f),
                                                    shape = MaterialTheme.shapes.medium
                                                )
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                    }

                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(0.2f)
                                            .height(16.dp)
                                            .background(
                                                color = Color.Gray.copy(alpha = 0.2f),
                                                shape = MaterialTheme.shapes.small
                                            )
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    repeat(2) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth(if (it == 0) 1f else 0.8f)
                                                .height(18.dp)
                                                .background(
                                                    color = Color.Gray.copy(alpha = 0.15f),
                                                    shape = MaterialTheme.shapes.small
                                                )
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                    }
                                }
                            }
                        } else {
                            HorizontalPager(state = pagerState) { _ ->
                                LazyColumn(modifier = Modifier.padding(16.dp)) {
                                    items(uiState.adhkaarItems.size) { index ->
                                        val adhkaarItem = uiState.adhkaarItems[index]

                                        if (index > 0) {
                                            Divider(
                                                color = Color.LightGray,
                                                thickness = 1.dp,
                                                modifier = Modifier.padding(vertical = 16.dp)
                                            )
                                        }

                                        Text(
                                            text = "${adhkaarItem.itemId}",
                                            style = MaterialTheme.typography.subtitle1,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colors.primary
                                        )

                                        Column(modifier = Modifier.fillMaxWidth()) {
                                            adhkaarItem.arabicText?.let {
                                                Text(
                                                    text = ArabicTextUtils.formatArabicText(it),
                                                    fontSize = baseTextSize * arabicTextScale,
                                                    fontFamily = uthmaniFont,
                                                    textAlign = TextAlign.Right,
                                                    letterSpacing = 1.sp,
                                                    lineHeight = (baseTextSize * arabicTextScale * 1.6f),
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(bottom = 16.dp, start = 8.dp, end = 8.dp)
                                                        .fillMaxWidth(),
                                                    style = MaterialTheme.typography.body1.copy(
                                                        fontFeatureSettings = "liga"
                                                    )
                                                )
                                            }

                                            adhkaarItem.englishText?.let {
                                                Text(
                                                    text = "English",
                                                    fontSize = 14.sp,
                                                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                                                )

                                                Column {
                                                    Text(
                                                        text = adhkaarItem.englishText,
                                                        style = MaterialTheme.typography.body1.copy(
                                                            fontSize = 16.sp,
                                                            color = MaterialTheme.colors.onSurface
                                                        ),
                                                        modifier = Modifier.padding(bottom = 8.dp)
                                                    )
                                                }
                                            }

                                            adhkaarItem.reference?.let { FootnoteText(text = it) }

                                            Spacer(modifier = Modifier.height(16.dp))
                                        }

                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}