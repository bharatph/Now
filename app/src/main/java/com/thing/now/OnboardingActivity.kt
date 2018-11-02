package com.thing.now

import android.content.DialogInterface
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.thing.now.model.User
import kotlinx.android.synthetic.main.activity_onboarding.*

class OnboardingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        if (FirebaseAuth.getInstance().currentUser != null) {
            onboard_exit.setOnClickListener {
                finish()
            }
            return
        }

        onboard_exit.setOnClickListener {
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
                            finish()
                        }
                    } else {
                        Log.w(MainActivity.TAG, "signInAnonymously:failure", task.exception)
                        AlertDialog.Builder(this).setPositiveButton("Ok") { _: DialogInterface, _: Int ->
                            recreate()
                        }.setMessage("No internet, please try again later").create().show()
                    }
                }
        }
    }

    override fun onBackPressed() {
        //ignore
    }
}
