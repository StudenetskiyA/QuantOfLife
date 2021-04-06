package com.skyfolk.quantoflife.ui.feeds

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skyfolk.quantoflife.DateTimeRepository
import com.skyfolk.quantoflife.IDateTimeRepository
import com.skyfolk.quantoflife.db.EventsStorageInteractor
import com.skyfolk.quantoflife.db.IQuantsStorageInteractor
import com.skyfolk.quantoflife.entity.*
import com.skyfolk.quantoflife.feeds.getStarTotal
import com.skyfolk.quantoflife.utils.getStartDateCalendar
import com.skyfolk.quantoflife.settings.SettingsInteractor
import com.skyfolk.quantoflife.feeds.getTotal
import com.skyfolk.quantoflife.ui.feeds.FeedsFragmentState.EventsListLoading.Companion.updateStateToLoading
import com.skyfolk.quantoflife.ui.feeds.FeedsFragmentState.LoadingEventsListCompleted.Companion.updateStateToCompleted
import com.skyfolk.quantoflife.utils.getEndDateCalendar
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList

class FeedsViewModel(
    private val eventsStorageInteractor: EventsStorageInteractor,
    private val settingsInteractor: SettingsInteractor,
    private val quantsStorageInteractor: IQuantsStorageInteractor,
    private val dateTimeRepository: IDateTimeRepository
) : ViewModel() {
    private val _state = MutableLiveData<FeedsFragmentState>().apply {
        value = FeedsFragmentState.EventsListLoading(
            listOfQuants = quantsStorageInteractor.getAllQuantsList(false),
            selectedTimeInterval = TimeInterval.toTimeInterval(
                settingsInteractor.getStatisticTimeIntervalSelectedElement(),
                settingsInteractor.getStatisticTimeStart(),
                settingsInteractor.getStatisticTimeEnd()
            ),
            selectedEventFilter = settingsInteractor.getSelectedEventFiler()
        )
    }
    val state: LiveData<FeedsFragmentState> = _state

    private fun runSearch(timeIntervalWasChanged: TimeIntervalWasChanged? = null, eventFilterWasChanged: EventFilterWasChanged? = null) {
        viewModelScope.launch {
            val selectedTimeInterval = timeIntervalWasChanged?.timeInterval ?: _state.value?.selectedTimeInterval
            selectedTimeInterval?.let { interval ->
                updateStateToLoading(_state)

               // delay(1000)

                val startDate =
                    dateTimeRepository.getCalendar().getStartDateCalendar(
                        interval,
                        settingsInteractor.getStartDayTime()
                    ).timeInMillis
                val endDate =
                    dateTimeRepository.getCalendar().getEndDateCalendar(
                        interval,
                        settingsInteractor.getStartDayTime()
                    ).timeInMillis
                Log.d("skyfolk-time","start = $startDate , end = $endDate")


                var listOfEvents = ArrayList(
                    eventsStorageInteractor.getAllEvents()
                        .filter { it.date in startDate until endDate })
                val selectedEventFilter = if (eventFilterWasChanged !=null) eventFilterWasChanged.eventFilter else _state.value?.selectedEventFilter
                selectedEventFilter?.let { filter ->
                    listOfEvents = ArrayList(listOfEvents.filter { it.quantId == filter })
                }

                updateStateToCompleted(
                    _state,
                    _timeInterval = interval,
                    _selectedEventFilter = selectedEventFilter,
                    _listOfEvents = listOfEvents,
                    _totalPhysicalFound = getTotal(
                        quantsStorageInteractor,
                        listOfEvents,
                        QuantCategory.Physical
                    ),
                    _totalEmotionalFound = getTotal(
                        quantsStorageInteractor,
                        listOfEvents,
                        QuantCategory.Emotion
                    ),
                    _totalEvolutionFound = getTotal(
                        quantsStorageInteractor,
                        listOfEvents,
                        QuantCategory.Evolution
                    ),
                    _totalFound = getTotal(quantsStorageInteractor, listOfEvents),
                    _totalStarFound = getStarTotal(quantsStorageInteractor, listOfEvents)
                )

            }
        }
    }

    fun setTimeIntervalState(timeInterval: TimeInterval) {
        settingsInteractor.writeStatisticTimeIntervalSelectedElement(timeInterval.javaClass.name)
        if (timeInterval is TimeInterval.Selected) {
            settingsInteractor.setStatisticTimeStart(timeInterval.start)
            settingsInteractor.setStatisticTimeEnd(timeInterval.end)
        }

        runSearch(timeIntervalWasChanged = TimeIntervalWasChanged(timeInterval))
    }

    fun setSelectedEventFilter(itemId: String?) {
        settingsInteractor.setSelectedEventFiler(itemId)
        runSearch(eventFilterWasChanged = EventFilterWasChanged(itemId))
    }

    fun eventEdited(event: EventBase) {
        eventsStorageInteractor.addEventToDB(event)
        runSearch()
    }

    fun deleteEvent(event: EventBase) {
        eventsStorageInteractor.deleteEvent(event)
        runSearch()
    }

    fun getQuantNameById(id: String): String? {
        return quantsStorageInteractor.getQuantById(id)?.name
    }

    fun getQuantIdByName(name: String): String? {
        return quantsStorageInteractor.getQuantByName(name)?.id
    }

    private data class TimeIntervalWasChanged(val timeInterval: TimeInterval)
    private data class EventFilterWasChanged(val eventFilter: String?)
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
    open val selectedEventFilter: String?
) {

    data class EventsListLoading(
        override val listOfQuants: ArrayList<QuantBase>,
        override val selectedTimeInterval: TimeInterval,
        override val selectedEventFilter: String?
    ) : FeedsFragmentState(listOfQuants, selectedTimeInterval, selectedEventFilter) {
        companion object {
            fun updateStateToLoading(state: MutableLiveData<FeedsFragmentState>) {
                state.value?.let {
                    state.value = EventsListLoading(
                        it.listOfQuants,
                        it.selectedTimeInterval,
                        it.selectedEventFilter
                    )
                }
            }
        }
    }

    data class LoadingEventsListCompleted(
        override val listOfQuants: ArrayList<QuantBase>,
        override val selectedTimeInterval: TimeInterval,
        override val selectedEventFilter: String?,
        val listOfEvents: ArrayList<EventBase>,
        val totalPhysicalFound: Double,
        val totalEmotionalFound: Double,
        val totalEvolutionFound: Double,
        val totalStarFound: Int,
        val totalFound: Double
    ) : FeedsFragmentState(listOfQuants, selectedTimeInterval, selectedEventFilter) {
        companion object {
            fun updateStateToCompleted(
                state: MutableLiveData<FeedsFragmentState>,
                _timeInterval: TimeInterval,
                _selectedEventFilter: String?,
                _listOfEvents: ArrayList<EventBase>,
                _totalPhysicalFound: Double,
                _totalEmotionalFound: Double,
                _totalEvolutionFound: Double,
                _totalStarFound: Int,
                _totalFound: Double
            ) {
                state.value?.let {
                    state.value = LoadingEventsListCompleted(
                        it.listOfQuants,
                        _timeInterval,
                        _selectedEventFilter,
                        _listOfEvents,
                        _totalPhysicalFound,
                        _totalEmotionalFound,
                        _totalEvolutionFound,
                        _totalStarFound,
                        _totalFound
                    )
                }
            }
        }
    }
}



