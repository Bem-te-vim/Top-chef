package com.sam.topchef.feature_import_recipe_from_tiktok.retrofit

import com.sam.topchef.feature_import_recipe_from_tiktok.model.TikTokData
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface TikWmApi {
    @GET(".")
    fun getTikTokData(@Query("url" ) url: String): Call<TikTokData>
}