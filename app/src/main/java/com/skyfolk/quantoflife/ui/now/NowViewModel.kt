package com.skyfolk.quantoflife.ui.now

import android.util.Log
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skyfolk.quantoflife.IDateTimeRepository
import com.skyfolk.quantoflife.db.EventsStorageInteractor
import com.skyfolk.quantoflife.db.IGoalStorageInteractor
import com.skyfolk.quantoflife.db.IQuantsStorageInteractor
import com.skyfolk.quantoflife.entity.*
import com.skyfolk.quantoflife.utils.getStartDateCalendar
import com.skyfolk.quantoflife.settings.SettingsInteractor
import com.skyfolk.quantoflife.feeds.getTotal
import com.skyfolk.quantoflife.import.ImportInteractor
import com.skyfolk.quantoflife.timeInterval.TimeInterval
import com.skyfolk.quantoflife.ui.create_quant.CreateQuantDialogFragment
import com.skyfolk.quantoflife.ui.goals.CreateGoalDialogFragment
import com.skyfolk.quantoflife.utils.SingleLiveEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList

class NowViewModel(
    private val quantsStorageInteractor: IQuantsStorageInteractor,
    private val eventsStorageInteractor: EventsStorageInteractor,
    private val goalStorageInteractor: IGoalStorageInteractor,
    private val settingsInteractor: SettingsInteractor,
    private val dateTimeRepository: IDateTimeRepository,
    private val importInteractor: ImportInteractor
) : ViewModel(), INowViewModel {
    private val _toastState = SingleLiveEvent<String>()
    val toastState: LiveData<String> get() = _toastState

    private val _dialogState = SingleLiveEvent<DialogFragment>()
    val dialogState: LiveData<DialogFragment> get() = _dialogState

    private val _listOfQuants = MutableLiveData<List<QuantBase>>().apply {
        value = quantsStorageInteractor.getAllQuantsList(false)
    }
    val listOfQuants: LiveData<List<QuantBase>> = _listOfQuants

    private val _todayTotal = MutableStateFlow(0.0)
    val todayTotal: StateFlow<Double> = _todayTotal.asStateFlow()

    private val _listOfGoals = MutableLiveData<ArrayList<GoalPresent>>().apply {
        value = arrayListOf()
    }
    val listOfGoal: LiveData<ArrayList<GoalPresent>> = _listOfGoals

    init {
        if (quantsStorageInteractor.getAllQuantsList(false).isEmpty()) {
            viewModelScope.launch {
                importInteractor.importEventsFromRaw {
                    _listOfQuants.value = quantsStorageInteractor.getAllQuantsList(false)
                }
            }
        }

        updateTodayTotal()
    }

    override fun openCreateNewQuantDialog(existQuant: QuantBase?) {
        val dialog = CreateQuantDialogFragment(existQuant, settingsInteractor)
        dialog.setDialogListener(object : CreateQuantDialogFragment.DialogListener {
            override fun onConfirm(quant: QuantBase) {
                quantsStorageInteractor.addQuantToDB(quant) {
                    _listOfQuants.value = quantsStorageInteractor.getAllQuantsList(false)
                    updateTodayTotal()
                }
            }

            override fun onDelete(quant: QuantBase) {
                quantsStorageInteractor.deleteQuant(quant) {
                    _listOfQuants.value = quantsStorageInteractor.getAllQuantsList(false)
                }
            }

            override fun onDecline() {}
        })
        _dialogState.value = dialog
    }

    override fun openCreateNewGoalDialog(existGoal: Goal?) {
        val dialog = CreateGoalDialogFragment(existGoal, settingsInteractor)
        dialog.setDialogListener(object : CreateGoalDialogFragment.DialogListener {
            override fun onConfirm(goal: Goal) {
                goalStorageInteractor.addGoalToDB(goal)
                updateTodayTotal()
            }

            override fun onDelete(goal: Goal) {
                goalStorageInteractor.deleteGoal(goal)
                updateTodayTotal()
            }

            override fun onDecline() {}
        })
        _dialogState.value = dialog
    }

    override fun onEventCreated(event: EventBase) {
        eventsStorageInteractor.addEventToDB(event) {
            quantsStorageInteractor.incrementQuantUsage(event.quantId)
            _listOfQuants.value = quantsStorageInteractor.getAllQuantsList(false)
            updateTodayTotal()
        }
    }

    private fun updateTodayTotal() {
        viewModelScope.launch {
            val startDate = dateTimeRepository.getCalendar().getStartDateCalendar(
                TimeInterval.Today,
                settingsInteractor.startDayTime
            ).timeInMillis
            val endDate = dateTimeRepository.getTimeInMillis()

            val resultList = ArrayList(
                eventsStorageInteractor.getAllEvents()
                    .filter { it.date in startDate until endDate })

            _todayTotal.update {
                getTotal(quantsStorageInteractor.getAllQuantsList(false), resultList)
            }

            val millisecondsInDay = 24 * 60 * 60 * 1000
            val goals = goalStorageInteractor.getListOfGoals()
            val goalsPresentList: ArrayList<GoalPresent> = arrayListOf()
            for (goal in goals) {
                val goalStartDate = dateTimeRepository.getCalendar().getStartDateCalendar(
                    goal.duration,
                    settingsInteractor.startDayTime
                ).timeInMillis

                val goalResultList = ArrayList(
                    eventsStorageInteractor.getAllEvents()
                        .filter { it.date in goalStartDate until endDate })

                val daysGone = ((endDate - goalStartDate) / millisecondsInDay).toInt() + 1
                val completed = getTotal(
                    quantsStorageInteractor.getAllQuantsList(false),
                    goalResultList,
                    goal.type
                )
                val durationInDays = when (goal.duration) {
                    is TimeInterval.Today -> 1
                    is TimeInterval.Week -> 7
                    is TimeInterval.Month -> dateTimeRepository.getCalendar()
                        .getActualMaximum(Calendar.DAY_OF_MONTH)
                    is TimeInterval.All -> 0
                    is TimeInterval.Selected -> 0
                    is TimeInterval.Year -> 365
                }

                goalsPresentList.add(
                    GoalPresent(
                        goal.id,
                        goal.duration,
                        durationInDays,
                        goal.target,
                        completed,
                        daysGone,
                        goal.type
                    )
                )
            }

            _listOfGoals.value = goalsPresentList
        }
    }
}