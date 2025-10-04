package com.sam.topchef.feature_feed_main.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.imageview.ShapeableImageView
import com.sam.topchef.R
import com.sam.topchef.feature_feed_main.data.model.RecipeCategory

class CategoryRecipeAdapter(
    private val categories: List<RecipeCategory>,
    val onCategoryClick: (RecipeCategory) -> Unit
) :
    RecyclerView.Adapter<CategoryRecipeAdapter.CategoryRecipeViewHolder>() {

    inner class CategoryRecipeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvCategory: TextView = view.findViewById(R.id.tv_category)
        val imgCategory: ShapeableImageView = view.findViewById(R.id.img_category)

        fun bind(item: RecipeCategory) {


            Glide.with(itemView.context)
                .load(item.coverUrl)
                .placeholder(R.drawable.placeholder_item)
                .into(imgCategory)

            tvCategory.text = item.type

            itemView.setOnClickListener {
                onCategoryClick(item)
            }
        }

    }


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CategoryRecipeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_categories_recipe_item, parent, false)

        return CategoryRecipeViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: CategoryRecipeViewHolder,
        position: Int
    ) {
        val item = categories[position]
        holder.bind(item)

    }

    override fun getItemCount(): Int = categories.size


}