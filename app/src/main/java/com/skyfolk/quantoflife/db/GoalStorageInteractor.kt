package com.skyfolk.quantoflife.db

import com.skyfolk.quantoflife.entity.Goal

class GoalStorageInteractor(private val dbInteractor: DBInteractor): IGoalStorageInteractor {
    override fun getListOfGoals(): ArrayList<Goal> {
        val result = ArrayList<Goal>()
        val allGoalsDBEntity = dbInteractor.getDB().where(GoalDbEntity::class.java).findAll()
        for (goalDBEntity in allGoalsDBEntity) {
            result.add(goalDBEntity.toGoal())
        }

        return result
    }

    override fun addGoalToDB(goal: Goal) {
        dbInteractor.getDB().executeTransaction {
                 dbInteractor.getDB().insertOrUpdate(GoalDbEntity(goal.duration.javaClass.name, goal.target, goal.type.name, goal.id))
        }
    }

    override fun deleteGoal(goal: Goal) {
        dbInteractor.getDB().executeTransaction {
            existGoalOrNull(goal.id)?.deleteFromRealm()
        }
    }

    private fun existGoalOrNull(goalId: String): GoalDbEntity? {
        return dbInteractor.getDB().where(GoalDbEntity::class.java)
            .equalTo("id", goalId)
            .findFirst()
    }
}