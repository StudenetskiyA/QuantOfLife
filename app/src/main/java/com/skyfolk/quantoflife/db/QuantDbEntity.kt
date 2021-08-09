package com.skyfolk.quantoflife.db

import com.skyfolk.quantoflife.entity.QuantBase
import com.skyfolk.quantoflife.entity.QuantBonusBase
import com.skyfolk.quantoflife.entity.QuantCategory
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.util.*
import kotlin.collections.ArrayList

open class QuantDbEntity(
    @PrimaryKey
    var id: String = UUID.randomUUID().toString(),
    var name: String = "",
    var icon: String = "",
    var primalCategoryDescription: String = "",
    var bonuses: RealmList<QuantBonusDbEntity> = RealmList(),
    var typeDescription: String = QuantBase.QuantNote::class.java.name,
    var description: String = "Подсказка для оценки",
    var isDeleted: Boolean = false,
    var usageCount: Int = 0
) : RealmObject() {
    companion object {
        fun toQuantDbEntity(quant: QuantBase): QuantDbEntity {
            var type = QuantBase.QuantNote::class.java.name
            val bonusList = RealmList<QuantBonusDbEntity>()
            when (quant) {
                is QuantBase.QuantRated -> {
                    type = QuantBase.QuantRated::class.java.name
                    for (bonus in quant.bonuses) {
                        bonusList.add(QuantBonusDbEntity.toQuantBonusDbEntity(bonus))
                    }
                }
                is QuantBase.QuantMeasure -> {
                    type = QuantBase.QuantMeasure::class.java.name
                }
                is QuantBase.QuantNote -> {

                }
            }

            return QuantDbEntity(
                quant.id,
                quant.name,
                quant.icon,
                quant.primalCategory.name,
                bonusList,
                type,
                quant.description
            )
        }
    }

    fun toQuantBase(): QuantBase {
        when (typeDescription) {
            QuantBase.QuantRated::class.java.name -> {
                val bonusList = ArrayList<QuantBonusBase.QuantBonusRated>()
                for (bonus in bonuses) {
                    bonusList.add(bonus.toQuantBonusRated())
                }
                return QuantBase.QuantRated(
                    id,
                    name,
                    icon,
                    QuantCategory.valueOf(primalCategoryDescription),
                    bonusList,
                    description,
                    usageCount
                )
            }
            QuantBase.QuantMeasure::class.java.name -> {
                return QuantBase.QuantMeasure(
                    id,
                    name,
                    icon,
                    QuantCategory.valueOf(primalCategoryDescription),
                    description,
                    usageCount
                )
            }
            else -> {
                return QuantBase.QuantNote(
                    id,
                    name,
                    icon,
                    QuantCategory.valueOf(primalCategoryDescription),
                    description,
                    usageCount
                )
            }
        }
    }
}

open class QuantBonusDbEntity(
    var categoryDescription: String = "",
    var baseBonus: Double = 0.0,
    var bonusForEachRating: Double = 0.0
) : RealmObject() {
    companion object {
        fun toQuantBonusDbEntity(quantBonus: QuantBonusBase.QuantBonusRated): QuantBonusDbEntity {
            return QuantBonusDbEntity(
                quantBonus.category.name,
                quantBonus.baseBonus,
                quantBonus.bonusForEachRating
            )
        }
    }

    fun toQuantBonusRated(): QuantBonusBase.QuantBonusRated {
        return QuantBonusBase.QuantBonusRated(
            QuantCategory.valueOf(categoryDescription),
            baseBonus,
            bonusForEachRating
        )
    }
}