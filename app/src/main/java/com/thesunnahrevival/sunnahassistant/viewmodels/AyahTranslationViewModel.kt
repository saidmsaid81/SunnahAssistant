package com.thesunnahrevival.sunnahassistant.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.thesunnahrevival.sunnahassistant.data.QuranRepository
import com.thesunnahrevival.sunnahassistant.data.model.FullAyahDetails
import com.thesunnahrevival.sunnahassistant.data.model.Translation
import com.thesunnahrevival.sunnahassistant.views.adapters.Ayah
import com.thesunnahrevival.sunnahassistant.views.adapters.AyahTranslation
import com.thesunnahrevival.sunnahassistant.views.adapters.Line
import com.thesunnahrevival.sunnahassistant.views.adapters.Surah
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

open class AyahTranslationViewModel(application: Application) : AndroidViewModel(application) {

    private val mQuranRepository = QuranRepository.getInstance(getApplication())

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


    fun toggleTranslationSelection(translation: Translation) {
        translation.selected = !translation.selected
        viewModelScope.launch(Dispatchers.IO) {
            mQuranRepository.updateTranslation(translation)
        }
    }

    private val _selectedAyah = MutableStateFlow<FullAyahDetails?>(null)
    val selectedAyah = _selectedAyah.asStateFlow()
    val translations = mQuranRepository.getTranslations()

    val selectedTranslations = translations.map { translationsList ->
        translationsList.filter { it.selected }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun setSelectedAyah(ayah: FullAyahDetails) {
        _selectedAyah.update { ayah }
    }

    suspend fun getAyahById(ayahId: Int) = mQuranRepository.getFullAyahDetailsById(ayahId)

}