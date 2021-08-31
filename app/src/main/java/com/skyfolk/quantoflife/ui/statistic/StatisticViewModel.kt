package com.skyfolk.quantoflife.ui.statistic

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.skyfolk.quantoflife.IDateTimeRepository
import com.skyfolk.quantoflife.QLog
import com.skyfolk.quantoflife.db.EventsStorageInteractor
import com.skyfolk.quantoflife.db.IQuantsStorageInteractor
import com.skyfolk.quantoflife.entity.EventBase
import com.skyfolk.quantoflife.entity.QuantBase
import com.skyfolk.quantoflife.feeds.getTotal
import com.skyfolk.quantoflife.feeds.getTotalAverageStar
import com.skyfolk.quantoflife.feeds.getTotalCount
import com.skyfolk.quantoflife.meansure.Measure
import com.skyfolk.quantoflife.meansure.QuantFilter
import com.skyfolk.quantoflife.utils.getEndDateCalendar
import com.skyfolk.quantoflife.settings.SettingsInteractor
import com.skyfolk.quantoflife.statistic.IntervalAxisValueFormatter
import com.skyfolk.quantoflife.timeInterval.TimeInterval
import com.skyfolk.quantoflife.utils.*
import kotlinx.coroutines.launch
import kotlin.collections.ArrayList

