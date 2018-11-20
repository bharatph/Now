package com.thing.now.model

data class User(
    val id: String,
    var name: String,
    var status: String,
    var dp: String,
    var task: Task?,
    val connections: ArrayList<String>,
    val eventHistory: ArrayList<String>
) {
    constructor() : this(
        "",
        "Name",
        "Topic",
        "",
        null,
        ArrayList<String>(),
        ArrayList<String>()
    )

    interface OnUserAddedListener {
        fun onUserAdd(user: User)
    }

    interface OnUserRemovedListener {
        fun onUserRemove(user: User)
    }

    interface OnUserModifiedListener {
        fun onUserModified(user: User)
    }
}