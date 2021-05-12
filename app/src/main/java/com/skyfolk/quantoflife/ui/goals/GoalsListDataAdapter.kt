package com.skyfolk.quantoflife.ui.goals

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.skyfolk.quantoflife.R
import com.skyfolk.quantoflife.entity.GoalPresent
import com.skyfolk.quantoflife.settings.SettingsInteractor
import com.skyfolk.quantoflife.utils.format

class GoalsListDataAdapter(
    private val goalsList: ArrayList<GoalPresent>,
    private val settingsInteractor: SettingsInteractor,
    private val longClickListener: (GoalPresent) -> Boolean
) : RecyclerView.Adapter<GoalsListDataAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.goal_present, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItems(goalsList[position], longClickListener, settingsInteractor)
    }

    override fun getItemCount(): Int {
        return goalsList.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bindItems(
            goalPresent: GoalPresent,
            deleteClickListener: (GoalPresent) -> Boolean,
            settingsInteractor: SettingsInteractor
        ) {
            val progressBar: LinearProgressIndicator = itemView.findViewById(R.id.goal_progress)
            val progressText: TextView = itemView.findViewById(R.id.goal_progress_text)
            val moreText: TextView = itemView.findViewById(R.id.goal_more)
            val titleText: TextView = itemView.findViewById(R.id.goal_title)

            titleText.text =
                "Цель - ${goalPresent.target} ${settingsInteractor.getCategoryName(goalPresent.type)} за ${goalPresent.duration.toStringName()}."

            val progress = (goalPresent.comleted / goalPresent.target) * 100
            progressBar.setProgress(progress.toInt(), false)
            if (progress > 100) {
                progressText.text = "Цель выполнена!"
                moreText.visibility = View.INVISIBLE
                progressBar.setIndicatorColor(itemView.context.resources.getColor(R.color.progressCompleted, itemView.context.theme))
            } else {
                progressText.text =
                    "${goalPresent.comleted.format(0)} из ${goalPresent.target.format(0)}"
            }

            val daysTotal = goalPresent.durationInDays
            val dayGone = goalPresent.daysGone

            val futureProgress = goalPresent.comleted / dayGone * daysTotal
            if (futureProgress >= goalPresent.target) {
                moreText.text = "Все отлично, продолжайте в том же духе!"
            } else {
                val needToBeInTarget =
                    (goalPresent.target - futureProgress) * dayGone / daysTotal
                moreText.text =
                    "Чтобы выполнять цель, получите сегодня еще ${needToBeInTarget.format(2)} ☆"
            }

            itemView.setOnLongClickListener {
                deleteClickListener(goalPresent)
                true
            }
        }
    }
}