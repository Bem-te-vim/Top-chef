package com.sam.topchef.feature_import_from_tiktok.adapter

import android.annotation.SuppressLint
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sam.topchef.R
import com.sam.topchef.feature_import_from_tiktok.model.TiktokSection
import com.sam.topchef.feature_import_from_tiktok.model.TiktokStep

class TiktokStepsAdapter(private val steps: List<TiktokStep>) :
    RecyclerView.Adapter<TiktokStepsAdapter.TiktokStepsViewHolder>() {

    private var checks: Int = -1

    inner class TiktokStepsViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val context = itemView.context
        val numberOfStep: TextView = view.findViewById(R.id.number_of_step)
        val txtStep: TextView = view.findViewById(R.id.step_description)
        val checkBoxStep: CheckBox = view.findViewById(R.id.checkBox_step_completed)


        @SuppressLint("NotifyDataSetChanged")
        fun bind(item: TiktokStep) {
            numberOfStep.text = item.stepName

            txtStep.text = item.stepDesc

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
    ): TiktokStepsViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_steps_detail_item, parent, false)

        return TiktokStepsViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: TiktokStepsViewHolder,
        position: Int
    ) {
        val step = steps[position]
        holder.bind(step)
    }

    override fun getItemCount(): Int = steps.size

}