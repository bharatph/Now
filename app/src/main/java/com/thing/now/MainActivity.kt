package com.thing.now

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.text.InputType
import android.transition.Fade
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import com.google.firebase.auth.FirebaseAuth
import com.thing.now.adapter.NowFriendsAdapter
import com.thing.now.fragment.*
import com.thing.now.model.User
import kotlinx.android.synthetic.main.header.*
import kotlinx.android.synthetic.main.fragment_now_friends.*
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper
import com.thing.now.model.Task
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.circle_timer.*
import uk.co.chrisjenx.calligraphy.CalligraphyConfig
import java.util.*
import kotlin.concurrent.timerTask


class MainActivity : AppActivity(), View.OnClickListener {

    companion object {
        const val TAG = "MainActivity"
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.appInfoBtn -> {
                loadOnboarding()
            }
            R.id.userName -> {
                val editText = EditText(this).apply {
                    inputType = InputType.TYPE_CLASS_TEXT
                }
                AlertDialog.Builder(this)
                    .setTitle("Change User name")
                    .setPositiveButton("Ok") { _, _ ->
                        val str = editText.text.toString()
                        if (str.isEmpty()) {
                            editText.error = "Invalid user name"
                            return@setPositiveButton
                        }
                        NowHelper.user?.name = str
                        NowHelper.updateUser().addOnSuccessListener {
                            toast("User name updated successfully")
                            userName.text = str
                        }.addOnFailureListener {
                            toast("User name failed to update")
                        }
                    }
                    .setNegativeButton("Cancel") { _, _ ->

                    }
                    .setView(editText).show()
            }
            R.id.circleTimer -> {
                if (NowHelper.timeElapsed() == null) startTask() else endTask()
            }
        }
    }

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase))
    }


    private var timer: Timer? = null
    private fun startTimer(elapsed: Long = 0) {
        var i = elapsed
        timer = Timer().apply {
            scheduleAtFixedRate(timerTask {
                Handler(mainLooper).post {
                    nowTimer.text = "${++i / 60} : ${i % 60}"
                }
            }, 0, 1000)
        }
    }

    private fun stopTimer() {
        timer?.cancel()
    }


    fun startTask() {
        load(true)
        val editText = EditText(this).apply {
            inputType = InputType.TYPE_CLASS_TEXT
            hint = context.getString(R.string.task_name_hint)
        }
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.task_start))
            .setPositiveButton(getString(R.string.start_timer)) { _, _ ->
                var str = editText.text.toString()
                if (str.isEmpty()) {
                    editText.error = getString(R.string.task_invalid)
                    str = getString(R.string.task_default_name)
                }
                val callback = NowHelper.startTask(str)
                if (callback == null) {
                    toast(getString(R.string.task_unknown_error))
                    load(false)
                    return@setPositiveButton
                }
                callback.addOnSuccessListener {
                    //                    load(false)
                    resumeTask()
                }.addOnFailureListener {
                    //                    load(false)
                    toast("starting task failed")
                }
            }
            .setView(editText).show()
    }

    private fun resumeTask() {
        val task = NowHelper.user?.task ?: return
        startTimer((Date().time - task.startedOn.time).div(1000))
        userStatus.text = task.name
    }


    private fun endTask() {
        AlertDialog.Builder(this).setTitle(getString(R.string.confirm_task_end)).setPositiveButton("Ok") { _, _ ->
            val callback = NowHelper.endTask()
            if (callback == null) {
                toast(getString(R.string.task_stop_error))
                return@setPositiveButton
            }
            callback.addOnSuccessListener {
                stopTimer()
                nowTimer.text = getString(R.string.start_timer)
                userStatus.text = getString(R.string.user_status_idle)

            }.addOnFailureListener {
                toast(getString(R.string.task_network_error))
            }
        }.show()
    }

    private fun settleTasks() {
        val elapsedTime = NowHelper.timeElapsed()
        if (elapsedTime != null) {
            resumeTask()
        }
    }

    private fun settleUser(user: User) {
        //dp
//        if (user.dp.isNotBlank()) {
//            Picasso.get().load(user.dp)
//                .error(R.drawable.ic_user)
//                .placeholder(R.drawable.ic_user)
//        }
        //name
        userName.text = if (user.name.isEmpty()) getString(R.string.default_user_name) else user.name
        userStatus.text = if (user.status.isEmpty()) getString(R.string.user_status_idle) else user.status
        load(false)
        settleTasks()
    }

    private fun loadOnboarding() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.onboardingFragment, OnboardingFragment().apply {
                enterTransition = Fade(Fade.MODE_IN)
            })
            .addToBackStack(getString(R.string.onboarding_fragment_tag))
            .commit()
        supportFragmentManager.beginTransaction()
            .replace(R.id.bottomFrameLayout, AcknowledgementsFragment().apply {
                enterTransition = Fade(Fade.MODE_IN)
            })
            .addToBackStack(getString(R.string.onboarding_fragment_tag)) //FIXME double bach button bug
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
        appInfoBtn.setOnClickListener(this)
        setFinishOnTouchOutside(true)
        load()
        if (isFirstTime) userOnboard() else loadUser()
    }

    fun load(shouldLoadUser: Boolean = true) {
//        progressBar.isIndeterminate = shouldLoadUser
//        progressBar.visibility = if (shouldLoadUser) View.VISIBLE else View.GONE

        if (shouldLoadUser) {
            userStatus.text = getString(R.string.loading_main)
        } else {
            userStatus.text = NowHelper.user?.task?.name ?: getString(R.string.task_default_name)
        }
    }

    private fun loadUser() {
        var status = NowHelper.loadUser(FirebaseAuth.getInstance().currentUser!!.uid)
            ?.addOnSuccessListener {
                val user = it.toObject(User::class.java)
                if (user == null) {
                    load()
                    show(getString(R.string.app_fatal_error),
                        DialogInterface.OnClickListener { dialog, which -> finish() })
                    return@addOnSuccessListener
                }
                settleUser(NowHelper.user!!)
                settleFriends()

                //register clicks
                userName.setOnClickListener(this)
                circleTimer.setOnClickListener(this)
            }?.addOnFailureListener {
                load()
                show(getString(R.string.app_unknown_error))
            }
        if (status == null) {
            load()
            show(getString(R.string.app_network_unreachable_error),
                DialogInterface.OnClickListener { _, _ -> finish() })
        }
    }

    private fun settleFriends() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.bottomFrameLayout, NowFriendsFragment().apply {
                enterTransition = Fade(Fade.MODE_IN)
            }).commit()
    }
}
