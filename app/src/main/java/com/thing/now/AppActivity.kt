package com.thing.now

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.text.InputType
import android.widget.EditText
import android.widget.Toast

@SuppressLint("Registered")
open class AppActivity : AppCompatActivity() {

    private var initialized = false
    var isFirstTime = true
    var isDark = false
    var isFontSerif = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!initialized) {
            val preferences = getSharedPreferences(getString(R.string.settings_pref_file), Context.MODE_PRIVATE)
            isFirstTime = preferences.getBoolean(getString(R.string.settings_isFirstTime), true)
            isDark = preferences.getBoolean(getString(R.string.settings_isDark), true)
            isFontSerif = preferences.getBoolean(getString(R.string.settings_isFontSerif), true)
            initialized = true
        }
    }

    fun show(
        message: String,
        listener: DialogInterface.OnClickListener? = null,
        dismissListener: DialogInterface.OnDismissListener? = null
    ) {
        AlertDialog.Builder(this).setPositiveButton("Ok", listener)
            .setOnDismissListener(dismissListener)
            .setMessage(message).create().show()
    }

    fun toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    fun toggleTheme() {
        isDark = !isDark
        recreate()
    }

    fun toggleFont() {
        isFontSerif = !isFontSerif
        recreate()
    }

    override fun onPause() {
        super.onPause()
        val editor = getSharedPreferences(getString(R.string.settings_pref_file), Context.MODE_PRIVATE).edit()
        editor.putBoolean(getString(R.string.settings_isFirstTime), isFirstTime)
        editor.putBoolean(getString(R.string.settings_isDark), isDark)
        editor.putBoolean(getString(R.string.settings_isFontSerif), isFontSerif)
        editor.commit()
    }
}