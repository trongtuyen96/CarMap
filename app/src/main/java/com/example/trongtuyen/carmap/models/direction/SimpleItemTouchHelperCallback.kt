package com.example.trongtuyen.carmap.models.direction

import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper

class SimpleItemTouchHelperCallback(private var mAdapter: ItemTouchHelperAdapter) : ItemTouchHelper.Callback() {
    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
//        mAdapter.onItemDismiss(viewHolder.adapterPosition)
    }

    override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder?): Int {
        // Set movement flags based on the layout manager
            return if (recyclerView.layoutManager is GridLayoutManager) {
                val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
                val swipeFlags = 0
                ItemTouchHelper.Callback.makeMovementFlags(dragFlags, swipeFlags)
            } else {
                val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN
                val swipeFlags = ItemTouchHelper.START or ItemTouchHelper.END
                ItemTouchHelper.Callback.makeMovementFlags(dragFlags, swipeFlags)
            }
    }

    override fun onMove(recyclerView: RecyclerView?, source: RecyclerView.ViewHolder?, target: RecyclerView.ViewHolder?): Boolean {
        if (target != null) {
            if (source != null) {
                if (source.itemViewType != target.itemViewType) {
                    return false
                }
            }
        }

        // Notify the adapter of the move
        if (source != null) {
            if (target != null) {
                mAdapter.onItemMove(source.adapterPosition, target.adapterPosition)
            }
        }
        return true
    }

    override fun isLongPressDragEnabled(): Boolean {
        return true
    }

    override fun isItemViewSwipeEnabled(): Boolean {
        return false
    }
}