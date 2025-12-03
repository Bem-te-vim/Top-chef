package com.sam.topchef.feature_recipe_detail.adapter

import android.annotation.SuppressLint
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sam.topchef.R
import com.sam.topchef.feature_recipe_detail.model.Step

class StepsAdapter(private val steps: List<Step>) :
    RecyclerView.Adapter<StepsAdapter.StepsViewHolder>() {

    private var checks: Int = -1

    inner class StepsViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val context = itemView.context
        val numberOfStep: TextView = view.findViewById(R.id.number_of_step)
        val txtStep: TextView = view.findViewById(R.id.step_description)
        val checkBoxStep: CheckBox = view.findViewById(R.id.checkBox_step_completed)


        @SuppressLint("NotifyDataSetChanged")
        fun bind(item: Step) {
            numberOfStep.text = context.getString(R.string.steps, adapterPosition + 1)

            txtStep.text = item.step

            if(adapterPosition < checks){
                txtStep.paintFlags = txtStep.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                numberOfStep.setTextColor(context.resources.getColor(R.color.default_color_app))
                checkBoxStep.isChecked = true
            }else{
                txtStep.paintFlags = txtStep.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                numberOfStep.setTextColor(context.resources.getColor(R.color.WhiteForTxt))
                checkBoxStep.isChecked = false
            }


            checkBoxStep.setOnClickListener {
                checks = if (checks == adapterPosition + 1) {
                    -1

                } else {
                    adapterPosition + 1
                }
                notifyDataSetChanged()
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): StepsViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_steps_detail_item, parent, false)

        return StepsViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: StepsViewHolder,
        position: Int
    ) {
        val step = steps[position]
        holder.bind(step)
    }

    override fun getItemCount(): Int = steps.size

}