package com.sam.topchef.core.utils

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sam.topchef.feature_import_from_tiktok.model.TiktokSection
import com.sam.topchef.feature_import_from_tiktok.model.TiktokStep

object TiktokConverter {

    @TypeConverter
    fun fromSectionList(value: List<TiktokSection>): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toSectionList(value: String): List<TiktokSection> {
        val listType = object : TypeToken<List<TiktokSection>>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun fromStepList(value: List<TiktokStep>): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toStepList(value: String): List<TiktokStep> {
        val listType = object : TypeToken<List<TiktokStep>>() {}.type
        return Gson().fromJson(value, listType)
    }
}
