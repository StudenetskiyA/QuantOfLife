package com.skyfolk.quantoflife.db

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.util.*

open class EventDbEntity(
    var quantId: String = "",
    var date: Long = 0,
    var rate: Int? = null,
    var numericValue: Int? = null,
    var note: String = "",
    @PrimaryKey
    var id: String = UUID.randomUUID().toString(),
) : RealmObject()