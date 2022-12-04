package com.thesunnahrevival.sunnahassistant.views.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.thesunnahrevival.sunnahassistant.R
import com.thesunnahrevival.sunnahassistant.data.model.ToDo

class MalformedToDosAdapter(private val listener: MalformedToDoInteractionListener) :
    RecyclerView.Adapter<MalformedToDosAdapter.ViewHolder>() {

    private val malformedToDos: ArrayList<ToDo> = arrayListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.malformed_to_do_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        malformedToDos.getOrNull(position)?.let {
            holder.bind(it)
        }
    }

    override fun getItemCount() = malformedToDos.size

    fun setData(toDos: List<ToDo>) {
        val diffResult =
            DiffUtil.calculateDiff(MalformedToDoDiffCallBack(this.malformedToDos, toDos))
        this.malformedToDos.clear()
        this.malformedToDos.addAll(toDos)
        diffResult.dispatchUpdatesTo(this)
    }


    inner class ViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        fun bind(toDo: ToDo) {
            view.findViewById<TextView>(R.id.to_do_title).text = toDo.name

            view.findViewById<TextView>(R.id.fix).setOnClickListener {
                listener.onFixClickListener(toDo)
            }

            view.findViewById<TextView>(R.id.delete_to_do).setOnClickListener {
                listener.onDeleteClickListener(toDo)
            }
        }
    }

    interface MalformedToDoInteractionListener {
        fun onFixClickListener(toDo: ToDo)
        fun onDeleteClickListener(toDo: ToDo)
    }
}

class MalformedToDoDiffCallBack(
    private val oldToDoList: List<ToDo>,
    private val newToDoList: List<ToDo>
) :
    DiffUtil.Callback() {
    override fun getOldListSize(): Int {
        return oldToDoList.size
    }

    override fun getNewListSize(): Int {
        return newToDoList.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldToDoList[oldItemPosition].id == newToDoList[newItemPosition].id
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldToDoList == newToDoList
    }

}