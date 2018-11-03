package com.thing.now

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.BottomSheetBehavior
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import com.google.firebase.auth.FirebaseAuth
import com.squareup.picasso.Picasso
import com.thing.now.adapter.NowFriendsAdapter
import com.thing.now.model.User
import kotlinx.android.synthetic.main.header.*
import kotlinx.android.synthetic.main.now_friends_fragment.*
import kotlinx.android.synthetic.main.user_item.*
import uk.co.chrisjenx.calligraphy.CalligraphyConfig
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper
import android.text.InputType
import android.widget.EditText
import android.widget.Toast


class MainActivity : AppActivity(), View.OnClickListener {

    companion object {
        const val TAG = "MainActivity"
    }

    var adapter: NowFriendsAdapter? = null

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.sendInviteBtn -> {
                NowHelper.createConnection().addOnSuccessListener {
                    val shareIntent: Intent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, it.id)
                        type = "text/plain"
                    }
                    startActivity(shareIntent)
                }.addOnFailureListener {
                    show("Cannot send invite at this moment")
                }
            }
            R.id.addFriendBtn -> {
                var m_Text = ""
                val builder = AlertDialog.Builder(this)
                builder.setTitle("Title")

                val input = EditText(this)
                input.inputType = InputType.TYPE_CLASS_TEXT
                builder.setView(input)

                builder.setPositiveButton(
                    "OK"
                ) { dialog, which ->
                    m_Text = input.text.toString()
                    NowHelper.completeConnection(m_Text)?.addOnSuccessListener {
                        Toast.makeText(this, "Friend added", Toast.LENGTH_LONG).show()
                        adapter?.notifyDataSetChanged()
                    }
                }
                builder.setNegativeButton(
                    "Cancel"
                ) { dialog, which -> dialog.cancel() }

                builder.show()
            }
            R.id.sortBtn -> {

            }
            R.id.appInfoBtn -> {
                startActivity(Intent(this, OnboardingActivity::class.java))
            }
        }

    }

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase))
    }

    fun show(message: String, listener: DialogInterface.OnClickListener? = null) {
        AlertDialog.Builder(this).setPositiveButton("Ok", listener).setOnDismissListener {
            DialogInterface.OnDismissListener { dialogInterface ->
                listener?.onClick(dialogInterface, 0)
            }
        }.setMessage(message).create().show()
    }

    fun settleUser(user: User) {
        //dp
        if (user.dp.isNotBlank()) {
            Picasso.get().load(user.dp)
                .error(R.drawable.ic_user)
                .placeholder(R.drawable.ic_user)
        }
        //name
        userName.text = user.name
        userStatus.text = user.status
    }

    fun settleOtherUsers() {
        var firestoreUsers = FirestoreList<User>(User::class.java, NowHelper.usersRef)
        adapter = NowFriendsAdapter(this, firestoreUsers)
        firestoreUsers.setOnAddListener { firestoreId, t ->
            adapter?.notifyDataSetChanged()
            emptyList.visibility = View.GONE
        }
        firestoreUsers.setOnDeleteListener { firestoreId, t ->
            adapter?.notifyDataSetChanged()
            if (firestoreUsers.size == 1) {
                emptyList.visibility = View.VISIBLE
            }
        }
        nowFriendsRecyclerView.adapter = adapter
        nowFriendsRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayout.VERTICAL, false)
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

        if (isFirstTime) {
            startActivity(Intent(this, OnboardingActivity::class.java))

            FirebaseAuth.getInstance().signInAnonymously()
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Log.d(MainActivity.TAG, "signInAnonymously:success")
                        NowHelper.createUser(
                            FirebaseAuth.getInstance().uid!!,
                            User(
                                FirebaseAuth.getInstance().uid!!,
                                "No name",
                                "",
                                "",
                                ArrayList<String>(),
                                ArrayList<String>(),
                                null
                            )
                        ).addOnSuccessListener {
                            isFirstTime = false
                            loadUser()
                        }
                    } else {
                        Log.w(MainActivity.TAG, "signInAnonymously:failure", task.exception)
                        show("No Internet, please try again later", DialogInterface.OnClickListener { _, _ ->
                            finish()
                        })
                    }
                }
        } else {
            loadUser()
        }
    }

    private fun loadUser() {
        var status = NowHelper.loadUser(FirebaseAuth.getInstance().currentUser!!.uid)?.addOnSuccessListener {
            val user = it.toObject(User::class.java)
            if (user == null) {
                show("Restart application, fatal error",
                    DialogInterface.OnClickListener { dialog, which -> finish() })
                return@addOnSuccessListener
            }
            settleUser(NowHelper.user!!)
            settleOtherUsers()

            sendInviteBtn.setOnClickListener(this)
            addFriendBtn.setOnClickListener(this)
            sortBtn.setOnClickListener(this)
            appInfoBtn.setOnClickListener(this)


        }?.addOnFailureListener {
            show("Unknown error occured")
        }
        if (status == null) {
            show("Cannot communicate with servers, restart application",
                DialogInterface.OnClickListener { _, _ -> finish() })
        }
    }
}
