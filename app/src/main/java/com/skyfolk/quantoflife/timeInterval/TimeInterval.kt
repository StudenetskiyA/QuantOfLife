package com.skyfolk.quantoflife.timeInterval

import android.util.Log
import com.skyfolk.quantoflife.utils.getStartDateCalendar
import com.skyfolk.quantoflife.utils.toDate
import java.util.*

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

    fun getPeriod(firstDate: Long, index: Int, startDayTime: Long): Period {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = firstDate
        calendar.timeInMillis = calendar.getStartDateCalendar(
            this,
            startDayTime
        ).timeInMillis
        //Log.d("skyfolk-index", "getPeriod/init: ${calendar.timeInMillis.toDate()}")

        calendar.add(this.toCalendarPeriod(), index + 1)
        //Log.d("skyfolk-index", "getPeriod/2: ${calendar.timeInMillis.toDate()}")

        val startFirstPeriodTimeInMillis =
            calendar.getStartDateCalendar(
                this,
                startDayTime
            ).timeInMillis

        calendar.timeInMillis = startFirstPeriodTimeInMillis
        //Log.d("skyfolk-index", "getPeriod/3: ${calendar.timeInMillis.toDate()}")

        calendar[this.toCalendarPeriod()] += 1
        calendar.timeInMillis -= 24*60*60*1000

       // Log.d("skyfolk-index", "getPeriod: ${startFirstPeriodTimeInMillis.toDate()} to ${calendar.timeInMillis.toDate()}, index = $index")
        return Period(startFirstPeriodTimeInMillis, calendar.timeInMillis)
    }

    private fun toCalendarPeriod(): Int {
       return when (this) {
            Week -> Calendar.WEEK_OF_YEAR
            Month -> Calendar.MONTH
            Today -> Calendar.DAY_OF_YEAR
            else -> Calendar.DAY_OF_YEAR
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
}

data class Period(
    val start: Long,
    val end: Long
)
