package com.skyfolk.quantoflife.ui.statistic

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.skyfolk.quantoflife.QLog
import com.skyfolk.quantoflife.db.EventsStorageInteractor
import com.skyfolk.quantoflife.db.IQuantsStorageInteractor
import com.skyfolk.quantoflife.entity.*
import com.skyfolk.quantoflife.settings.SettingsInteractor
import com.skyfolk.quantoflife.statistic.getTotal
import java.util.*
import kotlin.collections.ArrayList

class StatisticViewModel(
    private val eventsStorageInteractor: EventsStorageInteractor,
    private val settingsInteractor: SettingsInteractor,
    private val quantsStorageInteractor: IQuantsStorageInteractor
) : ViewModel() {
    private val _listOfEvents = MutableLiveData<ArrayList<EventBase>>().apply {
        QLog.d("events list loading")
        value = eventsStorageInteractor.getAllEvents()
    }
    val listOfEvents: LiveData<ArrayList<EventBase>> = _listOfEvents

    private val _listOfQuants = MutableLiveData<ArrayList<QuantBase>>().apply {
        value = quantsStorageInteractor.getAllQuantsList(false)
    }
    val listOfQuants: LiveData<ArrayList<QuantBase>> = _listOfQuants

    private val _totalPhysicalFound = MutableLiveData<Double>().apply {
        value = getTotal(quantsStorageInteractor, eventsStorageInteractor.getAllEvents(), QuantCategory.Physical)
    }
    val totalPhysicalFound: LiveData<Double> = _totalPhysicalFound

    private val _totalEmotionalFound = MutableLiveData<Double>().apply {
        value = getTotal(quantsStorageInteractor, eventsStorageInteractor.getAllEvents(), QuantCategory.Emotion)
    }
    val totalEmotionalFound: LiveData<Double> = _totalEmotionalFound

    private val _totalEvolutionFound = MutableLiveData<Double>().apply {
        value = getTotal(quantsStorageInteractor, eventsStorageInteractor.getAllEvents(), QuantCategory.Emotion)
    }
    val totalEvolutionFound: LiveData<Double> = _totalEvolutionFound

    private val _totalFound = MutableLiveData<Double>().apply {
        value = getTotal(quantsStorageInteractor, eventsStorageInteractor.getAllEvents())
    }
    val totalFound: LiveData<Double> = _totalFound

    val selectedTimeInterval: TimeInterval = TimeInterval.toTimeInterval(settingsInteractor.getStatisticTimeIntervalSelectedElement())

    private var selectedCalendar = Calendar.getInstance()
    private var selectedEventFilter: String? = null

    init {
        setSelectedTimeInterval(TimeInterval.toTimeInterval(settingsInteractor.getStatisticTimeIntervalSelectedElement()))
        runSearch()
    }

    fun runSearch() {
        setSelectedTimeInterval(TimeInterval.toTimeInterval(settingsInteractor.getStatisticTimeIntervalSelectedElement()))
        val startDate = selectedCalendar.timeInMillis
        val endDate = System.currentTimeMillis()

        var resultList = ArrayList(
            eventsStorageInteractor.getAllEvents().filter { it.date in startDate until endDate })
        if (selectedEventFilter != null) resultList = ArrayList(resultList.filter { it.quantId == selectedEventFilter })

        setListOfEventsValue(resultList)
    }

    fun clearSearch() {
    }

    private fun setListOfEventsValue(value: ArrayList<EventBase>) {
        _listOfEvents.value = value
        _totalPhysicalFound.value = getTotal(quantsStorageInteractor, value, QuantCategory.Physical)
        _totalEmotionalFound.value = getTotal(quantsStorageInteractor, value, QuantCategory.Emotion)
        _totalEvolutionFound.value = getTotal(quantsStorageInteractor, value, QuantCategory.Evolution)
        _totalFound.value = getTotal(quantsStorageInteractor, value)
    }

    private fun setSelectedTimeInterval(timeInterval: TimeInterval) {
        selectedCalendar = Calendar.getInstance()
        when (timeInterval) {
            TimeInterval.All -> {
                selectedCalendar[Calendar.YEAR] = 1900
            }
            TimeInterval.Month -> {
                selectedCalendar[Calendar.DAY_OF_MONTH] = 1
                selectedCalendar[Calendar.MINUTE] = 0
                selectedCalendar[Calendar.SECOND] = 0
            }
            TimeInterval.Week -> {
                selectedCalendar[Calendar.DAY_OF_WEEK] = 2
                selectedCalendar[Calendar.MINUTE] = 0
                selectedCalendar[Calendar.SECOND] = 0
            }
            TimeInterval.Today -> {
                selectedCalendar[Calendar.HOUR_OF_DAY] = 0
                selectedCalendar[Calendar.MINUTE] = 0
                selectedCalendar[Calendar.SECOND] = 0
            }
        }
    }

    fun saveTimeIntervalState(timeInterval: TimeInterval) {
      settingsInteractor.writeStatisticTimeIntervalSelectedElement(timeInterval.name)
    }

    fun setSelectedEventFilter(item: String?) {
        selectedEventFilter = item
    }
}

enum class TimeInterval {
    Today, Week, Month, All;

    companion object {
        fun toTimeInterval(enumString: String) : TimeInterval {
            return try {
                valueOf(enumString)
            } catch (ex: Exception) {
                All
            }
        }
    }
}