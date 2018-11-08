package com.thing.now

import android.app.TaskStackBuilder
import android.arch.core.executor.TaskExecutor
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.thing.now.model.Connection
import com.thing.now.model.User

object ConnectionHelper {

    fun getConnectionFromId(connectionId: String): Task<DocumentSnapshot> {
        return NowHelper.connectionsRef.document(connectionId).get()
    }

    fun resolveConnection(connectionId: String): Task<DocumentSnapshot> {
        return NowHelper.connectionsRef.document(connectionId).get()
    }


    fun getUserIdFrom(connection: Connection): String {
        return if (connection.user1 == FirebaseAuth.getInstance().uid) {
            connection.user1
        } else connection.user2
    }
}