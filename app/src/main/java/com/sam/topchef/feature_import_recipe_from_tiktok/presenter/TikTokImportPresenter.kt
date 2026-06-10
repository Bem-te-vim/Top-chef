package com.sam.topchef.feature_import_recipe_from_tiktok.presenter

import com.sam.topchef.feature_import_recipe_from_tiktok.model.TikTokData
import com.sam.topchef.feature_import_recipe_from_tiktok.retrofit.TikWmCallBack
import com.sam.topchef.feature_import_recipe_from_tiktok.retrofit.TikWmRemoteDataSource


class TikTokImportPresenter(
    private val uiCallBack: TikTokUICallBack,
    private val remoteDataSource: TikWmRemoteDataSource = TikWmRemoteDataSource()
) : TikWmCallBack {


    fun getTikTokData(url: String) {
        uiCallBack.showProgress()
        remoteDataSource.getTikTokData(url, this)
    }


    override fun onTikWmSuccess(data: TikTokData) {
        uiCallBack.showData(data)
    }


    override fun onTikWmFailure(error: String) {
        uiCallBack.showFailure(error)
    }

    override fun onTikWmComplete() {
        uiCallBack.hideProgress()
    }
}