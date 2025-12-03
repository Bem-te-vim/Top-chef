package com.sam.topchef.feature_feed_main.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.imageview.ShapeableImageView
import com.sam.topchef.R
import com.sam.topchef.core.utils.LoadImages
import com.sam.topchef.feature_feed_main.adapter_interface.AdapterChanges
import com.sam.topchef.feature_feed_main.data.model.RecipePost

class RecipePostAdapter(val adapterChanges: AdapterChanges) :
    RecyclerView.Adapter<RecipePostAdapter.RecipePostViewHolder>(){

    private val recipePosts = mutableListOf<RecipePost>()

    @SuppressLint("NotifyDataSetChanged")
    fun setItems(items: List<RecipePost>) {
        recipePosts.clear()
        recipePosts.addAll(items)
        notifyDataSetChanged()
    }

    inner class RecipePostViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val context = itemView.context

        val imgRecipePost: ShapeableImageView = view.findViewById(R.id.img_recipe_post)
        val btnFavorite: ImageButton = view.findViewById(R.id.btn_favorite_post)
        val txtTitle: TextView = view.findViewById(R.id.txt_title_post)
        val txtReviews: TextView = view.findViewById(R.id.txt_reviews_post)

        fun bind(
            item: RecipePost,
        ) {
            LoadImages().loadImagesWithBlur(item.coverUrl, imgRecipePost)


            itemView.setOnClickListener {
                adapterChanges.onRecipeClicked(item.id)
            }

            itemView.setOnLongClickListener {
                adapterChanges.onRecipeTools(item.id)
                true
            }


            txtReviews.setOnClickListener {
                adapterChanges.onRecipeReview(item.id, item.reviews)
            }

            setButtonState(item.isFavorite, btnFavorite, context)

            btnFavorite.setOnClickListener {
                item.isFavorite = !item.isFavorite
                adapterChanges.onRecipeLiked(item.id, item.isFavorite)
            }

            txtTitle.text = item.title
            txtReviews.text = context.getString(R.string.reviews, item.reviews)
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): RecipePostViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_recipes_post_item, parent, false)
        return RecipePostViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: RecipePostViewHolder, position: Int
    ) {
        val item = recipePosts[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int {
        return recipePosts.size
    }


    fun onLikeNotify(id: Int, isFavorite: Boolean) {
        val index = recipePosts.indexOfFirst { it.id == id }

        if (index != -1) {
            recipePosts[index].isFavorite = isFavorite
            notifyItemChanged(index)
        }
    }

    fun onReviewNotify() {
        TODO("Not yet implemented")
    }


    fun onDeleteNotify(id: Int) {
        val index = recipePosts.indexOfFirst { it.id == id }

        if (index != -1) {
            recipePosts.removeAt(index)
            notifyItemRemoved(index)
        }
    }

     fun onEditNotify() {
        TODO("Not yet implemented")
    }

    fun setButtonState(isFavorite: Boolean, btnFavorite: ImageButton, context: Context) {
        if (isFavorite) btnFavorite.imageTintList = ColorStateList.valueOf(
            ContextCompat.getColor(context, R.color.default_color_app)
        ) else btnFavorite.imageTintList = ColorStateList.valueOf(
            ContextCompat.getColor(context, R.color.myGray)
        )

    }
}