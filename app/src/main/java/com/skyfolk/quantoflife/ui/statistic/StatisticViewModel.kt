package com.skyfolk.quantoflife.ui.statistic

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.skyfolk.quantoflife.DateTimeRepository
import com.skyfolk.quantoflife.IDateTimeRepository
import com.skyfolk.quantoflife.QLog
import com.skyfolk.quantoflife.db.EventsStorageInteractor
import com.skyfolk.quantoflife.db.IQuantsStorageInteractor
import com.skyfolk.quantoflife.entity.QuantBase
import com.skyfolk.quantoflife.feeds.getTotal
import com.skyfolk.quantoflife.utils.getEndDateCalendar
import com.skyfolk.quantoflife.settings.SettingsInteractor
import com.skyfolk.quantoflife.ui.feeds.FeedsFragmentState
import com.skyfolk.quantoflife.ui.feeds.TimeInterval
import com.skyfolk.quantoflife.utils.*
import kotlin.collections.ArrayList

class StatisticViewModel(
    private val eventsStorageInteractor: EventsStorageInteractor,
    private val quantsStorageInteractor: IQuantsStorageInteractor,
    private val settingsInteractor: SettingsInteractor,
    private val dateTimeRepository: IDateTimeRepository,
) : ViewModel() {
    private val _barEntryData = MutableLiveData<EntryAndFirstDate?>().apply {
        value = getBarEntryData()
    }
    val barEntryData: LiveData<EntryAndFirstDate?> = _barEntryData

    private val _listOfQuants = MutableLiveData<ArrayList<QuantBase>>().apply {
        value = quantsStorageInteractor.getAllQuantsList(false)
    }
    val listOfQuants: LiveData<ArrayList<QuantBase>> = _listOfQuants

    private fun getBarEntryData(): EntryAndFirstDate? {
        return runSearch(null)
    }

    fun setSelectedEventFilter(itemId: String?) {
        runSearch(itemId)?.let {

            _barEntryData.value = it
        }
    }

    fun runSearch(onlyQuant: String?): EntryAndFirstDate? {
        val result = ArrayList<Entry>()
        var resultCount = 0
        val allEvents = eventsStorageInteractor.getAllEvents().filter {
            if (onlyQuant != null) {
                it.quantId == onlyQuant
            } else {
                true
            }
        }

        if (allEvents.firstOrNull() == null) return null

        QLog.d("skyfolk-statistic","total count = ${allEvents.size}")

        val firstDate = allEvents.first().date
        val lastDate = dateTimeRepository.getTimeInMillis()

        var currentPeriodStart = firstDate
        var currentPeriodEnd = firstDate

        while (currentPeriodEnd <= lastDate) {
            currentPeriodEnd = currentPeriodStart.toCalendar().getEndDateCalendar(
                TimeInterval.Week,
                settingsInteractor.getStartDayTime()
            ).timeInMillis
            val filteredEvents =   allEvents.filter { it.date in currentPeriodStart until currentPeriodEnd }

            val totalByPeriod = getTotal(
                quantsStorageInteractor,
                filteredEvents
              )
            QLog.d("skyfolk-statistic","count = ${filteredEvents.size}, total = ${totalByPeriod}")

            result.add(BarEntry((resultCount).toFloat(), totalByPeriod.toFloat()))
            resultCount++
            currentPeriodStart = currentPeriodEnd + 1
        }

        // QLog.d("skyfolk-statistic", "firstDate = $firstDate is ${firstDate.toShortDate()}")

        return EntryAndFirstDate(result, firstDate)
    }

    fun getQuantIdByName(name: String): String? {
        return quantsStorageInteractor.getQuantByName(name)?.id
    }
}

data class EntryAndFirstDate(
    val entry: ArrayList<Entry>,
    val firstDate: Long
)