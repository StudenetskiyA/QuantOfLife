package com.skyfolk.quantoflife.utils

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