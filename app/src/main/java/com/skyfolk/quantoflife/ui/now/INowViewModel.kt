package com.skyfolk.quantoflife.ui.now

import com.skyfolk.quantoflife.entity.EventBase
import com.skyfolk.quantoflife.entity.Goal
import com.skyfolk.quantoflife.entity.QuantBase

interface INowViewModel {
    fun onEventCreated(event: EventBase)
    fun openCreateNewQuantDialog(existQuant: QuantBase?)
    fun openCreateNewGoalDialog(existGoal: Goal?)
}