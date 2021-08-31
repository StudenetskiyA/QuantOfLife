package com.skyfolk.quantoflife.db

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.Required
import java.util.*

open class EventDbEntity(
    var quantId: String = "",
    var date: Long = 0,
    var rate: Double? = null,
    var numericValue: Double? = null,
    var note: String = "",
    @PrimaryKey
    var id: String = UUID.randomUUID().toString(),
) : RealmObject()