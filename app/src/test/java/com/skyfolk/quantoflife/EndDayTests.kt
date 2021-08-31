package com.skyfolk.quantoflife

import com.skyfolk.quantoflife.timeInterval.TimeInterval
import com.skyfolk.quantoflife.utils.getEndDateCalendar
import org.junit.Assert
import org.junit.Test
import java.util.*

class EndDayTests {
    private val calendar = Calendar.getInstance()
    private var startDayTimeInMillis = ((5 * 60 * 60 * 1000) + (20 * 60 * 1000)).toLong() //05:20

    init {
        calendar[Calendar.YEAR] = 2021
        calendar[Calendar.MONTH] = 2
        calendar[Calendar.DAY_OF_MONTH] = 6
        calendar[Calendar.HOUR_OF_DAY] = 5
        calendar[Calendar.MINUTE] = 5
    }

    @Test
    fun selectedTimeInterval_Day() {
        //05:05
        Assert.assertEquals(calendar[Calendar.DAY_OF_MONTH], calendar.getEndDateCalendar(
            TimeInterval.Today, startDayTimeInMillis)[Calendar.DAY_OF_MONTH])

        //05:25
        calendar[Calendar.HOUR_OF_DAY] = 5
        calendar[Calendar.MINUTE] = 25
        Assert.assertEquals(calendar[Calendar.DAY_OF_MONTH]+1, calendar.getEndDateCalendar(TimeInterval.Today, startDayTimeInMillis)[Calendar.DAY_OF_MONTH])
    }

    @Test
    fun selectedTimeInterval_Week() {
        //06-02 05:05
        Assert.assertEquals(8, calendar.getEndDateCalendar(TimeInterval.Week, startDayTimeInMillis)[Calendar.DAY_OF_MONTH])
        Assert.assertEquals(20, calendar.getEndDateCalendar(TimeInterval.Week, startDayTimeInMillis)[Calendar.MINUTE])

        //06-02 05:25
        calendar[Calendar.MINUTE] = 25
        Assert.assertEquals(8, calendar.getEndDateCalendar(TimeInterval.Week, startDayTimeInMillis)[Calendar.DAY_OF_MONTH])
        Assert.assertEquals(20, calendar.getEndDateCalendar(TimeInterval.Week, startDayTimeInMillis)[Calendar.MINUTE])

        //01-02 05:25
        calendar[Calendar.DAY_OF_MONTH] = 1
        Assert.assertEquals(8, calendar.getEndDateCalendar(TimeInterval.Week, startDayTimeInMillis)[Calendar.DAY_OF_MONTH])
        Assert.assertEquals(20, calendar.getEndDateCalendar(TimeInterval.Week, startDayTimeInMillis)[Calendar.MINUTE])

        //01-02 05:05
        calendar[Calendar.MINUTE] = 5
        Assert.assertEquals(1, calendar.getEndDateCalendar(TimeInterval.Week, startDayTimeInMillis)[Calendar.DAY_OF_MONTH])
        Assert.assertEquals(20, calendar.getEndDateCalendar(TimeInterval.Week, startDayTimeInMillis)[Calendar.MINUTE])

        //08-02 05:05
        calendar[Calendar.DAY_OF_MONTH] = 8
        Assert.assertEquals(8, calendar.getEndDateCalendar(TimeInterval.Week, startDayTimeInMillis)[Calendar.DAY_OF_MONTH])
        Assert.assertEquals(20, calendar.getEndDateCalendar(TimeInterval.Week, startDayTimeInMillis)[Calendar.MINUTE])

        //08-02 05:25
        calendar[Calendar.MINUTE] = 25
        Assert.assertEquals(15, calendar.getEndDateCalendar(TimeInterval.Week, startDayTimeInMillis)[Calendar.DAY_OF_MONTH])
        Assert.assertEquals(20, calendar.getEndDateCalendar(TimeInterval.Week, startDayTimeInMillis)[Calendar.MINUTE])

        //01-01 05:05
        calendar[Calendar.DAY_OF_MONTH] = 1
        calendar[Calendar.MONTH] = 0
        calendar[Calendar.MINUTE] = 5
        Assert.assertEquals(4, calendar.getEndDateCalendar(TimeInterval.Week, startDayTimeInMillis)[Calendar.DAY_OF_MONTH])
        Assert.assertEquals(0, calendar.getEndDateCalendar(TimeInterval.Week, startDayTimeInMillis)[Calendar.MONTH])
        Assert.assertEquals(20, calendar.getEndDateCalendar(TimeInterval.Week, startDayTimeInMillis)[Calendar.MINUTE])
        Assert.assertEquals(2021, calendar.getEndDateCalendar(TimeInterval.Week, startDayTimeInMillis)[Calendar.YEAR])


    }

    @Test
    fun selectedTimeInterval_Month() {
        //06-02 05:05
        Assert.assertEquals(1, calendar.getEndDateCalendar(TimeInterval.Month, startDayTimeInMillis)[Calendar.DAY_OF_MONTH])
        Assert.assertEquals(3, calendar.getEndDateCalendar(TimeInterval.Month, startDayTimeInMillis)[Calendar.MONTH])
        Assert.assertEquals(20, calendar.getEndDateCalendar(TimeInterval.Month, startDayTimeInMillis)[Calendar.MINUTE])

        //06-02 05:25
        calendar[Calendar.MINUTE] = 25
        Assert.assertEquals(1, calendar.getEndDateCalendar(TimeInterval.Month, startDayTimeInMillis)[Calendar.DAY_OF_MONTH])
        Assert.assertEquals(3, calendar.getEndDateCalendar(TimeInterval.Month, startDayTimeInMillis)[Calendar.MONTH])
        Assert.assertEquals(20, calendar.getEndDateCalendar(TimeInterval.Month, startDayTimeInMillis)[Calendar.MINUTE])

        //01-02 05:05
        calendar[Calendar.DAY_OF_MONTH] = 1
        calendar[Calendar.MINUTE] = 5
        Assert.assertEquals(1, calendar.getEndDateCalendar(TimeInterval.Month, startDayTimeInMillis)[Calendar.DAY_OF_MONTH])
        Assert.assertEquals(2, calendar.getEndDateCalendar(TimeInterval.Month, startDayTimeInMillis)[Calendar.MONTH])
        Assert.assertEquals(20, calendar.getEndDateCalendar(TimeInterval.Month, startDayTimeInMillis)[Calendar.MINUTE])

        //01-02 05:25
        calendar[Calendar.MINUTE] = 25
        Assert.assertEquals(1, calendar.getEndDateCalendar(TimeInterval.Month, startDayTimeInMillis)[Calendar.DAY_OF_MONTH])
        Assert.assertEquals(3, calendar.getEndDateCalendar(TimeInterval.Month, startDayTimeInMillis)[Calendar.MONTH])
        Assert.assertEquals(20, calendar.getEndDateCalendar(TimeInterval.Month, startDayTimeInMillis)[Calendar.MINUTE])
    }
}