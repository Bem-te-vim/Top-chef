package com.sam.topchef.feature_import_recipe_from_tiktok.presenter

import com.sam.topchef.feature_import_recipe_from_tiktok.model.TikTokData

interface TikTokUICallBack {

    fun showData(response: TikTokData)
    fun showFailure(message: String)
    fun showProgress()
    fun hideProgress()
}