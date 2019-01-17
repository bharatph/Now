package com.thing.now

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.transition.Fade
import android.view.View
import com.thing.now.fragment.AcknowledgementsFragment
import com.thing.now.fragment.NowFriendsFragment
import com.thing.now.fragment.OnboardingFragment
import com.thing.now.fragment.UserFragment
import com.thing.now.helper.NowHelper
import com.thing.now.model.User
import kotlinx.android.synthetic.main.activity_main.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import uk.co.chrisjenx.calligraphy.CalligraphyConfig
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper

class MainActivity : BaseActivity(), View.OnClickListener, UserFragment.OnUserFragmentInteractionListener {


    companion object {
        const val TAG = "MainActivity"
        const val PROGRESS_SHOW = 0
        const val PROGRESS_HIDE = 1
    }

    override fun onUserFragmentInteraction(i: Int) {
        when (i) {
            R.id.appInfoBtn -> {
                showInfo()
            }
            PROGRESS_SHOW -> {
                showLoading(true)
            }
            PROGRESS_HIDE -> {
                showLoading(false)
            }
        }
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.onboardingRootView -> {
                showUser()
            }
        }
    }

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase))
    }

    private fun showInfo() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.mainFrameLayout, OnboardingFragment().apply {
                enterTransition = Fade(Fade.MODE_IN)
            }).commit()
        supportFragmentManager.beginTransaction()
            .replace(R.id.bottomFrameLayout, AcknowledgementsFragment().apply {
                enterTransition = Fade(Fade.MODE_IN)
            }).commit()
    }

    private fun showUser() {
        //user showLoading
        supportFragmentManager.beginTransaction()
            .replace(R.id.mainFrameLayout, UserFragment().apply {
                enterTransition = Fade(Fade.MODE_IN)
            })
            .commit()
        //friends showLoading
        supportFragmentManager.beginTransaction()
            .replace(R.id.bottomFrameLayout, NowFriendsFragment().apply {
                enterTransition = Fade(Fade.MODE_IN)
            })
            .commit()
    }


    fun showLoading(shouldLoad: Boolean = true) {
        progressBar.isIndeterminate = shouldLoad
        progressBar.visibility = if (shouldLoad) View.VISIBLE else View.GONE
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
        showLoading(true)
        if (isFirstTime) {
            showInfo()
            //create User
            NowHelper.generateUserId()
        } else {
            NowHelper.loadExistingUser()
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onUserLogin(event: OnUserLoginEvent) {
        showLoading(false)
        if (event.uid.isNullOrEmpty()) {
            AlertDialog.Builder(this).setMessage(R.string.app_internet_error).setOnCancelListener {
                recreate() //FIXME rerun network operation
            }.create().show()
        } else {
            NowHelper.createUser(User(event.uid!!, getString(R.string.default_user_name)))
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onUserCreate(event: OnUserCreateEvent) {
        if (event.user == null) {
            //todo handle error
            return
        }
        isFirstTime = false
        //new user is now existing user
        recreate()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onUserLoad(event: OnUserLoadEvent) {
        showLoading(false)
        if (event.user == null) { //checking user in global scope
            AlertDialog.Builder(this).setMessage(R.string.app_fatal_error).setOnCancelListener {
                supportFragmentManager?.popBackStack()
            }.create().show()
        } else {
            showUser()
            EventBus.getDefault().unregister(this)
        }
    }

    data class OnUserLoginEvent(var uid: String?)
    data class OnUserCreateEvent(var user: User?)
    data class OnUserUpdateEvent(var user: User?)
    data class OnUserLoadEvent(var user: User?)
}
