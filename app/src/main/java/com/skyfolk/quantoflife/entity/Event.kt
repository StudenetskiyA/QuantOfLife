package com.skyfolk.quantoflife.entity

sealed class EventBase(
    open var quantId: String,
    open var date: Long,
    open var note: String
) {
    data class EventNote(
        override var quantId: String,
        override var date: Long,
        override var note: String
    ) : EventBase(quantId, date, note) {
        override fun copy(): EventNote {
            return EventNote(quantId, date, note)
        }
    }
    data class EventMeasure(
        override var quantId: String,
        override var date: Long,
        override var note: String,
        var value: Int
    ) : EventBase(quantId, date, note) {
        override fun copy(): EventMeasure {
            return EventMeasure(quantId, date, note, value)
        }
    }
    data class EventRated(
        override var quantId: String,
        override var date: Long,
        override var note: String,
        var rate: Int
    ) : EventBase(quantId, date, note) {
        override fun copy(): EventRated {
            return EventRated(quantId, date, note, rate)
        }
    }

    fun isEqual(event: EventBase): Boolean {
        return quantId == event.quantId && date == event.date
    }

    abstract fun copy() : EventBase
}


