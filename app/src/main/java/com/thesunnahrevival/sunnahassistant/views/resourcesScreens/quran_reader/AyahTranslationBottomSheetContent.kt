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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Checkbox
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.BookmarkAdd
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thesunnahrevival.sunnahassistant.R
import com.thesunnahrevival.sunnahassistant.theme.SunnahAssistantTheme
import com.thesunnahrevival.sunnahassistant.views.adapters.Ayah
import com.thesunnahrevival.sunnahassistant.views.adapters.AyahTranslation
import com.thesunnahrevival.sunnahassistant.views.adapters.Surah

@Composable
fun SheetContent(
    selectedAyah: Ayah,
    allTranslations: List<AyahTranslation>,
    selectedTranslations: List<AyahTranslation>,
    nextAyah: () -> Unit,
    previousAyah: () -> Unit,
    onSelection: (AyahTranslation) -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {

        GrayLine(modifier = Modifier.align(Alignment.CenterHorizontally))

        Spacer(modifier = Modifier.height(16.dp))

        AyahTitle(
            selectedAyah.surah?.name ?: "",
            stringResource(R.string.ayah_number, selectedAyah.number),
            modifier = Modifier.fillMaxWidth(),
            Alignment.CenterHorizontally
        )

        TranslationDropdown(selectedTranslations, allTranslations, onSelection)

        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            AyahTranslations(
                selectedAyah,
                selectedTranslations
            )
        }
        // Share, Copy, and Bookmark Buttons (Centered)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.Center // Center the icons
        ) {

            val ayahTexts = getAyahText(
                selectedAyah,
                selectedTranslations,
                stringResource(R.string.surah_number, selectedAyah.surah?.number ?: 0)
            )

            ShareIcon(
                context = context,
                textToShare = ayahTexts,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(32.dp)) // Space between icons

            CopyIcon(
                context,
                ayahTexts,
                stringResource(R.string.copy_label),
                stringResource(R.string.copy_message),
                Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(32.dp)) // Space between icons

            BookmarkIcon(modifier = Modifier.size(24.dp)) {

            }
        }

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
    selectedAyah: Ayah,
    selectedTranslations: List<AyahTranslation>,
    modifier: Modifier = Modifier
) {
    Text(
        text = selectedAyah.arabicText,
        fontSize = 18.sp,
        modifier = modifier
            .padding(bottom = 16.dp)

    )

    // Translations List
    selectedAyah.ayahTranslations.filter {
        selectedTranslations.any { selectedTranslation -> selectedTranslation.id == it.id }
    }.forEach {
        Text(
            text = it.source,
            fontSize = 14.sp,
            color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
        )

        Text(
            text = it.text,
            fontSize = 16.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun TranslationDropdown(
    selectedTranslations: List<AyahTranslation>,
    allTranslations: List<AyahTranslation>,
    onSelection: (AyahTranslation) -> Unit
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
                    selectedTranslations.size == 1 -> stringResource(
                        R.string.translation,
                        allTranslations.find { it == selectedTranslations[0] }?.source ?: ""
                    )

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
            allTranslations.forEach { translation ->
                DropdownMenuItem(onClick = {
                    onSelection(translation)
                }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = selectedTranslations.contains(translation),
                            onCheckedChange = {
                                onSelection(translation)
                            },
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = translation.source, maxLines = 1)
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
    ayah: Ayah,
    selectedTranslations: List<AyahTranslation>,
    surahNumber: String
): String {
    val translations = ayah.ayahTranslations.filter {
        selectedTranslations.any { selectedTranslation -> selectedTranslation.id == it.id }
    }
        .joinToString(separator = "") {
            "${it.source} \n" +
                    "${it.text} \n\n"
        }

    return "${ayah.surah?.name} ($surahNumber)\n\n" +
            "Ayah ${ayah.number}\n" +
            "${ayah.arabicText}\n\n" +
            translations
}

@Preview(
    name = "Dark Mode",
    showBackground = true,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
@Preview(name = "Light Mode", showBackground = true)
@Composable
private fun SheetContentPreview() {
    val selectedAyah = Ayah(
        id = 1,
        number = 105,
        surah = Surah(17, "Suratul Isra"),
        arabicText = "وَبِٱلْحَقِّ أَنزَلْنَـٰهُ وَبِٱلْحَقِّ نَزَلَ ۗ وَمَآ أَرْسَلْنَـٰكَ إِلَّا مُبَشِّرًۭا وَنَذِيرًۭا",
        ayahTranslations = listOf(
            AyahTranslation(
                1,
                "Sahih International",
                "And with the truth We have sent it [i.e., the Qur’ān] down, and with the truth it has descended. And We have not sent you, [O Muḥammad], except as a bringer of good tidings and a warner."
            ),
            AyahTranslation(
                2,
                "Mohsin Khan & Muhammad al-Hilali",
                "And with truth We have sent it down (i.e. the Qur’ân), and with truth it has descended. And We have sent you (O Muhammad صلى الله عليه و سلم) as nothing but a bearer of glad tidings (of Paradise for those who follow your Message of Islâmic Monotheism), and a warner (of Hell-fire for those who refuse to follow your Message of Islâmic Monotheism)"
            )
        )
    )

    val allTranslations = listOf(
        AyahTranslation(
            1,
            "Sahih International",
            ""
        ),
        AyahTranslation(
            2,
            "Mohsin Khan & Muhammad al-Hilali",
            ""
        ),
        AyahTranslation(
            3,
            "Mufti Taqi Usmani",
            ""
        ),
        AyahTranslation(
            4,
            "Dr. Mustafa Khattab, the Clear Quran",
            ""
        ),
        AyahTranslation(
            5,
            "Abdul Haleem",
            ""
        )
    )

    SunnahAssistantTheme {
        Surface {
            SheetContent(
                selectedAyah,
                allTranslations,
                allTranslations.filter { it.id <= 2 },
                {},
                {},
                {})
        }
    }
}