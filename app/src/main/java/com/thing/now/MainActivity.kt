package com.thing.now

import android.content.ClipboardManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.BaseTransientBottomBar
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
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper


class MainActivity : AppActivity(), View.OnClickListener {

    companion object {
        const val TAG = "MainActivity"
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.addFriendBtn -> {
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
            R.id.sortBtn -> {

            }
            R.id.appInfoBtn -> {
                startActivity(Intent(this, OnboardingActivity::class.java))
            }
        }

    }

    var bottomSheet: BottomSheetBehavior<View>? = null

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
        var adapter = NowFriendsAdapter(this, firestoreUsers)
        firestoreUsers.setOnAddListener { firestoreId, t ->
            adapter.notifyDataSetChanged()
            emptyList.visibility = View.GONE
        }
        firestoreUsers.setOnDeleteListener { firestoreId, t ->
            adapter.notifyDataSetChanged()
            if (firestoreUsers.size == 1) {
                emptyList.visibility = View.VISIBLE
            }
        }
        nowFriendsRecyclerView.adapter = adapter
        nowFriendsRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayout.VERTICAL, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


//TODO add custom font
//        CalligraphyConfig.initDefault(
//            CalligraphyConfig.Builder()
//                .setDefaultFontPath("fonts/Roboto-RobotoRegular.ttf")
//                .setFontAttrId(R.attr.fontPath)
//                .build()
//        )

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

            addFriendBtn.setOnClickListener(this)
            sortBtn.setOnClickListener(this)
            appInfoBtn.setOnClickListener(this)

            val clipboard = this.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

            clipboard.addPrimaryClipChangedListener {
                clipboardFriendIndicator.visibility = View.GONE
                clipboardFriendIndicator.setOnClickListener(null)
                val clip = clipboard.primaryClip!!.getItemAt(0).text
                NowHelper.checkForUser(clip.toString())?.addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        clipboardFriendIndicator.visibility = View.VISIBLE
                        clipboardFriendIndicator.setOnClickListener {
                            clipboardFriendIndicator.visibility = View.GONE
                            NowHelper.completeConnection(doc.id)
                        }
                    }
                }
            }


        }?.addOnFailureListener {
            show("Unknown error occured")
        }
        if (status == null) {
            show("Cannot communicate with servers, restart application",
                DialogInterface.OnClickListener { _, _ -> finish() })
        }
    }
}
