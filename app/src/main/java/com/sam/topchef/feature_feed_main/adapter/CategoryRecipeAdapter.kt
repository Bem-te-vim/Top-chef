package com.sam.topchef.feature_feed_main.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.imageview.ShapeableImageView
import com.sam.topchef.R
import com.sam.topchef.core.utils.LoadImages
import com.sam.topchef.feature_feed_main.data.model.RecipeCategory

class CategoryRecipeAdapter() :
    RecyclerView.Adapter<CategoryRecipeAdapter.CategoryRecipeViewHolder>() {

    private val categories = mutableListOf<RecipeCategory>()

    /**
     *  with and the first time inserted items NotifyDataSetChanged will not affect the performance.
     *  it's necessary because the adapter must have started for set data
     *  else the application crash.
     *  */

    @SuppressLint("NotifyDataSetChanged")
    fun setItems(items: List<RecipeCategory>) {
        categories.clear()
        categories.addAll(items)
        notifyDataSetChanged()
    }

    val onCategoryClick: ((category: String) -> Unit)? = null

    inner class CategoryRecipeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvCategory: TextView = view.findViewById(R.id.tv_category)
        val imgCategory: ShapeableImageView = view.findViewById(R.id.img_category)

        fun bind(item: RecipeCategory) {
            LoadImages().loadImagesWithBlur(item.coverUrl, imgCategory)

            tvCategory.text = item.type

            itemView.setOnClickListener {
                onCategoryClick?.invoke(item.type)
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


    fun onDeleteNotify(id: Int) {
        val index = categories.indexOfFirst { it.id == id }

        if (index != -1) {
            categories.removeAt(index)
            notifyItemRemoved(index)
        }
    }
}