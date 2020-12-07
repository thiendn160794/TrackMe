package com.thiendn.trackme

import android.app.Application

class BaseApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        private var instance: BaseApplication? = null

        fun getInstance() = instance!!
    }

}