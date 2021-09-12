package com.skyfolk.quantoflife.db

import com.skyfolk.quantoflife.entity.*
import io.realm.Realm
import kotlin.collections.ArrayList

class QuantsStorageInteractor(private val dbInteractor: DBInteractor) : IQuantsStorageInteractor {
    override fun addQuantToDB(quant: QuantBase, onComplete: () -> Unit) {
        dbInteractor.getDB().executeTransactionAsync( {
            val existQuant = existEventOrNull(it, quant)
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

    override fun getAllQuantsList(includeDeleted: Boolean): ArrayList<QuantBase> {
        val result = ArrayList<QuantBase>()
        for (r in dbInteractor
            .getDB()
            .where(QuantDbEntity::class.java)
            .findAll()
            .sortedWith(quantComparator)) {
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

    override fun getQuantByName(name: String): QuantBase? {
        return getAllQuantsList(true).find { it.name == name }
    }

    override fun incrementQuantUsage(id: String) {
        dbInteractor.getDB().executeTransactionAsync { realm ->
            realm.where(QuantDbEntity::class.java).equalTo("id", id).findFirst()?.let {
              it.usageCount++
            }
        }
    }

    private fun existEventOrNull(realm: Realm, quant: QuantBase): QuantDbEntity? {
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
