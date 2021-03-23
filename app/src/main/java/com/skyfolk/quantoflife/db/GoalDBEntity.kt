package com.skyfolk.quantoflife.db

import com.skyfolk.quantoflife.entity.QuantCategory
import com.skyfolk.quantoflife.ui.feeds.TimeInterval
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.util.*

open class GoalDBEntity(
    var duration: String = TimeInterval.Week.javaClass.name,
    var target: Double = 0.0,
    var type: String = QuantCategory.All.javaClass.name,
    @PrimaryKey
    var id: String = UUID.randomUUID().toString(),
) //: RealmObject()