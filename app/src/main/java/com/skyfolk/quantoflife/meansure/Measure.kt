package com.skyfolk.quantoflife.meansure

import com.skyfolk.quantoflife.timeInterval.TimeInterval

enum class Meansure {
    TotalCount,
    Quantity,
    AverageRating
}

fun Int.fromPositionToMea(): Meansure {
    return when (this) {
        0 -> Meansure.TotalCount
        1 -> Meansure.Quantity
        2 -> Meansure.AverageRating
        else -> Meansure.TotalCount
    }
}