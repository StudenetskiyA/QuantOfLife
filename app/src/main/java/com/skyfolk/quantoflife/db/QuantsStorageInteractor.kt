package com.skyfolk.quantoflife.db

import com.skyfolk.quantoflife.entity.*
import io.realm.Realm

class QuantsStorageInteractor(private val dbInteractor: DBInteractor) : IQuantsStorageInteractor {
    override fun addQuantToDB(quant: QuantBase, onComplete: () -> Unit) {
        dbInteractor.getDB().executeTransactionAsync( {
            val existQuant = existQuantOrNull(it, quant)
            var usageCount = 0
            if (existQuant != null) {
                usageCount = existQuant.usageCount
            }
            val quantDbEntity = QuantDbEntity.toQuantDbEntity(quant)
            quantDbEntity.usageCount = usageCount
            it.insertOrUpdate(quantDbEntity)
        }, {
            onComplete()
        }, null)
    }

    override fun deleteQuant(quant: QuantBase, onComplete: () -> Unit) {
        dbInteractor.getDB().executeTransactionAsync( { realm ->
            realm.where(QuantDbEntity::class.java).equalTo("id", quant.id)
                    .findFirst()?.let {
                    it.isDeleted = true
                }
        }, {
            onComplete()
        }, null)
    }

    override fun getAllQuantsList(includeDeleted: Boolean): List<QuantBase> {

        return dbInteractor
            .getDB()
            .where(QuantDbEntity::class.java)
            .findAll()
            .sortedWith(quantComparator)
            .filter {
                !it.isDeleted || includeDeleted
            }
            .map {
                it.toQuantBase()
            }
    }

    override fun alreadyHaveQuant(quant: QuantBase): Boolean {

        return dbInteractor
            .getDB()
            .where(QuantDbEntity::class.java)
            .equalTo("id", quant.id)
            .findFirst() != null
    }

    override fun getQuantById(id: String): QuantBase? {

        return dbInteractor
            .getDB()
            .where(QuantDbEntity::class.java)
            .equalTo("id", id)
            .findFirst()
            ?.toQuantBase()
    }

    override fun getQuantIdByName(name: String): String? {

        return dbInteractor
            .getDB()
            .where(QuantDbEntity::class.java)
            .equalTo("name", name)
            .findAll().firstOrNull {
                !it.isDeleted
            }
            ?.id
    }

    override fun incrementQuantUsage(id: String) {
        dbInteractor.getDB().executeTransactionAsync { realm ->
            realm.where(QuantDbEntity::class.java).equalTo("id", id).findFirst()?.let {
              it.usageCount++
            }
        }
    }

    private fun existQuantOrNull(realm: Realm, quant: QuantBase): QuantDbEntity? {
        return realm.where(QuantDbEntity::class.java)
            .equalTo("id", quant.id)
            .findFirst()
    }

    private val quantComparator =  Comparator<QuantDbEntity> { entity, entity2 ->
        when {
            (entity.usageCount < entity2.usageCount) -> 1
            (entity.usageCount > entity2.usageCount) -> -1
            (entity.usageCount == entity2.usageCount) -> {
                when {
                        (entity.name > entity2.name) -> 1
                        (entity.name < entity2.name) -> -1
                    else -> 0
                }
            }
            else -> 1
        }
    }
}
