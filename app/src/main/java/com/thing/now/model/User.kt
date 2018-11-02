package com.thing.now.model

data class User(
    val id: String,
    val name: String,
    val status: String,
    val dp: String,
    val eventHistory: List<Event>,
    val event: String?
) {
    constructor() : this("", "", "", "", ArrayList<Event>(), null)
}