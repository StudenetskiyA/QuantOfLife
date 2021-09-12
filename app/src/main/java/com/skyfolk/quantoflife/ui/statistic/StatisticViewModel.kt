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
import java.lang.Integer.max
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

    private val _selectedFilter = MutableLiveData<SelectedGraphFilter>().apply {
        value = SelectedGraphFilter(
            timeInterval = settingsInteractor.getSelectedGraphPeriod(),
            measure = settingsInteractor.getSelectedGraphMeasure(),
            filter = settingsInteractor.getSelectedGraphQuant(1),
            filter2 = settingsInteractor.getSelectedGraphQuant(2),
            listOfQuants = quantsStorageInteractor.getAllQuantsList(false)
                .filterIsInstance<QuantBase.QuantRated>()
                .filter { it.usageCount > 9 }
        )
    }
    val selectedFilter: LiveData<SelectedGraphFilter> = _selectedFilter

    fun setEventFilter(position: Int, filter: QuantFilter) {
        settingsInteractor.writeSelectedGraphQuant(position, filter)
        when (position) {
            1 -> _selectedFilter.value = _selectedFilter.value?.copy(filter = filter)
            2 -> _selectedFilter.value = _selectedFilter.value?.copy(filter2 = filter)
        }
    }

    fun setMeasureFilter(measure: Measure) {
        settingsInteractor.writeSelectedGraphMeasure(measure)
         _selectedFilter.value = _selectedFilter.value?.copy(measure = measure)
    }

    fun setTimeIntervalFilter(timeInterval: TimeInterval) {
        settingsInteractor.writeSelectedGraphPeriod(timeInterval)
        _selectedFilter.value = _selectedFilter.value?.copy(timeInterval = timeInterval)
    }

    private fun getEntries(
        allEvents: ArrayList<EventBase>,
        allQuants: ArrayList<QuantBase>,
        quantFilter: QuantFilter?,
        lastDate: Long,
        startDayTime: Long,
        timeInterval: TimeInterval = TimeInterval.Week,
        measure: Measure
    ): StatisticFragmentState.EntriesAndFirstDate {
        val result = ArrayList<Entry>()
        var resultCount = 0
        val allFilteredEvents = allEvents.filter {
            when (quantFilter) {
                QuantFilter.All -> true
                QuantFilter.Nothing -> false
                is QuantFilter.OnlySelected -> it.quantId == getQuantIdByName(quantFilter.selectQuant)
                else -> true
            }
        }

        val firstDate = if (allEvents.isNotEmpty()) allEvents.first().date else lastDate

        var currentPeriodStart = firstDate
        var currentPeriodEnd = firstDate

        var maximumWith = 0
        var totalMaximumWith = 0
        var lastPeriodWith = false
        var maximumWithStartTime: Long = 0
        var totalMaximumWithStartTime: Long = 0
        var maximumWithout = 0
        var totalMaximumWithout = 0
        var lastPeriodWithout = false
        var maximumWithoutStartTime: Long = 0
        var totalMaximumWithoutStartTime: Long = 0

        while (currentPeriodEnd <= lastDate) {
            currentPeriodEnd = currentPeriodStart.toCalendar().getEndDateCalendar(
                timeInterval,
                startDayTime
            ).timeInMillis
            val filteredEvents =
                allFilteredEvents.filter { it.date in currentPeriodStart until currentPeriodEnd }

            when (filteredEvents.isEmpty()) {
                true -> {
                    when (lastPeriodWithout) {
                        true -> {
                            maximumWithout++
                        }
                        false -> {
                            maximumWithout = 1
                            maximumWithoutStartTime = currentPeriodStart
                        }
                    }
                    if (maximumWithout > totalMaximumWithout) {
                        totalMaximumWithoutStartTime = maximumWithoutStartTime
                    }
                    totalMaximumWithout = max(maximumWithout, totalMaximumWithout)
                    lastPeriodWith = false
                    lastPeriodWithout = true
                }
                false -> {
                    when (lastPeriodWith) {
                        true -> {
                            maximumWith++
                        }
                        false -> {
                            maximumWith = 1
                            maximumWithStartTime = currentPeriodStart
                        }
                    }
                    if (maximumWith > totalMaximumWith) {
                        totalMaximumWithStartTime = maximumWithStartTime
                    }
                    totalMaximumWith = max(maximumWith, totalMaximumWith)
                    lastPeriodWithout = false
                    lastPeriodWith = true
                }
            }

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

        val name = when (quantFilter) {
            QuantFilter.All -> "Все события"
            QuantFilter.Nothing -> "Ничего"
            is QuantFilter.OnlySelected -> quantFilter.selectQuant
            else -> "Все события"
        }

        QLog.d("skyfolk-name","name = $name")

        return StatisticFragmentState.EntriesAndFirstDate(
            name = name,
            entries = result,
            firstDate = firstDate,
            maximumWith = StatisticFragmentState.MaximumContinuously(totalMaximumWith, totalMaximumWithStartTime),
            maximumWithout = StatisticFragmentState.MaximumContinuously(totalMaximumWithout, totalMaximumWithoutStartTime)
        )
    }

    fun runSearch() {
        _barEntryData.value = StatisticFragmentState.Loading

        val onlyQuant = _selectedFilter.value?.filter
        val onlyQuant2 = _selectedFilter.value?.filter2
        val timeInterval = _selectedFilter.value?.timeInterval
        val measure = _selectedFilter.value?.measure

        QLog.d("skyfolk-graph", "run search with ${onlyQuant}, ${onlyQuant2}, ${timeInterval}, ${measure}")
        viewModelScope.launch {
            val result: ArrayList<StatisticFragmentState.EntriesAndFirstDate> = arrayListOf()
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
    class Entries(val entries: ArrayList<EntriesAndFirstDate>) : StatisticFragmentState()
    data class EntriesAndFirstDate(
        val name: String,
        val entries: ArrayList<Entry>,
        var firstDate: Long,
        val maximumWith: MaximumContinuously,
        val maximumWithout: MaximumContinuously
    )

    data class MaximumContinuously(val lenght: Int, val startDate: Long)

    object Loading : StatisticFragmentState()
}

data class SelectedGraphFilter(
    var timeInterval: TimeInterval,
    var measure: Measure,
    var filter: QuantFilter,
    var filter2: QuantFilter,
    var listOfQuants: List<QuantBase>
)

