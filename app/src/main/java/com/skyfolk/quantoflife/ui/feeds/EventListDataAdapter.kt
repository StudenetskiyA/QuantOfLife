package com.skyfolk.quantoflife.ui.feeds

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.skyfolk.quantoflife.R
import com.skyfolk.quantoflife.entity.EventDisplayable
import com.skyfolk.quantoflife.entity.QuantCategory
import com.skyfolk.quantoflife.utils.toDate
import kotlin.collections.ArrayList

class EventListDataAdapter(
    private val eventsList: ArrayList<EventDisplayable>,
    private val quantCategoryNames: ArrayList<Pair<QuantCategory, String>>,
    private val clickListener: (String) -> Unit
) : RecyclerView.Adapter<EventListDataAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_event, parent, false)
        return ViewHolder(v, quantCategoryNames)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItems(eventsList.reversed()[position],
            clickListener)
    }

    override fun getItemCount(): Int {
        return eventsList.size
    }

    class ViewHolder(
        itemView: View,
        private val quantCategoryName: ArrayList<Pair<QuantCategory, String>>
    ) : RecyclerView.ViewHolder(itemView) {

        fun bindItems(event: EventDisplayable,
                      clickListener: (String) -> Unit) {
            val eventNameView = itemView.findViewById(R.id.event_name) as TextView
            val eventImage = itemView.findViewById(R.id.quant_image) as ImageView

            eventNameView.text = event.name

            if (itemView.context.resources.getIdentifier(
                    event.icon,
                    "drawable",
                    itemView.context.packageName
                ) != 0
            ) {
                val imageResource = itemView.context.resources.getIdentifier(
                    event.icon,
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

            when {
                ((event.bonuses != null) && (event.value != null)) -> {
                    var result = ":"
                    for (bonus in event.bonuses) {
                        result += " " + (quantCategoryName.firstOrNull {
                            it.first == bonus.category
                        }?.second ?: "")
                        // 0,3*3=0.899999999999 WTF??!!
                        val value = bonus.baseBonus.toBigDecimal().add(
                            bonus.bonusForEachRating.toBigDecimal()
                                .multiply(event.value.toBigDecimal())
                        )
                        result += "=$value;"
                    }
                    eventBonusesView.text = result
                    eventBonusesView.visibility = View.VISIBLE
                    eventRatingView.visibility = View.VISIBLE
                    eventRatingView.rating = event.value.toFloat()
                }
                ((event.bonuses == null) && (event.value != null)) -> {
                    eventBonusesView.text = event.value.toString()
                    eventBonusesView.visibility = View.VISIBLE
                    eventRatingView.visibility = View.GONE
                }
                ((event.bonuses == null) && (event.value == null)) -> {
                    eventBonusesView.visibility = View.GONE
                    eventRatingView.visibility = View.GONE
                }
            }

            val eventNotesView = itemView.findViewById(R.id.event_notes) as TextView
            eventNotesView.text = event.note

            itemView.setOnLongClickListener {
                clickListener(event.id)
                        Log.d("skyfolk-compose","internal click id = ${event.id}")
                true
            }
        }
    }
}
