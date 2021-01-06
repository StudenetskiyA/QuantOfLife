package com.skyfolk.quantoflife.entity

sealed class EventBase(
    open val id: String,
    open var quantId: String,
    open var date: Long,
    open var note: String
) {
    data class EventNote(
        override val id: String,
        override var quantId: String,
        override var date: Long,
        override var note: String
    ) : EventBase(id, quantId, date, note) {
        override fun copy(): EventNote {
            return EventNote(id, quantId, date, note)
        }
    }
    data class EventMeasure(
        override val id: String,
        override var quantId: String,
        override var date: Long,
        override var note: String,
        var value: Int
    ) : EventBase(id, quantId, date, note) {
        override fun copy(): EventMeasure {
            return EventMeasure(id, quantId, date, note, value)
        }
    }
    data class EventRated(
        override val id: String,
        override var quantId: String,
        override var date: Long,
        override var note: String,
        var rate: Int
    ) : EventBase(id, quantId, date, note) {
        override fun copy(): EventRated {
            return EventRated(id, quantId, date, note, rate)
        }
    }

    fun isEqual(event: EventBase): Boolean {
        return id == event.id
    }

    abstract fun copy() : EventBase
}


