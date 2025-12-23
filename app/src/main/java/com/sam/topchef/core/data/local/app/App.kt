package com.sam.topchef.core.data.local.app

import android.app.Application
import com.sam.topchef.core.data.local.appDataBase.AppDataBase

class App: Application() {
    val db: AppDataBase by lazy {
        AppDataBase.getDataBase(this)
    }

    val cartDao by lazy {
        db.cartDao()
    }

    val recipeDao by lazy {
        db.recipeDao()
    }

    val typeDao by lazy {
        db.typeDao()
    }
}