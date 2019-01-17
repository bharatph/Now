package com.thing.now.helper

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.thing.now.MainActivity
import com.thing.now.model.User
import org.greenrobot.eventbus.EventBus


object NowHelper {
    val TAG = "NowHelper"
    var usersRef = FirebaseFirestore.getInstance().collection("users")
    var connectionsRef = FirebaseFirestore.getInstance().collection("connections")

    init {
    }

    fun getUid(): String? {
        return FirebaseAuth.getInstance().currentUser?.uid
    }

    fun updateUser(user: User) {
        usersRef.document(getUid()!!).set(user).addOnSuccessListener {
            EventBus.getDefault().post(MainActivity.OnUserUpdateEvent(user))
        }.addOnFailureListener {
            EventBus.getDefault().post(MainActivity.OnUserUpdateEvent(null))
        }
    }

    fun createUser(user: User) {
        usersRef.document(getUid()!!).set(user).addOnSuccessListener {
            EventBus.getDefault().post(MainActivity.OnUserCreateEvent(user))
        }.addOnFailureListener {
            EventBus.getDefault().post(MainActivity.OnUserCreateEvent(null))
        }
    }

    fun loadExistingUser() {
        if (getUid() == null) {
            EventBus.getDefault().post(MainActivity.OnUserLoadEvent(null))
            return
        }
        usersRef.document(getUid()!!).get().addOnSuccessListener {
            val user = it.toObject(User::class.java)
            EventBus.getDefault().post(MainActivity.OnUserLoadEvent(user))
        }.addOnFailureListener {
            EventBus.getDefault().post(MainActivity.OnUserLoadEvent(null))
        }
    }

    fun generateUserId() {
        FirebaseAuth.getInstance().signInAnonymously().addOnCompleteListener {
            EventBus.getDefault().post(
                MainActivity.OnUserLoginEvent(getUid())
            )
        }
    }

//        val friends: ArrayList<User> = ArrayList()
//        if (user.connections.size == 0) {
//            l(friends)
//        } else {
//            for (con in user.connections) {
//                connectionsRef.document(con).get().addOnSuccessListener {
//                    val connection: Connection? = it.toObject(Connection::class.java)
//                    if (connection == null) {
//                        l(null) //FIXME
//                    } else {
//                        val friendId: String = if (connection.user1 != uid) connection.user1 else connection.user2
//
//                        if (friendId.isEmpty()) return@addOnSuccessListener
//
//                        usersRef.document(friendId).get().addOnSuccessListener {
//                            friends.add(it.toObject(User::class.java)!!)
//                            l(friends)
//                        }
//                    }
//                }
//            }


}