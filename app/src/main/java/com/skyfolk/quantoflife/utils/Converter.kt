package com.skyfolk.quantoflife.utils

import android.util.Log
import com.skyfolk.quantoflife.entity.QuantCategory
import com.skyfolk.quantoflife.timeInterval.TimeInterval
import java.sql.Time
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*
import kotlin.reflect.full.createInstance

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
        if (calendar[Calendar.DAY_OF_MONTH] + 1 <= 10) "0${calendar[Calendar.DAY_OF_MONTH]}" else calendar[Calendar.DAY_OF_MONTH]
    val month =
        if (calendar[Calendar.MONTH] + 1 < 10) "0${calendar[Calendar.MONTH] + 1}" else calendar[Calendar.MONTH] + 1
    return "$day.$month"
}

fun Long.toMonthAndYear(): String {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = this

    val monthNames = arrayOf(
        "Январь",
        "Февраль",
        "Март",
        "Апрель",
        "Май",
        "Июнь",
        "Июль",
        "Август",
        "Сентябрь",
        "Октябрь",
        "Ноябрь",
        "Декабрь"
    )
    val month = monthNames[calendar[Calendar.MONTH]]

    return "$month ${calendar[Calendar.YEAR]}"
}

fun Long.toMediumDate(): String {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = this
    val day =
        if (calendar[Calendar.DAY_OF_MONTH] + 1 <= 10) "0${calendar[Calendar.DAY_OF_MONTH]}" else calendar[Calendar.DAY_OF_MONTH]
    val month =
        if (calendar[Calendar.MONTH] + 1 < 10) "0${calendar[Calendar.MONTH] + 1}" else calendar[Calendar.MONTH] + 1

    return "$day.$month.${calendar[Calendar.YEAR]}"
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
        is TimeInterval.Year -> 5
    }
}

fun Int.fromPositionToTimeInterval(): TimeInterval {
    return when (this) {
        0 -> TimeInterval.Today
        1 -> TimeInterval.Week
        2 -> TimeInterval.Month
        else -> TimeInterval.Week
    }
}
