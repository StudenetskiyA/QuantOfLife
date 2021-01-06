package com.skyfolk.quantoflife.db

import com.skyfolk.quantoflife.entity.EventBonusBase
import io.realm.RealmList
import io.realm.RealmObject

open class EventDbEntity(
    var name: String = "",
    var date: Long = 0,
    var rate: Int? = null,
    var numericValue: Int? = null,
    var bonusDbEntities: RealmList<EventBonusDbEntity> = RealmList(),
    var note: String = ""
) : RealmObject()

open class EventBonusDbEntity(
    var categoryDescription: String = "",
    var value: Double = 0.0
) : RealmObject() {
    companion object {
        fun parseEventBonusBase(eventBonus: EventBonusBase) : EventBonusDbEntity {
            return EventBonusDbEntity(eventBonus.category.name, eventBonus.value)
        }
    }
}