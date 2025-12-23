package com.sam.topchef.feature_profile.adaper

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.imageview.ShapeableImageView
import com.sam.topchef.R
import com.sam.topchef.core.data.model.Recipe
import com.sam.topchef.core.utils.LoadImages

class AllForProfileAdapter(@LayoutRes private val layout: Int = R.layout.row_images) :
    RecyclerView.Adapter<AllForProfileAdapter.AllForProfileViewHolder>() {

    var itemClick: ((id: Int) -> Unit)? = null
    var itemLongClick: ((id: Int) -> Unit)? = null

    private val recipes = mutableListOf<Recipe>()

    @SuppressLint("NotifyDataSetChanged")
    fun submitList(list: List<Recipe>) {
        recipes.clear()
        recipes.addAll(list)
        notifyDataSetChanged()
    }

    inner class AllForProfileViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val imageview: ShapeableImageView = view.findViewById(R.id.image_item)
        fun bind(item: Recipe) {
            LoadImages().loadImagesWithBlur(item.imageUriString.firstOrNull(), imageview)

            itemView.setOnClickListener { itemClick?.invoke(item.id) }
            itemView.setOnLongClickListener {
                itemLongClick?.invoke(item.id)
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