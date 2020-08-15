package com.thesunnahrevival.sunnahassistant.views.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.thesunnahrevival.sunnahassistant.R
import java.util.*

class CategoriesSettingsAdapter(private val categoriesList: TreeSet<String>, private val deleteListener: DeleteCategoryListener) : RecyclerView.Adapter<CategoriesSettingsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.category_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return categoriesList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position)
    }

    inner class ViewHolder(val view : View) :RecyclerView.ViewHolder(view) {
        fun bind(position :Int) {
            view.findViewById<LinearLayout>(R.id.root).visibility = View.VISIBLE
            view.findViewById<TextView>(R.id.category_name).text = categoriesList.elementAt(position)
            val deleteButton = view.findViewById<ImageView>(R.id.delete_button)
            deleteButton.setOnClickListener { deleteListener.deleteReminderCategory(categoriesList, categoriesList.elementAt(position)) }
        }
    }

    interface DeleteCategoryListener {
       fun deleteReminderCategory(categoriesList: TreeSet<String>, category:String)
    }

}