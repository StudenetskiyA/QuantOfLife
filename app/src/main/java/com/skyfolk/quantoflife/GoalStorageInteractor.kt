package com.skyfolk.quantoflife

import com.skyfolk.quantoflife.db.IGoalStorageInteractor
import com.skyfolk.quantoflife.entity.Goal
import com.skyfolk.quantoflife.entity.QuantCategory
import com.skyfolk.quantoflife.ui.feeds.TimeInterval

class GoalStorageInteractor(): IGoalStorageInteractor {
    override fun getListOfGoals(): ArrayList<Goal> {
        return arrayListOf(Goal(TimeInterval.Week, 20.0, QuantCategory.All), Goal(TimeInterval.Month, 200.0, QuantCategory.Physical))
    }
}