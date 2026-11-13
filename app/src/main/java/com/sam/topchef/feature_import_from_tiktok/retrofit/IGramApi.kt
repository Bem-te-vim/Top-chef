package com.sam.topchef.feature_import_from_tiktok.retrofit

import com.sam.topchef.feature_import_from_tiktok.model.IgramMediaResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface IGramApi {
    @GET("v1/media")
    fun getMedia(@Query("url") url: String): Call<IgramMediaResponse>

}