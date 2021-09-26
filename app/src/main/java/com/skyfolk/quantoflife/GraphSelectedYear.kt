package com.skyfolk.quantoflife

sealed class GraphSelectedYear {
    object All: GraphSelectedYear()
    data class OnlyYear(val year: Int): GraphSelectedYear()

    fun toGraphPosition(listOfYears: List<String>) : Int {
        return when (this) {
            is GraphSelectedYear.All -> 0
            is GraphSelectedYear.OnlyYear -> {
                listOfYears.indexOf(year.toString())
            }
        }
    }
}