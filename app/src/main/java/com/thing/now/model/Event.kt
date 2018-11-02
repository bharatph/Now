package com.thing.now.model

import java.util.Date

data class Event(
    val status: String,
    val startedOn: Date = Date(),
    val endedOn: Date? = null
) {
    constructor() : this("", Date(), null)
}