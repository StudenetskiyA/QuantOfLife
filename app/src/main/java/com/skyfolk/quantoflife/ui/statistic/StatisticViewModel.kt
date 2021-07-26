package com.skyfolk.quantoflife.ui.statistic

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

    private val _listOfQuants = MutableLiveData<ArrayList<QuantBase>>().apply {
        value = quantsStorageInteractor.getAllQuantsList(false)
    }
    val listOfQuants: LiveData<ArrayList<QuantBase>> = _listOfQuants

    fun setSelectedEventFilter(itemId: String?) {
        runSearch(itemId)
    }

    private fun runSearch(onlyQuant: String?) {
        viewModelScope.launch {
            val result = ArrayList<Entry>()
            var resultCount = 0
            val allEvents = eventsStorageInteractor.getAllEvents().filter {
                if (onlyQuant != null) {
                    it.quantId == onlyQuant
                } else {
                    true
                }
            }

            if (allEvents.firstOrNull() == null) {
                _barEntryData.value = null
            }

            val firstDate = allEvents.first().date
            val lastDate = dateTimeRepository.getTimeInMillis()

            var currentPeriodStart = firstDate
            var currentPeriodEnd = firstDate

            viewModelScope.launch {

            }
            while (currentPeriodEnd <= lastDate) {
                currentPeriodEnd = currentPeriodStart.toCalendar().getEndDateCalendar(
                    TimeInterval.Week,
                    settingsInteractor.getStartDayTime()
                ).timeInMillis
                val filteredEvents =
                    allEvents.filter { it.date in currentPeriodStart until currentPeriodEnd }

                val totalByPeriod = getTotal(
                    quantsStorageInteractor,
                    filteredEvents
                )
                QLog.d(
                    "skyfolk-statistic",
                    "count = ${filteredEvents.size}, total = ${totalByPeriod}"
                )

                result.add(BarEntry((resultCount).toFloat(), totalByPeriod.toFloat()))
                resultCount++
                currentPeriodStart = currentPeriodEnd + 1
            }

            _barEntryData.value = StatisticFragmentState.EntryAndFirstDate(result, firstDate)
        }
    }


    fun getQuantIdByName(name: String): String? {
        return quantsStorageInteractor.getQuantByName(name)?.id
    }
}

sealed class StatisticFragmentState() {
    data class EntryAndFirstDate(
        val entry: ArrayList<Entry>,
        val firstDate: Long
    ): StatisticFragmentState()
    object Loading: StatisticFragmentState()
}

