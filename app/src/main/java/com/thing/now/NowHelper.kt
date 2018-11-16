package com.thing.now

import android.content.Context
import android.telecom.Call
import android.util.Log
import com.google.android.gms.common.api.internal.TaskUtil
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.thing.now.model.Connection
import com.thing.now.model.Event
import com.thing.now.model.User
import java.lang.Exception
import java.util.*
import java.util.concurrent.*
import javax.xml.datatype.DatatypeConstants.SECONDS
import kotlin.concurrent.thread
import kotlin.concurrent.timerTask


object NowHelper {
    val TAG = "NowHelper"
    var auth: FirebaseAuth = FirebaseAuth.getInstance()
    var user: User? = null
    var uid: String? = null

    var usersRef = FirebaseFirestore.getInstance().collection("users")
    var connectionsRef = FirebaseFirestore.getInstance().collection("connections")
    var eventsRef = FirebaseFirestore.getInstance().collection("events")

    init {
    }

    fun addEvent(string: String): Task<DocumentReference>? {
        var event = Event(string, Date(), null)
        return when {
            string.isEmpty() -> {
                null
            }
            else -> {
                eventsRef.add(event).addOnSuccessListener {
                    user!!.eventHistory.add(it.id)
                    updateUser() //FIXME this should return the handle
                }
            }
        }
    }

    fun updateUser(): Task<Void> {
        return usersRef.document(uid!!).set(user!!)
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


    interface OnUsersAdd {
        fun onUsersAdd(friends: ArrayList<User>?)
    }

    fun friendList(l: (ArrayList<User>?) -> Unit) {
        var friends: ArrayList<User> = ArrayList()
        if (user!!.connections.size == 0) {
            l(null)
        } else {
            for (con in user!!.connections) {
                connectionsRef.document(con).get().addOnSuccessListener {
                    var connection: Connection? = it.toObject(Connection::class.java)
                    if (connection == null) {
                        l(null) //FIXME
                        return@addOnSuccessListener
                    }

                    var friendId: String = if (connection.user1 != uid) {
                        connection.user1
                    } else
                        connection.user2

                    if (friendId.isEmpty()) return@addOnSuccessListener

                    usersRef.document(friendId).get().addOnSuccessListener {
                        friends.add(it.toObject(User::class.java)!!)
                        l(friends)
                    }
                }
            }
        }
    }


    fun createConnection(): Task<DocumentReference> {
        return connectionsRef.add(Connection(user!!.id, "")).addOnSuccessListener {
            user!!.connections.add(it.id)
            updateUser()
        }
    }

    fun completeConnection(id: String): Task<Void>? {
        return when {
            user!!.connections.contains(id) -> null
            id.contains("/") -> null
            else -> connectionsRef.document(id).update("user2", user!!.id)
                .addOnSuccessListener {
                    user!!.connections.add(id)
                    updateUser() //FIXME this should return the handle
                }
        }
    }
}