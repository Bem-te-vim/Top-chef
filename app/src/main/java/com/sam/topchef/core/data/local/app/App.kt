package com.sam.topchef.core.data.local.app

import android.app.Application
import com.sam.topchef.core.data.local.appDataBase.AppDataBase
import com.sam.topchef.core.utils.TypeUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        cleanUpDuplicateTypes()
    }

    fun cleanUpDuplicateTypes() {
        CoroutineScope(Dispatchers.IO).launch {
            TypeUtils.cleanUpDuplicateTypes(db)
        }
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
