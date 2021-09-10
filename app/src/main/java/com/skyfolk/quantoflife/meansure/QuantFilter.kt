package com.skyfolk.quantoflife.meansure

sealed class QuantFilter {
    object Nothing : QuantFilter()
    object All : QuantFilter()
    class OnlySelected(val selectQuant: String): QuantFilter()

    fun toGraphPosition(listOfQuantName: List<String>) : Int {
        return when (this) {
            is All -> 0
            is Nothing -> 1
            is OnlySelected -> {
                listOfQuantName.indexOf(selectQuant)
            }
        }
    }

    override fun toString(): String {
        return when (this) {
            is Nothing -> "Ничего"
            is All -> "Все события"
            is OnlySelected -> this.selectQuant
        }
    }
}

fun Int.fromPositionToQuantFilter(selectedQuant: String): QuantFilter {
    return when (this) {
        0 -> QuantFilter.All
        1 -> QuantFilter.Nothing
        2 -> QuantFilter.OnlySelected(selectQuant = selectedQuant)
        else -> QuantFilter.All
    }
}