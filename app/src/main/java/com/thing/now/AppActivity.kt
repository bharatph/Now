package com.thing.now

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity

@SuppressLint("Registered")
open class AppActivity : AppCompatActivity() {

    private var initialized = false
    var isFirstTime = true
    var isDark = false
    var isFontSerif = true
    var userUid = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!initialized) {
            val preferences = getSharedPreferences(getString(R.string.settings_pref_file), Context.MODE_PRIVATE)
            isFirstTime = preferences.getBoolean(getString(R.string.settings_isFirstTime), true)
            isDark = preferences.getBoolean(getString(R.string.settings_isDark), true)
            isFontSerif = preferences.getBoolean(getString(R.string.settings_isFontSerif), true)
            userUid = preferences.getString(getString(R.string.settings_userUid), "")!!
            initialized = true
        }
    }

    fun toggleTheme() {
        isDark = !isDark
        recreate()
    }

    fun toggleFont() {
        isFontSerif = !isFontSerif
        recreate()
    }

    override fun onDestroy() {
        super.onDestroy()
        val editor = getSharedPreferences(getString(R.string.settings_pref_file), Context.MODE_PRIVATE).edit()
        editor.putBoolean(getString(R.string.settings_isFirstTime), isFirstTime)
        editor.putBoolean(getString(R.string.settings_isDark), isDark)
        editor.putBoolean(getString(R.string.settings_isFontSerif), isFontSerif)
        editor.putString(getString(R.string.settings_userUid), userUid)
        editor.apply()
    }
}