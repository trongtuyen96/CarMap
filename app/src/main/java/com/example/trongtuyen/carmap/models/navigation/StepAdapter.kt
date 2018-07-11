package com.example.trongtuyen.carmap.models.navigation


import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.example.trongtuyen.carmap.R
import com.example.trongtuyen.carmap.R.id.textView
import com.example.trongtuyen.carmap.models.direction.Distance
import com.example.trongtuyen.carmap.models.direction.Step
import java.util.ArrayList

class StepAdapter(private val myStepSet: ArrayList<Step>) :
        RecyclerView.Adapter<StepAdapter.ViewHolder>() {

//    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder.
    // Each data item is just a string in this case that is shown in a TextView.
    class ViewHolder(val view: View ) : RecyclerView.ViewHolder(view)



    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): StepAdapter.ViewHolder {
        // create a new view
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.step_adapter_layout, parent, false)


        // set the view's size, margins, paddings and layout parameters
//        ...
        return StepAdapter.ViewHolder(view)
    }

//    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
//        holder.textView.text = myStepSet[position]

    val imInstruction = holder.view.findViewById<ImageView>(R.id.imInstruction_step_adapter)
    val tvInstruction=holder.view.findViewById<TextView>(R.id.tvInstruction_step_adapter)
    val tvDistance=holder.view.findViewById<TextView>(R.id.tvDistance_step_adapter)
    tvInstruction.text = myStepSet[position].instruction
    tvDistance.text = myStepSet[position].distance!!.text
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = myStepSet.size
}