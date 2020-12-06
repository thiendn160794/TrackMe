package com.thiendn.trackme

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat

object Extension {
    fun Context.hasPermissions(vararg ids: String): Boolean{
        for (id in ids){
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                return false
        }
        return true
    }

}