package com.example.trongtuyen.carmap.models.direction

import android.annotation.SuppressLint
import android.graphics.Color
import android.support.v4.view.MotionEventCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.example.trongtuyen.carmap.R
import com.google.android.gms.location.places.Place
import java.util.*
import com.example.trongtuyen.carmap.activity.MainActivity

class PlaceAdapter(private val myPlaceSet: ArrayList<SimplePlace>,private var mDragStartListener:OnStartDragListener):
        RecyclerView.Adapter<PlaceAdapter.ViewHolder>(), ItemTouchHelperAdapter {

    override fun onItemMove(fromPosition: Int, toPosition: Int): Boolean {
        Collections.swap(myPlaceSet, fromPosition, toPosition)
        notifyItemMoved(fromPosition, toPosition)
        return true
    }

    override fun onItemDismiss(position: Int) {
        myPlaceSet.removeAt(position)
        notifyItemRemoved(position)
    }

    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceAdapter.ViewHolder {
        // create a new view
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.place_adapter_layout, parent, false)

        // set the view's size, margins, paddings and layout parameters
//        ...
        return PlaceAdapter.ViewHolder(view)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val tvPlace=holder.view.findViewById<TextView>(R.id.tvPlace_place_adapter_layout)
        val btnRemove=holder.view.findViewById<ImageView>(R.id.imRemove_place_adapter_layout)
        val btnMove=holder.view.findViewById<ImageView>(R.id.imDrag_place_adapter_layout)

        tvPlace.text = myPlaceSet[position].name
        btnRemove.setOnClickListener{
            onItemDismiss(position)
        }
        btnMove.setOnTouchListener { _, event ->
            if (MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_DOWN) {
                mDragStartListener.onStartDrag(holder)
            }
            false
        }
    }

    override fun getItemCount()=myPlaceSet.size

//    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), ItemTouchHelperViewHolder {
//
//        val textView: TextView = itemView.findViewById<View>(R.id.text) as TextView
//        val handleView: ImageView = itemView.findViewById<View>(R.id.imDrag_place_adapter_layout) as ImageView
//
//        override fun onItemSelected() {
//            itemView.setBackgroundColor(Color.LTGRAY)
//        }
//
//        override fun onItemClear() {
//            itemView.setBackgroundColor(0)
//        }
//    }
}