package com.skyfolk.quantoflife.statistic

import com.github.mikephil.charting.charts.BarLineChartBase
import com.github.mikephil.charting.formatter.ValueFormatter
import com.skyfolk.quantoflife.getEndDateCalendar
import com.skyfolk.quantoflife.getStartDateCalendar
import com.skyfolk.quantoflife.settings.SettingsInteractor
import com.skyfolk.quantoflife.ui.feeds.TimeInterval
import com.skyfolk.quantoflife.utils.toCalendar
import com.skyfolk.quantoflife.utils.toDate
import com.skyfolk.quantoflife.utils.toShortDate

class WeekAxisValueFormatter(private val startFirstWeekTimeInMillis: Long) : ValueFormatter() {
    override fun getFormattedValue(value: Float): String {
        val nWeek = value.toInt()
        val day: Long = 24 * 60 * 60 * 1000
        val week: Long = day * 7

        val time: Long = startFirstWeekTimeInMillis + week * nWeek

        val currentPeriodStart = time.toCalendar().getStartDateCalendar(TimeInterval.Week, 0).timeInMillis
        val currentPeriodEnd = time.toCalendar().getEndDateCalendar(TimeInterval.Week, 0).timeInMillis - day
        return "${currentPeriodStart.toShortDate()} - ${currentPeriodEnd.toShortDate()}"
    }
}
