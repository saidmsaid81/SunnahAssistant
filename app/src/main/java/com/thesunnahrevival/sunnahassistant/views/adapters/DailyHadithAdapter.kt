package com.thesunnahrevival.sunnahassistant.views.adapters

import android.text.method.LinkMovementMethod
import android.text.method.MovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.text.HtmlCompat
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.thesunnahrevival.sunnahassistant.R
import com.thesunnahrevival.sunnahassistant.data.model.DailyHadith
import com.thesunnahrevival.sunnahassistant.utilities.generateDateText
import com.thesunnahrevival.sunnahassistant.utilities.getLocale
import java.util.Date
import java.util.GregorianCalendar

class DailyHadithAdapter: PagingDataAdapter<DailyHadith, DailyHadithAdapter.ViewHolder>(
    dailyHadithComparator) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DailyHadithAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.daily_hadith_view, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: DailyHadithAdapter.ViewHolder, position: Int) {
        getItem(position)?.let {
            holder.bind(it)
        }
    }

    inner class ViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        fun bind(hadith: DailyHadith) {
            val titleTextView = view.findViewById<TextView>(R.id.title)
            titleTextView.text = hadith.title
            titleTextView.setTextIsSelectable(true)

            view.findViewById<TextView>(R.id.published_date).text = generateDateText(
                GregorianCalendar(getLocale()).apply {
                    time = Date(hadith.pubDateMilliseconds)
                }
            )

            val contentTextView = view.findViewById<TextView>(R.id.content)
            contentTextView.text = HtmlCompat.fromHtml(hadith.content, HtmlCompat.FROM_HTML_MODE_LEGACY)
            contentTextView.movementMethod = LinkMovementMethod.getInstance()
            contentTextView.setTextIsSelectable(true)
        }
    }

    companion object {
        private val dailyHadithComparator = object : DiffUtil.ItemCallback<DailyHadith>() {
            override fun areItemsTheSame(oldItem: DailyHadith, newItem: DailyHadith): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: DailyHadith, newItem: DailyHadith): Boolean {
                return oldItem == newItem
            }
        }
    }
}