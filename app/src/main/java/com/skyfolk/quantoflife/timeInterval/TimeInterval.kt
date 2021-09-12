package com.skyfolk.quantoflife.timeInterval

sealed class TimeInterval {
    object Today : TimeInterval()
    object Week : TimeInterval()
    object Month : TimeInterval()
    class Selected(val start: Long, val end: Long) : TimeInterval()
    object All : TimeInterval()

    companion object {
        fun toTimeInterval(enumString: String, start: Long, end: Long): TimeInterval {
            return when (enumString) {
                Today.javaClass.name -> Today
                Week.javaClass.name -> Week
                Month.javaClass.name -> Month
                Selected(start, end).javaClass.name -> Selected(start, end)
                else -> All
            }
        }
    }

    fun toGraphPosition(): Int {
        return when(this) {
            is Today -> 0
            is Week -> 1
            else -> 2
        }
    }

    fun toStringName(array: Array<String>): String {
        return when (this) {
            is Today -> array[0]
            is Week -> array[1]
            is Month -> array[2]
            is All -> array[3]
            is Selected -> array[4]
        }
    }
}
