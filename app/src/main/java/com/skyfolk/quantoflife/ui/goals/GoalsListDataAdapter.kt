package com.skyfolk.quantoflife.ui.goals

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.skyfolk.quantoflife.R
import com.skyfolk.quantoflife.entity.GoalPresent
import com.skyfolk.quantoflife.settings.SettingsInteractor
import com.skyfolk.quantoflife.timeInterval.TimeInterval
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


            titleText.text = itemView.context.applicationContext.getString(
                R.string.goal_title_with_values,
                goalPresent.target,
                settingsInteractor.getCategoryName(goalPresent.type),
                goalPresent.duration.toStringName(
                    itemView.context.applicationContext.resources.getStringArray(R.array.goal_time_interval)
                )
            )

            val progress = (goalPresent.comleted / goalPresent.target) * 100
            progressBar.setProgress(progress.toInt(), false)
            if (progress > 100) {
                progressText.text =
                    itemView.context.applicationContext.getString(R.string.goal_complete)
                moreText.visibility = View.INVISIBLE
                progressBar.setIndicatorColor(
                    itemView.context.resources.getColor(
                        R.color.progressCompleted,
                        itemView.context.theme
                    )
                )
            } else {
                progressText.text = itemView.context.applicationContext.getString(
                    R.string.goal_complete_part,
                    goalPresent.comleted.format(0), goalPresent.target.format(0)
                )
            }

            val daysTotal = goalPresent.durationInDays
            val dayGone = goalPresent.daysGone

            if (goalPresent.duration != TimeInterval.All) {
                val futureProgress = goalPresent.comleted / dayGone * daysTotal
                if (futureProgress >= goalPresent.target) {
                    moreText.text =
                        itemView.context.applicationContext.getString(R.string.goal_progress_ok)
                } else {
                    val needToBeInTarget =
                        (goalPresent.target - futureProgress) * dayGone / daysTotal
                    moreText.text = itemView.context.applicationContext.getString(
                        R.string.goal_to_complete_note,
                        needToBeInTarget.format(2)
                    )
                }
            } else {
                moreText.visibility = View.INVISIBLE
            }

            itemView.setOnLongClickListener {
                deleteClickListener(goalPresent)
                true
            }
        }
    }
}