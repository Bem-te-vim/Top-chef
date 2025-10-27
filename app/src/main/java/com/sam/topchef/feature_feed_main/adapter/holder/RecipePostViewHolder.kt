package com.sam.topchef.feature_feed_main.adapter.holder

import android.content.res.ColorStateList
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.imageview.ShapeableImageView
import com.sam.topchef.R
import com.sam.topchef.feature_feed_main.data.model.MainFeedItem

class RecipePostViewHolder(
    val onLongClick: ((position: Int, id: Int) -> Boolean)? = null, view: View
) :
    RecyclerView.ViewHolder(view) {
    private val context = itemView.context

    val imgRecipePost: ShapeableImageView = view.findViewById(R.id.img_recipe_post)
    val btnFavorite: ImageButton = view.findViewById(R.id.btn_favorite_post)
    val txtTitle: TextView = view.findViewById(R.id.txt_title_post)
    val txtReviews: TextView = view.findViewById(R.id.txt_reviews_post)

    fun bind(
        item: MainFeedItem.RecipePost,
        onItemClickListener: ((Int) -> Unit)? = null,
        onLikeClickListener: ((id: Int, isLiked: Boolean) -> Unit)? = null
    ) {
        Glide.with(context).load(item.coverUrl).placeholder(R.drawable.placeholder_item)
            .into(imgRecipePost)

        itemView.setOnClickListener {
            onItemClickListener?.invoke(item.id)
        }

        itemView.setOnLongClickListener {
            onLongClick?.invoke(adapterPosition, item.id)
            true
        }


        txtReviews.setOnClickListener {
            // TODO: add feature review
        }

        if (item.isFavorite) btnFavorite.imageTintList = ColorStateList.valueOf(
            ContextCompat.getColor(context, R.color.default_color_app)
        ) else btnFavorite.imageTintList = ColorStateList.valueOf(
            ContextCompat.getColor(context, R.color.myGray)
        )

        btnFavorite.setOnClickListener {
            item.isFavorite = !item.isFavorite

            if (item.isFavorite) {
                btnFavorite.imageTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(context, R.color.default_color_app)
                )
            } else {
                btnFavorite.imageTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(context, R.color.myGray)
                )
            }

            onLikeClickListener?.invoke(item.id, item.isFavorite)
        }
        txtTitle.text = item.title
        txtReviews.text = context.getString(R.string.reviews, item.reviews)
    }
}