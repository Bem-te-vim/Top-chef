package com.sam.topchef.core.retrofit.data

import com.sam.topchef.core.retrofit.model.WebRecipePage
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface WebRecipeApi {
    @GET("receitas/todas")
    fun getAllRecipes(@Query("page" ) page: Int = 1,
                      @Query("limit") limit: Int = 93): Call<WebRecipePage>
}