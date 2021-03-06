package com.skyfolk.quantoflife.utils

import com.skyfolk.quantoflife.entity.QuantCategory
import com.skyfolk.quantoflife.ui.feeds.TimeInterval
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*

fun Long.toDate(): String {
    return DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)
        .withLocale(Locale("ru"))
        .format(
            ZonedDateTime.ofInstant(
                Instant.ofEpochMilli(this),
                ZoneId.systemDefault()
            )
        )
}

fun Long.toDateWithoutHourAndMinutes(): String {
    return DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).format(
        ZonedDateTime.ofInstant(
            Instant.ofEpochMilli(this),
            ZoneId.systemDefault()
        )
    )
}

fun Long.toShortDate(): String {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = this
    val day =
        if (calendar[Calendar.DAY_OF_MONTH] + 1 < 10) "0${calendar[Calendar.DAY_OF_MONTH]}" else calendar[Calendar.DAY_OF_MONTH]
    val month =
        if (calendar[Calendar.MONTH] + 1 < 10) "0${calendar[Calendar.MONTH] + 1}" else calendar[Calendar.MONTH] + 1
    return "$day:$month"
}

fun Long.toCalendarOnlyHourAndMinute(): Calendar {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = this
    val hour: Int = (this / (60 * 60 * 1000)).toInt()
    val minute: Int = (this / (60 * 1000) % 60).toInt()

    calendar[Calendar.HOUR_OF_DAY] = hour
    calendar[Calendar.MINUTE] = minute

    return calendar
}

fun Long.toCalendar(): Calendar {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = this
    return calendar
}

fun fromPositionToTimeInterval(
    position: Int,
    startIntervalCalendar: Long,
    endIntervalCalendar: Long
): TimeInterval {
    return when (position) {
        0 -> {
            TimeInterval.Today
        }
        1 -> {
            TimeInterval.Week
        }
        2 -> {
            TimeInterval.Month
        }
        3 -> {
            TimeInterval.All
        }
        4 -> {
            TimeInterval.Selected(
                startIntervalCalendar,
                endIntervalCalendar
            )
        }
        else -> TimeInterval.All
    }
}

fun TimeInterval.toPosition(): Int {
    return when (this) {
        is TimeInterval.Today -> 0
        is TimeInterval.Week -> 1
        is TimeInterval.Month -> 2
        is TimeInterval.All -> 3
        is TimeInterval.Selected -> 4
    }
}
