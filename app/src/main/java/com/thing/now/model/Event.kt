package com.thing.now.model

import java.util.Date

data class Event(
    var name: String,
    var startedOn: Date = Date(),
    var endedOn: Date? = null
) {
    constructor() : this("", Date(), null)
}