package com.thesunnahrevival.sunnahassistant.views.utilities

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BookmarkAdd
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thesunnahrevival.sunnahassistant.R
import com.thesunnahrevival.sunnahassistant.views.resourcesScreens.quran_reader.BookmarkIcon
import com.thesunnahrevival.sunnahassistant.views.resourcesScreens.quran_reader.CopyIcon
import com.thesunnahrevival.sunnahassistant.views.resourcesScreens.quran_reader.ShareIcon

@Composable
fun ArabicTextWithTranslation(
    context: Context,
    index: Int,
    sectionMarker: String,
    arabicText: String,
    translationTexts: List<TranslationText>,
    textToShare: String,
    onBookmarkClick: () -> Unit = {}
) {

    val uthmaniFont = FontLoader.loadUthmaniFont()
    val baseTextSize = 18.sp
    val arabicTextScale = 1.4f

    if (index > 0) {
        Divider(
            color = Color.LightGray,
            thickness = 1.dp,
            modifier = Modifier.padding(vertical = 16.dp)
        )
    }

    Text(
        text = sectionMarker,
        style = MaterialTheme.typography.subtitle1,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colors.primary
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = ArabicTextUtils.formatArabicText(arabicText),
            fontSize = baseTextSize * arabicTextScale,
            fontFamily = uthmaniFont,
            textAlign = TextAlign.Right,
            letterSpacing = 1.sp,
            lineHeight = (baseTextSize * arabicTextScale * 1.6f),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp, start = 8.dp, end = 8.dp),
            style = MaterialTheme.typography.body1.copy(
                fontFeatureSettings = "liga"
            )
        )

        for (translationText in translationTexts) {
            Text(
                text = translationText.title,
                fontSize = 14.sp,
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
            )

            Text(
                text = translationText.text,
                style = MaterialTheme.typography.body1.copy(
                    fontSize = 16.sp,
                    color = MaterialTheme.colors.onSurface
                ),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            if (translationText.footnotes.isNotEmpty()) {
                if (translationText.footnoteLabel.isNotEmpty()) {
                    Text(
                        text = translationText.footnoteLabel,
                        fontSize = 14.sp,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                    )
                }

                for (footnote in translationText.footnotes) {
                    Text(
                        text = footnote,
                        style = MaterialTheme.typography.caption,
                        color = MaterialTheme.colors.onSurface,
                    )
                    Spacer(modifier = Modifier.padding(vertical = 8.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {

            ShareIcon(
                context = context,
                textToShare = textToShare,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(32.dp))

            CopyIcon(
                context,
                textToShare,
                stringResource(R.string.copy_label),
                stringResource(R.string.copy_message),
                Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(32.dp))

            BookmarkIcon(icon = Icons.Outlined.BookmarkAdd, modifier = Modifier.size(24.dp)) {
                onBookmarkClick()
            }
        }
    }
}

data class TranslationText(val title: String, val text: AnnotatedString, val footnoteLabel: String = "", val footnotes: List<AnnotatedString>)

@Composable
fun ArabicTextWithTranslationShimmer(index: Int) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
        if (index > 0) {
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color.LightGray.copy(alpha = 0.5f))
                    .padding(vertical = 16.dp)
            )
        }

        Spacer(
            modifier = Modifier
                .width(80.dp)
                .height(20.dp)
                .padding(top = 8.dp)
                .background(Color.LightGray.copy(alpha = 0.5f))
        )

        Spacer(modifier = Modifier.height(12.dp))

        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .background(Color.LightGray.copy(alpha = 0.5f))
        )

        Spacer(modifier = Modifier.height(16.dp))

        Spacer(
            modifier = Modifier
                .width(120.dp)
                .height(16.dp)
                .background(Color.LightGray.copy(alpha = 0.5f))
        )

        Spacer(modifier = Modifier.height(8.dp))

        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .background(Color.LightGray.copy(alpha = 0.5f))
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Spacer(
                modifier = Modifier
                    .size(24.dp)
                    .background(Color.LightGray.copy(alpha = 0.5f))
            )
            Spacer(modifier = Modifier.width(32.dp))
            Spacer(
                modifier = Modifier
                    .size(24.dp)
                    .background(Color.LightGray.copy(alpha = 0.5f))
            )
            Spacer(modifier = Modifier.width(32.dp))
            Spacer(
                modifier = Modifier
                    .size(24.dp)
                    .background(Color.LightGray.copy(alpha = 0.5f))
            )
        }
    }
}