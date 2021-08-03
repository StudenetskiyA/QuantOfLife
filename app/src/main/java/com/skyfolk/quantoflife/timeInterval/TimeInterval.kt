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

    fun toStringName(): String {
        return when (this) {
            is Today -> "сегодня"
            is Week -> "неделю"
            is Month -> "месяц"
            is All -> "все время"
            is Selected -> "выбранный интервал"
        }
    }
}
