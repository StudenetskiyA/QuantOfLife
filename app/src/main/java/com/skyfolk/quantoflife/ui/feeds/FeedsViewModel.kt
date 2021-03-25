package com.skyfolk.quantoflife.ui.feeds

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.skyfolk.quantoflife.db.EventsStorageInteractor
import com.skyfolk.quantoflife.db.IQuantsStorageInteractor
import com.skyfolk.quantoflife.entity.*
import com.skyfolk.quantoflife.utils.getStartDateCalendar
import com.skyfolk.quantoflife.settings.SettingsInteractor
import com.skyfolk.quantoflife.feeds.getTotal
import com.skyfolk.quantoflife.utils.getEndDateCalendar
import java.util.*
import kotlin.collections.ArrayList

class FeedsViewModel(
    private val eventsStorageInteractor: EventsStorageInteractor,
    private val settingsInteractor: SettingsInteractor,
    private val quantsStorageInteractor: IQuantsStorageInteractor
) : ViewModel() {
    private val _listOfEvents = MutableLiveData<ArrayList<EventBase>>().apply {
        value = eventsStorageInteractor.getAllEvents()
    }
    val listOfEvents: LiveData<ArrayList<EventBase>> = _listOfEvents

    private val _listOfQuants = MutableLiveData<ArrayList<QuantBase>>().apply {
        value = quantsStorageInteractor.getAllQuantsList(false)
    }
    val listOfQuants: LiveData<ArrayList<QuantBase>> = _listOfQuants

    private val _totalPhysicalFound = MutableLiveData<Double>().apply {
        value = getTotal(
            quantsStorageInteractor,
            eventsStorageInteractor.getAllEvents(),
            QuantCategory.Physical
        )
    }
    val totalPhysicalFound: LiveData<Double> = _totalPhysicalFound

    private val _totalEmotionalFound = MutableLiveData<Double>().apply {
        value = getTotal(
            quantsStorageInteractor,
            eventsStorageInteractor.getAllEvents(),
            QuantCategory.Emotion
        )
    }
    val totalEmotionalFound: LiveData<Double> = _totalEmotionalFound

    private val _totalEvolutionFound = MutableLiveData<Double>().apply {
        value = getTotal(
            quantsStorageInteractor,
            eventsStorageInteractor.getAllEvents(),
            QuantCategory.Emotion
        )
    }
    val totalEvolutionFound: LiveData<Double> = _totalEvolutionFound

    private val _totalFound = MutableLiveData<Double>().apply {
        value = getTotal(quantsStorageInteractor, eventsStorageInteractor.getAllEvents())
    }
    val totalFound: LiveData<Double> = _totalFound

    private val _selectedTimeInterval = MutableLiveData<TimeInterval>().apply {
        value = TimeInterval.toTimeInterval(
            settingsInteractor.getStatisticTimeIntervalSelectedElement(),
            settingsInteractor.getStatisticTimeStart(),
            settingsInteractor.getStatisticTimeEnd()
        )
    }

    val selectedTimeInterval: LiveData<TimeInterval> = _selectedTimeInterval

    private var selectedEventFilter: String? = null

    init {
        runSearch()
    }

    fun runSearch() {
        selectedTimeInterval.value?.let {
            val startDate =
                Calendar.getInstance().getStartDateCalendar(
                    it,
                    settingsInteractor.getStartDayTime()
                ).timeInMillis
            val endDate =
                Calendar.getInstance().getEndDateCalendar(
                    it,
                    settingsInteractor.getStartDayTime()
                ).timeInMillis


            var resultList = ArrayList(
                eventsStorageInteractor.getAllEvents()
                    .filter { it.date in startDate until endDate })
            if (selectedEventFilter != null) {
                resultList = ArrayList(resultList.filter { it.quantId == selectedEventFilter })
            }

            setListOfEventsValue(resultList)
        }
    }

    private fun setListOfEventsValue(value: ArrayList<EventBase>) {
        _listOfEvents.value = value
        _totalPhysicalFound.value = getTotal(quantsStorageInteractor, value, QuantCategory.Physical)
        _totalEmotionalFound.value = getTotal(quantsStorageInteractor, value, QuantCategory.Emotion)
        _totalEvolutionFound.value =
            getTotal(quantsStorageInteractor, value, QuantCategory.Evolution)
        _totalFound.value = getTotal(quantsStorageInteractor, value)
    }

    fun saveTimeIntervalState(timeInterval: TimeInterval) {
        settingsInteractor.writeStatisticTimeIntervalSelectedElement(timeInterval.javaClass.name)
        if (timeInterval is TimeInterval.Selected) {
            settingsInteractor.setStatisticTimeStart(timeInterval.start)
            settingsInteractor.setStatisticTimeEnd(timeInterval.end)
        }
        _selectedTimeInterval.value = timeInterval
    }

    fun setSelectedEventFilter(item: String?) {
        selectedEventFilter = if (item != null) {
            quantsStorageInteractor.getAllQuantsList(true).first { it.name == item }.id
        } else {
            null
        }
        runSearch()
    }

    fun eventEdited(event: EventBase) {
        eventsStorageInteractor.addEventToDB(event)
        _listOfQuants.value = quantsStorageInteractor.getAllQuantsList(false)
        runSearch()
    }

    fun deleteEvent(event: EventBase) {
        eventsStorageInteractor.deleteEvent(event)
        _listOfQuants.value = quantsStorageInteractor.getAllQuantsList(false)
        runSearch()
    }
}

sealed class TimeInterval {
    object Today : TimeInterval()
    object Week : TimeInterval()
    object Month : TimeInterval()
    class Selected(val start: Long, val end: Long) : TimeInterval()
    object All : TimeInterval()

    companion object {
        fun toTimeInterval(enumString: String, start: Long, end: Long): TimeInterval {
            return when (enumString) {
                Today.javaClass.name -> Today
                Week.javaClass.name -> Week
                Month.javaClass.name -> Month
                Selected(start, end).javaClass.name -> Selected(start, end)
                else -> All
            }
        }
    }
}