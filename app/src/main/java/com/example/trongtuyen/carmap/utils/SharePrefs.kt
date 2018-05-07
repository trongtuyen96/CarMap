package com.example.trongtuyen.carmap.utils

import android.content.Context
import android.util.Log

/**
 * Created by tuyen on 07/05/2018.
 */

///// Check this if error happens, should build this class in Java class
class SharePrefs
private constructor() {
    fun GetInt(key: String): Int {
        val sharedPref = mContext.getSharedPreferences(FileName, Context.MODE_PRIVATE)
        return sharedPref.getInt(key, 0)
    }

    fun SetInt(key: String, value: Int) {
        val sharedPref = mContext.getSharedPreferences(FileName, Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putInt(key, value)
        editor.commit()
    }

    fun GetFloat(key: String): Float {
        val sharedPref = mContext.getSharedPreferences(FileName, Context.MODE_PRIVATE)
        return sharedPref.getFloat(key, 0f)
    }

    fun SetFloat(key: String, value: Int) {
        val sharedPref = mContext.getSharedPreferences(FileName, Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putFloat(key, value.toFloat())
        editor.commit()
    }

    fun GetString(key: String): String {
        val sharedPref = mContext.getSharedPreferences(FileName, Context.MODE_PRIVATE)
        return sharedPref.getString(key, "")
    }

    fun SetString(key: String, value: String) {
        val sharedPref = mContext.getSharedPreferences(FileName, Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putString(key, value)
        editor.commit()
    }

    companion object {
        internal var TAG = SharePrefs::class.java.name
        internal lateinit var mInstance: SharePrefs
        internal lateinit var mContext: Context
        internal var FileName = "SharePrefs"

        fun Initialize(context: Context) {
            mContext = context
            mInstance = SharePrefs()
        }

        val instance: SharePrefs?
            get() {
                if (mInstance == null) {
                    Log.e(TAG, "You have to invoke Initialize(context) first!")
                    return null
                }
                return mInstance
            }
    }
}