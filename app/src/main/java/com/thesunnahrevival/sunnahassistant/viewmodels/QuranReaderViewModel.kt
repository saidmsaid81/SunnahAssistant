package com.thesunnahrevival.sunnahassistant.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.thesunnahrevival.sunnahassistant.views.adapters.Ayah
import com.thesunnahrevival.sunnahassistant.views.adapters.Line
import com.thesunnahrevival.sunnahassistant.views.adapters.QuranPage

class QuranReaderViewModel(application: Application) : AndroidViewModel(application) {

    fun getQuranPages(pageNumbers: List<Int>): List<QuranPage> {
        val quranPages = mutableListOf<QuranPage>()
        pageNumbers.forEach {
            quranPages.add(
                QuranPage(
                    it,
                    listOf(
                        Ayah(id = 1, number = 1, lines = listOf(Line(1, 174f, 30f, 1239f, 149f))),
                        Ayah(id = 2, number = 2, lines = listOf(Line(2, 60f, 181f, 1234f, 281f))),
                        Ayah(
                            id = 3,
                            number = 3, lines = listOf(
                                Line(3, 73f, 299f, 1235f, 418f),
                                Line(3, 701f, 440f, 1237f, 554f)
                            )
                        )
                    )
                )
            )
        }
        return quranPages
    }
}