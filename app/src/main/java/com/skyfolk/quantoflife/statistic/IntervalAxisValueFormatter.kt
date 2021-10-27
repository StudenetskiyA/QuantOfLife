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

        val time: Long = timeInterval.getPeriod(firstDate, value.toInt(), settingsInteractor.startDayTime).start

        return when (timeInterval) {
            TimeInterval.Month -> time.toMonthAndYear()
            TimeInterval.Today -> time.toMediumDate()
            else -> "c ${time.toMediumDate()}"
        }
    }
}
