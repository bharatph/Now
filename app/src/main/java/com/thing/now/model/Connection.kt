package com.thing.now.model

data class Connection(val user1: String, var user2: String) {
    constructor() : this("", "")
}