class StatisticViewModel(
    private val eventsStorageInteractor: EventsStorageInteractor,
    private val quantsStorageInteractor: IQuantsStorageInteractor,
    private val settingsInteractor: SettingsInteractor,
    private val dateTimeRepository: IDateTimeRepository,
) : ViewModel() {
    private val _barEntryData = MutableLiveData<StatisticFragmentState>().apply {
        value = StatisticFragmentState.Loading
    }
    val barEntryData: LiveData<StatisticFragmentState> = _barEntryData

    private val _listOfQuants = MutableLiveData<List<QuantBase>>().apply {
        value = quantsStorageInteractor.getAllQuantsList(false)
            .filterIsInstance<QuantBase.QuantRated>()
            .filter { it.usageCount > 9 }
    }
    val listOfQuants: LiveData<List<QuantBase>> = _listOfQuants

    private val _selectedTimeInterval = MutableLiveData<TimeInterval>().apply {
        value = settingsInteractor.getSelectedGraphPeriod()
    }
    val selectedTimeInterval: LiveData<TimeInterval> = _selectedTimeInterval
    private val _selectedMeasure = MutableLiveData<Measure>().apply {
        QLog.d("skyfolk-graph","from setting = ${settingsInteractor.getSelectedGraphMeasure()}")
        value = settingsInteractor.getSelectedGraphMeasure()
    }
    val selectedMeasure: LiveData<Measure> = _selectedMeasure
    private val _selectedFirstQuantFilter = MutableLiveData<QuantFilter>().apply {
        value = settingsInteractor.getSelectedGraphQuant(1)
    }
    val selectedFirstQuantFilter: LiveData<QuantFilter> = _selectedFirstQuantFilter
    private val _selectedSecondQuantFilter = MutableLiveData<QuantFilter>().apply {
        value = settingsInteractor.getSelectedGraphQuant(2)
    }
    val selectedSecondQuantFilter: LiveData<QuantFilter> = _selectedSecondQuantFilter

    fun setSelectedEventFilter(
        itemId: QuantFilter?,
        itemId2: QuantFilter?,
        timeInterval: TimeInterval?,
        measure: Measure?
    ) {
        if (itemId !=null && itemId2 != null && timeInterval != null && measure != null) {
            settingsInteractor.apply {
                writeSelectedGraphMeasure(measure)
                QLog.d("skyfolk-graph", "write setting = ${measure}")

                writeSelectedGraphPeriod(timeInterval)
                writeSelectedGraphQuant(1, itemId)
                writeSelectedGraphQuant(2, itemId2)
            }
            runSearch(itemId, itemId2, timeInterval, measure)
        }
    }

    private fun getEntries(
        allEvents: ArrayList<EventBase>,
        allQuants: ArrayList<QuantBase>,
        quantFilter: QuantFilter,
        lastDate: Long,
        startDayTime: Long,
        timeInterval: TimeInterval = TimeInterval.Week,
        measure: Measure
    ): StatisticFragmentState.EntryAndFirstDate {
        val result = ArrayList<Entry>()
        var resultCount = 0
        val allFilteredEvents = allEvents.filter {
            when (quantFilter) {
                QuantFilter.All -> true
                QuantFilter.Nothing -> false
                is QuantFilter.OnlySelected -> it.quantId == getQuantIdByName(quantFilter.selectQuant)
            }
        }

        val firstDate = allEvents.first().date

        var currentPeriodStart = firstDate
        var currentPeriodEnd = firstDate

        while (currentPeriodEnd <= lastDate) {
            currentPeriodEnd = currentPeriodStart.toCalendar().getEndDateCalendar(
                timeInterval,
                startDayTime
            ).timeInMillis
            val filteredEvents =
                allFilteredEvents.filter { it.date in currentPeriodStart until currentPeriodEnd }


            val totalByPeriod = when (measure) {
                Measure.TotalCount ->
                    getTotal(
                        allQuants,
                        filteredEvents
                    )
                Measure.AverageRating ->
                    getTotalAverageStar(
                        allQuants,
                        filteredEvents
                    )
                Measure.Quantity ->
                    getTotalCount(filteredEvents)
            }

            result.add(BarEntry((resultCount).toFloat(), totalByPeriod.toFloat()))
            resultCount++
            currentPeriodStart = currentPeriodEnd + 1
        }

        return StatisticFragmentState.EntryAndFirstDate(result, firstDate)
    }

    private fun runSearch(
        onlyQuant: QuantFilter,
        onlyQuant2: QuantFilter,
        timeInterval: TimeInterval?,
        measure: Measure?
    ) {
        viewModelScope.launch {
            val result: ArrayList<StatisticFragmentState.EntryAndFirstDate> = arrayListOf()
            if (onlyQuant != QuantFilter.Nothing) {
                result.add(
                    getEntries(
                        allEvents = eventsStorageInteractor.getAllEvents(),
                        allQuants = quantsStorageInteractor.getAllQuantsList(false),
                        quantFilter = onlyQuant,
                        lastDate = dateTimeRepository.getTimeInMillis(),
                        startDayTime = settingsInteractor.getStartDayTime(),
                        timeInterval = timeInterval ?: TimeInterval.Week,
                        measure = measure ?: Measure.TotalCount
                    )
                )
            }
            if (onlyQuant2 != QuantFilter.Nothing) {
                result.add(
                    getEntries(
                        allEvents = eventsStorageInteractor.getAllEvents(),
                        allQuants = quantsStorageInteractor.getAllQuantsList(false),
                        quantFilter = onlyQuant2,
                        lastDate = dateTimeRepository.getTimeInMillis(),
                        startDayTime = settingsInteractor.getStartDayTime(),
                        timeInterval = timeInterval ?: TimeInterval.Week,
                        measure = measure ?: Measure.TotalCount
                    )
                )
            }

            _barEntryData.value = StatisticFragmentState.Entries(
                result
            )
        }
    }

    private fun getQuantIdByName(name: String): String? {
        return quantsStorageInteractor.getQuantByName(name)?.id
    }

    fun getFormatter(firstDate: Long, timeInterval: TimeInterval): IntervalAxisValueFormatter {
        return IntervalAxisValueFormatter(firstDate, timeInterval, settingsInteractor)
    }
}

sealed class StatisticFragmentState {
    class Entries(val entries: ArrayList<EntryAndFirstDate>) : StatisticFragmentState()
    data class EntryAndFirstDate(
        val entry: ArrayList<Entry>,
        var firstDate: Long
    )

    object Loading : StatisticFragmentState()
}

