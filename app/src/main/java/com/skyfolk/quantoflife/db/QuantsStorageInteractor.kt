package com.skyfolk.quantoflife.db

import com.skyfolk.quantoflife.entity.*
import kotlin.collections.ArrayList

class QuantsStorageInteractor(private val dbInteractor: DBInteractor) : IQuantsStorageInteractor {
    override fun addQuantToDB(quant: QuantBase) {
        dbInteractor.getDB().executeTransaction {
            val existQuant = existEventOrNull(quant)
            var usageCount = 0
            if (existQuant != null) {
                usageCount = existQuant.usageCount
            }
            val quantDbEntity = QuantDbEntity.toQuantDbEntity(quant)
            quantDbEntity.usageCount = usageCount
            dbInteractor.getDB().insertOrUpdate(quantDbEntity)
        }
    }

    override fun deleteQuant(quant: QuantBase) {
        dbInteractor.getDB().executeTransaction {
            val result: QuantDbEntity =
                dbInteractor.getDB().where(QuantDbEntity::class.java).equalTo("id", quant.id)
                    .findFirst()
            result.isDeleted = true
        }
    }

    override fun getAllQuantsList(includeDeleted: Boolean): ArrayList<QuantBase> {
        val result = ArrayList<QuantBase>()
        for (r in dbInteractor.getDB().where(QuantDbEntity::class.java).findAll().sortedByDescending { it.usageCount }) {
            if (!r.isDeleted || includeDeleted) result.add(r.toQuantBase())
        }
        return result
    }

    //TODO This is bad implementation
    override fun alreadyHaveQuant(quant: QuantBase): Boolean {
        for (storedQuant in getAllQuantsList(true)) {
            if (quant.isEqual(storedQuant)) return true
        }
        return false
    }

    override fun getQuantById(id: String): QuantBase? {
        return getAllQuantsList(true).find { it.id == id }
    }

    override fun incrementQuantUsage(id: String) {
        dbInteractor.getDB().executeTransaction {
            dbInteractor.getDB().where(QuantDbEntity::class.java).equalTo("id", id).findFirst().usageCount++
        }
    }

    private fun existEventOrNull(quant: QuantBase): QuantDbEntity? {
        return dbInteractor.getDB().where(QuantDbEntity::class.java)
            .equalTo("id", quant.id)
            .findFirst()
    }

