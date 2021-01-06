package com.skyfolk.quantoflife.entity

import java.util.*

sealed class QuantBase(
    open var id: String = UUID.randomUUID().toString(), //TODO Replace to GUID
    open var name: String,
    open var icon: String = "",
    open var primalCategory: QuantCategory,
    open var description: String = "Подсказка для оценки"
) {
    data class QuantNote(
        override var id: String = UUID.randomUUID().toString(),
        override var name: String,
        override var icon: String = "",
        override var primalCategory: QuantCategory,
        override var description: String = "Подсказка для оценки"
    ) : QuantBase(id, name, icon, primalCategory, description) {
        override fun copy(): QuantNote {
            return QuantNote(id, name, icon, primalCategory, description)
        }
    }

    data class QuantRated(
        override var id: String = UUID.randomUUID().toString(),
        override var name: String,
        override var icon: String = "",
        override var primalCategory: QuantCategory,
        var bonuses: ArrayList<QuantBonusBase.QuantBonusRated>,
        override var description: String = "Подсказка для оценки"
    ) : QuantBase(id, name, icon, primalCategory, description) {
        fun getBonusFor(category: QuantCategory): QuantBonusBase.QuantBonusRated? {
            for (bonus in bonuses) {
                if (bonus.category == category) return bonus
            }
            return null
        }
        override fun copy(): QuantRated {
            val oldBonuses = ArrayList<QuantBonusBase.QuantBonusRated>()
            for (bonus in bonuses) {
                oldBonuses.add(bonus.copy())
            }
            return QuantRated(id, name, icon, primalCategory, oldBonuses, description)
        }
    }

    data class QuantMeasure(
        override var id: String = UUID.randomUUID().toString(),
        override var name: String,
        override var icon: String = "",
        override var primalCategory: QuantCategory,
        override var description: String = "Подсказка для оценки"
    ) : QuantBase(id, name, icon, primalCategory, description) {
        override fun copy(): QuantMeasure {
            return QuantMeasure(id, name, icon, primalCategory, description)
        }
    }

    fun toEvent(rate: Int, date: Long, note: String): EventBase {
        when (this) {
            is QuantRated -> {
                val bonusesList = ArrayList<EventBonusBase>()
                for (bonus in this.bonuses) {
                    bonusesList.add(bonus.toEventBonusBase(rate))
                }
                return EventBase.EventRated(
                    this.name,
                    date,
                    note,
                    rate,
                    bonusesList
                )
            }
            is QuantMeasure -> {
                return EventBase.EventMeasure(
                    this.name,
                    date,
                    note,
                    rate
                )
            }
            is QuantNote -> {
                return EventBase.EventNote(this.name, date, note)
            }
        }
    }

    fun isEqual(quant: QuantBase): Boolean {
        return id == quant.id
    }

    abstract fun copy() : QuantBase
}

sealed class QuantBonusBase(
    open var category: QuantCategory

) {
    data class QuantBonusRated(
        override var category: QuantCategory,
        var baseBonus: Double,
        var bonusForEachRating: Double
    ) : QuantBonusBase(category) {
        fun toEventBonusBase(rating: Int): EventBonusBase {
            return EventBonusBase(
                this.category,
                this.baseBonus + this.bonusForEachRating * rating
            )
        }
    }
}

enum class QuantCategory {
    Physical, Emotion, Evolution, Other, None
}