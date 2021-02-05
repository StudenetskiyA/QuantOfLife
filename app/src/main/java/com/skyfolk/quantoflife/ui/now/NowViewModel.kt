package com.skyfolk.quantoflife.ui.now

import androidx.fragment.app.DialogFragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.skyfolk.quantoflife.db.EventsStorageInteractor
import com.skyfolk.quantoflife.db.IQuantsStorageInteractor
import com.skyfolk.quantoflife.entity.EventBase
import com.skyfolk.quantoflife.entity.QuantBase
import com.skyfolk.quantoflife.getStartDateCalendar
import com.skyfolk.quantoflife.settings.SettingsInteractor
import com.skyfolk.quantoflife.feeds.getTotal
import com.skyfolk.quantoflife.ui.create_quant.CreateQuantDialogFragment
import com.skyfolk.quantoflife.ui.feeds.TimeInterval
import com.skyfolk.quantoflife.utils.SingleLiveEvent
import java.util.*
import kotlin.collections.ArrayList

class NowViewModel(
    private val quantsStorageInteractor: IQuantsStorageInteractor,
    private val eventsStorageInteractor: EventsStorageInteractor,
    private val settingsInteractor: SettingsInteractor
) : ViewModel(), INowViewModel {
    private val _toastState = SingleLiveEvent<String>()
    val toastState: LiveData<String> get() = _toastState

    private val _dialogState = SingleLiveEvent<DialogFragment>()
    val dialogState: LiveData<DialogFragment> get() = _dialogState

    private val _listOfQuants = MutableLiveData<ArrayList<QuantBase>>().apply {
        value = quantsStorageInteractor.getAllQuantsList(false)
    }
    val listOfQuants: LiveData<ArrayList<QuantBase>> = _listOfQuants

    private val _todayTotal = MutableLiveData<Double>().apply {
        value = calculateTodayTotal()
    }
    val todayTotal: LiveData<Double> = _todayTotal

    init {
        if (quantsStorageInteractor.getAllQuantsList(false).isEmpty()) {
            for (quant in quantsStorageInteractor.getPresetQuantsList()) {
                quantsStorageInteractor.addQuantToDB(quant)
            }
            _listOfQuants.value = quantsStorageInteractor.getAllQuantsList(false)
        }
    }

    override fun openCreateNewQuantDialog(existQuant: QuantBase?) {
        val dialog = CreateQuantDialogFragment(existQuant, settingsInteractor)
        dialog.setDialogListener(object : CreateQuantDialogFragment.DialogListener {
            override fun onConfirm(quant: QuantBase) {
                quantsStorageInteractor.addQuantToDB(quant)
                _listOfQuants.value = quantsStorageInteractor.getAllQuantsList(false)
                _todayTotal.value = calculateTodayTotal()
            }

            override fun onDelete(quant: QuantBase) {
                quantsStorageInteractor.deleteQuant(quant)
                _listOfQuants.value = quantsStorageInteractor.getAllQuantsList(false)
            }

            override fun onDecline() {}
        })
        _dialogState.value = dialog
    }

    override fun onEventCreated(event: EventBase) {
        eventsStorageInteractor.addEventToDB(event)
        quantsStorageInteractor.incrementQuantUsage(event.quantId)
        _listOfQuants.value = quantsStorageInteractor.getAllQuantsList(false)
        _todayTotal.value = calculateTodayTotal()
    }

    private fun calculateTodayTotal() : Double{
        val startDate = Calendar.getInstance().getStartDateCalendar(TimeInterval.Today, settingsInteractor.getStartDayTime()).timeInMillis
        val endDate = System.currentTimeMillis()

        val resultList = ArrayList(
            eventsStorageInteractor.getAllEvents().filter { it.date in startDate until endDate })

        return getTotal(quantsStorageInteractor, resultList)
    }

}