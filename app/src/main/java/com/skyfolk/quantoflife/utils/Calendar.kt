package com.skyfolk.quantoflife.utils

import java.util.*

fun Calendar.timeInMillis(time: Long) : Calendar {
    this.timeInMillis = time
    return this
}