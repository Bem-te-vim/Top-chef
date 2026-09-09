package com.sam.topchef.feature_feed_main.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.sam.topchef.R
import com.sam.topchef.feature_feed_main.adapter_interface.AdapterChanges
import com.sam.topchef.feature_feed_main.data.model.PopularRecipe

class PopularHeaderAdapter(
    private val adapterChanges: AdapterChanges,
    private val onSeeAllClick: () -> Unit
) : RecyclerView.Adapter<PopularHeaderAdapter.ViewHolder>() {

    private var items = emptyList<PopularRecipe>()
    private val popularRecipesAdapter = PopularRecipesAdapter(adapterChanges)

    fun setItems(newItems: List<PopularRecipe>) {
        items = newItems
        notifyDataSetChanged()
    }

    fun onLikeNotify(id: Int, isFavorite: Boolean, isTikTok: Boolean = false) {
        popularRecipesAdapter.onLikeNotify(id, isFavorite, isTikTok)
    }

    fun onDeleteNotify(id: Int, isTikTok: Boolean = false) {
        popularRecipesAdapter.onDeleteNotify(id, isTikTok)
    }

    fun onEditNotify() {
        popularRecipesAdapter.onEditNotify()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_rv_horizontal_popular_recipes, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items)
    }

    override fun getItemCount() = if (items.isNotEmpty()) 1 else 0

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val rv = view.findViewById<RecyclerView>(R.id.rv_popular_recipes)
        private val btnSeeAll = view.findViewById<View>(R.id.btn_see_all_popular_recipes)

        init {
            rv.layoutManager = LinearLayoutManager(view.context, LinearLayoutManager.HORIZONTAL, false)
            rv.adapter = popularRecipesAdapter
            val pagerSnapHelper = LinearSnapHelper()
            pagerSnapHelper.attachToRecyclerView(rv)
            btnSeeAll.setOnClickListener { onSeeAllClick() }
        }

        fun bind(items: List<PopularRecipe>) {
            popularRecipesAdapter.setItems(items)
        }
    }
}
