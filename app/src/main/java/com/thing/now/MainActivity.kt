package com.thing.now

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.BottomSheetBehavior
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import com.thing.now.adapter.NowFriendsAdapter
import com.thing.now.model.User
import kotlinx.android.synthetic.main.now_friends_fragment.*
import kotlinx.android.synthetic.main.user_item.*
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper
import uk.co.chrisjenx.calligraphy.CalligraphyConfig

class MainActivity : AppCompatActivity(), View.OnClickListener {
    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.addFriendBtn -> {

            }
            R.id.sortBtn -> {

            }
        }

    }

    var bottomSheet: BottomSheetBehavior<View>? = null

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase))
    }

    fun updateUI(user: User) {
    }

    fun settleUser(user: User) {
        //dp
        if (user.dp.isBlank())
            Picasso.get().load(R.drawable.ic_user).into(userImage)
        else
            Picasso.get().load(user.dp).into(userImage)
        //name
        userName.text = user.name
        userStatus.text = user.status
    }

    companion object {
        const val TAG = "MainActivity"
    }

    fun settleOtherUsers() {
        var firestoreUsers = FirestoreList<User>(User::class.java, NowHelper.usersRef)
        var adapter = NowFriendsAdapter(this, firestoreUsers)
        firestoreUsers.setOnAddListener { firestoreId, t ->
            adapter.notifyDataSetChanged()
        }
        firestoreUsers.setOnDeleteListener { firestoreId, t ->
            adapter.notifyDataSetChanged()
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

        FirebaseAuth.getInstance().signInAnonymously()
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "signInAnonymously:success")
//                    if (NowHelper.user == null) {
//                        //startActivity(Intent(this, OnboardingActivity::class.java))
//                    } else {
                        settleUser(User())
                        settleOtherUsers()
//                    }
                } else {
                    Log.w(TAG, "signInAnonymously:failure", task.exception)
                }
            }
    }
}
