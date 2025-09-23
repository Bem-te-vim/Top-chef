package com.sam.topchef.core.data.local.app

import android.app.Application
import com.sam.topchef.core.data.local.appDataBase.AppDataBase

class App: Application() {
    lateinit var db: AppDataBase
    override fun onCreate() {
        super.onCreate()
        db = AppDataBase.getDataBase(this)
    }
}