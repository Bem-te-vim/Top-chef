package com.sam.topchef.feature_profile.adaper

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.imageview.ShapeableImageView
import com.sam.topchef.R
import com.sam.topchef.core.retrofit.model.WebRecipe
import com.sam.topchef.core.utils.LoadImages

class WebRecipeAdapter(val webRecipes: List<WebRecipe>) :
    RecyclerView.Adapter<WebRecipeAdapter.WebRecipeViewHolder>() {


    class WebRecipeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val image: ShapeableImageView = view.findViewById(R.id.web_image_item)
        val btnSave: ImageButton = view.findViewById(R.id.btn_save_web_recipe)
        val txtRecipeName: TextView = view.findViewById(R.id.txt_title_post)

        fun bind(item: WebRecipe) {
            LoadImages().loadImagesWithBlur(item.imageUrl, image)
            txtRecipeName.text = item.recipeName

            btnSave.setOnClickListener {  }
        }

    }


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): WebRecipeViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.row_web_recipe_item, parent, false)
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