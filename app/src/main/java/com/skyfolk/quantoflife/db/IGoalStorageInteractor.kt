package com.skyfolk.quantoflife.db

import com.skyfolk.quantoflife.entity.Goal

interface IGoalStorageInteractor {
    fun getListOfGoals() : ArrayList<Goal>
}