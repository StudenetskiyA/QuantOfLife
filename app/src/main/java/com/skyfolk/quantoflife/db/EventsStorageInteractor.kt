package com.skyfolk.quantoflife.db

import com.skyfolk.quantoflife.entity.*

class EventsStorageInteractor(private val dbInteractor: DBInteractor) {
    fun clearDataBase() {
        dbInteractor.getDB().executeTransaction { dbInteractor.getDB().deleteAll() } //TODO Separate
    }

    fun addEventToDB(event: EventBase) {
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
        val eventDbElement = EventDbEntity(event.quantId, event.date, rate, numericValue, event.note)

        dbInteractor.getDB().executeTransaction {
            dbInteractor.getDB().insertOrUpdate(eventDbElement)
        }
    }

    fun getAllEvents(): ArrayList<EventBase> {
        val result = ArrayList<EventBase>()
        for (r in dbInteractor.getDB().where(EventDbEntity::class.java).findAll().sortedBy { it.date }) {
            when {
                (r.rate != null) -> {
                    result.add(EventBase.EventRated(r.quantId, r.date, r.note, r.rate!!))
                }
                (r.numericValue != null) -> {
                    result.add(EventBase.EventMeasure(r.quantId, r.date, r.note, r.rate!!))
                }
                else -> {
                    result.add(EventBase.EventNote(r.quantId, r.date, r.note))
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
}