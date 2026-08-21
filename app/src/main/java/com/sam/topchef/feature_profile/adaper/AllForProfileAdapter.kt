package com.sam.topchef.feature_profile.adaper

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.imageview.ShapeableImageView
import com.sam.topchef.R
import com.sam.topchef.core.utils.LoadImages
import com.sam.topchef.feature_feed_main.data.model.RecipePost

class AllForProfileAdapter(@param:LayoutRes private val layout: Int = R.layout.row_images) :
    RecyclerView.Adapter<AllForProfileAdapter.AllForProfileViewHolder>() {

    var itemClick: ((id: Int, isTikTok: Boolean) -> Unit)? = null
    var itemLongClick: ((id: Int, isTikTok: Boolean) -> Unit)? = null

    private val recipes = mutableListOf<RecipePost>()

    @SuppressLint("NotifyDataSetChanged")
    fun submitList(list: List<RecipePost>) {
        recipes.clear()
        recipes.addAll(list)
        notifyDataSetChanged()
    }

    inner class AllForProfileViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val imageview: ShapeableImageView = view.findViewById(R.id.image_item)
        fun bind(item: RecipePost) {
            LoadImages().loadImagesWithBlur(item.coverUrl, imageview)

            itemView.setOnClickListener { itemClick?.invoke(item.id, item.isTikTok) }
            itemView.setOnLongClickListener {
                itemLongClick?.invoke(item.id, item.isTikTok)
                true
            }
        }

    }


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AllForProfileViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(layout, parent, false)
        return AllForProfileViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: AllForProfileViewHolder,
        position: Int
    ) {
        val item = recipes[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int = recipes.size


}