package com.sam.topchef.feature_feed_main.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sam.topchef.R
import com.sam.topchef.feature_feed_main.adapter.holder.CategoriesRecipeViewHolder
import com.sam.topchef.feature_feed_main.adapter.holder.PopularRecipesViewHolder
import com.sam.topchef.feature_feed_main.adapter.holder.RecipePostViewHolder
import com.sam.topchef.feature_feed_main.data.model.MainFeedItem

class FeedMainAdapter() :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_CATEGORY_RECIPE = 0
        private const val VIEW_TYPE_POPULAR_RECIPE = 1
        private const val VIEW_TYPE_RECIPES_POST = 3
    }

    var onItemClickListener: ((Int) -> Unit)? = null
    var onWhatShowListener: ((String) -> Unit)? = null

    private val items= mutableListOf<MainFeedItem>()
    fun setItems(newItems: List<MainFeedItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is MainFeedItem.Categories -> VIEW_TYPE_CATEGORY_RECIPE
            is MainFeedItem.PopularRecipes -> VIEW_TYPE_POPULAR_RECIPE
            is MainFeedItem.RecipePost -> VIEW_TYPE_RECIPES_POST
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_CATEGORY_RECIPE -> CategoriesRecipeViewHolder(
                inflater.inflate(
                    R.layout.layout_item_rv_horizontal,
                    parent,
                    false
                )
            )

            VIEW_TYPE_POPULAR_RECIPE -> PopularRecipesViewHolder(
                inflater.inflate(
                    R.layout.layout_item_rv_horizontal,
                    parent,
                    false
                )
            )

            VIEW_TYPE_RECIPES_POST -> RecipePostViewHolder(
                inflater.inflate(
                    R.layout.row_recipes_post_item,
                    parent,
                    false
                )
            )

            else -> throw IllegalArgumentException("ViewType Invalid")
        }
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int
    ) {
        when (val item = items[position]) {
            is MainFeedItem.Categories -> (holder as CategoriesRecipeViewHolder).bind(
                item.categories,
                onWhatShowListener
            )

            is MainFeedItem.PopularRecipes -> (holder as PopularRecipesViewHolder).bind(
                item.popularRecipes,
                onItemClickListener,
                onWhatShowListener
            )

            is MainFeedItem.RecipePost -> (holder as RecipePostViewHolder).bind(
                item,
                onItemClickListener
            )
        }
    }

    override fun getItemCount(): Int = items.size
}