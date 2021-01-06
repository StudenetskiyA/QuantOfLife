package com.skyfolk.quantoflife.entity

sealed class EventBase(
    open var name: String,
    open var date: Long,
    open var note: String
) {
    data class EventNote(
        override var name: String ,
        override var date: Long,
        override var note: String
    ) : EventBase(name, date, note) {
        override fun copy(): EventNote {
            return EventNote(name, date, note)
        }
    }
    data class EventMeasure(
        override var name: String,
        override var date: Long,
        override var note: String,
        var value: Int
    ) : EventBase(name, date, note) {
        override fun copy(): EventMeasure {
            return EventMeasure(name, date, note, value)
        }
    }
    data class EventRated(
        override var name: String,
        override var date: Long,
        override var note: String,
        var rate: Int,
        var bonuses: ArrayList<EventBonusBase>
    ) : EventBase(name, date, note) {
        override fun copy(): EventRated {
            val oldBonuses = ArrayList<EventBonusBase>()
            for (bonus in bonuses) {
                oldBonuses.add(bonus.copy())
            }
            return EventRated(name, date, note, rate, oldBonuses)
        }
    }
//    data class EventRatedFact(
//        override var name: String,
//        override var date: Long,
//        override var note: String,
//        var bonuses: ArrayList<EventBonusBase>
//    ) : EventBase(name, date, note)

    fun isEqual(event: EventBase): Boolean {
        return name == event.name && date == event.date
    }

    abstract fun copy() : EventBase
}



class EventBonusBase(
    var category: QuantCategory,
    var value: Double
) {
    fun copy(): EventBonusBase {
        return EventBonusBase(category, value)
    }
}


