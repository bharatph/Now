package com.thing.now

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.support.v4.app.FragmentManager
import android.transition.Fade
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.thing.now.fragment.*
import com.thing.now.listener.OnSwitchListener
import com.thing.now.model.User
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper
import uk.co.chrisjenx.calligraphy.CalligraphyConfig
import java.util.*


class MainActivity : AppActivity(), OnSwitchListener {
    override fun onSwitch(id: Int) {
        supportFragmentManager.popBackStack(getString(R.string.main_tag), FragmentManager.POP_BACK_STACK_INCLUSIVE)
        supportFragmentManager.popBackStack(
            getString(R.string.bottom_sheet_tag),
            FragmentManager.POP_BACK_STACK_INCLUSIVE
        )
        when (id) {
            R.id.appInfoBtn -> {
                loadOnboarding()
            }
            else -> {
                loadUser()
            }
        }
    }

    companion object {
        const val TAG = "MainActivity"
    }

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase))
    }

    private fun loadOnboarding() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.mainFrameLayout, OnboardingFragment().apply {
                enterTransition = Fade(Fade.MODE_IN)
            })
            .addToBackStack(getString(R.string.main_tag))
            .commit()
    }

    private fun userOnboard() {
        loadOnboarding()
        //create User
        FirebaseAuth.getInstance().signInAnonymously()
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(MainActivity.TAG, "signInAnonymously:success")
                    NowHelper.createUser(
                        FirebaseAuth.getInstance().uid!!,
                        User(
                            FirebaseAuth.getInstance().uid!!,
                            getString(R.string.default_user_name),
                            "",
                            "",
                            null,
                            ArrayList(),
                            ArrayList()
                        )
                    ).addOnSuccessListener {
                        isFirstTime = false
                        loadUser()
                    }
                } else {
                    Log.w(MainActivity.TAG, "signInAnonymously:failure", task.exception)
                    show(getString(R.string.app_internet_error), DialogInterface.OnClickListener { _, _ ->
                        //                        load(false)
                        recreate()//FIXME rerun network operation
                    })
                }
            }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        CalligraphyConfig.initDefault(
            CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/GlacialIndifference-Regular.otf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        )
        setContentView(R.layout.activity_main)
        //info page available even while loading
        setFinishOnTouchOutside(true)
        if (isFirstTime) userOnboard() else loadUser()
    }


    private fun loadUser() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.mainFrameLayout, UserFragment().apply {
                enterTransition = Fade(Fade.MODE_IN)
            })
            .addToBackStack(getString(R.string.main_tag))
            .commit()
//        supportFragmentManager.beginTransaction()
//            .replace(R.id.bottomSheetFragmentContainer, NowFriendsFragment().apply {
//                enterTransition = Fade(Fade.MODE_IN)
//            })
//            .addToBackStack(getString(R.string.bottom_sheet_tag))
//            .commit()
    }
}
