package com.skyfolk.quantoflife.db

import com.skyfolk.quantoflife.entity.Goal
import com.skyfolk.quantoflife.entity.QuantBase

interface IGoalStorageInteractor {
    fun getListOfGoals() : ArrayList<Goal>

    fun addGoalToDB(goal: Goal)
    fun deleteGoal(goal: Goal)
}