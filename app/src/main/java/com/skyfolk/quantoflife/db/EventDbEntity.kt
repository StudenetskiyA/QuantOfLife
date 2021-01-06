package com.skyfolk.quantoflife.db

import io.realm.RealmObject

open class EventDbEntity(
    var quantId: String = "",
    var date: Long = 0,
    var rate: Int? = null,
    var numericValue: Int? = null,
    var note: String = ""
) : RealmObject()