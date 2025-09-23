package com.sam.topchef.feature_feed_main.adapter.holder

import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.imageview.ShapeableImageView
import com.sam.topchef.R
import com.sam.topchef.feature_feed_main.data.model.MainFeedItem

class RecipePostViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    private val context = itemView.context

    val imgRecipe: ShapeableImageView = view.findViewById(R.id.img_popular_recipe)
    val btnFavorite: ImageButton = view.findViewById(R.id.btn_favorite_popular_recipe)
    val txtTitle: TextView = view.findViewById(R.id.txt_popularRecipe_title)
    val txtReviews: TextView = view.findViewById(R.id.txt_popularRecipe_reviews)

    fun bind(item: MainFeedItem.RecipePost, onItemClickListener: ((Int) -> Unit)? = null ) {
        itemView.setOnClickListener {
       onItemClickListener?.invoke(item.id)
        }

        imgRecipe //todo implement Glide API
        btnFavorite //todo
        txtTitle.text = item.title
        txtReviews.text = context.getString(R.string.reviews, item.reviews)
    }
}