package com.sam.topchef.feature_feed_main.adapter.holder

import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.sam.topchef.R
import com.sam.topchef.feature_feed_main.adapter.PopularRecipesAdapter
import com.sam.topchef.feature_feed_main.data.model.PopularRecipe

class PopularRecipesViewHolder(
    view: View,
) : RecyclerView.ViewHolder(view) {

    private val context = itemView.context

    private lateinit var popularRecipesAdapter: PopularRecipesAdapter

    private val rvPopularRecipes: RecyclerView = view.findViewById(R.id.rv_horizontal)
    private val tvTitle: TextView = view.findViewById(R.id.tv_title)
    private val btnSeeAll: AppCompatButton = view.findViewById(R.id.btn_see_all)

    init {
        val pagerSnapHelper = LinearSnapHelper()
        pagerSnapHelper.attachToRecyclerView(rvPopularRecipes)
    }

    fun bind(
        popularRecipes: List<PopularRecipe>,
        onItemClickListener: ((Int) -> Unit)? = null,
        onWhatShowListener: ((String) -> Unit)? = null
    ) {

        popularRecipesAdapter = PopularRecipesAdapter(popularRecipes) { id ->
            onItemClickListener?.invoke(id)
        }

        rvPopularRecipes.apply {
            layoutManager = LinearLayoutManager(itemView.context, LinearLayoutManager.HORIZONTAL, false)
            adapter = popularRecipesAdapter
            setHasFixedSize(true)
            isNestedScrollingEnabled = false
            setItemViewCacheSize(10)
        }

        if (popularRecipes.isEmpty()){
            tvTitle.visibility = View.GONE
            btnSeeAll.visibility = View.GONE
        }else{
            tvTitle.visibility = View.VISIBLE
            btnSeeAll.visibility = View.VISIBLE
        }

        tvTitle.text = context.getString(R.string.popular_recipes)

        btnSeeAll.setOnClickListener {
            onWhatShowListener?.invoke("AllPopularRecipes")
        }

    }

}