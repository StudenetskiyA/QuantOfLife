package com.skyfolk.quantoflife.db

import com.skyfolk.quantoflife.entity.QuantBase

interface IQuantsStorageInteractor {
    fun addQuantToDB(quant: QuantBase, onComplete: () -> Unit)
    fun deleteQuant(quant: QuantBase, onComplete: () -> Unit)

    fun getAllQuantsList(includeDeleted: Boolean) : ArrayList<QuantBase>

    fun alreadyHaveQuant(quant : QuantBase) : Boolean
    fun getQuantById(id: String) : QuantBase?
    fun getQuantByName(name: String) : QuantBase?
    fun incrementQuantUsage(id: String)
}