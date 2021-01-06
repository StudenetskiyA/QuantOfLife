package com.skyfolk.quantoflife.ui.statistic

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.skyfolk.quantoflife.R
import com.skyfolk.quantoflife.db.IQuantsStorageInteractor
import com.skyfolk.quantoflife.entity.EventBase
import com.skyfolk.quantoflife.entity.QuantBase
import com.skyfolk.quantoflife.settings.SettingsInteractor
import com.skyfolk.quantoflife.utils.toDate
import kotlin.collections.ArrayList

class EventListDataAdapter(
    private val eventsList: ArrayList<EventBase>,
    private val quantsStorageInteractor: IQuantsStorageInteractor,
    private val settingsInteractor: SettingsInteractor,
    private val clickListener: (EventBase) -> Unit
) : RecyclerView.Adapter<EventListDataAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_event, parent, false)
        return ViewHolder(v, quantsStorageInteractor, settingsInteractor)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItems(eventsList.reversed()[position], clickListener)
    }

    override fun getItemCount(): Int {
        return eventsList.size
    }

    class ViewHolder(
        itemView: View,
        private val quantsStorageInteractor: IQuantsStorageInteractor,
        private val settingsInteractor: SettingsInteractor
    ) : RecyclerView.ViewHolder(itemView) {

        fun bindItems(event: EventBase, clickListener: (EventBase) -> Unit) {
            val eventNameView = itemView.findViewById(R.id.event_name) as TextView
            val eventImage = itemView.findViewById(R.id.quant_image) as ImageView

            val quant = quantsStorageInteractor.getAllQuantsList(true)
                .firstOrNull { it.id == event.quantId }
            eventNameView.text = quant?.name ?: "Unknown"

            if (quant != null && itemView.context.resources.getIdentifier(
                    quant.icon,
                    "drawable",
                    itemView.context.packageName
                ) != 0
            ) {
                val imageResource = itemView.context.resources.getIdentifier(
                    quant.icon,
                    "drawable",
                    itemView.context.packageName
                )
                if (imageResource != 0) {
                    eventImage.setImageResource(imageResource)
                } else {
                    eventImage.setImageResource(
                        itemView.context.resources.getIdentifier(
                            "quant_default",
                            "drawable",
                            itemView.context.packageName
                        )
                    )
                }
            }

            val eventRatingView = itemView.findViewById(R.id.event_rating) as RatingBar
            val eventDateView = itemView.findViewById(R.id.event_date) as TextView
            eventDateView.text = event.date.toDate()

            val eventBonusesView = itemView.findViewById(R.id.event_bonuses) as TextView
            when (event) {
                is EventBase.EventRated -> {
                    var result = ":"
                    val foundQuant = quantsStorageInteractor.getQuantById(event.quantId)
                    if (foundQuant is QuantBase.QuantRated) {
                        for (bonus in foundQuant.bonuses) {
                            result += " " + settingsInteractor.getCategoryName(bonus.category)
                            val value = bonus.baseBonus + bonus.bonusForEachRating * event.rate
                            result += "=$value;"
                        }
                    }
                    eventBonusesView.text = result
                    eventRatingView.rating = event.rate.toFloat()
                }
                is EventBase.EventMeasure -> {
                    eventBonusesView.text = event.value.toString()
                    eventRatingView.visibility = View.GONE
                }
                is EventBase.EventNote -> {
                    eventBonusesView.visibility = View.GONE
                    eventRatingView.visibility = View.GONE
                }
            }

            val eventNotesView = itemView.findViewById(R.id.event_notes) as TextView
            eventNotesView.text = event.note

            itemView.setOnClickListener { clickListener(event) }
        }
    }
}
