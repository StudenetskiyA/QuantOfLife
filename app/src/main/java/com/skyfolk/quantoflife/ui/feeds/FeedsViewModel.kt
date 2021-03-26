package com.skyfolk.quantoflife.ui.feeds

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.skyfolk.quantoflife.db.EventsStorageInteractor
import com.skyfolk.quantoflife.db.IQuantsStorageInteractor
import com.skyfolk.quantoflife.entity.*
import com.skyfolk.quantoflife.feeds.getStarTotal
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

    private val _totalStarFound = MutableLiveData<Int>().apply {
        value = getStarTotal(
            quantsStorageInteractor,
            eventsStorageInteractor.getAllEvents()
        )
    }
    val totalStarFound: LiveData<Int> = _totalStarFound

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
        selectedTimeInterval.value?.let { interval ->
            val startDate =
                Calendar.getInstance().getStartDateCalendar(
                    interval,
                    settingsInteractor.getStartDayTime()
                ).timeInMillis
            val endDate =
                Calendar.getInstance().getEndDateCalendar(
                    interval,
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

    private fun setListOfEventsValue(listOfEvents: ArrayList<EventBase>) {
        _listOfEvents.value = listOfEvents
        _totalPhysicalFound.value =
            getTotal(quantsStorageInteractor, listOfEvents, QuantCategory.Physical)
        _totalEmotionalFound.value =
            getTotal(quantsStorageInteractor, listOfEvents, QuantCategory.Emotion)
        _totalEvolutionFound.value =
            getTotal(quantsStorageInteractor, listOfEvents, QuantCategory.Evolution)
        _totalFound.value = getTotal(quantsStorageInteractor, listOfEvents)
        _totalStarFound.value = getStarTotal(quantsStorageInteractor, listOfEvents)
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

sealed class FeedsFragmentState(
    open val listOfQuants: ArrayList<QuantBase>,
    open val selectedTimeInterval: TimeInterval,
    open val selectedEventFilter: String
) {
    data class Loading(
        override val listOfQuants: ArrayList<QuantBase>,
        override val selectedTimeInterval: TimeInterval,
        override val selectedEventFilter: String
    ) : FeedsFragmentState(listOfQuants, selectedTimeInterval, selectedEventFilter)

    data class Completed(
        override val listOfQuants: ArrayList<QuantBase>,
        override val selectedTimeInterval: TimeInterval,
        override val selectedEventFilter: String
    ) : FeedsFragmentState(listOfQuants, selectedTimeInterval, selectedEventFilter)
}