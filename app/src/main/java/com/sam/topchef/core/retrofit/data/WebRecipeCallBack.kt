package com.sam.topchef.core.retrofit.data

import com.sam.topchef.core.retrofit.model.WebRecipePage

interface WebRecipeCallBack {
    fun onCompleted()
    fun onSuccess(response: WebRecipePage)
    fun onError(message: String)
}