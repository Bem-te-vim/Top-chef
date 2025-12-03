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
import com.sam.topchef.feature_feed_main.data.model.PopularRecipe

class PopularRecipesAdapter(val adapterChanges: AdapterChanges) :
    RecyclerView.Adapter<PopularRecipesAdapter.PopularRecipesViewHolder>() {


    private val popularRecipes = mutableListOf<PopularRecipe>()

    /**
     *  with and the first time inserted items NotifyDataSetChanged will not affect the performance.
     *  it's necessary because the adapter must have started for set data
     *  else the application crash.
     *  */

    @SuppressLint("NotifyDataSetChanged")
    fun setItems(items: List<PopularRecipe>) {
        popularRecipes.clear()
        popularRecipes.addAll(items)
        notifyDataSetChanged()
    }

    inner class PopularRecipesViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val context = itemView.context

        val imgCover: ShapeableImageView = view.findViewById(R.id.img_popular_recipe)
        val txtReview: TextView = view.findViewById(R.id.txt_reviews)
        val btnFavorite: ImageButton = view.findViewById(R.id.btn_favorite_popular_recipe)
        val txtTitle: TextView = view.findViewById(R.id.txt_popularRecipe_title)
        val txtTimerAndDifficultAndChef: TextView = view.findViewById(R.id.txt_popularRecipe_info)

        fun bind(item: PopularRecipe) {
            LoadImages().loadImagesWithBlur(item.coverUrl, imgCover)

            txtReview.setOnClickListener {
                // TODO: add feature review
            }

            itemView.setOnClickListener { adapterChanges.onRecipeClicked(item.id) }
            itemView.setOnLongClickListener {
                adapterChanges.onRecipeTools(item.id)
                true
            }

            txtTitle.text = item.title

            txtReview.text = itemView.context.getString(R.string.reviews, item.reviews)

            val difficult = when (item.difficult) {
                1 -> context.getString(R.string.very_easy)
                2 -> context.getString(R.string.easy)
                3 -> context.getString(R.string.average)
                4 -> context.getString(R.string.hard)
                5 -> context.getString(R.string.very_hard)
                else -> "ERROR"
            }
            txtTimerAndDifficultAndChef.text = itemView.context.getString(
                R.string.recipe_info,
                timeFormater(item.time),
                difficult,
                item.chef
            )

            setButtonState(item.isFavorite, btnFavorite, context)

            btnFavorite.setOnClickListener {
                item.isFavorite = !item.isFavorite
                adapterChanges.onRecipeLiked(item.id, item.isFavorite)
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


    fun onLikeNotify(id: Int, isFavorite: Boolean) {
        val index = popularRecipes.indexOfFirst { it.id == id }

        if (index != -1) {
            popularRecipes[index].isFavorite = isFavorite
            notifyItemChanged(index)
        }
    }

    fun onReviewNotify() {
        TODO("Not yet implemented")
    }

    fun onDeleteNotify(id: Int) {
        val index = popularRecipes.indexOfFirst { it.id == id }

        if (index != -1) {
            popularRecipes.removeAt(index)
            notifyItemRemoved(index)
        }
    }

    fun onEditNotify() {
        TODO("Not yet implemented")
    }

    @SuppressLint("DefaultLocale")
    private fun timeFormater(totalMinutes: Int): String {
        val h = totalMinutes / 60
        val min = totalMinutes % 60

        return String.format("%dh:%02dmin", h, min)
    }

    fun setButtonState(isFavorite: Boolean, btnFavorite: ImageButton, context: Context) {
        if (isFavorite) btnFavorite.imageTintList = ColorStateList.valueOf(
            ContextCompat.getColor(context, R.color.default_color_app)
        ) else btnFavorite.imageTintList = ColorStateList.valueOf(
            ContextCompat.getColor(context, R.color.myGray)
        )

    }
}