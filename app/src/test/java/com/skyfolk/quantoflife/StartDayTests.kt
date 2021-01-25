package com.skyfolk.quantoflife

import com.skyfolk.quantoflife.ui.statistic.TimeInterval
import org.junit.Assert
import org.junit.Test
import java.util.*

class StartDayTests {
    private val calendar = Calendar.getInstance()
    private val startDayTimeInMillis = ((5 * 60 * 60 * 1000) + (20 * 60 * 1000)).toLong() //05:20

    init {
        calendar[Calendar.YEAR] = 2021
        calendar[Calendar.MONTH] = 0
        calendar[Calendar.DAY_OF_MONTH] = 14
        calendar[Calendar.HOUR_OF_DAY] = 5
        calendar[Calendar.MINUTE] = 5
    }

    @Test
    fun selectedTimeInterval_Day() {
        //05:05
        Assert.assertEquals(calendar[Calendar.DAY_OF_MONTH]-1, calendar.getStartDateCalendar(TimeInterval.Today, startDayTimeInMillis)[Calendar.DAY_OF_MONTH])

        //05:25
        calendar[Calendar.MINUTE] = 25
        Assert.assertEquals(calendar[Calendar.DAY_OF_MONTH], calendar.getStartDateCalendar(TimeInterval.Today, startDayTimeInMillis)[Calendar.DAY_OF_MONTH])
    }

    @Test
    fun selectedTimeInterval_Week() {
        //14-01 05:05
        Assert.assertEquals(11, calendar.getStartDateCalendar(TimeInterval.Week, startDayTimeInMillis)[Calendar.DAY_OF_MONTH])
        Assert.assertEquals(20, calendar.getStartDateCalendar(TimeInterval.Week, startDayTimeInMillis)[Calendar.MINUTE])

        //14-01 05:25
        calendar[Calendar.MINUTE] = 25
        Assert.assertEquals(11, calendar.getStartDateCalendar(TimeInterval.Week, startDayTimeInMillis)[Calendar.DAY_OF_MONTH])
        Assert.assertEquals(20, calendar.getStartDateCalendar(TimeInterval.Week, startDayTimeInMillis)[Calendar.MINUTE])

        //11-01 05:25
        calendar[Calendar.DAY_OF_MONTH] = 11
        Assert.assertEquals(11, calendar.getStartDateCalendar(TimeInterval.Week, startDayTimeInMillis)[Calendar.DAY_OF_MONTH])
        Assert.assertEquals(20, calendar.getStartDateCalendar(TimeInterval.Week, startDayTimeInMillis)[Calendar.MINUTE])

        //11-01 05:05
        calendar[Calendar.DAY_OF_MONTH] = 11
        calendar[Calendar.MINUTE] = 5
        Assert.assertEquals(4, calendar.getStartDateCalendar(TimeInterval.Week, startDayTimeInMillis)[Calendar.DAY_OF_MONTH])
        Assert.assertEquals(20, calendar.getStartDateCalendar(TimeInterval.Week, startDayTimeInMillis)[Calendar.MINUTE])

        //03-01 05:25
        calendar[Calendar.DAY_OF_MONTH] = 3
        calendar[Calendar.MINUTE] = 25
        Assert.assertEquals(28, calendar.getStartDateCalendar(TimeInterval.Week, startDayTimeInMillis)[Calendar.DAY_OF_MONTH])
        Assert.assertEquals(20, calendar.getStartDateCalendar(TimeInterval.Week, startDayTimeInMillis)[Calendar.MINUTE])
        Assert.assertEquals(2020, calendar.getStartDateCalendar(TimeInterval.Week, startDayTimeInMillis)[Calendar.YEAR])
    }

    @Test
    fun selectedTimeInterval_Month() {
        //14-01 05:05
        Assert.assertEquals(1, calendar.getStartDateCalendar(TimeInterval.Month, startDayTimeInMillis)[Calendar.DAY_OF_MONTH])
        Assert.assertEquals(20, calendar.getStartDateCalendar(TimeInterval.Month, startDayTimeInMillis)[Calendar.MINUTE])

        //14-01 05:25
        calendar[Calendar.MINUTE] = 25
        Assert.assertEquals(1, calendar.getStartDateCalendar(TimeInterval.Month, startDayTimeInMillis)[Calendar.DAY_OF_MONTH])
        Assert.assertEquals(20, calendar.getStartDateCalendar(TimeInterval.Month, startDayTimeInMillis)[Calendar.MINUTE])

        //11-01 05:25
        calendar[Calendar.DAY_OF_MONTH] = 11
        Assert.assertEquals(1, calendar.getStartDateCalendar(TimeInterval.Month, startDayTimeInMillis)[Calendar.DAY_OF_MONTH])
        Assert.assertEquals(20, calendar.getStartDateCalendar(TimeInterval.Month, startDayTimeInMillis)[Calendar.MINUTE])

        //11-01 05:05
        calendar[Calendar.DAY_OF_MONTH] = 11
        calendar[Calendar.MINUTE] = 5
        Assert.assertEquals(1, calendar.getStartDateCalendar(TimeInterval.Month, startDayTimeInMillis)[Calendar.DAY_OF_MONTH])
        Assert.assertEquals(20, calendar.getStartDateCalendar(TimeInterval.Month, startDayTimeInMillis)[Calendar.MINUTE])

        //01-01 05:25
        calendar[Calendar.DAY_OF_MONTH] = 1
        calendar[Calendar.MINUTE] = 25
        Assert.assertEquals(1, calendar.getStartDateCalendar(TimeInterval.Month, startDayTimeInMillis)[Calendar.DAY_OF_MONTH])
        Assert.assertEquals(20, calendar.getStartDateCalendar(TimeInterval.Month, startDayTimeInMillis)[Calendar.MINUTE])

        //01-01 05:05
        calendar[Calendar.DAY_OF_MONTH] = 1
        calendar[Calendar.MINUTE] = 5
        Assert.assertEquals(1, calendar.getStartDateCalendar(TimeInterval.Month, startDayTimeInMillis)[Calendar.DAY_OF_MONTH])
        Assert.assertEquals(11, calendar.getStartDateCalendar(TimeInterval.Month, startDayTimeInMillis)[Calendar.MONTH])
        Assert.assertEquals(20, calendar.getStartDateCalendar(TimeInterval.Month, startDayTimeInMillis)[Calendar.MINUTE])
    }
}