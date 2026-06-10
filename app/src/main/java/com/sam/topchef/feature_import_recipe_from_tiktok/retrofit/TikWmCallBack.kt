package com.sam.topchef.feature_import_recipe_from_tiktok.retrofit

import com.sam.topchef.feature_import_recipe_from_tiktok.model.TikTokData

interface TikWmCallBack {

    fun onTikWmSuccess(data: TikTokData)

    fun onTikWmFailure(error: String)

    fun onTikWmComplete()
}