    override fun getPresetQuantsList(): ArrayList<QuantBase> {
        return ArrayList(
            listOf(
                QuantBase.QuantRated(
                    id = "1",
                    name = "Упражнения",
                    icon = "quant_gym",
                    primalCategory = QuantCategory.Physical,
                    bonuses = arrayListOf(QuantBonusBase.QuantBonusRated(QuantCategory.Physical, 0.5, 0.2)),
                    description = "Сколько пятиминуток?"
                ),

                QuantBase.QuantRated(
                    id = "2",
                    name = "Бассейн",
                    icon = "quant_swim",
                    primalCategory = QuantCategory.Physical,
                    bonuses = arrayListOf(QuantBonusBase.QuantBonusRated(QuantCategory.Physical, 1.0, 0.1)),
                    description = "Насколько выложился?"
                ),

                QuantBase.QuantMeasure(
                    id = "3",
                    name = "Вес",
                    icon = "quant_weight",
                    primalCategory = QuantCategory.Physical,
                    description = "В килограммах"
                ),
                QuantBase.QuantMeasure(
                    id = "4",
                    name = "Настроение",
                    icon = "quant_mood",
                    primalCategory = QuantCategory.Emotion,
                    description = "По десятибальной шкале"
                ),
                QuantBase.QuantRated(
                    id = "5",
                    name = "Гитара",
                    icon = "quant_guitar",
                    primalCategory = QuantCategory.Evolution,
                    bonuses = arrayListOf(
                        QuantBonusBase.QuantBonusRated(QuantCategory.Evolution, 0.3, 0.3),
                        QuantBonusBase.QuantBonusRated(QuantCategory.Emotion, 0.0, 0.3)
                    ),
                    description = "Сколько песен хорошо?"
                ),
                QuantBase.QuantRated(
                    id = "6",
                    name = "Секс",
                    icon = "quant_hearth",
                    primalCategory = QuantCategory.Emotion,
                    bonuses = arrayListOf(
                        QuantBonusBase.QuantBonusRated(QuantCategory.Physical, 1.0, 0.0),
                        QuantBonusBase.QuantBonusRated(QuantCategory.Emotion, 0.0, 0.25)
                    ),
                    description = "Насколько понравилось?"
                ),
                QuantBase.QuantRated(
                    id = "7",
                    name = "Голова болит",
                    icon = "quant_fatigue",
                    primalCategory = QuantCategory.Emotion,
                    bonuses = arrayListOf(
                        QuantBonusBase.QuantBonusRated(QuantCategory.Physical, 0.0, -0.3)
                    ),
                    description = "Интенсивность по пятибальной шкале?"
                ),
                QuantBase.QuantNote(
                    id = "8",
                    name = "Заметка",
                    icon = "quant_note",
                    primalCategory = QuantCategory.Other,
                    description = "Произвольная заметка"
                ),
                QuantBase.QuantRated(
                    id = "9",
                    name = "Танцы",
                    icon = "quant_dance",
                    primalCategory = QuantCategory.Emotion,
                    bonuses = arrayListOf(
                        QuantBonusBase.QuantBonusRated(QuantCategory.Physical, 0.5, 0.0),
                        QuantBonusBase.QuantBonusRated(QuantCategory.Emotion, 0.5, 0.25)
                    ),
                    description = "Насколько понравилось?"
                ),
                QuantBase.QuantRated(
                    id = "10",
                    name = "Дети",
                    icon = "quant_family",
                    primalCategory = QuantCategory.Emotion,
                    bonuses = arrayListOf(
                        QuantBonusBase.QuantBonusRated(QuantCategory.Emotion, 0.3, 0.25),
                        QuantBonusBase.QuantBonusRated(QuantCategory.Evolution, 0.3, 0.0)
                    ),
                    description = "Насколько понравилось общение?"
                ),
                QuantBase.QuantRated(
                    id = "11",
                    name = "Алкоголь",
                    icon = "quant_drink",
                    primalCategory = QuantCategory.Physical,
                    bonuses = arrayListOf(
                        QuantBonusBase.QuantBonusRated(QuantCategory.Physical, 0.0, -0.5),
                        QuantBonusBase.QuantBonusRated(QuantCategory.Evolution, 0.0, -0.25)
                    ),
                    description = "Звездочка за каждые 0.5 литра пивного эквивалента."
                ),
                QuantBase.QuantRated(
                    id = "12",
                    name = "Общение",
                    icon = "quant_communication",
                    primalCategory = QuantCategory.Emotion,
                    bonuses = arrayListOf(
                        QuantBonusBase.QuantBonusRated(QuantCategory.Emotion, 0.0, 0.5),
                        QuantBonusBase.QuantBonusRated(QuantCategory.Evolution, 0.0, 0.25)
                    ),
                    description = "Насколько близким было общение?"
                ),
                QuantBase.QuantRated(
                    id = "13",
                    name = "Прогулка",
                    icon = "quant_walk",
                    primalCategory = QuantCategory.Physical,
                    bonuses = arrayListOf(
                        QuantBonusBase.QuantBonusRated(QuantCategory.Physical, 0.0, 0.25)
                    ),
                    description = "Звездочка за каждый километр."
                ),
                QuantBase.QuantRated(
                    id = "14",
                    name = "Развивал мозг",
                    icon = "quant_brainstorm",
                    primalCategory = QuantCategory.Evolution,
                    bonuses = arrayListOf(
                        QuantBonusBase.QuantBonusRated(QuantCategory.Evolution, 0.0, 0.25)
                    ),
                    description = "Насколько сильно (5 - прорыв в завитии, 1 - стал чуть лучше)?"
                ),
                QuantBase.QuantRated(
                    id = "15",
                    name = "Первый раз",
                    icon = "quant_idea",
                    primalCategory = QuantCategory.Evolution,
                    bonuses = arrayListOf(
                        QuantBonusBase.QuantBonusRated(QuantCategory.Evolution, 0.0, 0.5)
                    ),
                    description = "Насколько крупный опыт?"
                )
            )
        )
    }
}
