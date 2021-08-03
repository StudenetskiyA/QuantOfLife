package com.skyfolk.quantoflife.ui.feeds

import androidx.lifecycle.MutableLiveData
import com.skyfolk.quantoflife.entity.EventDisplayable
import com.skyfolk.quantoflife.entity.QuantBase
import com.skyfolk.quantoflife.entity.QuantCategory
import com.skyfolk.quantoflife.timeInterval.TimeInterval

sealed class FeedsFragmentState(
    open val listOfQuants: ArrayList<QuantBase>,
    open val selectedTimeInterval: TimeInterval,
    open val selectedEventFilter: String?,
    open val quantCategoryNames: ArrayList<Pair<QuantCategory, String>>
) {
    data class EventsListLoading(
        override val listOfQuants: ArrayList<QuantBase>,
        override val selectedTimeInterval: TimeInterval,
        override val selectedEventFilter: String?,
        override val quantCategoryNames: ArrayList<Pair<QuantCategory, String>>
    ) : FeedsFragmentState(
        listOfQuants,
        selectedTimeInterval,
        selectedEventFilter,
        quantCategoryNames
    ) {
        companion object {
            fun updateStateToLoading(state: MutableLiveData<FeedsFragmentState>) {
                state.value?.let {
                    state.value = EventsListLoading(
                        it.listOfQuants,
                        it.selectedTimeInterval,
                        it.selectedEventFilter,
                        it.quantCategoryNames
                    )
                }
            }
        }
    }

    data class LoadingEventsListCompleted(
        override val listOfQuants: ArrayList<QuantBase>,
        override val selectedTimeInterval: TimeInterval,
        override val selectedEventFilter: String?,
        override val quantCategoryNames: ArrayList<Pair<QuantCategory, String>>,
        val listOfEvents: ArrayList<EventDisplayable>,
        val totalPhysicalFound: Double,
        val totalEmotionalFound: Double,
        val totalEvolutionFound: Double,
        val totalStarFound: Int,
        val totalFound: Double
    ) : FeedsFragmentState(
        listOfQuants,
        selectedTimeInterval,
        selectedEventFilter,
        quantCategoryNames
    ) {
        companion object {
            fun updateStateToCompleted(
                state: MutableLiveData<FeedsFragmentState>,
                _timeInterval: TimeInterval,
                _selectedEventFilter: String?,
                _quantCategoryName: ArrayList<Pair<QuantCategory, String>>,
                _listOfEvents: ArrayList<EventDisplayable>,
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
                        _quantCategoryName,
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
