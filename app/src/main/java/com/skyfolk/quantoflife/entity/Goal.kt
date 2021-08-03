package com.skyfolk.quantoflife.entity

import com.skyfolk.quantoflife.timeInterval.TimeInterval
import java.util.*

data class Goal(
    var id: String = UUID.randomUUID().toString(),
    var duration: TimeInterval,
    var target: Double,
    var type: QuantCategory
)

data class GoalPresent(
    var id: String,
    var duration: TimeInterval,
    var durationInDays: Int,
    var target: Double,
    var comleted: Double,
    var daysGone: Int,
    var type: QuantCategory
)
