package com.thing.now

import com.thing.now.model.Connection
import com.thing.now.model.User
import java.util.concurrent.Callable

object FriendHelper {
    fun getFriendsOf(user: User) {

    }

    fun getFriendsUserIds(user: User): Callable<ArrayList<String>> {
        return Callable {
            val friends = ArrayList<String>()
            for (uid in user.connections) {
                ConnectionHelper.resolveConnection(uid).addOnSuccessListener {
                    val connectionObj = it.toObject(Connection::class.java)!!
                    friends.add(ConnectionHelper.getUserIdFrom(connectionObj))
                }
            }
            friends
        }
    }
}