package com.sam.topchef.feature_profile.adaper

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
import com.sam.topchef.core.utils.LoadImages
import com.sam.topchef.feature_import_from_tudogostoso.model.WebRecipeModel

class WebRecipeAdapter(val webRecipes: List<WebRecipeModel>) :
    RecyclerView.Adapter<WebRecipeAdapter.WebRecipeViewHolder>() {

    var onClick: ((recipeLinkPath: String) -> Unit)? = null
    var onLongClick: ((recipe: WebRecipeModel) -> Unit)? = null

    inner class WebRecipeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val image: ShapeableImageView = view.findViewById(R.id.img_recipe_post)
        val recipeName: TextView = view.findViewById(R.id.txt_title_post)
        val btnFavorite: ImageButton = view.findViewById(R.id.btn_favorite_post)

        init {
            Glide.with(itemView.context).load(R.drawable.tudo_gostoso).into(btnFavorite)
        }

        fun bind(item: WebRecipeModel) {
            LoadImages().loadImagesWithBlur(item.imageUrl, image)
            recipeName.text = item.title

            itemView.setOnClickListener { onClick?.invoke(item.recipeLinksPath) }
            itemView.setOnLongClickListener {
                onLongClick?.invoke(item)
                true
            }
        }

    }


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): WebRecipeViewHolder {
        val view =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.row_recipes_post_item, parent, false)
        return WebRecipeViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: WebRecipeViewHolder,
        position: Int
    ) {
        val item = webRecipes[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int {
        return webRecipes.size
    }


}