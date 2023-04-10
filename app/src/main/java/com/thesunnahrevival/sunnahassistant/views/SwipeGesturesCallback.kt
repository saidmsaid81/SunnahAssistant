package com.thesunnahrevival.sunnahassistant.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.LEFT
import androidx.recyclerview.widget.ItemTouchHelper.RIGHT
import androidx.recyclerview.widget.RecyclerView
import com.thesunnahrevival.sunnahassistant.R
import com.thesunnahrevival.sunnahassistant.views.adapters.ToDoListAdapter

class SwipeGesturesCallback(context: Context) :
    ItemTouchHelper.SimpleCallback(0, LEFT or RIGHT) {

    private val deleteBackground = ColorDrawable(Color.RED)
    private val markAsCompleteBackground =
        ColorDrawable(
            ContextCompat.getColor(
                context,
                R.color.colorAccent
            )
        )
    private val deleteIcon =
        ContextCompat.getDrawable(context, R.drawable.ic_delete)
    private val markAsCompleteIcon =
        ContextCompat.getDrawable(context, R.drawable.ic_mark_as_complete)

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val adapter = viewHolder.bindingAdapter as ToDoListAdapter
        val adapterPosition = viewHolder.bindingAdapterPosition
        if (adapterPosition in 0 until adapter.itemCount) {
            when (direction) {
                LEFT -> {
                    adapter.deleteToDo(adapterPosition)
                }
                RIGHT -> {
                    adapter.markAsComplete(adapterPosition)
                }
            }
        }

    }

    override fun onChildDraw(
        canvas: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        val icon = if (dX > 0) markAsCompleteIcon else deleteIcon
        val itemView = viewHolder.itemView
        val backgroundCornerOffset = 20 //so background is behind the rounded corners of itemView
        val iconMargin = (itemView.height - icon!!.intrinsicHeight) / 2
        val iconTop = itemView.top + (itemView.height - icon.intrinsicHeight) / 2
        val iconBottom = iconTop + icon.intrinsicHeight
        when {
            dX > 0 -> { // Swiping to the right
                val iconLeft = itemView.left + iconMargin + icon.intrinsicWidth
                val iconRight = itemView.left + iconMargin
                icon.setBounds(iconRight, iconTop, iconLeft, iconBottom)
                markAsCompleteBackground.setBounds(
                    itemView.left, itemView.top,
                    itemView.left + dX.toInt() + backgroundCornerOffset, itemView.bottom
                )
                markAsCompleteBackground.draw(canvas)
            }
            dX < 0 -> { // Swiping to the left
                val iconLeft = itemView.right - iconMargin - icon.intrinsicWidth
                val iconRight = itemView.right - iconMargin
                icon.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                deleteBackground.setBounds(
                    itemView.right + dX.toInt() - backgroundCornerOffset,
                    itemView.top, itemView.right, itemView.bottom
                )
                deleteBackground.draw(canvas)
            }
            else -> { // view is unSwiped

            }
        }

        icon.draw(canvas)
        super.onChildDraw(canvas, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }

}