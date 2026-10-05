package com.sam.topchef.core.data.local.app

import android.app.Application
import com.google.firebase.Firebase
import com.google.firebase.appcheck.appCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.initialize
import com.sam.topchef.core.data.local.appDataBase.AppDataBase

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        Firebase.initialize(context = this)

        Firebase.appCheck.installAppCheckProviderFactory(
            DebugAppCheckProviderFactory.getInstance()
        )
    }


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

    val userDao by lazy {
        db.userDao()
    }

    val tiktokDao by lazy {
        db.tiktokDao()
    }

}
