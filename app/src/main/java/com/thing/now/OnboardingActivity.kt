package com.thing.now

import android.content.Context
import android.content.DialogInterface
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.thing.now.model.User
import kotlinx.android.synthetic.main.activity_onboarding.*
import uk.co.chrisjenx.calligraphy.CalligraphyConfig
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper

class OnboardingActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        CalligraphyConfig.initDefault(
            CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/GlacialIndifference-Regular.otf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        )
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)
        onboard_exit.setOnClickListener {
            finish()
        }
    }
}
