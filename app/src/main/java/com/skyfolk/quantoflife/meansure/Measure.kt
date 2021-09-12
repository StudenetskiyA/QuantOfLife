package com.skyfolk.quantoflife.meansure

enum class Measure {
    TotalCount,
    Quantity,
    AverageRating;

    fun toPosition(): Int {
        return this.ordinal
    }
}

fun Int.fromPositionToMeasure(): Measure {
    return when (this) {
        0 -> Measure.TotalCount
        1 -> Measure.Quantity
        2 -> Measure.AverageRating
        else -> Measure.TotalCount
    }
}