package com.thing.now

import android.content.Context
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.thing.now.model.Connection
import com.thing.now.model.User
import java.lang.Exception

object NowHelper {
    val TAG = "NowHelper"
    var auth: FirebaseAuth = FirebaseAuth.getInstance()
    var user: User? = null
    var uid: String? = null

    var usersRef = FirebaseFirestore.getInstance().collection("users")
    var connectionsRef = FirebaseFirestore.getInstance().collection("connections")

    init {
    }

    fun updateUser() {

    }

    fun checkForUser(id: String): Task<DocumentSnapshot>? {
        return try {
            usersRef.document(id).get()
        } catch (e: Exception) {
            null
        }
    }

    fun loadUser(uid: String): Task<DocumentSnapshot>? {
        return try {
            usersRef.document(uid).get().addOnSuccessListener {
                this.user = it.toObject(User::class.java)
                this.uid = it.id
            }
        } catch (e: Exception) {
            return null
        }
    }

    fun createUser(uid: String, user: User): Task<Void> {
        this.uid = uid
        this.user = user
        return usersRef.document(uid).set(user)
    }

    interface ConnectionCreatedListener {
        fun onConnectionCreate()
    }

    fun createConnection(): Task<DocumentReference> {
        return connectionsRef.add(Connection(user!!.id, "")).addOnSuccessListener {
            user!!.connections.add(it.id)
        }
    }

    fun completeConnection(id: String): Task<Void> {
        return connectionsRef.document(id).update("user2", user!!.id).addOnSuccessListener {
            user!!.connections.add(id)
        }
    }
}