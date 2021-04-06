package com.skyfolk.quantoflife

import java.util.*

interface IDateTimeRepository {
    fun getTimeInMillis() : Long
    fun getCalendar() : Calendar
}

class DateTimeRepository : IDateTimeRepository {
    override fun getTimeInMillis(): Long {
        return System.currentTimeMillis()
    }

    override fun getCalendar(): Calendar {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = getTimeInMillis()
        return calendar
    }
}