package com.skyfolk.quantoflife.ui.now

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.skyfolk.quantoflife.R
import com.skyfolk.quantoflife.entity.GoalPresent
import com.skyfolk.quantoflife.utils.format

class GoalView @JvmOverloads
constructor(context: Context,
            attrs: AttributeSet? = null,
            defStyleAttr: Int = 0)
    : FrameLayout(context, attrs, defStyleAttr) {
    private var progressBar: LinearProgressIndicator
    private var progressText: TextView
    private var moreText: TextView
    private var titleText: TextView

    init {
        val goalRootView = inflate(getContext(), R.layout.goal_present, this)
        progressBar = goalRootView.findViewById(R.id.goal_progress)
        progressText = goalRootView.findViewById(R.id.goal_progress_text)
        moreText = goalRootView.findViewById(R.id.goal_more)
        titleText = goalRootView.findViewById(R.id.goal_title)
    }

    fun updateViewState(goalPresent: GoalPresent) {
        titleText.text = "Цель - ${goalPresent.target} ☆ за неделю."

        val progress = ( goalPresent.comleted / goalPresent.target ) * 100
        progressBar.setProgress(progress.toInt(), true)
        progressText.text = "${goalPresent.comleted.format(0)} из ${goalPresent.target.format(0)}"

        val daysTotal = 7
        val dayGone = 2

        val futureProgress = goalPresent.comleted / dayGone * daysTotal
        if (futureProgress >= goalPresent.target) {
            moreText.text = "Все отлично, продолжайте в том же духе!"
        } else {
            val needToBeInTarget = (goalPresent.target - futureProgress) * dayGone / daysTotal
            moreText.text = "Чтобы выполнять цель, получите сегодня еще ${needToBeInTarget.format(2)} ☆"
        }
    }
}