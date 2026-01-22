package com.sam.topchef.core.retrofit.presentation

import com.sam.topchef.core.retrofit.data.WebRecipeCallBack
import com.sam.topchef.core.retrofit.data.WebRecipeRemoteDataSource
import com.sam.topchef.core.retrofit.model.WebRecipePage

class WebRecipePresenter(
    val callBack: WebRecipeCallBack,
    val dataSource: WebRecipeRemoteDataSource = WebRecipeRemoteDataSource()
) : WebRecipeCallBack {

    fun getWebRecipePage() {
        callBack.showProgress()
        dataSource.getAllRecipes(this)
    }

    override fun onCompleted() {
      callBack.hideProgress()
    }

    override fun onSuccess(response: WebRecipePage) {
      callBack.showWebRecipePage(response)
    }

    override fun onError(message: String) {
        callBack.showFailure(message)
    }

    interface WebRecipeCallBack {
        fun showWebRecipePage(response: WebRecipePage)
        fun showFailure(message: String)
        fun showProgress()
        fun hideProgress()
    }


}