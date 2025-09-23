package com.sam.topchef.feature_feed_main.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.imageview.ShapeableImageView
import com.sam.topchef.R
import com.sam.topchef.feature_feed_main.data.model.PopularRecipe

class PopularRecipesAdapter(private val popularRecipes: List<PopularRecipe>,
    val popularRecipeClick: (Int) -> Unit
) :
    RecyclerView.Adapter<PopularRecipesAdapter.PopularRecipesViewHolder>() {


    inner class PopularRecipesViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgCover: ShapeableImageView = view.findViewById(R.id.img_popular_recipe)
        val txtReview: TextView = view.findViewById(R.id.txt_reviews)
        val btnFavorite: ImageButton = view.findViewById(R.id.btn_favorite_popular_recipe)
        val txtTitle: TextView = view.findViewById(R.id.txt_popularRecipe_title)
        val txtTimerAndDifficultAndChef: TextView = view.findViewById(R.id.txt_popularRecipe_info)

        fun bind(item: PopularRecipe) {
            imgCover //Todo
            imgCover.setOnClickListener { popularRecipeClick(item.id) }

            txtTitle.text = item.title

            txtReview.text = itemView.context.getString(R.string.reviews, item.reviews)

            txtTimerAndDifficultAndChef.text = itemView.context.getString(
                R.string.recipe_info,
                item.time,
                "min",
                item.difficult,
                item.chef
            )

            btnFavorite.setOnClickListener {
                //todo
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PopularRecipesViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_popular_recipe_item, parent, false)
        return PopularRecipesViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: PopularRecipesViewHolder,
        position: Int
    ) {
        val item = popularRecipes[position]
        holder.bind(item)
    }


    override fun getItemCount(): Int = popularRecipes.size


}