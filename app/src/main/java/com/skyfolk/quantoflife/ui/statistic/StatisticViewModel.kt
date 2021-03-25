package com.skyfolk.quantoflife.ui.statistic

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.skyfolk.quantoflife.QLog
import com.skyfolk.quantoflife.db.EventsStorageInteractor
import com.skyfolk.quantoflife.db.IQuantsStorageInteractor
import com.skyfolk.quantoflife.feeds.getTotal
import com.skyfolk.quantoflife.utils.getEndDateCalendar
import com.skyfolk.quantoflife.settings.SettingsInteractor
import com.skyfolk.quantoflife.ui.feeds.TimeInterval
import com.skyfolk.quantoflife.utils.*
import kotlin.collections.ArrayList

class StatisticViewModel(
    private val eventsStorageInteractor: EventsStorageInteractor,
    private val quantsStorageInteractor: IQuantsStorageInteractor,
    private val settingsInteractor: SettingsInteractor
) : ViewModel() {
    private val _barEntryData = MutableLiveData<EntryAndFirstDate?>().apply {
        value = getBarEntryData()
    }
    val barEntryData: LiveData<EntryAndFirstDate?> = _barEntryData

    private fun getBarEntryData() : EntryAndFirstDate? {
        val result = ArrayList<Entry>()
        var resultCount = 0
        val allEvents = eventsStorageInteractor.getAllEvents()

        if (allEvents.firstOrNull() == null) return null

        val firstDate = allEvents.first().date
        val lastDate = System.currentTimeMillis()

        var currentPeriodStart = firstDate
        var currentPeriodEnd = firstDate

        while (currentPeriodEnd <= lastDate) {
            currentPeriodEnd = currentPeriodStart.toCalendar().getEndDateCalendar(TimeInterval.Week, settingsInteractor.getStartDayTime()).timeInMillis
            val totalByPeriod = getTotal(quantsStorageInteractor, allEvents.filter { it.date in currentPeriodStart until currentPeriodEnd })
          //  QLog.d("skyfolk-statistic","period delta = $currentPeriodEnd is ${currentPeriodEnd.toShortDate()}")
            //result.add(BarEntry((currentPeriodEnd/1000).toFloat(), totalByPeriod.toFloat()))
            result.add(BarEntry((resultCount).toFloat(), totalByPeriod.toFloat()))
            resultCount++
            currentPeriodStart = currentPeriodEnd + 1
        }
        QLog.d("skyfolk-statistic","firstDate = $firstDate is ${firstDate.toShortDate()}")

        return EntryAndFirstDate(result,firstDate)
    }
}

data class EntryAndFirstDate(
    val entry: ArrayList<Entry>,
    val firstDate: Long
)