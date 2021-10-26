package com.skyfolk.quantoflife.timeInterval

sealed class TimeInterval {
    object Today : TimeInterval()
    object Week : TimeInterval()
    object Month : TimeInterval()
    object Year: TimeInterval()
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
            is Year -> array[5]
        }
    }

    fun toDuration(): Long {
        val day: Long = (24 * 60 * 60 * 1000).toLong()
        return when (this) {
            is Today -> day
            is Week -> day * 7
            // TODO
            is Month -> day * 31
            is All -> 0
            is Selected -> 0
            is Year -> day * 365
        }
    }
}
