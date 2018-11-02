package com.thing.now.model

data class User(
    val id: String,
    var name: String,
    var status: String,
    var dp: String,
    val connections: ArrayList<String>,
    val eventHistory: ArrayList<String>,
    var event: String?
) {
    constructor() : this(
        "",
        "Name",
        "Topic",
        "https://static2.tripoto.com/media/filter/nl/img/193747/TripDocument/1523643618_img_20180214_123205_blog.jpg",
        ArrayList<String>(),
        ArrayList<String>(),
        null
    )
}