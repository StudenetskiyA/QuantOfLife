package com.skyfolk.quantoflife

import com.google.android.material.snackbar.Snackbar
import com.skyfolk.quantoflife.entity.QuantBase
import com.skyfolk.quantoflife.ui.feeds.TimeInterval
import com.skyfolk.quantoflife.utils.toCalendar
import java.util.*
import kotlin.collections.ArrayList

fun Snackbar.setOnHideByTimeout(onTimeout: () -> Unit) {
    this.addCallback(object : Snackbar.Callback() {
        override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
            super.onDismissed(transientBottomBar, event)
            if (event == 2) {
                onTimeout()
            }
        }
    })
}

fun ArrayList<QuantBase>.filterToArrayList(predicate: (QuantBase) -> Boolean): ArrayList<QuantBase> {
    return ArrayList(this.filter(predicate))
}

fun Calendar.lessHourAndMinute(calendar: Calendar) : Boolean {
    if ((this[Calendar.HOUR_OF_DAY] < calendar[Calendar.HOUR_OF_DAY]) ||
        (this[Calendar.HOUR_OF_DAY] == calendar[Calendar.HOUR_OF_DAY] && this[Calendar.MINUTE] < calendar[Calendar.MINUTE])) {
        return true
    }
    return false
}

fun Calendar.moreHourAndMinute(calendar: Calendar) : Boolean {
    if ((this[Calendar.HOUR_OF_DAY] > calendar[Calendar.HOUR_OF_DAY]) ||
        (this[Calendar.HOUR_OF_DAY] == calendar[Calendar.HOUR_OF_DAY] && this[Calendar.MINUTE] > calendar[Calendar.MINUTE])) {
        return true
    }
    return false
}

//Возвращает начало временного интервала timeInterval,
//учитывая, что сутки начинаются в startDayTime миллисекунд, а не в полночь
fun Calendar.getStartDateCalendar(timeInterval: TimeInterval, startDayTime: Long) : Calendar {
    val calendar: Calendar = this.clone() as Calendar
    when (timeInterval) {
        TimeInterval.All -> {
            calendar[Calendar.YEAR] = 1900
        }
        TimeInterval.Month -> {
            if (calendar[Calendar.DAY_OF_MONTH]==1 && calendar.lessHourAndMinute(startDayTime.toCalendar())) {
                calendar[Calendar.MONTH] --
            }
            calendar[Calendar.DAY_OF_MONTH] = 1
        }
        TimeInterval.Week -> {
            if (calendar[Calendar.DAY_OF_WEEK]==2 && calendar.lessHourAndMinute(startDayTime.toCalendar())) {
                calendar[Calendar.WEEK_OF_YEAR] --
            }
            calendar[Calendar.DAY_OF_WEEK] = 2
        }
        TimeInterval.Today -> {
            if (calendar.lessHourAndMinute(startDayTime.toCalendar())) {
                calendar[Calendar.DAY_OF_MONTH] --
            }
        }
    }
    calendar[Calendar.HOUR_OF_DAY] = startDayTime.toCalendar()[Calendar.HOUR_OF_DAY]
    calendar[Calendar.MINUTE] = startDayTime.toCalendar()[Calendar.MINUTE]
    calendar[Calendar.SECOND] = 0
    return calendar
}

//Возвращает оконачние временного интервала timeInterval,
//учитывая, что сутки начинаются в startDayTime миллисекунд, а не в полночь
fun Calendar.getEndDateCalendar(timeInterval: TimeInterval, startDayTime: Long) : Calendar {
    val calendar: Calendar = this.clone() as Calendar
    when (timeInterval) {
        TimeInterval.All -> {
            calendar[Calendar.YEAR] = 30900
        }
        TimeInterval.Month -> {
            if (calendar[Calendar.DAY_OF_MONTH]>1 ||
                (calendar[Calendar.DAY_OF_MONTH]==1 && calendar.moreHourAndMinute(startDayTime.toCalendar()))) {
                calendar[Calendar.MONTH] ++
            }
            calendar[Calendar.DAY_OF_MONTH] = 1
        }
        TimeInterval.Week -> {
            if (calendar[Calendar.DAY_OF_WEEK]>2 ||
                (calendar[Calendar.DAY_OF_WEEK]==2 && calendar.moreHourAndMinute(startDayTime.toCalendar()))) {
                calendar[Calendar.WEEK_OF_YEAR] ++
            }
            calendar[Calendar.DAY_OF_WEEK] = 2
        }
        TimeInterval.Today -> {
            if (calendar.moreHourAndMinute(startDayTime.toCalendar())) {
                calendar[Calendar.DAY_OF_MONTH] ++
            }
        }
    }
    calendar[Calendar.HOUR_OF_DAY] = startDayTime.toCalendar()[Calendar.HOUR_OF_DAY]
    calendar[Calendar.MINUTE] = startDayTime.toCalendar()[Calendar.MINUTE]
    calendar[Calendar.SECOND] = 0
    return calendar
}