package com.sam.topchef.feature_add_recipe.adapter

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.imageview.ShapeableImageView
import com.sam.topchef.R

class RecipeDifficultAdapter(
    private var difficultyLevel: Int = 1,
) : RecyclerView.Adapter<RecipeDifficultAdapter.RecipeDifficultViewHolder>() {

    inner class RecipeDifficultViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val context = itemView.context
        val ic: ShapeableImageView = view.findViewById(R.id.img_ic_difficult)
        @SuppressLint("NotifyDataSetChanged")
        fun bind(position: Int) {
            if (position < difficultyLevel) {
                    ic.imageTintList = ColorStateList.valueOf(
                        ContextCompat.getColor(context, R.color.default_color_app)
                    )
            } else {
                ic.imageTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(context, R.color.myGray)
                )
            }

            // clique para atualizar o nível
            ic.setOnClickListener {
                difficultyLevel = if (difficultyLevel == position + 1) {
                    // se clicar no mesmo nível -> mantém só ele
                    1
                } else {
                    // se clicar em outro -> pinta até ele
                    position + 1
                }
                notifyDataSetChanged()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeDifficultViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.row_difficult_item, parent, false)
        return RecipeDifficultViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecipeDifficultViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int = 5

    @SuppressLint("NotifyDataSetChanged")
    fun setDifficultyLevel(level: Int) {
        difficultyLevel = level.coerceIn(1, 5)
        notifyDataSetChanged()
    }

    fun getDifficultyLevel(): Int = difficultyLevel
}