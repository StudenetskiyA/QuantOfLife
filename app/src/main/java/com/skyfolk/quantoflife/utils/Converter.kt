package com.skyfolk.quantoflife.utils

import com.skyfolk.quantoflife.QLog
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*

fun Long.toDate() : String {
    return DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)
        .withLocale(Locale("ru"))
        .format(
            ZonedDateTime.ofInstant(
                Instant.ofEpochMilli(this),
                ZoneId.systemDefault()))
}

fun Long.toCalendar() : Calendar {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = this
    val hour: Int = (this/(60*60*1000)).toInt()
    val minute: Int = (this/(60*1000)%60).toInt()
    QLog.d("hour = $hour, minute = $minute")

    calendar[Calendar.HOUR_OF_DAY] = hour
    calendar[Calendar.MINUTE] = minute

    return calendar
}