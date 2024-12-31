package com.thesunnahrevival.sunnahassistant.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.thesunnahrevival.sunnahassistant.views.adapters.Ayah
import com.thesunnahrevival.sunnahassistant.views.adapters.AyahTranslation
import com.thesunnahrevival.sunnahassistant.views.adapters.Line
import com.thesunnahrevival.sunnahassistant.views.adapters.Surah
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class AyahTranslationViewModel(application: Application) : AndroidViewModel(application) {

    val ayahs = listOf(
        Ayah(
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
            ),
            lines = listOf(Line(1, 174f, 30f, 1239f, 149f))
        ),
        Ayah(
            id = 2,
            number = 106,
            surah = Surah(17, "Suratul Isra"),
            arabicText = "وَقُرۡءَانٗا فَرَقۡنَٰهُ لِتَقۡرَأَهُۥ عَلَى ٱلنَّاسِ عَلَىٰ مُكۡثٖ وَنَزَّلۡنَٰهُ تَنزِيلٗا",
            ayahTranslations = listOf(
                AyahTranslation(
                    1,
                    "Sahih International",
                    "And [it is] a Qur’ān which We have separated [by intervals] that you might recite it to the people over a prolonged period. And We have sent it down progressively."
                ),
                AyahTranslation(
                    2,
                    "Mohsin Khan & Muhammad al-Hilali",
                    "And (it is) a Qur’ân which We have divided (into parts), in order that you might recite it to men at intervals. And We have revealed it by stages (in 23 years)."
                )
            ),
            lines = listOf(Line(2, 60f, 181f, 1234f, 281f))
        )
    )

    private val _translations = MutableStateFlow(
        listOf(
            AyahTranslation(1, "Sahih International", ""),
            AyahTranslation(2, "Mohsin Khan & Muhammad al-Hilali", ""),
            AyahTranslation(3, "Mufti Taqi Usmani", ""),
            AyahTranslation(4, "Dr. Mustafa Khattab, the Clear Quran", ""),
            AyahTranslation(5, "Abdul Haleem", "")
        )
    )

    private val _selectedTranslations = MutableStateFlow(_translations.value.subList(0, 2))


    val translations = _translations.asStateFlow()

    val selectedTranslations = _selectedTranslations.asStateFlow()

    fun toggleTranslationSelection(translation: AyahTranslation) {
        _selectedTranslations.update { currentTranslations ->
            if (currentTranslations.contains(translation)) {
                currentTranslations - translation
            } else {
                currentTranslations + translation
            }
        }
    }

    fun getAyah(ayahId: Int): Ayah? {
        return ayahs.find { it.id == ayahId }
    }
}