package com.skyfolk.quantoflife.statistic

import com.github.mikephil.charting.formatter.ValueFormatter
import com.skyfolk.quantoflife.settings.SettingsInteractor
import com.skyfolk.quantoflife.timeInterval.TimeInterval
import com.skyfolk.quantoflife.utils.getEndDateCalendar
import com.skyfolk.quantoflife.utils.getStartDateCalendar
import com.skyfolk.quantoflife.utils.toCalendar
import com.skyfolk.quantoflife.utils.toShortDate

class IntervalAxisValueFormatter(
    private val startFirstIntervalTimeInMillis: Long,
    private val timeInterval: TimeInterval,
    private val settingsInteractor: SettingsInteractor
) : ValueFormatter() {
    override fun getFormattedValue(value: Float): String {
        val day: Long = 24 * 60 * 60 * 1000
        val week: Long = day * 7
        val month: Long = day * 31
        val period = when (timeInterval) {
            is TimeInterval.Week -> week
            is TimeInterval.Month -> month
            is TimeInterval.Today -> day
            else -> week
        }

        val time: Long = startFirstIntervalTimeInMillis + period * value.toInt()

        val currentPeriodStart =
            time.toCalendar().getStartDateCalendar(timeInterval, settingsInteractor.getStartDayTime()).timeInMillis
        val currentPeriodEnd =
            time.toCalendar().getEndDateCalendar(timeInterval, settingsInteractor.getStartDayTime()).timeInMillis - day
        return "${currentPeriodStart.toShortDate()} - ${currentPeriodEnd.toShortDate()}"
    }
}
