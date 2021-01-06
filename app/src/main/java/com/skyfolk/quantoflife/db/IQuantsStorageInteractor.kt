package com.skyfolk.quantoflife.db

import com.skyfolk.quantoflife.entity.QuantBase

interface IQuantsStorageInteractor {
    fun addQuantToDB(quant: QuantBase)
    fun deleteQuant(quant: QuantBase)

    fun getAllQuantsList(includeDeleted: Boolean) : ArrayList<QuantBase>
    fun getPresetQuantsList(): ArrayList<QuantBase>

    fun alreadyHaveQuant(quant : QuantBase) : Boolean
    fun getQuantById(id: String) : QuantBase?
}