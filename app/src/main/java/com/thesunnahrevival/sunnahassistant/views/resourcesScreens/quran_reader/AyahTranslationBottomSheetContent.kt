package com.thesunnahrevival.sunnahassistant.views.resourcesScreens.quran_reader

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.BookmarkAdd
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thesunnahrevival.sunnahassistant.R
import com.thesunnahrevival.sunnahassistant.data.model.Footnote
import com.thesunnahrevival.sunnahassistant.data.model.FullAyahDetails
import com.thesunnahrevival.sunnahassistant.data.model.Translation
import com.thesunnahrevival.sunnahassistant.views.utilities.ArabicTextUtils
import com.thesunnahrevival.sunnahassistant.views.utilities.FontLoader
import com.thesunnahrevival.sunnahassistant.views.utilities.SunnahAssistantCheckbox

@Composable
fun SheetContent(
    selectedAyah: FullAyahDetails,
    translations: List<Translation>,
    selectedTranslations: List<Translation>,
    nextAyah: () -> Unit,
    previousAyah: () -> Unit,
    onSelection: (Translation) -> Unit,
    visibleFootnotes: Map<String, Footnote>,
    onFootnoteClick: (ayahTranslationId: Int, footnoteNumber: Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        GrayLine(modifier = Modifier.align(Alignment.CenterHorizontally))

        Spacer(modifier = Modifier.height(16.dp))

        AyahTitle(
            selectedAyah.surah.transliteratedName,
            stringResource(R.string.ayah_number, selectedAyah.ayah.number),
            modifier = Modifier.fillMaxWidth(),
            Alignment.CenterHorizontally
        )

        TranslationDropdown(translations, selectedTranslations, onSelection)

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            item {
                AyahTranslations(
                    selectedAyah,
                    selectedTranslations,
                    visibleFootnotes,
                    onFootnoteClick,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        AyahInteractionRow(
            selectedAyah,
            selectedTranslations,
            LocalContext.current,
            Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )

        Row(modifier = Modifier.fillMaxWidth()) {
            Next {
                nextAyah()
            }

            Spacer(modifier = Modifier.weight(1f))

            Previous {
                previousAyah()
            }
        }
    }
}

@Composable
fun AyahInteractionRow(
    selectedAyah: FullAyahDetails,
    selectedTranslations: List<Translation>,
    context: Context,
    modifier: Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center
    ) {
        val ayahTexts = getAyahText(
            selectedAyah,
            selectedTranslations,
            stringResource(R.string.surah_number, selectedAyah.surah.id ?: 0)
        )

        ShareIcon(
            context = context,
            textToShare = ayahTexts,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(32.dp))

        CopyIcon(
            context,
            ayahTexts,
            stringResource(R.string.copy_label),
            stringResource(R.string.copy_message),
            Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(32.dp))

        BookmarkIcon(modifier = Modifier.size(24.dp)) {

        }
    }
}

@Composable
fun Next(modifier: Modifier = Modifier, onClick: () -> Unit) {
    Icon(
        imageVector = Icons.Filled.KeyboardArrowDown,
        contentDescription = stringResource(R.string.next),
        modifier = modifier
            .size(36.dp)
            .clickable { onClick() }
    )
}

@Composable
fun Previous(modifier: Modifier = Modifier, onClick: () -> Unit) {
    Icon(
        imageVector = Icons.Filled.KeyboardArrowUp,
        contentDescription = stringResource(R.string.previous),
        modifier = modifier
            .size(36.dp)
            .clickable { onClick() }
    )
}

@Composable
fun BookmarkIcon(modifier: Modifier = Modifier, onClick: () -> Unit) {
    Icon(
        imageVector = Icons.Outlined.BookmarkAdd,
        contentDescription = stringResource(R.string.bookmark),
        modifier = modifier
            .clickable { onClick() }
    )
}

@Composable
fun CopyIcon(
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
fun ShareIcon(
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
fun AyahTranslations(
    selectedAyah: FullAyahDetails,
    selectedTranslations: List<Translation>,
    visibleFootnotes: Map<String, Footnote>,
    onFootnoteClick: (ayahTranslationId: Int, footnoteNumber: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val uthmaniFont = FontLoader.loadUthmaniFont()
    val baseTextSize = 18.sp
    val arabicTextScale = 1.4f

    Column(modifier = modifier) {
        Text(
            text = ArabicTextUtils.formatArabicText(selectedAyah.ayah.arabicText),
            fontSize = baseTextSize * arabicTextScale,
            fontFamily = uthmaniFont,
            textAlign = TextAlign.Right,
            letterSpacing = 1.sp,
            lineHeight = (baseTextSize * arabicTextScale * 1.6f),
            modifier = modifier
                .padding(bottom = 16.dp, start = 8.dp, end = 8.dp)
                .fillMaxWidth(),
            style = MaterialTheme.typography.body1.copy(
                fontFeatureSettings = "liga"
            )
        )

        val selectedTranslationIds = selectedTranslations.map { it.id }.toSet()

        selectedAyah.ayahTranslations
            .filter { it.translation.id in selectedTranslationIds }
            .forEach { translationWithFootnotes ->
                Text(
                    text = translationWithFootnotes.translation.name,
                    fontSize = 14.sp,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                )

                Column {
                    val ayahTranslationText = translationWithFootnotes.ayahTranslation.text
                    val annotatedText = buildAnnotatedString {
                        val pattern = "\\[(\\d+)\\]".toRegex()
                        var lastIndex = 0

                        pattern.findAll(ayahTranslationText).forEach { matchResult ->
                            // Add text before the footnote
                            append(
                                ayahTranslationText.substring(
                                    lastIndex,
                                    matchResult.range.first
                                )
                            )

                            withLink(
                                LinkAnnotation.Clickable(
                                    tag = "footnote",
                                    linkInteractionListener = {
                                        onFootnoteClick(
                                            translationWithFootnotes.ayahTranslation.id,
                                            matchResult.groupValues[1].toInt()
                                        )
                                    })
                            ) {
                                withStyle(
                                    style = SpanStyle(
                                        baselineShift = BaselineShift.Superscript,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colors.primary
                                    )
                                ) {
                                    append(matchResult.groupValues[1])
                                }
                            }

                            lastIndex = matchResult.range.last + 1
                        }

                        // Append remaining text
                        if (lastIndex < ayahTranslationText.length) {
                            append(ayahTranslationText.substring(lastIndex))
                        }
                    }

                    Text(
                        text = annotatedText,
                        style = MaterialTheme.typography.body1.copy(
                            fontSize = 16.sp,
                            color = MaterialTheme.colors.onSurface
                        ),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Display footnotes if visible
                    val pattern = "\\[(\\d+)\\]".toRegex()
                    pattern.findAll(ayahTranslationText).forEach { matchResult ->
                        val footnoteNumber = matchResult.groupValues[1].toInt()
                        val footnoteKey =
                            "${translationWithFootnotes.ayahTranslation.id}-$footnoteNumber"

                        visibleFootnotes[footnoteKey]?.let { footnote ->
                            FootnoteText(footnote.number, footnote.text)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
    }
}

@Composable
private fun FootnoteText(number: Int, text: String) {
    Row(
        modifier = Modifier
            .padding(start = 16.dp, end = 16.dp, bottom = 8.dp)
            .background(
                color = MaterialTheme.colors.surface,
                shape = RoundedCornerShape(4.dp)
            )
            .padding(8.dp),
    ) {
        Text(
            text = number.toString(),
            style = MaterialTheme.typography.caption.copy(
                baselineShift = BaselineShift.Superscript,
                fontSize = 12.sp,
                color = MaterialTheme.colors.primary
            ),
            modifier = Modifier.padding(end = 4.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.caption,
            color = MaterialTheme.colors.onSurface,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun TranslationDropdown(
    translations: List<Translation>,
    selectedTranslations: List<Translation>,
    onSelection: (Translation) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(bottom = 16.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
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
                overflow = TextOverflow.Ellipsis
            )
            Icon(
                imageVector = Icons.Filled.ArrowDropDown,
                contentDescription = stringResource(R.string.dropdown_arrow)
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth()
        ) {
            translations.forEach { translation ->
                DropdownMenuItem(onClick = {
                    onSelection(translation)
                }) {
                    SunnahAssistantCheckbox(
                        text = translation.name,
                        checked = translation.selected
                    ) {
                        onSelection(translation)
                    }
                }
            }
        }
    }
}

@Composable
fun AyahTitle(
    title: String,
    subTitle: String,
    modifier: Modifier,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start
) {
    Column(
        horizontalAlignment = horizontalAlignment,
        modifier = modifier
    ) {
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Text(
            text = stringResource(id = R.string.ayah_number, subTitle.split(" ")[1].toInt()),
            fontSize = 12.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )
    }
}

@Composable
fun GrayLine(modifier: Modifier) {
    Box(
        modifier = modifier
            .width(40.dp)
            .height(4.dp)
            .background(
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.2f),
                shape = RoundedCornerShape(2.dp)
            )
            .padding(bottom = 16.dp)
    )
}

private fun shareText(
    context: Context,
    textToShare: String,
    title: String
) {
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(
            Intent.EXTRA_TEXT,
            textToShare
        )
    }
    context.startActivity(Intent.createChooser(shareIntent, title))
}

private fun copyToClipboard(
    context: Context,
    textToCopy: String,
    label: String,
    message: String
) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

    val clip = ClipData.newPlainText(
        label,
        textToCopy
    )
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}

private fun getAyahText(
    ayah: FullAyahDetails,
    selectedTranslations: List<Translation>,
    surahNumber: String
): String {
    val selectedTranslationIds = selectedTranslations.map { it.id }.toSet()

    val translations = ayah.ayahTranslations
        .filter { it.translation.id in selectedTranslationIds }
        .joinToString(separator = "") {
            "${it.translation.name} \n" +
                    "${it.ayahTranslation.text} \n\n"
        }

    return "${ayah.surah.transliteratedName} ($surahNumber)\n\n" +
            "Ayah ${ayah.ayah.number}\n" +
            "${ayah.ayah.arabicText}\n\n" +
            translations
}