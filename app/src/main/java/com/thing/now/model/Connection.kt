package com.thing.now.model

data class Connection(val user1: String, val user2: String) {
    constructor() : this("", "")
}