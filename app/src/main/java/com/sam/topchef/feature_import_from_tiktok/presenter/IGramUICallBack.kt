package com.sam.topchef.feature_import_from_tiktok.presenter

import com.sam.topchef.feature_import_from_tiktok.model.IgramMediaResponse
import com.sam.topchef.feature_import_from_tiktok.model.TikTokData

interface IGramUICallBack {

    fun showData(response: IgramMediaResponse)
    fun showFailure(message: String)
    fun showProgress()
    fun hideProgress()
}