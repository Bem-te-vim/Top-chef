package com.sam.topchef.feature_feed_main.adapter.holder

import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.sam.topchef.R
import com.sam.topchef.feature_feed_main.adapter.PopularRecipesAdapter
import com.sam.topchef.feature_feed_main.data.model.PopularRecipe

class PopularRecipesViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    private val context = itemView.context

    private lateinit var popularRecipesAdapter: PopularRecipesAdapter

    private val rvPopularRecipes: RecyclerView = view.findViewById(R.id.rv_horizontal)
    private val tvTitle: TextView = view.findViewById(R.id.tv_title)
    private val btnSeeAll: AppCompatButton = view.findViewById(R.id.btn_see_all)


    fun bind(
        onItemClickListener: ((Int) -> Unit)? = null,
        onWhatShowListener: ((String) -> Unit)? = null
    ) {
        tvTitle.text = context.getString(R.string.popular_recipes)

        btnSeeAll.setOnClickListener {
            onWhatShowListener?.invoke("AllPopularRecipes")
        }

        val fakeList = mutableListOf<PopularRecipe>()
        for (i in 0 until 10) {
            val fakeItem = PopularRecipe(
                i,
                "Sam Keller",
                "Easy",
                34,
                "test $i",
                reviews = 4.9
            )
            fakeList.add(fakeItem)
        }

        val pagerSnapHelper = PagerSnapHelper()
        pagerSnapHelper.attachToRecyclerView(rvPopularRecipes)
        rvPopularRecipes.layoutManager =
            LinearLayoutManager(itemView.context, LinearLayoutManager.HORIZONTAL, false)

        popularRecipesAdapter = PopularRecipesAdapter(fakeList) { id ->
            onItemClickListener?.invoke(id)
        }
        rvPopularRecipes.adapter = popularRecipesAdapter

    }

}