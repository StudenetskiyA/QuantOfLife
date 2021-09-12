package com.skyfolk.quantoflife.db

import com.skyfolk.quantoflife.QLog
import com.skyfolk.quantoflife.entity.*
import io.realm.Realm
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class EventsStorageInteractor(private val dbInteractor: DBInteractor) {
    fun clearDataBase() {
        dbInteractor.getDB().executeTransactionAsync {
            it.deleteAll()
        }
    }

    fun clearEvents() {
        dbInteractor.getDB().executeTransactionAsync {
            it.delete(EventDbEntity::class.java)
            for (quant in it.where(QuantDbEntity::class.java).findAll()) {
                quant.usageCount = 0
            }
        }
    }

    fun addEventToDB(event: EventBase, onComplete: () -> Unit) {
        var rate: Double? = null
        var numericValue: Double? = null

        when (event) {
            is EventBase.EventRated -> {
                rate = event.rate
            }
            is EventBase.EventMeasure -> {
                numericValue = event.value
            }
            is EventBase.EventNote -> {
            }
        }
        val eventDbElement =
            EventDbEntity(event.quantId, event.date, rate, numericValue, event.note)

        dbInteractor.getDB().executeTransactionAsync( {
            val existEvent = existEventOrNull(it, event)
            if (existEvent != null) {
                QLog.d("edit event")
                existEvent.date = event.date
                existEvent.rate = rate
                existEvent.numericValue = numericValue
                existEvent.note = event.note
            } else {
                it.insertOrUpdate(eventDbElement)
            }
        }, {
           onComplete()
        }, null)
    }

    fun deleteEvent(event: EventBase, onComplete: () -> Unit) {
        dbInteractor.getDB().executeTransactionAsync( {
            existEventOrNull(it, event)?.deleteFromRealm()
        }, {
           onComplete()
        }, null)
    }

    suspend fun getAllEvents(): ArrayList<EventBase> = withContext(Dispatchers.IO){
        val result = ArrayList<EventBase>()
        for (r in dbInteractor.getDB().freeze().where(EventDbEntity::class.java).findAll() //TODO Async
            .sortedBy { it.date }) {
            when {
                (r.rate != null) -> {
                    result.add(EventBase.EventRated(r.id, r.quantId, r.date, r.note, r.rate!!))
                }
                (r.numericValue != null) -> {
                    result.add(EventBase.EventMeasure(r.id, r.quantId, r.date, r.note, r.numericValue!!))
                }
                else -> {
                    result.add(EventBase.EventNote(r.id, r.quantId, r.date, r.note))
                }
            }
        }
        return@withContext result
    }

    //TODO This is bad implementation
    suspend fun alreadyHaveEvent(event: EventBase): Boolean = withContext(Dispatchers.IO){
        for (storedEvent in getAllEvents()) {
            if (event.isEqual(storedEvent)) return@withContext true
        }
        return@withContext false
    }

    private fun existEventOrNull(realm: Realm, event: EventBase): EventDbEntity? {
        return realm.where(EventDbEntity::class.java)
            .equalTo("id", event.id)
            .findFirst()
    }
}