package com.sam.topchef.core.retrofit.model

import com.google.gson.annotations.SerializedName

data class WebRecipe(
    @SerializedName("receita")
    val recipeName: String,

    @SerializedName("ingredientes")
    val ingredients: String,

    @SerializedName("modo_preparo")
    val preparationMode: String,

    @SerializedName("link_imagem")
    val imageUrl: String,

    @SerializedName("tipo")
    val type: String
)