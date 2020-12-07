package com.thiendn.trackme

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.thiendn.trackme.Constants.PREFS_NAME

@Suppress("UNCHECKED_CAST")
class SharePres {

    private val mSharedPreferences: SharedPreferences =
        BaseApplication.getInstance().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private var gson: Gson = Gson()

    operator fun <T> get(key: String, anonymousClass: Class<T>): T {
        return when (anonymousClass){
            String::class.java -> mSharedPreferences.getString(key, "") as T
            Boolean::class.java -> mSharedPreferences.getBoolean(key, false) as T
            Float::class.java -> mSharedPreferences.getFloat(key, 0f) as T
            Int::class.java -> mSharedPreferences.getInt(key, 0) as T
            else -> mSharedPreferences.getLong(key, 0) as T
        }
    }

    operator fun <T> get(key: String, anonymousClass: Class<T>, defValue: T): T {
        return when (anonymousClass){
            String::class.java -> mSharedPreferences.getString(key, defValue.toString()) as T
            Boolean::class.java -> mSharedPreferences.getBoolean(key, defValue as Boolean) as T
            Float::class.java -> mSharedPreferences.getFloat(key, defValue as Float) as T
            Int::class.java -> mSharedPreferences.getInt(key, defValue as Int) as T
            else -> mSharedPreferences.getLong(key, defValue as Long) as T
        }
    }

    fun <T> saveModelToJson(key: String, model: T){
        val editor = mSharedPreferences.edit()
        if(model != null) {
            val inString = gson.toJson(model)
            editor.putString(key, inString).apply()
        }else{
            editor.remove(key)
            editor.apply()
        }
    }

    fun <T> getModelFromJson(key: String, c: Class<T>): T?{
        if(mSharedPreferences.contains(key)) {
            val value = mSharedPreferences.getString(key, null)
            if (value != null) {
                return gson.fromJson(value, c)
            }
            return null
        }else{
            return null
        }
    }

    fun containsKey(key: String): Boolean{
        return mSharedPreferences.contains(key)
    }

    fun clear() {
        mSharedPreferences.edit().clear().apply()
    }

    companion object{
        const val PREFS_SEASON_ID = "prefs_season_id"
        const val PREFS_ROUTE = "prefs_route"

        private var instance: SharePres? = null

        @Synchronized
        fun getInstance(): SharePres? {
            if (instance == null) instance = SharePres()
            return instance
        }
    }

}