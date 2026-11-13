package com.sam.topchef.feature_import_from_tiktok.retrofit

import com.sam.topchef.feature_import_from_tiktok.model.IgramMediaResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class IGramRemoteDataSource {
    fun getInstagramMedia(url: String, callBack: IGramCallBack){

        HTTPClient.retrofitForInstagram().create(IGramApi::class.java).getMedia(url)
            .enqueue(object : Callback<IgramMediaResponse>{
                override fun onResponse(
                    call: Call<IgramMediaResponse?>,
                    response: Response<IgramMediaResponse?>
                ) {
                   if (response.isSuccessful){
                       val data = response.body()
                       if (data != null){
                           callBack.iGramOnSuccess(data)
                       }else{
                           callBack.iGramOnFailure("Response body is null")
                       }
                   }else {
                       val errorBody = response.errorBody()?.string()
                       callBack.iGramOnFailure(errorBody ?: "Unknown error")
                   }
                    callBack.iGramOnComplete()
                }

                override fun onFailure(
                    call: Call<IgramMediaResponse?>,
                    t: Throwable
                ) {
                    callBack.iGramOnFailure(t.message ?: "Unknown error")
                    callBack.iGramOnComplete()
                }

            })


    }
}