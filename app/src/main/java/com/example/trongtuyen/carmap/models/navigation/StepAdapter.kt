package com.example.trongtuyen.carmap.models.navigation

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.example.trongtuyen.carmap.R
import com.example.trongtuyen.carmap.models.direction.ItemTouchHelperAdapter
import com.example.trongtuyen.carmap.models.direction.Step
import java.util.*

class StepAdapter(private val myStepSet: ArrayList<Step>) :
        RecyclerView.Adapter<StepAdapter.ViewHolder>() {

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
    when (myStepSet[position].maneuver) {
        "ferry" -> {
            imInstruction.setImageResource(R.drawable.ferry)
        }
        "ferry-train" -> {
            imInstruction.setImageResource(R.drawable.ferry_train)
        }
        "fork-left" -> {
            imInstruction.setImageResource(R.drawable.fork_left)
        }
        "fork-right" -> {
            imInstruction.setImageResource(R.drawable.fork_right)
        }
        "keep-left" -> {
            imInstruction.setImageResource(R.drawable.keep_left)
        }
        "keep-right" -> {
            imInstruction.setImageResource(R.drawable.keep_right)
        }
        "merge" -> {
            imInstruction.setImageResource(R.drawable.merge)
        }
        "ramp-left" -> {
            imInstruction.setImageResource(R.drawable.ramp_left)
        }
        "ramp-right" -> {
            imInstruction.setImageResource(R.drawable.ramp_right)
        }
        "roundabout-left" -> {
            imInstruction.setImageResource(R.drawable.roundabout_left)
        }
        "roundabout-right" -> {
            imInstruction.setImageResource(R.drawable.roundabout_right)
        }
        "straight" -> {
            imInstruction.setImageResource(R.drawable.straight)
        }
        "turn-left" -> {
            imInstruction.setImageResource(R.drawable.turn_left)
        }
        "turn-right" -> {
            imInstruction.setImageResource(R.drawable.turn_right)
        }
        "turn-sharp-left" -> {
            imInstruction.setImageResource(R.drawable.turn_sharp_left)
        }
        "turn-sharp-right" -> {
            imInstruction.setImageResource(R.drawable.turn_sharp_right)
        }
        "turn-slight-left" -> {
            imInstruction.setImageResource(R.drawable.turn_slight_left)
        }
        "turn-slight-right" -> {
            imInstruction.setImageResource(R.drawable.turn_slight_right)
        }
        "uturn-left" -> {
            imInstruction.setImageResource(R.drawable.uturn_left)
        }
        "uturn-right" -> {
            imInstruction.setImageResource(R.drawable.uturn_right)
        }
    }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = myStepSet.size
}