package com.sam.topchef.feature_feed_main.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.sam.topchef.core.data.local.appDataBase.AppDataBase
import com.sam.topchef.feature_feed_main.data.model.PopularRecipe
import com.sam.topchef.feature_feed_main.data.model.RecipeCategory
import com.sam.topchef.feature_feed_main.data.model.RecipePost
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel(private val db: AppDataBase) : ViewModel() {

    val recipePosts: Flow<PagingData<RecipePost>> = Pager(
        config = PagingConfig(pageSize = 20, enablePlaceholders = false),
        pagingSourceFactory = { db.recipeDao().getAllRecipesPaged() }
    ).flow.map { pagingData ->
        pagingData.map { recipe ->
            RecipePost(
                recipe.id,
                recipe.title,
                recipe.imageUriString.firstOrNull(),
                recipe.isFavorite,
                recipe.reviews
            )
        }
    }.cachedIn(viewModelScope)

    private val _popularRecipes = MutableStateFlow<List<PopularRecipe>>(emptyList())
    val popularRecipes: StateFlow<List<PopularRecipe>> = _popularRecipes

    private val _categories = MutableStateFlow<List<RecipeCategory>>(emptyList())
    val categories: StateFlow<List<RecipeCategory>> = _categories

    init {
        loadHeaders()
    }

    fun loadHeaders() {
        viewModelScope.launch {
            val popular = withContext(Dispatchers.IO) {
                db.recipeDao().getPopularRecipes(10).map { recipe ->
                    PopularRecipe(
                        recipe.id,
                        recipe.chef ?: "Top Chef",
                        recipe.difficult,
                        recipe.preparationTime,
                        recipe.title,
                        recipe.imageUriString.firstOrNull(),
                        recipe.reviews,
                        recipe.isFavorite
                    )
                }
            }
            _popularRecipes.value = popular

            val categoryItems = withContext(Dispatchers.IO) {
                db.recipeDao().getAllCategoryNames().take(10).mapNotNull { type ->
                    val firstRecipe = db.recipeDao().getFirstRecipeByType(type)
                    firstRecipe?.let {
                        RecipeCategory(it.id, type, it.imageUriString.firstOrNull())
                    }
                }
            }
            _categories.value = categoryItems
        }
    }
}
