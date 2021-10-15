package com.skyfolk.quantoflife.ui.statistic

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.mikephil.charting.data.BarEntry
import com.skyfolk.quantoflife.GraphSelectedYear
import com.skyfolk.quantoflife.IDateTimeRepository
import com.skyfolk.quantoflife.QLog
import com.skyfolk.quantoflife.db.EventsStorageInteractor
import com.skyfolk.quantoflife.db.IQuantsStorageInteractor
import com.skyfolk.quantoflife.entity.EventBase
import com.skyfolk.quantoflife.entity.QuantBase
import com.skyfolk.quantoflife.entity.QuantCategory
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
import java.util.*
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
        QLog.d(
            "skyfolk-settings",
            "get interval mutable = ${settingsInteractor.selectedTimeInterval}"
        )
        value = SelectedGraphFilter(
            selectedYear = settingsInteractor.selectedYearFilter,
            timeInterval = settingsInteractor.selectedTimeInterval,
            measure = settingsInteractor.selectedGraphMeasure,
            filter = settingsInteractor.selectedGraphQuantFirst,
            filter2 = settingsInteractor.selectedGraphQuantSecond,
            listOfQuants = quantsStorageInteractor.getAllQuantsList(false)
                .filterIsInstance<QuantBase.QuantRated>()
                .filter { it.usageCount > 9 },
            listOfYears = eventsStorageInteractor.getAllEventsYears(settingsInteractor.startDayTime)
        )
    }
    val selectedFilter: LiveData<SelectedGraphFilter> = _selectedFilter

    fun setEventFilter(position: Int, filter: QuantFilter) {
        when (position) {
            1 -> {
                settingsInteractor.selectedGraphQuantFirst = filter
                _selectedFilter.value = _selectedFilter.value?.copy(filter = filter)
            }
            2 -> {
                settingsInteractor.selectedGraphQuantSecond = filter
                _selectedFilter.value = _selectedFilter.value?.copy(filter2 = filter)
            }
        }
    }

    fun setYearFilter(filter: GraphSelectedYear) {
        settingsInteractor.selectedYearFilter = filter
        _selectedFilter.value = _selectedFilter.value?.copy(selectedYear = filter)
    }

    fun setMeasureFilter(measure: Measure) {
        settingsInteractor.selectedGraphMeasure = measure
        _selectedFilter.value = _selectedFilter.value?.copy(measure = measure)
    }

    fun setTimeIntervalFilter(timeInterval: TimeInterval) {
        QLog.d(
            "skyfolk-settings",
            "setTimeIntervalFilter = ${timeInterval}"
        )
        settingsInteractor.selectedTimeInterval = timeInterval
        _selectedFilter.value = _selectedFilter.value?.copy(timeInterval = timeInterval)
    }

    private fun getEntries(
        selectedYear: GraphSelectedYear,
        allEvents: ArrayList<EventBase>,
        allQuants: ArrayList<QuantBase>,
        quantFilter: QuantFilter?,
        startDayTime: Long,
        timeInterval: TimeInterval = TimeInterval.Week,
        measure: Measure
    ): StatisticFragmentState.EntriesAndFirstDate {
        QLog.d(
            "skyfolk-graph",
            "run search with ${selectedYear}, ${timeInterval}, ${measure}"
        )
        val result = ArrayList<BarEntry>()
        var resultCount = 0

        var lastDate = when (selectedYear) {
            GraphSelectedYear.All -> dateTimeRepository.getTimeInMillis()
            is GraphSelectedYear.OnlyYear -> {
                val calendar = dateTimeRepository.getCalendar()
                calendar[Calendar.YEAR] = selectedYear.year
                calendar
                    .getEndDateCalendar(TimeInterval.Year, settingsInteractor.startDayTime)
                    .timeInMillis
            }
        }
        lastDate = Math.min(lastDate, dateTimeRepository.getTimeInMillis())

        val firstDate = when (selectedYear) {
            GraphSelectedYear.All ->  if (allEvents.isNotEmpty()) allEvents.first().date else lastDate
            is GraphSelectedYear.OnlyYear -> {
                val calendar = dateTimeRepository.getCalendar()
                calendar[Calendar.YEAR] = selectedYear.year
                calendar
                    .getStartDateCalendar(TimeInterval.Year, settingsInteractor.startDayTime)
                    .timeInMillis
            }
        }

        val allFilteredEvents = allEvents.filter {
            when (quantFilter) {
                QuantFilter.All -> true
                QuantFilter.Nothing -> false
                is QuantFilter.OnlySelected -> it.quantId == getQuantIdByName(quantFilter.selectQuant)
                else -> true
            }
        }.filter {
            when (selectedYear) {
                GraphSelectedYear.All -> true
                is GraphSelectedYear.OnlyYear -> {
                    val calendar = Calendar.getInstance()
                    calendar[Calendar.YEAR] = selectedYear.year

                    val startYear = calendar
                        .getStartDateCalendar(
                            TimeInterval.Year,
                            settingsInteractor.startDayTime
                        )
                        .timeInMillis

                    val endYear = calendar
                        .getEndDateCalendar(TimeInterval.Year, settingsInteractor.startDayTime)
                        .timeInMillis

                    it.date in startYear until endYear
                }
            }
        }

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

        while (currentPeriodEnd < lastDate) {

            currentPeriodEnd = currentPeriodStart.toCalendar().getEndDateCalendar(
                timeInterval,
                startDayTime
            ).timeInMillis

            val filteredEvents =
                allFilteredEvents.filter { it.date in currentPeriodStart until currentPeriodEnd }

//            QLog.d("skyfolk-graph",
//                "from ${currentPeriodStart} to ${currentPeriodEnd}, " +
//                        "lastDate = ${lastDate}")

            val allEventsInPeriod =
                allEvents.filter { it.date in currentPeriodStart until currentPeriodEnd }

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
                Measure.TotalPhysical -> {
                    getTotal(
                        allQuants,
                        filteredEvents,
                        QuantCategory.Physical
                    )
                }
                Measure.TotalEmotional -> {
                    getTotal(
                        allQuants,
                        filteredEvents,
                        QuantCategory.Emotion
                    )
                }
                Measure.TotalEvolution -> {
                    getTotal(
                        allQuants,
                        filteredEvents,
                        QuantCategory.Evolution
                    )
                }
                Measure.AverageRating ->
                    getTotalAverageStar(
                        allQuants,
                        filteredEvents
                    )
                Measure.Quantity ->
                    getTotalCount(filteredEvents)
            }

            when (allEventsInPeriod.isNotEmpty()) {
                true -> {
                    result.add(BarEntry((resultCount).toFloat(), totalByPeriod.toFloat()))
                }
            }

            resultCount++
            currentPeriodStart = currentPeriodEnd + 1
        }

        val name = when (quantFilter) {
            QuantFilter.All -> "Все события"
            QuantFilter.Nothing -> "Ничего"
            is QuantFilter.OnlySelected -> quantFilter.selectQuant
            else -> "Все события"
        }

        return StatisticFragmentState.EntriesAndFirstDate(
            name = name,
            entries = result,
            firstDate = firstDate,
            maximumWith = StatisticFragmentState.MaximumContinuously(
                totalMaximumWith,
                totalMaximumWithStartTime
            ),
            maximumWithout = StatisticFragmentState.MaximumContinuously(
                totalMaximumWithout,
                totalMaximumWithoutStartTime
            )
        )
    }

    fun runSearch() {
        _barEntryData.value = StatisticFragmentState.Loading

        val onlyQuant = _selectedFilter.value?.filter
        val onlyQuant2 = _selectedFilter.value?.filter2
        val timeInterval = _selectedFilter.value?.timeInterval
        val measure = _selectedFilter.value?.measure
        val selectedYear = _selectedFilter.value?.selectedYear

        viewModelScope.launch {
            val result: ArrayList<StatisticFragmentState.EntriesAndFirstDate> = arrayListOf()
            if (onlyQuant != QuantFilter.Nothing) {
                result.add(
                    getEntries(
                        selectedYear = selectedYear ?: GraphSelectedYear.All,
                        allEvents = eventsStorageInteractor.getAllEvents(),
                        allQuants = quantsStorageInteractor.getAllQuantsList(false),
                        quantFilter = onlyQuant,
                        startDayTime = settingsInteractor.startDayTime,
                        timeInterval = timeInterval ?: TimeInterval.Week,
                        measure = measure ?: Measure.TotalCount
                    )
                )
            }
            if (onlyQuant2 != QuantFilter.Nothing) {
                result.add(
                    getEntries(
                        selectedYear = selectedYear ?: GraphSelectedYear.All,
                        allEvents = eventsStorageInteractor.getAllEvents(),
                        allQuants = quantsStorageInteractor.getAllQuantsList(false),
                        quantFilter = onlyQuant2,
                        startDayTime = settingsInteractor.startDayTime,
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
        val entries: ArrayList<BarEntry>,
        var firstDate: Long,
        val maximumWith: MaximumContinuously,
        val maximumWithout: MaximumContinuously
    )

    data class MaximumContinuously(val lenght: Int, val startDate: Long)

    object Loading : StatisticFragmentState()
}

data class SelectedGraphFilter(
    var selectedYear: GraphSelectedYear,
    var timeInterval: TimeInterval,
    var measure: Measure,
    var filter: QuantFilter,
    var filter2: QuantFilter,
    var listOfQuants: List<QuantBase>,
    var listOfYears: List<String>
)

