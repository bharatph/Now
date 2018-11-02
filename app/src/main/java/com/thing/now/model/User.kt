package com.thing.now.model

data class User(
    val id: String,
    val name: String,
    val status: String,
    val dp: String,
    val connections: List<Connection>,
    val eventHistory: List<Event>,
    val event: String?
) {
    constructor() : this(
        "",
        "Name",
        "Topic",
        "https://static2.tripoto.com/media/filter/nl/img/193747/TripDocument/1523643618_img_20180214_123205_blog.jpg",
        ArrayList<Connection>(),
        ArrayList<Event>(),
        null
    )
}