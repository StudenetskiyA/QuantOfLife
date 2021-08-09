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
import com.skyfolk.quantoflife.utils.getEndDateCalendar
import com.skyfolk.quantoflife.settings.SettingsInteractor
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

    fun setSelectedEventFilter(itemId: String?, itemId2: String?) {
        runSearch(itemId, itemId2)
    }

    private fun getEntries(
        allEvents: ArrayList<EventBase>,
        allQuants: ArrayList<QuantBase>,
        quantFilter: String?,
        lastDate: Long,
        startDayTime: Long,
        timeInterval: TimeInterval = TimeInterval.Week
    ): StatisticFragmentState.EntryAndFirstDate {
        val result = ArrayList<Entry>()
        var resultCount = 0
        val allFilteredEvents = allEvents.filter {
            if (quantFilter != null) {
                it.quantId == quantFilter
            } else {
                true
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

            val totalByPeriod = getTotal(
                allQuants,
                filteredEvents
            )

            result.add(BarEntry((resultCount).toFloat(), totalByPeriod.toFloat()))
            resultCount++
            currentPeriodStart = currentPeriodEnd + 1
        }

        return StatisticFragmentState.EntryAndFirstDate(result, firstDate)
    }

    private fun runSearch(onlyQuant: String? = null, onlyQuant2: String? = null) {
        QLog.d("skyfolk-statistic","onlyQuant = $onlyQuant")
        viewModelScope.launch {
            val result = getEntries(
                allEvents = eventsStorageInteractor.getAllEvents(),
                allQuants = quantsStorageInteractor.getAllQuantsList(false),
                quantFilter = onlyQuant,
                lastDate = dateTimeRepository.getTimeInMillis(),
                startDayTime = settingsInteractor.getStartDayTime(),
                timeInterval = TimeInterval.Month
            )
            val result2 = getEntries(
                allEvents = eventsStorageInteractor.getAllEvents(),
                allQuants = quantsStorageInteractor.getAllQuantsList(false),
                quantFilter = onlyQuant2,
                lastDate = dateTimeRepository.getTimeInMillis(),
                startDayTime = settingsInteractor.getStartDayTime(),
                timeInterval = TimeInterval.Month
            )

            _barEntryData.value = StatisticFragmentState.Entries(
                listOf(result,result2)
            )
        }
    }


    fun getQuantIdByName(name: String): String? {
        return quantsStorageInteractor.getQuantByName(name)?.id
    }
}

sealed class StatisticFragmentState {
    class Entries(val entries: List<EntryAndFirstDate>) : StatisticFragmentState()
    data class EntryAndFirstDate(
        val entry: ArrayList<Entry>,
        var firstDate: Long
    )

    object Loading : StatisticFragmentState()
}

