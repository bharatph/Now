package com.thing.now.helper

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.thing.now.helper.NowHelper.connectionsRef
import com.thing.now.fragment.NowFriendsFragment
import com.thing.now.helper.NowHelper.usersRef
import com.thing.now.model.Connection
import com.thing.now.model.User
import org.greenrobot.eventbus.EventBus

object ConnectionHelper {

    fun getConnectionFromId(connectionId: String) {
        NowHelper.connectionsRef.document(connectionId).get()
            .addOnSuccessListener {
                EventBus.getDefault().post(it.toObject(Connection::class.java))
            }
    }

    fun resolveConnection(connectionId: String) {
        NowHelper.connectionsRef.document(connectionId).get().addOnSuccessListener {
            EventBus.getDefault().post(it.toObject(Connection::class.java))
        }
    }


    fun getFriendUid(connection: Connection): String {
        return if (connection.user1 == FirebaseAuth.getInstance().uid) {
            connection.user1
        } else connection.user2
    }

    fun getConnection(connectionId: String) {
        connectionsRef.document(connectionId).get().addOnSuccessListener {
            EventBus.getDefault().post(it.toObject(Connection::class.java));
        }
    }

    fun listenConnection(connectionId: String) {
        connectionsRef.addSnapshotListener { documentChanges, e ->
            for (documentChange in documentChanges!!) {
                EventBus.getDefault().post(documentChanges.toObjects(Connection::class.java))
            }
        }
    }

    fun loadFriends(user: User) {
        if (user.connections.size == 0)
            return EventBus.getDefault().post(NowFriendsFragment.FriendsLoadEvent(ArrayList()))

        for (con in user.connections) {
            usersRef.whereArrayContains("connections", con).get().addOnSuccessListener {
                val friends = it.toObjects(User::class.java)
                EventBus.getDefault().post(NowFriendsFragment.FriendsLoadEvent(friends))
            }.addOnFailureListener {
                EventBus.getDefault().post(NowFriendsFragment.FriendsLoadEvent(null))
            }
        }
    }


    fun createInvite(user: User) {
        if (NowHelper.getUid() == null) {
            return EventBus.getDefault().post(NowFriendsFragment.InviteCreateEvent(null))
        }
        val con = Connection(NowHelper.getUid()!!, "")
        NowHelper.connectionsRef.add(con)
            .addOnSuccessListener {
                val id = it.id
                user.connections.add(id)
                NowHelper.usersRef.document(NowHelper.getUid()!!).update("connections", FieldValue.arrayUnion(id))
                    .addOnSuccessListener {
                        EventBus.getDefault().post(NowFriendsFragment.InviteCreateEvent(id))
                    }.addOnFailureListener {
                        EventBus.getDefault().post(NowFriendsFragment.InviteCreateEvent(null))
                    }
            }.addOnFailureListener {
                EventBus.getDefault().post(NowFriendsFragment.InviteCreateEvent(null))
            }
    }

    fun acceptInvite(user: User, id: String) {
        if (NowHelper.getUid() == null)
            return EventBus.getDefault().post(NowFriendsFragment.InviteAcceptEvent(null))
        NowHelper.connectionsRef.document(id).update("user2", NowHelper.getUid())
            .addOnSuccessListener {
                user.connections.add(id)
                NowHelper.usersRef.document(NowHelper.getUid()!!).set(user).addOnSuccessListener {
                    EventBus.getDefault().post(NowFriendsFragment.InviteAcceptEvent(id))
                }.addOnFailureListener {
                    EventBus.getDefault().post(NowFriendsFragment.InviteAcceptEvent(null))
                }
            }.addOnFailureListener {
                EventBus.getDefault().post(NowFriendsFragment.InviteAcceptEvent(null))
            }
    }
}