package com.thing.now

import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.thing.now.model.Connection
import com.thing.now.model.User

object NowHelper {
    val TAG = "NowHelper"
    var auth: FirebaseAuth = FirebaseAuth.getInstance()
    var user: User? = null

    var usersRef = FirebaseFirestore.getInstance().collection("users")
    var connectionsRef = FirebaseFirestore.getInstance().collection("connections")

    init {
    }

    fun createConnection(): String {
        connectionsRef.add(Connection(user!!.id, ""))
        return ""
    }
}