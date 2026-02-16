package com.thesunnahrevival.sunnahassistant.views.utilities

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.BookmarkAdd
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thesunnahrevival.sunnahassistant.R
import com.thesunnahrevival.sunnahassistant.data.model.entity.Translation
import com.thesunnahrevival.sunnahassistant.utilities.SUPPORT_EMAIL
import com.thesunnahrevival.sunnahassistant.utilities.copyToClipboard
import com.thesunnahrevival.sunnahassistant.utilities.shareText
import com.valentinilk.shimmer.shimmer

@Composable
fun ArabicTextWithTranslation(
    context: Context,
    index: Int,
    sectionMarker: String,
    arabicText: String,
    translationTexts: List<TranslationText>,
    textToShare: String,
    bookmarked: Boolean = false,
    onBookmarkClick: () -> Unit = {},
    arabicTextFontSize: Int = 18,
    translationTextFontSize: Int = 16,
    footnoteTextFontSize: Int = 12,
    showTopDivider: Boolean = index > 0,
    showSectionMarker: Boolean = true,
    showActions: Boolean = true
) {

    val uthmaniFont = FontLoader.loadUthmaniFont()
    val arabicTextScale = 1.4f

    if (showTopDivider) {
        Divider(
            color = Color.LightGray,
            thickness = 1.dp,
            modifier = Modifier.padding(vertical = 16.dp)
        )
    }

    if (showSectionMarker) {
        Text(
            text = sectionMarker,
            style = MaterialTheme.typography.subtitle1,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colors.primary
        )
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = ArabicTextUtils.formatArabicText(arabicText),
            fontSize = (arabicTextFontSize * arabicTextScale).sp,
            fontFamily = uthmaniFont,
            textAlign = TextAlign.Right,
            letterSpacing = 1.sp,
            lineHeight = (arabicTextFontSize * arabicTextScale * 1.6f).sp,
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
                    fontSize = translationTextFontSize.sp,
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
                        fontSize = footnoteTextFontSize.sp,
                        color = MaterialTheme.colors.onSurface,
                    )
                    Spacer(modifier = Modifier.padding(vertical = 8.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        if (showActions) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {

                ShareIcon(
                    context = context,
                    textToShare = textToShare,
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(24.dp))

                CopyIcon(
                    context,
                    textToShare,
                    stringResource(R.string.copy_label),
                    stringResource(R.string.copy_message),
                    Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(24.dp))

                BookmarkIcon(icon = if (bookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkAdd, modifier = Modifier.size(24.dp)) {
                    onBookmarkClick()
                }

                Spacer(modifier = Modifier.width(24.dp))

                ReportIssueIcon(
                    context = context,
                    textToReport = textToShare,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun BookmarkIcon(
    icon: ImageVector, modifier: Modifier = Modifier, onClick: () -> Unit
) {
    Icon(
        imageVector = icon,
        contentDescription = stringResource(R.string.bookmark),
        modifier = modifier
            .clickable { onClick() }
    )
}

@Composable
private fun CopyIcon(
    context: Context,
    textToCopy: String,
    label: String,
    message: String,
    modifier: Modifier = Modifier
) {
    Icon(
        imageVector = Icons.Filled.ContentCopy,
        contentDescription = stringResource(R.string.copy),
        modifier = modifier
            .clickable {
                copyToClipboard(
                    context,
                    textToCopy,
                    label,
                    message
                )
            }
    )
}

@Composable
private fun ShareIcon(
    context: Context,
    textToShare: String,
    modifier: Modifier = Modifier
) {
    val title = stringResource(R.string.share_via)
    Icon(
        imageVector = Icons.Filled.Share,
        contentDescription = stringResource(R.string.share),
        modifier = modifier
            .clickable {
                shareText(context, textToShare, title)
            }
    )
}

@Composable
private fun ReportIssueIcon(
    context: Context,
    textToReport: String,
    modifier: Modifier = Modifier
) {
    Icon(
        imageVector = Icons.Filled.ReportProblem,
        contentDescription = "Report Issue",
        modifier = modifier
            .clickable {
                val emailSubject = "Issue Report - Text Error"
                val emailBody = "Please describe the issue you found with the following text:\n\n" +
                        "Text Content:\n$textToReport\n\n" +
                        "Issue Description:\n[Please describe the issue here]\n\n" +
                        "Thank you for helping us improve the app!"
                
                val intent = Intent(Intent.ACTION_SENDTO).apply {
                    data = android.net.Uri.parse("mailto:")
                    putExtra(Intent.EXTRA_EMAIL, arrayOf(SUPPORT_EMAIL))
                    putExtra(Intent.EXTRA_SUBJECT, emailSubject)
                    putExtra(Intent.EXTRA_TEXT, emailBody)
                }
                
                val chooser = Intent.createChooser(intent, "Send Email")
                if (intent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(chooser)
                }
            }
    )
}


@Composable
fun TranslationDropdown(
    translations: List<Translation>,
    selectedTranslations: List<Translation>,
    translationsDownloadInProgress: List<Translation>,
    expanded: MutableState<Boolean>,
    onSelection: (Translation) -> Unit
) {
    val translationsDownloadInProgressIds = translationsDownloadInProgress.map { it.id }.toSet()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded.value = !expanded.value }
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = when {
                    selectedTranslations.isEmpty() -> stringResource(R.string.select_translation)
                    selectedTranslations.size == 1 -> selectedTranslations.firstOrNull()?.name ?: ""
                    else -> stringResource(
                        R.string.translations_selected,
                        selectedTranslations.size
                    )
                },
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = if (selectedTranslations.isEmpty()) MaterialTheme.colors.primary else Color.Unspecified
            )
            Icon(
                imageVector = Icons.Filled.ArrowDropDown,
                contentDescription = stringResource(R.string.dropdown_arrow)
            )
        }

        if (expanded.value) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colors.onError)
            ) {
                translations.forEach { translation ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSelection(translation)
                            }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (!translationsDownloadInProgressIds.contains(translation.id)) {
                            SunnahAssistantCheckbox(
                                text = translation.name,
                                checked = translation.selected,
                                onSelection = { onSelection(translation) }
                            )
                        } else {
                            Box {
                                SunnahAssistantCheckbox(
                                    text = translation.name,
                                    checked = translation.selected,
                                    onSelection = {  }
                                )
                                CircularProgressIndicator(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .align(Alignment.CenterStart)
                                        .offset(x = 15.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

data class TranslationText(val title: String, val text: AnnotatedString, val footnoteLabel: String = "", val footnotes: List<AnnotatedString>)

@Composable
fun ArabicTextWithTranslationShimmer(index: Int) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp).shimmer()) {
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
            Spacer(modifier = Modifier.width(24.dp))
            Spacer(
                modifier = Modifier
                    .size(24.dp)
                    .background(Color.LightGray.copy(alpha = 0.5f))
            )
            Spacer(modifier = Modifier.width(24.dp))
            Spacer(
                modifier = Modifier
                    .size(24.dp)
                    .background(Color.LightGray.copy(alpha = 0.5f))
            )
            Spacer(modifier = Modifier.width(24.dp))
            Spacer(
                modifier = Modifier
                    .size(24.dp)
                    .background(Color.LightGray.copy(alpha = 0.5f))
            )
        }
    }
}
