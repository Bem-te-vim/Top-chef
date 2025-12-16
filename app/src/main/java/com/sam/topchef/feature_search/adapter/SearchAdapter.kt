package com.sam.topchef.feature_search.adapter

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
import com.bumptech.glide.Glide
import com.google.android.material.imageview.ShapeableImageView
import com.sam.topchef.R
import com.sam.topchef.core.data.model.Recipe

class SearchAdapter() :
    RecyclerView.Adapter<SearchAdapter.SearchViewHolder>() {

    var onItemClickListener: ((id: Int) -> Unit)? = null

    private val recipes = mutableListOf<Recipe>()

    @SuppressLint("NotifyDataSetChanged")
    fun submitList(list: List<Recipe>) {
        recipes.clear()
        recipes.addAll(list)
        notifyDataSetChanged()
    }

    inner class SearchViewHolder(view: View) :
        RecyclerView.ViewHolder(view) {
        private val context = itemView.context

        val imgRecipePost: ShapeableImageView = view.findViewById(R.id.img_recipe_post)
        val btnFavorite: ImageButton = view.findViewById(R.id.btn_favorite_post)
        val txtTitle: TextView = view.findViewById(R.id.txt_title_post)
        val txtReviews: TextView = view.findViewById(R.id.txt_reviews_post)

        fun bind(item: Recipe) {
            Glide.with(context)
                .load(item.imageUriString.firstOrNull())
                .placeholder(R.drawable.placeholder_item)
                .into(imgRecipePost)

            itemView.setOnClickListener {
                onItemClickListener?.invoke(item.id)
            }

            txtReviews.setOnClickListener {
                // TODO: add feature review
            }

            setButtonState(item.isFavorite, btnFavorite, context)

            btnFavorite.setOnClickListener {
                item.isFavorite = !item.isFavorite

                setButtonState(item.isFavorite, btnFavorite, context)

                //todo  notify dataBase
            }
            txtTitle.text = item.title
            txtReviews.text = context.getString(R.string.reviews, item.reviews)
        }

    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SearchViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_recipes_post_item, parent, false)

        return SearchViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: SearchViewHolder,
        position: Int
    ) {
        val recipe = recipes[position]
        holder.bind(recipe)
    }

    override fun getItemCount(): Int {
        return recipes.size
    }

    private fun setButtonState(isFavorite: Boolean, btnFavorite: ImageButton, context: Context) {
        if (isFavorite) btnFavorite.imageTintList = ColorStateList.valueOf(
            ContextCompat.getColor(context, R.color.default_color_app)
        ) else btnFavorite.imageTintList = ColorStateList.valueOf(
            ContextCompat.getColor(context, R.color.myGray)
        )

    }
}
