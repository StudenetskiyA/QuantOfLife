package com.skyfolk.quantoflife.db

import com.skyfolk.quantoflife.QLog
import com.skyfolk.quantoflife.entity.*
import io.realm.Realm

class EventsStorageInteractor(private val dbInteractor: DBInteractor) {
    fun clearDataBase() {
        dbInteractor.getDB().executeTransactionAsync {
            it.deleteAll()
        }
    }

    fun addEventToDB(event: EventBase, onComplete: () -> Unit) {
        var rate: Int? = null
        var numericValue: Int? = null

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

    fun getAllEvents(): ArrayList<EventBase> {
        val result = ArrayList<EventBase>()
        for (r in dbInteractor.getDB().where(EventDbEntity::class.java).findAll() //TODO Async
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
        return result
    }

    //TODO This is bad implementation
    fun alreadyHaveEvent(event: EventBase): Boolean {
        for (storedEvent in getAllEvents()) {
            if (event.isEqual(storedEvent)) return true
        }
        return false
    }

    private fun existEventOrNull(realm: Realm, event: EventBase): EventDbEntity? {
        return realm.where(EventDbEntity::class.java)
            .equalTo("id", event.id)
            .findFirst()
    }
}