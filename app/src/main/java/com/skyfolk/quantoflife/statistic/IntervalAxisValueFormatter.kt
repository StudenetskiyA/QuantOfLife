package com.skyfolk.quantoflife.statistic

import com.github.mikephil.charting.formatter.ValueFormatter
import com.skyfolk.quantoflife.QLog
import com.skyfolk.quantoflife.settings.SettingsInteractor
import com.skyfolk.quantoflife.timeInterval.TimeInterval
import com.skyfolk.quantoflife.utils.*

class IntervalAxisValueFormatter(
    private val firstDate: Long,
    private val timeInterval: TimeInterval,
    private val settingsInteractor: SettingsInteractor
) : ValueFormatter() {
    override fun getFormattedValue(value: Float): String {
        val day: Long = 24 * 60 * 60 * 1000
        val week: Long = day * 7
        //TODO
        val month: Long = day * 31
        val period = when (timeInterval) {
            is TimeInterval.Week -> week
            is TimeInterval.Month -> month
            is TimeInterval.Today -> day
            else -> week
        }

        val startFirstPeriodTimeInMillis =
            firstDate.toCalendar().getStartDateCalendar(timeInterval, settingsInteractor.startDayTime).timeInMillis

        val time: Long = startFirstPeriodTimeInMillis + period * value.toInt()

        return when (timeInterval) {
            TimeInterval.Month -> time.toMonthAndYear()
            TimeInterval.Today -> time.toMediumDate()
            else -> "c ${time.toMediumDate()}"
        }
    }
}
