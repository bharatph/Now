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
import com.thing.now.model.User
import kotlinx.android.synthetic.main.header.*
import kotlinx.android.synthetic.main.now_friends_fragment.*
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper
import com.thing.now.fragment.InviteFragment
import com.thing.now.fragment.OnboardingFragment
import com.thing.now.fragment.UserAddFragment
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.circle_timer.*
import uk.co.chrisjenx.calligraphy.CalligraphyConfig
import java.util.*
import kotlin.concurrent.timerTask


class MainActivity : AppActivity(), View.OnClickListener,
    User.OnUserAddListener, UserAddFragment.OnUserResolveListener {

    override fun onUserResolve(enableBtn: Boolean) {
        if (enableBtn) {
            settleFriends()
        }
    }

    override fun onUserAdd(user: User) {
        toast(getString(R.string.invite_link_user_added))
        nowFriendsRecyclerView.adapter?.notifyDataSetChanged()
    }

    companion object {
        const val TAG = "MainActivity"
    }

    override fun onClick(v: View?) {
        if (supportFragmentManager.fragments.size > 0) {
            supportFragmentManager.popBackStack()
            return
        }
        when (v!!.id) {
            R.id.sendInviteBtn -> {
                supportFragmentManager.beginTransaction()
                    .add(R.id.fragmentOptionContainer, InviteFragment())
                    .addToBackStack(getString(R.string.friends_menu_tag))
                    .commit()
            }
            R.id.addFriendBtn -> {
                supportFragmentManager.beginTransaction()
                    .add(R.id.fragmentOptionContainer, UserAddFragment())
                    .addToBackStack(getString(R.string.friends_menu_tag))
                    .commit()
            }
            R.id.sortBtn -> {
                //TODO
            }
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
                settleTasks()
            }
        }
    }

    private fun settleTasks() {
        var elapsed = NowHelper.timeElapsed()
        if (elapsed == null) {
            //no timer running start timer
            startTask()
        } else {
            //timer is running stop it
            startTask(false)
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


    fun startTask(b: Boolean = true) {
        if (b) {
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
                        return@setPositiveButton
                    }
                    callback.addOnSuccessListener {
                        startTimer((Date().time - NowHelper.user!!.task!!.startedOn.time).div(1000))
                        userStatus.text = str
                    }.addOnFailureListener {
                        toast("starting task failed")
                    }
                }
                .setView(editText).show()
        } else {
            val callback = NowHelper.endTask()
            if (callback == null) {
                toast(getString(R.string.task_stop_error))
                return
            }
            callback.addOnSuccessListener {
                stopTimer()
                nowTimer.text = getString(R.string.start_timer)
                userStatus.text = getString(R.string.user_status_idle)

            }.addOnFailureListener {
                toast(getString(R.string.task_network_error))
            }
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
        settleTasks()
        load(false)
    }

    private fun settleFriends() {
        loadFriends()
        NowHelper.friendList {
            loadFriends(false)
            if (it == null) {
                emptyList.visibility = View.VISIBLE
                return@friendList
            }
            emptyList.visibility = View.GONE
            nowFriendsRecyclerView.adapter = NowFriendsAdapter(this, it)
        }
        nowFriendsRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayout.VERTICAL, false)
    }

    private fun loadOnboarding() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.onboardingFragment, OnboardingFragment().apply {
                enterTransition = Fade(Fade.MODE_IN)
            })
            .addToBackStack(getString(R.string.onboarding_fragment_tag))
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
                        load(false)
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
        progressBar.isIndeterminate = shouldLoadUser
        progressBar.visibility = if (shouldLoadUser) View.VISIBLE else View.GONE
    }

    fun loadFriends(shouldLoadFriends: Boolean = true) {
        friendsProgressBar.isIndeterminate = shouldLoadFriends
        friendsProgressBar.visibility = if (shouldLoadFriends) View.VISIBLE else View.GONE
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
                sendInviteBtn.setOnClickListener(this)
                addFriendBtn.setOnClickListener(this)
                sortBtn.setOnClickListener(this)
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
}
