package com.skyfolk.quantoflife.db

import com.skyfolk.quantoflife.entity.Goal
import io.realm.Realm

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
        dbInteractor.getDB().executeTransactionAsync {
                 it.insertOrUpdate(GoalDbEntity(goal.duration.javaClass.name, goal.target, goal.type.name, goal.id))
        }
    }

    override fun deleteGoal(goal: Goal) {
        dbInteractor.getDB().executeTransactionAsync {
            existGoalOrNull(it, goal.id)?.deleteFromRealm()
        }
    }

    private fun existGoalOrNull(realm: Realm, goalId: String): GoalDbEntity? {
        return realm.where(GoalDbEntity::class.java)
            .equalTo("id", goalId)
            .findFirst()
    }
}