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
import kotlinx.android.synthetic.main.circle_timer.*
import uk.co.chrisjenx.calligraphy.CalligraphyConfig
import io.grpc.Deadline.after
import java.util.*
import kotlin.concurrent.timerTask


class MainActivity : AppActivity(), View.OnClickListener,
    User.OnUserAddListener {
    override fun onUserAdd(user: User) {
        toast(getString(R.string.invite_link_user_added))
        nowFriendsRecyclerView.adapter?.notifyDataSetChanged()
    }

    companion object {
        const val TAG = "MainActivity"
    }

    override fun onClick(v: View?) {
        supportFragmentManager.popBackStack()
        when (v!!.id) {
            R.id.sendInviteBtn -> {
                supportFragmentManager.beginTransaction()
                    .add(R.id.fragmentOptionContainer, InviteFragment())
                    .addToBackStack("stack")
                    .commit()
            }
            R.id.addFriendBtn -> {
                var clipboard = getSystemService(CLIPBOARD_SERVICE) as android.content.ClipboardManager
                var inviteLink: String? = clipboard.primaryClip?.getItemAt(0)?.text as String?
                if (inviteLink.isNullOrEmpty()) {
                    toast(getString(R.string.invite_link_empty))
                    return
                }
                addFriendBtn.isEnabled = false
                var callback = NowHelper.completeConnection(inviteLink)
                if (callback == null) {
                    addFriendBtn.isEnabled = true
                    toast(getString(R.string.invite_link_invalid))
                    return
                }
                callback.addOnSuccessListener {
                    addFriendBtn.isEnabled = true
                    toast(getString(R.string.invite_link_user_added))
                    settleOtherUsers()
                }.addOnFailureListener {
                    addFriendBtn.isEnabled = true
                    toast(getString(R.string.invite_link_error))
                }
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
                val editText = EditText(this).apply {
                    inputType = InputType.TYPE_CLASS_TEXT
                }
                AlertDialog.Builder(this)
                    .setTitle("Start event")
                    .setPositiveButton(getString(R.string.start_timer)) { _, _ ->
                        val str = editText.text.toString()
                        if (str.isEmpty()) {
                            editText.error = "Invalid task"
                            return@setPositiveButton
                        }
                        val callback = NowHelper.addEvent(str)
                        if (callback == null) {
                            toast("Retreival error")
                            return@setPositiveButton
                        }
                        callback.addOnSuccessListener {
                            //TODO upload start date
                            var i = 0
                            val timer = Timer().apply {
                                scheduleAtFixedRate(timerTask {
                                    Handler(mainLooper).post {
                                        nowTimer.text = i++.toString()
                                    }
                                }, 0, 1000)
                            }

                            //rewire click
                            circleTimer.setOnClickListener {
                                //TODO upload end date
                                timer.cancel()
                                nowTimer.text = getString(R.string.start_timer)
                                circleTimer.setOnClickListener(this@MainActivity)
                            }

                        }.addOnFailureListener {
                            toast("starting task failed")
                        }
                    }
                    .setNegativeButton("Cancel") { _, _ ->
                    }
                    .setView(editText).show()
            }
        }
    }

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase))
    }

    fun settleUser(user: User) {
        //dp
//        if (user.dp.isNotBlank()) {
//            Picasso.get().load(user.dp)
//                .error(R.drawable.ic_user)
//                .placeholder(R.drawable.ic_user)
//        }
        //name
        userName.text = if (user.name.isEmpty()) getString(R.string.default_user_name) else user.name
        userStatus.text = if (user.status.isEmpty()) getString(R.string.user_status_idle) else user.status
    }

    fun settleOtherUsers() {
        NowHelper.friendList {
            if (it == null) {
                emptyList.visibility = View.VISIBLE
                return@friendList
            }
            emptyList.visibility = View.GONE
            nowFriendsRecyclerView.adapter = NowFriendsAdapter(this, it)
        }
        nowFriendsRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayout.VERTICAL, false)
    }

    fun loadOnboarding() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.onboardingFragment, OnboardingFragment().apply {
                enterTransition = Fade(Fade.MODE_IN)
            })
            .addToBackStack(getString(R.string.onboarding_fragment_tag))
            .commit()
    }


    fun userOnboard() {
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
                        recreate()
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
        if (isFirstTime) userOnboard() else loadUser()
    }

    private fun loadUser() {
        var status = NowHelper.loadUser(FirebaseAuth.getInstance().currentUser!!.uid)
            ?.addOnSuccessListener {
                val user = it.toObject(User::class.java)
                if (user == null) {
                    show(getString(R.string.app_fatal_error),
                        DialogInterface.OnClickListener { dialog, which -> finish() })
                    return@addOnSuccessListener
                }
                settleUser(NowHelper.user!!)
                settleOtherUsers()

                userName.setOnClickListener(this)
                sendInviteBtn.setOnClickListener(this)
                addFriendBtn.setOnClickListener(this)
                sortBtn.setOnClickListener(this)
                appInfoBtn.setOnClickListener(this)
                circleTimer.setOnClickListener(this)


            }?.addOnFailureListener {
                show(getString(R.string.app_unknown_error))
            }
        if (status == null) {
            show(getString(R.string.app_network_unreachable_error),
                DialogInterface.OnClickListener { _, _ -> finish() })
        }
    }
}
