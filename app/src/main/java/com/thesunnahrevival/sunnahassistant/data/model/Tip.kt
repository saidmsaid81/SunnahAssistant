package com.thesunnahrevival.sunnahassistant.data.model

import androidx.recyclerview.widget.DiffUtil

data class Tip(
    val id: Int,
    val title: String,
    val icon: Int?,
    val infoLink: String = "",
    val launchFragment: Int? = null,
    val toDoId: Int? = null
)

class TipDiffCallBack(private val oldTipsList: List<Tip>, private val newTipsList: List<Tip>) :
    DiffUtil.Callback() {
    override fun getOldListSize(): Int {
        return oldTipsList.size
    }

    override fun getNewListSize(): Int {
        return newTipsList.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldTipsList[oldItemPosition].id == newTipsList[newItemPosition].id
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldTipsList == newTipsList
    }

}