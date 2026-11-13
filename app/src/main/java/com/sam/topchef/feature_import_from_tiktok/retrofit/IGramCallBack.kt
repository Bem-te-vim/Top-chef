package com.sam.topchef.feature_import_from_tiktok.retrofit

import android.os.Message
import com.sam.topchef.feature_import_from_tiktok.model.IgramMediaResponse

interface IGramCallBack {
    fun iGramOnSuccess(data: IgramMediaResponse)
    fun iGramOnFailure(message: String)
    fun iGramOnComplete()
}