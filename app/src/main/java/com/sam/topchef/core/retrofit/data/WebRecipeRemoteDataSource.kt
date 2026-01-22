package com.sam.topchef.core.retrofit.data

import com.sam.topchef.core.retrofit.model.WebRecipePage
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class WebRecipeRemoteDataSource() {
    fun getAllRecipes(callBack: WebRecipeCallBack) {
        HTTPClient.retrofit()
            .create(WebRecipeApi::class.java)
            .getAllRecipes()
            .enqueue(object : Callback<WebRecipePage> {

                override fun onResponse(
                    call: Call<WebRecipePage?>,
                    response: Response<WebRecipePage?>
                ) {
                    if (response.isSuccessful) {
                        val webRecipePage = response.body() ?: throw RuntimeException()
                        callBack.onSuccess(webRecipePage)
                    } else {
                        val error = response.errorBody()?.string() ?: "unknown error"
                        callBack.onError(error)
                    }
                    callBack.onCompleted()
                }

                override fun onFailure(
                    call: Call<WebRecipePage?>,
                    t: Throwable
                ) {
                    val errorMessage = t.message ?: "Internal error"
                    callBack.onError(errorMessage)
                    callBack.onCompleted()
                }

            })
    }
}