package com.skyfolk.quantoflife.ui.feeds

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skyfolk.quantoflife.IDateTimeRepository
import com.skyfolk.quantoflife.db.EventsStorageInteractor
import com.skyfolk.quantoflife.db.IQuantsStorageInteractor
import com.skyfolk.quantoflife.entity.EventBase
import com.skyfolk.quantoflife.entity.EventDisplayable
import com.skyfolk.quantoflife.entity.QuantBase
import com.skyfolk.quantoflife.entity.QuantCategory
import com.skyfolk.quantoflife.feeds.getStarTotal
import com.skyfolk.quantoflife.feeds.getTotal
import com.skyfolk.quantoflife.settings.SettingsInteractor
import com.skyfolk.quantoflife.timeInterval.TimeInterval
import com.skyfolk.quantoflife.ui.feeds.FeedsFragmentState.EventsListLoading.Companion.updateStateToLoading
import com.skyfolk.quantoflife.ui.feeds.FeedsFragmentState.LoadingEventsListCompleted.Companion.updateStateToCompleted
import com.skyfolk.quantoflife.utils.SingleLiveEvent
import com.skyfolk.quantoflife.utils.getEndDateCalendar
import com.skyfolk.quantoflife.utils.getStartDateCalendar
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList

class FeedsViewModel(
    private val eventsStorageInteractor: EventsStorageInteractor,
    private val settingsInteractor: SettingsInteractor,
    private val quantsStorageInteractor: IQuantsStorageInteractor,
    private val dateTimeRepository: IDateTimeRepository
) : ViewModel() {
    private val _state = MutableStateFlow<FeedsFragmentState>(
        FeedsFragmentState.EventsListLoading(
            listOfQuants = quantsStorageInteractor.getAllQuantsList(false),
            selectedTimeInterval = TimeInterval.toTimeInterval(
                settingsInteractor.statisticTimeIntervalSelectedElement,
                settingsInteractor.statisticTimeStart,
                settingsInteractor.statisticTimeEnd
            ),
            selectedEventFilter = getQuantNameById(settingsInteractor.selectedEventFiler),
            selectedTextFilter = settingsInteractor.statisticSearchText,
            quantCategoryNames = settingsInteractor.getCategoryNames()
        )
    )
    val state: StateFlow<FeedsFragmentState> = _state.asStateFlow()

    private val _singleLifeEvent = SingleLiveEvent<FeedsFragmentSingleLifeEvent>()
    val singleLifeEvent: LiveData<FeedsFragmentSingleLifeEvent> get() = _singleLifeEvent

    private fun runSearch(
        timeIntervalWasChanged: TimeIntervalWasChanged? = null,
        eventFilterWasChanged: EventFilterWasChanged? = null
    ) {
        Log.d("skyfolk-timer", "runSearchStart: ${System.currentTimeMillis()}")

        viewModelScope.launch {
            val selectedTimeInterval =
                timeIntervalWasChanged?.timeInterval ?: _state.value.selectedTimeInterval
            selectedTimeInterval.let { interval ->
                updateStateToLoading(_state)

                val searchText = settingsInteractor.statisticSearchText

                val startDate =
                    dateTimeRepository.getCalendar().getStartDateCalendar(
                        interval,
                        settingsInteractor.startDayTime
                    ).timeInMillis
                val endDate =
                    dateTimeRepository.getCalendar().getEndDateCalendar(
                        interval,
                        settingsInteractor.startDayTime
                    ).timeInMillis

                var listOfEvents = ArrayList(
                    eventsStorageInteractor.getAllEvents()
                        .filter { it.date in startDate until endDate }
                        .filter {
                                    it.note.contains(searchText, ignoreCase = true)
                        })

                val selectedEventFilter =
                    if (eventFilterWasChanged != null) eventFilterWasChanged.eventFilter else
                        getQuantIdByName(_state.value.selectedEventFilter)
                selectedEventFilter?.let { filter ->
                    listOfEvents = ArrayList(listOfEvents.filter { it.quantId == filter })
                }

                Log.d("skyfolk-timer", "runSearchEnd: ${System.currentTimeMillis()}")

                val allQuantsFound = quantsStorageInteractor.getAllQuantsList(false)

                val totalPhysicalFound = getTotal(
                    allQuantsFound,
                    listOfEvents,
                    QuantCategory.Physical
                )

                val totalEmotionalFound = getTotal(
                    allQuantsFound,
                    listOfEvents,
                    QuantCategory.Emotion
                )

                val totalEvolutionFound = getTotal(
                    allQuantsFound,
                    listOfEvents,
                    QuantCategory.Evolution
                )

                val totalFound = getTotal(allQuantsFound, listOfEvents)
                val starFound = getStarTotal(allQuantsFound, listOfEvents)

                Log.d("skyfolk-timer", "runSearchValuesEnd: ${System.currentTimeMillis()}")

                updateStateToCompleted(
                    _state,
                    _timeInterval = interval,
                    _selectedEventFilter = getQuantNameById(selectedEventFilter),
                    _selectedTextFilter = searchText,
                    _quantCategoryName = settingsInteractor.getCategoryNames(),
                    _listOfEvents = listOfEvents.toDisplayableEvents(allQuantsFound),
                    _totalPhysicalFound = totalPhysicalFound,
                    _totalEmotionalFound = totalEmotionalFound,
                    _totalEvolutionFound = totalEvolutionFound,
                    _totalFound = totalFound,
                    _totalStarFound = starFound
                )

                Log.d("skyfolk-timer", "runSearchUpdateStateEnd: ${System.currentTimeMillis()}")
            }
        }
    }

    fun getDefaultCalendar(): Calendar {
        return dateTimeRepository.getCalendar()
    }

    fun setTimeIntervalState(timeInterval: TimeInterval) {
        settingsInteractor.statisticTimeIntervalSelectedElement = timeInterval.javaClass.name
        if (timeInterval is TimeInterval.Selected) {
            settingsInteractor.statisticTimeStart = timeInterval.start
            settingsInteractor.statisticTimeEnd = timeInterval.end
        }

        runSearch(timeIntervalWasChanged = TimeIntervalWasChanged(timeInterval))
    }

    fun setSearchText(searchText: String) {
        settingsInteractor.statisticSearchText = searchText

        runSearch()
    }

    fun setSelectedEventFilter(itemName: String?, valueNotChange: Boolean = false) {
        if (valueNotChange) {
            runSearch(eventFilterWasChanged = EventFilterWasChanged(settingsInteractor.selectedEventFiler))
        } else {
            settingsInteractor.selectedEventFiler = getQuantIdByName(itemName)
            runSearch(eventFilterWasChanged = EventFilterWasChanged(getQuantIdByName(itemName)))
        }
    }

    fun editEvent(eventId: String) {
        viewModelScope.launch {
            eventsStorageInteractor.getAllEvents().firstOrNull { it.id == eventId }?.let { event ->
                quantsStorageInteractor.getQuantById(event.quantId)?.let { quant ->
                    _singleLifeEvent.value =
                        FeedsFragmentSingleLifeEvent.ShowEditEventDialog(quant, event)
                }
            }
        }
    }

    fun eventEdited(event: EventBase) {
        eventsStorageInteractor.addEventToDB(event) { runSearch() }
    }

    fun deleteEvent(event: EventBase) {
        eventsStorageInteractor.deleteEvent(event) { runSearch() }
    }

    private fun getQuantNameById(id: String?): String? {
        id?.let {
            return quantsStorageInteractor.getQuantById(it)?.name
        }
        return null
    }

    private fun getQuantIdByName(name: String?): String? {
        name?.let {
            return quantsStorageInteractor.getQuantIdByName(it)
        }
        return null
    }

    private data class TimeIntervalWasChanged(val timeInterval: TimeInterval)
    private data class EventFilterWasChanged(val eventFilter: String?)
}

fun ArrayList<EventBase>.toDisplayableEvents(quants: List<QuantBase>): ArrayList<EventDisplayable> {
    val result = arrayListOf<EventDisplayable>()

    for (event in this) {
        quants.firstOrNull { it.id == event.quantId }?.let {
            val value = when {
                (event is EventBase.EventRated) -> event.rate
                (event is EventBase.EventMeasure) -> event.value
                else -> null
            }

            val bonuses = if (it is QuantBase.QuantRated) it.bonuses else null
            result.add(
                EventDisplayable(
                    id = event.id,
                    name = it.name,
                    quantId = event.quantId,
                    icon = it.icon,
                    date = event.date,
                    note = event.note,
                    value = value,
                    bonuses = bonuses
                )
            )
        }
    }

    return result
}



