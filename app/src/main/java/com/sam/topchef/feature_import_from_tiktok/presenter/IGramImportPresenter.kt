package com.sam.topchef.feature_import_from_tiktok.presenter

import com.sam.topchef.feature_import_from_tiktok.model.IgramMediaResponse
import com.sam.topchef.feature_import_from_tiktok.model.TikTokData
import com.sam.topchef.feature_import_from_tiktok.retrofit.IGramApi
import com.sam.topchef.feature_import_from_tiktok.retrofit.IGramCallBack
import com.sam.topchef.feature_import_from_tiktok.retrofit.IGramRemoteDataSource
import com.sam.topchef.feature_import_from_tiktok.retrofit.TikWmCallBack
import com.sam.topchef.feature_import_from_tiktok.retrofit.TikWmRemoteDataSource


class IGramImportPresenter(
    private val uiCallBack: IGramUICallBack,
    private val remoteDataSource: IGramRemoteDataSource = IGramRemoteDataSource()
) : IGramCallBack {


    fun getIGramData(url: String) {
        uiCallBack.showProgress()
        remoteDataSource.getInstagramMedia(url, this)
    }

    override fun iGramOnSuccess(data: IgramMediaResponse) {
        uiCallBack.showData(data)
    }

    override fun iGramOnFailure(message: String) {
        uiCallBack.showFailure(message)
    }

    override fun iGramOnComplete() {
        uiCallBack.hideProgress()
    }
}