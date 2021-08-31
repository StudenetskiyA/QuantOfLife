package com.skyfolk.quantoflife

import android.content.SharedPreferences
import com.skyfolk.quantoflife.settings.SettingsInteractor
import com.skyfolk.quantoflife.statistic.IntervalAxisValueFormatter
import com.skyfolk.quantoflife.timeInterval.TimeInterval
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.util.*
import com.nhaarman.mockitokotlin2.*

class ValueFormaterTest {
    private val calendar = Calendar.getInstance()
    private val startDayTimeInMillis = ((5 * 60 * 60 * 1000) + (20 * 60 * 1000)).toLong() //05:20
    private val preferences: SharedPreferences = mock()
    private val settingsInteractor = SettingsInteractor(preferences)

    @Before
    fun prepare() {

        whenever(preferences.getLong(eq("start_day_time"), any())).thenReturn(startDayTimeInMillis)
    }

    init {
        calendar[Calendar.YEAR] = 2021
        calendar[Calendar.MONTH] = 0
        calendar[Calendar.DAY_OF_MONTH] = 14
        calendar[Calendar.HOUR_OF_DAY] = 5
        calendar[Calendar.MINUTE] = 5
    }

    @Test
    fun monthFormatterSimpleTest() {
        val formatter = IntervalAxisValueFormatter(calendar.timeInMillis, TimeInterval.Month, settingsInteractor)
        Assert.assertEquals("01.01 - 31.01",formatter.getFormattedValue(0f))
    }

    @Test
    fun nearEndPeriodTest() {
        calendar[Calendar.YEAR] = 2021
        calendar[Calendar.MONTH] = 0
        calendar[Calendar.DAY_OF_MONTH] = 1
        calendar[Calendar.HOUR_OF_DAY] = 0
        calendar[Calendar.MINUTE] = 57
        val formatter = IntervalAxisValueFormatter(calendar.timeInMillis, TimeInterval.Month, settingsInteractor)

        Assert.assertEquals("01.12 - 31.12",formatter.getFormattedValue(0f))
    }

    @Test
    fun weekFormatterSimpleTest() {
        val formatter = IntervalAxisValueFormatter(calendar.timeInMillis, TimeInterval.Week, settingsInteractor)
        Assert.assertEquals("11.01 - 17.01",formatter.getFormattedValue(0f))


    }

    @Test
    fun weekFormaterNearEndTest() {
        calendar[Calendar.YEAR] = 2021
        calendar[Calendar.MONTH] = 0
        calendar[Calendar.DAY_OF_MONTH] = 1
        calendar[Calendar.HOUR_OF_DAY] = 0
        calendar[Calendar.MINUTE] = 57
        val formatter = IntervalAxisValueFormatter(calendar.timeInMillis, TimeInterval.Week, settingsInteractor)

        Assert.assertEquals("28.12 - 03.01",formatter.getFormattedValue(0f))
    }
}