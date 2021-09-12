package com.skyfolk.quantoflife.meansure

enum class Measure {
    TotalCount,
    TotalPhysical,
    TotalEmotional,
    TotalEvolution,
    Quantity,
    AverageRating;

    fun toPosition(): Int {
        return this.ordinal
    }
}

fun Int.fromPositionToMeasure(): Measure {
    return when (this) {
        0 -> Measure.TotalCount
        1 -> Measure.TotalPhysical
        2 -> Measure.TotalEmotional
        3 -> Measure.TotalEvolution
        4 -> Measure.Quantity
        5 -> Measure.AverageRating
        else -> Measure.TotalCount
    }
}