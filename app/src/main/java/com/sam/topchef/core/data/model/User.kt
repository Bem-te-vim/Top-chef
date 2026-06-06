package com.sam.topchef.core.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class User(

    @ColumnInfo(name = "id")
    @PrimaryKey()
    val id: Int = 1,

    @ColumnInfo(name = "name")
    val name: String? = null,

    @ColumnInfo(name = "image_uri")
    val imageUri: String? = null
)

