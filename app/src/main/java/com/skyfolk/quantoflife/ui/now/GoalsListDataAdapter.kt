package com.skyfolk.quantoflife.ui.now

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.skyfolk.quantoflife.R
import com.skyfolk.quantoflife.entity.GoalPresent
import com.skyfolk.quantoflife.entity.QuantBase
import com.skyfolk.quantoflife.settings.SettingsInteractor
import com.skyfolk.quantoflife.utils.format

class GoalsListDataAdapter(
    private val goalsList: ArrayList<GoalPresent>,
    private val settingsInteractor: SettingsInteractor,
    private val deleteClickListener: (GoalPresent) -> Boolean
) : RecyclerView.Adapter<GoalsListDataAdapter.ViewHolder>() {

    //this method is returning the view for each item in the list
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.goal_present, parent, false)
        return ViewHolder(v)
    }

    //this method is binding the data on the list
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItems(goalsList[position], deleteClickListener, settingsInteractor)
    }

    //this method is giving the size of the list
    override fun getItemCount(): Int {
        return goalsList.size
    }

    //the class is hodling the list view
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
            val cardView: CardView = itemView.findViewById(R.id.goal_card_view)

            titleText.text =
                "Цель - ${goalPresent.target} ${settingsInteractor.getCategoryName(goalPresent.type)} за ${goalPresent.duration.toStringName()}."

            val progress = (goalPresent.comleted / goalPresent.target) * 100
            progressBar.setProgress(progress.toInt(), true)
            progressText.text =
                "${goalPresent.comleted.format(0)} из ${goalPresent.target.format(0)}"

            val daysTotal = goalPresent.durationInDays
            Log.d("skyfolk-time","durations in days = $daysTotal")
            val dayGone = goalPresent.daysGone

            val futureProgress = goalPresent.comleted / dayGone * daysTotal
            if (futureProgress >= goalPresent.target) {
                moreText.text = "Все отлично, продолжайте в том же духе!"
            } else {
                val needToBeInTarget = (goalPresent.target - futureProgress) * dayGone / daysTotal
                moreText.text =
                    "Чтобы выполнять цель, получите сегодня еще ${needToBeInTarget.format(2)} ☆"
            }

            cardView.setOnLongClickListener {
                deleteClickListener(goalPresent)
            }
        }
    }
}