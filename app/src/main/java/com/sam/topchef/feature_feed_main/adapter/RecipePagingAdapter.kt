package com.sam.topchef.feature_feed_main.adapter

import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.imageview.ShapeableImageView
import com.sam.topchef.R
import com.sam.topchef.core.utils.LoadImages
import com.sam.topchef.feature_feed_main.adapter_interface.AdapterChanges
import com.sam.topchef.feature_feed_main.data.model.RecipePost

class RecipePagingAdapter(private val adapterChanges: AdapterChanges) :
    PagingDataAdapter<RecipePost, RecipePagingAdapter.RecipeViewHolder>(RecipeDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_recipes_post_item, parent, false)
        return RecipeViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        getItem(position)?.let { holder.bind(it) }
    }

    inner class RecipeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val context = itemView.context
        private val imgRecipePost: ShapeableImageView = view.findViewById(R.id.img_recipe_post)
        private val btnFavorite: ImageButton = view.findViewById(R.id.btn_favorite_post)
        private val txtTitle: TextView = view.findViewById(R.id.txt_title_post)

        fun bind(item: RecipePost) {
            LoadImages().loadImagesWithBlur(item.coverUrl, imgRecipePost)
            txtTitle.text = item.title

            setButtonState(item.isFavorite, btnFavorite, context)

            btnFavorite.setOnClickListener {
                item.isFavorite = !item.isFavorite
                adapterChanges.onRecipeLiked(item.id, item.isFavorite, item.isTikTok)
                setButtonState(item.isFavorite, btnFavorite, context)
            }

            itemView.setOnClickListener {
                if (item.isTikTok) {
                    adapterChanges.onTikTokRecipeClicked(item.id)
                } else {
                    adapterChanges.onRecipeClicked(item.id)
                }
            }

            itemView.setOnLongClickListener {
                adapterChanges.onRecipeTools(item.id, item.isTikTok)
                true
            }
        }

        private fun setButtonState(isFavorite: Boolean, btnFavorite: ImageButton, context: Context) {
            if (isFavorite) btnFavorite.imageTintList = ColorStateList.valueOf(
                ContextCompat.getColor(context, R.color.default_color_app)
            ) else btnFavorite.imageTintList = ColorStateList.valueOf(
                ContextCompat.getColor(context, R.color.myGray)
            )
        }
    }

    class RecipeDiffCallback : DiffUtil.ItemCallback<RecipePost>() {
        override fun areItemsTheSame(oldItem: RecipePost, newItem: RecipePost): Boolean {
            return oldItem.id == newItem.id && oldItem.isTikTok == newItem.isTikTok
        }

        override fun areContentsTheSame(oldItem: RecipePost, newItem: RecipePost): Boolean {
            return oldItem == newItem
        }
    }
}
