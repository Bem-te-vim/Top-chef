package com.sam.topchef.feature_import_recipe_from_tiktok.retrofit

import com.sam.topchef.feature_import_recipe_from_tiktok.model.TikTokData
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TikWmRemoteDataSource {

    fun getTikTokData(url: String, callback: TikWmCallBack) {
        HTTPClient.retrofit().create(TikWmApi::class.java).getTikTokData(url)
            .enqueue(object : Callback<TikTokData> {

                override fun onResponse(
                    call: Call<TikTokData?>, response: Response<TikTokData?>
                ) {
                    if (response.isSuccessful) {
                        val data = response.body()
                        if (data != null) {
                            callback.onTikWmSuccess(data)
                        } else {
                            callback.onTikWmFailure("Response body is null")
                        }
                    } else {
                        val errorBody = response.errorBody()?.string()
                        callback.onTikWmFailure(errorBody ?: "Unknown error")
                    }

                    callback.onTikWmComplete()
                }

                override fun onFailure(
                    call: Call<TikTokData?>, t: Throwable
                ) {
                    callback.onTikWmFailure(t.message ?: "Unknown error")
                    callback.onTikWmComplete()
                }


            })
    }
}
