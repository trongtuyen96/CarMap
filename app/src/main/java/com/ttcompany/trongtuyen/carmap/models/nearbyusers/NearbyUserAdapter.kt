package com.ttcompany.trongtuyen.carmap.models.nearbyusers

import android.annotation.SuppressLint
import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.ttcompany.trongtuyen.carmap.R
import com.ttcompany.trongtuyen.carmap.models.User
import java.util.ArrayList

class NearbyUserAdapter(private val myNearbyUserSet: MutableList<User>) :
        RecyclerView.Adapter<NearbyUserAdapter.ViewHolder>() {

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder.
    // Each data item is just a string in this case that is shown in a TextView.
    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view)

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): NearbyUserAdapter.ViewHolder {
        // create a new view
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.nearby_user_chat_adapter_layout_new, parent, false)

        // set the view's size, margins, paddings and layout parameters
//        ...
        return NearbyUserAdapter.ViewHolder(view)
    }

    //    // Replace the contents of a view (invoked by the layout manager)
    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
//        holder.textView.text = myStepSet[position]

        val layoutNearbyUser = holder.view.findViewById<LinearLayout>(R.id.layoutUser_nearby_user_chat_adapter_layout)
        val imvTypeCar = holder.view.findViewById<ImageView>(R.id.imTypeCar_nearby_user_chat_adapter_layout)
        val tvName = holder.view.findViewById<TextView>(R.id.tvName_nearby_user_chat_adapter_layout)
        val tvTypeCar = holder.view.findViewById<TextView>(R.id.tvTypeCar_nearby_user_chat_adapter_layout)
        val imvColor = holder.view.findViewById<ImageView>(R.id.imColor_nearby_user_chat_adapter_layout_new)

        when (myNearbyUserSet[position].typeCar) {
            "xe con" -> {
                imvTypeCar.setImageResource(R.drawable.ic_marker_car)
            }
            "xe tai" -> {
                imvTypeCar.setImageResource(R.drawable.ic_marker_truck)
            }
            "xe khach" -> {
                imvTypeCar.setImageResource(R.drawable.ic_marker_bus)
            }
            "xe container" -> {
                imvTypeCar.setImageResource(R.drawable.ic_marker_container)
            }
            else -> imvTypeCar.setImageResource(R.drawable.ic_other_car)
        }
        tvName.text = myNearbyUserSet[position].name
        when (myNearbyUserSet[position].typeCar) {
            "xe con" -> {
                tvTypeCar.text = "Xe con"
            }
            "xe tai" -> {
                tvTypeCar.text = "Xe tải"
            }
            "xe khach" -> {
                tvTypeCar.text = "Xe khách"
            }
            "xe container" -> {
                tvTypeCar.text = "Xe container"
            }
        }

        if (myNearbyUserSet[position].colorCar != "") {
            imvColor.setBackgroundColor(Color.parseColor(myNearbyUserSet[position].colorCar))
        }

        layoutNearbyUser.setOnClickListener {
            onNearbyUserClick(position)
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = myNearbyUserSet.size

    private fun onNearbyUserClick(position : Int): Int{
        return position
    }

}