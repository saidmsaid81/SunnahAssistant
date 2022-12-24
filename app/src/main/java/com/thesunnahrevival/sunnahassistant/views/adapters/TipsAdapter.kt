package com.thesunnahrevival.sunnahassistant.views.adapters

import android.content.res.Resources
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.thesunnahrevival.sunnahassistant.R
import com.thesunnahrevival.sunnahassistant.data.model.Tip
import com.thesunnahrevival.sunnahassistant.data.model.TipDiffCallBack
import com.thesunnahrevival.sunnahassistant.utilities.isValidUrl

class TipsAdapter(private val listener: TipsItemInteractionListener) :
    RecyclerView.Adapter<TipsAdapter.ViewHolder>() {
    private val tips: ArrayList<Tip> = arrayListOf()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.tip_card_view, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        tips.getOrNull(position)?.let {
            holder.bind(it)
        }

    }

    override fun getItemCount(): Int {
        return tips.size
    }

    fun setData(tips: List<Tip>) {
        val diffResult = DiffUtil.calculateDiff(TipDiffCallBack(this.tips, tips))
        this.tips.clear()
        this.tips.addAll(tips)
        diffResult.dispatchUpdatesTo(this)
    }

    inner class ViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        fun bind(tip: Tip) {
            view.findViewById<TextView>(R.id.tip_title).text = tip.title

            if (tip.icon != null) {
                try {
                    view.findViewById<ImageView>(R.id.icon)
                        .setImageDrawable(
                            ResourcesCompat.getDrawable(
                                view.resources,
                                tip.icon,
                                null
                            )
                        )
                } catch (exception: Resources.NotFoundException) {
                    Log.v("Not Found", "Resource not found")
                }
            }

            val setupTextView = view.findViewById<TextView>(R.id.setup)
            if (tip.launchFragment != null) {
                setupTextView.setOnClickListener {
                    listener.onSetupClickListener(tip.launchFragment, tip.toDoId)
                }
                setupTextView.visibility = View.VISIBLE
            } else {
                setupTextView.visibility = View.GONE
            }

            val infoView = view.findViewById<ImageView>(R.id.info)
            if (isValidUrl(tip.infoLink)) {
                infoView.setOnClickListener {
                    listener.onInfoClickListener(tip.infoLink)
                }
                infoView.visibility = View.VISIBLE
            } else {
                infoView.visibility = View.INVISIBLE
            }
        }
    }

    interface TipsItemInteractionListener {
        fun onSetupClickListener(launchFragment: Int, toDoId: Int?)
        fun onInfoClickListener(link: String)
    }

}