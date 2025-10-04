package com.sam.topchef.core.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Type(
    @PrimaryKey
    @ColumnInfo(name = "type")
    val type: String
)
