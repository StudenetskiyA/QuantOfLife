package com.skyfolk.quantoflife.entity

import com.skyfolk.quantoflife.ui.feeds.TimeInterval

data class Goal(
    var duration: TimeInterval,
    var target: Double,
    var type: QuantCategory
)

data class GoalPresent(
    var duration: TimeInterval,
    var durationInDays: Int,
    var target: Double,
    var comleted: Double,
    var daysGone: Int,
    var type: QuantCategory
)
